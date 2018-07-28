package za.org.samac.harvest;

import android.content.Intent;
import android.location.Location;
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
import android.widget.SearchView;

import com.google.android.gms.common.api.Response;
import com.google.android.gms.maps.model.LatLng;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import za.org.samac.harvest.adapter.SessionsViewAdapter;
import za.org.samac.harvest.util.SearchedItem;
import za.org.samac.harvest.util.Worker;
import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.Farm;
import za.org.samac.harvest.util.Orchard;
import za.org.samac.harvest.util.WorkerType;

import static za.org.samac.harvest.MainActivity.farmerKey;

public class Sessions extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private TreeMap<String, SessionItem> sessions; //used to store session data
    private TreeMap<String, ArrayList<SearchedItem.Session>> filteredSessions;
    private ArrayList<SearchedItem.Session> adapterSource;
    private ArrayList<String> dates;
    private Map<String, String> foremenID; //used to look up name with foreman id
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ArrayList<Worker> foremen;
    private RecyclerView recyclerView;
    private SessionsViewAdapter adapter;
    private ProgressBar progressBar;
    public static SessionItem selectedItem;
    private ArrayList<Worker> workers;
    private ArrayList<Orchard> orchards;
    private ArrayList<Farm> farms;
    private HashMap<String, String> workerID;

    private String searchText = "";

    private String pageIndex = null;
    private Integer pageSize = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase

        listenForFarms();
        listenForWorkers();
        listenForOrchards();

        dates = new ArrayList<>();
        sessions = new TreeMap<>();
        adapterSource = new ArrayList<>();

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
                                startActivity(new Intent(Sessions.this, Analytics.class));
                                return true;
                        }
                        return true;
                    }
                });

        adapter = new SessionsViewAdapter(getApplicationContext(), this);
        adapter.setItems(adapterSource);
    }

    private SearchedItem.Session sessionsContainsId(ArrayList<SearchedItem.Session> sessions, String id) {
        for (SearchedItem.Session session: sessions) {
            if (session.session.key.compareTo(id) == 0) {
                return session;
            }
        }
        return null;
    }

    private Boolean filterSessions() {
        if (searchText.compareTo("") != 0) {
            filteredSessions = new TreeMap<>();
            for (String key: sessions.keySet()) {
                SessionItem item = sessions.get(key);
                for (SearchedItem foundItem: item.search(searchText, workers, foremen, orchards)) {
                    if (filteredSessions.get(foundItem.property) == null) {
                        filteredSessions.put(foundItem.property, new ArrayList<SearchedItem.Session>());
                    }
                    ArrayList<SearchedItem.Session> s = filteredSessions.get(foundItem.property);
                    if (sessionsContainsId(s, item.key) == null) {
                        filteredSessions.get(foundItem.property).add(new SearchedItem.Session(item, foundItem.reason));
                    }
                }
            }
        } else {
            filteredSessions = null;
        }
        return flattenDataSource();
    }

    private Boolean flattenDataSource() {
        int oldCount = adapterSource != null ? adapterSource.size() : 0;
        adapterSource.clear();
        if (filteredSessions == null) {
            for (String key : sessions.keySet()) {
                SessionItem item = sessions.get(key);
                adapterSource.add(new SearchedItem.Session(item, ""));
            }
        } else {
            Integer section = 1;
            for (String property : filteredSessions.keySet()) {
                ArrayList<SearchedItem.Session> items = filteredSessions.get(property);
                adapterSource.add(new SearchedItem.Session(null, property));
                for (SearchedItem.Session item : items) {
                    adapterSource.add(section, item);
                }
                section += adapterSource.size();
            }
        }
        return oldCount != adapterSource.size();
    }

    public void getNewPage() {
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
                    item.foremanId = "";
                    if (aChild.hasChild("wid")) {
                        item.foremanId = aChild.child("wid").getValue(String.class);
                        item.foreman = foremenID.get(item.foremanId);
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

                            item.addCollection(collectionSnapshot.getKey(), workerName, loc, time);
                            count++;
                        }
                    }
                    tempSessions.add(item);
                    tempDates.add(date);
                }

                if (tempSessions.size() > 1) {
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
                if (searchText == "") {
                    filteredSessions = null;
                } else {
                    filterSessions();
                }
                if (flattenDataSource()) {
                    adapter.notifyItemInserted(adapterSource.size() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void listenForOrchards() {
        orchards = new ArrayList<>();
        DatabaseReference orchardsRef = FirebaseDatabase.getInstance().getReference(MainActivity.farmerKey + "/orchards");
        orchardsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                orchards.clear();
                for (DataSnapshot dataSet : dataSnapshot.getChildren()) {
                    Orchard temp = new Orchard();
                    temp.setName(dataSet.child("name").getValue(String.class));
                    temp.setCrop(dataSet.child("crop").getValue(String.class));

                    //Iterate through coordinate sets
                    List<LatLng> coords = new Vector<>();
                    for (DataSnapshot coord : dataSet.child("coords").getChildren()){
                        // Iterate through
                        Double lats = coord.child("lat").getValue(Double.class);
                        Double lngs = coord.child("lng").getValue(Double.class);
                        coords.add(new LatLng(lats, lngs));
                    }
                    temp.setCoordinates(coords);

                    try {
                        String smeanBagMass = dataSet.child("bagMass").getValue(String.class);
                        Float meanBagMass = null;
                        if (smeanBagMass != null) {
                            if (!smeanBagMass.equals("")) {
                                meanBagMass = Float.parseFloat(smeanBagMass);
                            }
                        }
//                                else {
//                                    meanBagMass = 0;
//                                }
                        temp.setMeanBagMass(meanBagMass);
                    }
                    catch (com.google.firebase.database.DatabaseException e){
                        Long meanBagBass = dataSet.child("bagMass").getValue(Long.class);
                        if (meanBagBass != null) {
                            Float beanBagBass = meanBagBass.floatValue();
                            temp.setMeanBagMass(beanBagBass);
                        }
                    }

                    temp.setIrrigation(dataSet.child("irrigation").getValue(String.class));

                    Long tempL = dataSet.child("date").getValue(Long.class);
                    Date date;
                    Calendar c;
                    if (tempL != null){
                        c = Calendar.getInstance();
                        date = new Date(tempL);
                        c.setTime(date);
                        temp.setDatePlanted(c);
                    }

                    Farm assignedFarm = new Farm();
                    assignedFarm.setID(dataSet.child("farm").getValue(String.class));
                    temp.setAssignedFarm(assignedFarm);

                    Float row = null, tree = null;
                    try {
                        String sRow = dataSet.child("rowSpacing").getValue(String.class);
                        if (sRow != null) {
                            if (!sRow.equals("")) {
                                row = Float.parseFloat(sRow);
                            }
                        }
                    }
                    catch (com.google.firebase.database.DatabaseException e){
                        Long t = dataSet.child("rowSpacing").getValue(Long.class);
                        if (t != null){
                            row = t.floatValue();
                        }
                    }
                    try{
                        String sTree = dataSet.child("treeSpacing").getValue(String.class);
                        if (sTree != null) {
                            if(!sTree.equals("")){
                                tree = Float.parseFloat(sTree);
                            }
                        }
                    }
                    catch (com.google.firebase.database.DatabaseException e){
                        Long t = dataSet.child("rowSpacing").getValue(Long.class);
                        if (t != null){
                            tree = t.floatValue();
                        }
                    }

                    temp.setRow(row);
                    temp.setTree(tree);

                    for (DataSnapshot cultivar : dataSet.child("cultivars").getChildren()){
                        temp.addCultivar(cultivar.getValue(String.class));
                    }

                    temp.setFurther(dataSet.child("further").getValue(String.class));

                    temp.setID(dataSet.getKey());

                    orchards.add(temp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Orchard getOrchardFromIDString(String findMe){
        for (Orchard current: orchards){
            if (current.getID().equals(findMe)){
                return current;
            }
        }
        return null;
    }

    public void listenForWorkers() {
        workers = new ArrayList<>();
        foremen = new ArrayList<>();
        workerID = new HashMap<>();
        foremenID = new HashMap<>();

        DatabaseReference workersRef = FirebaseDatabase.getInstance().getReference(MainActivity.farmerKey + "/workers");

        workersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                workers.clear();
                foremen.clear();
                workerID.clear();
                foremenID.clear();
                for (DataSnapshot dataSet : dataSnapshot.getChildren()) {
                    Worker temp = new Worker();
                    temp.setfID(dataSet.getKey());
                    temp.setfName(dataSet.child("name").getValue(String.class));
                    temp.setsName(dataSet.child("surname").getValue(String.class));

                    //Orchards
                    List<Orchard> newOrhards = new Vector<>();
                    for (DataSnapshot orchard : dataSet.child("orchards").getChildren()){
                        Orchard newOrchard = getOrchardFromIDString(orchard.getValue(String.class));
                        if (newOrchard != null) {
                            newOrhards.add(newOrchard);
                        }
                    }
                    temp.setAssignedOrchards(newOrhards);

                    //Type
                    String sType = dataSet.child("type").getValue(String.class);
                    WorkerType type = WorkerType.WORKER;
                    assert sType != null;
                    if (sType.equals("Foreman")){
                        type = WorkerType.FOREMAN;
                    }
                    temp.setWorkerType(type);

                    temp.setnID(dataSet.child("idNumber").getValue(String.class));
                    temp.setFurther(dataSet.child("info").getValue(String.class));
                    temp.setPhone(dataSet.child("phoneNumber").getValue(String.class));

                    if (temp.getWorkerType() == WorkerType.FOREMAN) {
                        foremen.add(temp);
                        foremenID.put(temp.getfID(), temp.getfName() + " " + temp.getsName());
                    } else {
                        workers.add(temp);
                        workerID.put(temp.getfID(), temp.getfName() + " " + temp.getsName());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void listenForFarms() {
        farms = new ArrayList<>();

        DatabaseReference farmsRef = FirebaseDatabase.getInstance().getReference(MainActivity.farmerKey + "/farms");

        farmsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                farms.clear();
                for (DataSnapshot dataSet : dataSnapshot.getChildren()) {
                    Farm temp = new Farm();
                    temp.setName(dataSet.child("name").getValue(String.class));
                    temp.setCompany(dataSet.child("companyName").getValue(String.class));
                    temp.setEmail(dataSet.child("email").getValue(String.class));
                    temp.setPhone(dataSet.child("contactNumber").getValue(String.class));
                    temp.setProvince(dataSet.child("province").getValue(String.class));
                    temp.setTown(dataSet.child("town").getValue(String.class)); //TODO: Verify this typo
                    temp.setFurther(dataSet.child("further").getValue(String.class));
                    temp.setID(dataSet.getKey());
                    farms.add(temp);
                }
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
            default:
                super.onOptionsItemSelected(item);
                return true;
        }
//        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchText = query;
        if (filterSessions()) {
            synchronized (adapter) {
                adapter.notifyDataSetChanged();
            }
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchText = newText;
        if (filterSessions()) {
            synchronized (adapter) {
                adapter.notifyDataSetChanged();
            }
        }
        return false;
    }
}
