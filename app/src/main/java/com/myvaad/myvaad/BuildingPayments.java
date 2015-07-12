package com.myvaad.myvaad;

import android.app.Dialog;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;

import net.simonvt.numberpicker.NumberPicker;

import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.melnykov.fab.FloatingActionButton;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import adapters.BuildingExpensesAdapter;
import adapters.PaymentsAdminVaadBaitAdapter;
import adapters.PaymentsUserVaadBaitAdapter;

public class BuildingPayments extends Fragment {

    private ParseDB db;
    private BuildingExpensesAdapter customParseAdapter;
    private ListView listView;
    int totalExpensesAmount = 0;
    private TextView buildingTotalExpenses, noPaymentsTextView;
    ViewFlipper viewFlipper;
    private Toolbar toolBar;
    ListView userListView, adminListView;
    PaymentsAdminVaadBaitAdapter adminVaadBaitAdapter;
    PaymentsUserVaadBaitAdapter paymentsUserVaadBaitAdapter;
    View dialogLayout;
    Dialog paymentsDialog;
    NumberPicker npYear, npMonth;
    FloatingActionButton addPaymentBtn;
    Button okDialog, saveBtn, markAllBtn, cancelBtn;
    String yearValue, monthlyValue, currentClickedUserObjectId, currentClickedUserFamilyName, buildingCode;
    CheckedTextView cb1, cb2, cb3, cb4, cb5, cb6, cb7, cb8, cb9, cb10, cb11, cb12;
    RelativeLayout monthsCb;
    List<ParseObject> months;
    TextView familyNameTextView;
    boolean markAllBtnPressed=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Parse.initialize(getActivity());
        db = ParseDB.getInstance(getActivity());
        View rootView = inflater.inflate(R.layout.payments_vaad_bait_layout, container, false);

        if (getActivity().getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        buildingCode = db.getCurrentUserBuildingCode();
        viewFlipper = (ViewFlipper) rootView.findViewById(R.id.paymentsVaadBaitViewFlipper);

        //if is not admin, show all vaad bait open payments for dayar
        if (!db.isCurrentUserAdmin()) {
            userListView = (ListView) rootView.findViewById(R.id.paymentsDayarVaadBaitListView);
            loadUserListView();
            //change to layout of dayar vaad bait payments
            viewFlipper.setDisplayedChild(viewFlipper.indexOfChild(getActivity().findViewById(R.id.paymentsDayarLayout)));
        } else {
            adminListView = (ListView) rootView.findViewById(R.id.paymentsAdminVaadBaitListView);
            loadUserListViewForAdmin();

            adminListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ParseObject user = adminVaadBaitAdapter.getItem(i);
                    currentClickedUserObjectId = user.getObjectId();
                    currentClickedUserFamilyName = user.getString("familyName");
                    getMonthsFromParse();
                }
            });
        }



        addPaymentBtn = (FloatingActionButton) rootView.findViewById(R.id.add_payment_btn);
        addPaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPaymentDialogYear();
            }
        });
        noPaymentsTextView = (TextView) rootView.findViewById(R.id.no_payments_text);

        getActivity().setTitle(R.string.PaymentsTitle);
        setHasOptionsMenu(true);

        return rootView;
    }

    public void loadUserListView() {
        String buildingCode = db.getCurrentUserBuildingCode();
        String userObjectId = db.getCurrentUserObjectId();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        query.whereEqualTo("buildingCode", buildingCode);
        query.whereEqualTo("paymentType", "vaad");
        query.whereNotEqualTo("paidBy", userObjectId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> payments, ParseException e) {
                if (payments.isEmpty()) {
                    //loader.setVisibility(View.GONE);
                    noPaymentsTextView.setVisibility(View.VISIBLE);
                } else {
                    noPaymentsTextView.setVisibility(View.GONE);
                    paymentsUserVaadBaitAdapter = new PaymentsUserVaadBaitAdapter(getActivity(), payments, null, null);
                    userListView.setAdapter(paymentsUserVaadBaitAdapter);
                    //loader.setVisibility(View.GONE);
                }

            }
        });
    }

    public void loadUserListViewForAdmin() {
        final String buildingCode = db.getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("buildingCode", buildingCode);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> users, ParseException e) {
                adminVaadBaitAdapter = new PaymentsAdminVaadBaitAdapter(getActivity(), users);
                adminListView.setAdapter(adminVaadBaitAdapter);
                ParseQuery<ParseObject> queryB = ParseQuery.getQuery("payments");
                queryB.whereEqualTo("buildingCode", buildingCode);
                queryB.whereEqualTo("paymentType", "vaad");
                queryB.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> users, ParseException e) {
                        if (e == null) {

                        }
                    }
                });

            }
        });

    }

    //dialog template
    public void myDialog(int layout_name){
        dialogLayout = View.inflate(getActivity(), layout_name, null);
        paymentsDialog = new Dialog(getActivity());
        paymentsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        paymentsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        paymentsDialog.setContentView(dialogLayout);
        paymentsDialog.show();
    }

    //months view dialog
    public void checkMonthsDialog() {
        myDialog(R.layout.months_dialog);

        familyNameTextView = (TextView) dialogLayout.findViewById(R.id.months_family_name);
        familyNameTextView.setText(getResources().getString(R.string.familyAndSpace) + " " + currentClickedUserFamilyName);

        //check boxes container
        monthsCb = (RelativeLayout) dialogLayout.findViewById(R.id.months_cb);

        //pointers for months check boxes
        cb1 = (CheckedTextView) dialogLayout.findViewById(R.id.cb1);
        cb1.setOnClickListener(checkListener);
        cb2 = (CheckedTextView) dialogLayout.findViewById(R.id.cb2);
        cb2.setOnClickListener(checkListener);
        cb3 = (CheckedTextView) dialogLayout.findViewById(R.id.cb3);
        cb3.setOnClickListener(checkListener);
        cb4 = (CheckedTextView) dialogLayout.findViewById(R.id.cb4);
        cb4.setOnClickListener(checkListener);
        cb5 = (CheckedTextView) dialogLayout.findViewById(R.id.cb5);
        cb5.setOnClickListener(checkListener);
        cb6 = (CheckedTextView) dialogLayout.findViewById(R.id.cb6);
        cb6.setOnClickListener(checkListener);
        cb7 = (CheckedTextView) dialogLayout.findViewById(R.id.cb7);
        cb7.setOnClickListener(checkListener);
        cb8 = (CheckedTextView) dialogLayout.findViewById(R.id.cb8);
        cb8.setOnClickListener(checkListener);
        cb9 = (CheckedTextView) dialogLayout.findViewById(R.id.cb9);
        cb9.setOnClickListener(checkListener);
        cb10 = (CheckedTextView) dialogLayout.findViewById(R.id.cb10);
        cb10.setOnClickListener(checkListener);
        cb11 = (CheckedTextView) dialogLayout.findViewById(R.id.cb11);
        cb11.setOnClickListener(checkListener);
        cb12 = (CheckedTextView) dialogLayout.findViewById(R.id.cb12);
        cb12.setOnClickListener(checkListener);

        //setting marked or no for months display
        for (int i = 0; i < monthsCb.getChildCount(); i++) {
            boolean check;
            if (((months.get(i)).getList("paidBy")) == null) {
                check = false;
            } else {
                check = ((months.get(i)).getList("paidBy")).contains(currentClickedUserObjectId);
            }
            ((CheckedTextView) monthsCb.getChildAt(i)).setChecked(check);
            ((CheckedTextView) monthsCb.getChildAt(i)).setTypeface(check ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }

        cancelBtn = (Button) dialogLayout.findViewById(R.id.monthsCancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentsDialog.dismiss();
            }
        });

        markAllBtn = (Button) dialogLayout.findViewById(R.id.monthsMarkAllBtn);
        markAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!markAllBtnPressed){
                    //mark all
                    for (int i = 0; i < monthsCb.getChildCount(); i++) {
                        ((CheckedTextView) monthsCb.getChildAt(i)).setChecked(true);
                        ((CheckedTextView) monthsCb.getChildAt(i)).setTypeface(Typeface.DEFAULT_BOLD);
                    }
                    markAllBtn.setText(getResources().getString(R.string.clean));
                    markAllBtnPressed = true;
                }else{
                    //clear all
                    for (int i = 0; i < monthsCb.getChildCount(); i++) {
                        boolean check=false;
                        ((CheckedTextView) monthsCb.getChildAt(i)).setChecked(check);
                        ((CheckedTextView) monthsCb.getChildAt(i)).setTypeface(check ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    }
                    markAllBtn.setText(getResources().getString(R.string.mark_all));
                    markAllBtnPressed = false;
                }
            }
        });

        saveBtn = (Button) dialogLayout.findViewById(R.id.monthsKeepChangesBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List users = new ArrayList();
                for (int i = 0; i < monthsCb.getChildCount(); i++) {
                    ParseObject user = months.get(i);
                    if (((CheckedTextView) monthsCb.getChildAt(i)).isChecked()) {
                        user.addUnique("paidBy", currentClickedUserObjectId);
                    } else {
                        user.removeAll("paidBy", Arrays.asList(currentClickedUserObjectId));
                    }
                    users.add(user);
                }
                ParseObject.saveAllInBackground(users, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            paymentsDialog.dismiss();
                        } else {
                            Log.v("****PPPPARRRRSEEEE****", e.getLocalizedMessage());
                        }
                    }
                });
            }
        });

    }

    public View.OnClickListener checkListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ((CheckedTextView) view).setChecked(!((CheckedTextView) view).isChecked());
            ((CheckedTextView) view).setTypeface(((CheckedTextView) view).isChecked() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
    };

    public void getMonthsFromParse() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        query.whereEqualTo("buildingCode", buildingCode);
        query.whereEqualTo("paymentType", "vaad");
        query.addAscendingOrder("period");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> monthsServer, ParseException e) {
                if (e == null) {
                    months = monthsServer;
                    checkMonthsDialog();
                } else {
                    Log.v("********Parse**********", e.getLocalizedMessage());
                }

            }
        });
    }

    public void setPaymentDialogYear() {
        myDialog(R.layout.payments_set_vb_year);

        okDialog = (Button) dialogLayout.findViewById(R.id.paymentsDialogConfirmBtn);

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        String currentYear[] = {"" + year};
        yearValue = currentYear[0];

        npYear = (NumberPicker) dialogLayout.findViewById(R.id.paymentsDialogNumberPicker);
        //set max value for np
        final String[] years = new String[year - 2000 + 11];
        // set numbers of picker

        for (int i = years.length - 1; i >= 0; i--) {
            years[i] = Integer.toString(i * 1 + 2000);
        }

        npYear.setDisplayedValues(currentYear);
        npYear.setValue(year);
        //disable picking loop
        npYear.setWrapSelectorWheel(false);
        //disable keyboard pop up
        npYear.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        //number picker listener
        npYear.setOnValueChangedListener(new net.simonvt.numberpicker.NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(net.simonvt.numberpicker.NumberPicker picker, int oldVal, int newVal) {
                yearValue = years[picker.getValue()];
            }
        });
        npYear.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker picker, int i) {
                npYear.setDisplayedValues(years);
                npYear.setMaxValue(years.length - 1);
                npYear.setMinValue(0);
            }
        });

        okDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isExistPaymentForYear(yearValue);
            }
        });
    }

    public void setPaymentDialogMonth() {
        myDialog(R.layout.payments_set_vb_month);

        okDialog = (Button) dialogLayout.findViewById(R.id.paymentsDialogConfirmBtn);
        npMonth = (NumberPicker) dialogLayout.findViewById(R.id.paymentsDialogMonthNumberPicker);
        //set max value for np
        final String[] months = new String[2000 / 10];
        // set numbers of picker
        for (int i = 0; i < months.length; i++) {
            months[i] = Integer.toString(i * 10 + 10);
        }
        npMonth.setDisplayedValues(months);
        npMonth.setMaxValue(months.length - 1);
        npMonth.setMinValue(0);
        //disable picking loop
        npMonth.setWrapSelectorWheel(false);
        //disable keyboard pop up
        npMonth.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        //number picker listener
        npMonth.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                okDialog.setEnabled(true);
                monthlyValue = months[picker.getValue()];
            }
        });

        okDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createVaadBaitPayments(yearValue, monthlyValue);
            }
        });
    }

    /**
     * checks if there is payments existed for selected year, if no, open
     * next dialog of month
     */
    public void isExistPaymentForYear(final String yearValue) {
        String currentBuilding = db.getCurrentUserBuildingCode();
        ParseQuery query = ParseQuery.getQuery("payments");
        query.whereEqualTo("buildingCode", currentBuilding);
        query.whereEqualTo("paymentType", "vaad");
        query.whereEqualTo("year", yearValue);
        query.countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {
                if (e == null) {
                    //there's already payment for selected year
                    if (i != 0) {
                        Toast toast = Toast.makeText(getActivity(), R.string.vaad_bait_exists, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else {
                        //close current dialog
                        paymentsDialog.dismiss();
                        //open next dialog
                        setPaymentDialogMonth();
                    }
                } else {
                    Log.v("***PARSE ERROR***", e.getMessage());
                }
            }
        });
    }

    public void createVaadBaitPayments(final String yearValue, final String monthlyValue) {
        final String currentBuilding = db.getCurrentUserBuildingCode();
        List payments = new ArrayList();
        for (int i = 1; i <= 12; i++) {
            ParseObject payment = new ParseObject("payments");
            payment.put("buildingCode", currentBuilding);
            payment.put("amount", monthlyValue);
            payment.put("description", getResources().getString(R.string.vaad_bait));
            String periodi = (i < 10) ? "0" + i : "" + i;
            payment.put("period", periodi);
            payment.put("year", yearValue);
            payment.put("paymentType", "vaad");
            payments.add(payment);
        }
        ParseObject.saveAllInBackground(payments, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast toast = Toast.makeText(getActivity(), R.string.vaad_bait_success, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    paymentsDialog.dismiss();
                } else {
                    Toast toast = Toast.makeText(getActivity(), R.string.problem_text, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Log.v("***PARSE ERROR***",e.getMessage());
                }
            }
        });
    }

public void refreshPage(){
        this.getFragmentManager().beginTransaction().replace(R.id.pager,new BuildingPayments()).commit();
        }

        }



