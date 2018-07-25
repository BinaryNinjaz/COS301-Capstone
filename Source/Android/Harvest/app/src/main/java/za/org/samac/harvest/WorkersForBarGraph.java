package za.org.samac.harvest;

import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import za.org.samac.harvest.adapter.WorkerForBarGraphRecyclerViewAdapter;
import za.org.samac.harvest.util.AppUtil;

public class WorkersForBarGraph extends AppCompatActivity {
    private ArrayList<String> workers;
    private ArrayList<String> workerKeys;
    private DatabaseReference workersRef;
    private FirebaseDatabase database;
    private WorkerForBarGraphRecyclerViewAdapter adapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foremen_for_bar_graph);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase

        //bottom nav bar
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.actionStats);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                startActivity(new Intent(WorkersForBarGraph.this, MainActivity.class));
                                return true;
                            case R.id.actionInformation:
                                Intent openInformation= new Intent(WorkersForBarGraph.this, InformationActivity.class);
                                openInformation.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openInformation, 0);
                                return true;
                            case R.id.actionSession:
                                Intent openSessions= new Intent(WorkersForBarGraph.this, Sessions.class);
                                openSessions.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openSessions, 0);
                                return true;
                            case R.id.actionStats:
                                return true;
                        }
                        return true;
                    }
                });

        init();
        collectOrchards();
    }

    @Override
    public void onResume(){
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.actionStats);//set correct item to pop out on the nav bar
    }

    public void init() {
        this.workers = new ArrayList<>();
        this.workerKeys = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
    }

    public void collectOrchards() {
        workersRef = database.getReference(MainActivity.farmerKey + "/workers");
        workersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.child("type").getValue(String.class).equals("Worker")) {
                        workerKeys.add(child.getKey().toString());
                        workers.add(child.child("name").getValue(String.class)+" "+child.child("surname").getValue(String.class));
                    }
                }

                adapter = new WorkerForBarGraphRecyclerViewAdapter(getApplicationContext(), workers, workerKeys);
                recyclerView = findViewById(R.id.recView);//this encapsulates the worker buttons, it is better than gridview
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setHasFixedSize(false);
                recyclerView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
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
                return true;
            case R.id.settings:
                startActivity(new Intent(WorkersForBarGraph.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(WorkersForBarGraph.this, SignIn_Choose.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(WorkersForBarGraph.this, SignIn_Choose.class));
                                }
                            });
                }
                finish();
                return true;
//            case R.id.homeAsUp:
//                onBackPressed();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
//        return false;
    }
}
