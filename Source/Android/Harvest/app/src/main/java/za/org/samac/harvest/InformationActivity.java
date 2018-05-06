package za.org.samac.harvest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;

import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.Data;

import static za.org.samac.harvest.InformationActivity.Category.FARM;
import static za.org.samac.harvest.InformationActivity.Category.NAV;
import static za.org.samac.harvest.InformationActivity.Category.NOTHING;
import static za.org.samac.harvest.InformationActivity.Category.ORCHARD;
import static za.org.samac.harvest.InformationActivity.Category.WORKER;

public class InformationActivity extends AppCompatActivity{

    private boolean navFragVisible = true;
    private BottomNavigationView bottomNavigationView;

    protected enum Category{
        FARM,
        ORCHARD,
        WORKER,
        NOTHING,
        NAV
    }

    Category selectedCat = NOTHING;

    @Override
    protected void onResume(){
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.actionInformation);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        //bottom navigation bar
        bottomNavigationView = findViewById(R.id.BottomNav);
        bottomNavigationView.setSelectedItemId(R.id.actionInformation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
//                                finish();
//                                startActivity(new Intent(InformationActivity.this, MainActivity.class));
                                Intent openMainActivity= new Intent(InformationActivity.this, MainActivity.class);
                                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openMainActivity, 0);
                                return true;
                            case R.id.actionInformation:
                                return true;
                            case R.id.actionSession:

                                return true;

                        }
                        return true;
                    }
                });

        //Start the first fragment
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        showNavFrag();
    }

    private void showNavFrag(){
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        InfoNavFragment navFragment = new InfoNavFragment();
        fragmentTransaction.replace(R.id.infoMainPart, navFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        selectedCat = NAV;
    }

    //Override Back Button
    @Override
    public void onBackPressed(){
        if(getSupportFragmentManager().getBackStackEntryCount() == 1){
            finish();
        }
        else {
            getSupportFragmentManager().popBackStack();
            if(getSupportFragmentManager().getBackStackEntryCount() == 2){
                //The root Nav fragment
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                setTitle("Information");
            }
            else{
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    //Handle Buttons
    public void onInfoNavButtClick(View view){
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        InfoListFragment newFragment = new InfoListFragment();
        fragmentTransaction.replace(R.id.infoMainPart, newFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        if(view.getTag().equals("farms")){
            setTitle("Farms");
            selectedCat = FARM;
            Data data = new Data();
        }
        else if (view.getTag().equals("orchards")){
            setTitle("Orchards");
            selectedCat = ORCHARD;
        }
        else if (view.getTag().equals("workers")){
            setTitle("Workers");
            selectedCat = WORKER;
        }
    }

    //Handle the menu
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
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.search:

                //The search button will have different functionality than the main.

//                MenuItem searchMenu = menu.findItem(R.id.search);
//                final SearchView searchView = (SearchView) item.getActionView();
//                searchView.setIconified(false);
//                searchView.requestFocusFromTouch();
//                searchView.setOnQueryTextListener(this);
//                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
//                    @Override
//                    public boolean onMenuItemActionExpand(MenuItem menuItem) {
//                        return true;
//                    }
//
//                    @Override
//                    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
//                        return true;
//                    }
//                });
                return true;
            case R.id.settings:
                startActivity(new Intent(InformationActivity.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(InformationActivity.this, LoginActivity.class));
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

