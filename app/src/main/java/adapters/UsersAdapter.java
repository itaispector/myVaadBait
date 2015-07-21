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
import android.util.Log;
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
    List<ParseObject> users;
    View.OnClickListener sendBtnListener = null;

    public UsersAdapter(Context context, List<ParseObject> users) {
        this.context = context;
        this.users = users;
    }

    public int getCount() {
        return users.size();
    }

    @Override
    public ParseObject getItem(int idx) {
        return users.get(idx);
    }

    @Override
    public long getItemId(int idx) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void setSendBtnListener(View.OnClickListener lis){
        this.sendBtnListener = lis;
    }

    //helper class for holding the views in the listview, better for performance
    public class ViewHolder {
        TextView familyName, apartmentNumber;
        ImageView userImg;
        Button sendMsg, delUser;

    }

    @Override
    public View getView(final int idx, View convertView, ViewGroup parent) {

        LayoutInflater myInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            //inflating row view from layout xml
            convertView = myInflater.inflate(R.layout.users_list_view_row, null);
            holder = new ViewHolder();
            holder.familyName = (TextView) convertView.findViewById(R.id.usersRowFullName);
            holder.apartmentNumber = (TextView) convertView.findViewById(R.id.usersApartmentNumber);
            holder.userImg = (ImageView) convertView.findViewById(R.id.usersRowUserImage);
            holder.sendMsg = (Button) convertView.findViewById(R.id.usersRowSendBtn);
            holder.delUser = (Button) convertView.findViewById(R.id.usersRowDelBtn);

            convertView.setTag(holder);

        } else
            holder = (ViewHolder) convertView.getTag();

        //setting the data of the row
        ParseObject user = getItem(idx);
        final String familyName = user.getString("familyName");
        final String userObjectId = user.getObjectId();
        boolean hasApplication = user.getBoolean("hasApplication");
        String apartmentNumber = user.getString("apartmentNumber");
        holder.familyName.setText(context.getString(R.string.family) + " " + familyName);
        holder.apartmentNumber.setText(context.getString(R.string.appartment) + " " + apartmentNumber);
        ParseFile img = user.getParseFile("picture");
        Bitmap userPic = parseFileToBitmap(img);
        holder.userImg.setImageBitmap(userPic);
        holder.sendMsg.setVisibility(hasApplication ? View.VISIBLE : View.GONE);

        if (sendBtnListener!=null){
            holder.sendMsg.setOnClickListener(sendBtnListener);
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
        dialog.setMessage(delete + " " + family + " " + familyName + "\n" + areYouSure);
        dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("userObjectId", userObjectId);
                ParseCloud.callFunctionInBackground("deleteUser", params, new FunctionCallback<Object>() {
                    public void done(Object result, ParseException e) {
                        if (e == null) {
                            //Toast.makeText(context, ""+result, Toast.LENGTH_SHORT).show();
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

    // method from parseDB file
    protected Bitmap parseFileToBitmap(ParseFile file) {
        byte[] bitmapdata = null;
        try {
            bitmapdata = file.getData();
        } catch (ParseException e) {
            Log.i("***Parse Exception****", e.getLocalizedMessage());
            e.printStackTrace();
        }
        //this is Bitmap
        return BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
    }

}
