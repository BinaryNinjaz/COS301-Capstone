package za.org.samac.harvest;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;
import za.org.samac.harvest.util.DBInfoObject;
import za.org.samac.harvest.util.Worker;
import za.org.samac.harvest.util.WorkerType;

import static za.org.samac.harvest.Stats.THOUSAND;

public class Stats_Selector extends Fragment{

    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private Data data;
    private Category category;

    private Boolean showProceed;
    private Boolean showDateStuff = false;

    private double fromDate = 0, toDate = 0;

    private List<String> ids;

    private LinearLayout display;
    private LayoutInflater inflater;
    private List<CheckBox> checkBoxes;
    private View dateStuffView;
    private EditText upToDateEditText, fromDateEditText;

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

    @SuppressLint("SetTextI18n")
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

        upToDateEditText = view.findViewById(R.id.upToDate);
        fromDateEditText = view.findViewById(R.id.fromDate);

        if (showDateStuff){
            view.findViewById(R.id.dateStuff).setVisibility(View.VISIBLE);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis((long) fromDate);
            fromDateEditText.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR));
            calendar.setTimeInMillis((long) toDate);
            upToDateEditText.setText(calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) + 1) + "/" + calendar.get(Calendar.YEAR));
        }

        showDateStuff = false;

        fromDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateSpinner(v);
            }
        });

        upToDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateSpinner(v);
            }
        });

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

    public Stats.DateBundle getDates() {
        Stats.DateBundle dateBundle = new Stats.DateBundle();

        //noinspection ConstantConditions
        String[] tokens = fromDateEditText.getText().toString().split("/");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[1]) - 1, Integer.parseInt(tokens[0]), calendar.getActualMinimum(Calendar.HOUR_OF_DAY), calendar.getActualMinimum(Calendar.MINUTE), calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        Log.i(TAG, "start: " + calendar.getTime().toString());
        dateBundle.startDate = calendar.getTimeInMillis();

        //noinspection ConstantConditions
        tokens = upToDateEditText.getText().toString().split("/");
        calendar.set(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[1]) - 1, Integer.parseInt(tokens[0]), calendar.getMaximum(Calendar.HOUR_OF_DAY), calendar.getActualMaximum(Calendar.MINUTE), calendar.getActualMaximum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
        Log.i(TAG, "end: " + calendar.getTime().toString());
        dateBundle.endDate = calendar.getTimeInMillis();

        return dateBundle;
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

    public void showDateStuff(double from, double to){
        showDateStuff = true;
        fromDate = from;
        toDate = to;
    }

    public void setIDs(List<String> ids){
        this.ids = ids;
    }

    public static class StatsDatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
        EditText editText;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year, month, day;
            String text = editText.getText().toString();

            if (text.equals("")){
                //No date set, so set for today
                Calendar calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
            }
            else {
                String[] tokens = text.split("/");
                day = Integer.parseInt(tokens[0]);
                month = Integer.parseInt(tokens[1]) - 1;
                year = Integer.parseInt(tokens[2]);
            }

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @SuppressLint("SetTextI18n")
        public void onDateSet(DatePicker view, int year, int month, int day){
            int betterMonth = month + 1;
            editText.setText(day + "/" + betterMonth + "/" + year);
        }

        public void setEditText(EditText editText){
            this.editText = editText;
        }
    }

    public void showDateSpinner(View v){
        EditText editText = (EditText) v;
        Stats_Creator.StatsDatePickerFragment statsDatePickerFragment = new Stats_Creator.StatsDatePickerFragment();
        statsDatePickerFragment.setEditText(editText);
        statsDatePickerFragment.show(getFragmentManager(), "DATEPICKER");
    }
}