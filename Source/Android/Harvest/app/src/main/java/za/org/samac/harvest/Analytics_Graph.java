package za.org.samac.harvest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;

import static za.org.samac.harvest.MainActivity.farmerKey;

/**
 * Radar graph for orchards
 */
public class Analytics_Graph extends AppCompatActivity {

    //Views
    private BottomNavigationView bottomNavigationView;
    private ProgressBar progressBar;
    private View graph;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private static final String TAG = "Analytics";

    //Labels for graphs
    /*
     hours: 00:00 first, 23:00 last
     */
    private static String[] labels;

    //Filters for the graph
    private static ArrayList<String> ids;
    private static double start, end;
    private static String interval;
    private static String group;

    private Data data = new Data();

    private Category category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            //Get the filters from the Bundle
            Bundle extras = getIntent().getExtras();
            ids = extras.getStringArrayList(Analytics.KEY_IDS);
            start = extras.getDouble(Analytics.KEY_START);
            end = extras.getDouble(Analytics.KEY_END);
            interval = extras.getString(Analytics.KEY_INTERVAL);
            group = extras.getString(Analytics.KEY_GROUP);
        }
        catch (java.lang.NullPointerException e){
            Log.e(TAG, "NPE from bundle");
            e.printStackTrace();
            finish();
        }

        if (interval.equals(Analytics.DAILY)){
            setContentView(R.layout.activity_spiral_graph);
            graph = findViewById(R.id.spiralChart);
        }
        else {
            setContentView(R.layout.activity_bar_graph);
            graph = findViewById(R.id.barChart);
        }

        progressBar = findViewById(R.id.progressBar);

        switch (group){
            case Analytics.FOREMAN:
                category = Category.WORKER;
                break;
            case Analytics.ORCHARD:
                category = Category.ORCHARD;
                break;
            case Analytics.WORKER:
                category = Category.WORKER;
                break;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bottomNavigationView = findViewById(R.id.BottomNav);
        bottomNavigationView.setSelectedItemId(R.id.actionSession);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                Intent openMainActivity= new Intent(Analytics_Graph.this, MainActivity.class);
                                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openMainActivity, 0);
                                return true;
                            case R.id.actionInformation:
                                Intent openInformation= new Intent(Analytics_Graph.this, InformationActivity.class);
                                openInformation.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openInformation, 0);
                                return true;
                            case R.id.actionSession:
                                return true;
                            case R.id.actionStats:
                                Intent openAnalytics= new Intent(Analytics_Graph.this, Analytics.class);
                                openAnalytics.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openAnalytics, 0);
                                return true;
                        }
                        return true;
                    }
                });
        generateAndDisplayGraph();
    }

    private void populateLabels(String interval){
        switch (interval) {
            case Analytics.HOURLY:
                //hours
                labels = new String[24];
                for (int i = 0; i < 24; i++) {
                    StringBuilder build = new StringBuilder();
                    if (i < 10) {
                        build.append("0");
                    }
                    build.append(i).append(":00");
                    labels[i] = build.toString();
                }
            break;
            //days
            //Lazy, so starts at Monday
            case Analytics.DAILY:
                labels = new String[]{
                        "Monday",
                        "Tuesday",
                        "Wednesday",
                        "Thursday",
                        "Friday",
                        "Saturday",
                        "Sunday"
                };
            break;
            //Months
            case Analytics.MONTHLY:
                labels = new String[]{
                        "January",
                        "February",
                        "March",
                        "April",
                        "May",
                        "June",
                        "July",
                        "August",
                        "September",
                        "October",
                        "November",
                        "September"
                };
            break;
        }
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
        //Group
        base.append("&groupBy=").append(group);
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

    public void generateAndDisplayGraph() {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //The content view is correct, as set in onCreate.

                        //Get the result of the function
                        String response = sendPost(urlTotalBagsPerDay(), urlParameters());
                        final JSONObject functionResult = new JSONObject(response);

                        final List<Object> allEntries = new ArrayList<>();
                        int colour = 0;

                        XAxis xAxis;

                        if (interval.equals(Analytics.DAILY)){
                            //Radar
                            JSONArray names = functionResult.names();
                            if (names != null) {
                                for (int i = 0; i < names.length(); i++) {
                                    List<RadarEntry> entries = new ArrayList<>();
                                    JSONObject object = functionResult.getJSONObject(names.get(i).toString());
                                    JSONArray innerNames = object.names();
                                    if (innerNames != null) {
                                        for (int j = 0; j < innerNames.length(); j++) {
                                            entries.add(new RadarEntry((float) object.getDouble(innerNames.get(j).toString()), innerNames.get(j).toString()));
                                        }
                                    }
                                    RadarDataSet set = new RadarDataSet(entries, data.toStringID(names.get(i).toString(), category));
                                    set.setFillColor(ColorTemplate.COLORFUL_COLORS[colour]);
                                    set.setFillAlpha(145);
                                    set.setDrawFilled(true);
                                    set.setColor(ColorTemplate.COLORFUL_COLORS[colour++]);
                                    allEntries.add(set);
                                }
                            }
                            RadarChart radarChart = (RadarChart) graph;
                            xAxis = radarChart.getXAxis();
                        }
                        else {
                            //Bar
//                            Iterator<String> iter = functionResult.keys();
                            JSONArray names = functionResult.names();
                            for (int i = 0; i < names.length(); i++){
                                List<BarEntry> entries = new ArrayList<>();
//                                JSONObject object = functionResult
                            }
//                            while (iter.hasNext()){
//                                List<BarEntry> entries = new ArrayList<>();
//                                JSONObject object = functionResult.getJSONObject(iter.toString());
//                                Iterator<String> innerIter = object.keys();
//                                while (iter.hasNext()){
//                                    entries.add(new BarEntry(Integer.parseInt(innerIter.toString()), object.getInt(innerIter.toString())));
//                                    innerIter.next();
//                                }
//                                BarDataSet set = new BarDataSet(entries, data.toStringID(iter.toString(), category));
//                                set.setColors(ColorTemplate.COLORFUL_COLORS[colour++]);
//                                allEntries.add(set);
//                                iter.next();
//                            }
//                            BarChart barChart = (BarChart) graph;
//                            xAxis = barChart.getXAxis();
                        }

                        switch (interval){
                            case Analytics.WEEKLY:
                                break;
                            case Analytics.YEARLY:
                                break;
                            default:
                                populateLabels(interval);
                                break;
                        }

//                        xAxis.setXOffset(0f);
//                        xAxis.setYOffset(0f);
//                        xAxis.setTextSize(8f);
//                        xAxis.setValueFormatter(new IAxisValueFormatter() {
//                            @Override
//                            public String getFormattedValue(float value, AxisBase axis) {
//                                return labels[(int) value % labels.length];
//                            }
//                        });

                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressBar.setVisibility(View.GONE);//put progress bar until data is retrieved from firebase
                                graph.setVisibility(View.VISIBLE);

                                if (interval.equals(Analytics.DAILY)){
                                    RadarChart spiralGraph = (RadarChart) graph;
                                    spiralGraph.animateY(1500, Easing.getEasingFunctionFromOption(Easing.EasingOption.EaseInOutCubic));
                                    RadarData radarData = new RadarData();
                                    for (Object object : allEntries){
                                        RadarDataSet set = (RadarDataSet) object;
                                        radarData.addDataSet(set);
                                    }
                                    spiralGraph.setData(radarData);
                                    spiralGraph.notifyDataSetChanged();
                                }
                                else {
                                    BarChart barChart = (BarChart) graph;
                                    barChart.animateY(1500, Easing.getEasingFunctionFromOption(Easing.EasingOption.EaseInOutCubic));
                                    BarData barData = new BarData();
                                    for (Object object : allEntries){
                                        BarDataSet set = (BarDataSet) object;
                                        barData.addDataSet(set);
                                    }
                                    barChart.setData(barData);
                                    barChart.notifyDataSetChanged();
                                }
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
                startActivity(new Intent(Analytics_Graph.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(Analytics_Graph.this, SignIn_Choose.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(Analytics_Graph.this, SignIn_Choose.class));
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
