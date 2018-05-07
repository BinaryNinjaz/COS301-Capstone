package za.org.samac.harvest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;

import static za.org.samac.harvest.util.Category.FARM;
import static za.org.samac.harvest.util.Category.NAV;
import static za.org.samac.harvest.util.Category.NOTHING;
import static za.org.samac.harvest.util.Category.ORCHARD;
import static za.org.samac.harvest.util.Category.WORKER;

public class InformationActivity extends AppCompatActivity{

    private boolean navFragVisible = true;
    private BottomNavigationView bottomNavigationView;
    private Data data;



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
        data = new Data();

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
                selectedCat = NAV;
            }
            else{
//                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    //Handle Buttons
    public void onInfoNavButtClick(View view){
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(selectedCat == NAV) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            InfoListFragment newInfoListFragment = new InfoListFragment();
            fragmentTransaction.replace(R.id.infoMainPart, newInfoListFragment);
            fragmentTransaction.addToBackStack(null);
            newInfoListFragment.setData(data);
            if (view.getTag().equals("farms")) {
                setTitle("Farms");
                selectedCat = FARM;
            } else if (view.getTag().equals("orchards")) {
                setTitle("Orchards");
                selectedCat = ORCHARD;
            } else if (view.getTag().equals("workers")) {
                setTitle("Workers");
                selectedCat = WORKER;
            }
            newInfoListFragment.setCat(selectedCat);
            fragmentTransaction.commit();
//        newInfoListFragment.showList(selectedCat);
        }
    }


    //If a farm, orchard, worker is selected
    public void onSelectItemButtClick(View view){
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if(selectedCat == FARM){
            setTitle("View Farm");
            InfoFarmFragment newInfoFarmFragment = new InfoFarmFragment();
            fragmentTransaction.replace(R.id.infoMainPart, newInfoFarmFragment);
            fragmentTransaction.addToBackStack(null);
            String ID = view.getTag().toString();
            data.setStringID(ID);
            newInfoFarmFragment.setFields(data.getName(), data.getFurther());
            fragmentTransaction.commit();
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

