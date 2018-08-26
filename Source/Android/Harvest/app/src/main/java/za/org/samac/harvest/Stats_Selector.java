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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.DBInfoObject;
import za.org.samac.harvest.util.Worker;
import za.org.samac.harvest.util.WorkerType;

public class Stats_Selector extends Fragment{

    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private Data data;
    private Category category;

    private Boolean showProceed;
    private List<String> ids;

    private LinearLayout display;
    private LayoutInflater inflater;
    private List<CheckBox> checkBoxes;

    private Boolean farmOwnerChecked;


    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "Stats_Selector";

    public Stats_Selector(){
        //Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_selector, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        swipeRefreshLayout = getView().findViewById(R.id.stats_select_swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        if (!showProceed){
            view.findViewById(R.id.stats_select_proceed).setVisibility(View.GONE);
        }

        Log.i(TAG, "size: " + ids.size());

        inflater = getLayoutInflater();
        display = view.findViewById(R.id.stats_select_display);
        checkBoxes = new ArrayList<>();

        if (Data.isPulling()){
            swipeRefreshLayout.setRefreshing(true);
        }
        else {
            endRefresh();
        }
    }

    @Override
    public void onDestroyView() {
        showProceed = null;
        ids = null;
        farmOwnerChecked = null;
        super.onDestroyView();
    }

    public void setDataAndCategory(Data data, Category category){
        this.data = data;
        this.category = category;
    }

    public void setFarmOwnerChecked(Boolean farmOwnerChecked){
        this.farmOwnerChecked = farmOwnerChecked;
    }

    public Category getCategory() {
        return category;
    }

    private void refresh(){
        data.pull();
    }

    public void endRefresh(){

        List things = data.getThings(category);
        for (Object thing : things){ //Okay then
            DBInfoObject thingerThing = (DBInfoObject) thing;
            if (ids.contains(thingerThing.getId())){
                thingerThing.checked = true;
            }
            else {
                thingerThing.checked = false;
            }
        }

        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

        constructDisplay();
    }

    public void checkAllPerhaps(boolean check){
        data.toggleCheckedness(check);
        for (CheckBox checkBox : checkBoxes){
            checkBox.setChecked(check);
        }
    }

    @SuppressWarnings("CollectionAddAllCanBeReplacedWithConstructor")
    private void constructDisplay(){
        display.removeAllViews();
        List<DBInfoObject> items = new ArrayList<>();

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
                else if (worker.getWorkerType() == WorkerType.WORKER && category == Category.WORKER){
                    items.add(worker);
                }
            }
        }

        for (DBInfoObject object : items){
            LinearLayout newBox = (LinearLayout) inflater.inflate(R.layout.stats_select_to_display_item, null, false);
            CheckBox box = newBox.findViewById(R.id.stats_itemCheckBox);
            box.setText(object.toString());
            box.setTag(object.getId());
            box.setChecked(object.checked);
            checkBoxes.add(box);
            display.addView(newBox);
        }
        if (category == Category.FOREMAN) {
            LinearLayout newBox = (LinearLayout) inflater.inflate(R.layout.stats_select_to_display_item, null, false);
            CheckBox box = newBox.findViewById(R.id.stats_itemCheckBox);
            box.setText(getResources().getString(R.string.farm_owner));
            box.setTag(FirebaseAuth.getInstance().getCurrentUser().getUid());
            box.setChecked(farmOwnerChecked);
            box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    farmOwnerChecked = isChecked;
                }
            });
            checkBoxes.add(box);
            display.addView(newBox);
        }
    }

    public void showProceed(boolean show){
        showProceed = show;
    }

    public void setIDs(List<String> ids){
        this.ids = ids;
    }
}