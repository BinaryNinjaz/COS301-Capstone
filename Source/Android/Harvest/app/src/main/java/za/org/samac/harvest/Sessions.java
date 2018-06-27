package za.org.samac.harvest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import za.org.samac.harvest.adapter.MyData;
import za.org.samac.harvest.adapter.SessionsViewAdapter;
import za.org.samac.harvest.domain.Worker;

import static za.org.samac.harvest.MainActivity.getForemen;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        foremen = getForemen(); // get worker info to loop through it
        foremenID = new HashMap<>();
        for(int i = 0 ; i < foremen.size() ; ++i) {
            String id = foremen.get(i).getID();
            String name = foremen.get(i).getName();
            foremenID.put(id, name);
            System.out.println(">>>>>>>> " + id + ":" + name);
        }

        dates = new ArrayList<>();
        sessions = new TreeMap<>();
        uid = user.getUid();
        //testing block
        //Toast toast = Toast.makeText(getApplicationContext(), uid, Toast.LENGTH_SHORT);
        //toast.show();
        //
        database = FirebaseDatabase.getInstance();
        sessionsRef = database.getReference(uid + "/sessions/");
        //loop through sessions
        sessionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot zoneSnapshot : dataSnapshot.getChildren()) {
                    //get date of specific session
                    @SuppressWarnings("ConstantConditions") long startTime = (long) (zoneSnapshot.child("start_date").getValue(Double.class)*1000);



                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(startTime);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);


                    //loop through collections next
                    String collection = zoneSnapshot.getKey();
                    collectionsRef = sessionsRef.child(collection).child("collections");

                    SessionItem item = new SessionItem();
                    item.key = zoneSnapshot.getKey();
                    item.startDate = new Date((long)(zoneSnapshot.child("start_date").getValue(Double.class) * 1000));
                    item.endDate = new Date((long)(zoneSnapshot.child("end_date").getValue(Double.class) * 1000));
                    item.foreman = foremenID.get(zoneSnapshot.child("wid").getValue(String.class));

                    if (item.foreman == null) {
                        item.foreman = "Farm Owner";
                    }

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                    formatter.setCalendar(Calendar.getInstance());
                    final String date = formatter.format(item.startDate);

                    dates.add(date);
                    sessions.put(date, item);
                }
                addButtons();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
                                Intent openInformation= new Intent(Sessions.this, InformationActivity.class);
                                openInformation.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openInformation, 0);
                                return true;
                            case R.id.actionSession:
                                return true;
                            case R.id.actionStats:
                                Intent openAnalytics= new Intent(Sessions.this, Analytics.class);
                                openAnalytics.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openAnalytics, 0);
                                return true;
                        }
                        return true;
                    }
                });

        addButtons();
    }

    private String getMonth(int month) {
        String sMonth="";
        switch(month){
            case 0:
                sMonth="January";
                break;
            case 1:
                sMonth="February";
                break;
            case 2:
                sMonth="March";
                break;
            case 3:
                sMonth="April";
                break;
            case 4:
                sMonth="May";
                break;
            case 5:
                sMonth="June";
                break;
            case 6:
                sMonth="July";
                break;
            case 7:
                sMonth="August";
                break;
            case 8:
                sMonth="September";
                break;
            case 9:
                sMonth="October";
                break;
            case 10:
                sMonth="November";
                break;
            case 11:
                sMonth="December";
                break;
        }
        return sMonth;
    }

    private void addButtons() {
        adapter = new SessionsViewAdapter(getApplicationContext(), dates);
        adapter.setSessions(sessions);
        recyclerView = findViewById(R.id.recView);
        //RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        //recyclerView.addItemDecoration(new DividerItemDecoration(Sessions.this, GridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
    }

}
