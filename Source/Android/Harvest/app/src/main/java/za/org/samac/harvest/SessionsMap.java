package za.org.samac.harvest;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import za.org.samac.harvest.adapter.SessionDetails;
import za.org.samac.harvest.domain.Worker;
import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.ColorScheme;
import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.Farm;
import za.org.samac.harvest.util.Orchard;

public class SessionsMap extends AppCompatActivity implements OnMapReadyCallback {

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
    private PolylineOptions polyline;
    private ArrayList<MarkerOptions> pickups;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions_map);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //bottom nav bar
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.actionSession);
            BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);

            bottomNavigationView.setOnNavigationItemSelectedListener(
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.actionYieldTracker:
                                    Intent openMainActivity = new Intent(SessionsMap.this, MainActivity.class);
                                    openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    startActivityIfNeeded(openMainActivity, 0);
                                    return true;
                                case R.id.actionInformation:
                                    startActivityIfNeeded(new Intent(SessionsMap.this, InformationActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0);
                                    return true;
                                case R.id.actionSession:
                                    return true;
                                case R.id.actionStats:
                                    startActivityIfNeeded(new Intent(SessionsMap.this, Stats.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0);
                                    return true;
                            }
                            return true;
                        }
                    });
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.actionSession);//set correct item to pop out on the nav bar
        }
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

        mMap.clear();

        PolylineOptions polyline = new PolylineOptions();
        polyline.color(Color.RED);
        boolean first = true;
        for (Location loc : Sessions.selectedItem.track) {
            LatLng ll = new LatLng(loc.getLatitude(), loc.getLongitude());
            if (first) {
                moveMapHere = ll;
                first = false;
            }
            polyline.add(ll);
        }
        mMap.addPolyline(polyline);

        HashMap<String, ArrayList<Pickup>> cols = (HashMap<String, ArrayList<Pickup>>) Sessions.selectedItem.collectionPoints;
        for (String key : cols.keySet()) {
            ArrayList<Pickup> data = cols.get(key);

            for (int i = 0; i < data.size(); i++) {
                LatLng ll = new LatLng(data.get(i).lat, data.get(i).lng);
                if (first) {
                    moveMapHere = ll;
                    first = false;
                }
                mMap.addMarker(new MarkerOptions().position(ll).title(data.get(i).workerName));
            }
        }

        Data data = new Data();
        for (Orchard orchard : data.getOrchards()) {
            if (!orchard.getCoordinates().isEmpty()) {
                PolygonOptions polygon = new PolygonOptions();

                Farm farm = orchard.getAssignedFarm();
                String farmId = farm == null ? "" : farm.getID();

                int fillColor = ColorScheme.hashColor(farmId, orchard.getID(), 64);
                int strokeColor = ColorScheme.hashColor(farmId, orchard.getID(), 191);

                polygon.fillColor(fillColor);
                polygon.strokeColor(strokeColor);
                polygon.strokeWidth(3);

                for (LatLng coord : orchard.getCoordinates()) {
                    polygon.add(coord);
                }

                mMap.addPolygon(polygon);
            }
        }

        callCameraMove();
    }

    private void callCameraMove() {
        if (moveMapHere!=null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moveMapHere, 15));
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
                startActivity(new Intent(SessionsMap.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(SessionsMap.this, SignIn_Choose.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(SessionsMap.this, SignIn_Farmer.class));
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
