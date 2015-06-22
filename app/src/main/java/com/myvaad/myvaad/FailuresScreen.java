package com.myvaad.myvaad;



import java.util.List;

import com.parse.Parse;

import adapters.FailuresAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class FailuresScreen extends Fragment {

    ImageView imageView1;
    ListView failuresList;
    FailuresAdapter adapter;
    TextView content,dialogTitle;
    EditText titleEdit,contentEdit,prof,amount;
    View dialogLayout;
    Dialog failuresDialog,failuresAddDialog,failuresPriceDialog;
    Button addfailure,dialogFailureOkBtn,dialogFailureCancelBtn,add,edit,approval,delete,moveToPaymentsBtn,approveOkBtn,approveCancelBtn;
    ImageView addFailureBtn;
	int position;
    ParseDB db;
    String title, failureContent="", approvals,myList="", failureObjectId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	String appId="QdwF666zm76ORQcn4KF6JNwDfsb6cj97QunbpT1s";
        String clientId="OiJI3KdONEN9jML6Mi6r6iQTpR8mIOBv3YgsUhdv";
        //Initialize with keys
        Parse.initialize(getActivity(), appId, clientId);
    	db=ParseDB.getInstance(getActivity());
    	View rootView = inflater.inflate(R.layout.failures_screen, container, false);
        getActivity().setTitle(R.string.FailuresScreenTitle);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        
        //calls the list view and its adapter
        failuresList=(ListView)rootView.findViewById(R.id.FailuresListView);
        adapter =  new FailuresAdapter(getActivity(), db.getCurrentUserFailuresBoard(), db.isCurrentUserAdmin(), db.getcurrentUserFamilyName());
        failuresList.setAdapter(adapter);
        
        //adds a listener to item in the listview
        failuresList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int idx, long id) {
                position=idx;
                //toggles between open & closed box                
                adapter.setState(adapter.getState(idx)?false:true, idx);
                
                //add button listener
                add=(Button)item.findViewById(R.id.FailuresRowAddBtn);
                add.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						addBidPrice();						
					}
				});
                //edit button listener
                edit=(Button)item.findViewById(R.id.FailuresRowEditBtn);
                edit.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						editBidPrice();
						
					}
				});
                //approval button listener
                approval=(Button)item.findViewById(R.id.FailuresRowStatusBtn);
                approval.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						showApprovals();
						
					}
				});
                //delete button listener
                delete=(Button)item.findViewById(R.id.FailuresRowDeleteBtn);
                delete.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						deleteFailure();
						
					}
				});
              //approveOk button listener
                approveOkBtn=(Button)item.findViewById(R.id.FailuresRowApproveBtn);
                approveOkBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						db.updateFailureApprovedByCurrentUser(adapter.getObjectId(position));
						refreshFailures();
					}
				});
              //approveCancel button listener
                approveCancelBtn=(Button)item.findViewById(R.id.FailuresRowCancelBtn);
                approveCancelBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						db.removeFailureApprovedBy(db.getcurrentUserFamilyName(), adapter.getObjectId(position));
						refreshFailures();
						
					}
				});
        
            }
        });
            
        //add failure button pointer and listener
        addFailureBtn=(ImageView)rootView.findViewById(R.id.add_f_btnn);
        addFailureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFailure();
            }
        });
        return rootView;
    }
    
    //opens add failure dialog
    public void addFailure(){
    	dialogLayout = View.inflate(getActivity(), R.layout.failures_dialog_layout, null);
    	failuresDialog = new Dialog(getActivity());
    	failuresDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	failuresDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    	failuresDialog.setContentView(dialogLayout);   	
    	failuresDialog.show();

        dialogFailureCancelBtn=(Button)dialogLayout.findViewById(R.id.failuresDialogFailureCancelBtn);
        dialogFailureOkBtn=(Button)dialogLayout.findViewById(R.id.failuresDialogFailureOkBtn);

        dialogFailureOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogButtonsResult(v);
            }
        });
        dialogFailureCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	DialogButtonsResult(v);
            }
        });        
    }
    
    public void DialogButtonsResult(View v){
    	titleEdit = (EditText) dialogLayout.findViewById(R.id.failuresDialogFailureName);
    	contentEdit = (EditText) dialogLayout.findViewById(R.id.failuresDialogFailureData);
        title = titleEdit.getText().toString();
    	failureContent = contentEdit.getText().toString();
    	if(v.getId()==R.id.failuresDialogFailureCancelBtn){
        	failuresDialog.dismiss();
    	}else{
        	if(title.matches("") || failureContent.matches("")){
            	Toast toast=Toast.makeText(getActivity(),R.string.empty_notice,Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            	}else{
            	db.updateNewfailure(title,failureContent);
                refreshFailures();
                failuresDialog.dismiss();
            	}
    	}
    }
    
    public void addBidPrice(){
    	dialogLayout = View.inflate(getActivity(), R.layout.failures_add_dialog_layout, null);
    	failuresAddDialog = new Dialog(getActivity());
    	failuresAddDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	failuresAddDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    	failuresAddDialog.setContentView(dialogLayout);   	
    	failuresAddDialog.show();
    	
    	dialogFailureOkBtn = (Button)failuresAddDialog.findViewById(R.id.failuresAddDialogFailureOkBtn);
    	dialogFailureCancelBtn = (Button)failuresAddDialog.findViewById(R.id.failuresAddDialogFailureCancelBtn);
    	
    	dialogFailureCancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				failuresAddDialog.dismiss();
				
			}
		});
    	
    	dialogFailureOkBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				prof=(EditText)failuresAddDialog.findViewById(R.id.failuresAddDialogBusinessName);
				amount=(EditText)failuresAddDialog.findViewById(R.id.failuresAddDialogPrice);			
				title=prof.getText().toString();
				failureContent=amount.getText().toString();
				if(title.matches("") || failureContent.matches("")){
	            	Toast toast=Toast.makeText(getActivity(),R.string.empty_notice,Toast.LENGTH_SHORT);
	                toast.setGravity(Gravity.CENTER, 0, 0);
	                toast.show();
	            	}else{
	            	db.updateFailureBid(failureContent, title, adapter.getObjectId(position),true);
	            	refreshFailures();
	                failuresAddDialog.dismiss();
	            	}			
			}
		});
    }
    
    public void editBidPrice(){
    	dialogLayout = View.inflate(getActivity(), R.layout.failures_add_dialog_layout, null);
    	failuresAddDialog = new Dialog(getActivity());
    	failuresAddDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	failuresAddDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    	failuresAddDialog.setContentView(dialogLayout);   	
    	failuresAddDialog.show();
    	
    	dialogTitle=(TextView)failuresAddDialog.findViewById(R.id.failuresAddDialogTitle);
    	dialogTitle.setText(R.string.dialogTitle);
    	
    	prof=(EditText)failuresAddDialog.findViewById(R.id.failuresAddDialogBusinessName);
		amount=(EditText)failuresAddDialog.findViewById(R.id.failuresAddDialogPrice);
		prof.setText(""+((List) adapter.getItem(position)).get(4));
		amount.setText(""+((List) adapter.getItem(position)).get(3));
    	
    	dialogFailureOkBtn = (Button)failuresAddDialog.findViewById(R.id.failuresAddDialogFailureOkBtn);
    	dialogFailureCancelBtn = (Button)failuresAddDialog.findViewById(R.id.failuresAddDialogFailureCancelBtn);
    	
    	dialogFailureCancelBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				failuresAddDialog.dismiss();
				
			}
		});
    	
    	dialogFailureOkBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				prof=(EditText)failuresAddDialog.findViewById(R.id.failuresAddDialogBusinessName);
				amount=(EditText)failuresAddDialog.findViewById(R.id.failuresAddDialogPrice);				
				title=prof.getText().toString();
				failureContent=amount.getText().toString();
				if(title.matches("") || failureContent.matches("")){
	            	Toast toast=Toast.makeText(getActivity(),R.string.empty_notice,Toast.LENGTH_SHORT);
	                toast.setGravity(Gravity.CENTER, 0, 0);
	                toast.show();
	            	}else{
	            	db.updateFailureBid(failureContent, title, adapter.getObjectId(position),false);
	                refreshFailures();
	                failuresAddDialog.dismiss();
	            	}		
			}
		});
    }
   
    public void showApprovals(){
    	dialogLayout = View.inflate(getActivity(), R.layout.failures_approval_dialog_layout, null);
    	failuresAddDialog = new Dialog(getActivity());
    	failuresAddDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    	failuresAddDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    	failuresAddDialog.setContentView(dialogLayout);   	
    	failuresAddDialog.show();
    	
    	content=(TextView)failuresAddDialog.findViewById(R.id.failuresApprovalDialogList);
    	
    	for(int i=0;i<adapter.getApprovers(position).size();i++){
    		myList+=("משפחת "+adapter.getApprovers(position).get(i)+"\n");
    	}
    	content.setText(myList);
    	dialogFailureCancelBtn = (Button)failuresAddDialog.findViewById(R.id.failuresApprovalDialogFailureCancelBtn); 
    	moveToPaymentsBtn = (Button)failuresAddDialog.findViewById(R.id.failuresApprovalDialogFailureMoveToPaymentsBtn);
    	//check who is the user and set view
    	if(!db.isCurrentUserAdmin()){
    		moveToPaymentsBtn.setVisibility(View.GONE);
    		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)dialogFailureCancelBtn.getLayoutParams();
    		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    		dialogFailureCancelBtn.setLayoutParams(layoutParams);
    	}
    	
    	   	
    	//adds a listener to close button
    	dialogFailureCancelBtn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				failuresAddDialog.dismiss();
			}
		});
    	//adds a listener to close & move to payments button
    	moveToPaymentsBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//close & move to payments db method
				failureObjectId = adapter.getObjectId(position);
				db.setFailureInactive(failureObjectId);
				db.createPaymentFromFailure(failureObjectId);
				failuresAddDialog.dismiss();	
				refreshFailures();
			}
		});
    	//add a listener for dismiss (close) this dialog
    	failuresAddDialog.setOnDismissListener(new OnDismissListener() {
			
			public void onDismiss(DialogInterface dialog) {
				myList="";				
			}
		});
    	
    }
    
    public void deleteFailure(){
        	AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        	dialog.setMessage(R.string.deleteFailure);

        	dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    				db.deleteFailure(adapter.getObjectId(position));
    				refreshFailures();
    			}
        	});

        	dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {			
    			public void onClick(DialogInterface dialog, int which) {
    	
    			}
    		});

        	dialog.show();
    }
    
    //refresh loading the failures in the adapter
    public void refreshFailures(){
    	Fragment fragment1 = new FailuresScreen();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment1).commit();
    }
    
    public void myToast(String s){
    	Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
    }
    

}


