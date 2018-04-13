package za.org.samac.harvest.adapter;

import android.content.Context;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import za.org.samac.harvest.R;

public class WorkerRecyclerViewAdapter extends RecyclerView.Adapter<WorkerRecyclerViewAdapter.WorkerViewHolder> {

    public Context context;//made it private initially
    private ArrayList<String> workers;
    private ArrayList<Button> plus;
    private ArrayList<Button> minus;
    private ArrayList<TextView> incrementViews;
    private Location location;
    public int totalBagsCollected;
    private FirebaseAuth mAuth;
    private collections collectionObj;

    public WorkerRecyclerViewAdapter(Context context, ArrayList<String> workers) {
        this.context = context;
        this.workers = workers;
        this.totalBagsCollected = 0;
        plus = new ArrayList<>();
        minus = new ArrayList<>();
        incrementViews = new ArrayList<>();
        String email="";
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user =  mAuth.getCurrentUser();
        if(user!=null) {
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

    @Override
    public void onBindViewHolder(final WorkerViewHolder holder, int position) {

        final String personName = this.workers.get(position);
        holder.workerName.setText(personName);

        incrementViews.add(holder.increment);
        holder.btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Long value = Long.valueOf(holder.increment.getText().toString()) + 1;
                holder.increment.setText(String.format("%d", value));

                //make changes on firebase
                collectionObj.addCollection(personName, location);
                ++totalBagsCollected;
            }
        });
        plus.add(holder.btnPlus);

        holder.btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Long currentValue = Long.valueOf(holder.increment.getText().toString());
                if(currentValue > 0) {
                    Long value = currentValue - 1;
                    holder.increment.setText(String.format("%d", value));

                    //make changes on firebase
                    collectionObj.removeCollection(personName);
                    --totalBagsCollected;
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
