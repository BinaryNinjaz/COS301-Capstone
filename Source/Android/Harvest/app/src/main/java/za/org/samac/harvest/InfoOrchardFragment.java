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

import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.Orchard;
import za.org.samac.harvest.util.Worker;


/**
 * A 'simple' {@link Fragment} subclass.
 */
public class InfoOrchardFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Data data;
    private String ID;

    private boolean editable = false;
    private boolean newCreation = false;

    private Orchard orch;

    private TextView name;
    private TextView crop;
    private TextView mass;
    private TextView irig;
    private TextView date;
    private RecyclerView cultivars;
    private RecyclerView.Adapter cAdapter;
    private RecyclerView.LayoutManager cLayoutManager;
    private TextView nCultavar;
    private TextView row;
    private TextView tree;
    private TextView further;

    public InfoOrchardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_info_orch, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){

        if ((editable && newCreation)) throw new AssertionError();

        if (newCreation) {
            getView().findViewById(R.id.info_orch_name_look).setVisibility(View.GONE);
            name = getView().findViewById(R.id.info_orch_name_edit);
            name.setVisibility(View.VISIBLE);

            getView().findViewById(R.id.info_orch_crop_look).setVisibility(View.GONE);
            crop = getView().findViewById(R.id.info_orch_crop_edit);
            crop.setVisibility(View.VISIBLE);

            getView().findViewById(R.id.info_orch_mass_look).setVisibility(View.GONE);
            mass = getView().findViewById(R.id.info_orch_mass_edit);
            mass.setVisibility(View.VISIBLE);

            getView().findViewById(R.id.info_orch_irig_look).setVisibility(View.GONE);
            irig = getView().findViewById(R.id.info_orch_irig_edit);
            irig.setVisibility(View.VISIBLE);

            getView().findViewById(R.id.info_orch_date_look).setVisibility(View.GONE);
            date = getView().findViewById(R.id.info_orch_date_edit);
            date.setVisibility(View.VISIBLE);
            
            getView().findViewById(R.id.info_orch_row_look).setVisibility(View.GONE);
            row = getView().findViewById(R.id.info_orch_row_edit);
            row.setVisibility(View.VISIBLE);

            getView().findViewById(R.id.info_orch_tree_look).setVisibility(View.GONE);
            tree = getView().findViewById(R.id.info_orch_tree_edit);
            tree.setVisibility(View.VISIBLE);

            getView().findViewById(R.id.info_orch_further_look).setVisibility(View.GONE);
            further = getView().findViewById(R.id.info_orch_further_edit);
            further.setVisibility(View.VISIBLE);

            getView().findViewById(R.id.info_orch_butt_edit).setVisibility(View.INVISIBLE);
            View temp = getView().findViewById(R.id.info_orch_butt_save);
            temp.setVisibility(View.VISIBLE);
            temp.setTag("CREATE " + ID);

            getView().findViewById(R.id.info_orch_work_card).setVisibility(View.GONE);
            getView().findViewById(R.id.info_orch_butt_del).setVisibility(View.GONE);
        }
        else {
            data.findObject(ID);
            orch = data.getActiveOrchard();

            if (editable) {

                getView().findViewById(R.id.info_orch_name_look).setVisibility(View.GONE);
                name = getView().findViewById(R.id.info_orch_name_edit);
                name.setVisibility(View.VISIBLE);
                name.setText(orch.getName());

                getView().findViewById(R.id.info_orch_crop_look).setVisibility(View.GONE);
                crop = getView().findViewById(R.id.info_orch_crop_edit);
                crop.setVisibility(View.VISIBLE);
                crop.setText(orch.getCrop());

                getView().findViewById(R.id.info_orch_mass_look).setVisibility(View.GONE);
                mass = getView().findViewById(R.id.info_orch_mass_edit);
                mass.setVisibility(View.VISIBLE);
                if (orch.getMeanBagMass() != null) {
                    mass.setText(orch.getMeanBagMass().toString());
                }

                getView().findViewById(R.id.info_orch_irig_look).setVisibility(View.GONE);
                irig = getView().findViewById(R.id.info_orch_irig_edit);
                irig.setVisibility(View.VISIBLE);
                irig.setText(orch.getIrrigation());

                getView().findViewById(R.id.info_orch_date_look).setVisibility(View.GONE);
                date = getView().findViewById(R.id.info_orch_date_edit);
                date.setVisibility(View.VISIBLE);
                date.setText(orch.getDatePlanted().toString());

                getView().findViewById(R.id.info_orch_row_look).setVisibility(View.GONE);
                row = getView().findViewById(R.id.info_orch_row_edit);
                row.setVisibility(View.VISIBLE);
                if (orch.getRow() != null) {
                    row.setText(orch.getRow().toString());
                }

                getView().findViewById(R.id.info_orch_tree_look).setVisibility(View.GONE);
                tree = getView().findViewById(R.id.info_orch_tree_edit);
                tree.setVisibility(View.VISIBLE);
                if (orch.getTree() != null) {
                    tree.setText(orch.getTree().toString());
                }
                
                getView().findViewById(R.id.info_orch_further_look).setVisibility(View.GONE);
                further = getView().findViewById(R.id.info_orch_further_edit);
                further.setText(orch.getFurther());
                further.setVisibility(View.VISIBLE);

                getView().findViewById(R.id.info_orch_butt_edit).setVisibility(View.INVISIBLE);
                View temp = getView().findViewById(R.id.info_orch_butt_save);
                temp.setVisibility(View.VISIBLE);
                temp.setTag("SAVE " + ID);
                getView().findViewById(R.id.info_orch_butt_del).setTag("EDIT " + ID);

                //Maybe we want this, no harm in leaving it in, but it's something not editable.
                getView().findViewById(R.id.info_orch_work_card).setVisibility(View.GONE);
            } else {
                TextView temp;

                temp = getView().findViewById(R.id.info_orch_name_look);
                temp.setText(orch.getName());

                temp = getView().findViewById(R.id.info_orch_crop_look);
                temp.setText(orch.getCrop());

                temp = getView().findViewById(R.id.info_orch_mass_look);
                Float tem = orch.getMeanBagMass();
                if (tem == null){
                    temp.setText("");
                }
                else {
                    temp.setText(tem.toString());
                }

                temp = getView().findViewById(R.id.info_orch_irig_look);
                temp.setText(orch.getIrrigation());

                temp = getView().findViewById(R.id.info_orch_date_look);
                if (orch.getDatePlanted() != null) {
                    temp.setText(orch.getDatePlanted().toString());
                }

                temp = getView().findViewById(R.id.info_orch_row_look);
                tem = orch.getRow();
                if (tem == null){
                    temp.setText("");
                }
                else {
                    temp.setText(tem.toString());
                }

                temp = getView().findViewById(R.id.info_orch_tree_look);
                tem = orch.getTree();
                if (tem == null){
                    temp.setText("");
                }
                else {
                    temp.setText(tem.toString());
                }

                temp = getView().findViewById(R.id.info_orch_further_look);
                temp.setText(orch.getFurther());

                getView().findViewById(R.id.info_orch_butt_edit).setTag(ID + " FARM");
                getView().findViewById(R.id.info_orch_butt_save).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.info_orch_butt_del).setTag("LOOK " + ID);
            }

            mRecyclerView = getView().findViewById(R.id.info_orch_work_look);
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
        orch.setName(name.getText().toString());
        orch.setCrop(crop.getText().toString());
        orch.setMeanBagMass(Float.parseFloat(mass.getText().toString()));
        orch.setIrrigation(irig.getText().toString());
//        orch.setDatePlanted(date.getText().toString());
//        orch.setAssignedFarm();
        orch.setRow(Float.parseFloat(row.getText().toString()));
        orch.setTree(Float.parseFloat(tree.getText().toString()));
        orch.setFurther(further.getText().toString());
        data.modifyActiveOrchard(orch, false);
    }

    public void beNew(final boolean newThing){
        newCreation = newThing;
    }

    public void createEvent(){
        Orchard newOrch = new Orchard();
        newOrch.setName(name.getText().toString());
        newOrch.setCrop(crop.getText().toString());
        String temp = mass.getText().toString();
        if (!temp.equals("")){
            newOrch.setMeanBagMass(Float.parseFloat(temp));
        }
        newOrch.setIrrigation(irig.getText().toString());

//        orch.setDatePlanted(date.getText().toString());
//        orch.setAssignedFarm();
        temp = row.getText().toString();
        if (!temp.equals("")){
            newOrch.setRow(Float.parseFloat(temp));
        }
        temp = tree.getText().toString();
        if (!temp.equals("")){
            newOrch.setTree(Float.parseFloat(temp));
        }
        newOrch.setFurther(further.getText().toString());
        newOrch.setID(data.getNextIDForAddition());
        data.addOrchard(newOrch);
    }
}

class WorkerAdapter extends RecyclerView.Adapter<WorkerAdapter.ViewHolder>{
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

    public WorkerAdapter(Data data){
        if (data != null) {
            this.data = data;
            orchards = new Vector<String>();

            for (Worker current : data.getWorkers()) {
                if (current.getAssignedOrchard() != null) {
                    if (current.getAssignedOrchard().getID().equals(data.getActiveOrchard().getID())) {
                        orchards.addElement(current.getfName() + current.getsName());
                    }
                }
            }
        }
//        this.orchards = data.toNamesAsStringArray(Category.ORCHARD);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
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
