package za.org.samac.harvest;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;

/**
 * This activity handles all of the setup of getting information before calling on the appropriate graph.
 */
@SuppressWarnings("FieldCanBeLocal")
public class Stats extends AppCompatActivity implements SavedGraphsAdapter.HoldListener{

    private final android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

    private BottomNavigationView bottomNavigationView;

    private final String TAG = "Stats";

    //Various Fragments
    private Stats_Main stats_main;
    private Stats_Selector stats_selector;
    private Stats_Creator stats_creator;

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
    public static final String ACCUMULATION_TIME = "accumTime";

    public static final double THOUSAND = 1000.0000000;

    private String interval = NOTHING;
    private String accumulation = NOTHING;
    private String period = NOTHING;
    private String name = null;

    private Dialog dialog = null;

    private enum State{
        MAIN,
        CREATE,
        SELECTOR_THROUGH_MAIN,
        SELECTOR_THROUGH_CREATE
    }
    private State state;

    // For the selector.
    private Data data;

    // Information needed by the graph
    ArrayList<String> ids;
    Double start, end;

    // Needed to determine the correct graph
    String group = NOTHING;

    Category lastCategory = Category.NOTHING;

    //Activity related
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        bottomNavigationView = findViewById(R.id.BottomNav);
        bottomNavigationView.setSelectedItemId(R.id.actionStats);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.actionYieldTracker:
                                Intent openMainActivity= new Intent(Stats.this, MainActivity.class);
                                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivityIfNeeded(openMainActivity, 0);
                                return true;
                            case R.id.actionInformation:
                                startActivity(new Intent(Stats.this, InformationActivity.class));
                                return true;
                            case R.id.actionSession:
                                startActivity(new Intent(Stats.this, Sessions.class));
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
                startActivity(new Intent(Stats.this, SettingsActivity.class));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                if(!AppUtil.isUserSignedIn()){
                    startActivity(new Intent(Stats.this, SignIn_Farmer.class));
                }
                else {
//                    FirebaseAuth.getInstance().signOut();
                }
                if (SignIn_Farmer.mGoogleSignInClient != null) {
                    SignIn_Farmer.mGoogleSignInClient.signOut().addOnCompleteListener(this,
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(Stats.this, SignIn_Choose.class));
                                }
                            });
                }
                finish();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                super.onOptionsItemSelected(item);
                return true;
        }
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    @Override
    public void onBackPressed() {
        switch (state){
            case MAIN:

                finish();

                return;

            case CREATE:

                fragmentManager.popBackStack();
                state = State.MAIN;
                toggleUpButton(false);

                return;
            case SELECTOR_THROUGH_MAIN:

                fragmentManager.popBackStack();
                state = State.MAIN;
                toggleUpButton(false);

                return;
            case SELECTOR_THROUGH_CREATE:

                captureSelections();
                fragmentManager.popBackStack();
                state = State.CREATE;

                return;
        }
    }

    private void captureSelections(){
        if (state == State.SELECTOR_THROUGH_CREATE){
            Category category;
            switch (group){
                case FARM:
                    category = Category.FARM;
                    break;
                case ORCHARD:
                    category = Category.ORCHARD;
                    break;
                default:
                    category = Category.WORKER;
                    break;
            }
            StringBuilder builder = new StringBuilder();
            for (String id : ids){
                builder.append(data.toStringID(id, category)).append(", ");
            }
            if (builder.length() > 2) {
                builder.delete(builder.length() - 2, builder.length()).append(".");
            }
            stats_creator.setSelectedItemsText(builder.toString());
        }
    }

    private void toggleUpButton(boolean on){
        getSupportActionBar().setDisplayHomeAsUpEnabled(on);
    }


    //Fragment display
    private void showMain(){
        //Ask the user to pick a time and group filter
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //empty the back stack
        if(fragmentManager.getBackStackEntryCount() != 0){
            android.support.v4.app.FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(0);
            fragmentManager.popBackStack(backStackEntry.getId(), android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.executePendingTransactions();
        }

        stats_main = new Stats_Main();
        fragmentTransaction.replace(R.id.statsMainPart, stats_main, "MAIN");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        toggleUpButton(false);
        state = State.MAIN;
    }

    private void showSelector(){
        //Ask the user to select which things want to be displayed in the graph

        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        stats_selector = new Stats_Selector();
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
        if (temp != lastCategory){
            ids.clear();
            lastCategory = temp;
        }
        stats_selector.setDataAndCategory(this.data, temp);
        stats_selector.setIDs(ids);
        fragmentTransaction.replace(R.id.statsMainPart, stats_selector, "SELECTOR");
        if (state == State.CREATE) {
            stats_selector.showProceed(false);
        }
        else {
            stats_selector.showProceed(true);
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        toggleUpButton(true);
        if (state == State.MAIN){
            state = State.SELECTOR_THROUGH_MAIN;
        }
        else {
            state = State.SELECTOR_THROUGH_CREATE;
        }
    }

    private void showCreate(){
        toggleUpButton(true);
        data.toggleCheckedness(false);
        ids.clear();

        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        stats_creator = new Stats_Creator();

        fragmentTransaction.replace(R.id.statsMainPart, stats_creator, "CREATOR");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        state = State.CREATE;
    }


    //Button handling
    public void stats_main_buttonHandler(View v){
        switch (v.getId()){
            //Create Custom Graph

            case R.id.stats_choose_cus_make:
                showCreate();
                break;
        }
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    public void stats_selector_buttonHandler(View v){
        switch (v.getId()){
            case R.id.stats_select_proceed:
                displayGraph();
                return;
            case R.id.stats_select_all:
                stats_selector.checkAllPerhaps(true);
                return;
            case R.id.stats_select_none:
                stats_selector.checkAllPerhaps(false);
                return;
        }
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    public void stats_creator_buttonHandler(View v){
        switch (v.getId()){
            case R.id.stats_create_selectionButton:

                //They want to select things.
                group = stats_creator.getGroup();
                showSelector();
                stats_creator.notifySelectionMade();

                return;

            case R.id.stats_create_dispButton:

                //Get the bundle of joy.
                Bundle bundle = stats_creator.getConfigurations();
                if (bundle != null){
                    group = bundle.getString(KEY_GROUP);
                    //noinspection ConstantConditions
                    if (!group.equals(getGroupFromCategory(lastCategory))){
                        ids.clear();
                    }
                    period = bundle.getString(KEY_PERIOD);
                    interval = bundle.getString(KEY_INTERVAL);
                    accumulation = bundle.getString(KEY_ACCUMULATION);

                    //handle dates
                    if (!period.equals(BETWEEN_DATES)){
                        DateBundle dateBundle = determineDates(period);
                        assert dateBundle != null;
                        start = dateBundle.startDate;
                        end = dateBundle.endDate;
                    }
                    else {
                        start = bundle.getDouble(KEY_START);
                        end = bundle.getDouble(KEY_END);
                    }

                    displayGraph();
                }

                return;

            case R.id.stats_create_saveButton:

                //Check the input
                if (stats_creator.isInputValid()){
                    //Get the ball rolling
                    askForGraphName("", null, true);
                }

                return;
        }
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    public void stats_popup_buttonHandler(View v){
        switch (v.getId()){
            case R.id.stats_popup_renameButton:

                //ask for the new name and tell it to update the current graph.
                askForGraphName(name, null, false);

                return;

            case R.id.stats_popup_deleteButton:

                GraphDB.deleteGraph(name, this);
                dismissPopup();
                stats_main.updateRecyclers(Category.FARM);

                return;

            case R.id.stats_popup_cancelButton:

                dismissPopup();

                return;
        }
    }

    public void stats_main_graph_chosen(View v){
        //Have the name, so get the info from SharedPreferences, and send it forward.
        Button button = (Button) v;
        GraphDB.Graph graph = GraphDB.getGraphByName(button.getText().toString(), this);

        //TODO: Go through selector.

        //Set all the things
        assert graph != null;
        ids = new ArrayList<>(Arrays.asList(graph.ids));
        start = graph.start;
        end = graph.end;
        interval = graph.interval;
        group = graph.group;
        period = graph.period;
        accumulation = graph.accumulation;

        Category category;
        switch (group){
            case FARM: category = Category.FARM; break;
            case ORCHARD: category = Category.ORCHARD; break;
            default: category = Category.WORKER; break;
        }

        data.toggleCheckedness(false);
        for(String id : ids){
            data.findObject(id, category);
            data.getActiveThing().checked = true;
        }

        showSelector();
    }

    //Fragment support
    public void stats_checkEvent(View v){
        CheckBox box = (CheckBox) v;
        if (box.isChecked()){
            ids.add(box.getTag().toString());
        }
        else {
            for (int i = 0; i < ids.size(); i++){
                if (ids.get(i).equals(box.getTag().toString())){
                    ids.remove(i);
                }
            }
        }
    }

    public void showPopup(final String name){
        this.name = name;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_graph_popup, null);
        TextView title = v.findViewById(R.id.stats_popup_title);
        TextView hint = v.findViewById(R.id.stats_popup_hint);
        title.setText(name);
        hint.setText(getResources().getString(R.string.stats_popup_hint, name));

        builder.setView(v);

        dialog = builder.create();
        dialog.show();
    }

    public void dismissPopup(){
        if (dialog != null){
            this.dialog.dismiss();
            this.dialog = null;
            this.name = null;
        }
        else {
            Log.e(TAG, "Attempted to dismiss popup not showing.");
        }
    }

    //Support functions
    private void askForGraphName(final String name, String error, final boolean save) {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.stats_save_title));
        final EditText editText = new EditText(this);
        editText.setHint(getResources().getString(R.string.stats_save_enterName));
        editText.setText(name);
        editText.setError(error);
        editText.requestLayout();
        builder.setView(editText);

        builder.setPositiveButton(getResources().getString(R.string.stats_save_okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(save) saveGraph(editText.getText().toString());
                else updateGraphName(name, editText.getText().toString());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.stats_save_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void pullDone(){
        Stats_Selector stats_selector = (Stats_Selector) getSupportFragmentManager().findFragmentByTag("SELECTOR");
        if (stats_selector != null){
            stats_selector.endRefresh();
        }
    }

    private void displayGraph(){
        Bundle extras = new Bundle();
        extras.putStringArrayList(KEY_IDS, ids);
        extras.putDouble(KEY_START, start);
        extras.putDouble(KEY_END, end);
        extras.putString(KEY_INTERVAL, interval);
        extras.putString(KEY_GROUP, group);
        extras.putString(KEY_PERIOD, period);
        extras.putString(KEY_ACCUMULATION, accumulation);
        Intent intent = new Intent(this, Stats_Graph.class).putExtras(extras);
        startActivity(intent);
    }

    private void saveGraph(String name){
        //Make a graph for the 'DB'
        GraphDB.Graph graph = new GraphDB.Graph();

        //From the name asking popup
        graph.name = name;

        //Get the bundle
        Bundle bundle = stats_creator.getConfigurations();

        //From the bundle
        graph.group = bundle.getString(KEY_GROUP);
        graph.period = bundle.getString(KEY_PERIOD);
        graph.interval = bundle.getString(KEY_INTERVAL);
        graph.accumulation = bundle.getString(KEY_ACCUMULATION);

        //From the activity
        if (!graph.group.equals(getGroupFromCategory(lastCategory))){
            ids.clear();
        }
        String gIDs[] = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++){
            gIDs[i] = ids.get(i);
        }
        graph.ids = gIDs;

        //handle dates
        if (!graph.period.equals(BETWEEN_DATES)){
            DateBundle dateBundle = determineDates(graph.period);
            assert dateBundle != null;
            bundle.putDouble(KEY_START, dateBundle.startDate);
            bundle.putDouble(KEY_END, dateBundle.endDate);
        }
        graph.start = bundle.getDouble(KEY_START);
        graph.end = bundle.getDouble(KEY_END);

        boolean err = false;
        try{
            //Save it.
            GraphDB.saveGraph(graph, this);
        }
        catch (GraphDB.NotUniqueNameException e){
            //Try again with an error if the name is already taken.
            askForGraphName(name, getResources().getString(R.string.stats_save_unique), true);
            err = true;
        }
        if (!err) {
            showMain();
        }

        //And, that's all she wrote.
    }

    private void updateGraphName(final String oldName, final String newName){
        //Eh
        GraphDB.Graph graph = GraphDB.getGraphByName(oldName, this);
        assert graph != null;
        graph.name = newName;
        try {
            GraphDB.saveGraph(graph, this);
        }
        catch (GraphDB.NotUniqueNameException e){
            e.printStackTrace();
        }

        //Delete the old graph
        GraphDB.deleteGraph(oldName, this);

        //Update the main.
        dismissPopup();
        stats_main.updateRecyclers(Category.FARM);
        showPopup(newName);
    }

    /**
     * Using the selected configurations, determine the dates.
     * @param period represents the time period, must be lower case string that matches one of the finals in the stats class
     * @return dateBundle that holds two doubles, one for start date, and the other for the end date.
     */
    public static DateBundle determineDates(String period){
        final String TAG = "Stats_Creator-dates";

        period = period.toLowerCase();

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        switch (period){
            case Stats.TODAY:
                Log.i(TAG, "Period is today");

                startCal.set(Calendar.HOUR_OF_DAY, startCal.getActualMinimum(Calendar.HOUR_OF_DAY));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.set(Calendar.HOUR_OF_DAY, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Stats.YESTERDAY:
                Log.i(TAG, "Period is yesterday");

                startCal.add(Calendar.DATE, -1);
                startCal.set(Calendar.HOUR_OF_DAY, startCal.getActualMinimum(Calendar.HOUR_OF_DAY));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.add(Calendar.DATE, -1);
                endCal.set(Calendar.HOUR_OF_DAY, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Stats.THIS_WEEK:
                Log.i(TAG, "Period is this week");

                startCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                startCal.add(Calendar.WEEK_OF_YEAR, -1);
                startCal.set(Calendar.HOUR_OF_DAY, startCal.getActualMinimum(Calendar.HOUR_OF_DAY));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                endCal.set(Calendar.HOUR_OF_DAY, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Stats.LAST_WEEK:
                Log.i(TAG, "Period is last week");

                startCal.add(Calendar.WEEK_OF_YEAR, -2);
                startCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                startCal.set(Calendar.HOUR_OF_DAY, startCal.getActualMinimum(Calendar.HOUR_OF_DAY));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.add(Calendar.WEEK_OF_YEAR, -1);
                endCal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                endCal.set(Calendar.HOUR_OF_DAY, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Stats.THIS_MONTH:
                Log.i(TAG, "Period is this month");

                startCal.set(Calendar.DAY_OF_MONTH, startCal.getActualMinimum(Calendar.DAY_OF_MONTH));
                startCal.set(Calendar.HOUR_OF_DAY, startCal.getActualMinimum(Calendar.HOUR_OF_DAY));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR_OF_DAY, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Stats.LAST_MONTH:
                Log.i(TAG, "Period is last month");

                startCal.add(Calendar.MONTH, -1);
                startCal.set(Calendar.DAY_OF_MONTH, startCal.getActualMinimum(Calendar.DAY_OF_MONTH));
                startCal.set(Calendar.HOUR_OF_DAY, startCal.getActualMinimum(Calendar.HOUR_OF_DAY));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.add(Calendar.MONTH, -1);
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR_OF_DAY, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Stats.THIS_YEAR:
                Log.i(TAG, "Period is this year");

                startCal.set(Calendar.MONTH, startCal.getActualMinimum(Calendar.MONTH));
                startCal.set(Calendar.DAY_OF_MONTH, startCal.getActualMinimum(Calendar.DAY_OF_MONTH));
                startCal.set(Calendar.HOUR_OF_DAY, startCal.getActualMinimum(Calendar.HOUR_OF_DAY));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.set(Calendar.MONTH, endCal.getActualMaximum(Calendar.MONTH));
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR_OF_DAY, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Stats.LAST_YEAR:
                Log.i(TAG, "Period is last year");

                startCal.add(Calendar.YEAR, -1);
                startCal.set(Calendar.MONTH, startCal.getActualMinimum(Calendar.MONTH));
                startCal.set(Calendar.DAY_OF_MONTH, startCal.getActualMinimum(Calendar.DAY_OF_MONTH));
                startCal.set(Calendar.HOUR_OF_DAY, startCal.getActualMinimum(Calendar.HOUR_OF_DAY));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.add(Calendar.YEAR, -1);
                endCal.set(Calendar.MONTH, endCal.getActualMaximum(Calendar.MONTH));
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR_OF_DAY, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case Stats.NOTHING:
                Log.w(TAG, "Period is nothing.");
                return null;
        }

        Log.i(TAG, "START: " + startCal.getTime().toString());
        Log.i(TAG, "END: " + endCal.getTime().toString());

        DateBundle result = new DateBundle();

        result.startDate = (double) startCal.getTimeInMillis();
        result.endDate = (double) endCal.getTimeInMillis();

        result.startDate /= Stats.THOUSAND;
        result.endDate /= Stats.THOUSAND;

        return result;
    }

    public static class DateBundle{
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

    private String getGroupFromCategory(Category category){
        switch (category){
            case WORKER:
                return WORKER;
            case FOREMAN:
                return FOREMAN;
            case ORCHARD:
                return ORCHARD;
            case FARM:
                return FARM;
        }
        return "";
    }

    public static class GraphDB{

        //KEYS
        private static final String
                IDS = "IDS",
                START = "START",
                END = "END",
                INTERVAL = "INTERVAL",
                GROUP = "GROUP",
                PERIOD = "PERIOD",
                ACCUMULATION = "ACCUMULATION";

        private static String TAG = "GraphDB";

        private static List<String> farms, orchards, workers, foremen;

        public static void saveGraph(Graph graph, Context context) throws NotUniqueNameException {
            final String uid = FirebaseAuth.getInstance().getUid();
            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.stats_graph_pref, uid), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if(keyExists(sharedPreferences, graph.name)){
                throw new NotUniqueNameException();
            }
            else {
                JSONObject object = new JSONObject();
                JSONArray ids = new JSONArray(Arrays.asList(graph.ids));
                try {
                    object.put(IDS, ids);
                    object.put(START, graph.start);
                    object.put(END, graph.end);
                    object.put(INTERVAL, graph.interval);
                    object.put(GROUP, graph.group);
                    object.put(PERIOD, graph.period);
                    object.put(ACCUMULATION, graph.accumulation);
                    Log.i(TAG, "JSONObject assembled: " + object.toString());

                    editor.putString(graph.name, object.toString());

                    editor.apply();

                } catch (JSONException e) {
                    Log.e(TAG, "Problem assembling JSONObject. Not saved.");
                    e.printStackTrace();
                }
            }
        }

        public static void deleteGraph(String name, Context context){
            final String uid = FirebaseAuth.getInstance().getUid();
            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.stats_graph_pref, uid), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.remove(name);
            editor.apply();
        }

        /**
         * Return a GraphDB.Graph which has been found by name<br>
         *     The assumption is that the name is gotten from a recycler, which was populated by yours truly.
         * @param name name of graph to return.
         * @param context Context.
         * @return the graph itself.
         */
        public static Graph getGraphByName(String name, Context context){
            final String uid = FirebaseAuth.getInstance().getUid();
            SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.stats_graph_pref, uid), Context.MODE_PRIVATE);
            try {
                JSONObject object = new JSONObject(sharedPreferences.getString(name, null));

                Graph graph = new Graph();

                //Start with the name
                graph.name = name;

                //The ids
                JSONArray array = object.getJSONArray(IDS);
                String ids[] = new String[array.length()];
                for (int i = 0; i < array.length(); i++){
                    ids[i] = array.get(i).toString();
                }
                graph.ids = ids;

                //The rest
                graph.start = object.getDouble(START);
                graph.end = object.getDouble(END);
                graph.interval = object.getString(INTERVAL);
                graph.group = object.getString(GROUP);
                graph.period = object.getString(PERIOD);
                graph.accumulation = object.getString(ACCUMULATION);

                return graph;

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null; //Whoops!
        }

        /**
         * <p>The assumption is for this to be used in a recycler, so once what the user wants is known, the name can be used to get the actual graph.</p>
         * @param category the category for which the names are to be asked for.<br>
         *                 Acceptable values are:
         *                 <ul>
         *                 <li>FARM</li>
         *                 <li>ORCHARD</li>
         *                 <li>WORKER</li>
         *                 <li>FOREMAN</li>
         *                 </ul>
         * @param context Context
         * @param restore If true, the statically kept lists, will be reset, if this is NOT set true after some write operation or account change, the results will be incorrect. And nothing will be doable with them.<br>
         *                The intention is to keep this false if the method is being used in quick succession, like say, setting up four recyclers on the same fragment.
         * @return names of the graphs.
         */
        public static List<String> getNamesByCategory(Category category, Context context, boolean restore){
            if (restore) {
                farms = new ArrayList<>();
                orchards = new ArrayList<>();
                workers = new ArrayList<>();
                foremen = new ArrayList<>();

                final String uid = FirebaseAuth.getInstance().getUid();
                SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.stats_graph_pref, uid), Context.MODE_PRIVATE);
                Map<String, ?> all = sharedPreferences.getAll();
                Set<String> keysSet = all.keySet();

                for (String key : keysSet) {
                    try {
                        JSONObject object = new JSONObject(sharedPreferences.getString(key, null));
                        if (object.getString(GROUP).equals(Stats.FARM)) {
                            farms.add(key);
                        }
                        else if (object.getString(GROUP).equals(Stats.ORCHARD)) {
                            orchards.add(key);
                        }
                        else if (object.getString(GROUP).equals(Stats.WORKER)) {
                            workers.add(key);
                        }
                        else if (object.getString(GROUP).equals(Stats.FOREMAN)) {
                            foremen.add(key);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            switch (category){
                case FARM:
                    return farms;
                case ORCHARD:
                    return orchards;
                case WORKER:
                    return workers;
                case FOREMAN:
                    return foremen;
            }
            return null; //Whoops!
        }

        /**
         * Tells if a statically kept array holds elements. DOSE NOT get from SharedPreferences, so do after a restore with getting name.<br>
         *     Perfect for finding out if a title should be shown above the list of names.
         * @param category the category to find out about.
         * @return true if the list is not empty.
         */
        public static boolean isThere(Category category){
            switch (category){
                case FARM:
                    return !farms.isEmpty();
                case ORCHARD:
                    return !orchards.isEmpty();
                case WORKER:
                    return !workers.isEmpty();
                case FOREMAN:
                    return !foremen.isEmpty();
            }
            return false;
        }

        private static boolean keyExists(SharedPreferences sharedPreferences, String key){
            Map<String, ?> all = sharedPreferences.getAll();
            return all.containsKey(key);
        }

        public static class Graph{

            public String name;
            public String ids[];
            public double start, end;
            public String interval, group, period, accumulation;

            @Override
            public String toString() {
                return name;
            }
        }

        public static class NotUniqueNameException extends Exception{


            public NotUniqueNameException(){

            }

            public NotUniqueNameException(String message){
                super(message);
            }
        }
    }
}


