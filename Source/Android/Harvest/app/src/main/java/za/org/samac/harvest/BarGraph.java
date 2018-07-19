package za.org.samac.harvest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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

public class BarGraph extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private DatabaseReference timeRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String userUid;
    private String lastSession;
    private Date latestDate;
    private static String workerKey;
    private static String workerName;
    private static final String TAG = "Analytics";
    private ArrayList<BarEntry> entries = new ArrayList<>();
    private com.github.mikephil.charting.charts.PieChart pieChart;
    private ProgressBar progressBar;
    private BarChart barChart;
    private com.github.mikephil.charting.charts.BarChart barGraphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_graph);

        progressBar = findViewById(R.id.progressBar);
        barGraphView = findViewById(R.id.barChart);
        barChart = (BarChart) findViewById(R.id.barChart);

        bottomNavigationView = findViewById(R.id.BottomNav);
        bottomNavigationView.setSelectedItemId(R.id.actionSession);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                Intent openMainActivity= new Intent(BarGraph.this, MainActivity.class);
                                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openMainActivity, 0);
                                return true;
                            case R.id.actionInformation:
                                Intent openInformation= new Intent(BarGraph.this, InformationActivity.class);
                                openInformation.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openInformation, 0);
                                return true;
                            case R.id.actionSession:
                                return true;
                            case R.id.actionStats:
                                Intent openAnalytics= new Intent(BarGraph.this, Analytics.class);
                                openAnalytics.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openAnalytics, 0);
                                return true;
                        }
                        return true;
                    }
                });

        //Start the first fragment
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        database = FirebaseDatabase.getInstance();
        userUid = user.getUid();//ID or key of the current user
        workerKey = getIntent().getStringExtra("key");
        workerName = getIntent().getStringExtra("name");
        getTotalBagsPerDay();
        //displayGraph();
    }

    private static String urlTotalBagsPerDay() {
        String base = "https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/timedGraphSessions";
        return base;
    }

    private static String urlParameters() {
        String base = "";
        base = base + "id0=" + workerKey;
        base = base + "&groupBy=" + "worker";
        base = base + "&period=" + "hourly";
        double currentTime;
        double divideBy1000Var = 1000.0000000;
        currentTime = (System.currentTimeMillis()/divideBy1000Var);
        base = base + "&startDate=" + (currentTime - 60 * 13);
        base = base + "&endDate=" + currentTime;
        base = base + "&uid=" + farmerKey;

        return base;
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
                        final ArrayList<String> time = new ArrayList<>();
                        //time received is 2 hour behind so ahd to compensate
                        time.add("4");
                        time.add("5");
                        time.add("6");
                        time.add("7");
                        time.add("8");
                        time.add("9");
                        time.add("10");
                        time.add("11");
                        time.add("12");
                        time.add("13");
                        time.add("14");
                        time.add("15");
                        time.add("16");
                        JSONObject objWorker = objs.getJSONObject(workerKey);

                        final ArrayList<Integer> total = new ArrayList<>();

                        for (int i = 0; i < time.size(); i++) {
                            if (objWorker.has(time.get(i)) == true) {
                                total.add(objWorker.getInt(time.get(i)));
                            } else {
                                total.add(0);
                            }

                            entries.add(new BarEntry(i, total.get(i)));
                        }

                        XAxis xAxis = barChart.getXAxis();
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setXOffset(0f);
                        xAxis.setYOffset(0f);
                        xAxis.setTextSize(8f);
                        xAxis.setValueFormatter(new IAxisValueFormatter() {

                            private String[] mFactors = new String[]{"6am", "7am", "8am", "9am", "10am", "11am", "12pm", "1pm", "2pm", "3pm", "4pm", "5pm", "6pm"};

                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                return mFactors[(int) value % mFactors.length];
                            }
                        });

                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressBar.setVisibility(View.GONE);//put progress bar until data is retrieved from firebase
                                barGraphView.setVisibility(View.VISIBLE);
                                barChart.animateY(1500, Easing.getEasingFunctionFromOption(Easing.EasingOption.EaseInOutCubic));

                                BarDataSet dataset = new BarDataSet(entries, workerName);
                                dataset.setColors(ColorTemplate.COLORFUL_COLORS);

                                BarData data = new BarData(dataset);//labels was one of the parameters
                                barChart.setData(data); // set the data and list of lables into chart

                                Description description = new Description();
                                description.setText("Per Hour Worker Performance");
                                barChart.setDescription(description);  // set the description
                                barChart.notifyDataSetChanged();
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

    //Handle the menu
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
                startActivity(new Intent(BarGraph.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(BarGraph.this, SignIn_Choose.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                // Google sign out
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(BarGraph.this, SignIn_Choose.class));
                                }
                            });
                }
                finish();
                return true;
//            case R.id.homeAsUp:
//                onBackPressed();
//                return true;
            default:
                super.onOptionsItemSelected(item);
                return true;
        }
//        return false;
    }
}
