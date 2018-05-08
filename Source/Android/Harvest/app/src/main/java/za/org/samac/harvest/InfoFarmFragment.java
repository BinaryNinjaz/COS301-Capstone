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
import android.widget.TextView;

import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFarmFragment extends Fragment {

//    private String name;
//    private String further;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Data data;
    private String ID;

    public InfoFarmFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_farm, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        data.findObject(ID);
        TextView temp = getView().findViewById(R.id.info_farm_name_look);
        temp.setText(data.getName());
        temp = getView().findViewById(R.id.info_farm_further_look);
        temp.setText(data.getFurther());

        mRecyclerView = getView().findViewById(R.id.info_farm_orchards_look);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new OrchardAdapter(data);
        mRecyclerView.setAdapter(mAdapter);
    }

    public void setData(Data data, String ID){
//        this.name = getView().findViewById(R.id.thing);
        this.data = data;
        this.ID = ID;
    }
}

class OrchardAdapter extends RecyclerView.Adapter<OrchardAdapter.ViewHolder>{
    private String[] orchards;
    private Data data;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public Button mButton;

        public ViewHolder(View view){
            super(view);

            mButton = view.findViewById(R.id.info_list_butt);
        }
    }

    public OrchardAdapter(Data data){
        this.data = data;
        this.orchards = data.toNamesAsStringArray(Category.ORCHARD);
    }

    @Override
    public OrchardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        holder.mButton.setText(orchards[position]);
        holder.mButton.setTag(data.getIDFromPosInArray(position) + " " + "ORCHARD");
    }

    @Override
    public int getItemCount(){
        return orchards.length;
    }
}
