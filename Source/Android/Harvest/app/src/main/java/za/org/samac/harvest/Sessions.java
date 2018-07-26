package za.org.samac.harvest;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.Response;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

import za.org.samac.harvest.adapter.SessionsViewAdapter;
import za.org.samac.harvest.domain.Worker;
import za.org.samac.harvest.util.AppUtil;

import static za.org.samac.harvest.MainActivity.farmerKey;
import static za.org.samac.harvest.MainActivity.getForemen;
import static za.org.samac.harvest.MainActivity.getWorkers;

public class Sessions extends AppCompatActivity {

    private TreeMap<String, SessionItem> sessions; //used to store session data
    private ArrayList<String> dates;
    private Map<String, String> foremenID; //used to look up name with foreman id
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private DatabaseReference sessionsRef;
    private DatabaseReference collectionsRef;
    private ArrayList<Worker> foremen;
    private RecyclerView recyclerView;
    private SessionsViewAdapter adapter;
    private ProgressBar progressBar;
    public static SessionItem selectedItem;
    private ArrayList<Worker> workers;
    private HashMap<String, String> workerID;

    private String pageIndex = null;
    private Integer pageSize = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase

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

        dates = new ArrayList<>();
        sessions = new TreeMap<>();
        uid = user.getUid();

        getNewPage();

        //bottom nav bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.actionSession);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                startActivity(new Intent(Sessions.this, MainActivity.class));
                                return true;
                            case R.id.actionInformation:
                                startActivity(new Intent(Sessions.this, InformationActivity.class));
                                return true;
                            case R.id.actionSession:
                                return true;
                            case R.id.actionStats:
                                /*Intent openAnalytics= new Intent(Sessions.this, Analytics.class);
                                openAnalytics.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openAnalytics, 0);*/
                                startActivity(new Intent(Sessions.this, Analytics.class));
                                return true;
                        }
                        return true;
                    }
                });

        adapter = new SessionsViewAdapter(getApplicationContext(), this);
        adapter.setSessions(sessions);
        adapter.setDates(dates);

    }

    private void addButtons() {
        adapter.setSessions(sessions);
        adapter.setDates(dates);
        recyclerView = findViewById(R.id.recView);
        //RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        //recyclerView.addItemDecoration(new DividerItemDecoration(Sessions.this, GridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
    }

    public void getNewPage() {
        DatabaseReference sessionsRef;
        Query query;

        if (pageIndex == null) {
            query = FirebaseDatabase.getInstance().getReference(farmerKey + "/sessions/").orderByKey().limitToLast(pageSize);
        } else {
            query = FirebaseDatabase.getInstance().getReference(farmerKey + "/sessions/").orderByKey().endAt(pageIndex).limitToLast(pageSize);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String lastKey = "";
                ArrayList<SessionItem> tempSessions = new ArrayList<>();
                ArrayList<String> tempDates = new ArrayList<>();
                for(DataSnapshot aChild : dataSnapshot.getChildren()){
                    SessionItem item = new SessionItem();

                    if (lastKey == "") {
                        lastKey = aChild.getKey();
                    }

                    item.key = aChild.getKey();
                    item.foreman = aChild.child("wid").getValue(String.class);
                    if (aChild.hasChild("wid")) {
                        item.foreman = foremenID.get(aChild.child("wid").getValue(String.class));
                    }

                    if (item.foreman == null) {
                        item.foreman = "Farm Owner";
                    }

                    Double endDate = aChild.child("end_date").getValue(Double.class);
                    Double startDate = aChild.child("start_date").getValue(Double.class);
                    if (endDate == null) {
                        endDate = 0.0;
                    }
                    if (startDate == null) {
                        startDate = 0.0;
                    }
                    item.endDate = new Date((long) (endDate * 1000));
                    item.startDate = new Date((long) (startDate * 1000));

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
                    formatter.setCalendar(Calendar.getInstance());
                    final String date = formatter.format(item.startDate);

                    for (DataSnapshot trackSnapshot : aChild.child("track").getChildren()) {
                        Double lat = trackSnapshot.child("lat").getValue(Double.class);
                        Double lng = trackSnapshot.child("lng").getValue(Double.class);
                        Location loc = new Location("");
                        loc.setLatitude(lat.doubleValue());
                        loc.setLongitude(lng.doubleValue());

                        item.addTrack(loc);
                    }
                    for (DataSnapshot collectionSnapshot : aChild.child("collections").getChildren()) {
                        String workerName = workerID.get(collectionSnapshot.getKey());
                        int count = 0;
                        for (DataSnapshot collection : collectionSnapshot.getChildren()) {
                            Double lat = collection.child("coord").child("lat").getValue(Double.class);
                            Double lng = collection.child("coord").child("lng").getValue(Double.class);
                            Location loc = new Location("");
                            loc.setLatitude(lat.doubleValue());
                            loc.setLongitude(lng.doubleValue());
                            Double time = collectionSnapshot.child("date").getValue(Double.class);

                            item.addCollection(workerName, loc, time);
                            count++;
                        }
                    }

                    tempSessions.add(item);
                    tempDates.add(date);
                }

                if (tempSessions.size() > 0) {
                    for (int i = tempSessions.size() - 1; i > 0; i--) { // we miss the first one on purpose so it isn't duplicated on subsequent calls
                        sessions.put(tempDates.get(i), tempSessions.get(i));
                        dates.add(tempDates.get(i));
                    }
                } else if (tempSessions.size() == 1) {
                    sessions.put(tempDates.get(0), tempSessions.get(0));
                    dates.add(tempDates.get(0));
                }


                if (pageIndex == null) {
                    recyclerView = findViewById(R.id.recView);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    recyclerView.setHasFixedSize(false);
                    recyclerView.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);//put progress bar until data is retrieved from firebase
                    recyclerView.setVisibility(View.VISIBLE);
                }
                pageIndex = lastKey;
                adapter.notifyItemInserted(sessions.size() - 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
                startActivity(new Intent(Sessions.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(Sessions.this, SignIn_Choose.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(Sessions.this, SignIn_Choose.class));
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
