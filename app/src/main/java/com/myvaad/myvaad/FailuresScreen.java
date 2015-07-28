package com.myvaad.myvaad;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
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
    InputMethodManager imm;
    boolean firstAdd = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = ParseDB.getInstance(getActivity());
        View rootView = inflater.inflate(R.layout.failures_screen, container, false);
        getActivity().setTitle(R.string.FailuresScreenTitle);
        noFailuresTextView = (TextView) rootView.findViewById(R.id.no_failures_text);
        bar = (ProgressView) rootView.findViewById(R.id.progress_loader);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        failuresList = (ListView) rootView.findViewById(R.id.FailuresListView);
        addFailureBtn = (FloatingActionButton) rootView.findViewById(R.id.add_f_btnn);
        bar.setVisibility(View.VISIBLE);
        loadListViewData();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        // adds a listener to item in the listview
        failuresList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int idx, long id) {
                // toggles between open & closed box
                adapter.setState(adapter.getState(idx) ? false : true, idx);
            }
        });

        // add failure button listener
        addFailureBtn.attachToListView(failuresList);
        addFailureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFailure();
            }
        });
        return rootView;
    }

    // loads list view data
    private void loadListViewData() {
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
                               }

        );
    }

    // opens add failure dialog
    public void addFailure() {
        mDialog(R.layout.failures_dialog_layout);
        titleEdit = (EditText) dialogLayout.findViewById(R.id.failuresDialogFailureName);
        titleEdit.addTextChangedListener(textWatcherListener);
        titleEdit.requestFocus();
        openKeyboard();
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
            new UpdateNewFailureTask().execute();
            //db.updateNewfailure(title, failureContent);
            refreshFailures();
            failuresDialog.dismiss();
        }

    }

    class UpdateNewFailureTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return db.updateNewfailure(title, failureContent);
        }

        @Override
        protected void onPostExecute(Boolean isUpdated) {
            if (isUpdated) {
                bar.setVisibility(View.GONE);
                loadListViewData();
            }
        }
    }

    // add bid price dialog
    public void addBidPrice() {
        mDialog(R.layout.failures_add_dialog_layout);

        dialogFailureOkBtn = (Button) failuresDialog.findViewById(R.id.failuresAddDialogFailureOkBtn);

        titleEdit = (EditText) failuresDialog.findViewById(R.id.failuresAddDialogBusinessName);
        titleEdit.addTextChangedListener(textWatcherListener);
        titleEdit.requestFocus();
        openKeyboard();
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
                    firstAdd = true;
                    new UpdateFailureBidTask().execute();
                    //failuresDialog.dismiss();
                    //db.updateFailureBid(failureContent, title, adapter.getObjectId(position), true); /** need to improve this!! */
                    //refreshFailures();
                }
            }
        });
    }

    class UpdateFailureBidTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return db.updateFailureBid(failureContent, title, adapter.getObjectId(position), firstAdd);
        }

        @Override
        protected void onPostExecute(Boolean isAdded) {
            if (isAdded) {
                bar.setVisibility(View.GONE);
                loadListViewData();
            }
        }
    }

    // edit bid price dialog
    public void editBidPrice() {
        mDialog(R.layout.failures_edit_dialog_layout);
        dialogFailureOkBtn = (Button) failuresDialog.findViewById(R.id.failuresAddDialogFailureOkBtn);
        dialogFailureCancelBtn = (Button) failuresDialog.findViewById(R.id.failuresAddDialogFailureCancelBtn);

        titleEdit = (EditText) failuresDialog.findViewById(R.id.failuresAddDialogBusinessName);
        titleEdit.addTextChangedListener(textWatcherListener);
        titleEdit.requestFocus();
        openKeyboard();
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
                    firstAdd=false;
                    new UpdateFailureBidTask().execute();
                }
            }
        });
    }

    // approve families dialog
    public void showApprovals() {
        mDialog(R.layout.failures_approval_dialog_layout);
        content = (TextView) failuresDialog.findViewById(R.id.failuresApprovalDialogList);
        String family = getString(R.string.family);
        // create list for families which approved
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
            layoutParams.setMargins(0, 0, 0, 0);
            dialogFailureCancelBtn.setLayoutParams(layoutParams);
            // set width of button to match parent
            ViewGroup.LayoutParams lp = dialogFailureCancelBtn.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            dialogFailureCancelBtn.setLayoutParams(lp);
        }


        // adds a listener to close button
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
                new SetFailureInactiveTask().execute();
                failuresDialog.dismiss();
            }
        });
        //add a listener for dismiss (close) this dialog
        failuresDialog.setOnDismissListener(new OnDismissListener() {

            public void onDismiss(DialogInterface dialog) {
                myList = "";
            }
        });

    }

    class SetFailureInactiveTask extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected void onPreExecute() {
            bar.setVisibility(View.VISIBLE);
            db.createPaymentFromFailure(failureObjectId);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return db.setFailureInactive(failureObjectId);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            bar.setVisibility(View.GONE);
            loadListViewData();
        }
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
                        new DeleteFailureTask().execute();
                    }
                })
                .show();
        View cView = dialog.getCustomView();
        TextView content = (TextView) cView.findViewById(R.id.text);
        content.setText(getString(R.string.deleteFailure));
    }

    class DeleteFailureTask extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return db.deleteFailure(adapter.getObjectId(position));
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            bar.setVisibility(View.GONE);
            loadListViewData();
        }
    }

    // custom dialog
    public void mDialog(int layout) {
        dialogLayout = View.inflate(getActivity(), layout, null);
        failuresDialog = new Dialog(getActivity());
        failuresDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        failuresDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        failuresDialog.setContentView(dialogLayout);
        failuresDialog.show();

        failuresDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                closeKeyboard();
            }
        });
    }

    // reload page
    public void refreshFailures() {
        Fragment fragment1 = new FailuresScreen();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, fragment1).commit();
    }

    // manually show keyboard
    private void openKeyboard() {
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    // manually close keyboard (used to ensure when dialog closes, keyboard closes as well)
    private void closeKeyboard() {
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    // toast in middle of screen
    public void mToast(String s) {
        Toast toast = Toast.makeText(getActivity(), s, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


}


