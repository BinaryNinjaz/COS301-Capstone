package za.org.samac.harvest.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import za.org.samac.harvest.R;
import za.org.samac.harvest.SessionItem;
import za.org.samac.harvest.Sessions;

public class SessionsViewAdapter extends RecyclerView.Adapter<SessionsViewAdapter.SessionsViewHolder> {

    private ArrayList<String> dates;
    private TreeMap<String, SessionItem.Selection> sessions;
    private Context context;
    private Sessions sessionsClass;

    public SessionsViewAdapter(Context applicationContext, Sessions sc) {
        this.context = applicationContext;
        sessionsClass = sc;
    }

    @Override
    public SessionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sessions_grid_item, parent, false);

        return new SessionsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SessionsViewHolder holder, int position) {
        String date = dates.get(position);
        final SessionItem.Selection item = sessions.get(date);

        holder.sessionForeman.setText(item.foreman + " - " + dates.get(position));
        holder.sessionForeman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent details = new Intent(context, SessionDetails.class);
                Intent key = details.putExtra("key", item.key);
                Intent sdate = details.putExtra("start_date", item.startDate);
                Intent wkey = details.putExtra("wid", item.foreman);

                context.startActivity(details);
            }
        });

        if (position == dates.size() - 1) {
            sessionsClass.getNewPage();
        }
    }

    @Override
    public int getItemCount() {
        return this.dates.size();
    }



    public void setSessions(TreeMap<String, SessionItem.Selection> sessions) {
        this.sessions = sessions;
    }

    public void setDates(ArrayList<String> dates) {
        this.dates = dates;
    }

    public class SessionsViewHolder extends RecyclerView.ViewHolder {
        TextView dateOfSession;
        Button sessionForeman;

        SessionsViewHolder(View view) {
            super(view);
            sessionForeman = view.findViewById(R.id.sessionForeman);
        }
    }
}
