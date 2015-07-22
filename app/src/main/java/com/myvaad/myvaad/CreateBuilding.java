package com.myvaad.myvaad;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class CreateBuilding extends Fragment {
    TextView buildingNumber;
    EditText street, homeNumber, homeNumber2, paypal, numberOfHouses, adminApartmentNumber;
    AutoCompleteTextView autoCity;
    ParseDB db;
    Toast toast;
    View dialogLayout;
    Dialog housesDialog;
    NumberPicker np;
    String r;
    int npValue;
    boolean ret;
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

        //Initialize with keys
        Parse.initialize(getActivity());
        db = ParseDB.getInstance(getActivity());

        buildingNumber = (TextView) rootView.findViewById(R.id.CreateBuildingScreenBuildingCodeView);
        street = (EditText) rootView.findViewById(R.id.CreateBuildingScreenBuildingStreet);
        homeNumber = (EditText) rootView.findViewById(R.id.CreateBuildingScreenBuildingNumber);
        homeNumber2 = (EditText) rootView.findViewById(R.id.CreateBuildingScreenBuildingEntrance);
        paypal = (EditText) rootView.findViewById(R.id.CreateBuildingScreenPayPalAccount);
        adminApartmentNumber = (EditText) rootView.findViewById(R.id.CreateBuildingScreenAdminApartmentNumber);

        buildBtn = (Button) rootView.findViewById(R.id.CreateBuildingScreenCreateBtn);
        buildBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBuilding();
            }
        });

        numberOfHouses = (EditText) rootView.findViewById(R.id.CreateBuildingScreenBuildingHouseNumbers);

        //Check if BuildingNumber exist, if exist give new random number..
        r = randomBuildingNumber();
        buildingNumber.setText(getString(R.string.buildingcodestring) + " " + r);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("buildings");
        try {
            query.whereEqualTo("buildingCode", r);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (!list.isEmpty()) {
                        r = randomBuildingNumber();
                        Toast.makeText(getActivity(), r, Toast.LENGTH_LONG).show();
                        buildingNumber.setText(getString(R.string.buildingcodestring) + " " + r);
                    }
                }
            });
        } catch (Exception e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }

        return rootView;
    }

    protected boolean isBuildingCodeExists(String buildingCode){
        ret=false;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("buildings");
        try {
            query.whereEqualTo("buildingCode", buildingCode);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (!list.isEmpty()) {
                        ret=true;
                        r = randomBuildingNumber();
                        buildingNumber.setText(getString(R.string.buildingcodestring) + " " + r);
                        toast = Toast.makeText(getActivity(), getString(R.string.building_code_exists_error), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 150);
                        toast.show();
                    }
                }
            });
        } catch (Exception e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
        }
        return ret;
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
        String adminApartmentNum = adminApartmentNumber.getText().toString();


        if (isBuildingCodeExists(r)) {

        } else if (citysText.matches("")) {
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
        } else if(adminApartmentNum.matches("")){
            toast = Toast.makeText(getActivity(), getString(R.string.must_num_appartment), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 150);
            toast.show();
        } else {
            if (!homeNumber2Text.matches("")) {
                homeNumber2Text = getString(R.string.home_number2text) + " " + homeNumber2Text;
            }
            String adrees = streetText + " " + homeNumberText + " " + homeNumber2Text + " " + citysText;
            if (paypalText.matches("")) {
                db.signUpBuildingWithoutPaypal(buildingNumberDigits, adrees, numOfHouse,getActivity());
            } else {
                db.signUpBuilding(buildingNumberDigits, adrees, paypalText, numOfHouse,getActivity());
            }

            ParseUser currentUser = db.getcurrentUser();
            currentUser.put("apartmentNumber", adminApartmentNum);
            currentUser.saveInBackground();

            Intent i = new Intent(getActivity(), MainActivity.class);
            getActivity().startActivity(i);
            getActivity().finish();
            NotInBuilding.closeNotInBuildingActivity.finish();
        }
    }
}
