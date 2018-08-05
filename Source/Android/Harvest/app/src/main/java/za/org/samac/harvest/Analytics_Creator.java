package za.org.samac.harvest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

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
    private String entitySelection = Analytics.NOTHING;

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

        //Set selected items text
        setSelectedItemsText("");

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
                setEntityAccumulationTitle(entity);
                updateAccumulatorHint();
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
        compareSelectionTextView.setText(text);
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

    private void setEntityAccumulationTitle(String entity){
        ((RadioButton) getView().findViewById(R.id.anal_create_accumulator_radio_entity)).setText(getString(R.string.anal_create_accumulation_entity, Analytics.pluralizor(entity, false)));
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
        return compareSpinner.getSelectedItem().toString();
    }

    /**
     * Get a bundle consisting of all of the configurations that have been selected by the user, the keys match the strings in the Analytics class.
     * The bundle is constructed as follows (Key : Data Type : Description):
     *  KEY_GROUP : String : Matches a static from Analytics class
     *  KEY_PERIOD : String : Matches a static from Analytics class
     *  KEY_START : String : The selected start date, only occurs if the period is set to BETWEEN_DATES, its format is DD/MM/YYYY
     *  KEY_END : String : The selected end date, only occurs if the period is set to BETWEEN_DATES, its format is DD/MM/YYYY
     *  KEY_INTERVAL : String : Matches a static from Analytics class
     *  KEY_ACCUMULATION : String : Matches a static from Analytics class
     * @return Bundle of all the configurations.
     */
    public Bundle getConfigurations(){
        Bundle bundle = new Bundle();
        bundle.putString(Analytics.KEY_GROUP, compareSpinner.getSelectedItem().toString().toLowerCase());

        String period = periodSpinner.getSelectedItem().toString().toLowerCase();
        bundle.putString(Analytics.KEY_PERIOD, period);

        if (period.equals(Analytics.BETWEEN_DATES)){
            bundle.putString(Analytics.KEY_START, fromDateEditText.getText().toString());
            bundle.putString(Analytics.KEY_END, upToDateEditText.getText().toString());
        }

        bundle.putString(Analytics.KEY_INTERVAL, intervalSpinner.getSelectedItem().toString().toLowerCase());
        bundle.putString(Analytics.KEY_ACCUMULATION, accumulationSelection);

        return bundle;
    }

    //Support Functions

    private void showDateSpinner(View v){
        EditText editText = (EditText) v;
        String text = editText.getText().toString();

        if (text)
    }
}
