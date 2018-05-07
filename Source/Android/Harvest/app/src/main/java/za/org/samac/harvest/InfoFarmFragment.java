package za.org.samac.harvest;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFarmFragment extends Fragment {

    String name;
    String further;

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
        TextView temp = getView().findViewById(R.id.info_farm_name_look);
        temp.setText(name);
        temp = getView().findViewById(R.id.info_farm_further_look);
        temp.setText(this.further);
    }

    public void setFields(String name, String further){
//        this.name = getView().findViewById(R.id.thing);
        this.name = name;
        this.further = further;
    }
}
