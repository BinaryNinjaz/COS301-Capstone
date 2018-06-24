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

import java.util.Vector;

import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.Farm;
import za.org.samac.harvest.util.Orchard;


/**
 * A 'simple' {@link Fragment} subclass.
 */
public class InfoFarmFragment extends Fragment {


    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Data data;
    private String ID;

    private boolean editable = false;
    private boolean newCreation = false;

    private Farm farm;

    private TextView name;
    private TextView company;
    private TextView email;
    private TextView phone;
    private TextView province;
    private TextView town;
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
            //Farm
            getView().findViewById(R.id.info_farm_name_look).setVisibility(View.GONE);
            name = getView().findViewById(R.id.info_farm_name_edit);
            name.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.info_farm_company_look).setVisibility(View.GONE);
            company = getView().findViewById(R.id.info_farm_company_edit);
            company.setVisibility(View.VISIBLE);
            //Contact
            getView().findViewById(R.id.info_farm_email_look).setVisibility(View.GONE);
            email = getView().findViewById(R.id.info_farm_email_edit);
            email.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.info_farm_phone_look).setVisibility(View.GONE);
            phone = getView().findViewById(R.id.info_farm_phone_edit);
            phone.setVisibility(View.VISIBLE);
            //Location
            getView().findViewById(R.id.info_farm_province_look).setVisibility(View.GONE);
            province = getView().findViewById(R.id.info_farm_province_edit);
            province.setVisibility(View.VISIBLE);
            getView().findViewById(R.id.info_farm_town_look).setVisibility(View.GONE);
            town = getView().findViewById(R.id.info_farm_town_edit);
            town.setVisibility(View.VISIBLE);
            //Further
            getView().findViewById(R.id.info_farm_further_look).setVisibility(View.GONE);
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
                //Farm
                getView().findViewById(R.id.info_farm_name_look).setVisibility(View.GONE);
                name = getView().findViewById(R.id.info_farm_name_edit);
                name.setVisibility(View.VISIBLE);
                name.setText(farm.getName());
                getView().findViewById(R.id.info_farm_company_look).setVisibility(View.GONE);
                company = getView().findViewById(R.id.info_farm_company_edit);
                company.setVisibility(View.VISIBLE);
                company.setText(farm.getCompany());
                //Contact
                getView().findViewById(R.id.info_farm_email_look).setVisibility(View.GONE);
                email = getView().findViewById(R.id.info_farm_email_edit);
                email.setVisibility(View.VISIBLE);
                email.setText(farm.getEmail());
                getView().findViewById(R.id.info_farm_phone_look).setVisibility(View.GONE);
                phone = getView().findViewById(R.id.info_farm_phone_edit);
                phone.setVisibility(View.VISIBLE);
                phone.setText(farm.getPhone());
                //Location
                getView().findViewById(R.id.info_farm_province_look).setVisibility(View.GONE);
                province = getView().findViewById(R.id.info_farm_province_edit);
                province.setVisibility(View.VISIBLE);
                province.setText(farm.getProvince());
                getView().findViewById(R.id.info_farm_town_look).setVisibility(View.GONE);
                town = getView().findViewById(R.id.info_farm_town_edit);
                town.setVisibility(View.VISIBLE);
                town.setText(farm.getTown());
                //Further
                getView().findViewById(R.id.info_farm_further_look).setVisibility(View.GONE);
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
                TextView temp;
                temp = getView().findViewById(R.id.info_farm_name_look);
                temp.setText(farm.getName());
                temp = getView().findViewById(R.id.info_farm_company_look);
                temp.setText(farm.getCompany());
                temp = getView().findViewById(R.id.info_farm_email_look);
                temp.setText(farm.getEmail());
                temp = getView().findViewById(R.id.info_farm_phone_look);
                temp.setText(farm.getPhone());
                temp = getView().findViewById(R.id.info_farm_province_look);
                temp.setText(farm.getProvince());
                temp = getView().findViewById(R.id.info_farm_town_look);
                temp.setText(farm.getTown());
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
        farm.setCompany(company.getText().toString());
        farm.setEmail(email.getText().toString());
        farm.setPhone(phone.getText().toString());
        farm.setProvince(province.getText().toString());
        farm.setTown(town.getText().toString());
        farm.setFurther(further.getText().toString());
        data.modifyActiveFarm(farm, false);
    }

    public void beNew(final boolean newThing){
        newCreation = newThing;
    }

    public void createEvent(){
        Farm newFarm = new Farm();
        newFarm.setName(name.getText().toString());
        newFarm.setCompany(company.getText().toString());
        newFarm.setEmail(email.getText().toString());
        newFarm.setPhone(phone.getText().toString());
        newFarm.setProvince(province.getText().toString());
        newFarm.setTown(town.getText().toString());
        newFarm.setFurther(further.getText().toString());
        newFarm.setID(data.getNextIDForAddition());
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_goto, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position){
        holder.mButton.setText(orchards.elementAt(position));
        holder.mButton.setTag("Orchard " + data.getIDFromPosInArray(position));
    }

    @Override
    public int getItemCount(){
        return orchards.size();
    }
}
