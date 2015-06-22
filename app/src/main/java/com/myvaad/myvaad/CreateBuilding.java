package com.myvaad.myvaad;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class CreateBuilding extends Activity {
    TextView buildingNumber;
    EditText street,homeNumber,homeNumber2,paypal;
    AutoCompleteTextView autoCity;
    ParseDB db;
    Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //before the content is loading, an outer thread checks if the user is linked to a building
        //if connected, moves to the relvant screen
        //if not, loads the content of this screen
        setContentView(R.layout.create_building_screen);

        //Action bar background
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#007ca2"));
        getActionBar().setBackgroundDrawable(colorDrawable);

        autoCity=(AutoCompleteTextView)findViewById(R.id.citys_text_view);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.citys_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        autoCity.setAdapter(adapter);

        //To connect with parse - we need to provide 2 keys: appId & clientId
        String appId="QdwF666zm76ORQcn4KF6JNwDfsb6cj97QunbpT1s";
        String clientId="OiJI3KdONEN9jML6Mi6r6iQTpR8mIOBv3YgsUhdv";
        //Initialize with keys
        Parse.initialize(this, appId, clientId);
        db=ParseDB.getInstance(this);


        buildingNumber=(TextView)findViewById(R.id.CreateBuildingScreenBuildingCodeView);
        street=(EditText)findViewById(R.id.CreateBuildingScreenBuildingStreet);
        homeNumber=(EditText)findViewById(R.id.CreateBuildingScreenBuildingNumber);
        homeNumber2=(EditText)findViewById(R.id.CreateBuildingScreenBuildingEntrance);
        paypal=(EditText)findViewById(R.id.CreateBuildingScreenPayPalAccount);
        final ImageView paypalLogo=(ImageView)findViewById(R.id.small_paypal_logo);

        paypal.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    paypalLogo.setVisibility(View.GONE);
                }else{
                    paypalLogo.setVisibility(View.VISIBLE);
                }
            }
        });

        //Check if BuildingNumber exist, if exist give new random number..
        String r=randomBuildingNumber();
        while (db.isBuildingCodeExists(r)){
            r=randomBuildingNumber();
        }
        buildingNumber.setText(getString(R.string.buildingcodestring) + " " + r);
    }
    public String randomBuildingNumber(){
        int randomNumber=(int) Math.round(Math.random() * 999999);
        NumberFormat formatter = new DecimalFormat("000000");
        String buildingNumber = formatter.format(randomNumber);

        return buildingNumber;
    }

    public void createBuilding(View v){
        String streetText=street.getText().toString();
        String homeNumberText=homeNumber.getText().toString();
        String homeNumber2Text=homeNumber2.getText().toString();
        String paypalText=paypal.getText().toString();
        String citysText=autoCity.getText().toString();
        String buildingNumberText=buildingNumber.getText().toString();
        String buildingNumberDigits = buildingNumberText.replaceAll("[\\D]", "");


        if(db.isBuildingCodeExists(buildingNumberDigits)){
            String r=randomBuildingNumber();
            buildingNumber.setText(getString(R.string.buildingcodestring) + " " + r);
            toast=Toast.makeText(this,getString(R.string.building_code_exists_error),Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 150);
            toast.show();
        }else if(citysText.matches(getString(R.string.choose_city))){
            toast=Toast.makeText(this,getString(R.string.must_city),Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 150);
            toast.show();
        }else if(streetText.matches("")){
            toast=Toast.makeText(this,getString(R.string.must_street),Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 150);
            toast.show();
        }else if(homeNumberText.matches("")){
            toast=Toast.makeText(this,getString(R.string.must_home_number),Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 150);
            toast.show();
        }else{
            if(!homeNumber2Text.matches("")){
                homeNumber2Text=getString(R.string.home_number2text)+" "+homeNumber2Text;
            }
            String adrees=streetText+" "+homeNumberText+" "+homeNumber2Text+" "+citysText;
            if(paypalText.matches("")){
                db.signUpBuildingWithoutPaypal(buildingNumberDigits,adrees);
            }else{
                db.signUpBuilding(buildingNumberDigits,adrees,paypalText);
            }
            Intent i = new Intent(this, MainActivity.class);
            this.startActivity(i);
            this.finish();
            NotInBuilding.closeNotInBuildingActivity.finish();
        }
    }

}
