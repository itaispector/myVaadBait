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

public class UsersAdapter extends BaseAdapter {
	private Context context;
	ViewHolder holder;
	List users;

	public UsersAdapter(Context context, List users) {
		this.context = context;	
		this.users = users;
	}
	
	public int getCount() {
		return users.size();
	}

	@Override
	public Object getItem(int idx) {
		return this.users.get(idx);
	}

	@Override
	public long getItemId(int idx) {
		// TODO Auto-generated method stub
		return 0;
	}

	//helper class for holding the views in the listview, better for performance
	public class ViewHolder{
		TextView familyName;
		ImageView userImg;
		SurfaceView div;
		Button sendMsg,delUser;
		
	}
		
	@Override
	public View getView(int idx, View convertView, ViewGroup parent) {
			
		LayoutInflater myInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null){
            //inflating row view from layout xml
			convertView = myInflater.inflate(R.layout.users_list_view_row, null);           
            holder = new ViewHolder();     
            holder.familyName=(TextView)convertView.findViewById(R.id.usersRowFullName);
            holder.userImg=(ImageView)convertView.findViewById(R.id.usersRowUserImage);
            holder.div=(SurfaceView)convertView.findViewById(R.id.usersRowDivider4);
            holder.sendMsg=(Button)convertView.findViewById(R.id.usersRowSendBtn);
            holder.delUser=(Button)convertView.findViewById(R.id.usersRowDelBtn);
            
            convertView.setTag(holder);
       
		} else
            holder = (ViewHolder)convertView.getTag();
		
		//setting the data of the row
		holder.familyName.setText(" משפחת"+((List) users.get(idx)).get(0));
		holder.userImg.setImageBitmap((Bitmap)((List) users.get(idx)).get(1));

		return convertView;
	}
	

}
