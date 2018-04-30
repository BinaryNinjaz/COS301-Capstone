package za.org.samac.harvest.adapter;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import za.org.samac.harvest.LocationHelper;
import za.org.samac.harvest.MainActivity;
import za.org.samac.harvest.Manifest;
import za.org.samac.harvest.R;
import za.org.samac.harvest.domain.Worker;

public class WorkerRecyclerViewAdapter extends RecyclerView.Adapter<WorkerRecyclerViewAdapter.WorkerViewHolder> {

    public Context context;//made it private initially
    private ArrayList<Worker> workers;
    private ArrayList<Button> plus;
    private ArrayList<Button> minus;
    private ArrayList<TextView> incrementViews;
    private Location location;
    public int totalBagsCollected;
    //private FirebaseAuth mAuth;
    private collections collectionObj;
    FirebaseDatabase database;
    DatabaseReference myRef;
    double currentLat;
    double currentLong;
    private Date currentTime;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String childKey;
    private String workerID;
    private String workerIncrement;

    public WorkerRecyclerViewAdapter(Context context, ArrayList<Worker> workers) {
        this.context = context;
        this.workers = workers;
        this.totalBagsCollected = 0;
        plus = new ArrayList<>();
        minus = new ArrayList<>();
        incrementViews = new ArrayList<>();
        String email = "";
        //mAuth = FirebaseAuth.getInstance();
        //FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
        }
        collectionObj = new collections(email);
    }

    @Override
    public WorkerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.worker_grid_item, parent, false);

        return new WorkerViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return this.workers.size();
    }

    private void makeChangesToData(String userId, String username, String title, String body) {

    }

    @Override
    public void onBindViewHolder(final WorkerViewHolder holder, int position) {
        final Worker worker = this.workers.get(position);//set worker object that is being clicked on
        final String personName = worker.getName();
        holder.workerName.setText(personName);//set name of the worker
        holder.increment.setText(String.format("%d", worker.getValue()));//set incrementer of the worker (fixed not updating of increments)

        //plus button is clicked
        incrementViews.add(holder.increment);
        holder.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer value = worker.getValue() + 1;
                holder.increment.setText(String.format("%d", value));

                //get coordinates
                currentLat = LocationHelper.currentLat;
                currentLong = LocationHelper.currentLong;

                //get time
                currentTime = Calendar.getInstance().getTime();

                //make changes on Firebase or make changes on client side file (encrypt using SQLite)
                database = FirebaseDatabase.getInstance();
                String userUid = user.getUid();
                myRef = database.getReference(userUid + "/sessions/");//path to sessions collection in Firebase

                childKey = MainActivity.childKey;//childByAutoKey

                Map<String, Object> collections = new HashMap<>();//stores collections in Firebase
                workerID = worker.getID();//get worker ID
                workerIncrement = "" + worker.getValue();//get worker increment (number of yield)

                Map<String, Object> coordinates = new HashMap<>();
                coordinates.put("lat", currentLat);
                coordinates.put("lng", currentLong);

                Map<String, Object> collectionPoint = new HashMap<>();
                collectionPoint.put("coord", coordinates);
                collectionPoint.put("date", currentTime);

                Map<String, Object> workerInc = new HashMap<>();
                workerInc.put("" + workerIncrement, collectionPoint);

                Map<String, Object> workerItem = new HashMap<>();
                workerItem.put(workerID, workerInc);

                //collections.put(workerID + "/" + workerIncrement + "/coord/", coordinates);//store coordinates in collections path
                //collections.put(workerID + "/" + workerIncrement + "/date", currentTime);//store time in collections path
                collections.put("collections", workerItem);//store coordinates in collections path
                //collections.put(workerID + "/" + workerIncrement + "/date", currentTime);//store time in collections path

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(childKey, collections);//append changes all into one path

                System.out.println(childKey + "$$$$$$$$$$$$$$$$$");
                System.out.println(workerID + "#################");
                System.out.println(workerIncrement + "@@@@@@@@@@@@");

                myRef.updateChildren(childUpdates);//store plus button info in Firebase

                collectionObj.addCollection(personName, location);
                ++totalBagsCollected;
                worker.setValue(value);
            }
        });
        plus.add(holder.btnPlus);

        //minus button is clicked
        holder.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer currentValue = worker.getValue();
                if(currentValue > 0) {
                    Integer value = currentValue - 1;
                    holder.increment.setText(String.format("%d", value));

                    //get coordinates
                    currentLat = LocationHelper.currentLat;
                    currentLong = LocationHelper.currentLong;

                    //get time
                    Date currentTime = Calendar.getInstance().getTime();

                    //make changes on firebase
                    database = FirebaseDatabase.getInstance();
                    String userUid = user.getUid();
                    workerID = worker.getID();//get worker ID
                    workerIncrement = "" + worker.getValue();//get worker increment (number of yield)
                    myRef = database.getReference(userUid + "/sessions/" + childKey + "/collections/" + workerID + "/" + workerIncrement);//path to sessions increment in Firebase

                    myRef.removeValue();//remove latest increment

                    collectionObj.removeCollection(personName);
                    --totalBagsCollected;
                    worker.setValue(value);
                }
            }
        });
        minus.add(holder.btnMinus);
    }

    public class WorkerViewHolder extends RecyclerView.ViewHolder {
        TextView workerName;
        TextView increment;
        Button btnPlus;
        Button btnMinus;

        WorkerViewHolder(View view) {
            super(view);
            workerName = view.findViewById(R.id.workerName);
            increment = view.findViewById(R.id.increment);
            btnPlus = view.findViewById(R.id.btnPlus);
            btnMinus = view.findViewById(R.id.btnMinus);
        }
    }

    public void setPlusEnabled(boolean state) {
        for(int i = 0 ; i < plus.size() ; i++) {
            Button btn = plus.get(i);
            btn.setEnabled(state);
            plus.set(i, btn);
        }
    }

    public void setMinusEnabled(boolean state) {
        for(int i = 0 ; i < minus.size() ; i++) {
            Button btn = minus.get(i);
            btn.setEnabled(state);
            minus.set(i, btn);
        }
    }

    public void setIncrement(){
        for(int i = 0 ; i < incrementViews.size() ; i++) {
            TextView text = incrementViews.get(i);
            //TODO: save data before this
            text.setText("0");
            workers.get(i).setValue(0);
            incrementViews.set(i, text);
        }
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public collections getCollectionObj() {
        return collectionObj;
    }


}
