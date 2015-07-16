package com.myvaad.myvaad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import dialogs.RingProgressDialog;

public class PayPalActivity extends Activity {
	// Can be NO_NETWORK for OFFLINE, SANDBOX for TESTING and LIVE for PRODUCTION
	private static final String CONFIG_ENVIRONMENT = PaymentActivity.ENVIRONMENT_SANDBOX;
	// note that these credentials will differ between live & sandbox environments.
	private static final String CONFIG_CLIENT_ID = "AbhfVksoFRWtblCZieeunoPFYxnazV44SWX9dgxBluRJ0Fsf10Dp8Fz8C1IqT2bRZN3A68-0mPu-EPXC";
	// when testing in sandbox, this is likely the -facilitator email address.
	private static String CONFIG_RECEIVER_EMAIL="";
	
	private String payment, amount, objectId, uObjectId, email;
	private List objectIds;
	ParseDB db;
	PaymentsScreen pScreen;
	Context context;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		String appId="QdwF666zm76ORQcn4KF6JNwDfsb6cj97QunbpT1s";
        String clientId="OiJI3KdONEN9jML6Mi6r6iQTpR8mIOBv3YgsUhdv";
        //Initialize with keys
        context = getApplicationContext();
        Parse.initialize(context, appId, clientId);
    	db=ParseDB.getInstance(context);
		Intent i = this.getIntent();
		amount=i.getStringExtra("amount");
		payment=i.getStringExtra("paymentName");
		objectId=i.getStringExtra("paymentObjectId");
		uObjectId=i.getStringExtra("userObjectId");
		email=i.getStringExtra("email");
		objectIds=i.getStringArrayListExtra("objectIds");
		
		CONFIG_RECEIVER_EMAIL=email;
		
		PayPalPayment thingToBuy = new PayPalPayment(new BigDecimal(amount), "ILS", payment);
 
		Intent intent = new Intent(this, PaymentActivity.class);  
		  intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT, CONFIG_ENVIRONMENT);
		  intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, CONFIG_CLIENT_ID);
		  intent.putExtra(PaymentActivity.EXTRA_RECEIVER_EMAIL, CONFIG_RECEIVER_EMAIL);
		  intent.putExtra(PaymentActivity.EXTRA_LANGUAGE_OR_LOCALE, "he");
		  intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);
		  intent.putExtra(PaymentActivity.EXTRA_PAYER_ID, "myPayer");
		  startActivityForResult(intent, 0);
	
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		  if (resultCode == Activity.RESULT_OK) {
		    PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
		    if (confirm != null) {
		      //String amountS = data.getParcelableExtra(PaymentActivity.EXTRA_PAYMENT);
		      verifyPayment(confirm);
		      // multi payment
				if (objectIds!=null){
					final List<ParseObject> payments = new ArrayList<ParseObject>();
					ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
					query.whereContainedIn("objectId", objectIds);
					query.findInBackground(new FindCallback<ParseObject>() {
						@Override
						public void done(List<ParseObject> list, ParseException e) {
							for (ParseObject payment : list){
								payment.addUnique("paidBy", uObjectId);
								payments.add(payment);
							}
							ParseObject.saveAllInBackground(payments, new SaveCallback() {
								@Override
								public void done(ParseException e) {
									if (e==null){
										Toast.makeText(getApplicationContext(), "התשלום בוצע בהצלחה", Toast.LENGTH_LONG).show();
										finish();
										goBacktoScreen();
									}else{
										Toast.makeText(getApplicationContext(), "התשלום נכשל", Toast.LENGTH_LONG).show();
										finish();
										goBacktoScreen();
									}
								}
							});
						}
					});
		      }else{
		    	  // single payment
				  ParseQuery<ParseObject> query = ParseQuery.getQuery("payments");
				  query.getInBackground(objectId, new GetCallback<ParseObject>() {
					  @Override
					  public void done(ParseObject parseObject, ParseException e) {
						  parseObject.addUnique("paidBy", uObjectId);
						  parseObject.saveInBackground(new SaveCallback() {
							  @Override
							  public void done(ParseException e) {
								  Toast.makeText(getApplicationContext(), "התשלום בוצע בהצלחה", Toast.LENGTH_LONG).show();
								  finish();
								  goBacktoScreen();
							  }
						  });
					  }
				  });
		      }
		      finish();
		      goBacktoScreen();		      
		    }
		  } else if (resultCode == Activity.RESULT_CANCELED) {
		    // Show the user that this got canceled
			  Toast.makeText(getApplicationContext(), "התשלום נכשל", Toast.LENGTH_LONG).show();
			  finish();
			  goBacktoScreen();
			  

		  } else if (resultCode == PaymentActivity.RESULT_PAYMENT_INVALID) {
		    // Check the docs ;)
			  Toast.makeText(getApplicationContext(), "התשלום נכשל", Toast.LENGTH_LONG).show();
			  finish();
			  goBacktoScreen();  
		  }
		}
	
	private void verifyPayment(PaymentConfirmation confirm) {
		//Toast.makeText(this, "PAYMENT CONFIRMEDDDDDD", Toast.LENGTH_LONG).show();
	}

	public void onDestroy() {
		stopService(new Intent(this, PayPalService.class));
		super.onDestroy();  
	}
	
	public void goBacktoScreen(){
		Intent g = new Intent(getApplicationContext(), MainActivity.class);
		this.startActivity(g);
	}

}


