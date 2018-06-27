package za.org.samac.harvest.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import za.org.samac.harvest.PieChart;
import za.org.samac.harvest.R;
import za.org.samac.harvest.domain.Foreman;

public class ForemanRecyclerViewAdapter extends RecyclerView.Adapter<ForemanRecyclerViewAdapter.ForemanViewHolder> {

    private Context context;
    private List<Foreman> foremen;

    public ForemanRecyclerViewAdapter(Context context, List<Foreman> foremen) {
        this.context = context;
        this.foremen = foremen;
    }

    @Override
    public ForemanViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.foreman_list_item, parent, false);
        return new ForemanRecyclerViewAdapter.ForemanViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ForemanViewHolder holder, int position) {
        Foreman foreman = this.foremen.get(position);
        holder.tvEmail.setText(foreman.getEmail());
        holder.tvName.setText(foreman.getName());
    }

    @Override
    public int getItemCount() {
        return this.foremen.size();
    }

    public class ForemanViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvEmail;

        ForemanViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvName);
            tvEmail = view.findViewById(R.id.tvEmail);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PieChart.class);
                    context.startActivity(intent);
                }
            });
        }
    }
}
