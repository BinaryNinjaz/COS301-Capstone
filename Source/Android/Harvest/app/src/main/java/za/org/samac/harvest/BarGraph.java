package za.org.samac.harvest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

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
    private String workerKey;
    private ArrayList<String> workerKeys;
    private ArrayList<String> workerName;
    private ArrayList<Integer> yield;
    private String sessionKey;
    private static String orchardKey;
    private Query query;
    private static final String TAG = "Analytics";
    ArrayList<PieEntry> entries = new ArrayList<>();
    com.github.mikephil.charting.charts.PieChart pieChart;
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
        orchardKey = getIntent().getStringExtra("key");
        //getTotalBagsPerDay();
        displayGraph();
    }

    private static String urlTotalBagsPerDay() {
        String base = "https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/timedGraphSessions";
        return base;
    }

    private static String urlParameters() {
        String base = "";
        base = base + "id0=" + orchardKey;
        base = base + "&groupBy=" + "orchard";//get correct orchard ID
        base = base + "&period=" + "daily";
        double currentTime;
        double divideBy1000Var = 1000.0000000;
        currentTime = (System.currentTimeMillis()/divideBy1000Var);
        base = base + "&startDate=" + (currentTime - 7 * 24 * 60 * 60);
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

                        ArrayList<BarEntry> entries = new ArrayList<>();
                        entries.add(new BarEntry(7, 7));
                        entries.add(new BarEntry(8, 15));
                        entries.add(new BarEntry(9, 11));
                        entries.add(new BarEntry(10, 20));
                        entries.add(new BarEntry(11, 34));

                        progressBar.setVisibility(View.GONE);//put progress bar until data is retrieved from firebase
                        barGraphView.setVisibility(View.VISIBLE);

                        BarDataSet dataset = new BarDataSet(entries, "Dataset");
                        dataset.setColors(ColorTemplate.VORDIPLOM_COLORS);

                        BarData data = new BarData(dataset);//labels was one of the parameters
                        barChart.setData(data); // set the data and list of lables into chart

                        Description description = new Description();
                        description.setText("Orchard Performance");
                        barChart.setDescription(description);  // set the description

                        for (int i = 0; i < objs.length(); i++) {
                            objs.get(objs.keys().toString());
                            System.out.println(" *************** " + objs.get(objs.keys().toString()) + " *************** ");

                        }


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

    public void displayGraph() {
        timeRef = database.getReference(userUid + "/sessions/");//path to sessions increment in Firebase
        Query firstQuery = timeRef.limitToLast(1);
        firstQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot zoneSnapshot: dataSnapshot.getChildren()) {
                    lastSession = zoneSnapshot.getKey();
                    double startDate = zoneSnapshot.child("start_date").getValue(double.class);
                    latestDate = new Date((long) startDate);
                    //SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy"); // the format of date
                    //latestDate = sdf.format(date);
                }

                //TODO: get last 5 dates
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        });

        // To make vertical bar chart, initialize graph id this way
        BarChart barChart = (BarChart) findViewById(R.id.barChart);

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(7, 7));
        entries.add(new BarEntry(8, 15));
        entries.add(new BarEntry(9, 11));
        entries.add(new BarEntry(10, 20));
        entries.add(new BarEntry(11, 34));

        progressBar.setVisibility(View.GONE);//put progress bar until data is retrieved from firebase
        barGraphView.setVisibility(View.VISIBLE);

        BarDataSet dataset = new BarDataSet(entries, "Dataset");
        dataset.setColors(ColorTemplate.VORDIPLOM_COLORS);

        BarData data = new BarData(dataset);//labels was one of the parameters
        barChart.setData(data); // set the data and list of lables into chart

        Description description = new Description();
        description.setText("Orchard Performance");
        barChart.setDescription(description);  // set the description

        /*LineChart lineChart = (LineChart) findViewById(R.id.chart);
        // creating list of entry
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 4f));
        entries.add(new Entry(1, 8f));
        entries.add(new Entry(1, 8f));
        entries.add(new Entry(1, 8f));
        entries.add(new Entry(1, 8f));
        entries.add(new Entry(1, 8f));
        entries.add(new Entry(1, 8f));

        LineDataSet dataset = new LineDataSet(entries, "# of Calls");

        // creating labels
        ArrayList<String> labels = new ArrayList<>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");

        LineData data = new LineData(dataset);
        lineChart.setData(data); // set the data and list of lables into chart

        Description description = new Description();
        description.setText("Description");
        lineChart.setDescription(description); */ // set the description
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
                    startActivity(new Intent(BarGraph.this, LoginActivity.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
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
