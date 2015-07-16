package com.myvaad.myvaad;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;
import com.rey.material.widget.ProgressView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import adapters.UserExpensesAdapter;

public class UserExpenses extends Fragment implements DatePickerDialog.OnDateSetListener {
    private ParseDB db;
    private TextView userTotalExpensesTextView;
    int totalExpensesAmount = 0;
    private ListView listView;
    private UserExpensesAdapter customParseAdapter;
    ProgressView bar;
    FloatingActionButton addFilterBtn;
    FragmentActivity myContext;

    private int startYear;
    private int startMonthOfYear;
    private int startDayOfMonth;
    private int endYear;
    private int endMonthOfYear;
    private int endDayOfMonth;
    private String buildingCode;

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_expenses_layout, container, false);

        Calendar calendar = Calendar.getInstance();

        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonthOfYear = calendar.get(Calendar.MONTH);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        startYear = currentYear;
        startMonthOfYear = 0;
        startDayOfMonth = 1;

        endYear = currentYear;
        endMonthOfYear = currentMonthOfYear;
        endDayOfMonth = currentDayOfMonth;

        db = ParseDB.getInstance(getActivity());

        buildingCode = db.getCurrentUserBuildingCode();

        bar = (ProgressView) rootView.findViewById(R.id.progress_loader);

        userTotalExpensesTextView = (TextView) rootView.findViewById(R.id.userTotalExpensesAmount);
        userTotalExpensesTextView.setText("");

        // Initialize the subclass of ParseQueryAdapter
        customParseAdapter = new UserExpensesAdapter(getActivity(),buildingCode ,db.getCurrentUserObjectId());

        //disable Pagination
        customParseAdapter.setPaginationEnabled(false);

       //customParseAdapter.setObjectsPerPage(4);


        customParseAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoading() {
                //need to add loader here...
                bar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoaded(List<ParseObject> expenses, Exception e) {
                bar.setVisibility(View.GONE);
                if (e == null) {
                    calcExpenses(expenses);

                } else {
                    Log.d("***Exception***", e.getLocalizedMessage());
                }

            }
        });





        listView = (ListView) rootView.findViewById(R.id.userExpensesListview);
        listView.setAdapter(customParseAdapter);

        //floating add Button
        addFilterBtn = (FloatingActionButton) rootView.findViewById(R.id.add_filter_btn_user_expenses);
        addFilterBtn.attachToListView(listView);
        addFilterBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showFilterNotice();

            }
        });


        return rootView;
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        switch (view.getTag()){
            case "start":
                this.startYear = year;
                this.startMonthOfYear = monthOfYear;
                this.startDayOfMonth = dayOfMonth;
                showFilterNotice();
                break;
            case "end":
                this.endYear = year;
                this.endMonthOfYear = monthOfYear;
                this.endDayOfMonth = dayOfMonth;
                showFilterNotice();
                break;
        }
    }

    public void showDatePickerDialog(String dialogTag){
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                UserExpenses.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        dpd.setMaxDate(now);

        dpd.show(myContext.getFragmentManager(), dialogTag);
    }


    public void calcExpenses(List<ParseObject> expenses){
        totalExpensesAmount = 0;

        for (ParseObject expensesRow : expenses) {
            //get specific data from each row
            String amount = expensesRow.getString("amount");

            String paymentType = expensesRow.getString("paymentType");

            if(paymentType.equals("extra")){
                totalExpensesAmount += Integer.parseInt(amount)/expensesRow.getInt("houses");

            }else{
                totalExpensesAmount += Integer.parseInt(amount);
            }
        }
        userTotalExpensesTextView.setText(getActivity().getString(R.string.total) + " " + getActivity().getString(R.string.shekel) + totalExpensesAmount);
    }

    public void showFilterNotice() {

        final MaterialDialog dialogM = new MaterialDialog.Builder(getActivity())
                .title("סינון")
                .titleGravity(GravityEnum.START)
                .contentGravity(GravityEnum.START)
                .positiveColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .customView(R.layout.custom_filter_dialog, false) //false = can't scroll
                .negativeText("ביטול")
                .positiveText("סנן")
                .buttonsGravity(GravityEnum.END)
                .forceStacking(false)
                .btnStackedGravity(GravityEnum.END)
                .alwaysCallInputCallback() // this forces the callback to be invoked with every input change
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(startYear, startMonthOfYear, startDayOfMonth);
                        Date startDate = calendar.getTime();
                        calendar.set(endYear, endMonthOfYear, endDayOfMonth);
                        Date endDate = calendar.getTime();
                        if (startDate.before(endDate)) {
                            Log.d("***compere dates***", "start before end");
                            customParseAdapter = new UserExpensesAdapter(getActivity(),buildingCode, db.getCurrentUserObjectId(), startYear, startMonthOfYear, startDayOfMonth, endYear, endMonthOfYear, endDayOfMonth);
                            customParseAdapter.setPaginationEnabled(false);
                            customParseAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
                                @Override
                                public void onLoading() {
                                    //loader here...
                                    bar.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onLoaded(List<ParseObject> expenses, Exception e) {
                                    bar.setVisibility(View.GONE);
                                    if (e == null) {
                                        calcExpenses(expenses);
                                    } else {
                                        Log.d("***Exception***", e.getLocalizedMessage());
                                    }
                                }
                            });
                            listView.setAdapter(customParseAdapter);

                        } else {
                            Log.d("***compere dates***", "end before start");
                            showFilterNotice();
                            myToast("בחר תאריך התחלה קודם לתאריך סיום");

                        }

                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {

                    }

                })
                .show();

        View customDialogView = dialogM.getCustomView();
        final TextView startFilterTextView= (TextView) customDialogView.findViewById(R.id.startFilter);
        TextView endFilterTextView= (TextView) customDialogView.findViewById(R.id.endFilter);
        startFilterTextView.setText(this.startDayOfMonth + "." + (this.startMonthOfYear + 1) + "." + this.startYear);
        endFilterTextView.setText(this.endDayOfMonth+"."+(this.endMonthOfYear+1)+"."+this.endYear);
        startFilterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog("start");
                dialogM.dismiss();

            }
        });

        endFilterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog("end");
                dialogM.dismiss();

            }
        });
    }

    public void myToast(String content) {
        Toast toast = Toast.makeText(getActivity(), content, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 150);
        toast.show();
    }




}
