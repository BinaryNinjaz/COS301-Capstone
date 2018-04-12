package za.org.samac.harvest.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import za.org.samac.harvest.R;

public class WorkerGridAdapter extends BaseAdapter {

    public Context context;//made it private initially
    private ArrayList<String> workers;
    public int totalBagsCollected; //keeps track of total bags collected

    public WorkerGridAdapter(Context context, ArrayList<String> workers) {
        this.context = context;
        this.workers = workers;
        this.totalBagsCollected = 0;
    }

    @Override
    public int getCount() {
        return workers.size();
    }

    @Override
    public Object getItem(int i) {
        return workers.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        String personName = this.workers.get(position);

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.worker_grid_item , null);

            TextView workerName = convertView.findViewById(R.id.workerName);
            workerName.setText(personName);

            final TextView increment = convertView.findViewById(R.id.increment);
            increment.setText("0");
            Button btnPlus = convertView.findViewById(R.id.btnPlus);
            //btnPlus.setEnabled(false);



            btnPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Long value = Long.valueOf(increment.getText().toString()) + 1;
                    increment.setText(String.format("%d", value));

                    //make changes on firebase
                    ++totalBagsCollected;
                }
            });
            Button btnMinus = convertView.findViewById(R.id.btnMinus);
            ///btnMinus.setEnabled(false);
            btnMinus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Long currentValue = Long.valueOf(increment.getText().toString());
                    if(currentValue > 0) {
                        Long value = currentValue - 1;
                        increment.setText(String.format("%d", value));

                        //make changes on firebase
                        --totalBagsCollected;
                    }
                }
            });
        }

        //int convID = 1;
        //convertView.setId(convID);
        return convertView;
    }
}
