package za.org.samac.harvest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import za.org.samac.harvest.util.AppUtil;

import static za.org.samac.harvest.MainActivity.farmerKey;

/**
 * Radar graph for orchards
 */
public class Analytics_Graph_Orchards extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference timeRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String userUid;
    private String lastSession;
    private Date latestDate;
    private String workerKey;
    private ArrayList<String> workerKeys;
    private ArrayList<String> workerName;
    private ArrayList<Integer> yield;
    private String sessionKey;
    private static String orchardKey;
    private static String orchardName;
    private Query query;
    private static final String TAG = "Analytics";
    ArrayList<RadarEntry> entries = new ArrayList<>();
    com.github.mikephil.charting.charts.PieChart pieChart;
    private ProgressBar progressBar;
    private RadarChart spiralGraph;
    private com.github.mikephil.charting.charts.RadarChart spiralGraphView;

    //Filters for the graph
    private ArrayList<String> ids;
    private double start, end;
    private String interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spiral_graph);

        try {
            //Get the filters from the Bundle
            Bundle extras = getIntent().getExtras();
            ids = extras.getStringArrayList(Analytics.KEY_IDS);
            start = extras.getDouble(Analytics.KEY_START);
            end = extras.getDouble(Analytics.KEY_END);
            interval = extras.getString(Analytics.KEY_INTERVAL);
        }
        catch (java.lang.NullPointerException e){
            Log.e(TAG, "NPE from bundle");
            e.printStackTrace();
            finish();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBar);
        spiralGraphView = findViewById(R.id.spiralChart);
        spiralGraph = (RadarChart) findViewById(R.id.spiralChart);

        bottomNavigationView = findViewById(R.id.BottomNav);
        bottomNavigationView.setSelectedItemId(R.id.actionSession);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                Intent openMainActivity= new Intent(Analytics_Graph_Orchards.this, MainActivity.class);
                                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openMainActivity, 0);
                                return true;
                            case R.id.actionInformation:
                                Intent openInformation= new Intent(Analytics_Graph_Orchards.this, InformationActivity.class);
                                openInformation.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openInformation, 0);
                                return true;
                            case R.id.actionSession:
                                return true;
                            case R.id.actionStats:
                                Intent openAnalytics= new Intent(Analytics_Graph_Orchards.this, Analytics.class);
                                openAnalytics.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openAnalytics, 0);
                                return true;
                        }
                        return true;
                    }
                });

        //Start the first fragment
        database = FirebaseDatabase.getInstance();
        userUid = user.getUid();//ID or key of the current user
        getTotalBagsPerDay();
        //displayGraph();
    }

    private static String urlTotalBagsPerDay() {
        String base = "https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/timedGraphSessions";
        return base;
    }

    private String urlParameters() {
        StringBuilder base = new StringBuilder();
        //IDs
        base.append("id0=").append(ids.get(0));
        for (int i = 1; i < ids.size(); i++){
            base.append("&id").append(i).append("=").append(ids.get(i));
        }
        //Determined by the activity, so constant.
        base.append("&groupBy=").append("orchard");
        //Interval
        base.append("&period=").append(interval);
        //Period
        base.append("&startDate=").append(start);
        base.append("&endDate=").append(end);
        //UID
        base.append("&uid=").append(farmerKey);

        return base.toString();
    }

    // HTTP POST request
    private static String sendPost(String url, String urlParameters) throws Exception {
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public void getTotalBagsPerDay() {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String response = sendPost(urlTotalBagsPerDay(), urlParameters());
                        JSONObject objs = new JSONObject(response);
                        System.out.println(" %%%%%%%%%%%%% " + response + " %%%%%%%%%%%%% " + objs.keys());
                        //put entries in graph
                        final String Sunday = "Sunday";
                        final String Monday = "Monday";
                        final String Tuesday = "Tuesday";
                        final String Wednesday = "Wednesday";
                        final String Thursday = "Thursday";
                        final String Friday = "Friday";
                        final String Saturday = "Saturday";
                        int totalSunday = 0;
                        int totalMonday = 0;
                        int totalTuesday = 0;
                        int totalWednesday = 0;
                        int totalThursday = 0;
                        int totalFriday = 0;
                        int totalSaturday = 0;
                        JSONObject objOrchard = objs.getJSONObject(orchardKey);

                        if (objOrchard.has(Sunday) == true) {
                            totalSunday = objOrchard.getInt(Sunday);
                        }
                        if (objOrchard.has(Monday) == true) {
                            totalMonday = objOrchard.getInt(Monday);
                        }
                        if (objOrchard.has(Tuesday) == true) {
                            totalTuesday = objOrchard.getInt(Tuesday);
                        }
                        if (objOrchard.has(Wednesday) == true) {
                            totalWednesday = objOrchard.getInt(Wednesday);
                        }
                        if (objOrchard.has(Thursday) == true) {
                            totalThursday = objOrchard.getInt(Thursday);
                        }
                        if (objOrchard.has(Friday) == true) {
                            totalFriday = objOrchard.getInt(Friday);
                        }
                        if (objOrchard.has(Saturday) == true) {
                            totalSaturday = objOrchard.getInt(Saturday);
                        }

                        entries.add(new RadarEntry(totalSunday, Sunday));
                        entries.add(new RadarEntry(totalMonday, Monday));
                        entries.add(new RadarEntry(totalTuesday, Tuesday));
                        entries.add(new RadarEntry(totalWednesday, Wednesday));
                        entries.add(new RadarEntry(totalThursday, Thursday));
                        entries.add(new RadarEntry(totalFriday, Friday));
                        entries.add(new RadarEntry(totalSaturday, Saturday));

                        XAxis xAxis = spiralGraph.getXAxis();
                        xAxis.setXOffset(0f);
                        xAxis.setYOffset(0f);
                        xAxis.setTextSize(8f);
                        xAxis.setValueFormatter(new IAxisValueFormatter() {

                            private String[] mFactors = new String[]{Sunday, Monday,
                                    Tuesday, Wednesday,
                                    Thursday, Friday, Saturday};

                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                return mFactors[(int) value % mFactors.length];
                            }
                        });

                        runOnUiThread(new Runnable() {
                            public void run() {
                            progressBar.setVisibility(View.GONE);//put progress bar until data is retrieved from firebase
                            spiralGraphView.setVisibility(View.VISIBLE);
                            spiralGraph.animateY(1500, Easing.getEasingFunctionFromOption(Easing.EasingOption.EaseInOutCubic));

                            RadarDataSet dataset = new RadarDataSet(entries, orchardName);
                            dataset.setFillColor(ColorTemplate.COLORFUL_COLORS[0]);
                            dataset.setFillAlpha(145);
                            dataset.setDrawFilled(true);
                            dataset.setColor(ColorTemplate.COLORFUL_COLORS[0]);

                            RadarData data = new RadarData(dataset);//labels was one of the parameters
                            spiralGraph.setData(data); // set the data and list of lables into chart

                            Description description = new Description();
                            description.setText("Orchard Performance");
                            spiralGraph.setDescription(description);  // set the description
                            spiralGraph.notifyDataSetChanged();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.search:
                return true;
            case R.id.settings:
                startActivity(new Intent(Analytics_Graph_Orchards.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(Analytics_Graph_Orchards.this, SignIn_Choose.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(Analytics_Graph_Orchards.this, SignIn_Choose.class));
                                }
                            });
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
