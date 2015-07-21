package com.myvaad.myvaad;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class CreateBuilding extends Fragment {
    TextView buildingNumber;
    EditText street, homeNumber, homeNumber2, paypal, numberOfHouses;
    AutoCompleteTextView autoCity;
    ParseDB db;
    Toast toast;
    View dialogLayout;
    Dialog housesDialog;
    NumberPicker np;
    int npValue;
    Button ok, cancel, buildBtn;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //before the content is loading, an outer thread checks if the user is linked to a building
        //if connected, moves to the relvant screen
        //if not, loads the content of this screen
        View rootView = inflater.inflate(R.layout.create_building_screen, container, false);

        autoCity = (AutoCompleteTextView) rootView.findViewById(R.id.citys_text_view);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.citys_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        autoCity.setAdapter(adapter);

        //To connect with parse - we need to provide 2 keys: appId & clientId
        String appId = "QdwF666zm76ORQcn4KF6JNwDfsb6cj97QunbpT1s";
        String clientId = "OiJI3KdONEN9jML6Mi6r6iQTpR8mIOBv3YgsUhdv";
        //Initialize with keys
        Parse.initialize(getActivity(), appId, clientId);
        db = ParseDB.getInstance(getActivity());


        buildingNumber = (TextView) rootView.findViewById(R.id.CreateBuildingScreenBuildingCodeView);
        street = (EditText) rootView.findViewById(R.id.CreateBuildingScreenBuildingStreet);
        homeNumber = (EditText) rootView.findViewById(R.id.CreateBuildingScreenBuildingNumber);
        homeNumber2 = (EditText) rootView.findViewById(R.id.CreateBuildingScreenBuildingEntrance);
        paypal = (EditText) rootView.findViewById(R.id.CreateBuildingScreenPayPalAccount);

        buildBtn = (Button) rootView.findViewById(R.id.CreateBuildingScreenCreateBtn);
        buildBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBuilding();
            }
        });

        numberOfHouses = (EditText) rootView.findViewById(R.id.CreateBuildingScreenBuildingHouseNumbers);
        numberOfHouses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                housesDialog();
            }
        });

        //Check if BuildingNumber exist, if exist give new random number..
        String r = randomBuildingNumber();
        while (db.isBuildingCodeExists(r)) {
            r = randomBuildingNumber();
        }
        buildingNumber.setText(getString(R.string.buildingcodestring) + " " + r);


        return rootView;
    }

    public String randomBuildingNumber() {
        int randomNumber = (int) Math.round(Math.random() * 999999);
        NumberFormat formatter = new DecimalFormat("000000");
        String buildingNumber = formatter.format(randomNumber);

        return buildingNumber;
    }

    public void createBuilding() {
        String streetText = street.getText().toString();
        String homeNumberText = homeNumber.getText().toString();
        String homeNumber2Text = homeNumber2.getText().toString();
        String paypalText = paypal.getText().toString();
        String citysText = autoCity.getText().toString();
        String buildingNumberText = buildingNumber.getText().toString();
        String buildingNumberDigits = buildingNumberText.replaceAll("[\\D]", "");
        String numOfHouse = numberOfHouses.getText().toString();


        if (db.isBuildingCodeExists(buildingNumberDigits)) {
            String r = randomBuildingNumber();
            buildingNumber.setText(getString(R.string.buildingcodestring) + " " + r);
            toast = Toast.makeText(getActivity(), getString(R.string.building_code_exists_error), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 150);
            toast.show();
        } else if (citysText.matches(getString(R.string.choose_city))) {
            toast = Toast.makeText(getActivity(), getString(R.string.must_city), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 150);
            toast.show();
        } else if (streetText.matches("")) {
            toast = Toast.makeText(getActivity(), getString(R.string.must_street), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 150);
            toast.show();
        } else if (homeNumberText.matches("")) {
            toast = Toast.makeText(getActivity(), getString(R.string.must_home_number), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 150);
            toast.show();
        } else if (numOfHouse.matches("") || numOfHouse.matches("0")) {
            toast = Toast.makeText(getActivity(), getString(R.string.must_num_houses), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 150);
            toast.show();
        } else {
            if (!homeNumber2Text.matches("")) {
                homeNumber2Text = getString(R.string.home_number2text) + " " + homeNumber2Text;
            }
            String adrees = streetText + " " + homeNumberText + " " + homeNumber2Text + " " + citysText;
            if (paypalText.matches("")) {
                db.signUpBuildingWithoutPaypal(buildingNumberDigits, adrees, numOfHouse);
            } else {
                db.signUpBuilding(buildingNumberDigits, adrees, paypalText, numOfHouse);
            }
            Intent i = new Intent(getActivity(), MainActivity.class);
            getActivity().startActivity(i);
            getActivity().finish();
            NotInBuilding.closeNotInBuildingActivity.finish();
        }
    }

    public void housesDialog() {
        dialogLayout = View.inflate(getActivity(), R.layout.add_num_houses_dialog, null);
        housesDialog = new Dialog(getActivity());
        housesDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        housesDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        housesDialog.setContentView(dialogLayout);
        housesDialog.show();

        np = (NumberPicker) dialogLayout.findViewById(R.id.housesDialogNumberPicker);
        //set max value for np
        String[] numbers = new String[51 / 1];
        // set numbers of picker
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = Integer.toString(i * 1 + 0);
        }
        np.setDisplayedValues(numbers);
        np.setMaxValue(numbers.length - 1);
        np.setMinValue(0);
        //disable picking loop
        np.setWrapSelectorWheel(false);
        //disable keyboard pop up
        np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        //number picker listener
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //update number picker value
                npValue = newVal * 1 + 0;
            }
        });

        ok = (Button) dialogLayout.findViewById(R.id.housesDialogConfirmBtn);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (npValue == 0) {
                    //do nothing
                } else {
                    numberOfHouses.setText(npValue + "");
                    housesDialog.dismiss();
                }
            }
        });

        cancel = (Button) dialogLayout.findViewById(R.id.housesDialogCancelBtn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                housesDialog.dismiss();
            }
        });
    }

}
