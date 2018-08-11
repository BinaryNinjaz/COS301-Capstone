package za.org.samac.harvest.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaCas;
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
import android.widget.LinearLayout;
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

import za.org.samac.harvest.Stats;
import za.org.samac.harvest.BottomNavigationViewHelper;
import za.org.samac.harvest.InformationActivity;
import za.org.samac.harvest.Sessions;
import za.org.samac.harvest.SignIn_Choose;
import za.org.samac.harvest.MainActivity;
import za.org.samac.harvest.R;
import za.org.samac.harvest.SessionsMap;
import za.org.samac.harvest.SettingsActivity;
import za.org.samac.harvest.SignIn_Farmer;
import za.org.samac.harvest.SignIn_SignUp;
import za.org.samac.harvest.Stats;
import za.org.samac.harvest.domain.Worker;
import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.SearchedItem;

import static za.org.samac.harvest.MainActivity.getForemen;
import static za.org.samac.harvest.MainActivity.getWorkers;

public class SessionDetails extends AppCompatActivity {

    String key;
    String foreman;
    Date startDate;
    Date endDate;
    private ArrayList<Worker> workers;
    private HashMap<String, String> workerID;
    private ArrayList<Worker> foremen;
    private HashMap<String, String> foremenID;
    private ProgressBar progressBar;
    private LinearLayout linearLayoutSessDetails;

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
    private com.github.mikephil.charting.charts.PieChart pieChartView;
    Map<String, Float> collections = new HashMap<>();
    private Button deleteSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBar);
        linearLayoutSessDetails = findViewById(R.id.linearLayoutSessDetails);
        progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase
        linearLayoutSessDetails.setVisibility(View.GONE);
        pieChartView = findViewById(R.id.pieChart);
        deleteSession = findViewById(R.id.deleteSession);

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
            System.out.println(id + " " + name);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        formatter.setCalendar(Calendar.getInstance());

        startDate = Sessions.selectedItem.startDate;
        endDate = Sessions.selectedItem.endDate;
        foreman = Sessions.selectedItem.foreman;
        key = Sessions.selectedItem.key;

        TextView foremanTextView = findViewById(R.id.sessionDetailForemanTextView);
        TextView startTime = findViewById(R.id.sessionDetailStartDateTextView);
        TextView endTime = findViewById(R.id.sessionDetailEndDateTextView);

        foremanTextView.setText(foreman);
        startTime.setText(formatter.format(startDate));
        endTime.setText(formatter.format(endDate));

        //bottom nav bar
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.actionSession);
        BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                Intent openMainActivity= new Intent(SessionDetails.this, MainActivity.class);
                                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openMainActivity, 0);
                                return true;
                            case R.id.actionInformation:
                                startActivity(new Intent(SessionDetails.this, InformationActivity.class));
                                return true;
                            case R.id.actionSession:
                                return true;
                            case R.id.actionStats:
                                startActivity(new Intent(SessionDetails.this, Stats.class));
                                return true;
                        }
                        return true;
                    }
                });


        deleteSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(SessionDetails.this);
                builder.setMessage("Delete session?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                DatabaseReference myRef;
                                database = FirebaseDatabase.getInstance();
                                myRef = database.getReference(MainActivity.farmerKey + "/sessions/" + key);//path to sessions increment in Firebase
                                myRef.removeValue();//remove latest increment
                                dialog.dismiss();
                                Intent intent = new Intent(SessionDetails.this, Sessions.class);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alert = builder.create();
                alert.show();
            }
        });

        displayGraph();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.actionSession);//set correct item to pop out on the nav bar
        }
    }

    public void displayGraph() {
        pieChart = (com.github.mikephil.charting.charts.PieChart)findViewById(R.id.pieChart);
        for(String key : Sessions.selectedItem.collectionPoints.keySet()) {
            String workerName = Sessions.selectedItem.collectionPoints.get(key).get(0).workerName;
            Float yield = (float)Sessions.selectedItem.collectionPoints.get(key).size();
            entries.add(new PieEntry(yield, workerName));//exchange index with Worker Name
        }

        progressBar.setVisibility(View.GONE);
        linearLayoutSessDetails.setVisibility(View.VISIBLE);
        pieChartView.setVisibility(View.VISIBLE);
        pieChart.animateY(1500, Easing.getEasingFunctionFromOption(Easing.EasingOption.EaseInOutCubic));
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.getLegend().setEnabled(false);

        PieDataSet dataset = new PieDataSet(entries, "Dataset");
        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        dataset.setValueTextColor(Color.BLACK);

        PieData data = new PieData(dataset);//labels was one of the parameters
        pieChart.setData(data); // set the data and list of lables into chart

        Description description = new Description();
        description.setText("");
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
