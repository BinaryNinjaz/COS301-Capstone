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
import android.widget.Button;
import android.widget.TextView;

import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoListFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Category cat = Category.NOTHING;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Data data;

    public InfoListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){

        mSwipeRefreshLayout = getView().findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                }
        );

        String catString = "";
        switch (cat){
            case FARM:
                catString = "FARM";
                break;
            case ORCHARD:
                catString = "ORCHARD";
                break;
            case WORKER:
                catString = "WORKER";
                break;
        }
        getView().findViewById(R.id.addSomething).setTag(catString);

        mRecyclerView = getView().findViewById(R.id.showThings);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        data.setCategory(cat);

        if(Data.isPulling()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
        else {
            mAdapter = new infoAdapter(data);
            mRecyclerView.setAdapter(mAdapter);
        }

    }

    public void setData(Data data){
        this.data = data;
    }

    public void setCat(Category cat){
        this.cat = cat;
    }

    private void refresh() {
        data.pull();
    }

    public void endRefresh(){
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter = new infoAdapter(data);
        mRecyclerView.setAdapter(mAdapter);
    }
}

class infoAdapter extends RecyclerView.Adapter<infoAdapter.ViewHolder>{
    private String[] names;
    private Data data;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public Button mButton;

        public ViewHolder(View view){
            super(view);
            mButton = view.findViewById(R.id.info_list_butt);
        }
    }

    public infoAdapter(Data data){
        this.data = data;
        this.names = data.toNamesAsStringArray();
    }

    @Override
    public infoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        holder.mButton.setText(names[position]);
        holder.mButton.setTag(data.getIDFromPosInArray(position) + " " + data.getNamedCategory());
    }

    @Override
    public int getItemCount(){
        return names.length;
    }
}
