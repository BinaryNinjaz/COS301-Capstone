package za.org.samac.harvest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import za.org.samac.harvest.util.AppUtil;

public class WorkerOrForeman extends AppCompatActivity{
    private BottomNavigationView bottomNavigationView;
    private ArrayList<PieEntry> entries = new ArrayList<>();
    private Button perSesWorkerComparison;
    private Button orhHistPerformance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_or_foreman);

        bottomNavigationView = findViewById(R.id.BottomNav);
        bottomNavigationView.setSelectedItemId(R.id.actionStats);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                Intent openMainActivity= new Intent(WorkerOrForeman.this, MainActivity.class);
                                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openMainActivity, 0);
                                return true;
                            case R.id.actionInformation:
                                Intent openInformation= new Intent(WorkerOrForeman.this, InformationActivity.class);
                                openInformation.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openInformation, 0);
                                return true;
                            case R.id.actionSession:
                                Intent openSessions= new Intent(WorkerOrForeman.this, SessionsMap.class);
                                openSessions.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openSessions, 0);
                                return true;
                            case R.id.actionStats:
                                return true;

                        }
                        return true;
                    }
                });

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //user selects to see pie chart
        perSesWorkerComparison = findViewById(R.id.btnWorkers);
        perSesWorkerComparison.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkerOrForeman.this, WorkersForBarGraph.class);
                startActivity(intent);
            }
        });

        //user selects to see bar graph
        orhHistPerformance = findViewById(R.id.btnForemen);
        orhHistPerformance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WorkerOrForeman.this, ForemenForBarGraph.class);
                startActivity(intent);
            }
        });
    }

    //Handle the menu
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
                startActivity(new Intent(WorkerOrForeman.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(WorkerOrForeman.this, LoginActivity.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                finish();
                return true;
//            case R.id.homeAsUp:
//                onBackPressed();
//                return true;
            default:
                super.onOptionsItemSelected(item);
                return true;
        }
//        return false;
    }

}
