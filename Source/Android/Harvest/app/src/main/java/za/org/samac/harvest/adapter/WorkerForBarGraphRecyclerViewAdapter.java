package za.org.samac.harvest.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import za.org.samac.harvest.BarGraph;
import za.org.samac.harvest.BarGraphForemen;
import za.org.samac.harvest.R;

public class WorkerForBarGraphRecyclerViewAdapter extends RecyclerView.Adapter<WorkerForBarGraphRecyclerViewAdapter.WorkerViewHolder>{
    private Context context;
    private ArrayList<String> workers;
    private ArrayList<String> workerKeys;

    public WorkerForBarGraphRecyclerViewAdapter(Context applicationContext, ArrayList<String> workers, ArrayList<String> workerKeys) {
        this.context = applicationContext;
        this.workers = workers;
        this.workerKeys = workerKeys;
    }

    @Override
    public WorkerForBarGraphRecyclerViewAdapter.WorkerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_foremen_buttons, parent, false);

        return new WorkerForBarGraphRecyclerViewAdapter.WorkerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WorkerForBarGraphRecyclerViewAdapter.WorkerViewHolder holder, int position) {
        final String workerName = this.workers.get(position);
        final String workerKey = this.workerKeys.get(position);
        holder.button.setText(workerName);//set name of orchard
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent details = new Intent(context, BarGraph.class);
                details.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Intent key = details.putExtra("key", workerKey);
                Intent name = details.putExtra("name", workerName);

                context.startActivity(details);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.workers.size();
    }

    public class WorkerViewHolder extends RecyclerView.ViewHolder {
        Button button;

        WorkerViewHolder(View view) {
            super(view);
            button = view.findViewById(R.id.foremanGraph);
        }
    }
}
