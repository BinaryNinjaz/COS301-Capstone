package za.org.samac.harvest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
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
import java.util.Calendar;
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
    private static ArrayList<String> ids;   //ID's for the entities to display
    private static double start, end;       //Start and end dates
    private static String interval;         //hourly, daily, weekly, monthly, yearly, -- titled period
    private static String group;            //entity type
    private static String mode;             //accumulation

    private Data data = new Data();

    static int minTime = Integer.MAX_VALUE, maxTime = Integer.MIN_VALUE;

    private Category category;
    private Context context;

    //Startup
    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics_graph);

        context = this;

        progressBar = findViewById(R.id.progressBar);
        graph = findViewById(R.id.anal_graph);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            //Get the filters from the Bundle
            Bundle extras = getIntent().getExtras();
            ids = extras.getStringArrayList(Analytics.KEY_IDS);
            start = extras.getDouble(Analytics.KEY_START);
            end = extras.getDouble(Analytics.KEY_END);
            interval = extras.getString(Analytics.KEY_INTERVAL);
            group = extras.getString(Analytics.KEY_GROUP);
            mode = extras.getString(Analytics.KEY_ACCUMULATION);
        }
        catch (java.lang.NullPointerException e){
            Log.e(TAG, "NPE from bundle");
            e.printStackTrace();
            finish();
        }

        switch (group){
            case Analytics.FARM:
                category = Category.FARM;
                break;
            case  Analytics.ORCHARD:
                category = Category.ORCHARD;
                break;
            default:
                category = Category.WORKER;
                break;
        }

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

    //Function
    private static String urlTotalBagsPerDay() {
        String base = "https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/timedGraphSessions";
        return base;
    }

    private String urlParameters() {
        StringBuilder base = new StringBuilder();
        //IDs
        for (int i = 0; i < ids.size(); i++){
            if (i != 0) {
                base.append("&id");
            }
            else {
                base.append("id");
            }
            base.append(i).append("=").append(ids.get(i));
        }
        //Group
        base.append("&groupBy=").append(group);
        //Interval
        base.append("&period=").append(interval);
        //Period
        base.append("&startDate=").append(start);
        base.append("&endDate=").append(end);
        //Accumulation
        base.append("&mode=").append(mode);
        //UID
        base.append("&uid=").append(farmerKey);

        return base.toString();
    }

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
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    //Graph
    public void generateAndDisplayGraph() {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        //Get the result of the function
                        String response = sendPost(urlTotalBagsPerDay(), urlParameters());
                        Log.i(TAG, response);
                        final JSONObject functionResult = new JSONObject(response);

                        int colour = 0;

                        final LineChart lineChart = new LineChart(context);

                        //Line always
                        List<ILineDataSet> dataSets = new ArrayList<>(); //Holds all the data sets, so one for each entity
                        JSONArray entityNames = functionResult.names(); //to iterate through the top level entities
                        if (entityNames != null){
                            for (int i = 0; i < entityNames.length(); i++){
                                JSONObject object = functionResult.getJSONObject(entityNames.get(i).toString()); // The entity's entries
                                JSONArray entryNames = object.names(); //Keys of the entries, for iteration
                                List<Entry> entries = new ArrayList<>();
                                if (entryNames != null) {
                                    LineDataSet lineDataSet;
                                    for (int j = 0; j < entryNames.length(); j++) {
                                        //Get all of the entries, buy turning the key into an int (x axis), and the value to, erm, the value (y axis)
                                        Entry entry = new Entry(getIntegerFromKey(entryNames.get(j).toString()), (float) object.getDouble(entryNames.get(j).toString()));
                                        entries.add(entry);
                                    }
                                    if (!entityNames.get(i).toString().equals("avg")) {
                                        lineDataSet = new LineDataSet(entries, data.toStringID(entityNames.get(i).toString(), category));
                                    } else {
                                        lineDataSet = new LineDataSet(entries, getResources().getString(R.string.anal_gragh_averageLabel));
                                    }
                                    lineDataSet.setColor(ColorTemplate.COLORFUL_COLORS[colour]);
                                    dataSets.add(lineDataSet);
                                }
                            }
                        }
                        //No idea what I'm doing here, or more specifically, why. I'm just following the documentation.

                        //Following settings come from Letanyan.
                        lineChart.getDescription().setEnabled(false);
                        lineChart.setDragEnabled(true);
                        lineChart.setScaleEnabled(true);
                        lineChart.setPinchZoom(true);
                        lineChart.getXAxis().setDrawGridLines(false);
                        //...
                        lineChart.getAxisRight().setDrawGridLines(false);
                        lineChart.getXAxis().setAxisMinimum(0);
                        lineChart.getAxisRight().setEnabled(false);
                        lineChart.setNoDataText(getString(R.string.anal_graph_noData));

                        populateLabels();

                        //Following settings come from John.
                        lineChart.getXAxis().setXOffset(0f);
                        lineChart.getXAxis().setYOffset(0f);
                        lineChart.getXAxis().setTextSize(8f);
                        lineChart.getXAxis().setGranularity(1f);
                        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
//                                int position = ((int)value - minTime);
                                double fpos = value / maxTime;
                                fpos *= labels.length;
                                fpos = Math.floor(fpos);
                                int position = (int) fpos;
                                if (position >= labels.length){
                                    Log.e(TAG, "Calculated label position " + position + " derived from " + value + " is greater than array of size " + labels.length + ".");
                                }
                                else if (position < 0){
                                    Log.e(TAG, "Calculated label position " + position + " derived from " + value + " is less than zero.");
                                }
                                return labels[Math.abs(position % labels.length)];
                            }
                        });

                        LineData lineData = new LineData(dataSets);

                        lineChart.animateY(1500, Easing.getEasingFunctionFromOption(Easing.EasingOption.EaseInOutCubic));

                        lineChart.setData(lineData);

                        lineChart.invalidate();

                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressBar.setVisibility(View.GONE);//put progress bar until data is retrieved from FireBase
                                graph.setVisibility(View.VISIBLE);
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

    /**
     * Depending on the interval, create a string array, so that the integers on the x axis can access the array to get what label they actually represent.
     */
    private void populateLabels(){
        switch (interval) {
            case Analytics.HOURLY:
                labels = new String[maxTime - minTime + 1];
                for (int i = 0; i <= maxTime - minTime; i++) {
                    labels[i] = String.valueOf(minTime + i) + ":00";
                }
                break;
            case Analytics.WEEKLY:
                labels = new String[maxTime - minTime + 1];
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                for (int i = minTime; i <= maxTime; i++){
                    cal.set(Calendar.WEEK_OF_YEAR, i);
                    String builder = String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) +
                            "/" +
                            cal.get(Calendar.MONTH) +
                            "/" +
                            cal.get(Calendar.YEAR);
                    labels[i] = builder;
                }
                break;
            case Analytics.DAILY:
                labels = new String[]{
                        "Sunday",
                        "Monday",
                        "Tuesday",
                        "Wednesday",
                        "Thursday",
                        "Friday",
                        "Saturday",
                };
                break;
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
                        "December",
                };
                break;
            case Analytics.YEARLY:
                labels = new String[maxTime - minTime + 1];
                for (int i = 0; i <= maxTime - minTime; i++){
                    labels[i] = Integer.toString(minTime + i);
                }
                break;
        }
    }

    /**
     * Take any key, most notably a string key, and turn it into an integer.
     * @param key the key to be converted.
     * @return the integer representing the key.
     */
    private int getIntegerFromKey(String key){
        int result = 0;
        switch (interval){
            case Analytics.HOURLY:
//                return Integer.parseInt(key);
                String[] tokens = key.replaceAll(" ", "").split(":");
                result = Integer.parseInt(tokens[0]);
                break;
            case Analytics.DAILY:
                break;
            case Analytics.WEEKLY:
                switch (key){
                    case "Sunday":
                        result =  0;
                        break;
                    case "Monday":
                        result =  1;
                        break;
                    case "Tuesday":
                        result =  2;
                        break;
                    case "Wednesday":
                        result =  3;
                        break;
                    case "Thursday":
                        result =  4;
                        break;
                    case "Friday":
                        result =  5;
                        break;
                    case "Saturday":
                        result =  6;
                }
                break;
            case Analytics.MONTHLY:
                switch (key){
                    case "January":
                        result =  0;
                        break;
                    case "February":
                        result =  1;
                        break;
                    case "March":
                        result =  2;
                        break;
                    case "April":
                        result =  3;
                        break;
                    case "May":
                        result =  4;
                        break;
                    case "June":
                        result =  5;
                        break;
                    case "July":
                        result =  6;
                        break;
                    case "August":
                        result =  7;
                        break;
                    case "September":
                        result =  8;
                        break;
                    case "October":
                        result =  9;
                        break;
                    case "November":
                        result =  10;
                        break;
                    case "December":
                        result =  11;
                }
                break;
            case Analytics.YEARLY:
                result =  Integer.parseInt(key);
                break;
        }
        if (result < minTime){
            minTime = result;
        }
        if (result > maxTime){
            maxTime = result;
        }
        return result;
    }
}
