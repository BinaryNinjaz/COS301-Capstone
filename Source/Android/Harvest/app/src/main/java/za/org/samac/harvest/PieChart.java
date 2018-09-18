package za.org.samac.harvest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class PieChart extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private String userUid;
    private String workerKey;
    private ArrayList<String> workerKeys;
    private ArrayList<String> workerName;
    private ArrayList<Integer> yield;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Query query;
    private static final String TAG = "Stats";
    ArrayList<PieEntry> entries = new ArrayList<>();
    com.github.mikephil.charting.charts.PieChart pieChart;
    private ProgressBar progressBar;
    private com.github.mikephil.charting.charts.PieChart pieChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart);

        progressBar = findViewById(R.id.progressBar);
        pieChartView = findViewById(R.id.pieChart);

        bottomNavigationView = findViewById(R.id.BottomNav);
        bottomNavigationView.setSelectedItemId(R.id.actionSession);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                Intent openMainActivity= new Intent(PieChart.this, MainActivity.class);
                                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openMainActivity, 0);
                                return true;
                            case R.id.actionInformation:
                                Intent openInformation= new Intent(PieChart.this, InformationActivity.class);
                                openInformation.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openInformation, 0);
                                return true;
                            case R.id.actionSession:
                                return true;
                            case R.id.actionStats:
                                Intent openPieChart= new Intent(PieChart.this, Stats.class);
                                openPieChart.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openPieChart, 0);
                                return true;

                        }
                        return true;
                    }
                });

        //Start the first fragment
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //super.onOptionsItemSelected(item);
        workerKeys = new ArrayList<>();
        workerName = new ArrayList<>();
        yield = new ArrayList<>();
        displayGraph();
    }

    public void displayGraph() {
        database = FirebaseDatabase.getInstance();
        userUid = user.getUid();//ID or key of the current user
        myRef = database.getReference(userUid + "/sessions/");//path to sessions increment in Firebase

        query = myRef.limitToLast(1);

        pieChart = (com.github.mikephil.charting.charts.PieChart)findViewById(R.id.pieChart);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot zoneSnapshot: dataSnapshot.getChildren()) {
                    //List<Object> collections = (List<Object>) zoneSnapshot.child("collections").getValue();
                    Map<String, Object> collections = (Map<String, Object>) zoneSnapshot.child("collections").getValue();

                    if (collections.equals(null)) {

                    }

                    for(String key : collections.keySet()) {
                        Object workerId = collections.get(key);

                        if(workerId != null) {
                            workerKeys.add(key);
                            yield.add(((ArrayList)workerId).size());
                        }
                    }
                }

                getWorkerNames();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    public void getWorkerNames() {
        DatabaseReference ref = database.getReference(userUid + "/workers/");//path to workers increment in Firebase

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (int i = 0; i<workerKeys.size(); i++) {
                    workerKey = workerKeys.get(i);
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (workerKey.equals(child.getKey())) {
                            workerName.add(child.child("name").getValue(String.class) + " " + child.child("surname").getValue(String.class));
                            break;
                        }
                    }
                }

                if (workerName.size() == workerKeys.size()) {
                    //put labels on chart
                    for (int i = 0; i<workerName.size(); i++) {
                        entries.add(new PieEntry((float)yield.get(i), workerName.get(i)));//exchange index with Worker Name
                    }

                    progressBar.setVisibility(View.GONE);//put progress bar until data is retrieved from firebase
                    pieChartView.setVisibility(View.VISIBLE);

                    PieDataSet dataset = new PieDataSet(entries, "Dataset");
                    dataset.setColors(ColorTemplate.VORDIPLOM_COLORS);

                    PieData data = new PieData(dataset);//labels was one of the parameters
                    pieChart.setData(data); // set the data and list of lables into chart

                    Description description = new Description();
                    description.setText("Worker Performance");
                    pieChart.setDescription(description); // set the description
                    pieChart.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.search:
                return true;
            case R.id.settings:
                startActivity(new Intent(PieChart.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(PieChart.this, SignIn_Farmer.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(PieChart.this, SignIn_Choose.class));
                                }
                            });
                }
                finish();
                return true;
//            case R.id.homeAsUp:
//                onBackPressed();
//                return true;
            case R.id.website:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://harvestapp.co.za/"));
                startActivity(browserIntent);
                return true;
            default:
                super.onOptionsItemSelected(item);
                return true;
        }
//        return false;
    }
}
