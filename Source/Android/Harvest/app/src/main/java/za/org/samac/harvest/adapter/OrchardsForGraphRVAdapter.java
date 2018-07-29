package za.org.samac.harvest.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import za.org.samac.harvest.Analytics_Graph;
import za.org.samac.harvest.R;

public class OrchardsForGraphRVAdapter extends RecyclerView.Adapter<OrchardsForGraphRVAdapter.OrchardsViewHolder> {

    public Context context;
    private ArrayList<String> orchards;
    private ArrayList<String> orchardKeys;

    public OrchardsForGraphRVAdapter(Context context, ArrayList<String> orchards, ArrayList<String> orchardKeys) {
        this.context = context;
        this.orchards = orchards;
        this.orchardKeys = orchardKeys;
    }

    @Override
    public OrchardsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_orchard_graph_buttons, parent, false);

        return new OrchardsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(OrchardsViewHolder holder, int position) {
        final String orchardName = this.orchards.get(position);
        final String orchardKey = this.orchardKeys.get(position);
        holder.button.setText(orchardName);//set name of orchard
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent details = new Intent(context, Analytics_Graph.class);
                details.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Intent key = details.putExtra("key", orchardKey);
                Intent name = details.putExtra("name", orchardName);

                context.startActivity(details);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.orchards.size();
    }

    public class OrchardsViewHolder extends RecyclerView.ViewHolder {
        Button button;

        OrchardsViewHolder(View view) {
            super(view);
            button = view.findViewById(R.id.orchardGraph);
        }
    }
}
