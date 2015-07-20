package com.myvaad.myvaad;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapters.UsersAdapter;

public class UsersScreen extends Fragment {
    ListView usersListView;
    UsersAdapter adapter;
    ParseDB db;
    FloatingActionButton addUserBtn;
    View dialogLayout;
    Dialog usersDialog;
    EditText famName, apartNum, msgEditText;
    Button ok, cancel, msgOkBtn;
    List usersList = new ArrayList();
    /**
     * ProgressBarCircularIndeterminate bar;*
     */
    TextView noUsersText;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Initialize with keys
        Parse.initialize(getActivity());
        db = ParseDB.getInstance(getActivity());
        View rootView = inflater.inflate(R.layout.users_screen, container, false);
/**
 bar = (ProgressBarCircularIndeterminate) rootView.findViewById(R.id.progressBarCircularIndeterminate);
 bar.setVisibility(View.VISIBLE);**/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        noUsersText = (TextView) rootView.findViewById(R.id.no_users_text);
        //calls the list view and its adapter
        usersListView = (ListView) rootView.findViewById(R.id.UsersListView);

        //set data to list view
        findUsersForCurrentBuilding();

        //attaching button to list view, so it will disappear while scrolling down
        addUserBtn = (FloatingActionButton) rootView.findViewById(R.id.add_user_btn);
        addUserBtn.attachToListView(usersListView);
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

    public void findUsersForCurrentBuilding() {
        String buildingCode = db.getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        //Query Constraints-->all users from specific building
        query.whereContains("buildingCode", buildingCode);
        query.whereNotEqualTo("username", db.getcurrentUserName());
        query.orderByAscending("familyName");

        //finding all users for current user building
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> users, ParseException e) {
                if (e == null) {
                    if (users.isEmpty()) {
                        /**   bar.setVisibility(View.GONE);**/
                        noUsersText.setVisibility(View.VISIBLE);
                    } else {
                        noUsersText.setVisibility(View.GONE);
                        adapter = new UsersAdapter(getActivity(), users);
                        adapter.setSendBtnListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                for (int i = 0; i < usersListView.getChildCount(); i++) {
                                    if (view == usersListView.getChildAt(i).findViewById(R.id.usersRowSendBtn)) {
                                        String userObjectId = ((adapter.getItem(i)).getObjectId());
                                        sendMessageDialog(userObjectId);
                                    }
                                }
                            }
                        });
                        usersListView.setAdapter(adapter);
                        /**  bar.setVisibility(View.GONE);**/

                    }

                }

            }

        });
    }



    public void sendMessageDialog(final String userObjectId) {
        // inflate message layout
        myDialog(R.layout.send_message_dialog);

        // reference for edit text field and ok button
        msgEditText = (EditText) dialogLayout.findViewById(R.id.sendMessageDialogName);
        msgOkBtn = (Button) dialogLayout.findViewById(R.id.sendMessageDialogConfirmBtn);
        // add watcher listener for changes in edit text, enables/disables ok button
        msgEditText.addTextChangedListener(textWatcherListener);
        // msgOkBtn listener, when clicked, sends message
        msgOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = msgEditText.getText().toString();
                if ((msg.matches("\\s+"))) {
                    Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.empty_edittext_msg), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    usersDialog.dismiss();
                    sendNoti(userObjectId, msg);
                }
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
            check = (msgEditText.getText().toString().isEmpty());
            msgOkBtn.setEnabled(!check);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    public void sendNoti(String userObjectId, String msg){
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo("userObjectId", userObjectId);
        ParsePush androidPush = new ParsePush();
        androidPush.setMessage(msg);
        androidPush.setQuery(query);
        androidPush.sendInBackground();
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

                if (familyName.matches("") || (apartmentNumber.matches(""))) {
                    /**      bar.setVisibility(View.GONE);**/
                    Toast.makeText(getActivity(), getResources().getString(R.string.empty_edittext_msg), Toast.LENGTH_SHORT).show();
                } else {
                    /**    bar.setVisibility(View.VISIBLE);**/
                    addUser(familyName, apartmentNumber);
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

    private void addUser(final String familyName, final String apartmentNumber) {
        final String currentBuilding = db.getCurrentUserBuildingCode();
        final HashMap<String, Object> params = new HashMap<String, Object>();
        Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.nouser);
        byte[] data = db.convertImageToByteArray(bitmap);
        final ParseFile file = new ParseFile("user.png", data);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    //Toast.makeText(getActivity(), "" + e, Toast.LENGTH_LONG).show();
                } else {
                    //Toast.makeText(context, "hakol tov", Toast.LENGTH_LONG).show();
                    params.put("username", familyName + currentBuilding + apartmentNumber);
                    params.put("password", familyName + currentBuilding + apartmentNumber);
                    params.put("familyName", familyName);
                    params.put("apartmentNumber", apartmentNumber);
                    params.put("buildingCode", currentBuilding);
                    params.put("picture", file);
                    ParseCloud.callFunctionInBackground("saveNewUser", params, new FunctionCallback<String>() {
                        public void done(String result, ParseException e) {
                            if (e == null) {
                                refreshUsers();
                                /** bar.setVisibility(View.GONE);**/
                                //Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
                            } else {
                                //Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }

            }
        });
    }

    private void myDialog(int layout) {
        dialogLayout = View.inflate(getActivity(), layout, null);
        usersDialog = new Dialog(getActivity());
        usersDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        usersDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        usersDialog.setContentView(dialogLayout);
        usersDialog.show();
    }

    public void refreshUsers() {
        Fragment fragment1 = new UsersScreen();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment1).commit();
    }

    public void showLoader() {
        /** bar.setVisibility(View.VISIBLE);**/
    }

    private void mToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

}


