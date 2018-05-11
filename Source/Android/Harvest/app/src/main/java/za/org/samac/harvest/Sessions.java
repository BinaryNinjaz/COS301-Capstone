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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import za.org.samac.harvest.adapter.MyData;
import za.org.samac.harvest.adapter.SessionsViewAdapter;
import za.org.samac.harvest.domain.Worker;

import static za.org.samac.harvest.MainActivity.getWorkers;

public class Sessions extends AppCompatActivity {

    private Map<String, ArrayList<String>> sessions; //used to store session data
    private ArrayList<String> dates;
    private Map<String, String> workerID; //used to look up name with worker id
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private DatabaseReference sessionsRef;
    private DatabaseReference collectionsRef;
    private ArrayList<Worker> workers;
    private RecyclerView recyclerView;
    private SessionsViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        workers = getWorkers(); // get worker info to loop through it
        workerID = new HashMap<>();
        for(int i = 0 ; i < workers.size() ; ++i) {
            String id = workers.get(i).getID();
            String name = workers.get(i).getName();
            workerID.put(id,name);
        }

        dates = new ArrayList<>();
        sessions = new HashMap<>();
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
                    String sMonth = getMonth(month);
                    final String date= day+" "+sMonth+" "+year;
                    dates.add(date);
                    //loop through collections next
                    String collection = zoneSnapshot.getKey();
                    collectionsRef = sessionsRef.child(collection).child("collections");
                    collectionsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot collectionSnap : dataSnapshot.getChildren()) {
                                String id = collectionSnap.getKey();
                                String name = workerID.get(id);
                                if (sessions.containsKey(date)) {
                                    ArrayList<String> temp = sessions.get(date);
                                    temp.add(name);
                                    sessions.put(date, temp);
                                } else {
                                    ArrayList<String> temp = new ArrayList<>();
                                    temp.add(name);
                                    sessions.put(date, temp);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //bottom nav bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.actionYieldTracker);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                startActivity(new Intent(Sessions.this, MainActivity.class));
                                return true;
                            case R.id.actionInformation:
                                return true;
                            case R.id.actionSession:
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
