package za.org.samac.harvest;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import za.org.samac.harvest.domain.Worker;

public class SessionsMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    //private ArrayList<Marker> markersArray = new ArrayList<Marker>();
    private String uid;
    private FirebaseDatabase database;
    private DatabaseReference sessionsRef;
    private DatabaseReference collectionsRef;
    private DatabaseReference workerRef;
    private Query q;
    private Map<String, String> workerID;
    private ArrayList<Worker> workers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        uid = user.getUid();
        database = FirebaseDatabase.getInstance();
        sessionsRef = database.getReference(uid +"/sessions/");

        q = sessionsRef.limitToLast(1);
        //collectionsRef = q.getRef();
        //testing block
        //Toast toast = Toast.makeText(getApplicationContext(), wID, Toast.LENGTH_SHORT);
        //toast.show();
        //

        workers = MainActivity.getWorkers(); // get worker info to loop through it
        workerID = new HashMap<>();
        for(int i = 0 ; i < workers.size() ; ++i) {
            String id = workers.get(i).getID();
            String name = workers.get(i).getName();
            workerID.put(id,name);
        }

        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot collectionSnap : dataSnapshot.getChildren()) {
                    String wID = collectionSnap.getKey();
                    final String name = workerID.get(wID);
                    //testing block
                    //Toast toast = Toast.makeText(getApplicationContext(), wID, Toast.LENGTH_SHORT);
                    //toast.show();
                    //
                    workerRef = collectionSnap.child(wID).getRef();
                    workerRef.addValueEventListener(new ValueEventListener() {
                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot2) {
                            for (DataSnapshot workerSnap : dataSnapshot2.getChildren()) {
                                double lat = workerSnap.child("coord").child("lat").getValue(double.class);
                                double lng = workerSnap.child("coord").child("lng").getValue(double.class);
                                mMap.addMarker(new MarkerOptions().position(
                                        new LatLng(lat,lng)).title(name));
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

        onMapReady(mMap);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
