package za.org.samac.harvest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;
import java.util.Vector;

import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.DBInfoObject;
import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.Worker;
import za.org.samac.harvest.util.WorkerType;

public class Analytics_Main extends Fragment {
    
    RecyclerView farmsRecycler, orchardsRecycler, workersRecycler, foremenRecycler;
    TextView farmsText, orchardsText, workersText, foremenText;

    public Analytics_Main(){
        //Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        farmsRecycler = view.findViewById(R.id.anal_choose_farmRecycler);
        orchardsRecycler = view.findViewById(R.id.anal_choose_orchardRecycler);
        workersRecycler = view.findViewById(R.id.anal_choose_workerRecycler);
        foremenRecycler = view.findViewById(R.id.anal_choose_foremanRecycler);
        
        farmsText = view.findViewById(R.id.anal_choose_farms_title);
        orchardsText = view.findViewById(R.id.anal_choose_orchards_title);
        workersText = view.findViewById(R.id.anal_choose_workers_title);
        foremenText = view.findViewById(R.id.anal_choose_foremen_title);

        populate(Category.FARM, true, farmsRecycler, farmsText);
        populate(Category.ORCHARD, false, orchardsRecycler, orchardsText);
        populate(Category.WORKER, false, workersRecycler, workersText);
        populate(Category.FOREMAN, false, foremenRecycler, foremenText);
    }

    private void populate(Category category, boolean restore, RecyclerView recyclerView, TextView textView){
        List<String> list = GraphDB.getNamesByCategory(category, getContext(), restore);
        if (GraphDB.isThere(category)){
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);
            RecyclerView.Adapter adapter = new SavedGraphsAdapter(list);
            recyclerView.setAdapter(adapter);
            textView.setVisibility(View.VISIBLE);
        }
        else {
            textView.setVisibility(View.GONE);
        }
    }
}

class SavedGraphsAdapter extends RecyclerView.Adapter<SavedGraphsAdapter.ViewHolder>{

    private List<String> names;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public Button button;

        public ViewHolder(View view){
            super(view);

            button = view.findViewById(R.id.anal_graph_chooseButton);
        }
    }

    public SavedGraphsAdapter(List<String> names){
        this.names = names;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.analytics_graph_button, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.button.setText(names.get(position));
    }

    @Override
    public int getItemCount() {
        return names.size();
    }
}