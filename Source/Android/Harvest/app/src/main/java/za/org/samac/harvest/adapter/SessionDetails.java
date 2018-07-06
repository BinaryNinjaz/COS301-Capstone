package za.org.samac.harvest.adapter;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
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

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.org.samac.harvest.Analytics;
import za.org.samac.harvest.InformationActivity;
import za.org.samac.harvest.LoginActivity;
import za.org.samac.harvest.MainActivity;
import za.org.samac.harvest.R;
import za.org.samac.harvest.Sessions;
import za.org.samac.harvest.SessionsMap;
import za.org.samac.harvest.SignUpActivity;
import za.org.samac.harvest.domain.Worker;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase
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
                displayGraph();
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
                    for (DataSnapshot collection : childSnapshot.getChildren()) {
                        System.out.println(collection);
                        Double lat = collection.child("coord").child("lat").getValue(Double.class);
                        Double lng = collection.child("coord").child("lng").getValue(Double.class);
                        Location loc = new Location("");
                        loc.setLatitude(lat.doubleValue());
                        loc.setLongitude(lng.doubleValue());
                        Double time = childSnapshot.child("date").getValue(Double.class);

                        collected.addCollection(workername, loc, time);
                    }
                }

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
        database = FirebaseDatabase.getInstance();
        userUid = user.getUid();//ID or key of the current user
        myRef = database.getReference(MainActivity.farmerKey + "/sessions/" + key);//path to sessions increment in Firebase

        //query = myRef.limitToLast(1);

        pieChart = (com.github.mikephil.charting.charts.PieChart)findViewById(R.id.pieChart);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot zoneSnapshot: dataSnapshot.getChildren()) {
                    //List<Object> collections = (List<Object>) zoneSnapshot.child("collections").getValue();
                    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$ "+zoneSnapshot.child("start_date").toString()+" $$$$$$$$$$$$$$$$$$$$$$$");
                    Map<String, Object> collections = (Map<String, Object>) zoneSnapshot.child("collections").getValue();

                    if (collections == null) {
                        //no graph to show
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

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
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
}
