package za.org.samac.harvest.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import za.org.samac.harvest.R;

public class SessionsViewAdapter extends RecyclerView.Adapter<SessionsViewAdapter.SessionsViewHolder> {

    private final  ArrayList<String> dates;
    private Map<String, ArrayList<String>> sessions;
    private Context context;

    public SessionsViewAdapter(Context applicationContext,  ArrayList<String> dates) {
        this.dates = dates;
        this.context = applicationContext;
    }

    @Override
    public SessionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sessions_grid_item, parent, false);

        return new SessionsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SessionsViewHolder holder, int position) {
        holder.dateOfSession.setText(dates.get(position));
        ArrayList<String> workerNames = sessions.get(dates.get(position));
        for(int i = 0; i < workerNames.size(); ++i){
            Button btn = new Button(context);
            btn.setTextColor(Color.BLACK);
            btn.setBackgroundColor(Color.WHITE);
            btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            btn.setText(workerNames.get(i));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // finish later
                }
            });
            holder.linearLayout.addView(btn);
        }
    }

    @Override
    public int getItemCount() {
        return this.dates.size();
    }

    public void setSessions(Map<String,ArrayList<String>> sessions) {
        this.sessions = sessions;
    }

    public class SessionsViewHolder extends RecyclerView.ViewHolder {
        TextView dateOfSession;
        LinearLayout linearLayout;

        SessionsViewHolder(View view) {
            super(view);
            dateOfSession = view.findViewById(R.id.dateOfSession);
            linearLayout = view.findViewById(R.id.linearSessions);
        }
    }
}
