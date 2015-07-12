package com.myvaad.myvaad;

import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import adapters.UserExpensesAdapter;

public class UserExpenses extends Fragment {
    private ParseDB db;
    private TextView userTotalExpensesTextView;
    int totalExpensesAmount = 0;
    private ListView listView;
    private UserExpensesAdapter customParseAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_expenses_layout, container, false);

        db = ParseDB.getInstance(getActivity());

        userTotalExpensesTextView = (TextView) rootView.findViewById(R.id.userTotalExpensesAmount);
        userTotalExpensesTextView.setText("");

        // Initialize the subclass of ParseQueryAdapter
        customParseAdapter = new UserExpensesAdapter(getActivity(), db.getCurrentUserObjectId());

        //disable Pagination
        customParseAdapter.setPaginationEnabled(false);

       //customParseAdapter.setObjectsPerPage(4);


        customParseAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoading() {
                //need to add loader here...
            }

            @Override
            public void onLoaded(List<ParseObject> expenses, Exception e) {
                if (e == null) {
                    // totalExpensesAmount =0;
                    Log.d("***inside onLoded***", totalExpensesAmount + "");
                    for (ParseObject expensesRow : expenses) {
                        //get specific data from each row
                        String amount = expensesRow.getString("amount");
                        totalExpensesAmount += Integer.parseInt(amount);
                        Log.d("***expense***", totalExpensesAmount + "");
                    }
                    userTotalExpensesTextView.setText(getActivity().getString(R.string.total) + " " + getActivity().getString(R.string.shekel) + totalExpensesAmount);

                } else {
                    Log.d("***Exception***", e.getLocalizedMessage());
                }

            }
        });

        listView = (ListView) rootView.findViewById(R.id.userExpensesListview);
        listView.setAdapter(customParseAdapter);

        return rootView;
    }


}
