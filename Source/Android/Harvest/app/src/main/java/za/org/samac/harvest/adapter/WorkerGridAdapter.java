package za.org.samac.harvest.adapter;

import android.content.Context;
import android.location.Location;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import za.org.samac.harvest.R;

public class WorkerGridAdapter extends BaseAdapter {

    public Context context;//made it private initially
    private ArrayList<String> workers;
    private ArrayList<Button> plus;
    private ArrayList<Button> minus;
    private ArrayList<TextView> incrementViews;
    private Location location;
    public int totalBagsCollected;
    private FirebaseAuth mAuth;
    private collections collectionObj;

    public WorkerGridAdapter(Context context, ArrayList<String> workers) {
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
    public int getCount() {
        return workers.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final String personName = this.workers.get(position);
        final LayoutInflater layoutInflater = LayoutInflater.from(context);

        ConstraintLayout view;

        if (convertView == null) {
            view = (ConstraintLayout) layoutInflater.inflate(R.layout.worker_grid_item , null);

        } else {
            view = (ConstraintLayout) convertView;
        }
        TextView workerName = view.findViewById(R.id.workerName);
        workerName.setText(personName);

        final TextView increment = view.findViewById(R.id.increment);
        incrementViews.add(increment);
        Button btnPlus = view.findViewById(R.id.btnPlus);
        //btnPlus.setEnabled(false);
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Long value = Long.valueOf(increment.getText().toString()) + 1;
                increment.setText(String.format("%d", value));

                //make changes on firebase
                //collectionObj.addCollection(personName, );
                ++totalBagsCollected;
            }
        });
        plus.add(btnPlus);

        Button btnMinus = view.findViewById(R.id.btnMinus);
        //btnMinus.setEnabled(false);
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Long currentValue = Long.valueOf(increment.getText().toString());
                if(currentValue > 0) {
                    Long value = currentValue - 1;
                    increment.setText(String.format("%d", value));

                    //make changes on firebase
                    collectionObj.removeCollection(personName);
                    --totalBagsCollected;
                }
            }
        });
        minus.add(btnMinus);

        return view;
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