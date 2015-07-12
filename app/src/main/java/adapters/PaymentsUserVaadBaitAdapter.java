package adapters;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;

import com.myvaad.myvaad.PaymentsScreen;
import com.myvaad.myvaad.R;
import com.parse.ParseObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class PaymentsUserVaadBaitAdapter extends BaseAdapter {
	private Context context;
	ViewHolder holder;
	List<ParseObject> payments;
	String uObjectId;
	PaymentsScreen pScreen;

	public PaymentsUserVaadBaitAdapter(Context context, List payments, String uObjectId, PaymentsScreen pScreen){
		this.context=context;
		this.payments=payments;
		this.uObjectId=uObjectId;
		this.pScreen = pScreen;
	}
	
	public void reloadData(List payments){
		this.payments = payments;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return payments.size();
	}

	@Override
	public ParseObject getItem(int idx) {
		return payments.get(idx);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public class ViewHolder{
		TextView payment,amount;	
		Button pay;
	}
	
	@Override
	public View getView(int idx, View convertView, ViewGroup parent) {
		LayoutInflater myInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null){
            //inflating row view from layout xml
			convertView = myInflater.inflate(R.layout.payments_user_list_view_row, null);           
            holder = new ViewHolder();     
            holder.payment=(TextView)convertView.findViewById(R.id.paymentVaadBaitName);
            holder.amount=(TextView)convertView.findViewById(R.id.paymentVaadBaitAmount);           
            holder.pay=(Button)convertView.findViewById(R.id.paypalVaadBaitPay);
            convertView.setTag(holder);
       
		} else
            holder = (ViewHolder)convertView.getTag();
		
		//setting the data of the row
		ParseObject payment = getItem(idx);
		final String paymentName = payment.getString("description");
		final String amount = payment.getString("amount");
		final String objectId = payment.getObjectId();
		holder.payment.setText(paymentName);
		holder.amount.setText(amount);
		
		//set listener to pay btn
		holder.pay.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				//pScreen.pay(amount, payment, objectId, uObjectId);
			}
		});
		

		return convertView;
	}

}
