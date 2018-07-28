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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.List;

import za.org.samac.harvest.util.AppUtil;
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

    // For the selector.
    private Data data;

    // Information needed by the graph
    List<String> ids;
    Double start, end;

    // Needed to determine the correct graph
    enum Category{
        ORCHARD,
        WORKER,
        FOREMAN,
        NOTHING
    }
    Category category = Category.NOTHING;

    enum Period{ // The time period, not to be confused with the interval, which is called period on Firebase.
        TODAY,
        YESTERDAY,
        THIS_WEEK,
        LAST_WEEK,
        THIS_MONTH,
        LAST_MONTH,
        THIS_YEAR,
        LAST_YEAR,
        NOTHING
    }

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

        //We'll do this here so it can pull before the selector if it needs to.
        data = new Data();
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

    private void showMain(){
        toggleUpButton(false);

        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        //empty the back stack
        if(fragmentManager.getBackStackEntryCount() != 0){
            android.support.v4.app.FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(0);
            fragmentManager.popBackStack(backStackEntry.getId(), android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentManager.executePendingTransactions();
        }

        Analytics_Main analytics_main = new Analytics_Main();
        fragmentTransaction.replace(R.id.analMainPart, analytics_main, "MAIN");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void showSelector(){

    }

    public void anal_main_buttonHandler(View v){
        switch (v.getId()){
            //Orchard

            case R.id.anal_choose_orch_tod:
                category = Category.ORCHARD;
                determineDates(Period.TODAY);
                showSelector();
                break;

            case R.id.anal_choose_orch_yes:
                category = Category.ORCHARD;
                determineDates(Period.YESTERDAY);
                showSelector();
                break;

            case R.id.anal_choose_orch_tWeek:
                category = Category.ORCHARD;
                determineDates(Period.THIS_WEEK);
                showSelector();
                break;

            case R.id.anal_choose_orch_lWeek:
                category = Category.ORCHARD;
                determineDates(Period.LAST_WEEK);
                showSelector();
                break;

            case R.id.anal_choose_orch_tMonth:
                category = Category.ORCHARD;
                determineDates(Period.THIS_MONTH);
                showSelector();
                break;

            case R.id.anal_choose_orch_lMonth:
                category = Category.ORCHARD;
                determineDates(Period.LAST_MONTH);
                showSelector();
                break;

            case R.id.anal_choose_orch_tYear:
                category = Category.ORCHARD;
                determineDates(Period.THIS_YEAR);
                showSelector();
                break;

            case R.id.anal_choose_orch_lYear:
                category = Category.ORCHARD;
                determineDates(Period.LAST_YEAR);
                showSelector();
                break;

            //Worker
                
            case R.id.anal_choose_work_tod:
                category = Category.WORKER;
                determineDates(Period.TODAY);
                showSelector();
                break;

            case R.id.anal_choose_work_yes:
                category = Category.WORKER;
                determineDates(Period.YESTERDAY);
                showSelector();
                break;

            case R.id.anal_choose_work_tWeek:
                category = Category.WORKER;
                determineDates(Period.THIS_WEEK);
                showSelector();
                break;

            case R.id.anal_choose_work_lWeek:
                category = Category.WORKER;
                determineDates(Period.LAST_WEEK);
                showSelector();
                break;

            case R.id.anal_choose_work_tMonth:
                category = Category.WORKER;
                determineDates(Period.THIS_MONTH);
                showSelector();
                break;

            case R.id.anal_choose_work_lMonth:
                category = Category.WORKER;
                determineDates(Period.LAST_MONTH);
                showSelector();
                break;

            case R.id.anal_choose_work_tYear:
                category = Category.WORKER;
                determineDates(Period.THIS_YEAR);
                showSelector();
                break;

            case R.id.anal_choose_work_lYear:
                category = Category.WORKER;
                determineDates(Period.LAST_YEAR);
                showSelector();
                break;

            //Foreman

            case R.id.anal_choose_fore_tod:
                category = Category.FOREMAN;
                determineDates(Period.TODAY);
                showSelector();
                break;

            case R.id.anal_choose_fore_yes:
                category = Category.FOREMAN;
                determineDates(Period.YESTERDAY);
                showSelector();
                break;

            case R.id.anal_choose_fore_tWeek:
                category = Category.FOREMAN;
                determineDates(Period.THIS_WEEK);
                showSelector();
                break;

            case R.id.anal_choose_fore_lWeek:
                category = Category.FOREMAN;
                determineDates(Period.LAST_WEEK);
                showSelector();
                break;

            case R.id.anal_choose_fore_tMonth:
                category = Category.FOREMAN;
                determineDates(Period.THIS_MONTH);
                showSelector();
                break;

            case R.id.anal_choose_fore_lMonth:
                category = Category.FOREMAN;
                determineDates(Period.LAST_YEAR);
                showSelector();
                break;

            case R.id.anal_choose_fore_tYear:
                category = Category.FOREMAN;
                determineDates(Period.THIS_YEAR);
                showSelector();
                break;

            case R.id.anal_choose_fore_lYear:
                category = Category.FOREMAN;
                determineDates(Period.LAST_YEAR);
                showSelector();
                break;

            //Create Custom Graph

            case R.id.anal_choose_cus_buttonInRecycler:
                //TODO: Create custom graph
                break;
        }
    }

    public void anal_selector_buttonHandler(View v){

    }

    public void anal_creator_buttonHandler(View v){

    }

    private void determineDates(Period period){
        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();

        switch (period){
            case TODAY:
                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.set(Calendar.HOUR, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case YESTERDAY:
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

            case THIS_WEEK:
                startCal.set(Calendar.DAY_OF_WEEK, startCal.getActualMinimum(Calendar.DAY_OF_WEEK));
                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.set(Calendar.DAY_OF_WEEK, endCal.getActualMaximum(Calendar.DAY_OF_WEEK));
                endCal.set(Calendar.HOUR, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case LAST_WEEK:
                startCal.roll(Calendar.WEEK_OF_YEAR, -1);
                startCal.set(Calendar.DAY_OF_WEEK, startCal.getFirstDayOfWeek());
                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.roll(Calendar.WEEK_OF_YEAR, -1);
                endCal.set(Calendar.DAY_OF_WEEK, endCal.getActualMaximum(Calendar.DAY_OF_WEEK));
                endCal.set(Calendar.HOUR, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case THIS_MONTH:
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

            case LAST_MONTH:
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

            case THIS_YEAR:
                startCal.set(Calendar.MONTH, startCal.getActualMinimum(Calendar.MONTH));
                startCal.set(Calendar.DAY_OF_MONTH, startCal.getActualMinimum(Calendar.DAY_OF_MONTH));
                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.set(Calendar.MONTH, endCal.getActualMaximum(Calendar.MONTH));
                endCal.set(Calendar.WEEK_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case LAST_YEAR:
                startCal.roll(Calendar.YEAR, -1);
                startCal.set(Calendar.MONTH, startCal.getActualMinimum(Calendar.MONTH));
                startCal.set(Calendar.DAY_OF_MONTH, startCal.getActualMinimum(Calendar.DAY_OF_MONTH));
                startCal.set(Calendar.HOUR, startCal.getActualMinimum(Calendar.HOUR));
                startCal.set(Calendar.MINUTE, startCal.getActualMinimum(Calendar.MINUTE));
                startCal.set(Calendar.SECOND, startCal.getActualMinimum(Calendar.SECOND));
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));

                endCal.roll(Calendar.YEAR, -1);
                endCal.set(Calendar.MONTH, endCal.getActualMaximum(Calendar.MONTH));
                endCal.set(Calendar.WEEK_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endCal.set(Calendar.HOUR, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.MINUTE, endCal.getActualMaximum(Calendar.HOUR));
                endCal.set(Calendar.SECOND, endCal.getActualMaximum(Calendar.SECOND));
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));

                break;

            case NOTHING:
                Log.w("Analytics", "Period is nothing.");
                return;
        }

        start = (double) startCal.getTimeInMillis();
        end = (double) endCal.getTimeInMillis();
    }
}
