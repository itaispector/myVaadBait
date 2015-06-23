package com.myvaad.myvaad;

import java.util.ArrayList;
import java.util.List;

import adapters.FailuresAdapter;
import adapters.PaymentsAdapter;
import adapters.PaymentsAdminUsersListAdapter;
import adapters.PaymentsAdminVaadBaitAdapter;
import adapters.PaymentsUserVaadBaitAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.internal.view.menu.MenuView.ItemView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.parse.Parse;

public class PaymentsScreen extends Fragment {
    ListView paymentsList, adminPaymentsList, userVaadBaitList, adminVaadBaitList;
    PaymentsAdapter adapter;
    PaymentsAdminUsersListAdapter adminAdapterUL;
    PaymentsAdminVaadBaitAdapter adminAdapterVB;
    PaymentsUserVaadBaitAdapter userAdapterVB;
    ViewFlipper vf;
    String[] dataForPayment;
    String payment, amount, objectId, uObjectId, vaadPayPalAccount;
    TextView paymentName, paymentAmount;
    Button paypalPay, backBtn, backBtn2, backBtn3, vaadBaitPaymentsBtn, backBtn4, payAllVB, createVB;
    Button dialogPaymentOkBtn, dialogPaymentCancelBtn, sendNotificationBtn;
    CheckBox userPaid;
    ParseDB db;
    int position, sum, npValue;
    List myData;
    ArrayList objectIds = new ArrayList<String>();
    View dialogLayout;
    Dialog paymentsDialog;
    NumberPicker np;
    ImageView addPaymentBtn;
    EditText paymentNameField;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String appId = "QdwF666zm76ORQcn4KF6JNwDfsb6cj97QunbpT1s";
        String clientId = "OiJI3KdONEN9jML6Mi6r6iQTpR8mIOBv3YgsUhdv";
        //Initialize with keys
        Parse.initialize(getActivity(), appId, clientId);
        db = ParseDB.getInstance(getActivity());
        vaadPayPalAccount = db.getVaadPayPalAccount();
        uObjectId = db.getCurrentUserObjectId();
        final View rootView = inflater.inflate(R.layout.payments_screen, container, false);

        vf = (ViewFlipper) rootView.findViewById(R.id.paymentsViewFlipper);
        paymentName = (TextView) rootView.findViewById(R.id.paymentName);
        paymentAmount = (TextView) rootView.findViewById(R.id.paymentAmount);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        //listener for add payment button
        addPaymentBtn = (ImageView) rootView.findViewById(R.id.add_payment_btnn);
        addPaymentBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addPaymentDialog();
            }
        });

        //listener for vaad bait payments button
        vaadBaitPaymentsBtn = (Button) rootView.findViewById(R.id.showVaadBaitPaymentsBtn);
        vaadBaitPaymentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (db.isCurrentUserAdmin()) {
                    //show admin layout for vaad bait payments
                    vf.setDisplayedChild(vf.indexOfChild(getActivity().findViewById(R.id.paymentsAdminVaadBaitLayout)));
                } else {
                    //show user layout for vaad bait payments paymentsDayarLayout
                    vf.setDisplayedChild(vf.indexOfChild(getActivity().findViewById(R.id.paymentsDayarLayout)));
                }
            }
        });
        //calls the list views and their adapters
        //main layout payments list view
        paymentsList = (ListView) rootView.findViewById(R.id.PaymentsFamilyListView);
        //if admin is connected show all payments
        if (db.isCurrentUserAdmin()) {
            adapter = new PaymentsAdapter(getActivity(), db.getPayments());
            //reveal add payment button
            addPaymentBtn.setVisibility(View.VISIBLE);
        } else {
            //if user is connected load user payments list
            adapter = new PaymentsAdapter(getActivity(), db.getPaymentsForUser());
        }
        paymentsList.setAdapter(adapter);
        //adds a listner to item in the listview
        paymentsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int idx, long id) {
                if (db.isCurrentUserAdmin()) {
                    //show admin layout for payment
                    //init users listview with object id of payment
                    //admin layout listview for regular payments

                    String objectId = (adapter.getItem(idx))[3];
                    adminPaymentsList = (ListView) rootView.findViewById(R.id.paymentsDayarimListView);
                    adminAdapterUL = new PaymentsAdminUsersListAdapter(getActivity(), db.getPaidUsersForPayment(objectId), objectId, PaymentsScreen.this);
                    adminPaymentsList.setAdapter(adminAdapterUL);
                    vf.setDisplayedChild(vf.indexOfChild(getActivity().findViewById(R.id.paymentsAdminLayout)));
                } else {
                    //show dayar layout for payment
                    dataForPayment = (String[]) adapter.getItem(idx);
                    payment = dataForPayment[0];
                    amount = dataForPayment[1];
                    objectId = dataForPayment[3];
                    paymentName.setText(payment);
                    paymentAmount.setText("\u20AA " + amount);
                    vf.setDisplayedChild(vf.indexOfChild(getActivity().findViewById(R.id.paymentsUserLayout)));
                }
            }
        });

        userVaadBaitList = (ListView) rootView.findViewById(R.id.paymentsDayarVaadBaitListView);
        userAdapterVB = new PaymentsUserVaadBaitAdapter(getActivity(), db.getVaadBaitPaymentsForUser(), uObjectId, this);
        userVaadBaitList.setAdapter(userAdapterVB);

        adminVaadBaitList = (ListView) rootView.findViewById(R.id.paymentsAdminVaadBaitListView);
        createVB = (Button) rootView.findViewById(R.id.paymentsAdminAddVBPayments);
        sendNotificationBtn = (Button) rootView.findViewById(R.id.paymentsAdminSendNotification2);
        if (db.isVaadBaitPaymentsExists()) {
            adminAdapterVB = new PaymentsAdminVaadBaitAdapter(getActivity(), db.getUserListVaadBaitPayments(), this);
            adminVaadBaitList.setAdapter(adminAdapterVB);
            //add a listener to item in the list view
            adminVaadBaitList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View item,
                                        int idx, long id) {
                    //opens/closes each row
                    adminAdapterVB.setState(adminAdapterVB.getState(idx) ? false : true, idx);
                    myData = adminAdapterVB.getItem(idx);
                    uObjectId = "" + myData.get(1);
                    CheckBox cb1 = (CheckBox) item.findViewById(R.id.paymentsVaadBaitAdminListViewCB1);
                    cb1.setOnCheckedChangeListener(checkBoxListener);
                    CheckBox cb2 = (CheckBox) item.findViewById(R.id.paymentsVaadBaitAdminListViewCB2);
                    cb2.setOnCheckedChangeListener(checkBoxListener);
                    CheckBox cb3 = (CheckBox) item.findViewById(R.id.paymentsVaadBaitAdminListViewCB3);
                    cb3.setOnCheckedChangeListener(checkBoxListener);
                    CheckBox cb4 = (CheckBox) item.findViewById(R.id.paymentsVaadBaitAdminListViewCB4);
                    cb4.setOnCheckedChangeListener(checkBoxListener);
                    CheckBox cb5 = (CheckBox) item.findViewById(R.id.paymentsVaadBaitAdminListViewCB5);
                    cb5.setOnCheckedChangeListener(checkBoxListener);
                    CheckBox cb6 = (CheckBox) item.findViewById(R.id.paymentsVaadBaitAdminListViewCB6);
                    cb6.setOnCheckedChangeListener(checkBoxListener);
                    CheckBox cb7 = (CheckBox) item.findViewById(R.id.paymentsVaadBaitAdminListViewCB7);
                    cb7.setOnCheckedChangeListener(checkBoxListener);
                    CheckBox cb8 = (CheckBox) item.findViewById(R.id.paymentsVaadBaitAdminListViewCB8);
                    cb8.setOnCheckedChangeListener(checkBoxListener);
                    CheckBox cb9 = (CheckBox) item.findViewById(R.id.paymentsVaadBaitAdminListViewCB9);
                    cb9.setOnCheckedChangeListener(checkBoxListener);
                    CheckBox cb10 = (CheckBox) item.findViewById(R.id.paymentsVaadBaitAdminListViewCB10);
                    cb10.setOnCheckedChangeListener(checkBoxListener);
                    CheckBox cb11 = (CheckBox) item.findViewById(R.id.paymentsVaadBaitAdminListViewCB11);
                    cb11.setOnCheckedChangeListener(checkBoxListener);
                    CheckBox cb12 = (CheckBox) item.findViewById(R.id.paymentsVaadBaitAdminListViewCB12);
                    cb12.setOnCheckedChangeListener(checkBoxListener);
                }
            });
            createVB.setVisibility(View.GONE);
            sendNotificationBtn.setVisibility(View.VISIBLE);
            adminVaadBaitList.setVisibility(View.VISIBLE);
        } else {
            //add listener to create VB payments button
            sendNotificationBtn.setVisibility(View.GONE);
            adminVaadBaitList.setVisibility(View.GONE);
            createVB.setVisibility(View.VISIBLE);
            createVB.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    //opens dialog to let admin set monthly payment
                    setMonthlyPaymentDialog();
                }
            });
        }
        //add listener to paypal pay button
        paypalPay = (Button) rootView.findViewById(R.id.paypalPay);
        paypalPay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pay();
            }
        });

        //add listener to pay all vaad bait payments button
        payAllVB = (Button) rootView.findViewById(R.id.paymentsDayarVaadBaitPayAllBtn);
        payAllVB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                payAllVaadBaitDialog();
            }
        });


        //adds a listener to view switcher back button
        backBtn = (Button) rootView.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(goBackListener);
        backBtn2 = (Button) rootView.findViewById(R.id.goBackBtn2);
        backBtn2.setOnClickListener(goBackListener);
        backBtn3 = (Button) rootView.findViewById(R.id.goBackBtn3);
        backBtn3.setOnClickListener(goBackListener);
        backBtn4 = (Button) rootView.findViewById(R.id.goBackBtn);
        backBtn4.setOnClickListener(goBackListener);

        getActivity().setTitle(R.string.PaymentsTitle);
        setHasOptionsMenu(true);

        return rootView;
    }

    //listener class for months check box
    public OnCheckedChangeListener checkBoxListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int idx = (Integer.parseInt(buttonView.getText().toString())) - 1;
            List<String> objectIds = (List) myData.get(3);
            objectId = objectIds.get(idx);
            if (isChecked) {
                db.addPaidUserToPaymentList(objectId, uObjectId);
            } else {
                db.removePaidUserToVaadBaitPaymentList(objectId, uObjectId);
            }

        }
    };

    //listener class for go back button
    public OnClickListener goBackListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //go back to main layout of payments
            back();
        }
    };

    public void check(View v) {
        Toast.makeText(getActivity(), "im working", Toast.LENGTH_LONG).show();
    }

    //shows main layout of payments
    public void back() {
        vf.setDisplayedChild(vf.indexOfChild(getActivity().findViewById(R.id.paymentsMainLayout)));
    }

    public void pay() {
        Intent i = new Intent(getActivity(), PayPalActivity.class);
        i.putExtra("amount", amount.concat(".0"));
        i.putExtra("paymentName", payment);
        i.putExtra("paymentObjectId", objectId);
        i.putExtra("userObjectId", uObjectId);
        i.putExtra("email", vaadPayPalAccount);
        this.startActivity(i);
        back();
    }

    public void pay(String amount, String payment, String objectId, String uObjectId) {
        Intent i = new Intent(getActivity(), PayPalActivity.class);
        i.putExtra("amount", amount.concat(".0"));
        i.putExtra("paymentName", payment);
        i.putExtra("paymentObjectId", objectId);
        i.putExtra("userObjectId", uObjectId);
        i.putExtra("email", vaadPayPalAccount);
        this.startActivity(i);
        back();
    }

    public void payAllVaadBaitDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        StringBuilder message = new StringBuilder();
        message.append("הנך עומד/ת לשלם על: \n");
        int size = userAdapterVB.getCount();
        sum = 0;
        List tmpData;
        for (int i = 0; i < size; i++) {
            tmpData = userAdapterVB.getItem(i);
            String paymentName = "" + tmpData.get(0);
            String amount = "" + tmpData.get(1);
            objectId = "" + tmpData.get(2);
            objectIds.add(objectId);
            sum += Integer.parseInt(amount);
            message.append(paymentName + " \u20AA" + amount + "\n");
        }
        message.append("סה״כ \u20AA" + sum + "\n");
        message.append("האם אתה בטוח?");
        dialog.setMessage(message);

        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(getActivity(), PayPalActivity.class);
                i.putExtra("amount", ("" + sum).concat(".0"));
                i.putExtra("paymentName", "תשלומי ועד");
                i.putStringArrayListExtra("objectIds", objectIds);
                i.putExtra("userObjectId", uObjectId);
                i.putExtra("email", vaadPayPalAccount);
                getActivity().startActivity(i);
                back();
            }
        });

        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        dialog.show();
    }

    public void setMonthlyPaymentDialog() {
        dialogLayout = View.inflate(getActivity(), R.layout.payments_set_vb_dialog, null);
        paymentsDialog = new Dialog(getActivity());
        paymentsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        paymentsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        paymentsDialog.setContentView(dialogLayout);
        paymentsDialog.show();

        np = (NumberPicker) dialogLayout.findViewById(R.id.paymentsDialogNumberPicker);
        //set max value for np
        String[] numbers = new String[1000 / 10];
        // set numbers of picker
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = Integer.toString(i * 10 + 10);
        }
        np.setDisplayedValues(numbers);
        np.setMaxValue(numbers.length - 1);
        np.setMinValue(0);
        //disable picking loop
        np.setWrapSelectorWheel(false);
        //disable keyboard pop up
        np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        //number picker listener
        np.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //update number picker value
                npValue = newVal * 10 + 10;
            }
        });

        dialogPaymentOkBtn = (Button) dialogLayout.findViewById(R.id.paymentsDialogConfirmBtn);
        dialogPaymentOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.createVaadBaitPayments("" + npValue);
                paymentsDialog.dismiss();
                refreshPayments();
            }
        });

        dialogPaymentCancelBtn = (Button) dialogLayout.findViewById(R.id.paymentsDialogCancelBtn);
        dialogPaymentCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentsDialog.dismiss();
            }
        });
    }

    public void addPaymentDialog() {
        dialogLayout = View.inflate(getActivity(), R.layout.add_payment_dialog, null);
        paymentsDialog = new Dialog(getActivity());
        paymentsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        paymentsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        paymentsDialog.setContentView(dialogLayout);
        paymentsDialog.show();

        np = (NumberPicker) dialogLayout.findViewById(R.id.addPaymentDialogNumberPicker);
        //set max value for np
        String[] numbers = new String[10000 / 5];
        // set numbers of picker
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = Integer.toString(i * 5 + 5);
        }
        np.setDisplayedValues(numbers);
        np.setMaxValue(numbers.length - 1);
        np.setMinValue(0);
        //disable picking loop
        np.setWrapSelectorWheel(false);
        //disable keyboard pop up
        np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        //number picker listener
        np.setOnValueChangedListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //update number picker value
                npValue = newVal * 5 + 5;
            }
        });

        paymentNameField = (EditText) dialogLayout.findViewById(R.id.addPaymentDialogName);

        dialogPaymentOkBtn = (Button) dialogLayout.findViewById(R.id.addPaymentDialogConfirmBtn);
        dialogPaymentOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String paymentName = paymentNameField.getText().toString();
                String paymentPrice = "" + npValue;
                db.createPayment(paymentName, paymentPrice);
                paymentsDialog.dismiss();
                refreshPayments();
            }
        });

        dialogPaymentCancelBtn = (Button) dialogLayout.findViewById(R.id.addPaymentsDialogCancelBtn);
        dialogPaymentCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentsDialog.dismiss();
            }
        });
    }

    public void PaidUserToPaymentList(boolean isChecked, String objectId, String uObjectId) {
        if (isChecked) {
            db.addPaidUserToPaymentList(objectId, uObjectId);
        } else {
            db.removePaidUserToPaymentList(objectId, uObjectId);
        }
    }

    public void PaidUserToVaadBaitPayments(boolean isChecked, String objectId, String uObjectId) {
        if (isChecked) {
            db.addPaidUserToPaymentList(objectId, uObjectId);
        } else {
            db.removePaidUserToVaadBaitPaymentList(objectId, uObjectId);
        }
    }

    public void paidAllVaadBait(String uObjectId) {
        db.addPaidAllToUserVaadBaitPaymentList(uObjectId);
        refreshAdminVBAdapter();
    }

    public void refreshAdminVBAdapter() {
        adminAdapterVB.refresh();
    }

    @Override
    public void onStart() {
        adapter.reloadData(db.getPaymentsForUser());
        userAdapterVB.reloadData(db.getVaadBaitPaymentsForUser());
        super.onStart();
    }

    //refresh loading the payments in the adapter
    public void refreshPayments() {
        Fragment fragment1 = new PaymentsScreen();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment1).commit();
    }


}




