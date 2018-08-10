package za.org.samac.harvest;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

import static za.org.samac.harvest.Stats.THOUSAND;

@SuppressWarnings("FieldCanBeLocal")
public class Stats_Creator extends Fragment{

    //Views
    private Spinner compareSpinner, periodSpinner, intervalSpinner;
    private Button selectorButton;
    private TextView compareSelectionTextView, accumulatorDescriptionTextView;
    private EditText fromDateEditText, upToDateEditText;
    private RadioGroup accumulatorRadioGroup;

    //Specification
    private final String TAG = "Stats_Creator";

    //Others
    private String accumulationSelection = Stats.NOTHING;

    private String selectedItemsText;

    private int lastSelection, lastAcceptedSelection;


    public Stats_Creator(){
        //Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats_creator, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //initialize views
        compareSpinner = view.findViewById(R.id.stats_create_compSpinner);
        periodSpinner = view.findViewById(R.id.stats_create_periodSpinner);
        intervalSpinner = view.findViewById(R.id.stats_create_interval_spinner);

        selectorButton = view.findViewById(R.id.stats_create_selectionButton);

        compareSelectionTextView = view.findViewById(R.id.stats_create_selectionDisplayTextView);
        compareSelectionTextView.setVisibility(View.VISIBLE);
        accumulatorDescriptionTextView = view.findViewById(R.id.stats_create_accumulator_description);

        fromDateEditText = view.findViewById(R.id.stats_create_from);
        upToDateEditText = view.findViewById(R.id.stats_create_upTo);

        accumulatorRadioGroup = view.findViewById(R.id.stats_create_accumulator_radioGroup);

        //Populate Spinners
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.stats_create_compsChoose, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        compareSpinner.setAdapter(arrayAdapter);

        arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.stats_create_periodChoose, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(arrayAdapter);

        arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.stats_create_intervalChoose, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalSpinner.setAdapter(arrayAdapter);

        //Set selector button text
        setSelectorButtonTitle("farm");

        //Set default accumulator selection
        accumulatorRadioGroup.check(R.id.stats_create_accumulator_radio_none);
        accumulationSelection = Stats.ACCUMULATION_NONE;

        //Set Accumulation hint
        updateAccumulatorHint();

        //Set listeners
        compareSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                @SuppressWarnings("ConstantConditions") String entity = Stats.pluralizor(compareSpinner.getSelectedItem().toString(), false).toLowerCase();
                setSelectorButtonTitle(entity);
                updateAccumulatorTitles();
                updateAccumulatorHint();
                lastSelection = position;
                if (lastAcceptedSelection == lastSelection){
                    compareSelectionTextView.setVisibility(View.VISIBLE);
                }
                else {
                    compareSelectionTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toggleDates(periodSpinner.getSelectedItemPosition() == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        intervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateAccumulatorHint();
                updateAccumulatorTitles();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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

        accumulatorRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.stats_create_accumulator_radio_none:
                        accumulationSelection = Stats.ACCUMULATION_NONE;
                        break;
                    case R.id.stats_create_accumulator_radio_entity:
                        accumulationSelection = Stats.ACCUMULATION_ENTITY;
                        break;
                    case R.id.stats_create_accumulator_radio_interval:
                        accumulationSelection = Stats.ACCUMULATION_TIME;
                        break;
                }
                updateAccumulatorHint();
            }
        });

        compareSelectionTextView.setText(selectedItemsText);
    }

    @Override
    public void onDestroyView() {
        selectedItemsText = "";
        super.onDestroyView();
    }

    //UI
    private void setSelectorButtonTitle(String entity){
        selectorButton.setText(getString(R.string.stats_create_selectorButton, Stats.pluralizor(entity, true)));
    }

    /**
     * Set the text that shows the items that have been selected for comparison
     * @param text The text itself, so must be formatted already.
     */
    public void setSelectedItemsText(String text){
        selectedItemsText = text;
    }

    private void updateAccumulatorHint(){
        switch (accumulationSelection){
            case Stats.ACCUMULATION_NONE:
                accumulatorDescriptionTextView.setText(getString(R.string.stats_create_accumulation_desc_none, Stats.pluralizor(compareSpinner.getSelectedItem().toString(), false).toLowerCase()));
                break;
            case Stats.ACCUMULATION_ENTITY:
                accumulatorDescriptionTextView.setText(getString(R.string.stats_create_accumulation_desc_entity, Stats.pluralizor(compareSpinner.getSelectedItem().toString(), false).toLowerCase(), Stats.pluralizor(compareSpinner.getSelectedItem().toString(), true).toLowerCase(), Stats.timeConverter(intervalSpinner.getSelectedItem().toString(), false).toLowerCase()));
                break;
            case Stats.ACCUMULATION_TIME:
                accumulatorDescriptionTextView.setText(getString(R.string.stats_create_accumulation_desc_time, Stats.timeConverter(intervalSpinner.getSelectedItem().toString(), false).toLowerCase(), Stats.pluralizor(compareSpinner.getSelectedItem().toString(), true).toLowerCase()));
        }
    }

    private void updateAccumulatorTitles(){
        ((RadioButton) getView().findViewById(R.id.stats_create_accumulator_radio_entity)).setText(getString(R.string.stats_create_accumulation_entity, Stats.pluralizor(compareSpinner.getSelectedItem().toString(), false)));
        ((RadioButton) getView().findViewById(R.id.stats_create_accumulator_radio_interval)).setText(getString(R.string.stats_create_accumulation_interval, Stats.timeConverter(intervalSpinner.getSelectedItem().toString(), false)));
    }

    public void toggleDates(boolean on){
        if (on){
            fromDateEditText.setVisibility(View.VISIBLE);
            upToDateEditText.setVisibility(View.VISIBLE);
        }
        else {
            fromDateEditText.setVisibility(View.GONE);
            upToDateEditText.setVisibility(View.GONE);
        }
    }

    //Fragment > Activity Communication

    public String getGroup(){
        return Stats.pluralizor(compareSpinner.getSelectedItem().toString(), false).toLowerCase();
    }

    /**
     * Get a bundle consisting of all of the configurations that have been selected by the user, the keys match the strings in the Stats class.<br>
     * The bundle is constructed as follows (Key : Data Type : Description):<br>
     *  KEY_GROUP : String : Matches a static from Stats class.<br>
     *  KEY_PERIOD : String : Matches a static from Stats class<br>
     *  KEY_START : double : The selected start date divided by 1 000<br>
     *  KEY_END : double : The selected end date, divided by 1 000<br>
     *  KEY_INTERVAL : String : Matches a static from Stats class<br>
     *  KEY_ACCUMULATION : String : Matches a static from Stats class<br>
     * @return Bundle of all the configurations. NULL if error, such as a field not set.
     */
    public Bundle getConfigurations(){

        if (isInputValid()) {
            Bundle bundle = new Bundle();
            bundle.putString(Stats.KEY_GROUP, getGroup());

            String period = periodSpinner.getSelectedItem().toString().toLowerCase();
            bundle.putString(Stats.KEY_PERIOD, period.toLowerCase());

            if (period.equals(Stats.BETWEEN_DATES)) {

                //noinspection ConstantConditions
                String[] tokens = fromDateEditText.getText().toString().split("/");
                Calendar calendar = Calendar.getInstance();
                calendar.set(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[1]) - 1, Integer.parseInt(tokens[0]), calendar.getActualMinimum(Calendar.HOUR_OF_DAY), calendar.getActualMinimum(Calendar.MINUTE), calendar.getActualMinimum(Calendar.SECOND));
                calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
                Log.i(TAG, "start: " + calendar.getTime().toString());
                bundle.putDouble(Stats.KEY_START, calendar.getTimeInMillis() / THOUSAND);

                //noinspection ConstantConditions
                tokens = upToDateEditText.getText().toString().split("/");
                calendar.set(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[1]) - 1, Integer.parseInt(tokens[0]), calendar.getMaximum(Calendar.HOUR_OF_DAY), calendar.getActualMaximum(Calendar.MINUTE), calendar.getActualMaximum(Calendar.SECOND));
                calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
                Log.i(TAG, "end: " + calendar.getTime().toString());
                bundle.putDouble(Stats.KEY_END, calendar.getTimeInMillis() / THOUSAND);
            }

            bundle.putString(Stats.KEY_INTERVAL, intervalSpinner.getSelectedItem().toString().toLowerCase());
            bundle.putString(Stats.KEY_ACCUMULATION, accumulationSelection);

            return bundle;
        }
        return null;
    }

    public boolean isInputValid(){
        String period = periodSpinner.getSelectedItem().toString().toLowerCase();
        if (period.equals(Stats.BETWEEN_DATES)) {
            if (fromDateEditText.getText().toString().equals("")) {
                fromDateEditText.setError(getResources().getString(R.string.stats_create_dateError));
                return false;
            } else fromDateEditText.setError(null);
            if (upToDateEditText.getText().toString().equals("")) {
                upToDateEditText.setError(getResources().getString(R.string.stats_create_dateError));
                return false;
            } else fromDateEditText.setError(null);
        }
        return true;
    }

    //Activity > Fragment Communication

    public void notifySelectionMade(){
        lastAcceptedSelection = lastSelection;
    }

    //Support Functions

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
        StatsDatePickerFragment statsDatePickerFragment = new StatsDatePickerFragment();
        statsDatePickerFragment.setEditText(editText);
        statsDatePickerFragment.show(getFragmentManager(), "DATEPICKER");
    }
}
