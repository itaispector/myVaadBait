package com.myvaad.myvaad;

import android.app.Dialog;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
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
import com.rey.material.widget.ProgressView;

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
    Button ok, msgOkBtn;
    ProgressView loader;
    TextView noUsersText;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Initialize with keys
        Parse.initialize(getActivity());
        db = ParseDB.getInstance(getActivity());
        View rootView = inflater.inflate(R.layout.users_screen, container, false);

        // loader reference
        loader = (ProgressView) rootView.findViewById(R.id.progress_loader);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        // no users text reference
        noUsersText = (TextView) rootView.findViewById(R.id.no_users_text);
        //calls the list view and its adapter
        usersListView = (ListView) rootView.findViewById(R.id.UsersListView);
        // set data to list view
        findUsersForCurrentBuilding();
        // floating butting reference
        addUserBtn = (FloatingActionButton) rootView.findViewById(R.id.add_user_btn);
        //attaching button to list view, so it will disappear while scrolling down
        addUserBtn.attachToListView(usersListView);
        //set listener to add user btn
        addUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserDialog();
            }
        });
        // set title name
        getActivity().setTitle(R.string.UsersScreenTitle);
        setHasOptionsMenu(true);
        return rootView;
    }

    // load user's list view
    public void findUsersForCurrentBuilding() {
        // toggle loader (if gone - visible and opposite)
        mLoader();
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
                        mLoader();
                        noUsersText.setVisibility(View.VISIBLE);
                    } else {
                        mLoader();
                        noUsersText.setVisibility(View.GONE);
                        adapter = new UsersAdapter(getActivity(), users);
                        adapter.setSendBtnListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int idx = usersListView.getPositionForView(view);
                                String userObjectId = ((adapter.getItem(idx)).getObjectId());
                                sendMessageDialog(userObjectId);

                            }
                        });
                        adapter.setDeleteBtnListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int idx = usersListView.getPositionForView(view);
                                String userObjectId = ((adapter.getItem(idx)).getObjectId());
                                String familyName = ((adapter.getItem(idx)).getString("familyName"));
                                deleteDialog(userObjectId, familyName);
                            }
                        });
                        usersListView.setAdapter(adapter);
                    }
                }
            }

        });

    }

    public void deleteDialog(final String userObjectId, final String familyName) {
        final MaterialDialog mDialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.custom_layout_content, false)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.yes)
                .negativeColorRes(R.color.colorPrimary)
                .negativeText(R.string.no)
                .buttonsGravity(GravityEnum.END)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        HashMap<String, Object> params = new HashMap<String, Object>();
                        params.put("userObjectId", userObjectId);
                        mLoader();
                        ParseCloud.callFunctionInBackground("deleteUser", params, new FunctionCallback<Object>() {
                            public void done(Object result, ParseException e) {
                                if (e == null) {
                                    mLoader();
                                    findUsersForCurrentBuilding();
                                } else {
                                }
                            }
                        });
                    }
                })
                .show();
        View cView = mDialog.getCustomView();
        TextView content = (TextView) cView.findViewById(R.id.text);
        String delete = getActivity().getString(R.string.delete_user);
        String areYouSure = getActivity().getString(R.string.are_you_sure);
        String family = getActivity().getString(R.string.family);
        content.setText(delete + " " + family + " " + familyName + "\n" + areYouSure);

    }

    public void sendMessageDialog(final String userObjectId) {
        // inflate message layout
        myDialog(R.layout.send_message_dialog);

        // reference for edit text field and ok button
        msgEditText = (EditText) dialogLayout.findViewById(R.id.input);
        msgOkBtn = (Button) dialogLayout.findViewById(R.id.button);
        // add watcher listener for changes in edit text, enables/disables ok button
        msgEditText.addTextChangedListener(textWatcherListener);
        // msgOkBtn listener, when clicked, sends message
        msgOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = msgEditText.getText().toString();
                if ((msg.matches("\\s+"))) {
                    mToast(getString(R.string.empty_edittext_msg), true);
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

    public void sendNoti(String userObjectId, String msg) {
        ParseQuery query = ParseInstallation.getQuery();
        query.whereEqualTo("userObjectId", userObjectId);
        ParsePush androidPush = new ParsePush();
        androidPush.setMessage(msg);
        androidPush.setQuery(query);
        androidPush.sendInBackground();
    }

    public void addUserDialog() {
        myDialog(R.layout.add_user_dialog);
        famName = (EditText) dialogLayout.findViewById(R.id.usersAddDialogFamName);
        famName.addTextChangedListener(textWatcherListener2);
        apartNum = (EditText) dialogLayout.findViewById(R.id.usersAddDialogApartNum);
        apartNum.addTextChangedListener(textWatcherListener2);
        ok = (Button) dialogLayout.findViewById(R.id.usersAddDialogOkBtn);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String familyName = famName.getText().toString();
                String apartmentNumber = apartNum.getText().toString();
                if (familyName.matches("\\s+") || (apartmentNumber.matches("\\s+"))) {
                    mToast(getString(R.string.empty_edittext_msg), true);
                } else {
                    addUser(familyName, apartmentNumber);
                    usersDialog.dismiss();
                }
            }
        });
    }

    //listener to watch if fields are empty or not, if empty add button is disabled
    private TextWatcher textWatcherListener2 = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            boolean check = false;
            check = (famName.getText().toString().isEmpty() || apartNum.getText().toString().isEmpty());
            ok.setEnabled(!check);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void addUser(final String familyName, final String apartmentNumber) {
        final String currentBuilding = db.getCurrentUserBuildingCode();
        final HashMap<String, Object> params = new HashMap<String, Object>();
        Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.nouser);
        byte[] data = db.convertImageToByteArray(bitmap);
        final ParseFile file = new ParseFile("user.png", data);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    params.put("username", familyName + currentBuilding + apartmentNumber);
                    params.put("password", familyName + currentBuilding + apartmentNumber);
                    params.put("familyName", familyName);
                    params.put("apartmentNumber", apartmentNumber);
                    params.put("buildingCode", currentBuilding);
                    params.put("picture", file);
                    ParseCloud.callFunctionInBackground("saveNewUser", params, new FunctionCallback<String>() {
                        public void done(String result, ParseException e) {
                            if (e == null) {
                                findUsersForCurrentBuilding();
                            } else {
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

    // toggle visibility of loader
    public void mLoader() {
        loader.setVisibility(loader.isShown() ? View.GONE : View.VISIBLE);
    }

    // show toast on bottom of screen
    private void mToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    // show toast on center of screen (overloaded)
    private void mToast(String msg, boolean middle) {
        Toast toast = Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }


}


