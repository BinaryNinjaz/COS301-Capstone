package za.org.samac.harvest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import za.org.samac.harvest.util.Category;

@SuppressWarnings("FieldCanBeLocal")
public class Stats_Main extends Fragment{
    
    RecyclerView farmsRecycler, orchardsRecycler, workersRecycler, foremenRecycler;
    TextView farmsText, orchardsText, workersText, foremenText;

    private final String TAG = "Stats_Main";

    public Stats_Main(){
        //Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        farmsRecycler = view.findViewById(R.id.stats_choose_farmRecycler);
        orchardsRecycler = view.findViewById(R.id.stats_choose_orchardRecycler);
        workersRecycler = view.findViewById(R.id.stats_choose_workerRecycler);
        foremenRecycler = view.findViewById(R.id.stats_choose_foremanRecycler);
        
        farmsText = view.findViewById(R.id.stats_choose_farms_title);
        orchardsText = view.findViewById(R.id.stats_choose_orchards_title);
        workersText = view.findViewById(R.id.stats_choose_workers_title);
        foremenText = view.findViewById(R.id.stats_choose_foremen_title);

        updateRecyclers(Category.FARM);
    }

    /**
     * Update all of the recyclers
     * @param categories which of the recyclers need to be restored, as in, the stored contents need to be recreated.
     */
    public void updateRecyclers(Category ... categories){
        boolean farm = false, orchard = false, worker = false, foreman = false;
        for (Category category : categories){
            switch (category){
                case FARM:
                    farm = true;
                    break;
                case ORCHARD:
                    orchard = true;
                    break;
                case WORKER:
                    worker = true;
                    break;
                case FOREMAN:
                    foreman = true;
                    break;
            }
        }

        populate(Category.FARM, farm, farmsRecycler, farmsText);
        populate(Category.ORCHARD, orchard, orchardsRecycler, orchardsText);
        populate(Category.WORKER, worker, workersRecycler, workersText);
        populate(Category.FOREMAN, foreman, foremenRecycler, foremenText);

    }

    private void populate(Category category, boolean restore, RecyclerView recyclerView, TextView textView){
        List<String> list = Stats.GraphDB.getNamesByCategory(category, getContext(), restore);
        if (Stats.GraphDB.isThere(category)){
            recyclerView.setHasFixedSize(false);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);
            RecyclerView.Adapter adapter = new SavedGraphsAdapter(list, getActivity());
            recyclerView.setAdapter(adapter);
            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        else {
            textView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }
    }
}

class SavedGraphsAdapter extends RecyclerView.Adapter<SavedGraphsAdapter.ViewHolder>{

    private List<String> names;
    private HoldListener holdListener;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public Button button;

        public ViewHolder(View view){
            super(view);

            button = view.findViewById(R.id.stats_graph_chooseButton);
        }
    }

    public SavedGraphsAdapter(List<String> names, Activity activity){
        this.holdListener = (HoldListener) activity;
        this.names = names;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stats_graph_button, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.button.setText(names.get(position));
        holder.button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                holdListener.showPopup(((Button) v).getText().toString());
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public interface HoldListener{
        public void showPopup(String name);
    }
}