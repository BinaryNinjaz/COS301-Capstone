package za.org.samac.harvest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import za.org.samac.harvest.adapter.WorkerRecyclerViewAdapter;
import za.org.samac.harvest.util.AppUtil;

public class ForemenPerSession extends AppCompatActivity /*RecyclerView.Adapter<ForemenPerSession.ForemenViewHolder>*/ {
    private BottomNavigationView bottomNavigationView;
    private ArrayList<PieEntry> entries = new ArrayList<>();
    private Button perSesWorkerComparison;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        bottomNavigationView = findViewById(R.id.BottomNav);
        bottomNavigationView.setSelectedItemId(R.id.actionInformation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                Intent openMainActivity= new Intent(ForemenPerSession.this, MainActivity.class);
                                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openMainActivity, 0);
                                return true;
                            case R.id.actionInformation:
                                Intent openInformation= new Intent(ForemenPerSession.this, InformationActivity.class);
                                openInformation.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openInformation, 0);
                                return true;
                            case R.id.actionSession:
                                Intent openSessions= new Intent(ForemenPerSession.this, SessionsMap.class);
                                openSessions.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openSessions, 0);
                                return true;
                            case R.id.actionStats:
                                Intent openAnalytics= new Intent(ForemenPerSession.this, Analytics.class);
                                openAnalytics.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openAnalytics, 0);
                                return true;

                        }
                        return true;
                    }
                });

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //user selects to see pie chart
        perSesWorkerComparison = findViewById(R.id.perSesWorkerComparison);
        perSesWorkerComparison.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ForemenPerSession.this, za.org.samac.harvest.PieChart.class);
                startActivity(intent);
            }
        });
    }

    public class ForemenViewHolder extends RecyclerView.ViewHolder {
        Button foremanName;

        ForemenViewHolder(View view) {
            super(view);
            foremanName = view.findViewById(R.id.foremanName);
        }
    }

    @Override
    public ForemenPerSession.ForemenViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_foremen_per_session, parent, false);

        return new ForemenPerSession.ForemenViewHolder(itemView);
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
                startActivity(new Intent(ForemenPerSession.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(ForemenPerSession.this, LoginActivity.class));
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
