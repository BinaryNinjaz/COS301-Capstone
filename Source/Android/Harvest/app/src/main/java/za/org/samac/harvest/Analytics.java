package za.org.samac.harvest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;

import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;

/**
 * This activity handles all of the setup of getting information before calling on the appropriate graph.
 */
public class Analytics extends AppCompatActivity {
    /*
     The process is to show the initial monstrosity
     Once a time period has been selected, set the content view to the selector.
     Once the selector indicates that a selection has been made, call on the appropriate graph, and communicate the choices there.

     If a saved graph is chosen, then use the tag to read its content from SharedPreferences, and use that to call and communicate with the appropriate graph.

     If a new graph is to be created, then call the new graph activity, and let that handle everything.
     */

    /*
     The work flows are as follows:
      main -> selector -> graph
      main -> creator -> graph
      main (chooses saved graph) -> graph

     That means:
      no circular navigation
     */

    private final android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

    private BottomNavigationView bottomNavigationView;

    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "Analytics";

    //Various Fragments
    @SuppressWarnings("FieldCanBeLocal")
    private Analytics_Main analytics_main;
    private Analytics_Selector analytics_selector;
    private Analytics_Creator analytics_creator;

    //Keys for Bundles
    public static final String KEY_IDS = "KEY_IDS";
    public static final String KEY_START = "KEY_START";
    public static final String KEY_END = "KEY_END";
    public static final String KEY_INTERVAL = "KEY_INTERVAL";
    public static final String KEY_PERIOD = "KEY_PERIOD";
    public static final String KEY_GROUP = "KEY_GROUP";
    public static final String KEY_ACCUMULATION = "KEY_ACCUMULATION";
    
    public static final String NOTHING = "";

    //Intervals
    public static final String HOURLY = "hourly";
    public static final String DAILY = "daily";
    public static final String WEEKLY = "weekly";
    public static final String MONTHLY = "monthly";
    public static final String YEARLY = "yearly";

    //Periods
    public static final String BETWEEN_DATES = "between dates";
    public static final String YESTERDAY = "yesterday";
    public static final String TODAY = "today";
    public static final String THIS_WEEK = "this week";
    public static final String LAST_WEEK = "last week";
    public static final String THIS_MONTH = "this month";
    public static final String LAST_MONTH = "last month";
    public static final String THIS_YEAR = "this year";
    public static final String LAST_YEAR = "last year";

    //Groups
    public static final String ORCHARD = "orchard";
    public static final String WORKER = "worker";
    public static final String FOREMAN = "foreman";
    public static final String FARM = "farm";

    //Accumulation
    public static final String ACCUMULATION_NONE = "running";
    public static final String ACCUMULATION_ENTITY = "accumEntity";
    public static final String ACCUMULATION_INTERVAL = "accumTime";
    
    @SuppressWarnings("FieldCanBeLocal")
    public static final double THOUSAND = 1000.0000000;

    private String interval = NOTHING;

    // For the selector.
    private Data data;

    // Information needed by the graph
    ArrayList<String> ids;
    Double start, end;

    // Needed to determine the correct graph
    String group = "";

    //Activity related

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        bottomNavigationView = findViewById(R.id.BottomNav);
        bottomNavigationView.setSelectedItemId(R.id.actionStats);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                Intent openMainActivity= new Intent(Analytics.this, MainActivity.class);
                                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openMainActivity, 0);
                                return true;
                            case R.id.actionInformation:
                                startActivity(new Intent(Analytics.this, InformationActivity.class));
                                return true;
                            case R.id.actionSession:
                                startActivity(new Intent(Analytics.this, Sessions.class));
                                return true;
                            case R.id.actionStats:
                                return true;

                        }
                        return true;
                    }
                });
        showMain();

        ids = new ArrayList<>();

        //We'll do this here so it can pull before the selector if it needs to.
        data = new Data();
        data.notifyMe(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.actionStats);//set correct item to pop out on the nav bar
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
                startActivity(new Intent(Analytics.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(Analytics.this, SignIn_Farmer.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(Analytics.this, SignIn_Choose.class));
                                }
                            });
                }
                finish();
                return true;
            case android.R.id.home:
                showMain();
                return true;
            default:
                super.onOptionsItemSelected(item);
                return true;
        }
    }

    private void toggleUpButton(boolean on){
        getSupportActionBar().setDisplayHomeAsUpEnabled(on);
    }

    //Fragment display

    private void showMain(){
        //Ask the user to pick a time and group filter
        toggleUpButton(false);

        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //empty the back stack
        if(fragmentManager.getBackStackEntryCount() != 0){
            android.support.v4.app.FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(0);
            fragmentManager.popBackStack(backStackEntry.getId(), android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.executePendingTransactions();
        }

        analytics_main = new Analytics_Main();
        fragmentTransaction.replace(R.id.analMainPart, analytics_main, "MAIN");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void showSelector(){
        //Ask the user to select which things want to be displayed in the graph
        toggleUpButton(true);

        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        analytics_selector = new Analytics_Selector();
        Category temp = Category.NOTHING;
        switch (group){
            case FARM:
                temp = Category.FARM;
                break;
            case ORCHARD:
                temp = Category.ORCHARD;
                break;
            case WORKER:
                temp = Category.WORKER;
                break;
            case FOREMAN:
                temp = Category.FOREMAN;
                break;
        }
        analytics_selector.setDataAndCategory(this.data, temp);
        fragmentTransaction.replace(R.id.analMainPart, analytics_selector, "SELECTOR");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void showCustom(){
        toggleUpButton(true);

        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        analytics_creator = new Analytics_Creator();

        fragmentTransaction.replace(R.id.analMainPart, analytics_creator, "CREATOR");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    //Button handling

    public void anal_main_buttonHandler(View v){
        switch (v.getId()){
            //Create Custom Graph

            case R.id.anal_choose_cus_make:
                showCustom();
                break;
        }
    }

    public void anal_selector_buttonHandler(View v){
        //At this point, we have everything we need: group, start date, end date, and ids to show.
        switch (v.getId()){
            case R.id.anal_select_proceed:
                if (ids.size() < 1){
                    analytics_selector.selectNoneError();
                }
                else {
                    displayGraph();
                }
                break;
        }
    }

    public void anal_creator_buttonHandler(View v){

    }

    //Fragment support

    public void anal_checkEvent(View v){
        CheckBox box = (CheckBox) v;
        if (box.isChecked()){
            ids.add(box.getTag().toString());
        }
        else {
//            for (String id: ids){
//                if (id.equals(box.getTag().toString())){
//                    ids.remove(id);
//                }
//            }
            for (int i = 0; i < ids.size(); i++){
                if (ids.get(i).equals(box.getTag().toString())){
                    ids.remove(i);
                }
            }
        }
    }

    //Support functions

    public void pullDone(){
        Analytics_Selector analytics_selector = (Analytics_Selector) getSupportFragmentManager().findFragmentByTag("SELECTOR");
        if (analytics_selector != null){
            analytics_selector.endRefresh();
        }
    }

    private void displayGraph(){
        Bundle extras = new Bundle();
        extras.putStringArrayList(KEY_IDS, ids);
        extras.putDouble(KEY_START, start);
        extras.putDouble(KEY_END, end);
        extras.putString(KEY_INTERVAL, interval);
        extras.putString(KEY_GROUP, group);
        Intent intent = new Intent(this, Analytics_Graph.class).putExtras(extras);
        startActivity(intent);
    }

    /**
     * Using the selected configurations, determine the dates.
     * @param period represents the time period, must be lower case string that matches one of the finals in the analytics class
     * @return dateBundle that holds two doubles, one for start date, and the other for the end date.
     */
    public static dateBundle determineDates(String period){
        final String TAG = "Analytics_Creator-dates";

        period = period.toLowerCase();

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        switch (period){
            case Analytics.TODAY:
                Log.i(TAG, "Period is today");

                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.set(Calendar.HOUR, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Analytics.YESTERDAY:
                Log.i(TAG, "Period is yesterday");

                startCal.roll(Calendar.DATE, -1);
                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.roll(Calendar.DATE, -1);
                endCal.set(Calendar.HOUR, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Analytics.THIS_WEEK:
                Log.i(TAG, "Period is this week");

                //No accounting for locale I see...
//                startCal.set(Calendar.DAY_OF_WEEK, startCal.getActualMinimum(Calendar.DAY_OF_WEEK));
                startCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                startCal.roll(Calendar.WEEK_OF_YEAR, -1);
                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

//                endCal.set(Calendar.DAY_OF_WEEK, endCal.getActualMaximum(Calendar.DAY_OF_WEEK));
                endCal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
//                endCal.roll(Calendar.WEEK_OF_YEAR, 1);
                endCal.set(Calendar.HOUR, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Analytics.LAST_WEEK:
                Log.i(TAG, "Period is last week");

                startCal.roll(Calendar.WEEK_OF_YEAR, -1);
//                startCal.set(Calendar.DAY_OF_WEEK, startCal.getActualMinimum(Calendar.DAY_OF_WEEK);
                startCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.roll(Calendar.WEEK_OF_YEAR, -2);
//                endCal.set(Calendar.DAY_OF_WEEK, endCal.getActualMaximum(Calendar.DAY_OF_WEEK));
                endCal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                endCal.set(Calendar.HOUR, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Analytics.THIS_MONTH:
                Log.i(TAG, "Period is this month");

                startCal.set(Calendar.WEEK_OF_MONTH, startCal.getActualMinimum(Calendar.WEEK_OF_MONTH));
                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.set(Calendar.WEEK_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Analytics.LAST_MONTH:
                Log.i(TAG, "Period is last month");

                startCal.roll(Calendar.MONTH, -1);
                startCal.set(Calendar.WEEK_OF_MONTH, startCal.getActualMinimum(Calendar.WEEK_OF_MONTH));
                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.roll(Calendar.MONTH, -1);
                endCal.set(Calendar.WEEK_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Analytics.THIS_YEAR:
                Log.i(TAG, "Period is this year");

                startCal.set(Calendar.MONTH, startCal.getActualMinimum(Calendar.MONTH));
                startCal.set(Calendar.DAY_OF_MONTH, startCal.getActualMinimum(Calendar.DAY_OF_MONTH));
                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.set(Calendar.MONTH, endCal.getActualMinimum(Calendar.MONTH));
                endCal.set(Calendar.WEEK_OF_MONTH, endCal.getActualMinimum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR, endCal.getActualMinimum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMinimum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMinimum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMinimum(Calendar.MILLISECOND));
                endCal.roll(Calendar.YEAR, 1);

                break;

            case Analytics.LAST_YEAR:
                Log.i(TAG, "Period is last year");

                startCal.roll(Calendar.YEAR, -1);
                startCal.set(Calendar.MONTH, startCal.getActualMinimum(Calendar.MONTH));
                startCal.set(Calendar.DAY_OF_MONTH, startCal.getActualMinimum(Calendar.DAY_OF_MONTH));
                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.set(Calendar.MONTH, endCal.getActualMinimum(Calendar.MONTH));
                endCal.set(Calendar.WEEK_OF_MONTH, endCal.getActualMinimum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR, endCal.getActualMinimum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMinimum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMinimum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMinimum(Calendar.MILLISECOND));

                break;

            case Analytics.NOTHING:
                Log.w(TAG, "Period is nothing.");
                return null;
        }

        Log.i(TAG, "START: " + startCal.getTime().toString());
        Log.i(TAG, "END: " + endCal.getTime().toString());

        dateBundle result = new dateBundle();

        result.startDate = (double) startCal.getTimeInMillis();
        result.endDate = (double) endCal.getTimeInMillis();

        result.startDate /= Analytics.THOUSAND;
        result.endDate /= Analytics.THOUSAND;

        return result;
    }

    public static class dateBundle{
        double startDate, endDate;
    }

    /**
     * Takes an entity to make plural or singular
     * @param plurilizeMe the entity to be worked on.
     * @param plural true to return a plural, false to return the singular
     * @return the descriptor as requested.
     */
    public static String pluralizor(String plurilizeMe, boolean plural){
        plurilizeMe = plurilizeMe.toLowerCase();
        if (plurilizeMe.equals("farm") || plurilizeMe.equals("farms")){
            if (plural) {
                return "Farms";
            } else {
                return "Farm";
            }
        }
        if (plurilizeMe.equals("orchard") || plurilizeMe.equals("orchards")) {
            if (plural) {
                return "Orchards";
            } else {
                return "Orchard";
            }
        }
        if (plurilizeMe.equals("worker") || plurilizeMe.equals("workers")) {
            if (plural) {
                return "Workers";
            } else {
                return "Worker";
            }
        }
        if (plurilizeMe.equals("foreman") || plurilizeMe.equals("foremen")) {
            if (plural) {
                return "Foremen";
            } else {
                return "Foreman";
            }
        }
        return "";
    }

    /**
     * Turns hourly to hour, or hour to hourly.
     * @param time hour(ly), week(ly), etc.
     * @param ly true for hourly, false for hour.
     * @return hour(ly).
     */
    public static String timeConverter(String time, boolean ly){
        time = time.toLowerCase();
        if (time.equals("hourly") || time.equals("hour")){
            if (ly){
                return "Hourly";
            }
            else {
                return "Hour";
            }
        }
        else if (time.equals("daily") || time.equals("day")){
            if (ly){
                return "Daily";
            }
            else {
                return "Day";
            }
        }
        else if (time.equals("weekly") || time.equals("week")){
            if(ly){
                return "Weekly";
            }
            else{
                return "Week";
            }
        }
        else if (time.equals("monthly") || time.equals("month")){
            if (ly){
                return "Monthly";
            }
            else {
                return "Month";
            }
        }
        else if (time.equals("yearly") || time.equals("year")){
            if (ly){
                return "Yearly";
            }
            else {
                return "Year";
            }
        }
        return "";
    }
}
