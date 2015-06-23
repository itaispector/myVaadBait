package com.myvaad.myvaad;

import com.parse.Parse;

import adapters.UsersAdapter;

import android.app.Dialog;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class UsersScreen extends Fragment {
    ListView usersList;
    UsersAdapter adapter;
    ParseDB db;
    ImageView addUserBtn;
    View dialogLayout;
    Dialog usersDialog;
    EditText famName, apartNum;
    Button ok, cancel;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String appId = "QdwF666zm76ORQcn4KF6JNwDfsb6cj97QunbpT1s";
        String clientId = "OiJI3KdONEN9jML6Mi6r6iQTpR8mIOBv3YgsUhdv";
        //Initialize with keys
        Parse.initialize(getActivity(), appId, clientId);
        db = ParseDB.getInstance(getActivity());
        View rootView = inflater.inflate(R.layout.users_screen, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        //calls the list view and its adapter
        usersList = (ListView) rootView.findViewById(R.id.UsersListView);
        addUserBtn = (ImageView) rootView.findViewById(R.id.add_user_btn);
        adapter = new UsersAdapter(getActivity(), db.getUsersList());
        usersList.setAdapter(adapter);

        //set listener to add user btn
        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserDialog();
            }
        });
        getActivity().setTitle(R.string.UsersScreenTitle);
        setHasOptionsMenu(true);
        return rootView;
    }

    public void addUserDialog() {
        dialogLayout = View.inflate(getActivity(), R.layout.add_user_dialog, null);
        usersDialog = new Dialog(getActivity());
        usersDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        usersDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        usersDialog.setContentView(dialogLayout);
        usersDialog.show();

        famName = (EditText) dialogLayout.findViewById(R.id.usersAddDialogFamName);
        final String familyName = famName.getText().toString();
        apartNum = (EditText) dialogLayout.findViewById(R.id.usersAddDialogApartNum);
        final String apartmentNumber = apartNum.getText().toString();
        ok=(Button)dialogLayout.findViewById(R.id.usersAddDialogOkBtn);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (familyName.matches("") || (apartmentNumber.matches(""))) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.empty_edittext_msg), Toast.LENGTH_SHORT).show();
                } else {
                    db.addUser(familyName, apartmentNumber);
                    usersDialog.dismiss();
                }
            }
        });
        cancel=(Button)dialogLayout.findViewById(R.id.usersAddDialogCancelBtn);
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                usersDialog.dismiss();
            }
        });



    }


}


