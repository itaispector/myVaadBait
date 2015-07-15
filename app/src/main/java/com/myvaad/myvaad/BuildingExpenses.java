package com.myvaad.myvaad;


import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;
import com.rey.material.widget.ProgressView;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;


import android.app.Activity;
import android.content.Context;
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

import adapters.BuildingExpensesAdapter;

public class BuildingExpenses extends Fragment implements DatePickerDialog.OnDateSetListener{

    private ParseDB db;
    private BuildingExpensesAdapter customParseAdapter;
    private ListView listView;
    int totalExpensesAmount = 0;
    private TextView buildingTotalExpensesTextView;
    FloatingActionButton addFilterBtn;
    FragmentActivity myContext;
    ProgressView bar;

    private int startYear;
    private int startMonthOfYear;
    private int startDayOfMonth;
    private int endYear;
    private int endMonthOfYear;
    private int endDayOfMonth;


    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.building_expenses_layout, container, false);

        Calendar calendar = Calendar.getInstance();

        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonthOfYear = calendar.get(Calendar.MONTH);
        int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        startYear = currentYear;
        startMonthOfYear = currentMonthOfYear;
        startDayOfMonth = 1;

        endYear = currentYear;
        endMonthOfYear = currentMonthOfYear;
        endDayOfMonth = currentDayOfMonth;

        db = ParseDB.getInstance(getActivity());

        bar = (ProgressView) rootView.findViewById(R.id.progress_loader);


        buildingTotalExpensesTextView = (TextView) rootView.findViewById(R.id.buildingTotalExpensesAmount);

        buildingTotalExpensesTextView.setText("");

        // Initialize the subclass of ParseQueryAdapter
        customParseAdapter = new BuildingExpensesAdapter(getActivity(), startYear, startMonthOfYear, startDayOfMonth, endYear, endMonthOfYear, endDayOfMonth);

        //disable Pagination
        customParseAdapter.setPaginationEnabled(false);
        //page per page
       // customParseAdapter.setObjectsPerPage(4);


       // customParseAdapter.loadObjects();

        customParseAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoading() {
                // loader here...
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

        // Initialize ListView and set initial view to mainAdapter
        listView = (ListView) rootView.findViewById(R.id.buildingExpensesListview);

        listView.setAdapter(customParseAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Log.d("***item description***", customParseAdapter.getItem(position).getObjectId());
                ParseObject expenseObject = customParseAdapter.getItem(position);
                if(db.isCurrentUserAdmin()){
                    showDeleteNotice(expenseObject);
                }


            }
        });

        //floating add Button
        addFilterBtn = (FloatingActionButton) rootView.findViewById(R.id.add_filter_btn);
        addFilterBtn.attachToListView(listView);
        addFilterBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

               // showDatePickerDialog("start");
                showNotice();

            }
        });

        return rootView;
    }


    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Log.d("***Dialog Tag***",view.getTag());

        switch (view.getTag()){
            case "start":
                this.startYear = year;
                this.startMonthOfYear = monthOfYear;
                this.startDayOfMonth = dayOfMonth;
                showNotice();
                break;
            case "end":
                this.endYear = year;
                this.endMonthOfYear = monthOfYear;
                this.endDayOfMonth = dayOfMonth;
                showNotice();
                break;
        }


    }

    public void showDatePickerDialog(String dialogTag){
        Calendar now = Calendar.getInstance();
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                BuildingExpenses.this,
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
            totalExpensesAmount += Integer.parseInt(amount);
        }
        buildingTotalExpensesTextView.setText(getActivity().getString(R.string.total) + " " + getActivity().getString(R.string.shekel) + totalExpensesAmount);
    }

    public void showNotice() {

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
                            reloadListView();

                        } else {
                            showNotice();
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
        startFilterTextView.setText(this.startDayOfMonth+"."+(this.startMonthOfYear+1)+"."+this.startYear);
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

    public void showDeleteNotice(final ParseObject expenseObject) {

        String description = expenseObject.getString("description")+"  "+expenseObject.getString("amount")+" "+getActivity().getString(R.string.shekel);

         MaterialDialog dialogM = new MaterialDialog.Builder(getActivity())
                .title("מחיקת הוצאה")
                .content(description)
                .titleGravity(GravityEnum.START)
                .contentGravity(GravityEnum.START)
                .positiveColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .negativeText("ביטול")
                .positiveText("מחק")
                .buttonsGravity(GravityEnum.END)
                .forceStacking(false)
                .btnStackedGravity(GravityEnum.END)
                .alwaysCallInputCallback() // this forces the callback to be invoked with every input change
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        expenseObject.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                reloadListView();
                            }
                        });
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {

                    }

                })
                .show();
    }

    public void reloadListView(){
        customParseAdapter = new BuildingExpensesAdapter(getActivity(), startYear, startMonthOfYear, startDayOfMonth, endYear, endMonthOfYear, endDayOfMonth);
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
    }


}



