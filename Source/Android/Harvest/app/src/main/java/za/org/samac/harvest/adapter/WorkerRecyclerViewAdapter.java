package za.org.samac.harvest.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import za.org.samac.harvest.LocationHelper;
import za.org.samac.harvest.MainActivity;
//import za.org.samac.harvest.Manifest;
import za.org.samac.harvest.R;
import za.org.samac.harvest.domain.Worker;
import za.org.samac.harvest.util.WorkerComparator;

public class WorkerRecyclerViewAdapter extends RecyclerView.Adapter<WorkerRecyclerViewAdapter.WorkerViewHolder> {

    public Context context;//made it private initially
    private ArrayList<Worker> workers;
    private ArrayList<Button> plus;
    private ArrayList<Button> minus;
    private ArrayList<TextView> incrementViews;
    private Location location;
    public int totalBagsCollected;
    private collections collectionObj;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private double currentLat;
    private double currentLong;
    private int divideBy1000Var = 1000;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String sessionKey;
    private String workerID;
    private String workerIncrement;
    private String farmerKey;
    private DatabaseReference sessRef;
    private boolean gotCorrectFarmerKey;
    private DatabaseReference workersRef;
    private static final String TAG = "Button";
    private double lat = 0.0;
    private double lng = 0.0;

    private OnItemClickListener onItemClickListener;

    public WorkerRecyclerViewAdapter(Context context, ArrayList<Worker> workers, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.workers = workers;
        this.totalBagsCollected = 0;
        plus = new ArrayList<>();
        minus = new ArrayList<>();
        incrementViews = new ArrayList<>();
        String email = "";

        if (user != null) {
            email = user.getEmail();
        }
        collectionObj = new collections(email);
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public WorkerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.worker_grid_item, parent, false);

        return new WorkerViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return this.workers == null ? 0 : this.workers.size();
    }

    public void setWorkers(ArrayList<Worker> workers){
        this.workers = workers == null ? new ArrayList<Worker>() : workers;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final WorkerViewHolder holder, int position) {
        final Worker worker = this.workers.get(position);//set worker object that is being clicked on
        final String personName = worker.getName();
        holder.workerName.setText(personName);//set name of the worker
        holder.increment.setText(String.format("%d", worker.getValue()));//set incrementer of the worker (fixed not updating of increments)

        //plus button is clicked
        if (!incrementViews.contains(holder.increment)) {
            incrementViews.add(holder.increment);
        }
        holder.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Integer value = worker.getValue() + 1;
                holder.increment.setText(String.format("%d", value));

                //get coordinates
                currentLat = location.getLatitude();
                currentLong = location.getLongitude();

                //get time
                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm ZZ");
                String dateString = formatter.format(new Date((System.currentTimeMillis()/divideBy1000Var) * 1000L));
                //make changes on Firebase or make changes on client side file (encrypt using SQLite)
                database = FirebaseDatabase.getInstance();

                sessionKey = MainActivity.sessionKey;//get key/ID for a session
                workerID = worker.getID();//get worker ID
                workerIncrement = "" + worker.getValue();//get worker increment (number of yield)

                farmerKey = MainActivity.farmerKey;
                myRef = database.getReference(farmerKey + "/sessions/" + sessionKey + "/collections/" + workerID + "/" + workerIncrement + "/");//path to sessions increment in Firebase

                Map<String, Object> coordinates = new HashMap<>();
                coordinates.put("lat", currentLat);
                coordinates.put("lng", currentLong);

                Map<String, Object> childUpdates = new HashMap<>();
                //childUpdates.put(childKey, collections);//append changes all into one path
                childUpdates.put("coord", coordinates);
                childUpdates.put("date", dateString);

                Map<String, Object> sessionDate = new HashMap<>();
                sessRef = database.getReference(farmerKey + "/sessions/" + sessionKey + "/");//path to inside a session key in Firebase
                sessionDate.put("end_date", dateString);
                sessRef.updateChildren(sessionDate);//save data to Firebase
                myRef.updateChildren(childUpdates);//store plus button info in Firebase

                collectionObj.addCollection(personName, location, MainActivity.selectedOrchardKey);
                ++totalBagsCollected;

                //display incremented current yield
                if(onItemClickListener != null) {
                    onItemClickListener.onClick(totalBagsCollected);
                }
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

                    collectionObj.removeCollection(personName);
                    --totalBagsCollected;

                    //display incremented current yield
                    if(onItemClickListener != null) {
                        onItemClickListener.onClick(totalBagsCollected);
                    }

                    worker.setValue(value);

                    //make changes on firebase
                    database = FirebaseDatabase.getInstance();
                    //String userUid = user.getUid();
                    workerID = worker.getID();//get worker ID
                    sessionKey = MainActivity.sessionKey;//get key/ID for a session
                    workerIncrement = "" + worker.getValue();//get worker increment (number of yield)

                    Map<String, Object> sessionDate = new HashMap<>();
                    sessRef = database.getReference(farmerKey + "/sessions/" + sessionKey + "/");//path to inside a session key in Firebase
                    SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm ZZ");
                    String dateString = formatter.format(new Date((System.currentTimeMillis()/divideBy1000Var) * 1000L));
                    sessionDate.put("end_date", dateString);
                    sessRef.updateChildren(sessionDate);//save data to Firebase

                    farmerKey = MainActivity.farmerKey;
                    myRef = database.getReference(farmerKey + "/sessions/" + sessionKey + "/collections/" + workerID + "/" + workerIncrement);//path to sessions increment in Firebase
                    myRef.removeValue();//remove latest increment
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
            text.setText("0");
            //workers.get(i).setValue(0);
            incrementViews.set(i, text);
        }

        for(int i = 0 ; i < workers.size() ; i++) {
            workers.get(i).setValue(0);
        }
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setLatLng(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public collections getCollectionObj() {
        return collectionObj;
    }

    public interface OnItemClickListener {
        public void onClick(int value);
    }


}
