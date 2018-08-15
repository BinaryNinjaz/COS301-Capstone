package za.org.samac.harvest;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.DBInfoObject;
import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.Farm;
import za.org.samac.harvest.util.Orchard;
import za.org.samac.harvest.util.Worker;


/**
 * Displays and does the search
 */
public class InfoSearchFragment extends Fragment {

    Data data;
    List<Farm> farms;
    List<Orchard> orchards;
    List<Worker> workers;
    
    TextView titleFarms, titleOrchards, titleWorkers;
    RecyclerView recyclerFarms, recyclerOrchards, recyclerWorkers;
    SearcherAdapter adapterFarms, adapterOrchards, adapterWorkers;
    LinearLayout parent;
    Category category;

    public InfoSearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_search, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        titleFarms = getView().findViewById(R.id.info_search_farmsTitle);
        titleOrchards = getView().findViewById(R.id.info_search_orchardsTitle);
        titleWorkers = getView().findViewById(R.id.info_search_workersTitle);
        
        recyclerFarms = getView().findViewById(R.id.info_search_farms);
        recyclerOrchards = getView().findViewById(R.id.info_search_orchards);
        recyclerWorkers = getView().findViewById(R.id.info_search_workers);

        parent = getView().findViewById(R.id.recyclersParentLinearLayout);
        
        adapterFarms = new SearcherAdapter();
        adapterOrchards = new SearcherAdapter();
        adapterWorkers = new SearcherAdapter();
        
        recyclerFarms.setHasFixedSize(false);
        recyclerOrchards.setHasFixedSize(false);
        recyclerWorkers.setHasFixedSize(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setAutoMeasureEnabled(true);
        recyclerFarms.setLayoutManager(linearLayoutManager);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setAutoMeasureEnabled(true);
        recyclerOrchards.setLayoutManager(linearLayoutManager);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setAutoMeasureEnabled(true);
        recyclerWorkers.setLayoutManager(linearLayoutManager);
        
        recyclerFarms.setAdapter(adapterFarms);
        recyclerOrchards.setAdapter(adapterOrchards);
        recyclerWorkers.setAdapter(adapterWorkers);
    }

    public void setData(Data data){
        this.data = data;
    }

    public void setCategory(Category category){
        this.category = category;
    }
    
    public void searchForQuery(String query){
        adapterFarms.emptyResults();
        adapterOrchards.emptyResults();
        adapterWorkers.emptyResults();
        
        List<DBInfoObject> result = data.search(query, category);
        for (DBInfoObject dbInfoObject: result){
            if (dbInfoObject.getClass() == Farm.class){
                adapterFarms.addToShow(dbInfoObject);
            }
            else if (dbInfoObject.getClass() == Orchard.class){
                adapterOrchards.addToShow(dbInfoObject);
            }
            else if (dbInfoObject.getClass() == Worker.class){
                adapterWorkers.addToShow(dbInfoObject);
            }
        }

        adapterFarms.notifyDataSetChanged();
        adapterOrchards.notifyDataSetChanged();
        adapterWorkers.notifyDataSetChanged();

        recyclerFarms.invalidate();
        recyclerOrchards.invalidate();
        recyclerWorkers.invalidate();

        parent.invalidate();
        
        if (adapterFarms.shouldShow()){
            recyclerFarms.setVisibility(View.VISIBLE);
            titleFarms.setVisibility(View.VISIBLE);
        }
        else {
            recyclerFarms.setVisibility(View.GONE);
            titleFarms.setVisibility(View.GONE);
        }
        
        if (adapterOrchards.shouldShow()){
            recyclerOrchards.setVisibility(View.VISIBLE);
            titleOrchards.setVisibility(View.VISIBLE);
        }
        else {
            recyclerOrchards.setVisibility(View.GONE);
            titleOrchards.setVisibility(View.GONE);
        }
        
        if (adapterWorkers.shouldShow()){
            recyclerWorkers.setVisibility(View.VISIBLE);
            titleWorkers.setVisibility(View.VISIBLE);
        }
        else {
            recyclerWorkers.setVisibility(View.GONE);
            titleWorkers.setVisibility(View.GONE);
        }
    }
    
}

class SearcherAdapter extends RecyclerView.Adapter<SearcherAdapter.ViewHolder>{

    private List<DBInfoObject> showUs;
    
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public Button mButton;

        public ViewHolder(View view){
            super(view);

            mButton = view.findViewById(R.id.info_list_butt);
        }
    }

    public SearcherAdapter(){
        showUs = new ArrayList<>();
    }
    
    public void addToShow(DBInfoObject addMe){
        showUs.add(addMe);
    }
    
    public void emptyResults(){
        showUs.clear();
    }
    
    public boolean shouldShow(){
        return showUs.size() > 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_goto, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        holder.mButton.setText(showUs.get(position).toString());
        if (showUs.get(position).getClass() == Worker.class) {
            holder.mButton.setTag("Worker " + showUs.get(position).getId());
        }
        else if (showUs.get(position).getClass() == Orchard.class){
            holder.mButton.setTag("Orchard " + showUs.get(position).getId());
        }
        else if (showUs.get(position).getClass() == Farm.class){
            holder.mButton.setTag("Farm " + showUs.get(position).getId());
        }
    }

    @Override
    public int getItemCount(){
        return showUs.size();
    }
}
