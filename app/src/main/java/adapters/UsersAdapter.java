package adapters;

import java.util.HashMap;
import java.util.List;

import com.myvaad.myvaad.ParseDB;
import com.myvaad.myvaad.R;
import com.myvaad.myvaad.UsersScreen;
import com.parse.DeleteCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

public class UsersAdapter extends BaseAdapter {
	private Context context;
	ViewHolder holder;
	List users;
    UsersScreen us;

	public UsersAdapter(Context context, List users, UsersScreen us) {
		this.context = context;	
		this.users = users;
        this.us = us;
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
		TextView familyName, apartmentNumber;
		ImageView userImg;
		Button sendMsg,delUser;
		
	}
    public String getObjectId(int idx){
        return ""+((List) users.get(idx)).get(2);
    }
	@Override
	public View getView(final int idx, View convertView, ViewGroup parent) {
			
		LayoutInflater myInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null){
            //inflating row view from layout xml
			convertView = myInflater.inflate(R.layout.users_list_view_row, null);           
            holder = new ViewHolder();     
            holder.familyName=(TextView)convertView.findViewById(R.id.usersRowFullName);
            holder.apartmentNumber=(TextView)convertView.findViewById(R.id.usersApartmentNumber);
            holder.userImg=(ImageView)convertView.findViewById(R.id.usersRowUserImage);
            holder.sendMsg=(Button)convertView.findViewById(R.id.usersRowSendBtn);
            holder.delUser=(Button)convertView.findViewById(R.id.usersRowDelBtn);
            
            convertView.setTag(holder);
       
		} else
            holder = (ViewHolder)convertView.getTag();
		
		//setting the data of the row
		List myUsers = (List)users.get(idx);
        final String familyName = (String)myUsers.get(0);
        final String userObjectId = (String)myUsers.get(2);
        boolean hasApplication = (boolean)myUsers.get(3);
        String apartmentNumber = (String)myUsers.get(4);
        holder.familyName.setText("משפחת " + familyName);
        holder.apartmentNumber.setText(context.getString(R.string.appartment)+" "+apartmentNumber);
		holder.userImg.setImageBitmap((Bitmap) myUsers.get(1));
        if(hasApplication){
            holder.sendMsg.setVisibility(View.VISIBLE);
            holder.sendMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setMessage(R.string.deleteFailure);
                    final EditText input = new EditText(context);
                    dialog.setView(input);
                    dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (input.getText().toString().matches("")) {
                                Toast.makeText(context, "אנא הכנס/י הודעה", Toast.LENGTH_LONG).show();
                            } else {
                                ParseQuery query = ParseInstallation.getQuery();
                                query.whereEqualTo("userNamePush", getObjectId(idx));
                                ParsePush androidPush = new ParsePush();
                                androidPush.setMessage(input.getText().toString());
                                androidPush.setQuery(query);
                                androidPush.sendInBackground();
                            }
                        }
                    });
                    dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            });
        }else{
            holder.sendMsg.setVisibility(View.GONE);
        }


        holder.delUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUser(userObjectId, familyName);
            }
        });


        return convertView;
	}

    protected void deleteUser(final String userObjectId, String familyName) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        String delete = context.getString(R.string.delete_user);
        String areYouSure = context.getString(R.string.are_you_sure);
        String family = context.getString(R.string.family);
        dialog.setMessage(delete+" "+family+" "+familyName+"\n"+areYouSure);
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                us.showLoader();
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("userObjectId", userObjectId);
                ParseCloud.callFunctionInBackground("deleteUser", params, new FunctionCallback<Object>() {
                    public void done(Object result, ParseException e) {
                        if (e == null) {
                            //Toast.makeText(context, ""+result, Toast.LENGTH_SHORT).show();
                            us.refreshUsers();
                        } else {
                            //Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
        dialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
