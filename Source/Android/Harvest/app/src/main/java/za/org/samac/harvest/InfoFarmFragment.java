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

import java.util.Vector;

import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.Farm;
import za.org.samac.harvest.util.Orchard;


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

    private boolean editable = false;
    private boolean newCreation = false;

    private Farm farm;

    private TextView name;
    private TextView further;


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

        if ((editable && newCreation)) throw new AssertionError();

        if (newCreation) {
            getView().findViewById(R.id.info_farm_name_look).setVisibility(View.GONE);
            getView().findViewById(R.id.info_farm_further_look).setVisibility(View.GONE);
            name = getView().findViewById(R.id.info_farm_name_edit);
            name.setVisibility(View.VISIBLE);
            further = getView().findViewById(R.id.info_farm_further_edit);
            further.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.info_farm_butt_edit).setVisibility(View.INVISIBLE);
            View temp = getView().findViewById(R.id.info_farm_butt_save);
            temp.setVisibility(View.VISIBLE);
            temp.setTag("CREATE " + ID);

            getView().findViewById(R.id.info_farm_orchards_card).setVisibility(View.GONE);
            getView().findViewById(R.id.info_farm_butt_del).setVisibility(View.GONE);
        }
        else {
            data.findObject(ID);
            farm = data.getActiveFarm();

            if (editable) {
                getView().findViewById(R.id.info_farm_name_look).setVisibility(View.GONE);
                getView().findViewById(R.id.info_farm_further_look).setVisibility(View.GONE);
                name = getView().findViewById(R.id.info_farm_name_edit);
                name.setVisibility(View.VISIBLE);
                name.setText(farm.getName());
                further = getView().findViewById(R.id.info_farm_further_edit);
                further.setText(farm.getFurther());
                further.setVisibility(View.VISIBLE);
                getView().findViewById(R.id.info_farm_butt_edit).setVisibility(View.INVISIBLE);
                View temp = getView().findViewById(R.id.info_farm_butt_save);
                temp.setVisibility(View.VISIBLE);
                temp.setTag("SAVE " + ID);
                getView().findViewById(R.id.info_farm_butt_del).setTag("EDIT " + ID);

                //Maybe we want this, no harm in leaving it in, but it's something not editable.
                getView().findViewById(R.id.info_farm_orchards_card).setVisibility(View.GONE);
            } else {
                TextView temp = getView().findViewById(R.id.info_farm_name_look);
                temp.setText(farm.getName());
                temp = getView().findViewById(R.id.info_farm_further_look);
                temp.setText(farm.getFurther());
                getView().findViewById(R.id.info_farm_butt_edit).setTag(ID + " FARM");
                getView().findViewById(R.id.info_farm_butt_save).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.info_farm_butt_del).setTag("LOOK " + ID);
            }

            mRecyclerView = getView().findViewById(R.id.info_farm_orchards_look);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mAdapter = new OrchardAdapter(data);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    public void setDataAndID(Data data, String ID){
//        this.name = getView().findViewById(R.id.thing);
        this.data = data;
        this.ID = ID;
    }

    public void setData(Data data){
        this.data = data;
    }

    public void beEditable(final boolean editable){
        this.editable = editable;
    }

    public void saveEvent(){
        farm.setName(name.getText().toString());
        farm.setFurther(further.getText().toString());
        data.modifyActiveFarm(farm, false);
    }

    public void beNew(final boolean newThing){
        newCreation = newThing;
    }

    public void createEvent(){
        Farm newFarm = new Farm(name.getText().toString(), further.getText().toString(), data.getNextIDForAddition());
        data.addFarm(newFarm);
    }
}

class OrchardAdapter extends RecyclerView.Adapter<OrchardAdapter.ViewHolder>{
//    private String[] orchards;
    private Vector<String> orchards;
    private Data data;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public Button mButton;

        public ViewHolder(View view){
            super(view);

            mButton = view.findViewById(R.id.info_list_butt);
        }
    }

    public OrchardAdapter(Data data){
        if (data != null) {
            this.data = data;
            orchards = new Vector<String>();

            for (Orchard current : data.getOrchards()) {
                if (current.getAssignedFarm() != null) {
                    if (current.getAssignedFarm().getID().equals(data.getActiveFarm().getID())) {
                        orchards.addElement(current.getName());
                    }
                }
            }
        }
//        this.orchards = data.toNamesAsStringArray(Category.ORCHARD);
    }

    @Override
    public OrchardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        holder.mButton.setText(orchards.elementAt(position));
        holder.mButton.setTag(data.getIDFromPosInArray(position) + " " + "ORCHARD");
    }

    @Override
    public int getItemCount(){
        return orchards.size();
    }
}
