package za.org.samac.harvest.adapter;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

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

import za.org.samac.harvest.MainActivity;
import za.org.samac.harvest.R;
import za.org.samac.harvest.domain.Worker;

import static za.org.samac.harvest.MainActivity.getForemen;
import static za.org.samac.harvest.MainActivity.getWorkers;

public class SessionDetails extends AppCompatActivity {

    String key;
    String wid;
    Date startDate;
    Date endDate;
    collections collected;
    private ArrayList<Worker> workers;
    private HashMap<String, String> workerID;
    private ArrayList<Worker> foremen;
    private HashMap<String, String> foremenID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

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
            System.out.println(">>>>>> " + id + " : " + name);
        }

        collected = new collections("");

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference dbref = database.getReference(MainActivity.farmerKey + "/sessions/" + getIntent().getStringExtra("key"));

        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                startDate = new Date((long)(dataSnapshot.child("start_date").getValue(Double.class) * 1000));
                endDate = new Date((long)(dataSnapshot.child("end_date").getValue(Double.class) * 1000));
                key = dataSnapshot.getKey();
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
}
