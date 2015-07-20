package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.myvaad.myvaad.PaymentsScreen;
import com.myvaad.myvaad.R;
import com.parse.ParseObject;

import java.util.List;

public class PaymentsAdminUsersListAdapter extends BaseAdapter {
	private Context context;
	ViewHolder holder;
	List<ParseObject> users;
	List paidBy;

	public PaymentsAdminUsersListAdapter(Context context, List<ParseObject> users, List paidBy){
		this.context = context;
		this.users = users;
		this.paidBy = paidBy;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return users.size();
	}

	@Override
	public ParseObject getItem(int idx) {
		return users.get(idx);
	}

	public boolean getPaid(int idx) {
		boolean state = false;
		ParseObject user = getItem(idx);
		String userObjectId = user.getObjectId();
		if (paidBy != null) {
			state = paidBy.contains(userObjectId);
		}
		return state;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public class ViewHolder{
		TextView name;
		CheckBox checkBox;
	}
	
	@Override
	public View getView(final int idx, View convertView, ViewGroup parent) {
		LayoutInflater myInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null){
            //inflating row view from layout xml
			convertView = myInflater.inflate(R.layout.payments_admin_list_row, null);           
            holder = new ViewHolder();     
            holder.name=(TextView)convertView.findViewById(R.id.paymentsAdminListViewName);
			holder.checkBox=(CheckBox)convertView.findViewById(R.id.usersListCB);
            convertView.setTag(holder);
       
		}else
            holder = (ViewHolder)convertView.getTag();
		
		//setting the data of the row
		ParseObject user = getItem(idx);
		String famName = user.getString("familyName");





		holder.name.setText(context.getString(R.string.family) + " " + famName);
		holder.checkBox.setChecked(getPaid(idx));
		return convertView;
	}
}
