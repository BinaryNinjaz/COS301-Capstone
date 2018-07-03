package za.org.samac.harvest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import za.org.samac.harvest.adapter.MyData;
import za.org.samac.harvest.adapter.WorkerRecyclerViewAdapter;
import za.org.samac.harvest.adapter.collections;
import za.org.samac.harvest.domain.Worker;
import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.WorkerComparator;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final String TAG = "Clicker";

    private static ArrayList<Worker> workers;
    private ArrayList<Worker> workersSearch;
    private Map<Integer, Location> track;
    int trackCount = 0;
    boolean namesShowing = false;

    //used same names as IDs in xml
    private Button btnStart;
    private ProgressBar progressBar;
    private RelativeLayout relLayout;
    private RecyclerView recyclerView;//I used recycler view as the grid view duplicated and rearranged worker names
    private TextView textView;
    private WorkerRecyclerViewAdapter adapter;
    private LocationManager locationManager;
    private Location location;
    private FirebaseAuth mAuth;
    private boolean locationEnabled = false;
    private static final long LOCATION_REFRESH_TIME = 60000;
    private static final float LOCATION_REFRESH_DISTANCE = 3;
    private double startSessionTime;
    private double endSessionTime;
    private double divideBy1000Var = 1000.0000000;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String currentUserEmail;
    private String emailInDB;
    private String uid;
    private String foremanID;
    private String foremanName;
    private DatabaseReference currUserRef;
    private DatabaseReference farmRef;
    private DatabaseReference sessRef;
    private DatabaseReference farmLevelRef;
    public static String sessionKey;
    public static String farmerKey;
    private boolean isFarmer = false;
    Boolean rejectSess = false;

    private FirebaseDatabase database;
    //private Query q;
    private DatabaseReference workersRef;
    private BottomNavigationView bottomNavigationView;

    private void init() {
        track = new HashMap<>();
        this.workers = new ArrayList<>();//stores worker names
        workersSearch = new ArrayList<>();//stores worker names
        adapter = new WorkerRecyclerViewAdapter(getApplicationContext(), workersSearch);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        } else {
            locationEnabled = true;
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);//changed to network provider as GPS wasn't working

            /*List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                location = locationManager.getLastKnownLocation(provider);
                //Log.d("last known location, provider: %s, location: %s", provider, location);

                if (location == null) {
                    continue;
                }
                if (bestLocation == null
                        || location.getAccuracy() < bestLocation.getAccuracy()) {
                    //Log.d("found best last known location: %s", location);
                    bestLocation = location;
                }
            }*/
            location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);//changed to network provider as GPS wasn't working
            //adapter.setLocation(location);
        }

        locationPermissions();
        //new LocationHelper().getLocation(this);

        uid = user.getUid();
        currentUserEmail = user.getEmail();
        database = FirebaseDatabase.getInstance();

        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase
        determineIfFarmer();
        statusCheck();
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    private void determineIfFarmer() {
        DatabaseReference outerRef = database.getReference();
        outerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot child : dataSnapshot.getChildren()){
                    if (child.getKey().equals(uid)){
                        isFarmer = true;
                        break;
                    }
                }

                if (isFarmer){
                    progressBar.setVisibility(View.GONE);//remove progress bar
                    setContentView(R.layout.activity_farmer);
                }
                else {
                    progressBar.setVisibility(View.GONE);//remove progress bar
                    setContentView(R.layout.activity_foreman);
                }

                getPolygon();//set expected yield

                relLayout = findViewById(R.id.relLayout);
                progressBar = findViewById(R.id.progressBar);
                btnStart = findViewById(R.id.button_start);

                progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase
                relLayout.setVisibility(View.GONE);

                if (isFarmer){
                    farmerKey = uid;
                    currUserRef = database.getReference(uid);//Firebase reference
                    workersRef = currUserRef.child("workers");
                    collectWorkers();
                }
                else {
                    getFarmKey();
                }

                btnStart.setTag("green");//it is best not to use the tag to identify button status

                recyclerView = findViewById(R.id.recyclerView);//this encapsulates the worker buttons, it is better than gridview
                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, GridLayoutManager.VERTICAL));
                recyclerView.setAdapter(adapter);
                recyclerView.setVisibility(View.GONE);

                //Handle the bottom nav here
                if (isFarmer){

                    //bottom navigation bar
                    bottomNavigationView = findViewById(R.id.bottom_navigation);

                    bottomNavigationView.setSelectedItemId(R.id.actionYieldTracker);
                    bottomNavigationView.setOnNavigationItemSelectedListener(
                            new BottomNavigationView.OnNavigationItemSelectedListener() {
                                @Override
                                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.actionYieldTracker:
                                            return true;
                                        case R.id.actionInformation:
//                                            startActivity(new Intent(MainActivity.this, InformationActivity.class));
                                            Intent openMainActivity= new Intent(MainActivity.this, InformationActivity.class);
                                            openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            startActivityIfNeeded(openMainActivity, 0);
                                            return true;
                                        case R.id.actionSession:
                                            Intent openSessions= new Intent(MainActivity.this, SessionsMap.class);
                                            openSessions.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            startActivityIfNeeded(openSessions, 0);
                                            return true;
                                        case R.id.actionStats:
                                            Intent openAnalytics= new Intent(MainActivity.this, Analytics.class);
                                            openAnalytics.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                            startActivityIfNeeded(openAnalytics, 0);
                                            return true;
                                    }
                                    return true;
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    ArrayList<String> pathsToOrchardCoords = new ArrayList<>();
    ArrayList<String> coords = new ArrayList<>();
    List<List<Double>> polygonStore = new ArrayList();
    String currentOrchard;

    private void getPolygon() {
        DatabaseReference myRef;
        myRef = database.getReference(farmerKey + "/orchards");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    currentOrchard = child.getValue().toString();
                    pathsToOrchardCoords.add(farmerKey + "/orchards/" + child.getValue() + "/coords");
                }

                for (int i = 0; i<pathsToOrchardCoords.size(); i++) {
                    DatabaseReference myRef2;
                    myRef2 = database.getReference(pathsToOrchardCoords.get(i));
                    final int finalI = i;
                    myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                child.getValue();
                                coords.add(pathsToOrchardCoords.get(finalI) + "/" + child.getValue());
                            }

                            for (int j = 0; j<coords.size(); j++) {
                                DatabaseReference myRef3;
                                myRef3 = database.getReference(coords.get(j));
                                myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        int k = 0;
                                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                                            String lat = child.child("lat").toString();
                                            String lng = child.child("lng").toString();
                                            polygonStore.get(k).set( 0, Double.parseDouble(lat));
                                            polygonStore.get(k).set( 1, Double.parseDouble(lng));
                                            k++;
                                        }

                                        if (polygonContainsPoint(polygonStore) == true) {
                                            getExpectedYield();//set expected yield
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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private Boolean polygonContainsPoint(List<List<Double>> polygon) {
        Double pointx = 0.0;
        Double pointy = 0.0;
        if (location != null) {
            pointx = location.getLatitude();
            pointy = location.getLongitude();
        }

        int i = 0;
        int j = polygon.size() - 1;
        Boolean c = false;
        for (; i < polygon.size(); j = i++) {
            Point pi = (Point) polygon.get(i);
            Point pj = (Point) polygon.get(j);
            final Boolean yValid = (pi.y > pointy) != (pj.y > pointy);
            final Double xValidCond = (pj.x - pi.x) * (pointx - pi.y) / (pj.y - pi.y) + pi.x;

            if (yValid && pointx < xValidCond) {
                c = !c;
            }
        }
        return c;
    }

    private String urlExpectYieldText() {
        String base = "https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/expectedYield";
        base = base + "orchardId=" + currentOrchard;//get correct orchard ID
        double currentTime;
        double divideBy1000Var = 1000.0000000;
        currentTime = (System.currentTimeMillis()/divideBy1000Var);
        base = base + "&date=" + currentTime;
        base = base + "&uid=" + farmerKey;
        return base;
    }

    public void getExpectedYield() {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String response = sendGet(urlExpectYieldText());
                        System.out.println(" %%%%%%%%%%%%% " + response + " %%%%%%%%%%%%% ");
                        JSONObject obj = new JSONObject(response);
                        final Double expectedYield = obj.getDouble("expected"); // This is the value
                        System.out.println(" $$$$$$$$$$$$$$$$$$$ " + expectedYield + " $$$$$$$$$$$$$$$$$$$ ");
                        runOnUiThread(new Runnable() {
                            public void run() {
                                textView = findViewById(R.id.textView);
                                textView.setText("Expected Yield: " + expectedYield);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String sendGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

//    @Override
//    protected void onResume(){
//        super.onResume();
////        if(isFarmer) {
////            bottomNavigationView.setSelectedItemId(R.id.actionInformation);
////        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
//        MenuItem searchMenu = menu.findItem(R.id.search);
//        final SearchView searchView = (SearchView) searchMenu.getActionView();
//        searchView.setIconified(false);
//        searchView.requestFocusFromTouch();
//        searchView.setOnQueryTextListener(this);
//        searchMenu.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem menuItem) {
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
//                return true;
//            }
//        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
//                MenuItem searchMenu = menu.findItem(R.id.search);
                final SearchView searchView = (SearchView) item.getActionView();
                searchView.setIconified(false);
                searchView.requestFocusFromTouch();
                searchView.setOnQueryTextListener(this);
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem menuItem) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                        return true;
                    }
                });
                return true;
            case R.id.settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if (!AppUtil.isUserSignedIn()) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                } else {
//                    FirebaseAuth.getInstance().signOut();
                }
                finish();
                return true;
        }
        return false;
    }

    /***********************
     ** Function below creates arrays of the workers, how many bags they collect
     * and an array of buttons to be added to the view
     */

    protected void collectWorkers() {
        workersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot zoneSnapshot: dataSnapshot.getChildren()) {
                    Log.i(TAG, zoneSnapshot.child("name").getValue(String.class));
                    //collectWorkers((Map<String, Object>) zoneSnapshot.child("name").getValue(), zoneSnapshot.child("name").getKey());
                    //collectWorkers((Map<String, Object>) zoneSnapshot.child("name").getValue(), zoneSnapshot.getKey());

                    String fullName = zoneSnapshot.child("name").getValue(String.class) + " " + zoneSnapshot.child("surname").getValue(String.class);
                    //only add if person is a worker (not a foreman)
                    if(zoneSnapshot.child("type").getValue(String.class).equals("Worker")) {
                        Worker workerObj = new Worker();
                        workerObj.setName(fullName);
                        workerObj.setValue(0);
                        workerObj.setID(zoneSnapshot.getKey());
                        workers.add(workerObj);
                    } else {
                        if (zoneSnapshot.child("email").getValue(String.class).equals(currentUserEmail)) {
                            foremanID = zoneSnapshot.getKey();
                            foremanName = zoneSnapshot.child("name").getValue(String.class) + " " + zoneSnapshot.child("surname").getValue(String.class);
                        }
                    }
                }

                Collections.sort(workers, new WorkerComparator());

                workersSearch.addAll(workers);

                progressBar.setVisibility(View.GONE);//remove progress bar
                relLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                //user pressed start and all went well with retrieving data
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
                progressBar.setVisibility(View.GONE);
                relLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    public void getFarmKey() {
        String convertEmail = currentUserEmail;
        emailInDB = convertEmail.replace(".", ",");
        DatabaseReference outerRef = database.getReference("WorkingFor/"+emailInDB);
        outerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    farmerKey = child.getKey();

                    farmerKey = child.getKey();
                    farmLevelRef = database.getReference(farmerKey);//Firebase reference
                    workersRef = farmLevelRef.child("workers");
                    collectWorkers();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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

    /*******************************
     Code below handles the stop/start button, runs a timer and displays how many
     bags were collected in the elapsed time. It then clears for another timer to start.
     Sessions for each worker still needs to be implemented *
     */
    long startTime = 0, stopTime = 0;
    //Handler handler = new Handler();
    //int delay = 120000; //milliseconds
    Boolean locationWanted = false;

    @SuppressLint({"SetTextI18n", "MissingPermission"})
    public void onClickStart(View v) {
        if(location == null) {
            progressBar.setVisibility(View.VISIBLE);
            Snackbar.make(recyclerView, "Obtaining GPS Information...", 3000).show();
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
        }
        startSessionTime = (System.currentTimeMillis() / divideBy1000Var);//(start time of session)seconds since January 1, 1970 00:00:00 UTC

        sessRef = database.getReference(farmerKey + "/sessions/" + sessionKey + "/");//path to inside a session key in Firebase

        if(location != null) {
            DatabaseReference myRef;
            myRef = database.getReference(farmerKey + "/requestedLocations/" + foremanID);//path to sessions increment in Firebase
            myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.toString().equals(foremanID)) {
                            locationWanted = true;
                            break;
                        }
                    }

                    if (locationWanted == true) {
                        DatabaseReference myRef2;
                        myRef2 = database.getReference(farmerKey + "/locations/" + foremanID);//path to sessions increment in Firebase

                        Map<String, Object> coordinates = new HashMap<>();
                        coordinates.put("lat", location.getLatitude());
                        coordinates.put("lng", location.getLongitude());

                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("coord", coordinates);
                        childUpdates.put("display", foremanName);

                        myRef2.updateChildren(childUpdates);//store location
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        Map<String, Object> sessionDate = new HashMap<>();
        sessionDate.put("start_date", startSessionTime);
        sessionDate.put("wid", foremanID);//add foreman database ID to session;

        if (!namesShowing) {
            TextView textView = findViewById(R.id.startText);
            textView.setVisibility(View.GONE);
            namesShowing = true;
            adapter.notifyDataSetChanged();
        }
        if (btnStart.getTag() == "green") {
            sessionKey = sessRef.push().getKey();//generate key/ID for a session

            adapter.setPlusEnabled(true);
            adapter.setMinusEnabled(true);
            track = new HashMap<Integer, Location>(); //used in firebase function
            track.put(trackCount, location);
            if (locationEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            }
            startTime = System.currentTimeMillis();
            btnStart.setBackgroundColor(Color.parseColor("#FFFF8800"));
            btnStart.setText("Stop");
            btnStart.setTag("orange");
        } else {
            //TODO: check if app closes or crashes
            if (adapter.totalBagsCollected == 0) {
                String msg = adapter.totalBagsCollected + " bags collected, would you like to save this session?";
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setMessage(msg);
                dlgAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        recyclerView.setVisibility(View.GONE);
                        dialog.dismiss();
                    }
                });
                dlgAlert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference myRef;
                        myRef = database.getReference(farmerKey + "/sessions/" + sessionKey);//path to sessions increment in Firebase
                        myRef.removeValue();//remove latest increment
                        adapter.setIncrement();
                        recyclerView.setVisibility(View.GONE);
                        dialog.dismiss();
                        rejectSess = true;
                    }
                });
                dlgAlert.setCancelable(false);
                dlgAlert.create().show();
            }

            if (rejectSess == false) {
                endSessionTime = (System.currentTimeMillis() / divideBy1000Var);//(end time of session) seconds since January 1, 1970 00:00:00 UTC
                sessionDate.put("end_date", endSessionTime);
                sessRef.updateChildren(sessionDate);//save data to Firebase
            }

            stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            // do something with time
            int h = (int) ((elapsedTime / 1000) / 3600);
            int m = (int) (((elapsedTime / 1000) / 60) % 60);
            int s = (int) ((elapsedTime / 1000) % 60);
            //this is the output of the pop up when the user clicks stop (the session)
            String timeTaken = h + " hour(s), " + m + " minute(s) and " + s + " second(s)";
            String msg = "A total of " + adapter.totalBagsCollected + " bags have been collected in " + timeTaken + ".";
            if (locationEnabled) {
                locationManager.removeUpdates(mLocationListener);
            }

            adapter.totalBagsCollected = 0;//reset total number of bags collected for all workers
            for (int i = 0; i < workers.size(); i++) {
                workers.get(i).setValue(0);
            }
            adapter.setPlusEnabled(false);
            adapter.setMinusEnabled(false);
            collections collectionObj = adapter.getCollectionObj();
            collectionObj.sessionEnd();
            //****writeToFirebase(collectionObj);
            if (rejectSess == false) {
                //pop up is used to show how many bags were collected in the elapsed time
                AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
                dlgAlert.setMessage(msg);
                dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.setIncrement();
                        recyclerView.setVisibility(View.GONE);
                        dialog.dismiss();
                    }
                });
                dlgAlert.setCancelable(false);
                dlgAlert.create().show();
            }

            btnStart.setBackgroundColor(Color.parseColor("#FF0CCB29"));

            btnStart.setText("Start");
            btnStart.setTag("green");
        }
    }

    private void writeToFirebase(collections collectionObj) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String key = database.getReference("yields").push().getKey();
        DatabaseReference mRef = database.getReference().child("yields").child(key);
        mRef.setValue("collections");
        Map<String, MyData> map = collectionObj.getIndividualCollections();
        Map<String, String> collectionData = new HashMap<String, String>();
        collectionData.put("email", collectionObj.getForemanEmail());
        collectionData.put("end_date", Double.toString(collectionObj.getEnd_date()));
        collectionData.put("start_date", Double.toString(collectionObj.getStart_date()));
        mRef.child("collections").setValue(collectionData);
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            mRef.child("collections").setValue(pair.getKey());
            MyData data = map.get(pair.getKey());
            DatabaseReference dRef = mRef.child("collections").child((String) pair.getKey());
            for (int i = 0; i < data.size; i++) {
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
            Map.Entry pair = (Map.Entry) it1.next();
            Location loc = (Location) pair.getValue();
            if (loc != null) {
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

    public Location getLocation() {
        return location;
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location locationChange) {
            location = locationChange;
            trackCount++;
            track.put(trackCount, location);
            adapter.setLocation(location);
            //recyclerView.setVisibility(View.VISIBLE);
            //progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };


    @Override
    public boolean onQueryTextSubmit(String s) {
        doWorkersClientSideSearch(s);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        doWorkersClientSideSearch(s);
        return false;
    }

    public void locationPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }
    }

    private void doWorkersClientSideSearch(String searchText) {
        workersSearch.clear();
        if (searchText != null && !searchText.equals("")) {
            List<Worker> results = new ArrayList<>();
            for (Worker worker : workers) {
                if (worker.getName().toLowerCase().contains(searchText.toLowerCase())) {
                    results.add(worker);
                }
            }
            workersSearch.addAll(results);
        } else {
            workersSearch.addAll(workers);
        }
        adapter.notifyDataSetChanged();
    }

    void updateFarmer(boolean setMe) {
        isFarmer = setMe;
    }

    public static ArrayList<Worker> getWorkers(){return workers;}
}