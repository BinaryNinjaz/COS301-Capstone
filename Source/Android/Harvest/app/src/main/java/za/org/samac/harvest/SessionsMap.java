package za.org.samac.harvest;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
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

import static za.org.samac.harvest.MainActivity.getWorkers;

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
    private boolean isFirstCollection;
    private boolean isHere = false;
    private boolean isThere = false;
    private LatLng moveMapHere ; // just used to find where to move map to


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        isFirstCollection = true; // just used to find where to move map to
        uid = user.getUid();
        database = FirebaseDatabase.getInstance();
        sessionsRef = database.getReference(uid +"/sessions/");

        q = sessionsRef.limitToLast(1);
        checkPlayServices();


        workers = getWorkers(); // get worker info to loop through it
        workerID = new HashMap<>();
        for(int i = 0 ; i < workers.size() ; ++i) {
            String id = workers.get(i).getID();
            String name = workers.get(i).getName();
            workerID.put(id,name);
        }

        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String errorTxt = "How many kids? -> "+dataSnapshot.getChildrenCount();
                //Toast toast = Toast.makeText(getApplicationContext(), errorTxt, Toast.LENGTH_SHORT);
                //toast.show();
                DataSnapshot collections = dataSnapshot.child("collections");

                //DatabaseReference tempRef = collections.child("2").getRef();
                //String err = "Is der a kid doe? -> "+tempRef.getKey();
                //Toast tst = Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT);
                //tst.show();
                for(DataSnapshot collectionSnap : collections.getChildren()) {
                    //isHere = true;
                    String wID = collectionSnap.getKey();
                    final String name = workerID.get(wID);
                    workerRef = collectionSnap.child(wID).getRef();
                    workerRef.addValueEventListener(new ValueEventListener() {
                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot2) {
                            for (DataSnapshot workerSnap : dataSnapshot2.getChildren()) {
                                double lat = workerSnap.child("coord").child("lat").getValue(double.class);
                                double lng = workerSnap.child("coord").child("lng").getValue(double.class);
                                //isThere = true;
                                if (isFirstCollection) {
                                    moveMapHere = new LatLng(lat, lng);
                                    isFirstCollection = false;
                                }
                                mMap.addMarker(new MarkerOptions().position(
                                        new LatLng(lat, lng)).title(name));
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

        //String errorTxt = "Is here = "+isHere+" isThere = "+isThere;
        //Toast toast = Toast.makeText(getApplicationContext(), errorTxt, Toast.LENGTH_SHORT);
        //toast.show();
        callCameraMove();
    }

    private void callCameraMove() {
        if(moveMapHere!=null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(moveMapHere));
        }else{
            //hard-coding sessions due to current firebase bug
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(-25.72866109, 28.49997623)).title("Rob Kingston"));
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(-25.72866123, 28.50094444)).title("Sally Benjamin"));
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(-25.72866321, 28.50095555)).title("Ryan Benjamin"));
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(-25.7282345, 28.50123401)).title("James Worker"));
            mMap.addMarker(new MarkerOptions().position(
                    new LatLng(-25.7281111, 28.50095678)).title("Rob Kingston"));
            moveMapHere = new LatLng(-25.72866109, 28.49997623);
            mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(-25.7281, 28.49997), new LatLng(-25.7283, 28.50094))
                    .width(5)
                    .color(Color.BLUE));
            mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(-25.7283, 28.50094), new LatLng(-25.7285, 28.50095))
                    .width(5)
                    .color(Color.BLUE));
            mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(-25.7285, 28.50095), new LatLng(-25.7287, 28.50095555))
                    .width(5)
                    .color(Color.BLUE));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moveMapHere,15));
        }
    }

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }
}
