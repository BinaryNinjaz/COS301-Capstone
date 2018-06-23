package za.org.samac.harvest;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
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
    private TextView dateText;
    private Date date;
    private RecyclerView cRecyclerView;
    private RecyclerView.Adapter cAdapter;
    private RecyclerView.LayoutManager cLayoutManager;
    private TextView nCultavar;
    private TextView row;
    private TextView tree;
    private TextView further;

    private Vector<String> cults;

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

            cults = new Vector<>();

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
            dateText = getView().findViewById(R.id.info_orch_date_edit);
            dateText.setKeyListener(null);
            dateText.setVisibility(View.VISIBLE);

            getView().findViewById(R.id.info_orch_cultivars_add_constraint).setVisibility(View.VISIBLE);

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
            cults = orch.getCultivars();

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
                dateText = getView().findViewById(R.id.info_orch_date_edit);
                dateText.setVisibility(View.VISIBLE);
                dateText.setKeyListener(null);
                if (orch.getDatePlanted() != null) {
                    if (orch.getDatePlanted().isSet(Calendar.YEAR)) {
                        dateText.setText(Integer.toString(orch.getDatePlanted().get(Calendar.DAY_OF_MONTH)) + "/" + Integer.toString(orch.getDatePlanted().get(Calendar.MONTH)) + "/" + Integer.toString(orch.getDatePlanted().get(Calendar.YEAR)));
                    }
                }

                getView().findViewById(R.id.info_orch_cultivars_add_constraint).setVisibility(View.VISIBLE);

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
                    if (orch.getDatePlanted().isSet(Calendar.YEAR)) {
                        temp.setText(Integer.toString(orch.getDatePlanted().get(Calendar.DAY_OF_MONTH)) + " " + Integer.toString(orch.getDatePlanted().get(Calendar.MONTH)) + " " + Integer.toString(orch.getDatePlanted().get(Calendar.YEAR)));
                    }
                }
                else{
                    temp.setText("");
                }

                getView().findViewById(R.id.info_orch_cultivars_add_constraint).setVisibility(View.GONE);

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

        cRecyclerView = getView().findViewById(R.id.info_orch_cultivars_look);
        cRecyclerView.setHasFixedSize(true);
        cLayoutManager = new LinearLayoutManager(getActivity());
        cRecyclerView.setLayoutManager(cLayoutManager);
        if (editable || newCreation){
            cAdapter = new CultivarAdapter(cults, true);
        }
        else {
            cAdapter = new CultivarAdapter(cults, false);
        }
        cRecyclerView.setAdapter(cAdapter);
    }

    public void setDataAndID(Data data, String ID){
//        this.name = getView().findViewById(R.id.thing);
        this.data = data;
        this.ID = ID;
    }

    public void biteMe(int day, int month, int year){
        dateText.setText(Integer.toString(day) + "/" + Integer.toString(month) + "/" + Integer.toString(year));
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
        if (!mass.getText().toString().equals("")) {
            orch.setMeanBagMass(Float.parseFloat(mass.getText().toString()));
        }
        orch.setIrrigation(irig.getText().toString());

        //Date Planted
        Calendar c = Calendar.getInstance();
        String[] tokens = dateText.getText().toString().split("/");
        if (tokens.length == 3) {
            c.set(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[0]));
            orch.setDatePlanted(c);
        }

        orch.setCultivars(cults);

//        orch.setAssignedFarm();
        if (!row.getText().toString().equals("")) {
            orch.setRow(Float.parseFloat(row.getText().toString()));
        }
        if (!tree.getText().toString().equals("")) {
            orch.setTree(Float.parseFloat(tree.getText().toString()));
        }
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

        //Date Planted
        Calendar c = Calendar.getInstance();
        String[] tokens = dateText.getText().toString().split("/");
        if (tokens.length == 3) {
            c.set(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[0]));
            newOrch.setDatePlanted(c);
        }

        newOrch.setCultivars(cults);

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

    public void addCult(){
        TextView temp = getView().findViewById(R.id.info_orch_cultivars_add_text);
        String sTemp = temp.getText().toString();
        cults.addElement(sTemp);
        cAdapter = new CultivarAdapter(cults, true);
        cRecyclerView.setAdapter(cAdapter);
        temp.setText("");
    }

    public void delCult(int id){
        cults.removeElementAt(id);
        cAdapter = new CultivarAdapter(cults, true);
        cRecyclerView.setAdapter(cAdapter);
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

class CultivarAdapter extends RecyclerView.Adapter<CultivarAdapter.ViewHolder>{
    //    private String[] orchards;
    private Vector<String> cultivars;
    boolean show = true;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ConstraintLayout mConstraint;
        public TextView cultName;
        public ImageButton delCultButt;

        public ViewHolder(View view){
            super(view);

            mConstraint = view.findViewById(R.id.info_orch_cultivar_holder);
            cultName = view.findViewById(R.id.info_orch_cultivar_cultivar);
            delCultButt = view.findViewById(R.id.info_orch_cultivar_cultivar_delButt);

        }
    }

    public CultivarAdapter(Vector<String> cults, boolean show){
        cultivars = cults;
        this.show = show;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_cultivar_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        holder.cultName.setText(cultivars.elementAt(position));
        holder.delCultButt.setTag(position);
        if (!show){
            holder.delCultButt.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount(){
        if (cultivars == null){
            return 0;
        }
        return cultivars.size();
    }
}
