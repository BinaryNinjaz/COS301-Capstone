package za.org.samac.harvest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
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
import za.org.samac.harvest.util.ColorScheme;
import za.org.samac.harvest.util.Data;

import static za.org.samac.harvest.Stats.ACCUMULATION_TIME;
import static za.org.samac.harvest.Stats.DAILY;
import static za.org.samac.harvest.Stats.HOURLY;
import static za.org.samac.harvest.Stats.MONTHLY;
import static za.org.samac.harvest.Stats.THOUSAND;
import static za.org.samac.harvest.MainActivity.farmerKey;
import static za.org.samac.harvest.Stats.WEEKLY;
import static za.org.samac.harvest.Stats.YEARLY;

/**
 * Radar graph for orchards
 */
public class Stats_Graph extends AppCompatActivity {

    //Views
    private ProgressBar progressBar;
    private LineChart lineChart;

    private static final String TAG = "Stats";

    //Labels for graphs
    private static String[] labels;

    //Filters for the graph
    private static ArrayList<String> ids;   //ID's for the entities to display
    private static double start, end;       //Start and end dates
    private static String interval;         //hourly, daily, weekly, monthly, yearly, -- titled period
    private static String group;            //entity type
    private static String mode;             //accumulation
    private static String line;             //line type
    private static boolean average;         //true to show average line
    private static boolean expectedCondition;        //true to show expectedCondition lines

    private Boolean validExpected;

    private Data data;

    private double getYourHeadOutOfThePast = 0;
    private Calendar curCal = Calendar.getInstance();

    private SimpleDateFormat dateFormat;
    private String fmt;
    private Category category;

    private final String url = "https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/timedGraphSessions";

    //Startup
    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats_graph);

        getSupportActionBar().hide();

        data = new Data();

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
            line = extras.getString(Stats.KEY_LINE);
            expectedCondition = extras.getBoolean(Stats.KEY_EXPECTED);
            average = extras.getBoolean(Stats.KEY_AVERAGE);
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
        generateAndDisplayGraph();
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
        base.append("&startDate=").append(AppUtil.convertDate(start * THOUSAND));
        base.append("&endDate=").append(AppUtil.convertDate(end * THOUSAND));
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
                        updateDateFormat();

                        //Get the result of the function
                        String response = sendPost(url, urlParameters());
                        Log.i(TAG, "RESPONSE: " + response);

                        populateLabels();
                        LineData lineData = getDataFromString(response);

                        makeGraphPretty();

                        lineChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                return getLabel(value, axis);
                            }
                        });

                        lineChart.setData(lineData);

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
        lineChart.getXAxis().setGranularityEnabled(true);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setAxisMinimum((float) 0.0);
        lineChart.getAxisLeft().setAxisMinimum((float)0.0);

        lineChart.getLegend().setEnabled(true);
        lineChart.getLegend().setDrawInside(true);
        lineChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        lineChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        lineChart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
        lineChart.getLegend().setTextSize(8);
        lineChart.getLegend().setYOffset(10);
        lineChart.getLegend().setXOffset(10);
        lineChart.getLegend().setYEntrySpace(0);
    }

    //All function related things are done in here.
    private LineData getDataFromString(String response){
        try {
            final JSONObject functionResult = new JSONObject(response);

            //Set the expectedCondition object for the function.
            function_setExpected(functionResult.getJSONObject("exp"));

            //Set the intervals since the epoch. Really this could be done anywhere, but let's keep it tidy.
            function_updateIntervalsSinceEpochAtStart();

            int colour = 0;

            //Determine max and min values
            updateFormatDifference();

            //Line always
            List<ILineDataSet> dataSets = new ArrayList<>(); //Holds all the data sets, so one for each entity
            JSONArray tempNames = functionResult.names(); //to iterate through the top level entities
            ArrayList<Object> entityNames = new ArrayList<>();
            for (int i = 0; i < tempNames.length(); i++) {
                if (entityNames.isEmpty()) {
                    entityNames.add(tempNames.get(i));
                } else {
                    int a = ColorScheme.huePrecedence(tempNames.get(i).toString());
                    Boolean ins = false;
                    for (int j = 0; j < entityNames.size(); j++) {
                        int b = ColorScheme.huePrecedence(entityNames.get(j).toString());
                        if (a < b) {
                            entityNames.add(j, tempNames.get(i));
                            ins = true;
                            break;
                        }
                    }
                    if (!ins) {
                        entityNames.add(tempNames.get(i));
                    }
                }
            }


            for (int i = 0; i < entityNames.size(); i++) {
                //noinspection StatementWithEmptyBody
                if(entityNames.get(i).equals("exp")){
                    //This is handled elsewhere.
                }
                else if (!(entityNames.get(i).equals("avg") && !average)) {

                    //We're now working with a new entity, so let's tell the function so.
                    function_prepareForNewEntity(entityNames.get(i).toString());

                    JSONObject object = functionResult.getJSONObject(entityNames.get(i).toString()); // The entity's entries
                    Log.i(TAG, "Object: " + object.toString());
                    JSONArray entryNames = object.names(); //Keys of the entries, for iteration
                    List<Entry> entries = new ArrayList<>();
                    List<Entry> expectedEntries = new ArrayList<>(); //Expected entries.

                    double interval = (double)labels.length;
                    double diff = intervalsUptoEnd - intervalsSinceEpochAtStart;
                    double move = diff / interval;

                    double x = intervalsSinceEpochAtStart;
                    for (int lbl = 0; lbl < labels.length; lbl++) {
                        Entry entry;
                        Entry expectedEntry;
                        try {
                            entry = new Entry((float)lbl, (float)object.getDouble(labels[lbl].toString()));
                        } catch (Exception e) {
                            entry = new Entry((float) lbl, (float) 0.0);
                        }
                        entries.add(entry);

                        if (validExpected && entityNames.get(i).toString().compareTo("avg") != 0) {
                            expectedEntry = new Entry((float)lbl, (float)( a * Math.sin(b * x + c) + d));
                            expectedEntries.add(expectedEntry);
                            x += move;
                        }
                    }

                    LineDataSet lineDataSet = null;
                    LineDataSet expectedLineDataSet = null;

                    switch (entityNames.get(i).toString()) {
                        case "avg":
                            lineDataSet = new LineDataSet(entries, getResources().getString(R.string.stats_gragh_averageLabel));
                            lineDataSet.enableDashedLine(10, 10, 1);
                            lineDataSet.setColor(getResources().getColor(R.color.grey));
                            lineDataSet.setLineWidth(1);
                            break;
                        case "sum":
                            if (mode.equals(Stats.ACCUMULATION_ENTITY)) {
                                lineDataSet = new LineDataSet(entries, getResources().getString(R.string.stats_graph_sum));
                                lineDataSet.setColor(getResources().getColor(R.color.blueLinks));

                                expectedLineDataSet = new LineDataSet(expectedEntries, getResources().getString(R.string.stats_graph_sum) + " (expected)");
                                expectedLineDataSet.setColor(R.color.blueLinks, 255);
                                expectedLineDataSet.setMode(LineDataSet.Mode.LINEAR);
                                expectedLineDataSet.setDrawCircles(false);
                                expectedLineDataSet.setLineWidth(1);
                                lineDataSet.setLineWidth(2);
                            }
                            break;
                        default:
                            lineDataSet = new LineDataSet(entries, data.toStringID(entityNames.get(i).toString(), category));
                            lineDataSet.setColor(ColorScheme.hashColorOnce(entityNames.get(i).toString()));

                            expectedLineDataSet = new LineDataSet(expectedEntries, data.toStringID(entityNames.get(i).toString(), category) + " (expected)");
                            expectedLineDataSet.enableDashedLine(10, 5, 1);
                            expectedLineDataSet.setColor(ColorScheme.hashColorOnce(entityNames.get(i).toString()), 255);
                            expectedLineDataSet.setMode(LineDataSet.Mode.LINEAR);
                            expectedLineDataSet.setDrawCircles(false);
                            expectedLineDataSet.setLineWidth(1);
                            lineDataSet.setLineWidth(2);
                            break;
                    }
                    assert lineDataSet != null;
                    switch (line){
                        case Stats.LINE_CURVE:
                            lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                            break;
                        case Stats.LINE_STEP:
                            lineDataSet.setMode(LineDataSet.Mode.STEPPED);
                            break;
                        case Stats.LINE_STRAIGHT:
                            lineDataSet.setMode(LineDataSet.Mode.LINEAR);
                    }

                    lineDataSet.setDrawCircles(false);
                    if (lineDataSet.getEntryCount() > 0) {
                        dataSets.add(lineDataSet);
                    }
                    if (expectedLineDataSet != null && expectedLineDataSet.getEntryCount() > 0 && expectedCondition) {
                        dataSets.add(expectedLineDataSet);
                    }
                }
            }
            return new LineData(dataSets);
        } catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }

    /*
    value is the amount of seconds / intervals since the start of all time (1 January, 1970).
    ranges from zero to end, apparently.
     */
    public String getLabel(float value, AxisBase axisBase){
        double pc = (double)labels.length - 1;
        double max = axisBase == null || axisBase.getAxisMaximum() == 0 ? pc : axisBase.getAxisMaximum();
        int i = (int)Math.floor((double)value / max * pc);
        if (i >= 0 && i < labels.length) {
//            Log.i(TAG, "LABEL FORMAT: " + (new Integer(i)).toString() + " " + labels[i]);
            return labels[i];
        } else {
//            Log.i(TAG, "LABEL FORMAT: " + (new Integer(i)).toString());
            return "";
        }
    }

    /**
     * Take any key, most notably a string key, and turn it into an double.
     * @param key the key to be converted.
     * @return the double representing the key.
     */
    public double getDoubleFromKey(String key){
        double result = 0;
        try {
            Date date = dateFormat.parse(key, new ParsePosition(0));
            result = (double) (date.getTime());

            result /= THOUSAND; // Prevent loss of precision because stupid graph wants to work with floats
            result += getYourHeadOutOfThePast;

            date.setTime((long) (result * THOUSAND));

            Log.i(TAG, key + " parsed as " + date.toString());

        } catch (NullPointerException e){
            Log.w(TAG, "Failed parsing key to double.");
        }
    return result;
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @SuppressLint("SimpleDateFormat")
    private void updateDateFormat(){
        if (mode.equals(ACCUMULATION_TIME)){
            switch (interval){
                case HOURLY:
                    fmt = "HH";
                    break;
                case DAILY:
                    fmt = "EEEE";
                    break;
                case WEEKLY:
                    fmt = "w";
                    break;
                case MONTHLY:
                    fmt = "MMMM";
                    break;
                case YEARLY:
                    fmt = "yyyy";
                    break;
            }
        }
        else {
            Calendar startCal = Calendar.getInstance();
            startCal.setTimeInMillis((long) (start * THOUSAND));
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis((long) (end * THOUSAND));
            final String fmtYear = isSameYear(startCal, endCal) ? "" : "yyyy ";
            final String fmtMonth = isSameMonth(startCal, endCal) ? "" : "MMM ";
            final String fmtDay = isSameDay(startCal, endCal) ? "" : "dd";
            fmt = fmtYear + fmtMonth + fmtDay;
            switch (interval) {
                case Stats.HOURLY:
                    fmt = fmt + (fmt.equals("") ? "" : " ") + "HH:mm";
                    break;
                case Stats.DAILY:
                    fmt = fmt.equals("") ? "EEE" : fmt;
                    break;
                case Stats.WEEKLY:
                    fmt = fmt.equals("") ? "EEE" : fmt;
                    break;
                case Stats.MONTHLY:
                    fmt = fmtYear + "MMM";
                    break;
                case Stats.YEARLY:
                    fmt = "yyyy";
                    break;
            }
        }
        Log.i(TAG, "FORMAT: " + fmt);
        dateFormat = new SimpleDateFormat(fmt);
    }

    /**
     * updates 'getYourHeadOutOfThePast', which is to be added to formatted results to bring them forward to the asked for time.
     */
    private void updateFormatDifference() {
        Calendar diffCal = Calendar.getInstance();
        diffCal.setTimeInMillis(0);

        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis((long)(start * THOUSAND));

        int thing;
        if (mode.equals(ACCUMULATION_TIME)){

            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis((long) (end * THOUSAND));

            switch (interval){
                case HOURLY:
                    diffCal.set(startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DATE));

                    endCal.setTimeInMillis((long) (start * THOUSAND));
                    endCal.add(Calendar.DATE, 1);
                    end = endCal.getTimeInMillis() / THOUSAND;

                    break;
                case DAILY:
                    diffCal.set(Calendar.YEAR, startCal.get(Calendar.YEAR));
                    diffCal.set(Calendar.MONTH, startCal.get(Calendar.MONTH));
                    diffCal.add(Calendar.DATE, 1);

                    endCal.setTimeInMillis((long) (start * THOUSAND));
                    endCal.add(Calendar.WEEK_OF_YEAR, 1);

                    startCal.add(Calendar.DATE, 1);

                    start = startCal.getTimeInMillis() / THOUSAND;
                    end = endCal.getTimeInMillis() / THOUSAND;
                    break;
                case WEEKLY:
                    diffCal.set(Calendar.YEAR, startCal.get(Calendar.YEAR));
                    break;
                case MONTHLY:
                    diffCal.set(Calendar.YEAR, startCal.get(Calendar.YEAR));
                    break;
                case YEARLY:
                    //Do nothing
                    break;
            }
        }
        else {
            switch (interval) {
                case YEARLY:
                    //do nothing, always yyyy
                    break;
                case MONTHLY:
                    if (!fmt.contains("yyyy")) {
                        thing = Calendar.YEAR;
                        diffCal.set(thing, startCal.get(thing));
                    }
                    break;
                case WEEKLY:
                    if (!fmt.contains("yyyy")) {
                        thing = Calendar.YEAR;
                        diffCal.set(thing, startCal.get(thing));
                    }
                    if (!fmt.contains("MMM")) {
                        thing = Calendar.MONTH;
                        diffCal.set(thing, startCal.get(thing));
                    }
                    break;
                case DAILY:
                    if (!fmt.contains("yyyy")) {
                        thing = Calendar.YEAR;
                        diffCal.set(thing, startCal.get(thing));
                    }
                    if (!fmt.contains("MMM")) {
                        thing = Calendar.MONTH;
                        diffCal.set(thing, startCal.get(thing));
                    }
                    break;
                case HOURLY:
                    if (!fmt.contains("yyyy")) {
                        thing = Calendar.YEAR;
                        diffCal.set(thing, startCal.get(thing));
                    }
                    if (!fmt.contains("MMM")) {
                        thing = Calendar.MONTH;
                        diffCal.set(thing, startCal.get(thing));
                    }
                    if (!fmt.contains("dd")) {
                        thing = Calendar.DATE;
                        diffCal.set(thing, startCal.get(thing));
                    }
            }
        }

        getYourHeadOutOfThePast = diffCal.getTimeInMillis() / THOUSAND;
    }

    /**
     * Depending on the interval, create a string array, so that the integers on the x axis can access the array to get what label they actually represent.
     */
    public void populateLabels(){
        /*
        Here, the plan is to set a calendar to the start date, then keep adding to it by our interval, until it is > than our end date.
         */

        Calendar curCal = Calendar.getInstance();
        curCal.setTimeInMillis((long) (start * THOUSAND));

        Calendar endCal = Calendar.getInstance();
        endCal.setTimeInMillis((long) (end * THOUSAND));

        if (mode.equals(ACCUMULATION_TIME)){
            switch (interval){
                case HOURLY:
                    endCal.setTimeInMillis((long) (start * THOUSAND));
                    endCal.add(Calendar.DATE, 1);
                    break;
                case DAILY:
                    endCal.setTimeInMillis((long) (start * THOUSAND));
                    endCal.add(Calendar.WEEK_OF_YEAR, 1);
                    curCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                    endCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                    break;
            }
        }

        Log.i(TAG, "LABELS: startCal: " + curCal.getTime().toString() + ", endCal: " + endCal.getTime().toString());

        List<String> labelsList = new ArrayList<>();

        while (curCal.getTimeInMillis() < endCal.getTimeInMillis()){
            labelsList.add(dateFormat.format(curCal.getTime()));
            switch (interval){
                case Stats.HOURLY:
                    curCal.add(Calendar.HOUR_OF_DAY, 1);
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

    /**
     * Get a double representing the next key.<br>
     *
     * @return the next key.
     */
    private Double getNextKey(){
        Calendar maxCal = Calendar.getInstance();
        maxCal.setTimeInMillis((long) (end * THOUSAND));
        if (curCal.getTimeInMillis() > maxCal.getTimeInMillis()){
            return null;
        }
        else {
            Double result = curCal.getTimeInMillis() / THOUSAND;
            switch (interval){
                case HOURLY:
                    curCal.add(Calendar.HOUR_OF_DAY, 1);
                    break;
                case DAILY:
                    curCal.add(Calendar.DATE, 1);
                    break;
                case WEEKLY:
                    curCal.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case MONTHLY:
                    curCal.add(Calendar.MONTH, 1);
                    break;
                case YEARLY:
                    curCal.add(Calendar.YEAR, 1);
                    break;
            }
            return result;
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

    //Testing
    public void configureForLabelTesting(String mode, String interval, Category category){
        Stats_Graph.mode = mode;
        Stats_Graph.interval = interval;
        this.category = category;
    }

    public SimpleDateFormat testDateFormatWith(double start, double end, String interval, String mode){
        Stats_Graph.start = start;
        Stats_Graph.end = end;
        Stats_Graph.interval = interval;
        Stats_Graph.mode = mode;
        updateDateFormat();
        return dateFormat;
    }

    public SimpleDateFormat testDateFormatWith(double start, double end, String interval){
        return testDateFormatWith(start, end, interval, Stats.ACCUMULATION_NONE);
    }

    //Expected
    /**
    *  Here it is:
    *   grab a reference to the exp JSONArray at the very beginning
    *   calculate the number of intervals since the epoch to the start
    *   when a new entity is being processed, grab its relevant stuff from the exp array and generate the function
    *   every time it is processed, return the y, and increment the x, which is reset when a new entity comes into play.
    *
    *   a * sin(b * x + c) + d
    *   {avg: {*: #}, id0: {*: #, *: #, ...}, id1: {*: #, *: #, ...}, exp: {*: {a: #, b: #, c: #, d: #}, ...}}
    */

    private double a = 0, b = 0, c = 0, d = 0; //For the function.
    private int intervalsSinceStart = 0, // The number of intervals since the requested start for this graph.
            intervalsSinceEpochAtStart = 0, // The number of intervals since the epoch, to the requested start of this graph.
            intervalsUptoEnd = 0;
    JSONObject expected;

    /**
     * Simply set the expectedCondition JSONObject, which is used to create functions. Should be called only once.
     * @param expected the JSONObject. Format of *: {a: #, b: #, c: #, d: #}, ...
     */
    private void function_setExpected(JSONObject expected){
        this.expected = expected;
    }

    /**
     * Update the intervals to the start. This should be done only once. This value is built upon to determine x values.<br>
     *     start and interval <em>must</em> be set before calling this.
     */
    private void function_updateIntervalsSinceEpochAtStart(){
        /*
        Plan is simple, calendar for the very start of all things, increment it by the interval, until it no longer <= to start.
        Capture the number of increments.
         */

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(0); // It's the start of all things.

        Log.i(TAG, "Function: " + calendar.getTime().toString());
        while ((calendar.getTimeInMillis() / THOUSAND) <= end) {
            if ((calendar.getTimeInMillis() / THOUSAND) <= start) {
                intervalsSinceEpochAtStart++;
            }
            intervalsUptoEnd++;
            switch (interval) {
                case HOURLY:
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                    break;
                case DAILY:
                    calendar.add(Calendar.DATE, 1);
                    break;
                case WEEKLY:
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                    break;
                case MONTHLY:
                    calendar.add(Calendar.MONTH, 1);
                    break;
                case YEARLY:
                    calendar.add(Calendar.YEAR, 1);
                    break;
            }
        }
    }

    /**
     * Reset the interval (x) counter, and update the a, b, c, and d values.
     * @param id the id of the new entity.
     */
    private void function_prepareForNewEntity(String id){
        try {

            JSONObject targetedExpected = expected.getJSONObject(id);
            a = targetedExpected.getDouble("a");
            b = targetedExpected.getDouble("b");
            c = targetedExpected.getDouble("c");
            d = targetedExpected.getDouble("d");

            intervalsSinceStart = 0;
            validExpected = true;

        } catch (JSONException e) {
            validExpected = false;
            if (!id.equals("avg")) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get a y value.<br>
     *     Updates x at the end, so it can simply be called again.
     * @return float representing the y value.
     */
    private float function_getNextY(){
        return (float) (
                a * Math.sin(b * (intervalsSinceStart++ + intervalsSinceEpochAtStart) + c) + d
        );
    }
}
