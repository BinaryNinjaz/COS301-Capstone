package za.org.samac.harvest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;

import static za.org.samac.harvest.util.Category.FARM;
import static za.org.samac.harvest.util.Category.NAV;
import static za.org.samac.harvest.util.Category.NOTHING;
import static za.org.samac.harvest.util.Category.ORCHARD;
import static za.org.samac.harvest.util.Category.WORKER;

//TODO: Returning overwrites progress here.
//TODO: Leaving app/locking device resets.

public class InformationActivity extends AppCompatActivity{

    private boolean navFragVisible = true;
    private BottomNavigationView bottomNavigationView;
    private Data data;
    private boolean editing = false;

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
                                showNavFrag();
                                return true;
                            case R.id.actionSession:
                                return true;
                            case R.id.actionStats:
                                Intent openSessions= new Intent(InformationActivity.this, Analytics.class);
                                openSessions.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openSessions, 0);
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

        //empty the back stack
        if(fragmentManager.getBackStackEntryCount() != 0){
            android.support.v4.app.FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(0);
            fragmentManager.popBackStack(backStackEntry.getId(), android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.executePendingTransactions();
        }

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
            if (editing){
                editing = false;
            }
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
    public void onCreateButtClick(View view){
        String choice = view.getTag().toString();
        switch (choice){
            case "FARM":
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                InfoFarmFragment newInfoFarmFragment = new InfoFarmFragment();
                fragmentTransaction.replace(R.id.infoMainPart, newInfoFarmFragment, "CREATE");
                fragmentTransaction.addToBackStack(null);
                newInfoFarmFragment.beNew(true);
                newInfoFarmFragment.setData(data);
//                setTitle("Create Farm");
                selectedCat = FARM;
                fragmentTransaction.commit();
                return;
            case "ORCHARD":
                return;
            case "WORKER":

        }
    }

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
//                setTitle("Farms");
                selectedCat = FARM;
            } else if (view.getTag().equals("orchards")) {
//                setTitle("Orchards");
                selectedCat = ORCHARD;
            } else if (view.getTag().equals("workers")) {
//                setTitle("Workers");
                selectedCat = WORKER;
            }
            newInfoListFragment.setCat(selectedCat);
            fragmentTransaction.commit();
//        newInfoListFragment.showList(selectedCat);
        }
    }


    //If a farm, orchard, worker is selected
    public void onSelectItemButtClick(View view){
        String tags[] = view.getTag().toString().split(" ");
        if (tags.length == 2){
            if (tags[1].equals("FARM")) {
                showObject(tags[0], FARM);
            }
            else if (tags[1].equals("ORCHARD")) {
                showObject(tags[0], ORCHARD);
            }
            else if (tags[1].equals("WORKER")) {
                showObject(tags[0], WORKER);
            }
        }
    }

    private void showObject(String ID, Category category){
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (category == FARM){
            selectedCat = FARM;
//            setTitle("View Farm");
            InfoFarmFragment newInfoFarmFragment = new InfoFarmFragment();
            fragmentTransaction.replace(R.id.infoMainPart, newInfoFarmFragment);
            fragmentTransaction.addToBackStack(null);
            newInfoFarmFragment.setDataAndID(data, ID);
            fragmentTransaction.commit();
        }
        else if (category == ORCHARD){
            selectedCat = ORCHARD;
//            setTitle("View Orchard");
//                InfoFarmFragment newInfoFarmFragment = new InfoFarmFragment();
//                fragmentTransaction.replace(R.id.infoMainPart, newInfoFarmFragment);
//                fragmentTransaction.addToBackStack(null);
//                newInfoFarmFragment.setDataAndID(data, tags[0]);
//                fragmentTransaction.commit();
        }
        else if (category == WORKER){
            selectedCat = WORKER;
//            setTitle("View Worker");
//                InfoFarmFragment newInfoFarmFragment = new InfoFarmFragment();
//                fragmentTransaction.replace(R.id.infoMainPart, newInfoFarmFragment);
//                fragmentTransaction.addToBackStack(null);
//                newInfoFarmFragment.setDataAndID(data, tags[0]);
//                fragmentTransaction.commit();
        }
    }

    //Switch to Editing
    public void onEditChosen(View view){
        String[] tags = view.getTag().toString().split(" ");
//        getSupportFragmentManager().popBackStack();
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (selectedCat) {
            case FARM:
//                setTitle("Editing Farm");
                InfoFarmFragment newInfoFarmFragment = new InfoFarmFragment();
                fragmentTransaction.replace(R.id.infoMainPart, newInfoFarmFragment, "EDIT");
                fragmentTransaction.addToBackStack(null);
                newInfoFarmFragment.setDataAndID(data, tags[0]);
                newInfoFarmFragment.beEditable(true);
                fragmentTransaction.commit();
                editing = true;
                return;
            case ORCHARD:
                return;
            case WORKER:
        }
    }

    //Handle a save event
    public void onSaveChosen(View view){
        String[] tags = view.getTag().toString().split(" ");
        if (tags[0].equals("SAVE")){
            switch (selectedCat) {
                case FARM:
                    InfoFarmFragment temp = (InfoFarmFragment) getSupportFragmentManager().findFragmentByTag("EDIT");
                    temp.saveEvent();
                    getSupportFragmentManager().popBackStack();
                    getSupportFragmentManager().popBackStack();
                    showObject(tags[1], selectedCat);
                    break;
                case ORCHARD:
                    break;
                case WORKER:
                    break;
            }
        }
        else if(tags[0].equals("CREATE")){
            switch (selectedCat){
                case FARM:
                    InfoFarmFragment temp = (InfoFarmFragment) getSupportFragmentManager().findFragmentByTag("CREATE");
                    temp.createEvent();
                    getSupportFragmentManager().popBackStack();
//                    showObject(tags[1], selectedCat);
                    showNavFrag();
                    break;
                case ORCHARD:
                    break;
                case WORKER:
                    break;
            }
        }
        data.push();
    }

    //Handle a delete event
    public void onDeleteChosen(View view){
        final String[] tags = view.getTag().toString().split(" ");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String cat = "", name = "";
        switch (selectedCat){
            case WORKER:
                cat = "worker";
                name = data.getActiveWorker().getfName() + " " + data.getActiveWorker().getsName();
                break;
            case ORCHARD:
                cat = "orchard";
                name = data.getActiveOrchard().getName();
                break;
            case FARM:
                cat = "farm";
                name = data.getActiveFarm().getName();
        }

        builder.setMessage("Are you sure you wish to delete " + cat + " " + name).setTitle(R.string.sure);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (tags[0].equals("LOOK")){
                    getSupportFragmentManager().popBackStack();
                }
                else{
                    getSupportFragmentManager().popBackStack();
                    getSupportFragmentManager().popBackStack();
                }
                data.deleteObject(selectedCat, tags[1]);
                data.push();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Do nothing.
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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

