package com.myvaad.myvaad;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.rey.material.widget.ProgressView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import adapters.FailuresAdapter;

public class FailuresScreen extends Fragment {

    ListView failuresList;
    FailuresAdapter adapter;
    TextView content, noFailuresTextView;
    EditText titleEdit, contentEdit;
    View dialogLayout;
    Dialog failuresDialog;
    Button dialogFailureOkBtn, dialogFailureCancelBtn, moveToPaymentsBtn;
    FloatingActionButton addFailureBtn;
    ProgressView bar;
    int position;
    ParseDB db;
    String title, failureContent = "", myList = "", failureObjectId;
    List outputFailuresList = new ArrayList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = ParseDB.getInstance(getActivity());
        View rootView = inflater.inflate(R.layout.failures_screen, container, false);
        getActivity().setTitle(R.string.FailuresScreenTitle);
        noFailuresTextView = (TextView) rootView.findViewById(R.id.no_failures_text);
        bar = (ProgressView) rootView.findViewById(R.id.progress_loader);
        bar.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        // calls the list view and its adapter
        failuresList = (ListView) rootView.findViewById(R.id.FailuresListView);
        // load data of list view from query
        String CurrentUserBuildingCode = db.getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("failures");
        //Query Constraints-->all the failures for current user building
        query.whereContains("buildingCode", CurrentUserBuildingCode);
        query.whereEqualTo("state", true);
        query.orderByDescending("updatedAt");
        // finding all the failures for current user building
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> failures, ParseException e) {
                if (e == null) {
                    outputFailuresList.clear();
                    SimpleDateFormat postFormatter = new SimpleDateFormat("EEEE  dd " + "ב" + "MMMM  HH:mm", new Locale("he"));
                    for (ParseObject failuresRow : failures) {
                        List rowFailureList = new ArrayList();
                        //get specific data from each row
                        String title = failuresRow.getString("title");
                        String content = failuresRow.getString("content");
                        String bidValue = failuresRow.getString("bid");
                        String bidPerformedBy = failuresRow.getString("performedBy");
                        String status = failuresRow.getString("status");
                        Date updatedAt = failuresRow.getCreatedAt();
                        String noticeTime = postFormatter.format(updatedAt);
                        String ObjectId = failuresRow.getObjectId();
                        String familyName = failuresRow.getString("userFamilyName");
                        ParseFile userPicture = failuresRow.getParseFile("userPic");
                        Bitmap userPic = db.parseFileToBitmap(userPicture);
                        List<String> approvedByList = new ArrayList();
                        //List of all the users that approved the bid for the repair the malfunction
                        if (failuresRow.getList("approvedBy") != null) {
                            approvedByList = failuresRow.getList("approvedBy");
                        } else approvedByList.add("no one approve");
                        rowFailureList.add(ObjectId);
                        rowFailureList.add(title);
                        rowFailureList.add(content);
                        rowFailureList.add(bidValue);
                        rowFailureList.add(bidPerformedBy);
                        rowFailureList.add(status);
                        rowFailureList.add(noticeTime);
                        rowFailureList.add(familyName);
                        rowFailureList.add(userPic);
                        rowFailureList.add(approvedByList);
                        outputFailuresList.add(rowFailureList);
                        adapter = new FailuresAdapter(getActivity(), outputFailuresList, db.isCurrentUserAdmin(), db.getcurrentUserFamilyName());

                        adapter.setAddBtnListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // get position of click
                                position = failuresList.getPositionForView(view);
                                addBidPrice();
                            }
                        });

                        adapter.setEditBtnListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // get position of click
                                position = failuresList.getPositionForView(view);
                                editBidPrice();
                            }
                        });

                        adapter.setDeleteBtnListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // get position of click
                                position = failuresList.getPositionForView(view);
                                deleteFailure();
                            }
                        });

                        adapter.setApprovalBtnListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // get position of click
                                position = failuresList.getPositionForView(view);
                                showApprovals();
                            }
                        });

                        adapter.setApproveOkBtnListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // get position of click
                                position = failuresList.getPositionForView(view);
                                db.updateFailureApprovedByCurrentUser(adapter.getObjectId(position));
                                refreshFailures();
                            }
                        });

                        adapter.setApproveCancelBtnListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // get position of click
                                position = failuresList.getPositionForView(view);
                                db.removeFailureApprovedBy(db.getcurrentUserFamilyName(), adapter.getObjectId(position));
                                refreshFailures();
                            }
                        });

                        failuresList.setAdapter(adapter);
                        bar.setVisibility(View.GONE);
                    }
                    if (outputFailuresList.isEmpty()) {
                        bar.setVisibility(View.GONE);
                        noFailuresTextView.setVisibility(View.VISIBLE);
                    } else {
                        noFailuresTextView.setVisibility(View.GONE);
                    }
                } else {
                    Log.i("***Parse Exception****", e.getLocalizedMessage());
                }
            }
        });

        // adds a listener to item in the listview
        failuresList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int idx, long id) {
                // toggles between open & closed box
                adapter.setState(adapter.getState(idx) ? false : true, idx);
            }
        });

        // add failure button pointer and listener
        addFailureBtn = (FloatingActionButton) rootView.findViewById(R.id.add_f_btnn);
        addFailureBtn.attachToListView(failuresList);
        addFailureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFailure();
            }
        });
        return rootView;
    }

    // opens add failure dialog
    public void addFailure() {
        mDialog(R.layout.failures_dialog_layout);
        titleEdit = (EditText) dialogLayout.findViewById(R.id.failuresDialogFailureName);
        titleEdit.addTextChangedListener(textWatcherListener);
        contentEdit = (EditText) dialogLayout.findViewById(R.id.failuresDialogFailureData);
        contentEdit.addTextChangedListener(textWatcherListener);
        dialogFailureOkBtn = (Button) dialogLayout.findViewById(R.id.failuresDialogFailureOkBtn);

        dialogFailureOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogButtonsResult(v);
            }
        });

    }

    // listener to watch if fields are empty or not, if empty add button is disabled
    private TextWatcher textWatcherListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            boolean check = false;
            check = (titleEdit.getText().toString().isEmpty() || contentEdit.getText().toString().isEmpty());
            dialogFailureOkBtn.setEnabled(!check);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    // handles button clicks
    public void DialogButtonsResult(View v) {

        title = titleEdit.getText().toString();
        failureContent = contentEdit.getText().toString();

        if (title.matches("\\s+") || failureContent.matches("\\s+")) {
            mToast(getString(R.string.empty_notice));
        } else {
            db.updateNewfailure(title, failureContent);
            refreshFailures();
            failuresDialog.dismiss();
        }

    }

    // add bid price dialog
    public void addBidPrice() {
        mDialog(R.layout.failures_add_dialog_layout);

        dialogFailureOkBtn = (Button) failuresDialog.findViewById(R.id.failuresAddDialogFailureOkBtn);

        titleEdit = (EditText) failuresDialog.findViewById(R.id.failuresAddDialogBusinessName);
        titleEdit.addTextChangedListener(textWatcherListener);
        contentEdit = (EditText) failuresDialog.findViewById(R.id.failuresAddDialogPrice);
        contentEdit.addTextChangedListener(textWatcherListener);
        dialogFailureOkBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                title = titleEdit.getText().toString();
                failureContent = contentEdit.getText().toString();
                if (title.matches("\\s+") || failureContent.matches("\\s+")) {
                    mToast(getString(R.string.empty_notice));
                } else {
                    new UpdateFailureBid().execute();
                    //failuresDialog.dismiss();
                    //db.updateFailureBid(failureContent, title, adapter.getObjectId(position), true); /** need to improve this!! */
                    //refreshFailures();
                }
            }
        });
    }

    class UpdateFailureBid extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            db.updateFailureBid(failureContent, title, adapter.getObjectId(position), true);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            refreshFailures();
        }
    }

    // edit bid price dialog
    public void editBidPrice() {
        mDialog(R.layout.failures_edit_dialog_layout);
        dialogFailureOkBtn = (Button) failuresDialog.findViewById(R.id.failuresAddDialogFailureOkBtn);
        dialogFailureCancelBtn = (Button) failuresDialog.findViewById(R.id.failuresAddDialogFailureCancelBtn);

        titleEdit = (EditText) failuresDialog.findViewById(R.id.failuresAddDialogBusinessName);
        titleEdit.addTextChangedListener(textWatcherListener);
        contentEdit = (EditText) failuresDialog.findViewById(R.id.failuresAddDialogPrice);
        contentEdit.addTextChangedListener(textWatcherListener);
        titleEdit.setText("" + ((List) adapter.getItem(position)).get(4));
        contentEdit.setText("" + ((List) adapter.getItem(position)).get(3));


        dialogFailureCancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                failuresDialog.dismiss();
            }
        });

        dialogFailureOkBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                title = titleEdit.getText().toString();
                failureContent = contentEdit.getText().toString();
                if (title.matches("\\s+") || failureContent.matches("\\s+")) {
                    mToast(getString(R.string.empty_notice));
                } else {
                    failuresDialog.dismiss();
                    db.updateFailureBid(failureContent, title, adapter.getObjectId(position), false);
                    refreshFailures();
                }
            }
        });
    }

    // approve families dialog
    public void showApprovals() {
        mDialog(R.layout.failures_approval_dialog_layout);

        content = (TextView) failuresDialog.findViewById(R.id.failuresApprovalDialogList);
        String family = getString(R.string.family);
        for (int i = 0; i < adapter.getApprovers(position).size(); i++) {
            myList += (" - " + family + " " + adapter.getApprovers(position).get(i) + "\n");
        }
        content.setText(myList);
        dialogFailureCancelBtn = (Button) failuresDialog.findViewById(R.id.failuresApprovalDialogFailureCancelBtn);
        moveToPaymentsBtn = (Button) failuresDialog.findViewById(R.id.failuresApprovalDialogFailureMoveToPaymentsBtn);
        //check who is the user and set view
        if (!db.isCurrentUserAdmin()) {
            moveToPaymentsBtn.setVisibility(View.GONE);
            // set margin of button to 0
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) dialogFailureCancelBtn.getLayoutParams();
            layoutParams.setMargins(0,0,0,0);
            dialogFailureCancelBtn.setLayoutParams(layoutParams);
            // set width of button to match parent
            ViewGroup.LayoutParams lp = dialogFailureCancelBtn.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            dialogFailureCancelBtn.setLayoutParams(lp);
        }


        //adds a listener to close button
        dialogFailureCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                failuresDialog.dismiss();
            }
        });
        //adds a listener to close & move to payments button
        moveToPaymentsBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //close & move to payments db method
                failureObjectId = adapter.getObjectId(position);
                db.setFailureInactive(failureObjectId);
                db.createPaymentFromFailure(failureObjectId);
                failuresDialog.dismiss();
                refreshFailures();
            }
        });
        //add a listener for dismiss (close) this dialog
        failuresDialog.setOnDismissListener(new OnDismissListener() {

            public void onDismiss(DialogInterface dialog) {
                myList = "";
            }
        });

    }

    // delete failure dialog
    public void deleteFailure() {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.custom_layout_content, false)
                .buttonsGravity(GravityEnum.END)
                .positiveColorRes(R.color.colorPrimary)
                .positiveText(R.string.yes)
                .negativeColorRes(R.color.colorPrimary)
                .negativeText(R.string.no)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        db.deleteFailure(adapter.getObjectId(position));
                        refreshFailures();
                    }
                })
                .show();
        View cView = dialog.getCustomView();
        TextView content = (TextView) cView.findViewById(R.id.text);
        content.setText(getString(R.string.deleteFailure));
    }

    // custom dialog
    public void mDialog(int layout) {
        dialogLayout = View.inflate(getActivity(), layout, null);
        failuresDialog = new Dialog(getActivity());
        failuresDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        failuresDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        failuresDialog.setContentView(dialogLayout);
        failuresDialog.show();
    }

    // reload page
    public void refreshFailures() {
        Fragment fragment1 = new FailuresScreen();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, fragment1).commit();
    }

    // toast in middle of screen
    public void mToast(String s) {
        Toast toast = Toast.makeText(getActivity(), s, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


}


