package za.org.samac.harvest.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import za.org.samac.harvest.R;
import za.org.samac.harvest.SessionItem;
import za.org.samac.harvest.Sessions;
import za.org.samac.harvest.util.SearchedItem;

public class SessionsViewAdapter extends RecyclerView.Adapter<SessionsViewAdapter.SessionsViewHolder> {

    private ArrayList<SearchedItem.Session> items;
    private Context context;
    private Sessions sessionsClass;

    public SessionsViewAdapter(Context applicationContext, Sessions sc) {
        this.context = applicationContext;
        sessionsClass = sc;
    }

    @Override
    public SessionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == SessionsViewHolder.TYPE_HEADER) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sessions_grid_header, parent, false);
            return new SessionsHeaderViewHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sessions_grid_item, parent, false);
            return new SessionsItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(SessionsViewHolder holder, int position) {
        final SearchedItem.Session item = items.get(position);
        int type = getItemViewType(position);

        if (type == SessionsViewHolder.TYPE_HEADER) {
            SessionsHeaderViewHolder headerHolder = (SessionsHeaderViewHolder)holder;
            headerHolder.textView.setText(item.reason == null ? "" : item.reason);
        } else {
            SessionsItemViewHolder itemHolder = (SessionsItemViewHolder)holder;

            SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd MMMM YYYY 'at' HH:mm", Locale.getDefault());
            formatter.setCalendar(Calendar.getInstance());
            final String date = formatter.format(item.session.startDate);

            itemHolder.textView.setText(item.session.foreman + "\n" + date);
            itemHolder.detailTextView.setText(item.reason == null ? "" : item.reason);
            itemHolder.cell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent details = new Intent(context, SessionDetails.class);
                    details.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Sessions.selectedItem = item.session;
                    context.startActivity(details);
                }
            });
        }

        if (position == items.size() - 1) {
            sessionsClass.getNewPage();
        }
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).session == null
                ? SessionsViewHolder.TYPE_HEADER
                : SessionsViewHolder.TYPE_ITEM;
    }

    public void setItems(ArrayList<SearchedItem.Session> items) {
        this.items = items;
    }

    public abstract class SessionsViewHolder extends  RecyclerView.ViewHolder {
        public SessionsViewHolder(View view) {
            super(view);
        }

        public static final int TYPE_HEADER = 0;
        public static final int TYPE_ITEM = 1;

        abstract public int getType();
    }

    public class SessionsItemViewHolder extends SessionsViewHolder {
        CardView cell;
        TextView detailTextView;
        TextView textView;

        SessionsItemViewHolder(View view) {
            super(view);
            cell = view.findViewById(R.id.card_view_sessions);
            textView = view.findViewById(R.id.sessionGridItemTextView);
            detailTextView = view.findViewById(R.id.sessionGridItemDetailTextView);
        }

        @Override
        public int getType() {
            return TYPE_HEADER;
        }
    }

    public class SessionsHeaderViewHolder extends SessionsViewHolder {
        CardView cell;
        TextView textView;

        SessionsHeaderViewHolder(View view) {
            super(view);
            cell = view.findViewById(R.id.card_view_header_session);
            textView = view.findViewById(R.id.sessionHeaderTextView);
        }

        @Override
        public int getType() {
            return TYPE_HEADER;
        }
    }
}
