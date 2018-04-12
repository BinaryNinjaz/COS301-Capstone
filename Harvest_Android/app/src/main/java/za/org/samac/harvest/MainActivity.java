package za.org.samac.harvest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import za.org.samac.harvest.adapter.WorkerGridAdapter;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

public class MainActivity extends AppCompatActivity {
    //int totalBagsCollected = 0; //keeps track of total bags collected


    private static final String TAG = "Clicker";

    private ArrayList<String> workers;
    private ArrayList<Integer> bagsCollected;
    private ArrayList<Button> workerBtns;

    private GridView gridview;
    private WorkerGridAdapter adapter;

    //private View convertView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("workers");
        Query q = ref.orderByChild("name");
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null) {
                    collectWorkers((Map<String, Object>) dataSnapshot.getValue());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Error", databaseError.toString());
            }
        });

        setContentView(R.layout.activity_main);
        Button btn = findViewById(R.id.button_start);
        btn.setTag("green");
        workers = new ArrayList<>();
        gridview = findViewById(R.id.gridview);
        adapter = new WorkerGridAdapter(getApplicationContext(), workers);
        gridview.setAdapter(adapter);
    }

    /***********************
     ** Function below creates arrays of the workers, how many bags they collect
     * and an array of buttons to be added to the view
     */


    protected void collectWorkers(Map<String, Object> users) {

        bagsCollected = new ArrayList<>();
        workerBtns = new ArrayList<>();
        int screenWidth = getScreenWidth();
        int count = 0 ;
        for (Map.Entry<String, Object> entry : users.entrySet()) {
            Map singleUser = (Map) entry.getValue();
            String fullName = singleUser.get("name") + " " + singleUser.get("surname");
            workers.add(fullName);
           /* bagsCollected.add(0);
            final Button workerBtn = new Button(this);
            workerBtn.setTag(fullName);
            String btnText = fullName+System.getProperty("line.separator")
                    +System.getProperty("line.separator")+"0";
            workerBtn.setText(btnText);
            workerBtn.setId(count);

            ++count;
            int width =  (int)(screenWidth/2.2);
            workerBtn.setLayoutParams(new RelativeLayout.LayoutParams(width,width));
            workerBtn.setBackgroundColor(Color.parseColor("#275894"));
            workerBtn.setGravity(Gravity.CENTER);
            workerBtn.setTextColor(Color.parseColor("#FFFFFFFF"));
            workerBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    int pos = workers.indexOf(workerBtn.getTag());
                    bagsCollected.set(pos, bagsCollected.get(pos)+1);
                    ++totalBagsCollected;
                    String newText = workers.get(pos)+System.getProperty("line.separator")+
                            System.getProperty("line.separator")+bagsCollected.get(pos);
                    workerBtn.setText(newText);
                }
            });
            workerBtns.add(workerBtn);*/
        }
        adapter.notifyDataSetChanged();
        //addButtons();
    }

    //worker buttons need to be added dynamically
    //seperate function was created for testing purposes
    private void addButtons() {
        if(workerBtns.size() > 0) {
            final RelativeLayout rl = findViewById(R.id.rel);
            rl.addView(workerBtns.get(0));
            for (int i = 1; i < workerBtns.size(); i++) {
                RelativeLayout.LayoutParams layoutParam =
                        new RelativeLayout.LayoutParams((int)(getScreenWidth()/2.2),
                                (int)(getScreenWidth()/2.2));
                layoutParam.setMargins(0,5,0,5);

                if (i == 1) {
                    layoutParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, i - 2);
                } else if (i % 2 == 0) {
                    layoutParam.addRule(RelativeLayout.BELOW,i - 1);
                } else if (i % 2 == 1) { // odd
                    layoutParam.addRule(RelativeLayout.BELOW,i - 2);
                    layoutParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, i - 1);
                }

                rl.addView(workerBtns.get(i),layoutParam);
            }
        }
        /*****************************************
        **Code above does not work correctly, gets majority of buttons to show
        * but not even sure if it's the right approach (was probably my 1000th attempt)
        * there are most likely mistakes in the xml file
        */

    }

    /*******************************
     Code below handles the stop/start button, runs a timer and displays how many
     bags were collected in the elapsed time. It then clears for another timer to start.
     Sessions for each worker still needs to be implemented *
     */
    long startTime=0, stopTime=0;
    @SuppressLint("SetTextI18n")
    public void onClickStart(View v){
        /*final android.view.LayoutInflater layoutInflater = LayoutInflater.from(adapter.context);
        convertView = layoutInflater.inflate(R.layout.worker_grid_item , null);
        Button btnPlus = getView().findViewById(R.id.btnPlus);
        Button btnMinus = getView.findViewById(R.id.btnMinus);

        btnPlus.setEnabled(true);
        btnMinus.setEnabled(true);*/
        Button btn = findViewById(R.id.button_start);
        //adapter.totalBagsCollected=0;
        if(btn.getTag()=="green"){
            startTime = System.currentTimeMillis();
            btn.setBackgroundColor(Color.parseColor("#FFFF8800"));
            btn.setText("Stop");
            btn.setTag("orange");
        }else{
            stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            // do something with time
            for(int i = 0; i < bagsCollected.size(); i++){
                bagsCollected.set(i, 0);
            }
            int h = (int) ((elapsedTime / 1000) / 3600);
            int m = (int) (((elapsedTime / 1000) / 60) % 60);
            int s = (int) ((elapsedTime / 1000) % 60);
            String timeTaken = h+" hour(s), "+m+" minute(s) and "+s+" second(s)";
            String msg = "A total of "+adapter.totalBagsCollected+" bags have been collected in "+timeTaken+".";
            adapter.totalBagsCollected=0;
            //pop up is used to show how many bags were collected in the elapsed time
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage(msg);
            dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    for(int i = 0; i < bagsCollected.size(); i++){
                        Button temp = workerBtns.get(i);
                        temp.setText(workers.get(i)+System.getProperty("line.separator")+
                                System.getProperty("line.separator")+"0");
                        workerBtns.set(i, temp);
                    }
                    dialog.dismiss();
                }
            });
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();
            //
            btn.setBackgroundColor(Color.parseColor("#FF0CCB29"));
            btn.setText("Start");
            btn.setTag("green");
        }
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

}
/*public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}*/
