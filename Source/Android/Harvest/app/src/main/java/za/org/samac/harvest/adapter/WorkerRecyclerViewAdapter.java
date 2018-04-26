package za.org.samac.harvest.adapter;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

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
    private FirebaseAuth mAuth;
    private collections collectionObj;

    public WorkerRecyclerViewAdapter(Context context, ArrayList<Worker> workers) {
        this.context = context;
        this.workers = workers;
        this.totalBagsCollected = 0;
        plus = new ArrayList<>();
        minus = new ArrayList<>();
        incrementViews = new ArrayList<>();
        String email = "";
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
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

    /*@Override
    public void locationPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        PermissionUtils permissionUtils;

        permissionUtils=new

        PermissionUtils(MyLocationUsingLocationAPI.this);

            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

            permissionUtils.check_permission(permissions,"Need GPS permission for getting your location",1);

        GoogleApiClient mGoogleApiClient;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }*/


    @Override
    public void onBindViewHolder(final WorkerViewHolder holder, int position) {
        final Worker worker = this.workers.get(position);//set worker object that is being clicked on
        final String personName = worker.getName();
        holder.workerName.setText(personName);//set name of the worker
        holder.increment.setText(String.format("%d", worker.getValue()));//set incrementer of the worker (fixed not updating of increments)

        incrementViews.add(holder.increment);
        holder.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer value = worker.getValue() + 1;
                holder.increment.setText(String.format("%d", value));

                //make changes on client side file (encrypt using SQLite)
                //get coordinates
                //get time
                collectionObj.addCollection(personName, location);
                ++totalBagsCollected;
                worker.setValue(value);
            }
        });
        plus.add(holder.btnPlus);

        holder.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer currentValue = worker.getValue();
                if(currentValue > 0) {
                    Integer value = currentValue - 1;
                    holder.increment.setText(String.format("%d", value));

                    //make changes on firebase
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
