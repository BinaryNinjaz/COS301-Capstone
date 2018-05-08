package za.org.samac.harvest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieEntry;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_graph);

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
        entries.add(new BarEntry(4f, 0));
        entries.add(new BarEntry(8f, 1));
        entries.add(new BarEntry(6f, 2));
        entries.add(new BarEntry(12f, 3));
        entries.add(new BarEntry(18f, 4));
        entries.add(new BarEntry(9f, 5));

        BarDataSet dataset = new BarDataSet(entries, "# of Calls");

        // creating labels
        ArrayList<String> labels = new ArrayList<>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");

        BarData data = new BarData(dataset);//labels was one of the parameters
        barChart.setData(data); // set the data and list of lables into chart

        Description description = new Description();
        description.setText("Description");
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

        /*database = FirebaseDatabase.getInstance();
        String userUid = user.getUid();//ID or key of the current user
        sessionKey = MainActivity.sessionKey;//get key/ID for a session
        myRef = database.getReference(userUid + "/sessions/");//path to sessions increment in Firebase

        query = myRef.limitToLast(1);

        pieChart = (com.github.mikephil.charting.charts.PieChart)findViewById(R.id.chart);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot zoneSnapshot: dataSnapshot.getChildren()) {
                    Map<String, Object> collection = (Map<String, Object>) zoneSnapshot.child("collections").getValue();
                    for (String workerID :
                            collection.keySet()) {
                        Integer yield = ((ArrayList<Object>)collection.get(workerID)).size();
                        entries.add(new PieEntry((float)yield, workerID));
                    }
                }

                PieDataSet dataset = new PieDataSet(entries, "# of Calls");
                dataset.setColors(ColorTemplate.VORDIPLOM_COLORS);

                PieData data = new PieData(dataset);//labels was one of the parameters
                pieChart.setData(data); // set the data and list of lables into chart

                Description description = new Description();
                description.setText("Description");
                pieChart.setDescription(description); // set the description
                pieChart.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        });*/
    }
}
