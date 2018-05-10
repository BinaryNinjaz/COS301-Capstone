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

import java.util.ArrayList;
import java.util.Map;

import za.org.samac.harvest.util.AppUtil;

public class BarGraph extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String sessionKey;
    private Query query;
    private static final String TAG = "Analytics";
    ArrayList<PieEntry> entries = new ArrayList<>();
    com.github.mikephil.charting.charts.PieChart pieChart;
    private ProgressBar progressBar;
    private com.github.mikephil.charting.charts.BarChart barGraphView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_graph);

        progressBar = findViewById(R.id.progressBar);
        barGraphView = findViewById(R.id.barChart);

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
        displayGraph();
    }

    public void displayGraph() {
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
