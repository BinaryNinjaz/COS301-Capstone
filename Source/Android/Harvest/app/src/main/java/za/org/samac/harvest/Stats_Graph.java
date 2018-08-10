package za.org.samac.harvest;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;

import static za.org.samac.harvest.Stats.THOUSAND;
import static za.org.samac.harvest.MainActivity.farmerKey;

/**
 * Radar graph for orchards
 */
public class Stats_Graph extends AppCompatActivity {

    //Views
    private BottomNavigationView bottomNavigationView;
    private ProgressBar progressBar;
    private LineChart lineChart;

    private static final String TAG = "Stats";

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

    private Data data;

    private boolean dataFound = false;

    protected static double minTime = Double.MAX_VALUE, maxTime = Double.MIN_VALUE;

    private Category category;
    private Context context;
    private SimpleDateFormat dateFormat;

    //Startup
    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_graph);

        data = new Data();

        context = this;

        progressBar = findViewById(R.id.progressBar);
        lineChart = findViewById(R.id.stats_graph);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            //Get the filters from the Bundle
            Bundle extras = getIntent().getExtras();
            ids = extras.getStringArrayList(Stats.KEY_IDS);
            start = extras.getDouble(Stats.KEY_START);
            end = extras.getDouble(Stats.KEY_END);
            interval = extras.getString(Stats.KEY_INTERVAL);
            group = extras.getString(Stats.KEY_GROUP);
            mode = extras.getString(Stats.KEY_ACCUMULATION);
        }
        catch (java.lang.NullPointerException e){
            Log.e(TAG, "NPE from bundle");
            e.printStackTrace();
            finish();
        }

        switch (group){
            case Stats.FARM:
                category = Category.FARM;
                break;
            case  Stats.ORCHARD:
                category = Category.ORCHARD;
                break;
            default:
                category = Category.WORKER;
                break;
        }

        minTime = Double.MAX_VALUE;
        maxTime = Double.MIN_VALUE;
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
                startActivity(new Intent(Stats_Graph.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(Stats_Graph.this, SignIn_Choose.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(Stats_Graph.this, SignIn_Choose.class));
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
        //Minutes from GMT
        int minutes = (TimeZone.getDefault().getRawOffset() / 1000 / 60);
//        int minutes = 120;
        base.append("&offset=").append(minutes);
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

                        if (!mode.equals(Stats.ACCUMULATION_TIME)) {
                            setDateFormat();
                        }

                        //Get the result of the function
                        String response = sendPost(urlTotalBagsPerDay(), urlParameters());
                        Log.i(TAG, response);

                        LineData lineData = getDataFromString(response);
                        populateLabels();

                        makeGraphPretty();

                        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                return getLabel(value, axis);
                            }
                        });

                        if (dataFound) {
                            lineChart.setData(lineData);
                        }

                        runOnUiThread(new Runnable() {
                            public void run() {
                                progressBar.setVisibility(View.GONE);//put progress bar until data is retrieved from FireBase
                                lineChart.setVisibility(View.VISIBLE);
                                lineChart.animateY(1500, Easing.getEasingFunctionFromOption(Easing.EasingOption.EaseInOutCubic));
                                lineChart.invalidate();
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

    private void makeGraphPretty(){

        lineChart.getDescription().setEnabled(false);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getAxisRight().setDrawGridLines(false);
        lineChart.getXAxis().setAxisMinimum(0);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.setNoDataText(getString(R.string.stats_graph_noData));

        lineChart.getXAxis().setXOffset(0f);
        lineChart.getXAxis().setYOffset(0f);
        lineChart.getXAxis().setTextSize(8f);
        lineChart.getXAxis().setGranularity(1f);
    }

    private LineData getDataFromString(String response){
        try {
            final JSONObject functionResult = new JSONObject(response);

            int colour = 0;

            //Determine max and min values
            JSONArray entityNames = functionResult.names(); //to iterate through the top level entities
            if (entityNames != null) {
                for (int i = 0; i < entityNames.length(); i++) {
                    JSONObject object = functionResult.getJSONObject(entityNames.get(i).toString()); // The entity's entries
                    JSONArray entryNames = object.names(); //Keys of the entries, for iteration
                    if (entryNames != null) {
                        for (int j = 0; j < entryNames.length(); j++) {
                            getDoubleFromKey(entryNames.get(j).toString(), false);
                            dataFound = true;
                        }
                    }
                }
            }

            //If there's nothing, why bother?
            if (dataFound) {
                //Line always
                List<ILineDataSet> dataSets = new ArrayList<>(); //Holds all the data sets, so one for each entity
                entityNames = functionResult.names(); //to iterate through the top level entities
                if (entityNames != null) {
                    for (int i = 0; i < entityNames.length(); i++) {
                        if(entityNames.get(i).equals("exp")){
                            //Deal with this later.
                        }
                        else {
                            JSONObject object = functionResult.getJSONObject(entityNames.get(i).toString()); // The entity's entries
                            JSONArray entryNames = object.names(); //Keys of the entries, for iteration
                            List<Entry> entries = new ArrayList<>();
                            if (entryNames != null) {
                                LineDataSet lineDataSet = null;
                                for (int j = 0; j < entryNames.length(); j++) {
                                    //Get all of the entries, buy turning the key into an int (x axis), and the value to, erm, the value (y axis)
                                    Entry entry = new Entry((float) (getDoubleFromKey(entryNames.get(j).toString(), true)), (float) object.getDouble(entryNames.get(j).toString()));
                                    entries.add(entry);
                                }
                                switch (entityNames.get(i).toString()) {
                                    case "avg":
                                        lineDataSet = new LineDataSet(entries, getResources().getString(R.string.stats_gragh_averageLabel));
                                        lineDataSet.enableDashedLine(1, 1, 0);
                                        lineDataSet.setColor(getResources().getColor(R.color.grey));
                                        break;
                                    case "sum":
                                        if (mode.equals(Stats.ACCUMULATION_ENTITY)) {
                                            lineDataSet = new LineDataSet(entries, getResources().getString(R.string.stats_graph_sum));
                                            lineDataSet.setColor(getResources().getColor(R.color.blueLinks));
                                        }
                                        break;
                                    case "exp":
                                        //Oh what fun it all is.
                                        break;
                                    default:
                                        lineDataSet = new LineDataSet(entries, data.toStringID(entityNames.get(i).toString(), category));
                                        lineDataSet.setColor(ColorTemplate.COLORFUL_COLORS[colour++]);
                                        break;
                                }
                                assert lineDataSet != null;
                                lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                                lineDataSet.setDrawCircles(false);
                                lineDataSet.setLineWidth(2);
                                dataSets.add(lineDataSet);
                            }
                        }
                    }
                }
                return new LineData(dataSets);
            }
            return null;
        } catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    /*
    value is the amount of milliseconds / intervals since minTime.
     */
    public String getLabel(float value, AxisBase axisBase){
        int position;
        if (mode.equals(Stats.ACCUMULATION_TIME)) {
            if (value < 0) value = 0;
            position = (int) Math.floor((double) value);
        }
        else {
            //Need to determine where in the integer values the milliseconds best fit.
            //maxTime is the largest amount of milliseconds seen.
            double fpos = value / (maxTime - minTime);
            fpos *= labels.length;
            fpos = Math.floor(fpos);
            position = (int) fpos;
        }
        return labels[position];
    }

    /**
     * Take any key, most notably a string key, and turn it into an double.
     * @param key the key to be converted.
     * @param subMin if false, then the minimum will not be subtracted from the result, set false for initial determination of the min and max before doing anything.
     * @return the double representing the key.
     */
    public double getDoubleFromKey(String key, boolean subMin){
        double result = 0;
        if (mode.equals(Stats.ACCUMULATION_TIME)) {
            switch (interval) {
                case Stats.HOURLY:
                    String tokens[] = key.split(":");
                    result = Double.parseDouble(tokens[0]);
                    break;
                case Stats.DAILY:
                    switch (key) {
                        case "Sunday":
                            result = 0.0;
                            break;
                        case "Monday":
                            result = 1.0;
                            break;
                        case "Tuesday":
                            result = 2.0;
                            break;
                        case "Wednesday":
                            result = 3.0;
                            break;
                        case "Thursday":
                            result = 4.0;
                            break;
                        case "Friday":
                            result = 5.0;
                            break;
                        case "Saturday":
                            result = 6.0;
                    }
                    break;
                case Stats.WEEKLY:
                    result = Double.parseDouble(key);
                    break;
                case Stats.MONTHLY:
                    switch (key) {
                        case "January":
                            result = 0.0;
                            break;
                        case "February":
                            result = 1.0;
                            break;
                        case "March":
                            result = 2.0;
                            break;
                        case "April":
                            result = 3.0;
                            break;
                        case "May":
                            result = 4.0;
                            break;
                        case "June":
                            result = 5.0;
                            break;
                        case "July":
                            result = 6.0;
                            break;
                        case "August":
                            result = 7.0;
                            break;
                        case "September":
                            result = 8.0;
                            break;
                        case "October":
                            result = 9.0;
                            break;
                        case "November":
                            result = 10.0;
                            break;
                        case "December":
                            result = 11.0;
                    }
                    break;
                case Stats.YEARLY:
                    result = Double.parseDouble(key);
                    break;
            }
        }
        else {
            try {
                Date date = dateFormat.parse(key, new ParsePosition(0));
                result = (double) (date.getTime());

                Log.i(TAG, key + " parsed as " + date.toString());

                result /= THOUSAND; // Prevent loss of precision because stupid graph wants to work with floats
                //100 000 might be excessive though... Eh, it works, seemingly.

            } catch (NullPointerException e){
                Log.w(TAG, "Failed parsing key to double.");
            }
        }

        if (! subMin) {
            if (result < minTime) {
                minTime = result;
            }
            if (result > maxTime) {
                maxTime = result;
            }
        }
        else{
            result -= minTime;
        }
        return result;
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @SuppressLint("SimpleDateFormat")
    private void setDateFormat(){
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis((long) (start * THOUSAND));
        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis((long) (end * THOUSAND));
        final String fmtYear = isSameYear(startCal, endCal) ? "" : "yyyy";
        final String fmtMonth = isSameMonth(startCal, endCal) ? "" : "MMM";
        final String fmtDay = isSameDay(startCal, endCal) ? "" : "dd";
        final String fmt = (fmtYear.equals("") ? "" : fmtYear + " ") + (fmtMonth.equals("") ? "" : fmtMonth + " ") + fmtDay;
        switch (interval){
            case Stats.HOURLY:
                dateFormat = new SimpleDateFormat(fmt.equals("") ? "HH:mm" : fmt + " HH:mm");
                return;
            case Stats.DAILY:
                dateFormat = new SimpleDateFormat(fmt.equals("") ? "EEE" : fmt);
                return;
            case Stats.WEEKLY:
                dateFormat = new SimpleDateFormat(fmt.equals("") ? "EEE" : fmt);
                return;
            case Stats.MONTHLY:
                dateFormat = new SimpleDateFormat((fmtYear.equals("") ? "" : fmtYear + " ") + "MMM");
                return;
            case Stats.YEARLY:
                dateFormat = new SimpleDateFormat("yyyy");
                return;
        }
    }

    private boolean isSameYear(Calendar startCal, Calendar endCal){
        return startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR);
    }

    private boolean isSameMonth(Calendar startCal, Calendar endCal){
        return startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) && startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH);
    }

    private boolean isSameDay(Calendar startCal, Calendar endCal){
        return startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) && startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH) && startCal.get(Calendar.DAY_OF_MONTH) == endCal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Depending on the interval, create a string array, so that the integers on the x axis can access the array to get what label they actually represent.
     */
    public void populateLabels(){
        final int diffPOne = (int) (maxTime - minTime) + 1, diff = (int) (maxTime - minTime);
        if (diff < 0){
            Log.e(TAG, "DiffPOne is less than zero.");
            return;
        }
        if (mode.equals(Stats.ACCUMULATION_TIME)) {
            switch (interval) {
                case Stats.HOURLY:
                    labels = new String[diffPOne];
                    for (int i = 0; i <= diff; i++) {
                        labels[i] = String.valueOf((int) (minTime) + i) + ":00";
                    }
                    break;
                case Stats.WEEKLY:
                    labels = new String[diffPOne];

                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis((long) (start * THOUSAND));
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTimeInMillis((long) (end * THOUSAND));
                    if (!isSameYear(cal, endCal)) {
                        for (int i = 0; i < diff; i++){
                            labels[i] = String.valueOf(i + minTime);
                            //TODO: This needs to be improved
                        }
                    }
                    else {
                        cal.set(Calendar.DAY_OF_WEEK,
                                Calendar.SUNDAY //This here is the first day of week.
                        );
                        //Calendar sets up, so roll the week down.
                        cal.add(Calendar.WEEK_OF_YEAR, -1);

                        for (int i = 0; i <= diff; i++) {
                            cal.add(Calendar.WEEK_OF_YEAR, 1);
                            String builder = String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) +
                                    "/" +
                                    cal.get(Calendar.MONTH); // +
//                                "/" +
//                                cal.get(Calendar.YEAR);
                            labels[i] = builder;
                        }
                    }
                    break;
                case Stats.DAILY:
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
                case Stats.MONTHLY:
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
                case Stats.YEARLY:
                    labels = new String[diffPOne];
                    for (int i = 0; i <= diff; i++) {
                        labels[i] = Integer.toString((int) (minTime + i));
                    }
                    break;
            }
        }
        else {
            /*
            Here, the plan is to set a calendar to the start date, then keep rolling it by our interval, until it is > than our end date.
             */
            Calendar curCal = Calendar.getInstance();
            curCal.setTimeInMillis((long) (minTime * THOUSAND));

            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis((long) (maxTime * THOUSAND));

            Log.i(TAG, "LABELS: startCal: " + curCal.getTime().toString() + ", endCal: " + endCal.getTime().toString());

            List<String> labelsList = new ArrayList<>();

            while (curCal.getTimeInMillis() <= endCal.getTimeInMillis()){
                labelsList.add(dateFormat.format(curCal.getTime()));
                switch (interval){
                    case Stats.HOURLY:
                        curCal.add(Calendar.HOUR, 1);
                        break;
                    case Stats.DAILY:
                        curCal.add(Calendar.DAY_OF_MONTH, 1);
                        break;
                    case Stats.WEEKLY:
                        curCal.add(Calendar.WEEK_OF_YEAR, 1);
                        break;
                    case Stats.MONTHLY:
                        curCal.add(Calendar.MONTH, 1);
                        break;
                    case Stats.YEARLY:
                        curCal.add(Calendar.YEAR, 1);
                        break;
                }
            }
            //Turn our resizable list to standard array.
            labels = new String[labelsList.size()];
            for (int i = 0; i < labelsList.size(); i++){
                labels[i] = labelsList.get(i);
            }
        }
    }

    //Testing
    public void configureForLabelTesting(String mode, String interval, Category category){
        Stats_Graph.mode = mode;
        Stats_Graph.interval = interval;
        this.category = category;
        maxTime = Double.MIN_VALUE;
        minTime = Double.MAX_VALUE;
    }

    public SimpleDateFormat testDateFormatWith(double start, double end, String interval){
        Stats_Graph.start = start;
        Stats_Graph.end = end;
        Stats_Graph.interval = interval;
        setDateFormat();
        return dateFormat;
    }
}
