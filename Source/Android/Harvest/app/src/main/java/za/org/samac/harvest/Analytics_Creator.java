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
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

public class Analytics_Creator extends Fragment{

    Spinner compareSpinner, periodSpinner, intervalSpinner;
    Button selectorButton;
    TextView compareSelectionTextView, accumulatorDescriptionTextView;
    EditText fromDateEditText, upToDateEditText;
    RadioGroup accumulatorRadioGroup;

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
        setSelectorButtonTitle();

        //Set selected items text
        setSelectedItemsText("");

        //Set default accumulator selection
        accumulatorRadioGroup.check(R.id.anal_create_accumulator_radio_none);

        //Set accumulator hint
        setAccumulatorHint();

        //Set listeners
        compareSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSelectorButtonTitle();
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

        fromDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        upToDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        accumulatorRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setAccumulatorHint();
            }
        });
    }

    public void setSelectorButtonTitle(){
        selectorButton.setText(getString(R.string.anal_create_selectorButton, compareSpinner.getSelectedItem().toString()));
    }

    public void setSelectedItemsText(String text){
        compareSelectionTextView.setText(text);
    }

    public void setAccumulatorHint(){
        switch (accumulatorRadioGroup.getCheckedRadioButtonId()){
            case R.id.anal_create_accumulator_radio_none:
                accumulatorDescriptionTextView.setText(getString(R.string.anal_create_accumulation_desc_none));
                break;
            case R.id.anal_create_accumulator_radio_entity:
                accumulatorDescriptionTextView.setText(getString(R.string.anal_create_accumulation_desc_entity));
                break;
            case R.id.anal_create_accumulator_radio_interval:
                accumulatorDescriptionTextView.setText(getString(R.string.anal_create_accumulation_desc_interval));
        }
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
}
