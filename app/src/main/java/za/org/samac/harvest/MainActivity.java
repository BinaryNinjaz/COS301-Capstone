package za.org.samac.harvest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import za.org.samac.harvest.adapter.MyData;
import za.org.samac.harvest.adapter.WorkerGridAdapter;
import za.org.samac.harvest.adapter.collections;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Clicker";

    private ArrayList<String> workers;
    private Map<Integer, Location> track;
    int trackCount = 0;

    private GridView gridview;
    private WorkerGridAdapter adapter;
    private LocationManager locationManager;
    private Location location;
    private FirebaseAuth mAuth;
    private boolean locationEnabled = false;
    private static final long LOCATION_REFRESH_TIME = 60000;
    private static final float LOCATION_REFRESH_DISTANCE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }else {
            locationEnabled = true;
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);
            location = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            adapter.setLocation(location);
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("workers");
        Query q = ref.orderByChild("name");
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    collectWorkers((Map<String, Object>) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", databaseError.toString());
            }
        });

        setContentView(R.layout.activity_main);
        Button btn = findViewById(R.id.button_start);
        btn.setTag("green");
        workers = new ArrayList<>();
        gridview = findViewById(R.id.gridview);
        adapter = new WorkerGridAdapter(getApplicationContext(), workers);
        gridview.setAdapter(adapter);
    }

    /***********************
     ** Function below creates arrays of the workers, how many bags they collect
     * and an array of buttons to be added to the view
     */


    protected void collectWorkers(Map<String, Object> users) {
        for (Map.Entry<String, Object> entry : users.entrySet()) {
            Map singleUser = (Map) entry.getValue();
            String fullName = singleUser.get("name") + " " + singleUser.get("surname");
            workers.add(fullName);
        }

        Collections.sort(workers);
        adapter.notifyDataSetChanged();
    }


    /*******************************
     Code below handles the stop/start button, runs a timer and displays how many
     bags were collected in the elapsed time. It then clears for another timer to start.
     Sessions for each worker still needs to be implemented *
     */
    long startTime = 0, stopTime = 0;

    @SuppressLint({"SetTextI18n", "MissingPermission"})
    public void onClickStart(View v) {
        Button btn = findViewById(R.id.button_start);
        if (btn.getTag() == "green") {
            adapter.setPlusEnabled(true);
            adapter.setMinusEnabled(true);
            track = new HashMap<Integer, Location>();
            track.put(trackCount,location);
            if(locationEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            }
            startTime = System.currentTimeMillis();
            btn.setBackgroundColor(Color.parseColor("#FFFF8800"));
            btn.setText("Stop");
            btn.setTag("orange");
        } else {
            stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            // do something with time
            int h = (int) ((elapsedTime / 1000) / 3600);
            int m = (int) (((elapsedTime / 1000) / 60) % 60);
            int s = (int) ((elapsedTime / 1000) % 60);
            String timeTaken = h + " hour(s), " + m + " minute(s) and " + s + " second(s)";
            String msg = "A total of " + adapter.totalBagsCollected + " bags have been collected in " + timeTaken + ".";
            if(locationEnabled) {
                locationManager.removeUpdates(mLocationListener);
            }
            adapter.totalBagsCollected = 0;
            adapter.setPlusEnabled(false);
            adapter.setMinusEnabled(false);
            collections collectionObj = adapter.getCollectionObj();
            collectionObj.sessionEnd();
            writeToFirebase(collectionObj);
            //pop up is used to show how many bags were collected in the elapsed time
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setMessage(msg);
            dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    adapter.setIncrement();
                    dialog.dismiss();
                }
            });
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();
            //
            btn.setBackgroundColor(Color.parseColor("#FF0CCB29"));

            btn.setText("Start");
            btn.setTag("green");
        }
    }

    private void writeToFirebase(collections collectionObj) {
        FirebaseDatabase database =  FirebaseDatabase.getInstance();
        String key = database.getReference("yields").push().getKey();
        DatabaseReference mRef =  database.getReference().child("yields").child(key);
        mRef.setValue("collections");
        Map<String, MyData> map = collectionObj.getIndividualCollections();
        Map<String,String> collectionData = new HashMap<String, String>();
        collectionData.put("email", collectionObj.getForemanEmail());
        collectionData.put("end_date", Double.toString(collectionObj.getEnd_date()));
        collectionData.put("start_date", Double.toString(collectionObj.getStart_date()));
        mRef.child("collections").setValue(collectionData);
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            mRef.child("collections").setValue(pair.getKey());
            MyData data = map.get(pair.getKey());
            DatabaseReference dRef = mRef.child("collections").child((String)pair.getKey());
            for(int i = 0; i < data.size ; i++) {
                dRef.setValue(Integer.toString(i));
                dRef.child(Integer.toString(i)).setValue("coord");
                dRef.child(Integer.toString(i)).setValue("date");
                dRef.child("date").setValue(data.date.get(i));
                dRef.child("coord").setValue("lat");
                dRef.child("coord").setValue("lng");
                dRef.child("coord").child("lat").setValue(data.latitude.get(i));
                dRef.child("coord").child("lng").setValue(data.longitude.get(i));
            }
        }
        /*mRef.child("collections").setValue("email");
        mRef.child("collections").setValue("start_date");
        mRef.child("collections").setValue("end_date");
        mRef.child("collections").child("email").setValue(collectionObj.getForemanEmail());
        mRef.child("collections").child("start_date").setValue(collectionObj.getStart_date());
        mRef.child("collections").child("end_date").setValue(collectionObj.getEnd_date());*/
        mRef.setValue("track");
        Iterator it1 = track.entrySet().iterator();
        int count = 0;
        while (it1.hasNext()) {
            Map.Entry pair = (Map.Entry)it1.next();
            Location loc = (Location) pair.getValue();
            if(loc!=null) {
                double lat = loc.getLatitude();
                double lng = loc.getLongitude();
                mRef.child("track").setValue(Integer.toString(count));
                mRef.child("track").child(Integer.toString(count)).setValue("lat");
                mRef.child("track").child(Integer.toString(count)).setValue("lng");
                mRef.child("track").child(Integer.toString(count)).child("lat").setValue(lat);
                mRef.child("track").child(Integer.toString(count)).child("lng").setValue(lng);
            }
            ++count;
        }
    }

    public Location getLocation() { return location; }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location locationChange) {
            location = locationChange;
            trackCount++;
            track.put(trackCount,location);
            adapter.setLocation(location);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
        @Override
        public void onProviderEnabled(String provider) { }
        @Override
        public void onProviderDisabled(String provider) { }
    };


}
