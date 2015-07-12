package com.myvaad.myvaad;

import android.app.Dialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.rey.material.widget.ProgressView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapters.PaymentsAdapter;
import adapters.PaymentsAdminUsersListAdapter;

public class GeneralPayments extends Fragment {

    private ParseDB db;
    private ViewFlipper viewFlipper;
    private ListView generalPaymentsListView, adminPaymentsList;
    private PaymentsAdapter paymentsAdapter;
    private PaymentsAdminUsersListAdapter paymentsUserAdapter;
    private FloatingActionButton addPaymentBtn;
    private View dialogLayout;
    private Dialog paymentsDialog;
    private EditText paymentNameField, paymentPriceField;
    private Button dialogPaymentOkBtn, payBtn;
    private ProgressView loader;
    private ImageView backBtn, sendNotifications, moveToExpensesBtn, deletePaymentBtn;
    private TextView paymentTitle, paymentPrice;
    private List usersList;
    private String paymentObjectId, vaadPayPalAccount;
    private ParseObject payment;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //parse init
        Parse.initialize(getActivity());
        db = ParseDB.getInstance(getActivity());

        //inflate layout
        final View rootView = inflater.inflate(R.layout.payments_general_layout, container, false);

        //fix ltr issues
        if (getActivity().getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        //view flipper pointer
        viewFlipper = (ViewFlipper) rootView.findViewById(R.id.paymentsGeneralLayoutViewFlipper);

        //loader pointer and starter
        loader = (ProgressView) rootView.findViewById(R.id.my_progress_loader);
        loader.setVisibility(View.VISIBLE);

        //list view pointer
        generalPaymentsListView = (ListView) rootView.findViewById(R.id.PaymentsFamilyListView);

        //instantiating adapter and set it to list view
        listViewQueryInBackground();

        //list view item on click listener
        generalPaymentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                loader.setVisibility(View.VISIBLE);
                payment = paymentsAdapter.getItem(i);
                paymentObjectId = payment.getObjectId();
                String buildingCode = payment.getString("buildingCode");
                if (db.isCurrentUserAdmin()) {
                    // clicked by admin
                    // move to next view, and populate list view of users for payment in background
                    adminPaymentsList = (ListView) rootView.findViewById(R.id.paymentsDayarimListView);
                    loadListViewOfUsers(paymentObjectId, buildingCode);
                } else {
                    // clicked by user
                    loader.setVisibility(View.VISIBLE);
                    isExistPaypalAccount();
                }
            }
        });

        addPaymentBtn = (FloatingActionButton) rootView.findViewById(R.id.add_payment_btn);
        addPaymentBtn.attachToListView(generalPaymentsListView);

        addPaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if current user is admin, open add payment dialog
                if (!db.isCurrentUserAdmin()){
                    loader.setVisibility(View.VISIBLE);
                    addPaymentDialog();
                }else{
                    // current user is not admin, open pay all dialog

                }

            }
        });

        backBtn = (ImageView) rootView.findViewById(R.id.backToolBar);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vfSetLayout(R.id.payments_general_layout);
            }
        });

        sendNotifications = (ImageView) rootView.findViewById(R.id.notificationToolBar);
        sendNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getActivity())
                        .content(R.string.send_notice_dialog_content)
                        .positiveText(R.string.yes)
                        .contentGravity(GravityEnum.END)
                        .buttonsGravity(GravityEnum.END)
                        .positiveColorRes(R.color.colorPrimary)
                        .negativeColorRes(R.color.colorPrimary)
                        .negativeText(R.string.no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                sendNotification();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }
                        })
                        .show();

            }
        });

        moveToExpensesBtn = (ImageView) rootView.findViewById(R.id.cashToolBar);
        moveToExpensesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getActivity())
                        .content(R.string.move_to_expenses_content)
                        .positiveText(R.string.yes)
                        .contentGravity(GravityEnum.END)
                        .buttonsGravity(GravityEnum.END)
                        .positiveColorRes(R.color.colorPrimary)
                        .negativeColorRes(R.color.colorPrimary)
                        .negativeText(R.string.no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                db.movePaymentToExpenses(paymentObjectId);
                                vfSetLayout(R.id.payments_general_layout);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }
                        })
                        .show();
            }
        });

        deletePaymentBtn = (ImageView) rootView.findViewById(R.id.trashToolBar);
        deletePaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialDialog.Builder(getActivity())
                        .content(R.string.delete_payment_content)
                        .positiveText(R.string.yes)
                        .contentGravity(GravityEnum.END)
                        .buttonsGravity(GravityEnum.END)
                        .positiveColorRes(R.color.colorPrimary)
                        .negativeColorRes(R.color.colorPrimary)
                        .negativeText(R.string.no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                db.deletePayment(paymentObjectId);
                                vfSetLayout(R.id.payments_general_layout);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }
                        })
                        .show();
            }
        });


        getActivity().setTitle(R.string.PaymentsTitle);
        setHasOptionsMenu(true);
        return rootView;
    }

    private void listViewQueryInBackground() {
        String buildingCode = db.getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        query.whereContains("buildingCode", buildingCode);
        query.whereContains("paymentType", "regular");
        query.whereEqualTo("paymentApproved", false);
        //if current user is not admin, exclude paid payments of user in listview
        if (!db.isCurrentUserAdmin()) {
            String userObjectId = db.getCurrentUserObjectId();
            query.whereNotEqualTo("paidBy", userObjectId);
        }
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> payments, ParseException e) {
                if (e == null) {
                    paymentsAdapter = new PaymentsAdapter(getActivity(), payments);
                    generalPaymentsListView.setAdapter(paymentsAdapter);
                    loader.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadListViewOfUsers(final String paymentObjectId, String buildingCode) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("buildingCode", buildingCode);
        query.whereNotEqualTo("isAdmin", true);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> users, ParseException e) {
                if (e == null) {
                    ParseQuery<ParseObject> queryB = ParseQuery.getQuery("payments");
                    queryB.getInBackground(paymentObjectId, new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject paidBy, ParseException e) {
                            if (e == null) {
                                usersList = new ArrayList();
                                for (ParseObject user : users) {
                                    List rowUsersList = new ArrayList();
                                    String familyName = user.getString("familyName");
                                    String userObjectId = user.getObjectId();
                                    boolean isPaid = false;
                                    if (paidBy.getList("paidBy") != null) {
                                        isPaid = paidBy.getList("paidBy").contains(user.getObjectId());
                                    }
                                    rowUsersList.add(familyName);
                                    rowUsersList.add(userObjectId);
                                    rowUsersList.add(isPaid);
                                    usersList.add(rowUsersList);

                                }
                                paymentsUserAdapter = new PaymentsAdminUsersListAdapter(getActivity(), usersList, null, null);
                                adminPaymentsList.setAdapter(paymentsUserAdapter);
                                loader.setVisibility(View.GONE);
                                vfSetLayout(R.id.paymentsAdminLayout);
                            } else {
                                Log.v("***PARSE ERROR***", e.getMessage());
                                mToast(e.getMessage());
                            }


                        }
                    });

                } else {
                    Log.v("***PARSE ERROR***", e.getMessage());
                    mToast(e.getMessage());
                }

            }
        });
    }

    private void createPayment(String paymentName, String paymentPrice) {
        String currentBuilding = db.getCurrentUserBuildingCode();
        ParseObject payment = new ParseObject("payments");
        payment.put("buildingCode", currentBuilding);
        payment.put("description", paymentName);
        payment.put("amount", paymentPrice);
        payment.put("paymentType", "regular");
        payment.put("paymentApproved", false);
        payment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    refreshPage();
                }
            }
        });
    }

    private void addPaymentDialog() {
        myDialog(R.layout.add_payment_dialog);

        loader.setVisibility(View.GONE);

        paymentNameField = (EditText) dialogLayout.findViewById(R.id.addPaymentDialogName);
        paymentPriceField = (EditText) dialogLayout.findViewById(R.id.addPaymentDialogPrice);

        dialogPaymentOkBtn = (Button) dialogLayout.findViewById(R.id.addPaymentDialogConfirmBtn);

        paymentNameField.addTextChangedListener(textWatcherListener);
        paymentPriceField.addTextChangedListener(textWatcherListener);

        dialogPaymentOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.setVisibility(View.VISIBLE);
                String paymentName = paymentNameField.getText().toString();
                String paymentPrice = paymentPriceField.getText().toString();
                if ((paymentName.matches("\\s+")) || (paymentPrice.matches("\\s+"))) {
                    Toast toast = Toast.makeText(getActivity(), getResources().getString(R.string.empty_edittext_msg), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    paymentsDialog.dismiss();
                    createPayment(paymentName, paymentPrice);
                    loader.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private void paymentDialog() {
        loader.setVisibility(View.GONE);
        myDialog(R.layout.single_payment_dialog);
        paymentTitle = (TextView) dialogLayout.findViewById(R.id.paymentTitle);
        paymentPrice = (TextView) dialogLayout.findViewById(R.id.paymentPrice);
        payBtn=(Button)dialogLayout.findViewById(R.id.payBtn);
        String paymentName = payment.getString("description");
        String paymentPriceString = payment.getString("amount");
        paymentTitle.setText(paymentName);
        paymentPrice.setText(getResources().getString(R.string.shekel) + " " + paymentPriceString);
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pay();
            }
        });
    }

    public void isExistPaypalAccount(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("buildings");
        query.whereEqualTo("buildingCode", db.getCurrentUserBuildingCode());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e==null){
                    // paypal account exist, can open pay dialog
                    if (parseObject.getString("paypal")!=null){
                        vaadPayPalAccount = parseObject.getString("paypal");
                        paymentDialog();
                    }else{
                        // paypal account doesn't exist, show error dialog
                        loader.setVisibility(View.GONE);
                        new MaterialDialog.Builder(getActivity())
                                .content(R.string.no_paypal_message)
                                .contentGravity(GravityEnum.END)
                                .buttonsGravity(GravityEnum.END)
                                .neutralText(R.string.close)
                                .neutralColorRes(R.color.colorPrimary)
                                .show();
                    }
                }else{
                    Log.v("******PARSE ERROR******", e.getMessage());
                }

            }
        });
    }


    public void pay() {
        Intent i = new Intent(getActivity(), PayPalActivity.class);
        i.putExtra("amount", payment.getString("amount").concat(".0"));
        i.putExtra("paymentName", payment.getString("description"));
        i.putExtra("paymentObjectId", payment.getObjectId());
        i.putExtra("userObjectId", db.getCurrentUserObjectId());
        i.putExtra("email", vaadPayPalAccount);
        this.startActivity(i);
    }

    //listener to watch if fields are empty or not, if empty add button is disabled
    private TextWatcher textWatcherListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            boolean check = false;
            check = (paymentNameField.getText().toString().isEmpty() || paymentPriceField.getText().toString().isEmpty());
            dialogPaymentOkBtn.setEnabled(!check);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void sendNotification() {
        HashMap<String, Object> params = new HashMap<String, Object>();
        List<String> usersObjectId = new ArrayList<String>();
        for (int i = 0; i < paymentsUserAdapter.getCount(); i++) {
            List user = paymentsUserAdapter.getItem(i);
            if (!(boolean) user.get(2)) {
                usersObjectId.add((String) user.get(1));
            }
        }
        params.put("usersObjectIds", usersObjectId);
        params.put("paymentObjectId", paymentObjectId);
        params.put("buildingCode", db.getCurrentUserBuildingCode());
        ParseCloud.callFunctionInBackground("sendNotificationToUnPaidUsers", params, new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e == null) {
                    mToast(result);
                } else {
                    Toast.makeText(getActivity(), "" + e, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void myDialog(int layout) {
        dialogLayout = View.inflate(getActivity(), layout, null);
        paymentsDialog = new Dialog(getActivity());
        paymentsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        paymentsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        paymentsDialog.setContentView(dialogLayout);
        paymentsDialog.show();
    }

    private void refreshPage() {
        //
    }

    private void vfSetLayout(int layout) {
        viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(getActivity().findViewById(layout)));
    }

    private void mToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

}


