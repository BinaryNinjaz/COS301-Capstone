package za.org.samac.harvest;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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

import static za.org.samac.harvest.Analytics.THOUSAND;

@SuppressWarnings("FieldCanBeLocal")
public class Analytics_Creator extends Fragment{

    //Views
    private Spinner compareSpinner, periodSpinner, intervalSpinner;
    private Button selectorButton;
    private TextView compareSelectionTextView, accumulatorDescriptionTextView;
    private EditText fromDateEditText, upToDateEditText;
    private RadioGroup accumulatorRadioGroup;

    //Specification
    private final String TAG = "Analytics_Creator";

    //Others
    private String accumulationSelection = Analytics.NOTHING;

    private String selectedItemsText;

    private int lastSelection, lastAcceptanceSelection;


    public Analytics_Creator(){
        //Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics_creator, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //initialize views
        compareSpinner = view.findViewById(R.id.anal_create_compSpinner);
        periodSpinner = view.findViewById(R.id.anal_create_periodSpinner);
        intervalSpinner = view.findViewById(R.id.anal_create_interval_spinner);

        selectorButton = view.findViewById(R.id.anal_create_selectionButton);

        compareSelectionTextView = view.findViewById(R.id.anal_create_selectionDisplayTextView);
        compareSelectionTextView.setVisibility(View.VISIBLE);
        accumulatorDescriptionTextView = view.findViewById(R.id.anal_create_accumulator_description);

        fromDateEditText = view.findViewById(R.id.anal_create_from);
        upToDateEditText = view.findViewById(R.id.anal_create_upTo);

        accumulatorRadioGroup = view.findViewById(R.id.anal_create_accumulator_radioGroup);

        //Populate Spinners
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.anal_create_compsChoose, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        compareSpinner.setAdapter(arrayAdapter);

        arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.anal_create_periodChoose, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(arrayAdapter);

        arrayAdapter = ArrayAdapter.createFromResource(getContext(), R.array.anal_create_intervalChoose, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalSpinner.setAdapter(arrayAdapter);

        //Set selector button text
        setSelectorButtonTitle("farm");

        //Set default accumulator selection
        accumulatorRadioGroup.check(R.id.anal_create_accumulator_radio_none);
        accumulationSelection = Analytics.ACCUMULATION_NONE;

        //Set Accumulation hint
        updateAccumulatorHint();

        //Set listeners
        compareSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                @SuppressWarnings("ConstantConditions") String entity = Analytics.pluralizor(compareSpinner.getSelectedItem().toString(), false).toLowerCase();
                setSelectorButtonTitle(entity);
                updateAccumulatorTitles();
                updateAccumulatorHint();
                lastSelection = position;
                if (lastAcceptanceSelection == lastSelection){
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
                    case R.id.anal_create_accumulator_radio_none:
                        accumulationSelection = Analytics.ACCUMULATION_NONE;
                        break;
                    case R.id.anal_create_accumulator_radio_entity:
                        accumulationSelection = Analytics.ACCUMULATION_ENTITY;
                        break;
                    case R.id.anal_create_accumulator_radio_interval:
                        accumulationSelection = Analytics.ACCUMULATION_INTERVAL;
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
        selectorButton.setText(getString(R.string.anal_create_selectorButton, Analytics.pluralizor(entity, true)));
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
            case Analytics.ACCUMULATION_NONE:
                accumulatorDescriptionTextView.setText(getString(R.string.anal_create_accumulation_desc_none, Analytics.pluralizor(compareSpinner.getSelectedItem().toString(), false).toLowerCase()));
                break;
            case Analytics.ACCUMULATION_ENTITY:
                accumulatorDescriptionTextView.setText(getString(R.string.anal_create_accumulation_desc_entity, Analytics.pluralizor(compareSpinner.getSelectedItem().toString(), false).toLowerCase(), Analytics.pluralizor(compareSpinner.getSelectedItem().toString(), true).toLowerCase(), Analytics.timeConverter(intervalSpinner.getSelectedItem().toString(), false).toLowerCase()));
                break;
            case Analytics.ACCUMULATION_INTERVAL:
                accumulatorDescriptionTextView.setText(getString(R.string.anal_create_accumulation_desc_time, Analytics.timeConverter(intervalSpinner.getSelectedItem().toString(), false).toLowerCase(), Analytics.pluralizor(compareSpinner.getSelectedItem().toString(), true).toLowerCase()));
        }
    }

    private void updateAccumulatorTitles(){
        ((RadioButton) getView().findViewById(R.id.anal_create_accumulator_radio_entity)).setText(getString(R.string.anal_create_accumulation_entity, Analytics.pluralizor(compareSpinner.getSelectedItem().toString(), false)));
        ((RadioButton) getView().findViewById(R.id.anal_create_accumulator_radio_interval)).setText(getString(R.string.anal_create_accumulation_interval, Analytics.timeConverter(intervalSpinner.getSelectedItem().toString(), false)));
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
        return Analytics.pluralizor(compareSpinner.getSelectedItem().toString(), false).toLowerCase();
    }

    /**
     * Get a bundle consisting of all of the configurations that have been selected by the user, the keys match the strings in the Analytics class.
     * The bundle is constructed as follows (Key : Data Type : Description):
     *  KEY_GROUP : String : Matches a static from Analytics class.
     *  KEY_PERIOD : String : Matches a static from Analytics class
     *  KEY_START : double : The selected start date divided by 1 000
     *  KEY_END : double : The selected end date, divided by 1 000
     *  KEY_INTERVAL : String : Matches a static from Analytics class
     *  KEY_ACCUMULATION : String : Matches a static from Analytics class
     * @return Bundle of all the configurations. NULL if error, such as a field not set.
     */
    public Bundle getConfigurations(){
        Bundle bundle = new Bundle();
        bundle.putString(Analytics.KEY_GROUP, getGroup());

        String period = periodSpinner.getSelectedItem().toString().toLowerCase();
        bundle.putString(Analytics.KEY_PERIOD, period.toLowerCase());

        if (period.equals(Analytics.BETWEEN_DATES)){
            if (fromDateEditText.getText().toString().equals("")){
                fromDateEditText.setError(getResources().getString(R.string.anal_create_dateError));
                return null;
            }
            else fromDateEditText.setError("");
            if (upToDateEditText.getText().toString().equals("")){
                upToDateEditText.setError(getResources().getString(R.string.anal_create_dateError));
                return null;
            }
            else fromDateEditText.setError("");
            //noinspection ConstantConditions
            String[] tokens = fromDateEditText.getText().toString().split("/");
            Calendar calendar = Calendar.getInstance();
            calendar.set(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[0]));
            bundle.putDouble(Analytics.KEY_START,calendar.getTimeInMillis() / THOUSAND);

            //noinspection ConstantConditions
            tokens = upToDateEditText.getText().toString().split("/");
            calendar.set(Integer.parseInt(tokens[2]), Integer.parseInt(tokens[1]), Integer.parseInt(tokens[0]));
            bundle.putDouble(Analytics.KEY_END, calendar.getTimeInMillis() / THOUSAND);
        }

        bundle.putString(Analytics.KEY_INTERVAL, intervalSpinner.getSelectedItem().toString().toLowerCase());
        bundle.putString(Analytics.KEY_ACCUMULATION, accumulationSelection);

        return bundle;
    }

    //Activity > Fragment Communication

    public void notifySelectionMade(){
        lastAcceptanceSelection = lastSelection;
    }

    //Support Functions

    public static class AnalDatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener{
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
        AnalDatePickerFragment analDatePickerFragment = new AnalDatePickerFragment();
        analDatePickerFragment.setEditText(editText);
        analDatePickerFragment.show(getFragmentManager(), "DATEPICKER");
    }
}
