package adapters;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.myvaad.myvaad.PaymentsScreen;
import com.myvaad.myvaad.R;

import adapters.PaymentsUserVaadBaitAdapter.ViewHolder;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PaymentsAdminVaadBaitAdapter extends BaseAdapter {

	private Context context;
	ViewHolder holder;
	LinkedList<Boolean> state= new LinkedList<Boolean>();
	List users;
	PaymentsScreen pScreen;
	List myData;
	List<String> objectId;
	
	public PaymentsAdminVaadBaitAdapter(Context context, List users, PaymentsScreen pScreen){
		this.context=context;
		this.users=users;
		this.pScreen = pScreen;
		stateConstructor(getCount());
	}
	
	public void stateConstructor(int listSize){
		for(int i=0;i<getCount();i++){
			this.state.add(false);
		}
	}
	
	@Override
	public int getCount() {
		return users.size();
	}

	@Override
	public List getItem(int idx) {
		List myData=(List)this.users.get(idx);
		//returns List - 0: famName(String), 1: uObjectId(String), 2: paid List (Boolean), 3: objectId List (String)
		return myData;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
	
	public void refresh(){
		notifyDataSetChanged();
	}
	
	public void setState(boolean state,int idx) {
		this.state.set(idx, state);
		notifyDataSetChanged();
	}
	
	public boolean getState(int idx) {
		return this.state.get(idx);
	}
	
	public class ViewHolder{
		TextView name, paidAllTxt;
		RelativeLayout cbContainer;
		CheckBox a,b,c,d,e,f,g,h,i,j,k,l,paidAllCB;	
	}
	
	@Override
	public View getView(int idx, View convertView, ViewGroup parent) {
		LayoutInflater myInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null){
            //inflating row view from layout xml
			convertView = myInflater.inflate(R.layout.payments_vaad_bait_admin_list_view_row, null);           
            holder = new ViewHolder();     
            holder.name=(TextView)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewName);
            holder.paidAllTxt=(TextView)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewPaidAll);
            holder.paidAllCB=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB);
            holder.cbContainer=(RelativeLayout)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCBContainer);
            holder.a=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB1);
            holder.b=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB2);
            holder.c=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB3);
            holder.d=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB4);
            holder.e=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB5);
            holder.f=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB6);
            holder.g=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB7);
            holder.h=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB8);
            holder.i=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB9);
            holder.j=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB10);
            holder.k=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB11);
            holder.l=(CheckBox)convertView.findViewById(R.id.paymentsVaadBaitAdminListViewCB12);
            
            convertView.setTag(holder);
       
		} else{
            holder = (ViewHolder)convertView.getTag();		
		}
			//setting the data of the row
			myData = getItem(idx);
			String famName = ""+myData.get(0);
			final String uObjectId = ""+myData.get(1);
			List<Boolean>months = (List)myData.get(2);
			objectId = (List)myData.get(3);
			holder.name.setText(famName);
			holder.paidAllCB.setChecked(!months.contains(false));
			holder.a.setChecked(months.get(0));
			holder.b.setChecked(months.get(1));
			holder.c.setChecked(months.get(2));
			holder.d.setChecked(months.get(3));
			holder.e.setChecked(months.get(4));
			holder.f.setChecked(months.get(5));
			holder.g.setChecked(months.get(6));
			holder.h.setChecked(months.get(7));
			holder.i.setChecked(months.get(8));
			holder.j.setChecked(months.get(9));
			holder.k.setChecked(months.get(10));
			holder.l.setChecked(months.get(11));
			holder.cbContainer.setVisibility(this.state.get(idx)?View.VISIBLE:View.GONE);
			
			holder.paidAllCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					//pScreen.paidAllVaadBait(uObjectId);
				}
			});
			
		return convertView;
	}

}
