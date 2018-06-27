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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import za.org.samac.harvest.R;
import za.org.samac.harvest.SessionItem;

public class SessionsViewAdapter extends RecyclerView.Adapter<SessionsViewAdapter.SessionsViewHolder> {

    private final  ArrayList<String> dates;
    private TreeMap<String, SessionItem> sessions;
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
        String date = dates.get(position);
        SessionItem item = sessions.get(date);

        holder.dateOfSession.setText(dates.get(position));
        holder.sessionForeman.setText(item.foreman);

//        Button btn = new Button(context);
//        btn.setTextColor(Color.BLACK);
//        btn.setBackgroundColor(Color.WHITE);
//        btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT));
//        btn.setText(item.foreman);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // finish later
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return this.dates.size();
    }

    public void setSessions(TreeMap<String, SessionItem> sessions) {
        this.sessions = sessions;
    }

    public class SessionsViewHolder extends RecyclerView.ViewHolder {
        TextView dateOfSession;
        Button sessionForeman;

        SessionsViewHolder(View view) {
            super(view);
            dateOfSession = view.findViewById(R.id.dateOfSession);
            sessionForeman = view.findViewById(R.id.sessionForeman);
        }
    }
}
