package za.org.samac.harvest;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SearchView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.Inflater;

import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.ColorScheme;
import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.Farm;
import za.org.samac.harvest.util.Orchard;

import static za.org.samac.harvest.util.Category.FARM;
import static za.org.samac.harvest.util.Category.NAV;
import static za.org.samac.harvest.util.Category.NOTHING;
import static za.org.samac.harvest.util.Category.ORCHARD;
import static za.org.samac.harvest.util.Category.WORKER;

public class InformationActivity extends AppCompatActivity implements InfoOrchardMapFragment.LocNotAskAgain{

    private boolean mapLocationPermissionSessionAsked = false;
    private boolean mapLocationInformationSessionAsked = false;
    private BottomNavigationView bottomNavigationView;
    private static Data data;
    private boolean editing = false, map = false, listing = false;
    private static boolean setDateToday = false;
    private Stack<Category> backViews = new Stack<>();
    private List<LatLng> coords;
    Category selectedCat = NOTHING, stackTop = NOTHING;

    private final String TAG = "Information";

    private boolean searching = false, searched = false;
    private SearchView searchView;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);
        data = new Data();
        data.notifyMe(this);

        //bottom navigation bar
        bottomNavigationView = findViewById(R.id.BottomNav);
        bottomNavigationView.setSelectedItemId(R.id.actionInformation);
        BottomNavigationViewHelper.removeShiftMode(bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                Intent openMainActivity= new Intent(InformationActivity.this, MainActivity.class);
                                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openMainActivity, 0);
                                return true;
                            case R.id.actionInformation:
                                return true;
                            case R.id.actionSession:
                                startActivityIfNeeded(new Intent(InformationActivity.this, Sessions.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0);
                                return true;
                            case R.id.actionStats:
                                startActivityIfNeeded(new Intent(InformationActivity.this, Stats.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), 0);
                                return true;

                        }
                        return true;
                    }
                });

        //Start the first fragment
        showNavFrag();

//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    }

    @Override
    public void onResume(){
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.actionInformation);//set correct item to pop out on the nav bar
    }

    private void showNavFrag(){
        listing = false;
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
        toggleUpButton(false);
        setTitle("Information");

        if (Data.hasFarm() && Data.hasOrchard() && Data.hasWorker()){
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
        else{
            bottomNavigationView.setVisibility(View.GONE);
        }
    }

    public void tellAllPullDone(){
        InfoListFragment infoList = (InfoListFragment) getSupportFragmentManager().findFragmentByTag("LIST");
        if (infoList != null) {
            infoList.endRefresh();
        }
    }

    //Override Back Button
    @Override
    public void onBackPressed(){
        if(getSupportFragmentManager().getBackStackEntryCount() == 1){
            finish();
        }
        else {
            if (editing && !map){
                editing = false;
                switch (selectedCat) {
                    case FARM:
                        setTitle("View Farm");
                        break;
                    case WORKER:
                        setTitle("View Worker");
                        break;
                    case ORCHARD:
                        setTitle("View Orchard");
                        break;
                    default:
                        setTitle("Good Luck");
                        break;
                }
            }
            else if (map){
                map = false;
            }
            closeSearch();
            getSupportFragmentManager().popBackStack();
            if(getSupportFragmentManager().getBackStackEntryCount() == 2 && !searched){
                //The root Nav fragment
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                setTitle("Information");
                selectedCat = NAV;
            }
            else if(getSupportFragmentManager().getBackStackEntryCount() == 3 && !searched){
                data.clearActiveObjects();
                switch (selectedCat){
                    case FARM:
                        setTitle("Farms");
                        break;
                    case WORKER:
                        setTitle("Workers");
                        break;
                    case ORCHARD:
                        setTitle("Orchards");
                        break;
                    default:
                        setTitle("Good Luck");
                        break;
                }
            }
            else if (searched && getSupportFragmentManager().getBackStackEntryCount() == 2){
                //Done, get out of looking at things.
                data.clearActiveObjects();
                showNavFrag();
                searched = false;
                searching = false;
            }
            else if(!backViews.empty()){
                Category temp = backViews.pop();
                selectedCat = temp;
                switch (temp) {
                    case FARM:
                        setTitle("View Farm");
                        break;
                    case WORKER:
                        setTitle("View Worker");
                        break;
                    case ORCHARD:
                        setTitle("View Orchard");
                        break;
                    default:
                        setTitle("Good Luck");
                        break;
                }
            }
        }
    }

    private void toggleUpButton(boolean on){
        getSupportActionBar().setDisplayHomeAsUpEnabled(on);
    }

    //Handle Buttons
    public void onCreateButtClick(View view){
        String choice = view.getTag().toString();
        editing = true;
        listing = false;
        setDateToday = true;
        switch (choice){
            case "FARM":
                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                InfoFarmFragment newInfoFarmFragment = new InfoFarmFragment();
                fragmentTransaction.replace(R.id.infoMainPart, newInfoFarmFragment, "CREATE");
                fragmentTransaction.addToBackStack(null);
                newInfoFarmFragment.beNew(true);
                newInfoFarmFragment.setData(data);
                setTitle("Create Farm");
                fragmentTransaction.commit();
                selectedCat = FARM;
                return;
            case "ORCHARD":
                android.support.v4.app.FragmentManager fragmentManager1 = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction1 = fragmentManager1.beginTransaction();
                InfoOrchardFragment newInfoOrchardFragment1 = new InfoOrchardFragment();
                fragmentTransaction1.replace(R.id.infoMainPart, newInfoOrchardFragment1, "CREATE");
                fragmentTransaction1.addToBackStack(null);
                newInfoOrchardFragment1.beNew(true);
                newInfoOrchardFragment1.setData(data);

                coords = new Vector<>();
                newInfoOrchardFragment1.setCoords(coords);

                setTitle("Create Orchard");
                fragmentTransaction1.commit();
                selectedCat = ORCHARD;
                return;
            case "WORKER":
                android.support.v4.app.FragmentManager fragmentManager2 = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction2 = fragmentManager2.beginTransaction();
                InfoWorkerFragment newInfoWorkerFragment2 = new InfoWorkerFragment();
                fragmentTransaction2.replace(R.id.infoMainPart, newInfoWorkerFragment2, "CREATE");
                fragmentTransaction2.addToBackStack(null);
                newInfoWorkerFragment2.beNew(true);
                newInfoWorkerFragment2.setData(data);
                setTitle("Create Worker");
                fragmentTransaction2.commit();
                selectedCat = WORKER;
                return;
        }
    }

    public void onInfoNavButtClick(View view){
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(selectedCat == NAV) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            InfoListFragment newInfoListFragment = new InfoListFragment();
            fragmentTransaction.replace(R.id.infoMainPart, newInfoListFragment, "LIST");
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
            stackTop = selectedCat;
            newInfoListFragment.setCat(selectedCat);
            fragmentTransaction.commit();
//        newInfoListFragment.showList(selectedCat);
            toggleUpButton(true);
            listing = true;
        }
    }

    public void showList(Category cat){
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        showNavFrag();
        listing = true;
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        InfoListFragment newInfoListFragment = new InfoListFragment();
        fragmentTransaction.replace(R.id.infoMainPart, newInfoListFragment);
        fragmentTransaction.addToBackStack(null);
        newInfoListFragment.setData(data);
        if (cat == FARM) {
            setTitle("Farms");
            selectedCat = FARM;
        } else if (cat == ORCHARD) {
            setTitle("Orchards");
            selectedCat = ORCHARD;
        } else if (cat == WORKER) {
            setTitle("Workers");
            selectedCat = WORKER;
        }
        newInfoListFragment.setCat(selectedCat);
        fragmentTransaction.commit();
        backViews.clear();
//        newInfoListFragment.showList(selectedCat);
        toggleUpButton(true);
    }

    //If a farm, orchard, worker is selected
    public void onSelectItemButtClick(View view){
        listing = false;
        String tags[] = view.getTag().toString().split(" ");
        if (tags.length == 2){
            switch (tags[1]) {
                case "FARM":
                    showObject(tags[0], FARM);
                    break;
                case "ORCHARD":
                    showObject(tags[0], ORCHARD);
                    break;
                case "WORKER":
                    showObject(tags[0], WORKER);
                    break;
            }
        }
    }

    private void showObject(String ID, Category category){
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (category == FARM){
            selectedCat = FARM;
            setTitle("View Farm");
            InfoFarmFragment newInfoFarmFragment = new InfoFarmFragment();
            fragmentTransaction.replace(R.id.infoMainPart, newInfoFarmFragment);
            fragmentTransaction.addToBackStack(null);
            newInfoFarmFragment.setDataAndID(data, ID);
            fragmentTransaction.commit();
        }
        else if (category == ORCHARD){
            selectedCat = ORCHARD;
            setTitle("View Orchard");
            InfoOrchardFragment newInfoOrchardFragment = new InfoOrchardFragment();
            fragmentTransaction.replace(R.id.infoMainPart, newInfoOrchardFragment);
            fragmentTransaction.addToBackStack(null);
            newInfoOrchardFragment.setDataAndID(data, ID);
            fragmentTransaction.commit();
        }
        else if (category == WORKER){
            selectedCat = WORKER;
            setTitle("View Worker");
            InfoWorkerFragment newInfoWorkerFragment = new InfoWorkerFragment();
            fragmentTransaction.replace(R.id.infoMainPart, newInfoWorkerFragment);
            fragmentTransaction.addToBackStack(null);
            newInfoWorkerFragment.setDataAndID(data, ID);
            fragmentTransaction.commit();
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
                setTitle("Edit Farm");
                InfoFarmFragment newInfoFarmFragment = new InfoFarmFragment();
                fragmentTransaction.replace(R.id.infoMainPart, newInfoFarmFragment, "EDIT");
                fragmentTransaction.addToBackStack(null);
                newInfoFarmFragment.setDataAndID(data, tags[0]);
                newInfoFarmFragment.beEditable(true);
                fragmentTransaction.commit();
                editing = true;
                return;
            case ORCHARD:
                setTitle("Edit Orchard");
                InfoOrchardFragment newInfoOrchardFragment = new InfoOrchardFragment();
                fragmentTransaction.replace(R.id.infoMainPart, newInfoOrchardFragment, "EDIT");
                fragmentTransaction.addToBackStack(null);
                newInfoOrchardFragment.setDataAndID(data, tags[0]);
                newInfoOrchardFragment.beEditable(true);
                fragmentTransaction.commit();
                editing = true;
                return;
            case WORKER:
                setTitle("Edit Worker");
                InfoWorkerFragment newInfoWorkerFragment = new InfoWorkerFragment();
                fragmentTransaction.replace(R.id.infoMainPart, newInfoWorkerFragment, "EDIT");
                fragmentTransaction.addToBackStack(null);
                newInfoWorkerFragment.setDataAndID(data, tags[0]);
                newInfoWorkerFragment.beEditable(true);
                fragmentTransaction.commit();
                editing = true;
                return;
        }
    }

    //Handle a save event
    public void onSaveChosen(View view){
        String[] tags = view.getTag().toString().split(" ");
        editing = false;
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
                    InfoOrchardFragment temp1 = (InfoOrchardFragment) getSupportFragmentManager().findFragmentByTag("EDIT");
                    temp1.saveEvent();
                    getSupportFragmentManager().popBackStack();
                    getSupportFragmentManager().popBackStack();
                    showObject(tags[1], selectedCat);
                    break;
                case WORKER:
                    InfoWorkerFragment temp2 = (InfoWorkerFragment) getSupportFragmentManager().findFragmentByTag("EDIT");
                    temp2.saveEvent();
                    getSupportFragmentManager().popBackStack();
                    getSupportFragmentManager().popBackStack();
                    showObject(tags[1], selectedCat);
                    break;
            }
        }
        else if(tags[0].equals("CREATE")){
            switch (selectedCat){
                case FARM:
                    InfoFarmFragment temp = (InfoFarmFragment) getSupportFragmentManager().findFragmentByTag("CREATE");
                    temp.createEvent();
                    getSupportFragmentManager().popBackStack();
                    showList(FARM);
                    break;
                case ORCHARD:
                    InfoOrchardFragment temp1 = (InfoOrchardFragment) getSupportFragmentManager().findFragmentByTag("CREATE");
                    temp1.createEvent();
                    getSupportFragmentManager().popBackStack();
                    showList(ORCHARD);
                    break;
                case WORKER:
                    InfoWorkerFragment temp2 = (InfoWorkerFragment) getSupportFragmentManager().findFragmentByTag("CREATE");
                    temp2.createEvent();
                    getSupportFragmentManager().popBackStack();
                    showList(WORKER);
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

                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showList(selectedCat);
                        data.deleteObject(Category.WORKER, tags[1]);
                        data.push();
                    }
                });

                break;
            case ORCHARD:
                cat = "orchard";
                name = data.getActiveOrchard().getName();

                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showList(selectedCat);
                        data.deleteObject(Category.ORCHARD, tags[1]);
                        data.push();
                    }
                });

                break;
            case FARM:
                cat = "farm";
                name = data.getActiveFarm().getName();

                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showList(selectedCat);
                        data.deleteObject(FARM, tags[1]);
                        data.push();
                    }
                });

        }

        builder.setMessage("Are you sure you wish to delete " + cat + " " + name).setTitle(R.string.sure);

//        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (tags[0].equals("LOOK")){
////                    getSupportFragmentManager().popBackStack();
//                    onBackPressed();
//                }
//                else{
////                    getSupportFragmentManager().popBackStack();
////                    getSupportFragmentManager().popBackStack();
//                    onBackPressed();
//                    onBackPressed();
//                }
//                data.deleteObject(selectedCat, tags[1]);
//                data.push();
//            }
//        });
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
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.search:
                searchView = (SearchView) item.getActionView();
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem menuItem) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                        closeSearch();
                        showNavFrag();
                        return true;
                    }
                });
                showSearch();
                return true;
            case R.id.settings:
                startActivity(new Intent(InformationActivity.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(InformationActivity.this, SignIn_Choose.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(InformationActivity.this, SignIn_Choose.class));
                                }
                            });
                }
                finish();
                return true;
            case android.R.id.home:
                if (listing){
                    showNavFrag();
                    closeSearch();
                }
                else if (map){
                    onBackPressed();
                }
                else if (searched){
                    showNavFrag();
                    searched = false;
                }
                else {
                    showList(stackTop);
                }
                return true;
            case R.id.website:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://harvestapp.co.za/"));
                startActivity(browserIntent);
                return true;
            default:
                super.onOptionsItemSelected(item);
                return true;
        }
    }

    //Also when search result selected.
    public void onGotoButtClick(View view){
        if (searching) {
            closeSearch();
            toggleUpButton(true);
            searched = true;
        }

        Category cat;
        String token[] = view.getTag().toString().split(" ");
        switch (token[0]){
            case "Farm":
                cat = FARM;
                break;
            case "Orchard":
                cat = ORCHARD;
                break;
            case "Worker":
                cat = WORKER;
                break;
            default:
                cat = NOTHING;
                break;
        }
        backViews.push(selectedCat);
        data.setCategory(cat);
        showObject(token[1], cat);
    }

    public void showDateSpinner(View v){
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            int year, month, day;
            Calendar c;
            if (setDateToday){
                c = Calendar.getInstance();
                setDateToday = false;
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            }
            else {
                InfoOrchardFragment frag = (InfoOrchardFragment) getFragmentManager().findFragmentByTag("CREATE");
                if (frag == null){
                    frag = (InfoOrchardFragment) getFragmentManager().findFragmentByTag("EDIT");
                }
                String[] dat = frag.getSetDate();
                year = Integer.parseInt(dat[2]);
                month = Integer.parseInt(dat[1]) - 1;
                day = Integer.parseInt(dat[0]);
            }

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day){
            InfoOrchardFragment frag = (InfoOrchardFragment) getFragmentManager().findFragmentByTag("CREATE");
            if (frag == null){
                frag = (InfoOrchardFragment) getFragmentManager().findFragmentByTag("EDIT");
            }
            frag.biteMe(day, month + 1, year);
        }
    }

    public void addCultivar(View v){
        InfoOrchardFragment frag = (InfoOrchardFragment) getSupportFragmentManager().findFragmentByTag("CREATE");
        if (frag == null){
            frag = (InfoOrchardFragment) getSupportFragmentManager().findFragmentByTag("EDIT");
        }
        frag.addCult();
    }

    public void onCultivarDelete(View v){
        InfoOrchardFragment frag = (InfoOrchardFragment) getSupportFragmentManager().findFragmentByTag("CREATE");
        if (frag == null){
            frag = (InfoOrchardFragment) getSupportFragmentManager().findFragmentByTag("EDIT");
        }
        frag.delCult(Integer.parseInt(v.getTag().toString()));
    }

    public void onLocMapShowClick(View v){
        map = true;
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        InfoOrchardMapFragment infoOrchardMapFragment = new InfoOrchardMapFragment();
        infoOrchardMapFragment.setPermissionAskedInSession(mapLocationPermissionSessionAsked);
        infoOrchardMapFragment.setLocationInformationAskedInSession(mapLocationInformationSessionAsked);
        fragmentTransaction.replace(R.id.infoMainPart, infoOrchardMapFragment, "MAP");
        fragmentTransaction.addToBackStack(null);
        infoOrchardMapFragment.setMapShowBottomBit(editing);

        //Set the correct coords for the map
        Orchard orchard = data.getActiveOrchard();
        if (orchard == null){
            //It's new, so give it these coords, which will be saved in the orchard fragment.
            infoOrchardMapFragment.setCoordinates(coords);
            infoOrchardMapFragment.inferArea = true;
        }
        else {
            //It's not new, so just give it the same coords that the data has, and the map will update those.
            infoOrchardMapFragment.setCoordinates(orchard.getCoordinates());
            infoOrchardMapFragment.inferArea = orchard.getInferArea();
        }
        if (orchard != null) {
            Farm farm = orchard.getAssignedFarm();
            String farmId = farm == null ? "" : farm.getID();
            infoOrchardMapFragment.setFillColor(ColorScheme.hashColor(farmId, orchard.getID(), 64));
            infoOrchardMapFragment.setStrokeColor(ColorScheme.hashColor(farmId, orchard.getID(), 191));
        }
        else {
            infoOrchardMapFragment.setFillColor(ColorScheme.hashColor("   ", "   ", 64));
            infoOrchardMapFragment.setStrokeColor(ColorScheme.hashColor("   ", "   ", 191));
        }

        fragmentTransaction.commit();
    }

    public void onOrchMapRemAllClick(View view){
        InfoOrchardMapFragment temp = (InfoOrchardMapFragment) getSupportFragmentManager().findFragmentByTag("MAP");
        temp.erase();
    }

    public void onOrchMapRemLastClick(View view){
        InfoOrchardMapFragment temp = (InfoOrchardMapFragment) getSupportFragmentManager().findFragmentByTag("MAP");
        temp.removeLast();
    }

    public void onInfoOrchardMapMapTypeClick(View view) {
        final InfoOrchardMapFragment temp = (InfoOrchardMapFragment) getSupportFragmentManager().findFragmentByTag("MAP");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Map Type");

        final ArrayList<String> optionsArray = new ArrayList<>();

        optionsArray.add("Hybrid");
        optionsArray.add("Satellite");
        optionsArray.add("Normal");
        optionsArray.add("Terrain");
        optionsArray.add("Cancel");

        final CharSequence []options = optionsArray.toArray(new CharSequence[5]);

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String option = optionsArray.get(which);
                Button btn  = findViewById(R.id.info_orch_map_mapType_btn);
                if (option.compareTo("Hybrid") == 0) {
                    temp.gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                } else if (option.compareTo("Satellite") == 0) {
                    temp.gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else if (option.compareTo("Normal") == 0) {
                    temp.gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                } else if (option.compareTo("Terrain") == 0) {
                    temp.gMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                }
                if (option.compareTo("Cancel") != 0 && btn != null) {
                    btn.setText(options[which]);
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void onCheck(View view){
        InfoWorkerFragment temp = (InfoWorkerFragment) getSupportFragmentManager().findFragmentByTag("CREATE");
        if (temp == null){
            temp = (InfoWorkerFragment) getSupportFragmentManager().findFragmentByTag("EDIT");
        }
        temp.checkEvent(view);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        InfoOrchardMapFragment temp = (InfoOrchardMapFragment) getSupportFragmentManager().findFragmentByTag("MAP");
        switch (requestCode){
            case InfoOrchardMapFragment.PERMISSION_REQUEST_COARSE_LOCATION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    temp.activateLocation();
                }
                else {
                    if (!temp.isExplanationShown()){
                        temp.permissionAsk();
                    }
                }
                mapLocationPermissionSessionAsked = true;
                break;
        }
    }

    public void LocationInformationAsked(){
        mapLocationInformationSessionAsked = true;
    }

    //Everything related to searching
    /*
     The user hits the search button
     Display the search fragment
     Inflate the search bar at the top
     */
    private void showSearch(){
        searching = true;

        //show the search fragment
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        final InfoSearchFragment searchFrag = new InfoSearchFragment();
        fragmentTransaction.replace(R.id.infoMainPart, searchFrag, "SEARCHER");
        searchFrag.setData(data);
        searchFrag.setCategory(selectedCat);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        searchView.setIconified(false);
        searchView.requestFocusFromTouch();
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchFrag.searchForQuery(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchFrag.searchForQuery(s);
                return true;
            }
        });
    }

    private void closeSearch(){
        if (searching){
            searching = false;

            searchView.setQuery("", false);

            menu.findItem(R.id.search).collapseActionView();
        }
    }
}

