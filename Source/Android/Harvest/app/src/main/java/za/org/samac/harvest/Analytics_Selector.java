package za.org.samac.harvest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

    private Boolean showProceed;
    private List<String> ids;

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

        if (!showProceed){
            view.findViewById(R.id.anal_select_proceed).setVisibility(View.GONE);
        }

        recyclerView = getView().findViewById(R.id.anal_select_recycler);

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new Analytics_Selector_ItemDivider(getContext()));

        if (Data.isPulling()){
            swipeRefreshLayout.setRefreshing(true);
        }
        else {
            adapter = new Analytics_Selector_Adapter(data, category);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onDestroyView() {
        showProceed = null;
        super.onDestroyView();
    }

    public void setDataAndCategory(Data data, Category category){
        this.data = data;
        this.category = category;
    }

    private void refresh(){
        data.pull();
    }

    public void endRefresh(){
        for(String id : ids){
            data.findObject(id, category);
            data.getActiveThing().checked = true;
        }

        swipeRefreshLayout.setRefreshing(false);
        adapter = new Analytics_Selector_Adapter(data, category);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    public void showProceed(boolean show){
        showProceed = show;
    }

    public void setIDs(List<String> ids){
        this.ids = ids;
    }
}

class Analytics_Selector_ItemDivider extends RecyclerView.ItemDecoration{
    private Drawable divider;

    public Analytics_Selector_ItemDivider(Context context){
//        divider = context.getResources().getDrawable(R.drawable.line_divider);
        divider = ContextCompat.getDrawable(context, R.drawable.line_divider);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i ++){
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
}

class Analytics_Selector_Adapter extends RecyclerView.Adapter<Analytics_Selector_Adapter.ViewHolder>{

    private List<DBInfoObject> items;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public CheckBox checkBox;

        public ViewHolder(View view){
            super(view);

            checkBox = view.findViewById(R.id.anal_itemCheckBox);
        }
    }

    public Analytics_Selector_Adapter(Data data, Category category){
        items = new Vector<>();
        if (category == Category.ORCHARD){
            items.addAll(data.getOrchards());
        }
        else if (category == Category.FARM){
            items.addAll(data.getFarms());
        }
        else {
            List<Worker> workers = data.getWorkers();
            for (Worker worker : workers){
                if (worker.getWorkerType() == WorkerType.FOREMAN && category == Category.FOREMAN){
                    items.add(worker);
                }
                else if(worker.getWorkerType() == WorkerType.WORKER && category == Category.WORKER){
                    items.add(worker);
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
        holder.checkBox.setTag(items.get(position).getId());
        holder.checkBox.setText(items.get(position).toString());
        if (items.get(position).checked){
            holder.checkBox.setChecked(true);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}