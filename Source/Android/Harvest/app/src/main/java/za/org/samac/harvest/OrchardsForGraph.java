package za.org.samac.harvest;

import android.content.Intent;
import android.media.MediaCas;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import za.org.samac.harvest.adapter.OrchardsForGraphRVAdapter;

public class OrchardsForGraph extends AppCompatActivity {

    private ArrayList<String> orchards;
    private DatabaseReference orchardsRef;
    private FirebaseDatabase database;
    private OrchardsForGraphRVAdapter adapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orchards_for_graph);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);//put progress bar until data is retrieved from firebase

        //bottom nav bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.actionSession);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                startActivity(new Intent(OrchardsForGraph.this, MainActivity.class));
                                return true;
                            case R.id.actionInformation:
                                Intent openInformation= new Intent(OrchardsForGraph.this, InformationActivity.class);
                                openInformation.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openInformation, 0);
                                return true;
                            case R.id.actionSession:
                                Intent openSessions= new Intent(OrchardsForGraph.this, Sessions.class);
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

    public void init() {
        this.orchards = new ArrayList<>();
        database = FirebaseDatabase.getInstance();
    }

    public void collectOrchards() {
        orchardsRef = database.getReference(MainActivity.farmerKey + "/orchards");
        orchardsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    orchards.add(child.child("name").getValue(String.class));
                }

                adapter = new OrchardsForGraphRVAdapter(getApplicationContext(), orchards);
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
}
