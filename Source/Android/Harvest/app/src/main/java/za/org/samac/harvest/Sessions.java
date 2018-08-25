package za.org.samac.harvest;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import za.org.samac.harvest.adapter.SessionsViewAdapter;
import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.SearchedItem;
import za.org.samac.harvest.util.Worker;
import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.Farm;
import za.org.samac.harvest.util.Orchard;
import za.org.samac.harvest.util.WorkerType;

import static za.org.samac.harvest.MainActivity.farmerKey;

public class Sessions extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private TreeMap<Date, SessionItem> sessions; //used to store session data
    private TreeMap<String, ArrayList<SearchedItem.Session>> filteredSessions;
    private ArrayList<SearchedItem.Session> adapterSource;
    private ArrayList<Date> dates;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout recyclerRefresh;
    private SessionsViewAdapter adapter;
    private ProgressBar progressBar;
    public static SessionItem selectedItem;
    private String farmOwnerName = "Farm Owner";
    private Data data;
    private BottomNavigationView bottomNavigationView;

    private String searchText = "";

    private String pageIndex = null;
    private Integer pageSize = 21;
    private ArrayList<ValueEventListener> ids = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase

        recyclerRefresh = findViewById(R.id.recViewSwipeRefresh);

        getAdmin();

        data = new Data();
        data.notifyMe(this);

//        if(!Data.isPulling()){
//            getNewPage();
//        }

        if (Data.isPulling()){
            recyclerRefresh.setRefreshing(true);
        }
        else {
            endRefresh();
        }

        recyclerRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerRefresh.setRefreshing(true);
                data.pull();
            }
        });

        dates = new ArrayList<>();
        sessions = new TreeMap<>();
        adapterSource = new ArrayList<>();

        //bottom nav bar
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.actionSession);
        BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                Intent openMainActivity= new Intent(Sessions.this, MainActivity.class);
                                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openMainActivity, 0);
                                return true;
                            case R.id.actionInformation:
                                startActivity(new Intent(Sessions.this, InformationActivity.class));
                                return true;
                            case R.id.actionSession:
                                return true;
                            case R.id.actionStats:
                                startActivity(new Intent(Sessions.this, Stats.class));
                                return true;
                        }
                        return true;
                    }
                });

        adapter = new SessionsViewAdapter(getApplicationContext(), this);
        adapter.setItems(adapterSource);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.actionSession);//set correct item to pop out on the nav bar
        }
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
            for (Date key: sessions.keySet()) {
                SessionItem item = sessions.get(key);

                Vector<Worker> workers = new Vector<Worker>();
                Vector<Worker> foremen = new Vector<Worker>();

                for (Worker worker : data.getWorkers()) {
                    if (worker.getWorkerType() == WorkerType.FOREMAN) {
                        foremen.add(worker);
                    } else {
                        workers.add(worker);
                    }
                }

                for (SearchedItem foundItem: item.search(searchText, workers, foremen, data.getOrchards())) {
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

    static class DescOrder implements Comparator<Date> {

        @Override
        public int compare(Date o1, Date o2) {
            return o2.compareTo(o1);
        }
    }

    public static Date instanceToDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Boolean flattenDataSource() {
        int oldCount = adapterSource != null ? adapterSource.size() : 0;
        adapterSource.clear();
        if (filteredSessions == null) {
            SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            formatter.setCalendar(Calendar.getInstance());

            TreeMap<Date, ArrayList<SearchedItem.Session>> compacted = new TreeMap<>(new DescOrder());
            for (Date key : sessions.keySet()) {
                SessionItem item = sessions.get(key);
                Date day = instanceToDay(item.startDate);

                if (compacted.get(day) == null) {
                    compacted.put(day, new ArrayList<SearchedItem.Session>());
                }
                compacted.get(day).add(new SearchedItem.Session(item, null));
            }

            Integer section = 1;
            for (Date key : compacted.keySet()) {
                ArrayList<SearchedItem.Session> items = compacted.get(key);
                adapterSource.add(new SearchedItem.Session(null, formatter.format(key)));
                for (SearchedItem.Session item : items) {
                    adapterSource.add(section, item);
                }
                section = adapterSource.size() + 1;
            }
        } else {
            Integer section = 1;
            for (String property : filteredSessions.keySet()) {
                ArrayList<SearchedItem.Session> items = filteredSessions.get(property);
                adapterSource.add(new SearchedItem.Session(null, property));
                for (SearchedItem.Session item : items) {
                    adapterSource.add(section, item);
                }
                section = adapterSource.size() + 1;
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

        ValueEventListener listener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String lastKey = "";
                ArrayList<SessionItem> tempSessions = new ArrayList<>();
                ArrayList<Date> tempDates = new ArrayList<>();
                for(DataSnapshot aChild : dataSnapshot.getChildren()){
                    SessionItem item = new SessionItem();

                    if (lastKey == "") {
                        lastKey = aChild.getKey();
                    }

                    item.key = aChild.getKey();
                    item.foremanId = "";
                    if (aChild.hasChild("wid")) {
                        item.foremanId = aChild.child("wid").getValue(String.class);
                        Worker f = data.getWorkerFromIDString(item.foremanId);
                        if (f != null) {
                            item.foreman = f.getfName() + " " + f.getsName();
                        }
                    }

                    if (item.foreman == null) {
                        item.foreman = farmOwnerName;
                    }

                    Object ed = aChild.child("end_date");
                    Object sd = aChild.child("start_date");
                    Double endDate, startDate;
                    try {
                        endDate = aChild.child("end_date").getValue(Double.class);
                        startDate = aChild.child("start_date").getValue(Double.class);
                    } catch (Exception e) {
                        endDate = 0.0;
                        startDate = 0.0;
                    }

                    if (endDate == null) {
                        endDate = 0.0;
                    }
                    if (startDate == null) {
                        startDate = 0.0;
                    }
                    item.endDate = new Date((long) (endDate * 1000));
                    item.startDate = new Date((long) (startDate * 1000));

                    for (DataSnapshot trackSnapshot : aChild.child("track").getChildren()) {
                        Double lat = trackSnapshot.child("lat").getValue(Double.class);
                        Double lng = trackSnapshot.child("lng").getValue(Double.class);
                        Location loc = new Location("");
                        loc.setLatitude(lat == null ? 0 : lat);
                        loc.setLongitude(lng == null ? 0 : lng);

                        item.addTrack(loc);
                    }
                    for (DataSnapshot collectionSnapshot : aChild.child("collections").getChildren()) {
                        Worker w = data.getWorkerFromIDString(collectionSnapshot.getKey());
                        String fName = w == null ? "Unknown" : w.getfName();
                        String sName = w == null ? "Worker" : w.getsName();
                        String workerName = fName + " " + sName;
                        int count = 0;
                        for (DataSnapshot collection : collectionSnapshot.getChildren()) {
                            Double lat = collection.child("coord").child("lat").getValue(Double.class);
                            Double lng = collection.child("coord").child("lng").getValue(Double.class);
                            Location loc = new Location("");
                            loc.setLatitude(lat == null ? 0 : lat);
                            loc.setLongitude(lng == null ? 0 : lng);
                            Double time = collectionSnapshot.child("date").getValue(Double.class);

                            item.addCollection(collectionSnapshot.getKey(), workerName, loc, time);
                            count++;
                        }
                    }
                    tempSessions.add(item);
                    tempDates.add(item.startDate);
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
//                    recyclerView.setVisibility(View.VISIBLE);
                    recyclerRefresh.setVisibility(View.VISIBLE);
                }
                pageIndex = lastKey;
                Boolean reload = false;
                if (searchText.isEmpty()) {
                    filteredSessions = null;
                } else {
                    reload = filterSessions();
                }
                if (flattenDataSource() || reload) {
                    adapter.notifyItemInserted(adapterSource.size() - 1);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ids.add(listener);
    }

    public void pullDone(){
        endRefresh();
    }

    private void endRefresh() {
        if (sessions != null) {
            sessions.clear();
        }
        if (filteredSessions != null) {
            sessions.clear();
        }
        filteredSessions = null;
        pageIndex = null;
        getNewPage();

        recyclerRefresh.setRefreshing(false);
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

    private void getAdmin() {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("/" + user.getUid() + "/admin/");
        adminRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String fname = dataSnapshot.child("firstname").getValue(String.class);
                String sname = dataSnapshot.child("lastname").getValue(String.class);

                farmOwnerName = fname + " " + sname;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
