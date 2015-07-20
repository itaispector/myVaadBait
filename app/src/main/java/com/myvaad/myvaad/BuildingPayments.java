package com.myvaad.myvaad;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.parse.CountCallback;
import com.parse.DeleteCallback;
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

import net.simonvt.numberpicker.NumberPicker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import adapters.PaymentsAdminVaadBaitAdapter;
import adapters.PaymentsUserVaadBaitAdapter;
import dialogs.RingProgressDialog;

public class BuildingPayments extends Fragment {

    private ParseDB db;
    private TextView noPaymentsTextView, noPaymentsAdminTextView;
    private ViewFlipper viewFlipper;
    private ListView userListView, adminListView;
    private PaymentsAdminVaadBaitAdapter adminVaadBaitAdapter;
    private PaymentsUserVaadBaitAdapter paymentsUserVaadBaitAdapter;
    private View dialogLayout;
    private Dialog paymentsDialog;
    private NumberPicker npYear, npMonth;
    private FloatingActionButton addPaymentBtn;
    private Button okDialog, saveBtn, markAllBtn, cancelBtn, yearButton, payBtn;
    private String yearValue, monthlyValue, currentClickedUserObjectId, currentClickedUserFamilyName, buildingCode, vaadPayPalAccount;
    private CheckedTextView cb1, cb2, cb3, cb4, cb5, cb6, cb7, cb8, cb9, cb10, cb11, cb12;
    private RelativeLayout monthsCb;
    private List<ParseObject> months;
    private TextView familyNameTextView, paymentTitle, paymentPrice, paymentYear;
    private boolean markAllBtnPressed = false;
    private int year, selectedYear;
    private String[] years;
    private ProgressView loader;
    private ImageView trashBtn, notiBtn;
    private ParseObject payment;
    private int total;
    private ArrayList objectIds = new ArrayList<>();
    private boolean currentClickedUserPaidAll = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Parse.initialize(getActivity());
        db = ParseDB.getInstance(getActivity());
        View rootView = inflater.inflate(R.layout.payments_vaad_bait_layout, container, false);

        if (getActivity().getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        loader = (ProgressView) rootView.findViewById(R.id.progress_loader);

        buildingCode = db.getCurrentUserBuildingCode();
        viewFlipper = (ViewFlipper) rootView.findViewById(R.id.paymentsVaadBaitViewFlipper);

        // get current year
        getCurrentYear();

        noPaymentsTextView = (TextView) rootView.findViewById(R.id.no_payments_text);
        noPaymentsAdminTextView = (TextView) rootView.findViewById(R.id.no_payments_text_admin);

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
                    currentClickedUserPaidAll = adminVaadBaitAdapter.getPaid(i);
                    getMonthsFromParse();
                }
            });
        }
        if (!db.isCurrentUserAdmin()) {
            userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    payment = paymentsUserVaadBaitAdapter.getItem(i);
                    isExistPaypalAccount(false);
                }
            });
        }


        addPaymentBtn = (FloatingActionButton) rootView.findViewById(R.id.add_payment_btn);
        if (db.isCurrentUserAdmin()) {
            addPaymentBtn.attachToListView(adminListView);
        } else {
            addPaymentBtn.attachToListView(userListView);
        }
        addPaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if current user is admin open set payment dialog
                if (db.isCurrentUserAdmin()) {
                    setPaymentDialogYear();
                } else {
                    // clicked by user, open pay all dialog
                    isExistPaypalAccount(true);
                }

            }
        });

        yearButton = (Button) rootView.findViewById(R.id.currentDisplayedYear);
        yearButton.setText(year + "");
        yearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loader.setVisibility(View.VISIBLE);
                // add existing years to dialog choice list
                final List<String> tmp = new ArrayList<>();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
                query.whereEqualTo("paymentType", "vaad");
                query.orderByAscending("year");
                query.addAscendingOrder("period");
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (e == null) {
                            for (int i = 0; i < list.size(); i = i + 12) {
                                tmp.add(list.get(i).getString("year"));
                            }
                            years = new String[tmp.size()];
                            tmp.toArray(years);
                            loader.setVisibility(View.GONE);
                            // found results open dialog with years
                            if (!tmp.isEmpty()) {
                                new MaterialDialog.Builder(getActivity())
                                        .title(R.string.choose_year)
                                        .titleGravity(GravityEnum.END)
                                        .items(years)
                                        .itemsGravity(GravityEnum.END)
                                        .itemsCallback(new MaterialDialog.ListCallback() {
                                            @Override
                                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                yearButton.setText(years[which] + "");
                                                selectedYear = Integer.parseInt(years[which]);
                                                // if is admin, call list view of users for selected year
                                                if (db.isCurrentUserAdmin()) {
                                                    loadUserListViewForAdmin();
                                                } else {
                                                    // call user's list view of payments for selected year
                                                    loadUserListView();
                                                }

                                            }
                                        })
                                        .show();
                            } else {
                                noResultsDialog();
                            }


                        } else {
                            Log.v("*****PARSE ERROR*****", e.getMessage());
                        }


                    }
                });
            }
        });

        notiBtn = (ImageView) rootView.findViewById(R.id.notificationToolBar);
        notiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (noPaymentsAdminTextView.isShown()) {
                    noResultsDialog();
                } else {
                    new MaterialDialog.Builder(getActivity())
                            .content(getResources().getString(R.string.send_notification_vaad) + " " + selectedYear + "?")
                            .contentGravity(GravityEnum.END)
                            .positiveText(R.string.yes)
                            .positiveColorRes(R.color.colorPrimary)
                            .negativeText(R.string.no)
                            .negativeColorRes(R.color.colorPrimary)
                            .buttonsGravity(GravityEnum.END)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    HashMap<String, Object> params = new HashMap<String, Object>();
                                    List usersObjectId = new ArrayList<>();
                                    List familyNames = new ArrayList<>();
                                    for (int i = 0; i < adminVaadBaitAdapter.getCount(); i++) {
                                        // if user didn't pay yet, add him to list of users who didn't pay,
                                        // and notify them
                                        if (!adminVaadBaitAdapter.getPaid(i)) {
                                            String userObjectId = (adminVaadBaitAdapter.getItem(i)).getObjectId();
                                            String familyName = (adminVaadBaitAdapter.getItem(i)).getString("familyName");
                                            usersObjectId.add(userObjectId);
                                            familyNames.add(familyName);
                                        }
                                    }
                                    params.put("usersObjectIds", usersObjectId);
                                    params.put("familyNames", familyNames);
                                    params.put("msg", getString(R.string.notification_msg_vaad_bait)+ " " +selectedYear);
                                    ParseCloud.callFunctionInBackground("sendNotificationToUnPaidUsers", params, new FunctionCallback<String>() {
                                        public void done(String result, ParseException e) {
                                            if (e == null) {
                                                mToast(getString(R.string.noti_success));
                                            } else {
                                                mToast(getString(R.string.noti_failure));
                                                mToast(e.getMessage());
                                            }
                                        }
                                    });

                                }
                            })
                            .show();
                }
            }
        });

        trashBtn = (ImageView) rootView.findViewById(R.id.trashToolBar);
        // if is user, hide trash btn & noti Btn
        if (!db.isCurrentUserAdmin()) {
            trashBtn.setVisibility(View.GONE);
            notiBtn.setVisibility(View.GONE);
        }
        trashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (noPaymentsAdminTextView.isShown()) {
                    noResultsDialog();
                } else {
                    // delete all payments for this year
                    new MaterialDialog.Builder(getActivity())
                            .content(getResources().getString(R.string.delete_vaad_bait_payments) + " " + selectedYear + "?")
                            .contentGravity(GravityEnum.END)
                            .positiveText(R.string.yes)
                            .positiveColorRes(R.color.colorPrimary)
                            .negativeText(R.string.no)
                            .negativeColorRes(R.color.colorPrimary)
                            .buttonsGravity(GravityEnum.END)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    ParseQuery.getQuery("payments")
                                            .whereEqualTo("year", selectedYear + "")
                                            .findInBackground(new FindCallback<ParseObject>() {
                                                @Override
                                                public void done(List<ParseObject> list, ParseException e) {
                                                    if (e == null) {
                                                        ParseObject.deleteAllInBackground(list, new DeleteCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if (e == null) {
                                                                    loadUserListViewForAdmin();
                                                                } else {
                                                                    addLog(e.getMessage());
                                                                }
                                                            }
                                                        });

                                                    } else {
                                                        mToast(e.getMessage());
                                                    }

                                                }
                                            });

                                }
                            })
                            .show();
                }


            }
        });

        getActivity().setTitle(R.string.PaymentsTitle);
        setHasOptionsMenu(true);

        return rootView;
    }

    public void loadUserListView() {
        loader.setVisibility(View.VISIBLE);
        String buildingCode = db.getCurrentUserBuildingCode();
        String userObjectId = db.getCurrentUserObjectId();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        query.whereEqualTo("buildingCode", buildingCode);
        query.whereEqualTo("paymentType", "vaad");
        query.whereEqualTo("year", selectedYear + "");
        query.whereNotEqualTo("paidBy", userObjectId);
        query.orderByAscending("period");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> payments, ParseException e) {
                if (payments.isEmpty()) {
                    // turn off loader
                    loader.setVisibility(View.GONE);
                    // show no payments view
                    userListView.setVisibility(View.GONE);
                    noPaymentsTextView.setText(getString(R.string.no_vaad_bait_payments) + " " + selectedYear);
                    noPaymentsTextView.setVisibility(View.VISIBLE);
                    addPaymentBtn.setVisibility(View.GONE);
                } else {
                    noPaymentsTextView.setVisibility(View.GONE);
                    paymentsUserVaadBaitAdapter = new PaymentsUserVaadBaitAdapter(getActivity(), payments);
                    userListView.setVisibility(View.VISIBLE);
                    userListView.setAdapter(paymentsUserVaadBaitAdapter);
                    loader.setVisibility(View.GONE);
                    addPaymentBtn.setVisibility(View.VISIBLE);
                }

            }
        });
    }

    public void loadUserListViewForAdmin() {
        loader.setVisibility(View.VISIBLE);
        // first get users list for current building
        final String buildingCode = db.getCurrentUserBuildingCode();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
        query.whereEqualTo("buildingCode", buildingCode);
        query.orderByAscending("familyName");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> usersList, ParseException e) {
                if (e == null) {
                    // second, get paid all data for each user in building,
                    // also finds out if there's no payments for that year
                    ParseQuery<ParseObject> queryB = ParseQuery.getQuery("payments");
                    queryB.whereEqualTo("buildingCode", buildingCode);
                    queryB.whereEqualTo("year", selectedYear + "");
                    queryB.whereEqualTo("period", "01");
                    queryB.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject paidAllList, ParseException e) {
                            if (e == null) {
                                List paidAll = null;
                                loader.setVisibility(View.GONE);
                                noPaymentsAdminTextView.setVisibility(View.GONE);
                                if (paidAllList.getList("paidAll") != null) {
                                    paidAll = paidAllList.getList("paidAll");
                                }
                                adminVaadBaitAdapter = new PaymentsAdminVaadBaitAdapter(getActivity(), usersList, paidAll);
                                adminListView.setVisibility(View.VISIBLE);
                                adminListView.setAdapter(adminVaadBaitAdapter);
                            } else {
                                loader.setVisibility(View.GONE);
                                adminListView.setVisibility(View.GONE);
                                // show no payments view
                                noPaymentsAdminTextView.setText(getString(R.string.no_vaad_bait_payments) + " " + selectedYear);
                                noPaymentsAdminTextView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        });

    }

    //dialog template
    public void myDialog(int layout_name) {
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
        // sets mark all button state
        markAllBtn.setText(currentClickedUserPaidAll ? getString(R.string.clean) : getString(R.string.mark_all));
        markAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnState();
            }
        });

        saveBtn = (Button) dialogLayout.findViewById(R.id.monthsKeepChangesBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentsDialog.dismiss();
                loader.setVisibility(View.VISIBLE);
                List users = new ArrayList();
                // counter to check if all months paid, then mark user as paid all
                int counter = 0;
                for (int i = 0; i < monthsCb.getChildCount(); i++) {
                    ParseObject user = months.get(i);
                    if (((CheckedTextView) monthsCb.getChildAt(i)).isChecked()) {
                        user.addUnique("paidBy", currentClickedUserObjectId);
                        counter++;
                    } else {
                        user.removeAll("paidBy", Arrays.asList(currentClickedUserObjectId));
                    }
                    users.add(user);
                }
                // if paid all saves information in period 01 of selected year
                if (counter == 12) {
                    ParseObject user = months.get(0);
                    user.addUnique("paidBy", currentClickedUserObjectId);
                    user.addUnique("paidAll", currentClickedUserObjectId);
                    users.set(0, user);
                } else {
                    ParseObject user = months.get(0);
                    user.removeAll("paidAll", Arrays.asList(currentClickedUserObjectId));
                    users.set(0, user);
                }
                ParseObject.saveAllInBackground(users, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            //paymentsDialog.dismiss();
                            loadUserListViewForAdmin();
                        } else {
                            Log.v("****PPPPARRRRSEEEE****", e.getLocalizedMessage());
                        }
                    }
                });
            }
        });

    }

    // sets mark all button state according to the data and events
    public void btnState() {
        if (!markAllBtnPressed) {
            //mark all
            for (int i = 0; i < monthsCb.getChildCount(); i++) {
                ((CheckedTextView) monthsCb.getChildAt(i)).setChecked(true);
                ((CheckedTextView) monthsCb.getChildAt(i)).setTypeface(Typeface.DEFAULT_BOLD);
            }
            markAllBtn.setText(getResources().getString(R.string.clean));
            markAllBtnPressed = true;
        } else {
            //clear all
            for (int i = 0; i < monthsCb.getChildCount(); i++) {
                ((CheckedTextView) monthsCb.getChildAt(i)).setChecked(false);
                ((CheckedTextView) monthsCb.getChildAt(i)).setTypeface(Typeface.DEFAULT);
            }
            markAllBtn.setText(getResources().getString(R.string.mark_all));
            markAllBtnPressed = false;
        }
    }

    public View.OnClickListener checkListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ((CheckedTextView) view).setChecked(!((CheckedTextView) view).isChecked());
            ((CheckedTextView) view).setTypeface(((CheckedTextView) view).isChecked() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
    };

    public void getMonthsFromParse() {
        loader.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        query.whereEqualTo("buildingCode", buildingCode);
        query.whereEqualTo("paymentType", "vaad");
        query.whereEqualTo("year", selectedYear + "");
        query.addAscendingOrder("period");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> monthsServer, ParseException e) {
                if (e == null) {
                    months = monthsServer;
                    checkMonthsDialog();
                    loader.setVisibility(View.GONE);
                } else {
                    Log.v("********Parse**********", e.getLocalizedMessage());
                }

            }
        });
    }

    public void setPaymentDialogYear() {
        myDialog(R.layout.payments_set_vb_year);

        okDialog = (Button) dialogLayout.findViewById(R.id.paymentsDialogConfirmBtn);

        npYear = (NumberPicker) dialogLayout.findViewById(R.id.paymentsDialogNumberPicker);

        String currentYear[] = {"" + year};
        yearValue = currentYear[0];

        //set max value for np
        final String[] years = new String[2101 - year];

        // set numbers of picker
        for (int i = 0; i < years.length; i++) {
            years[i] = "" + (year + i);
        }

        npYear.setDisplayedValues(years);
        npYear.setMaxValue(years.length - 1);
        npYear.setMinValue(0);

        // disable picking loop
        npYear.setWrapSelectorWheel(true);
        // disable keyboard pop up
        npYear.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        // number picker listener
        npYear.setOnValueChangedListener(new net.simonvt.numberpicker.NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(net.simonvt.numberpicker.NumberPicker picker, int oldVal, int newVal) {
                yearValue = years[picker.getValue()];
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

    private void paymentDialog() {
        loader.setVisibility(View.GONE);
        myDialog(R.layout.single_payment_dialog);
        paymentTitle = (TextView) dialogLayout.findViewById(R.id.paymentTitle);
        paymentPrice = (TextView) dialogLayout.findViewById(R.id.paymentPrice);
        payBtn = (Button) dialogLayout.findViewById(R.id.payBtn);
        String name = payment.getString("description");
        String period = payment.getString("period");
        String month = getResources().getString(R.string._month);
        String paymentPriceString = payment.getString("amount");
        paymentTitle.setText(name + " - \n" + month + " " + period);
        paymentPrice.setText(getResources().getString(R.string.shekel) + " " + paymentPriceString);
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pay();
            }
        });
    }

    /**
     * checks if there is payments existed for selected year, if no, open
     * next dialog of month
     */
    public void isExistPaymentForYear(final String yearValue) {
        paymentsDialog.dismiss();
        final RingProgressDialog ringLoader = new RingProgressDialog(getActivity());
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
                        ringLoader.dismiss();
                        Toast toast = Toast.makeText(getActivity(), R.string.vaad_bait_exists, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        setPaymentDialogYear();
                    } else {
                        ringLoader.dismiss();
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
                    loadUserListViewForAdmin();
                } else {
                    Toast toast = Toast.makeText(getActivity(), R.string.problem_text, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Log.v("***PARSE ERROR***", e.getMessage());
                }
            }
        });
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

    private void payAllDialog() {
        // inflate list of all existing payments
        LinearLayout myView = new LinearLayout(getActivity());
        myView.setOrientation(LinearLayout.VERTICAL);
        total = 0;
        for (int i = 0; i < paymentsUserVaadBaitAdapter.getCount(); i++) {
            View inflation = View.inflate(getActivity(), R.layout.pay_all_item, null);
            TextView pName = (TextView) inflation.findViewById(R.id.list_item_paymentName);
            TextView pAmount = (TextView) inflation.findViewById(R.id.list_item_paymentAmount);
            String name = ((paymentsUserVaadBaitAdapter.getItem(i)).getString("description"));
            String period = ((paymentsUserVaadBaitAdapter.getItem(i)).getString("period"));
            String month = getResources().getString(R.string._month);
            pName.setText(name + " " + " - " + month + " " + period);
            String amount = ((paymentsUserVaadBaitAdapter.getItem(i)).getString("amount"));
            pAmount.setText(getResources().getString(R.string.shekel) + " " + amount);
            myView.addView(inflation);
            total += Integer.parseInt(amount);
            objectIds.add((paymentsUserVaadBaitAdapter.getItem(i)).getObjectId());
        }
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

    public void noResultsDialog() {
        new MaterialDialog.Builder(getActivity())
                .content(R.string.no_year_found)
                .contentGravity(GravityEnum.END)
                .positiveText(R.string.close)
                .positiveColorRes(R.color.colorPrimary)
                .btnStackedGravity(GravityEnum.START)
                .forceStacking(true)
                .show();
    }

    public void pay() {
        Intent i = new Intent(getActivity(), PayPalActivity.class);
        i.putExtra("amount", payment.getString("amount"));
        String pName = payment.getString("description");
        String pMonth = payment.getString("period");
        String month = getResources().getString(R.string._month);
        i.putExtra("paymentName", pName + " - " + month + " " + pMonth);
        i.putExtra("paymentObjectId", payment.getObjectId());
        i.putExtra("userObjectId", db.getCurrentUserObjectId());
        i.putExtra("email", vaadPayPalAccount);
        i.putExtra("selectedYear", selectedYear+"");
        i.putExtra("buildingCode", db.getCurrentUserBuildingCode());
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
        i.putExtra("selectedYear", selectedYear+"");
        i.putExtra("buildingCode", db.getCurrentUserBuildingCode());
        this.startActivity(i);
    }

    public void refreshPage() {
        this.getFragmentManager().beginTransaction().replace(R.id.pager, new BuildingPayments()).commit();
    }

    public void getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        selectedYear = year;
    }

    private void mToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    private void addLog(String msg) {
        Log.v("******PARSE*******", msg);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!db.isCurrentUserAdmin()){
            loadUserListView();
        }
    }
}



