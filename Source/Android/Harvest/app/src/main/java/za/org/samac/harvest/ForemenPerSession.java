package za.org.samac.harvest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

import za.org.samac.harvest.util.AppUtil;

public class ForemenPerSession extends AppCompatActivity /*RecyclerView.Adapter<ForemenPerSession.ForemenViewHolder>*/ {
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foremen_session);

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
                                Intent openSessions= new Intent(ForemenPerSession.this, Sessions.class);
                                openSessions.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openSessions, 0);
                                return true;
                            case R.id.actionStats:
                                Intent openStats= new Intent(ForemenPerSession.this, Stats.class);
                                openStats.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openStats, 0);
                                return true;

                        }
                        return true;
                    }
                });

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
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
                    startActivity(new Intent(ForemenPerSession.this, SignIn_Farmer.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                finish();
                return true;
//            case R.id.homeAsUp:
//                onBackPressed();
//                return true;
            case R.id.website:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://harvestapp.co.za/"));
                startActivity(browserIntent);
                return true;
            default:
                super.onOptionsItemSelected(item);
                return true;
        }
//        return false;
    }
}
