package com.myvaad.myvaad;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.Parse;


public class NotInBuilding extends Activity {
    ParseDB db;
    EditText bCodeNumber, apartmentNumber;
    private Toolbar mToolBar;
    View dialogLayout;
    Dialog joinBuildingDialog;
    Button ok, cancel, createBuildingBtn, joinBuildingBtn, logout;
    public static Activity closeNotInBuildingActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.not_in_building);

        db = ParseDB.getInstance(this);
        closeNotInBuildingActivity = this;

        createBuildingBtn = (Button) findViewById(R.id.CreateBuildingBtn);
        createBuildingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBuilding();
            }
        });

        joinBuildingBtn = (Button) findViewById(R.id.JoinBuildingBtn);
        joinBuildingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinBuilding();
            }
        });

        logout = (Button) findViewById(R.id.NotInBuildingLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.logOutUser(NotInBuilding.this);
            }
        });


    }

    public void joinBuilding() {
        mDialog(R.layout.join_building_dialog);

        ok = (Button) dialogLayout.findViewById(R.id.joinBuildingDialogOkBtn);

        bCodeNumber = (EditText) dialogLayout.findViewById(R.id.JoinBuildingEnterBuildingCode);
        bCodeNumber.addTextChangedListener(textWatcherListener);
        apartmentNumber = (EditText) dialogLayout.findViewById(R.id.add_apartment_number);
        apartmentNumber.addTextChangedListener(textWatcherListener);

        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogButtons(v);
            }
        });

    }

    //listener to watch if fields are empty or not, if empty add button is disabled
    private TextWatcher textWatcherListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            boolean check = false;
            check = (bCodeNumber.getText().toString().isEmpty() || apartmentNumber.getText().toString().isEmpty());
            ok.setEnabled(!check);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void dialogButtons(View v) {
        String buildingCodeText = bCodeNumber.getText().toString();
        String userApartmentNumber = apartmentNumber.getText().toString();

        if (buildingCodeText.length() < 6) {
            Toast toast = Toast.makeText(this, getString(R.string.b_code_less_nums), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else {
            if (db.isBuildingCodeExists(buildingCodeText)) {
                db.updateUserBuildingCode(buildingCodeText, userApartmentNumber);
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                this.finish();
            } else {
                Toast toast = Toast.makeText(this, R.string.problem_text, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }

    }

    public void mDialog(int layout) {
        dialogLayout = View.inflate(this, layout, null);
        joinBuildingDialog = new Dialog(this);
        joinBuildingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        joinBuildingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        joinBuildingDialog.setContentView(dialogLayout);
        joinBuildingDialog.show();
    }

    public void createBuilding() {
        Fragment mFragment = new CreateBuilding();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_framee, mFragment).addToBackStack("NotInBuilding").commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack();
    }
}

