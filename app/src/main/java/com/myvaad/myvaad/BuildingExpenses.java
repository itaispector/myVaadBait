package com.myvaad.myvaad;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.Parse;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import adapters.BuildingExpensesAdapter;

public class BuildingExpenses extends Fragment {

    private ParseDB db;
    private BuildingExpensesAdapter customParseAdapter;
    private ListView listView;
    int totalExpensesAmount = 0;
    private TextView buildingTotalExpenses;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.building_expenses_layout, container, false);

        db = ParseDB.getInstance(getActivity());

        buildingTotalExpenses = (TextView) rootView.findViewById(R.id.buildingTotalExpensesAmount);
        buildingTotalExpenses.setText("");

        // Initialize the subclass of ParseQueryAdapter
        customParseAdapter = new BuildingExpensesAdapter(getActivity());

        customParseAdapter.setObjectsPerPage(4);

        // Initialize ListView and set initial view to mainAdapter
        listView = (ListView) rootView.findViewById(R.id.buildingExpensesListview);

        listView.setAdapter(customParseAdapter);
        customParseAdapter.loadObjects();


        ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
        //Query Constraints-->all users from specific building
        query.whereEqualTo("buildingCode", db.getCurrentUserBuildingCode());
        query.whereEqualTo("paymentType", "regular");
        //query.whereEqualTo("paymentApproved", true);
        query.orderByDescending("createdAt");

        //finding all payments for current user
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> payments, ParseException e) {
                if (e == null) {
                    for (ParseObject paymentRow : payments) {

                        //get specific data from each row
                        String amount = paymentRow.getString("amount");
                        totalExpensesAmount += Integer.parseInt(amount);

                    }

                    buildingTotalExpenses.setText(getActivity().getString(R.string.total) + " " + getActivity().getString(R.string.shekel) + totalExpensesAmount);

                } else {//ParseException
                    Log.e("***Parse Exception***", e.getLocalizedMessage());
                }
            }
        });

        return rootView;
    }

}



