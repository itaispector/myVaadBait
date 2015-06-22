package adapters;

import java.util.List;

import com.myvaad.myvaad.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class PaymentsAdapter extends BaseAdapter {
	private Context context;
	ViewHolder holder;
		
	//data
	List payments;
	
	public PaymentsAdapter(Context context, List payments) {
		this.context = context;	
		this.payments = payments;
	}
	
	public void reloadData(List payments) {
		this.payments = payments;
		notifyDataSetChanged();
	}
	
	public int getCount() {
		return payments.size();
	}

	@Override
	public String[] getItem(int idx) {
		List tmpData = (List) this.payments.get(idx);
		String[] myData = {""+tmpData.get(0),""+tmpData.get(1),""+tmpData.get(2),""+tmpData.get(3)};
		//returns array - 0: paymentName, 1: amount, 2: date, 3: objectId
		return myData;
	}

	@Override
	public long getItemId(int idx) {
		// TODO Auto-generated method stub
		return 0;
	}

	//helper class for holding the views in the list view, better for performance
	public class ViewHolder{
		TextView payment,amount, date;		
	}
	
	@Override
	public View getView(int idx, View convertView, ViewGroup parent) {
			
		LayoutInflater myInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null){
            //inflating row view from layout xml
			convertView = myInflater.inflate(R.layout.payments_list_view_row, null);           
            holder = new ViewHolder();     
            holder.payment=(TextView)convertView.findViewById(R.id.paymentsRowFamilyName);
            holder.amount=(TextView)convertView.findViewById(R.id.paymentsRowAmount);
            holder.date=(TextView)convertView.findViewById(R.id.paymentsRowDate);
            
            convertView.setTag(holder);
       
		} else
            holder = (ViewHolder)convertView.getTag();
		
		//setting the data of the row
		String[] data = getItem(idx);
		holder.payment.setText(data[0]);
		holder.amount.setText("\u20AA "+data[1]);
		holder.date.setText(data[2]);

		return convertView;
	}
	

}
