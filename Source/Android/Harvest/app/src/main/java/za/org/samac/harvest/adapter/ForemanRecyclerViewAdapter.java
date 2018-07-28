package za.org.samac.harvest.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import za.org.samac.harvest.Analytics_Graph_Foremen;
import za.org.samac.harvest.R;

public class ForemanRecyclerViewAdapter extends RecyclerView.Adapter<ForemanRecyclerViewAdapter.ForemanViewHolder> {

    private Context context;
    private ArrayList<String> foremen;
    private ArrayList<String> foremenKeys;

    public ForemanRecyclerViewAdapter(Context applicationContext, ArrayList<String> foremen, ArrayList<String> foremenKeys) {
        this.context = applicationContext;
        this.foremen = foremen;
        this.foremenKeys = foremenKeys;
    }

    @Override
    public ForemanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_foremen_buttons, parent, false);

        return new ForemanViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ForemanViewHolder holder, int position) {
        final String foremenName = this.foremen.get(position);
        final String foremenKey = this.foremenKeys.get(position);
        holder.button.setText(foremenName);//set name of orchard
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent details = new Intent(context, Analytics_Graph_Foremen.class);
                details.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Intent key = details.putExtra("key", foremenKey);
                Intent name = details.putExtra("name", foremenName);

                context.startActivity(details);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.foremen.size();
    }

    public class ForemanViewHolder extends RecyclerView.ViewHolder {
        Button button;

        ForemanViewHolder(View view) {
            super(view);
            button = view.findViewById(R.id.foremanGraph);
        }
    }
}
