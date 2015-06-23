package com.myvaad.myvaad;

import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;
import com.parse.Parse;
import com.parse.ParseQuery;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class UserExpenses extends Fragment {
	ParseDB db;
	int usersInBuilding = 5;
	
	  @Override
	   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	       View rootView =inflater.inflate(R.layout.user_expenses_layout,container,false);
	        String appId="QdwF666zm76ORQcn4KF6JNwDfsb6cj97QunbpT1s";
	        String clientId="OiJI3KdONEN9jML6Mi6r6iQTpR8mIOBv3YgsUhdv";
	        //Initialize with keys
	        Parse.initialize(getActivity(), appId, clientId);
	    	db=ParseDB.getInstance(getActivity());
	    	
	    	//simple query
	        //ParseQueryAdapter<ParseObject> adapter = new ParseQueryAdapter<ParseObject>(getActivity(), "noticeBoard");
	    	
	    	//adapter with defined query
	    	ParseQueryAdapter<ParseObject> adapter = new ParseQueryAdapter<ParseObject>(getActivity(), new ParseQueryAdapter.QueryFactory<ParseObject>() {

				@Override
				public ParseQuery<ParseObject> create() {
					//Define query
					ParseQuery query = new ParseQuery("payments");
					query.whereEqualTo("buildingCode", "239250");
					query.whereEqualTo("paymentType", "vaad");
					query.whereEqualTo("paymentApproved", true);
					query.whereEqualTo("paidBy", db.getCurrentUserObjectId());
					query.orderByDescending("createdAt");
					return query;
				}
			}){
	    		@Override
	    		public View getItemView(ParseObject object, View v, ViewGroup parent) {
	    		  if (v == null) {
	    		    v = View.inflate(getContext(), R.layout.expenses_adapter_item, null);
	    		  }

	    		  // Take advantage of ParseQueryAdapter's getItemView logic for
	    		  // populating the main TextView/ImageView.
	    		  // The IDs in your custom layout must match what ParseQueryAdapter expects
	    		  // if it will be populating a TextView or ImageView for you.
	    		  //super.getItemView(object, v, parent);

	    		  // Do additional configuration before returning the View.
	    		  TextView descriptionView = (TextView) v.findViewById(R.id.expenseDescription);
	    		  descriptionView.setText(object.getString("description"));
	    		  TextView amountView = (TextView)v.findViewById(R.id.expenseAmount);
	    		  String thisExpense = object.getString("amount");
	    		  amountView.setText(getActivity().getString(R.string.shekel)+thisExpense);
	    		  
	    		  
	    		  TextView createTimeView = (TextView)v.findViewById(R.id.expenseCreatTime);
	    		  String time = object.getCreatedAt().toLocaleString();
	    		  createTimeView.setText(time);
	    		  
	    		  
	    		  return v;
	    		  
	    		  
	    		}
	    	};

	    	//adapter.setTextKey("content");
	    	
	        //adapter.setImageKey("userPic");

	        ListView listView = (ListView) rootView.findViewById(R.id.userExpensesListview);
	        listView.setAdapter(adapter);
	        
	        TextView buildingTotalExpenses = (TextView)rootView.findViewById(R.id.userTotalExpensesAmount);
	    	buildingTotalExpenses.setText( getActivity().getString(R.string.total)+" "+ getActivity().getString(R.string.shekel)+ db.getCurrentUserTotalExpenses());
	    	
	        return rootView;
	   }

}
