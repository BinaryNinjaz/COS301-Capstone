package za.org.samac.harvest;

import android.content.Intent;
import android.os.AsyncTask;
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

import com.google.android.gms.common.api.Response;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;

import za.org.samac.harvest.adapter.SessionsViewAdapter;
import za.org.samac.harvest.domain.Worker;
import za.org.samac.harvest.util.AppUtil;

import static za.org.samac.harvest.MainActivity.farmerKey;
import static za.org.samac.harvest.MainActivity.getForemen;

public class Sessions extends AppCompatActivity {

    private TreeMap<String, SessionItem.Selection> sessions; //used to store session data
    private ArrayList<String> dates;
    private Map<String, String> foremenID; //used to look up name with foreman id
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    private DatabaseReference sessionsRef;
    private DatabaseReference collectionsRef;
    private ArrayList<Worker> foremen;
    private RecyclerView recyclerView;
    private SessionsViewAdapter adapter;
    private ProgressBar progressBar;
    BottomNavigationView bottomNavigationView;

    private Integer pageNo = 0;
    private Integer pageSize = 8;

    private String urlSessionText() {
        String base = "https://us-central1-harvest-ios-1522082524457.cloudfunctions.net/flattendSessions?";
        base = base + "pageNo=" + pageNo.toString();
        base = base + "&pageSize=" + pageSize.toString();
        base = base + "&uid=" + farmerKey;
        return base;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase

        foremen = getForemen(); // get worker info to loop through it
        foremenID = new HashMap<>();
        for(int i = 0 ; i < foremen.size() ; ++i) {
            String id = foremen.get(i).getID();
            String name = foremen.get(i).getName();
            foremenID.put(id, name);
        }

        dates = new ArrayList<>();
        sessions = new TreeMap<>();
        uid = user.getUid();

        getNewPage();

        //bottom nav bar
        bottomNavigationView = findViewById(R.id.bottom_navigation);

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
                                /*Intent openAnalytics= new Intent(Sessions.this, Analytics.class);
                                openAnalytics.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openAnalytics, 0);*/
                                startActivity(new Intent(Sessions.this, Analytics.class));
                                return true;
                        }
                        return true;
                    }
                });

        adapter = new SessionsViewAdapter(getApplicationContext(), this);
        adapter.setSessions(sessions);
        adapter.setDates(dates);

    }

    @Override
    public void onResume(){
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.actionSession);//set correct item to pop out on the nav bar
    }

    private void addButtons() {
        adapter.setSessions(sessions);
        adapter.setDates(dates);
        recyclerView = findViewById(R.id.recView);
        //RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        //recyclerView.addItemDecoration(new DividerItemDecoration(Sessions.this, GridLayoutManager.VERTICAL));
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
    }

    public void getNewPage() {
        pageNo++;
        try {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try  {
                        String response = sendGet(urlSessionText());
                        System.out.println(response);
                        JSONArray objs = new JSONArray(response);
                        for (int i = 0; i < objs.length(); i++) {
                            JSONObject obj = objs.getJSONObject(i);
                            SessionItem.Selection item = new SessionItem.Selection();
                            item.key = obj.getString("key");
                            item.startDate = new Date((long) (obj.getDouble("start_date") * 1000));
                            if (obj.has("wid")) {
                                item.foreman = foremenID.get(obj.getString("wid"));
                            }

                            if (item.foreman == null) {
                                item.foreman = "Farm Owner";
                            }

                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                            formatter.setCalendar(Calendar.getInstance());
                            final String date = formatter.format(item.startDate);

                            dates.add(date);
                            sessions.put(date, item);
                            runOnUiThread(new Runnable() {
                                public void run(){
                                    if (pageNo == 1) {
                                        recyclerView = findViewById(R.id.recView);
                                        //RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
                                        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                        //recyclerView.addItemDecoration(new DividerItemDecoration(Sessions.this, GridLayoutManager.VERTICAL));
                                        recyclerView.setHasFixedSize(false);
                                        recyclerView.setAdapter(adapter);
                                        progressBar.setVisibility(View.GONE);//put progress bar until data is retrieved from firebase
                                        recyclerView.setVisibility(View.VISIBLE);
                                    }
                                    adapter.notifyItemInserted(sessions.size() - 1);
                                }
                            });
                        }

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
    }
}
