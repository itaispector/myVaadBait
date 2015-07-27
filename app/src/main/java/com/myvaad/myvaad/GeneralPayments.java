package com.myvaad.myvaad;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import dialogs.RingProgressDialog;

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
    private TextView paymentTitle, paymentPrice, noPaymentsText;
    private List usersList;
    private String paymentObjectId, vaadPayPalAccount, paymentType;
    private ParseObject payment;
    private double amountToPay, total = 0.0;
    private ArrayList objectIds = new ArrayList<String>();
    private RingProgressDialog loaderDialog;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //parse init
        Parse.initialize(getActivity());
        db = ParseDB.getInstance(getActivity());
        //inflate layout
        final View rootView = inflater.inflate(R.layout.payments_general_layout, container, false);
        //fix ltr issues
        if (getActivity().getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        viewFlipper = (ViewFlipper) rootView.findViewById(R.id.paymentsGeneralLayoutViewFlipper);
        loader = (ProgressView) rootView.findViewById(R.id.my_progress_loader);
        noPaymentsText = (TextView) rootView.findViewById(R.id.no_payments_text);
        generalPaymentsListView = (ListView) rootView.findViewById(R.id.PaymentsFamilyListView);

        //instantiating adapter and set it to list view
        listViewQueryInBackground();

        //list view item on click listener
        generalPaymentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                payment = paymentsAdapter.getItem(i);
                paymentObjectId = payment.getObjectId();
                String buildingCode = payment.getString("buildingCode");
                if (db.isCurrentUserAdmin()) {
                    // clicked by admin
                    // move to next view, and populate list view of users for payment in background
                    loaderDialog = new RingProgressDialog(getActivity());
                    adminPaymentsList = (ListView) rootView.findViewById(R.id.paymentsDayarimListView);
                    adminPaymentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            CheckBox check = (CheckBox) view.findViewById(R.id.usersListCB);
                            check.setChecked(!check.isChecked());
                        }
                    });
                    loadListViewOfUsers(paymentObjectId, buildingCode);
                } else {
                    // clicked by user
                    loader.setVisibility(View.VISIBLE);
                    isExistPaypalAccount(false);
                }
            }
        });


        addPaymentBtn = (FloatingActionButton) rootView.findViewById(R.id.add_payment_btn);
        addPaymentBtn.attachToListView(generalPaymentsListView);

        addPaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if current user is admin, open add payment dialog
                if (db.isCurrentUserAdmin()) {
                    loader.setVisibility(View.VISIBLE);
                    addPaymentDialog();
                } else {
                    isExistPaypalAccount(true);
                }

            }
        });

        backBtn = (ImageView) rootView.findViewById(R.id.backToolBar);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GoBackPaymentTask().execute();
            }
        });

        sendNotifications = (ImageView) rootView.findViewById(R.id.notificationToolBar);
        sendNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .customView(R.layout.custom_layout_content, false)
                        .positiveText(R.string.yes)
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
                View v = dialog.getCustomView();
                TextView tv = (TextView) v.findViewById(R.id.text);
                tv.setText(getString(R.string.send_notice_dialog_content));

            }
        });

        moveToExpensesBtn = (ImageView) rootView.findViewById(R.id.cashToolBar);
        moveToExpensesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .customView(R.layout.custom_layout_content, false)
                        .positiveText(R.string.yes)
                        .buttonsGravity(GravityEnum.END)
                        .positiveColorRes(R.color.colorPrimary)
                        .negativeColorRes(R.color.colorPrimary)
                        .negativeText(R.string.no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                new MovePaymentTask().execute();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }
                        })
                        .show();
                View v = dialog.getCustomView();
                TextView tv = (TextView) v.findViewById(R.id.text);
                tv.setText(getString(R.string.move_to_expenses_content));
            }
        });

        deletePaymentBtn = (ImageView) rootView.findViewById(R.id.trashToolBar);
        deletePaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .customView(R.layout.custom_layout_content, false)
                        .positiveText(R.string.yes)
                        .buttonsGravity(GravityEnum.END)
                        .positiveColorRes(R.color.colorPrimary)
                        .negativeColorRes(R.color.colorPrimary)
                        .negativeText(R.string.no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                new DeletePaymentTask().execute();
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }
                        })
                        .show();
                View v = dialog.getCustomView();
                TextView tv = (TextView) v.findViewById(R.id.text);
                tv.setText(getString(R.string.delete_payment_content));
            }
        });


        getActivity().setTitle(R.string.PaymentsTitle);
        setHasOptionsMenu(true);
        return rootView;
    }

    class GoBackPaymentTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            loaderDialog = new RingProgressDialog(getActivity());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List usersObjectIds = new ArrayList();
            for (int i = 0; i < paymentsUserAdapter.getCount(); i++) {
                // if user didn't pay yet, add him to list of users who didn't pay,
                // and notify them
                CheckBox v = (CheckBox) (adminPaymentsList.getChildAt(i)).findViewById(R.id.usersListCB);
                if (v.isChecked()) {
                    String userObjectId = (paymentsUserAdapter.getItem(i)).getObjectId();
                    usersObjectIds.add(userObjectId);
                }
            }
            db.savePaidUsers(paymentObjectId, usersObjectIds);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            vfSetLayout(R.id.payments_general_layout);
            loaderDialog.dismiss();
        }
    }

    class MovePaymentTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            loaderDialog = new RingProgressDialog(getActivity());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            db.movePaymentToExpenses(paymentObjectId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            listViewQueryInBackground();
            vfSetLayout(R.id.payments_general_layout);
            loaderDialog.dismiss();
        }
    }

    class DeletePaymentTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            loaderDialog = new RingProgressDialog(getActivity());
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return db.deletePaymentBoolean(paymentObjectId);
        }

        @Override
        protected void onPostExecute(Boolean isDeleted) {
            if (isDeleted) {
                listViewQueryInBackground();
                vfSetLayout(R.id.payments_general_layout);
                loaderDialog.dismiss();
                mToast(getString(R.string.payment_successfully_deleted));
            } else {
                mToast(getString(R.string.payment_not_deleted));
            }

        }
    }


    private void listViewQueryInBackground() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        query.whereContains("buildingCode", db.getCurrentUserBuildingCode());
        query.whereContains("paymentType", "extra");
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
                    paymentsAdapter = new PaymentsAdapter(getActivity(), payments, db.isCurrentUserAdmin());
                    generalPaymentsListView.setAdapter(paymentsAdapter);
                    if (payments.isEmpty()) {
                        // show no payments
                        noPaymentsText.setVisibility(View.VISIBLE);
                        // if current its current user, hide pay all button
                        if (!db.isCurrentUserAdmin()) {
                            addPaymentBtn.setVisibility(View.GONE);
                        }
                    } else {
                        addPaymentBtn.setVisibility(View.VISIBLE);
                        noPaymentsText.setVisibility(View.GONE);
                    }
                    loader.setVisibility(View.GONE);
                } else {
                    //Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.v("***PARSE ERROR***", e.getMessage());
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
                        public void done(ParseObject paidByList, ParseException e) {
                            if (e == null) {
                                List paidBy = null;
                                if (paidByList.getList("paidBy") != null) {
                                    paidBy = paidByList.getList("paidBy");
                                }
                                paymentsUserAdapter = new PaymentsAdminUsersListAdapter(getActivity(), users, paidBy);
                                adminPaymentsList.setAdapter(paymentsUserAdapter);
                                loaderDialog.dismiss();
                                vfSetLayout(R.id.paymentsAdminLayout);
                            } else {
                                Log.v("***PARSE ERROR***", e.getMessage());
                                //mToast(e.getMessage());
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

    private void createPayment(final String paymentName, final String paymentPrice, final String paymentType) {
        final String currentBuilding = db.getCurrentUserBuildingCode();
        ParseQuery.getQuery("payments")
                .whereEqualTo("buildingCode", currentBuilding)
                .getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (e == null) {
                            ParseObject payment = new ParseObject("payments");
                            payment.put("buildingCode", currentBuilding);
                            payment.put("description", paymentName);
                            payment.put("amount", paymentPrice);
                            payment.put("paymentType", paymentType);
                            payment.put("houses", parseObject.getInt("houses"));
                            payment.put("paymentApproved", false);
                            payment.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        listViewQueryInBackground();
                                    }
                                }
                            });
                        }
                    }
                });


    }

    private void addPaymentDialog() {
        // opens dialog to choose extra or regular payment
        new MaterialDialog.Builder(getActivity())
                .title(R.string.add_expense)
                .titleGravity(GravityEnum.END)
                .content(R.string.choose_type)
                .contentGravity(GravityEnum.END)
                .items(R.array.payment_types_list)
                .itemsGravity(GravityEnum.END)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        // array of payment types
                        paymentType = getResources().getStringArray(R.array.payment_types)[i];
                        // open dialog for add payment
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
                                    loader.setVisibility(View.VISIBLE);
                                    createPayment(paymentName, paymentPrice, paymentType);
                                }
                            }
                        });
                    }
                })
                .show();


    }

    private void paymentDialog() {
        loader.setVisibility(View.GONE);
        myDialog(R.layout.single_payment_dialog);
        paymentTitle = (TextView) dialogLayout.findViewById(R.id.paymentTitle);
        paymentPrice = (TextView) dialogLayout.findViewById(R.id.paymentPrice);
        payBtn = (Button) dialogLayout.findViewById(R.id.payBtn);
        String paymentName = payment.getString("description");
        String paymentPriceString = payment.getString("amount");
        int numOfHouses = payment.getInt("houses");
        paymentTitle.setText(paymentName);
        amountToPay = Math.round((Double.parseDouble(paymentPriceString) / numOfHouses) * 100.0) / 100.0;
        paymentPrice.setText(getResources().getString(R.string.shekel) + " " + amountToPay);
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pay();
            }
        });
    }

    private void payAllDialog() {
        // inflate list of all existing payments
        LinearLayout myView = new LinearLayout(getActivity());
        myView.setOrientation(LinearLayout.VERTICAL);
        total = 0.0;
        for (int i = 0; i < paymentsAdapter.getCount(); i++) {
            View inflation = View.inflate(getActivity(), R.layout.pay_all_item, null);
            TextView pName = (TextView) inflation.findViewById(R.id.list_item_paymentName);
            TextView pAmount = (TextView) inflation.findViewById(R.id.list_item_paymentAmount);
            pName.setText(((paymentsAdapter.getItem(i)).getString("description")));
            int numOfHouses = Integer.parseInt(((paymentsAdapter.getItem(i)).getString("houses")));
            double calc = Math.round((Double.parseDouble((paymentsAdapter.getItem(i)).getString("amount")) / numOfHouses) * 100.0) / 100.0;
            pAmount.setText(getResources().getString(R.string.shekel) + " " + calc);
            myView.addView(inflation);
            total += calc;
            objectIds.add((paymentsAdapter.getItem(i)).getObjectId());
        }
        total = Math.round(total * 100.0) / 100.0;
        //add total sum line
        View inflation = View.inflate(getActivity(), R.layout.pay_all_item, null);
        TextView pName = (TextView) inflation.findViewById(R.id.list_item_paymentName);
        TextView pAmount = (TextView) inflation.findViewById(R.id.list_item_paymentAmount);
        pName.setTypeface(Typeface.DEFAULT_BOLD);
        pName.setText(getResources().getString(R.string.total));
        pAmount.setTypeface(Typeface.DEFAULT_BOLD);
        pAmount.setText(getResources().getString(R.string.shekel) + " " + total);
        myView.addView(inflation);

        new MaterialDialog.Builder(getActivity())
                .title(R.string.pay_all)
                .titleGravity(GravityEnum.END)
                .customView(myView, true)
                .positiveText(R.string.yes)
                .positiveColorRes(R.color.colorPrimary)
                .buttonsGravity(GravityEnum.END)
                .negativeText(R.string.no)
                .negativeColorRes(R.color.colorPrimary)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        pay(total, objectIds);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .show();
    }

    public void isExistPaypalAccount(final boolean fabClicked) {
        loader.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("buildings");
        query.whereEqualTo("buildingCode", db.getCurrentUserBuildingCode());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) {
                    // paypal account exist, can open pay dialog
                    if (parseObject.getString("paypal") != null) {
                        vaadPayPalAccount = parseObject.getString("paypal");
                        // check if click came from floating action button or from list view
                        if (fabClicked) {
                            // pay all, clicked from floating button
                            loader.setVisibility(View.GONE);
                            payAllDialog();
                        } else {
                            // single payment, clicked from payments list
                            loader.setVisibility(View.GONE);
                            paymentDialog();
                        }
                    } else {
                        // paypal account doesn't exist, show error dialog
                        loader.setVisibility(View.GONE);
                        new MaterialDialog.Builder(getActivity())
                                .content(R.string.no_paypal_message)
                                .contentGravity(GravityEnum.END)
                                .buttonsGravity(GravityEnum.END)
                                .positiveText(R.string.close)
                                .positiveColorRes(R.color.colorPrimary)
                                .show();
                    }
                } else {
                    Log.v("******PARSE ERROR******", e.getMessage());
                }

            }
        });
    }


    public void pay() {
        Intent i = new Intent(getActivity(), PayPalActivity.class);
        i.putExtra("amount", amountToPay + "");
        i.putExtra("paymentName", payment.getString("description"));
        i.putExtra("paymentObjectId", payment.getObjectId());
        i.putExtra("userObjectId", db.getCurrentUserObjectId());
        i.putExtra("email", vaadPayPalAccount);
        this.startActivity(i);
        paymentsDialog.dismiss();
    }

    public void pay(double total, ArrayList<String> objectIds) {
        Intent i = new Intent(getActivity(), PayPalActivity.class);
        i.putExtra("amount", total + "");
        i.putExtra("paymentName", getResources().getString(R.string.total_pay));
        i.putStringArrayListExtra("objectIds", objectIds);
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
        List usersObjectIds = new ArrayList<>();
        List familyNames = new ArrayList<>();
        for (int i = 0; i < paymentsUserAdapter.getCount(); i++) {
            // if user didn't pay yet, add him to list of users who didn't pay,
            // and notify them
            CheckBox v = (CheckBox) (adminPaymentsList.getChildAt(i)).findViewById(R.id.usersListCB);
            if (!v.isChecked()) {
                String userObjectId = (paymentsUserAdapter.getItem(i)).getObjectId();
                String familyName = (paymentsUserAdapter.getItem(i)).getString("familyName");
                usersObjectIds.add(userObjectId);
                familyNames.add(familyName);
            }
        }
        params.put("usersObjectIds", usersObjectIds);
        params.put("familyNames", familyNames);
        params.put("msg", getString(R.string.notification_msg_extra_payments) + payment.getString("description"));
        ParseCloud.callFunctionInBackground("sendNotificationToUnPaidUsers", params, new FunctionCallback<String>() {
            public void done(String result, ParseException e) {
                if (e == null) {
                    mToast(getString(R.string.noti_success));
                } else {
                    mToast(getString(R.string.noti_failure));
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


