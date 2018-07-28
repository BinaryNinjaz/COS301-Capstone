package za.org.samac.harvest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;
import java.util.Vector;

import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.DBInfoObject;
import za.org.samac.harvest.util.Worker;
import za.org.samac.harvest.util.WorkerType;

public class Analytics_Selector extends Fragment{

    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private Data data;
    private Category category;

    public Analytics_Selector(){
        //Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics_selector, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        swipeRefreshLayout = getView().findViewById(R.id.anal_select_swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        recyclerView = getView().findViewById(R.id.anal_select_recycler);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        if (Data.isPulling()){
            swipeRefreshLayout.setRefreshing(true);
        }
        else {
            adapter = new Analytics_Selector_Adapter(data, category);
            recyclerView.setAdapter(adapter);
        }
    }

    public void setDataAndCategory(Data data, Category category){
        this.data = data;
        this.category = category;
    }

    private void refresh(){
        data.pull();
    }

    public void endRefresh(){
        swipeRefreshLayout.setRefreshing(false);
        adapter = new Analytics_Selector_Adapter(data, category);
        recyclerView.setAdapter(adapter);
    }
}

class Analytics_Selector_Adapter extends RecyclerView.Adapter<Analytics_Selector_Adapter.ViewHolder>{

    private List<DBInfoObject> items;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public CheckBox checkBox;
        public TextView textView;

        public ViewHolder(View view){
            super(view);

            checkBox = view.findViewById(R.id.anal_itemCheckBox);
            textView = view.findViewById(R.id.anal_itemText);
        }
    }

    public Analytics_Selector_Adapter(Data data, Category category){
        items = new Vector<>();
        if (category == Category.ORCHARD){
            items.addAll(data.getOrchards());
        }
        else {
            items.addAll(data.getWorkers());
            for(int i = 0; i < items.size(); i++){
                Worker worker = (Worker) items.get(i);
                if (category == Category.FOREMAN && worker.getWorkerType() == WorkerType.WORKER){
                    items.remove(i);
                }
                else if (category == Category.WORKER && worker.getWorkerType() == WorkerType.FOREMAN){
                    items.remove(i);
                }
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.analytics_select_to_display_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(items.get(position).toString());
        holder.checkBox.setTag(items.get(position).getId());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}