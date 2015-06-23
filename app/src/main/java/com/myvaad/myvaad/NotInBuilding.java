package com.myvaad.myvaad;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.Parse;


public class NotInBuilding extends MainLoginScreen {
    ParseDB db;
    EditText bCodeNumber;
    View dialogLayout;
    Dialog joinBuildingDialog;
    public static Activity closeNotInBuildingActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.not_in_building);
        String appId="QdwF666zm76ORQcn4KF6JNwDfsb6cj97QunbpT1s";
        String clientId="OiJI3KdONEN9jML6Mi6r6iQTpR8mIOBv3YgsUhdv";
        //Initialize with keys
        Parse.initialize(this, appId, clientId);
        db=ParseDB.getInstance(this);
        closeNotInBuildingActivity=this;

    }
    public void joinBuilding(View v){
            dialogLayout = View.inflate(this, R.layout.join_building_dialog, null);
            joinBuildingDialog = new Dialog(this);
            joinBuildingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            joinBuildingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            joinBuildingDialog.setContentView(dialogLayout);
            joinBuildingDialog.show();

            ok = (Button)dialogLayout.findViewById(R.id.joinBuildingDialogOkBtn);
            cancel = (Button)dialogLayout.findViewById(R.id.joinBuildingDialogCancelBtn);
            bCodeNumber=(EditText) dialogLayout.findViewById(R.id.JoinBuildingEnterBuildingCode);

            ok.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialogButtons(v);

                }
            });

            cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dialogButtons(v);

                }
            });
    }

    public void dialogButtons(View v) {
        String buildingCodeText=bCodeNumber.getText().toString();
        if (v.getId() == R.id.joinBuildingDialogCancelBtn){
            //Exit from keyboard when the dialog dismissed
            InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(bCodeNumber.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            joinBuildingDialog.dismiss();
        } else {
            if(buildingCodeText.length()<6){
                Toast toast=Toast.makeText(getApplicationContext(),getString(R.string.b_code_less_nums),Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                //Exit from keyboard when the dialog dismissed
                InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(bCodeNumber.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
            }else{
                if(db.isBuildingCodeExists(buildingCodeText)){
                    db.updateUserBuildingCode(buildingCodeText);
                    Toast toast=Toast.makeText(getApplicationContext(),buildingCodeText,Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    //Exit from keyboard when the dialog dismissed
                    InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(bCodeNumber.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                    this.finish();
                }else{
                    Toast toast=Toast.makeText(getApplicationContext(),R.string.problem_text,Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    //Exit from keyboard when the dialog dismissed
                    InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(bCodeNumber.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
    }
    public void createBuilding(View v){
        Fragment mFragment = new CreateBuilding();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().show(mFragment);
    }
}

