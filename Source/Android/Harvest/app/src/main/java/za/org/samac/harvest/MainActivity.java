package za.org.samac.harvest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import za.org.samac.harvest.adapter.WorkerRecyclerViewAdapter;
import za.org.samac.harvest.adapter.collections;
import za.org.samac.harvest.domain.Worker;
import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.WorkerComparator;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private final int GPS_SETTINGS_UPDATE = 989;
    private static final String TAG = "Clicker";

    private static ArrayList<Worker> workers;
    private static ArrayList<Worker> foremen;
    private ArrayList<Worker> workersSearch;
    private Map<Integer, Location> track;
    int trackCount = 0;
    boolean namesShowing = false;

    //used same names as IDs in xml
    private Button btnStart;
    private ProgressBar progressBar;
    private android.support.constraint.ConstraintLayout constraintLayout;
    private RecyclerView recyclerView;//I used recycler view as the grid view duplicated and rearranged worker names
    private static TextView textView;
    private TextView textViewPressStart;
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
    private String currentUserNumber;
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
    private boolean isFarmer = true;
    Boolean rejectSess = false;

    private FirebaseDatabase database;
    //private Query q;
    private DatabaseReference workersRef;
    private BottomNavigationView bottomNavigationView;

    private void init() {
        track = new HashMap<>();
        this.workers = new ArrayList<>();//stores worker names
        workersSearch = new ArrayList<>();//stores worker names
        this.foremen = new ArrayList<>();
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);//changed to network provider as GPS wasn't working

            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                location = locationManager.getLastKnownLocation(provider);
                //Log.d("last known location, provider: %s, location: %s", provider, location);

                if (location != null) {
                    break;
                }
                /*if (bestLocation == null
                        || location.getAccuracy() < bestLocation.getAccuracy()) {
                    //Log.d("found best last known location: %s", location);
                    bestLocation = location;
                }*/
            }

            if (location == null) {
                location = locationManager
                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);//changed to network provider as GPS wasn't working
                //adapter.setLocation(location);
            }
            adapter.setLocation(location);
        }

        locationPermissions();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationEnabled = true;
            if (locationManager == null) {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (locationManager != null) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);//changed to network provider as GPS wasn't working
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);//changed to network provider as GPS wasn't working
                }
            }
        }
        //new LocationHelper().getLocation(this);

        uid = user.getUid();
        currentUserEmail = user.getEmail();
        currentUserNumber = user.getPhoneNumber();
        database = FirebaseDatabase.getInstance();

        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase
        determineIfFarmer();
        statusCheck();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GPS_SETTINGS_UPDATE) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (locationManager != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, mLocationListener);//changed to network provider as GPS wasn't working
                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);//changed to network provider as GPS wasn't working
                    return;
                }

            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
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

        if (user != null){
            for(UserInfo profile : user.getProviderData()){
                if (profile.getProviderId().equals(PhoneAuthProvider.PROVIDER_ID)){
                    isFarmer = false;
                    break;
                }
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

        constraintLayout = findViewById(R.id.relLayout);
        progressBar = findViewById(R.id.progressBar);
        btnStart = findViewById(R.id.button_start);

        progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase
        constraintLayout.setVisibility(View.GONE);

        if (isFarmer){
            farmerKey = uid;
            getPolygon();//set expected yield
            currUserRef = database.getReference(uid);//Firebase reference
            workersRef = currUserRef.child("workers");
            collectWorkers();
        }
        else {
            getFarmKey();
        }

        btnStart.setTag("green");//it is best not to use the tag to identify button status

        textViewPressStart = findViewById(R.id.startText);
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
                                            startActivity(new Intent(MainActivity.this, InformationActivity.class));
                                            return true;
                                        case R.id.actionSession:
                                            startActivity(new Intent(MainActivity.this, Sessions.class));
                                            return true;
                                        case R.id.actionStats:
                                            startActivity(new Intent(MainActivity.this, Analytics.class));
                                            return true;
                                    }
                                    return true;
                                }
                            });
        }
    }

    ArrayList<String> pathsToOrchardCoords = new ArrayList<>();
    ArrayList<String> coords = new ArrayList<>();
    List<Double> polygonStoreX = new ArrayList();
    List<Double> polygonStoreY = new ArrayList();
    List<Double> polygonX = new ArrayList();
    List<Double> polygonY = new ArrayList();
    ArrayList<String> currentOrchard = new ArrayList<>();
    String correctOrchard = "";
    int holdi;

    private void getPolygon() {
        DatabaseReference myRef;
        myRef = database.getReference(farmerKey + "/orchards");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child : dataSnapshot.getChildren()){
                    currentOrchard.add(child.getKey().toString());
                    pathsToOrchardCoords.add(farmerKey + "/orchards/" + currentOrchard.get(currentOrchard.size()-1) + "/coords");
                    System.out.println(" @@@@@@@@@@@@@@@@@@@@@ *** " + currentOrchard.get(currentOrchard.size()-1) + " @@@@@@@@@@@@@@@@@@@@@ ");
                }

                for (int i = 0; i<pathsToOrchardCoords.size(); i++) {
                    DatabaseReference myRef2;
                    myRef2 = database.getReference(pathsToOrchardCoords.get(i));
                    holdi = i;
                    pathAfterGotOrchard(pathsToOrchardCoords.get(i), myRef2);
                }
                System.out.println(" @@@@@@@@@@@@@@@@@@@@@ " + "Got in function" + " @@@@@@@@@@@@@@@@@@@@@ ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void pathAfterGotOrchard(final String path, DatabaseReference myRef2) {
        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int m = 0;
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    coords.add(path + "/" + child.getKey().toString());
                    m++;
                    /*if (dataSnapshot.getChildrenCount() == m) {
                        pathAfterGotCoords();
                    }*/
                }

                pathAfterGotCoords();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    int coordsCount = 0;
    private void pathAfterGotCoords() {
        for (int j = 0; j<coords.size(); j++) {
            DatabaseReference myRef3;
            myRef3 = database.getReference(coords.get(j));
            myRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String lat = dataSnapshot.child("lat").getValue().toString();
                    String lng = dataSnapshot.child("lng").getValue().toString();
                    polygonStoreX.add(Double.parseDouble(lat));
                    polygonStoreY.add(Double.parseDouble(lng));
                    coordsCount++;
                    /*for (DataSnapshot child : dataSnapshot.getChildren()) {
                        String lat = child.child("lat").toString();
                        String lng = child.child("lng").toString();
                        polygonStoreX.add(Double.parseDouble(lat));
                        polygonStoreY.add(Double.parseDouble(lng));
                        k++;
                        if (dataSnapshot.getChildrenCount() == k) {
                            if (polygonContainsPoint(polygonStore) == true) {
                                System.out.println(" @@@@@@@@@@@@@@@@@@@@@ " + "Contains Point" + " @@@@@@@@@@@@@@@@@@@@@ ");
                                getExpectedYield();//set expected yield
                            }
                        }
                    }*/

                    if (coordsCount == coords.size()) {
                        for (int m = 0; m < currentOrchard.size(); m++) {
                            for (int i = 0; i < coords.size(); i++) {
                                if (coords.get(i).contains(currentOrchard.get(m))) {
                                    polygonX.add(polygonStoreX.get(i));
                                    polygonY.add(polygonStoreY.get(i));
                                    correctOrchard = currentOrchard.get(m);
                                }
                            }

                            if (polygonContainsPoint(polygonX, polygonY) == true) {
                                getExpectedYield();//set expected yield
                                break;
                            } else {
                                polygonX.clear();
                                polygonY.clear();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private Boolean polygonContainsPoint(List<Double> px, List<Double> py) {
        Double pointx = 0.0;
        Double pointy = 0.0;
        if (location != null) {
            pointx = location.getLatitude();
            pointy = location.getLongitude();
        }

        /*int i = 0;
        int j = polygon.size() - 1;*/
        /*Boolean c = false;

        int k = 0;
        int m = px.size()- 1;
        for (; k<px.size(); m = k++) {
            final Boolean yValid = (py.get(k) > pointy) != (py.get(m) > pointy);
            final Double xValidCond = (px.get(m) - px.get(k)) * (pointx - py.get(k)) / (py.get(m) - py.get(k)) + px.get(k);

            if (yValid && pointx < xValidCond) {
                c = !c;
            }
        }*/

        int i = 0;
        int j = px.size() - 1;
        Boolean c = false;
        for (; i < px.size(); j = i++) {
            final Boolean yValid = (py.get(i) > pointy) != (py.get(j) > pointy);
            final Double xValidCond = (px.get(j) - px.get(i)) * (pointy - py.get(i)) / (py.get(j) - py.get(i)) + px.get(i);

            if (yValid && pointx < xValidCond) {
                c = !c;
            }
        }

        return c;
    }

    private String urlExpectYieldText() {
        String base = "https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/expectedYield?";
        base = base + "orchardId=" + correctOrchard;
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
                        JSONObject obj = new JSONObject(response);
                        final Double expectedYield = obj.getDouble("expected"); // This is the value
                        runOnUiThread(new Runnable() {
                            public void run() {
                                textView = findViewById(R.id.textView);
                                textView.setText("Expected Yield: " + Math.round(expectedYield));
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
                    startActivity(new Intent(MainActivity.this, SignIn_Choose.class));
                } else {
//                    FirebaseAuth.getInstance().signOut();
                }

                // Google sign out
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(MainActivity.this, SignIn_Choose.class));
                                }
                            });
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

        /* TODO: The constant Listener causes a crash when a new worker is added, seemingly:
            To reproduce
            > In Debug mode, add a worker from the android information, then crash, but, after the crash and subsequent restart, it works.*/
//        workersRef.addValueEventListener(new ValueEventListener() {
        workersRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot zoneSnapshot: dataSnapshot.getChildren()) {
                    Log.i(TAG, zoneSnapshot.child("name").getValue(String.class));
                    //collectWorkers((Map<String, Object>) zoneSnapshot.child("name").getValue(), zoneSnapshot.child("name").getKey());
                    //collectWorkers((Map<String, Object>) zoneSnapshot.child("name").getValue(), zoneSnapshot.getKey());

                    String fullName = zoneSnapshot.child("name").getValue(String.class) + " " + zoneSnapshot.child("surname").getValue(String.class);

                    //only add if person is a worker (not a foreman)
                    if (zoneSnapshot.child("type").getValue(String.class).equals("Worker")) {
                        Worker workerObj = new Worker();
                        workerObj.setName(fullName);
                        workerObj.setValue(0);
                        workerObj.setID(zoneSnapshot.getKey());
                        workers.add(workerObj);
                    } else {
                        Worker workerObj = new Worker();
                        workerObj.setName(fullName);
                        workerObj.setValue(0);
                        workerObj.setID(zoneSnapshot.getKey());
                        foremen.add(workerObj);

                        if (zoneSnapshot.child("phoneNumber").getValue(String.class) != null) {
                            if (zoneSnapshot.child("phoneNumber").getValue(String.class).equals(currentUserNumber)) {
                                foremanID = zoneSnapshot.getKey();
                                foremanName = zoneSnapshot.child("name").getValue(String.class) + " " + zoneSnapshot.child("surname").getValue(String.class);
                            }
                        }

                        /*if (zoneSnapshot.child("email").getValue(String.class) != null) {
                            if (zoneSnapshot.child("email").getValue(String.class).equals(currentUserEmail)) {
                                foremanID = zoneSnapshot.getKey();
                                foremanName = zoneSnapshot.child("name").getValue(String.class) + " " + zoneSnapshot.child("surname").getValue(String.class);
                            }
                        }*/
                    }
                }

                Collections.sort(workers, new WorkerComparator());

                workersSearch.addAll(workers);

                progressBar.setVisibility(View.GONE);//remove progress bar
                constraintLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                //user pressed start and all went well with retrieving data
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
                progressBar.setVisibility(View.GONE);
                constraintLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    public void getFarmKey() {
//        String convertEmail = currentUserEmail;
//        emailInDB = convertEmail.replace(".", ",");
//        DatabaseReference outerRef = database.getReference("WorkingFor/"+emailInDB);
//        outerRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot child : dataSnapshot.getChildren()) {
//                    farmerKey = child.getKey();
//
//                    farmerKey = child.getKey();
//                    getPolygon();//set expected yield
//                    farmLevelRef = database.getReference(farmerKey);//Firebase reference
//                    workersRef = farmLevelRef.child("workers");
//                    collectWorkers();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.w(TAG, "onCancelled", databaseError.toException());
//            }
//        });
//        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedPref_signIn), MODE_PRIVATE);
//        farmerKey = sharedPreferences.getString(getString(R.string.sharedPref_signIn_farmerid), "0");
        farmerKey = AppUtil.readStringFromSharedPrefs(this, getString(R.string.farmerID_Pref));
        if (farmerKey == null){
            FirebaseAuth.getInstance().signOut();
            startActivityIfNeeded(new Intent(this, SignIn_Choose.class), 0);
            finish();
            return;
        }
        getPolygon();
        farmLevelRef = database.getReference(farmerKey);
        workersRef = farmLevelRef.child("workers");
        collectWorkers();
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {//changed from GPS to NETWORK
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_SETTINGS_UPDATE);
                        //startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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
    Handler handler = new Handler();
    int delay = 5000; //milliseconds
    Boolean locationWanted = false;
    int secondsLocationIsNull = 0;

    @SuppressLint({"SetTextI18n", "MissingPermission"})
    public void onClickStart(View v) {
        textViewPressStart.setVisibility(View.GONE);

        if (workers.size() == 0 && btnStart.getTag() == "green") {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            String msg = "No workers added to orchard";
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

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && btnStart.getTag() == "green") {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            String msg = "These services are unavailable, please switch on location to gain access";
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

        if (location == null && btnStart.getTag() == "green") {
            progressBar.setVisibility(View.VISIBLE);
            Snackbar.make(recyclerView, "Obtaining GPS Information...", 3000).show();
            recyclerView.setVisibility(View.GONE);

            handler.postDelayed(new Runnable() {
                public void run() {
                    //do something
                    if (location == null) {

                    } else {
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        return;
                    }

                    if (secondsLocationIsNull >= 30 && btnStart.getTag() != "green") {
                        secondsLocationIsNull = 0;
                        progressBar.setVisibility(View.GONE);
                        Snackbar.make(recyclerView, "Could not obtaining GPS Information. Please restart session.", 5000)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                })
                                .show();
                        return;
                    }

                    secondsLocationIsNull += 5;

                    handler.postDelayed(this, delay);
                }
            }, delay);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        startSessionTime = (System.currentTimeMillis() / divideBy1000Var);//(start time of session)seconds since January 1, 1970 00:00:00 UTC

        Map<String, Object> sessionDate = new HashMap<>();
        sessionDate.put("start_date", startSessionTime);

        if (isFarmer) {
            sessionDate.put("wid", uid);//add foreman database ID to session;
        } else {
            sessionDate.put("wid", foremanID);//add foreman database ID to session;
        }

        sessRef = database.getReference(farmerKey + "/sessions/" + sessionKey + "/");//path to inside a session key in Firebase

        sessRef.updateChildren(sessionDate);//save data to Firebase

        if (location != null) {
            final DatabaseReference myRef;
            myRef = database.getReference(farmerKey + "/requestedLocations/" + foremanID);//path to sessions increment in Firebase

            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        if (dataSnapshot.getKey().toString().equals(foremanID)) {
                            myRef.removeValue();
                            locationWanted = true;
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
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            myRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot != null) {
                        if (dataSnapshot.getKey().toString().equals(foremanID)) {
                            myRef.removeValue();
                            locationWanted = true;
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
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);//changed from GPS to NETWORK
            startTime = System.currentTimeMillis();
            btnStart.setBackgroundColor(Color.parseColor("#FFFF8800"));
            btnStart.setText("Stop");
            btnStart.setTag("orange");
        } else {
            //TODO: check if app closes or crashes
            textViewPressStart.setText(R.string.pressStart);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            String msger = adapter.totalBagsCollected + " bags collected, would you like to save this session?";
            AlertDialog.Builder dlgAlerter = new AlertDialog.Builder(this);
            dlgAlerter.setMessage(msger);
            dlgAlerter.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    recyclerView.setVisibility(View.GONE);
                    dialog.dismiss();
                }
            });
            dlgAlerter.setNegativeButton("No", new DialogInterface.OnClickListener() {
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
            dlgAlerter.setCancelable(false);
            dlgAlerter.create().show();

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
            //TODO: check iOS thread on slack
            //TODO: add press start again
            String timeTaken = h + " hour(s), " + m + " minute(s) and " + s + " second(s)";
            String msg = adapter.totalBagsCollected + " bags collected " + timeTaken + ".";
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

            btnStart.setBackgroundColor(Color.parseColor("#FF0CCB29"));

            btnStart.setText("Start");
            btnStart.setTag("green");
            textViewPressStart.setVisibility(View.VISIBLE);
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
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getApplicationContext(),
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
    public static ArrayList<Worker> getForemen(){return foremen;}
}