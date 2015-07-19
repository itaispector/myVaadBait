package adapters;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.myvaad.myvaad.PaymentsScreen;
import com.myvaad.myvaad.R;
import com.parse.ParseObject;

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
    List<ParseObject> users;
    List paidAll;

    public PaymentsAdminVaadBaitAdapter(Context context, List users, List paidAll) {
        this.context = context;
        this.users = users;
        this.paidAll = paidAll;
    }

    @Override
    public int getCount() {
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
        if (paidAll != null) {
            state = paidAll.contains(userObjectId);
        }
        return state;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    public class ViewHolder {
        TextView name, paidAllTxt;
    }

    @Override
    public View getView(int idx, View convertView, ViewGroup parent) {
        LayoutInflater myInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            //inflating row view from layout xml
            convertView = myInflater.inflate(R.layout.payments_vaad_bait_admin_list_view_row, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.paymentsVaadBaitAdminListViewName);
            holder.paidAllTxt = (TextView) convertView.findViewById(R.id.paymentsVaadBaitAdminListViewPaidAll);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //setting the data of the row
        ParseObject user = getItem(idx);
        String famName = user.getString("familyName");
        holder.name.setText(context.getResources().getString(R.string.family) + " " + famName);
        String userObjectId = user.getObjectId();
        int visibility = View.INVISIBLE;
        if (paidAll != null) {
            visibility = (paidAll.contains(userObjectId) ? View.VISIBLE : View.INVISIBLE);
        }
        holder.paidAllTxt.setVisibility(visibility);

        return convertView;
    }

}
