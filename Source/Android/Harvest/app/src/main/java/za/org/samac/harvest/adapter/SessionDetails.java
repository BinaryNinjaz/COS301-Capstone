package za.org.samac.harvest.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import za.org.samac.harvest.SignIn_Choose;
import za.org.samac.harvest.MainActivity;
import za.org.samac.harvest.R;
import za.org.samac.harvest.SessionsMap;
import za.org.samac.harvest.SettingsActivity;
import za.org.samac.harvest.SignIn_Farmer;
import za.org.samac.harvest.SignIn_SignUp;
import za.org.samac.harvest.domain.Worker;
import za.org.samac.harvest.util.AppUtil;

import static za.org.samac.harvest.MainActivity.getForemen;
import static za.org.samac.harvest.MainActivity.getWorkers;

public class SessionDetails extends AppCompatActivity {

    String key;
    String wid;
    Date startDate;
    Date endDate;
    public static collections collected;
    private ArrayList<Worker> workers;
    private HashMap<String, String> workerID;
    private ArrayList<Worker> foremen;
    private HashMap<String, String> foremenID;
    private ProgressBar progressBar;

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
    private static final String TAG = "Analytics";
    ArrayList<PieEntry> entries = new ArrayList<>();
    com.github.mikephil.charting.charts.PieChart pieChart;
    private com.github.mikephil.charting.charts.PieChart pieChartView;
    Map<String, Float> collections = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

        progressBar = findViewById(R.id.progressBar);
        //progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase
        pieChartView = findViewById(R.id.pieChart);

        collected = new collections("");

        Button mapButton = findViewById(R.id.sessionDetailsMapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent map = new Intent(getApplicationContext(), SessionsMap.class);
                //getApplicationContext().startActivity(map);
                Intent intent = new Intent(SessionDetails.this, SessionsMap.class);
                startActivity(intent);
            }
        });

        workers = getWorkers(); // get worker info to loop through it
        workerID = new HashMap<>();
        for(int i = 0 ; i < workers.size() ; ++i) {
            String id = workers.get(i).getID();
            String name = workers.get(i).getName();
            workerID.put(id, name);
        }

        foremen = getForemen(); // get worker info to loop through it
        foremenID = new HashMap<>();
        for(int i = 0 ; i < foremen.size() ; ++i) {
            String id = foremen.get(i).getID();
            String name = foremen.get(i).getName();
            foremenID.put(id, name);
        }

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference dbref = database.getReference(MainActivity.farmerKey + "/sessions/" + getIntent().getStringExtra("key"));

        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot zoneSnapshot: dataSnapshot.getChildren()) {
                    //startDate = new Date((long)(zoneSnapshot.child("start_date").getValue(Double.class)*1000));
                }
                startDate = new Date((long)(dataSnapshot.child("start_date").getValue(Double.class) * 1000));
                Double ed = dataSnapshot.child("end_date").getValue(Double.class);
                if (ed != null) {
                    endDate = new Date((long)(ed * 1000));
                } else {
                    endDate = startDate;
                }
                key = dataSnapshot.getKey();
                workerKeys = new ArrayList<>();
                workerName = new ArrayList<>();
                yield = new ArrayList<>();

                wid = dataSnapshot.child("wid").getValue(String.class);
                for (DataSnapshot childSnapshot : dataSnapshot.child("track").getChildren()) {
                    Double lat = childSnapshot.child("lat").getValue(Double.class);
                    Double lng = childSnapshot.child("lng").getValue(Double.class);
                    Location loc = new Location("");
                    loc.setLatitude(lat.doubleValue());
                    loc.setLongitude(lng.doubleValue());

                    collected.addTrack(loc);
                }
                for (DataSnapshot childSnapshot : dataSnapshot.child("collections").getChildren()) {
                    String workername = workerID.get(childSnapshot.getKey());
                    int count = 0;
                    for (DataSnapshot collection : childSnapshot.getChildren()) {
                        System.out.println(collection);
                        Double lat = collection.child("coord").child("lat").getValue(Double.class);
                        Double lng = collection.child("coord").child("lng").getValue(Double.class);
                        Location loc = new Location("");
                        loc.setLatitude(lat.doubleValue());
                        loc.setLongitude(lng.doubleValue());
                        Double time = childSnapshot.child("date").getValue(Double.class);

                        collected.addCollection(workername, loc, time);
                        count++;
                    }
                    collections.put(workername, (float) count);
                }

                displayGraph();

                TextView foremanTextView = findViewById(R.id.sessionDetailForemanTextView);
                TextView startTime = findViewById(R.id.sessionDetailStartDateTextView);
                TextView endTime = findViewById(R.id.sessionDetailEndDateTextView);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                formatter.setCalendar(Calendar.getInstance());

                String fname = foremenID.get(wid) == null ? "Farm Owner" : foremenID.get(wid);
                foremanTextView.setText("Foreman: " + fname);
                startTime.setText("Time Started: " + formatter.format(startDate));
                endTime.setText("Time Ended: " + formatter.format(endDate));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void displayGraph() {
        pieChart = (com.github.mikephil.charting.charts.PieChart)findViewById(R.id.pieChart);
        for(String key : collections.keySet()) {
            String workerName = key;
            Float yield = collections.get(workerName);
            entries.add(new PieEntry(yield, workerName));//exchange index with Worker Name
        }

        //progressBar.setVisibility(View.GONE);//put progress bar until data is retrieved from firebase
        pieChartView.setVisibility(View.VISIBLE);
        pieChart.animateY(1500, Easing.getEasingFunctionFromOption(Easing.EasingOption.EaseInOutCubic));
        pieChart.setEntryLabelColor(Color.BLACK);

        PieDataSet dataset = new PieDataSet(entries, "Dataset");
        dataset.setColors(ColorTemplate.VORDIPLOM_COLORS);
        dataset.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataset);//labels was one of the parameters
        pieChart.setData(data); // set the data and list of lables into chart

        Description description = new Description();
        description.setText("Worker Performance");
        pieChart.setDescription(description); // set the description
        pieChart.notifyDataSetChanged();
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
                startActivity(new Intent(SessionDetails.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(SessionDetails.this, SignIn_Choose.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(SessionDetails.this, SignIn_Farmer.class));
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
