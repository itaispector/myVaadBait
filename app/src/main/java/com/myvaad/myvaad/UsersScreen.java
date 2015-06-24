package com.myvaad.myvaad;

import android.app.Dialog;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.Parse;

import adapters.UsersAdapter;

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
        //Initialize with keys
        Parse.initialize(getActivity());
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
        ok = (Button) dialogLayout.findViewById(R.id.usersAddDialogOkBtn);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                famName = (EditText) dialogLayout.findViewById(R.id.usersAddDialogFamName);
                String familyName = famName.getText().toString();
                apartNum = (EditText) dialogLayout.findViewById(R.id.usersAddDialogApartNum);
                String apartmentNumber = apartNum.getText().toString();

                if (familyName.matches("")||(apartmentNumber.matches(""))) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.empty_edittext_msg), Toast.LENGTH_SHORT).show();
                } else {
                    db.addUser(familyName, apartmentNumber);
                    usersDialog.dismiss();
                }
            }
        });
        cancel = (Button) dialogLayout.findViewById(R.id.usersAddDialogCancelBtn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersDialog.dismiss();
            }
        });


    }


}


