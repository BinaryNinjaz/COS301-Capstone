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
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;
import java.util.Vector;

import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.Worker;
import za.org.samac.harvest.util.Orchard;
import za.org.samac.harvest.util.WorkerType;


/**
 * A 'simple' {@link Fragment} subclass.
 */
public class InfoWorkerFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Data data;
    private String ID;

    private boolean editable = false;
    private boolean newCreation = false;

    private Worker worker;

    private TextView fName;
    private TextView sName;
    private TextView id;
    private Switch fore;
    private TextView phone;
    private TextView further;


    public InfoWorkerFragment() {
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

        data.findObject(ID);
        worker = data.getActiveWorker();

        if (newCreation) {
            //Worker
            getView().findViewById(R.id.info_work_fName_look).setVisibility(View.GONE);
            fName = getView().findViewById(R.id.info_work_fName_edit);
            fName.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.info_work_sName_look).setVisibility(View.GONE);
            sName = getView().findViewById(R.id.info_work_sName_edit);
            sName.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.info_work_id_look).setVisibility(View.GONE);
            id = getView().findViewById(R.id.info_work_id_edit);
            id.setVisibility(View.VISIBLE);

            //Foreman
            fore = getView().findViewById(R.id.info_work_fore_switch);
            fore.setEnabled(true);

            //Phone
            getView().findViewById(R.id.info_work_phone_look).setVisibility(View.GONE);
            phone = getView().findViewById(R.id.info_work_phone_edit);
            phone.setVisibility(View.VISIBLE);

            //Further
            getView().findViewById(R.id.info_work_further_look).setVisibility(View.GONE);
            further = getView().findViewById(R.id.info_work_further_edit);
            further.setVisibility(View.VISIBLE);

            getView().findViewById(R.id.info_work_butt_edit).setVisibility(View.INVISIBLE);
            View temp = getView().findViewById(R.id.info_work_butt_save);
            temp.setVisibility(View.VISIBLE);
            temp.setTag("CREATE " + ID);

            getView().findViewById(R.id.info_work_butt_del).setVisibility(View.GONE);

            //Orchards
            mAdapter = new WorkerOrchardAdapter(data, true);
        }
        else {

            if (editable) {
                //Worker
                getView().findViewById(R.id.info_work_fName_look).setVisibility(View.GONE);
                fName = getView().findViewById(R.id.info_work_fName_edit);
                fName.setVisibility(View.VISIBLE);
                fName.setText(worker.getfName());
                getView().findViewById(R.id.info_work_sName_look).setVisibility(View.GONE);
                sName = getView().findViewById(R.id.info_work_sName_edit);
                sName.setVisibility(View.VISIBLE);
                sName.setText(worker.getsName());
                getView().findViewById(R.id.info_work_id_look).setVisibility(View.GONE);
                id = getView().findViewById(R.id.info_work_id_edit);
                id.setVisibility(View.VISIBLE);
                id.setText(worker.getnID());

                //Foreman
                fore = getView().findViewById(R.id.info_work_fore_switch);
                fore.setEnabled(true);
                if (worker.getWorkerType() == WorkerType.FOREMAN){
                    fore.setChecked(true);
                }
                else {
                    fore.setChecked(false);
                }

                //Phone
                getView().findViewById(R.id.info_work_phone_look).setVisibility(View.GONE);
                phone = getView().findViewById(R.id.info_work_phone_edit);
                phone.setVisibility(View.VISIBLE);
                phone.setText(worker.getnID());
                
                //Further
                getView().findViewById(R.id.info_work_further_look).setVisibility(View.GONE);
                further = getView().findViewById(R.id.info_work_further_edit);
                further.setText(worker.getFurther());
                further.setVisibility(View.VISIBLE);

                getView().findViewById(R.id.info_work_butt_edit).setVisibility(View.INVISIBLE);
                View temp = getView().findViewById(R.id.info_work_butt_save);
                temp.setVisibility(View.VISIBLE);
                temp.setTag("SAVE " + ID);
                getView().findViewById(R.id.info_work_butt_del).setTag("EDIT " + ID);

                //Orchards
                mAdapter = new WorkerOrchardAdapter(data, true);
                
            } else {
                TextView temp;
                temp = getView().findViewById(R.id.info_work_fName_look);
                temp.setText(worker.getfName());
                temp = getView().findViewById(R.id.info_work_sName_look);
                temp.setText(worker.getsName());
                temp = getView().findViewById(R.id.info_work_id_look);
                temp.setText(worker.getnID());

                //Foreman
                Switch sTemp = getView().findViewById(R.id.info_work_fore_switch);
                sTemp.setEnabled(false);
                if (worker.getWorkerType() == WorkerType.FOREMAN){
                    sTemp.setChecked(true);
                }
                else {
                    sTemp.setChecked(false);
                }

                temp = getView().findViewById(R.id.info_work_phone_look);
                temp.setText(worker.getPhone());
                
                temp = getView().findViewById(R.id.info_work_further_look);
                temp.setText(worker.getFurther());

                getView().findViewById(R.id.info_work_butt_edit).setTag(ID + " FARM");
                getView().findViewById(R.id.info_work_butt_save).setVisibility(View.INVISIBLE);
                getView().findViewById(R.id.info_work_butt_del).setTag("LOOK " + ID);

                //Orchards
                mAdapter = new WorkerOrchardAdapter(data, false);
            }

        }

        mRecyclerView = getView().findViewById(R.id.info_work_orchards_look);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

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
        worker.setfName(fName.getText().toString());
        worker.setsName(sName.getText().toString());
        worker.setnID(id.getText().toString());

        //Foreman
        if (fore.isChecked()){
            worker.setWorkerType(WorkerType.FOREMAN);
        }
        else {
            worker.setWorkerType(WorkerType.WORKER);
        }

        worker.setPhone(phone.getText().toString());
        worker.setFurther(further.getText().toString());

        //Assigned Orchards

        //Save Changes
        data.modifyActiveWorker(worker, false);
    }

    public void beNew(final boolean newThing){
        newCreation = newThing;
    }

    public void createEvent(){
        Worker newWorker = new Worker();
        newWorker.setfName(fName.getText().toString());
        newWorker.setsName(sName.getText().toString());
        newWorker.setnID(id.getText().toString());

        //Foreman
        if (fore.isChecked()){
            newWorker.setWorkerType(WorkerType.FOREMAN);
        }
        else {
            newWorker.setWorkerType(WorkerType.WORKER);
        }

        newWorker.setPhone(phone.getText().toString());
        newWorker.setFurther(further.getText().toString());

        //Assigned Orchards

        //Save
        newWorker.setfID(data.getNextIDForAddition());
        data.addWorker(newWorker);
    }
}

class WorkerOrchardAdapter extends RecyclerView.Adapter<WorkerOrchardAdapter.ViewHolder>{

    class OrchardSelect{
        public Orchard orchard;
        public boolean selected;

        public OrchardSelect(Orchard orchard){
            this.orchard = orchard;
        }

        public void check(boolean check){
            selected = check;
        }
    }
    private List<OrchardSelect> orchards;
    private Data data;
    private boolean select;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public CheckBox mCheckBox;
        public Button mButton;

        public ViewHolder(View view){
            super(view);

            mCheckBox = view.findViewById(R.id.info_work_orch_checkBox);
            mButton = view.findViewById(R.id.info_work_orch_button);
        }
    }

    public WorkerOrchardAdapter(Data data, boolean select){
        if (data != null) {
            this.select = select;
            this.data = data;
            orchards = new Vector<>();
//            List<Orchard> temp;
//            if(select) {
//                temp = data.getOrchards();
//            }
//            else {
//                temp = data.getActiveWorker().getAssignedOrchards();
//            }
            if (select){
                for (Orchard current : data.getOrchards()){
                    orchards.add(new OrchardSelect(current));
                }
                for (Orchard checked : data.getActiveWorker().getAssignedOrchards()){
                    for (OrchardSelect orchard : orchards){
                        if (checked == orchard.orchard){
                            orchard.check(true);
                            break;
                        }
                    }
                }
            }
            else {
                for (Orchard current : data.getActiveWorker().getAssignedOrchards()){
                    orchards.add(new OrchardSelect(current));
                }
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_goto, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        if (select) {
            holder.mCheckBox.setText(orchards.get(position).orchard.getName());
            holder.mCheckBox.setTag("Orchard " + data.getIDFromPosInArray(position));
            holder.mCheckBox.setChecked(orchards.get(position).selected);
            holder.mButton.setVisibility(View.GONE);
        }
        else {
            holder.mButton.setText(orchards.get(position).orchard.getName());
            holder.mButton.setTag("Orchard " + data.getIDFromPosInArray(position));
            holder.mCheckBox.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount(){
        return orchards.size();
    }
}
