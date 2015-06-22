package adapters;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

import com.myvaad.myvaad.ParseDB;
import com.myvaad.myvaad.PaymentsScreen;
import com.myvaad.myvaad.R;

import adapters.PaymentsUserVaadBaitAdapter.ViewHolder;
import android.content.Context;
import android.text.InputFilter.LengthFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentsAdminUsersListAdapter extends BaseAdapter {
	private Context context;
	ViewHolder holder;
	List users;
	int position;
	PaymentsScreen pScreen;
	String objectId;

	public PaymentsAdminUsersListAdapter(Context context, List users, String objectId, PaymentsScreen pScreen){
		this.context = context;
		this.users = users;
		this.objectId = objectId;
		this.pScreen = pScreen;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return users.size();
	}

	@Override
	public Object[] getItem(int idx) {
		List tmpData =(List) this.users.get(idx);
		Object[] myData = {""+tmpData.get(0),""+tmpData.get(1),(Boolean)tmpData.get(2)};
		//returns array - 0: famName(String), 1: uObjectId(String), 2: paid or not(Boolean)
		return myData;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public class ViewHolder{
		TextView name;
		CheckBox cb;
	}
	
	@Override
	public View getView(final int idx, View convertView, ViewGroup parent) {
		LayoutInflater myInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null){
            //inflating row view from layout xml
			convertView = myInflater.inflate(R.layout.payments_admin_list_row, null);           
            holder = new ViewHolder();     
            holder.name=(TextView)convertView.findViewById(R.id.paymentsAdminListViewName);
            holder.cb=(CheckBox)convertView.findViewById(R.id.paymentsAdminListViewCB);           
            convertView.setTag(holder);
       
		}else
            holder = (ViewHolder)convertView.getTag();
		
		//setting the data of the row
		final Object[] data = getItem(idx);
		holder.name.setText(""+data[0]);
		holder.cb.setChecked((Boolean)data[2]);
		
		holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {		
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				String uObjectId = ""+data[1];
				if (buttonView.isChecked()){
					pScreen.PaidUserToPaymentList(true, objectId, uObjectId);
				}else{
					pScreen.PaidUserToPaymentList(false, objectId, uObjectId);
				}
			}
		});
		
		return convertView;
	}
}
