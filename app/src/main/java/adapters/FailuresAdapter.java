package adapters;

import java.util.LinkedList;
import java.util.List;

import com.myvaad.myvaad.FailuresScreen;
import com.myvaad.myvaad.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

public class FailuresAdapter extends BaseAdapter {
    private Context context;
    ViewHolder holder;
    List failures;
    LinkedList<Boolean> state = new LinkedList<Boolean>();
    boolean isAdmin;
    String familyName;
    View.OnClickListener addBtnListener = null, editBtnListener = null, approvalBtnListener = null, deleteBtnListener = null, approveOkBtnListener = null, approveCancelBtnListener = null;

    public FailuresAdapter(Context context, List failures, boolean isAdmin, String familyName) {
        this.context = context;
        this.failures = failures;
        stateConstructor(getCount());
        this.isAdmin = isAdmin;
        this.familyName = familyName;
    }

    public void stateConstructor(int listSize) {
        for (int i = 0; i < listSize; i++) {
            this.state.add(false);
        }
    }

    public int getCount() {
        return this.failures.size();
    }

    @Override
    public Object getItem(int idx) {
        return this.failures.get(idx);
    }

    @Override
    public long getItemId(int idx) {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getObjectId(int idx) {
        return "" + ((List) failures.get(idx)).get(0);
    }

    public void refresh(List failures) {
        this.failures = failures;
        stateConstructor(getCount());
        notifyDataSetChanged();
    }

    public String countApprovers(int idx) {
        return "" + ((List) ((List) failures.get(idx)).get(9)).size();
    }

    public List getApprovers(int idx) {
        return (List) ((List) failures.get(idx)).get(9);
    }

    //setter & getter for variable "state" is in charge of open & close the box
    public void setState(boolean state, int idx) {
        this.state.set(idx, state);
        notifyDataSetChanged();
    }

    public void setAddBtnListener(View.OnClickListener lis) {
        this.addBtnListener = lis;
    }

    public void setEditBtnListener(View.OnClickListener lis) {
        this.editBtnListener = lis;
    }

    public void setApprovalBtnListener(View.OnClickListener lis) {
        this.approvalBtnListener = lis;
    }

    public void setDeleteBtnListener(View.OnClickListener lis) {
        this.deleteBtnListener = lis;
    }

    public void setApproveOkBtnListener(View.OnClickListener lis) {
        this.approveOkBtnListener = lis;
    }

    public void setApproveCancelBtnListener(View.OnClickListener lis) {
        this.approveCancelBtnListener = lis;
    }

    public boolean getState(int idx) {
        return this.state.get(idx);
    }


    //helper class for holding the views in the listview, better for performance
    public class ViewHolder {
        TextView fullname, failure, failureContent, bidText, bidPrice, bidProf, date, status;
        ImageView image;
        SurfaceView divider, divider2;
        Button add, edit, statusBtn, deleteBtn, approveBtn, cancelBtn;
    }

    @Override
    public View getView(final int idx, View convertView, final ViewGroup parent) {

        LayoutInflater myInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            //inflating row view from layout xml
            convertView = myInflater.inflate(R.layout.failure_list_view_row, null);
            holder = new ViewHolder();
            holder.fullname = (TextView) convertView.findViewById(R.id.failuresRowFullName);
            holder.image = (ImageView) convertView.findViewById(R.id.failuresRowUserImage);
            holder.divider = (SurfaceView) convertView.findViewById(R.id.failuresRowDivider);
            holder.failure = (TextView) convertView.findViewById(R.id.failuresRowfailureName);
            holder.failureContent = (TextView) convertView.findViewById(R.id.failuresRowFailureContent);
            holder.date = (TextView) convertView.findViewById(R.id.failuresRowDate);
            holder.divider2 = (SurfaceView) convertView.findViewById(R.id.failuresRowDivider4);
            holder.bidText = (TextView) convertView.findViewById(R.id.failuresRowfailureBidText);
            holder.bidPrice = (TextView) convertView.findViewById(R.id.failuresRowfailureBidPrice);
            holder.bidProf = (TextView) convertView.findViewById(R.id.failuresRowfailureBidBusinessName);
            holder.add = (Button) convertView.findViewById(R.id.FailuresRowAddBtn);
            holder.edit = (Button) convertView.findViewById(R.id.FailuresRowEditBtn);
            holder.status = (TextView) convertView.findViewById(R.id.FailuresRowStatusView);
            holder.statusBtn = (Button) convertView.findViewById(R.id.FailuresRowStatusBtn);
            holder.deleteBtn = (Button) convertView.findViewById(R.id.FailuresRowDeleteBtn);
            holder.approveBtn = (Button) convertView.findViewById(R.id.FailuresRowApproveBtn);
            holder.cancelBtn = (Button) convertView.findViewById(R.id.FailuresRowCancelBtn);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //setting the data of the row
        holder.fullname.setText(context.getString(R.string.family) + " " + ((List) failures.get(idx)).get(7));
        holder.failure.setText("" + ((List) failures.get(idx)).get(1));
        holder.failureContent.setText("" + ((List) failures.get(idx)).get(2));
        if (!("" + ((List) failures.get(idx)).get(3)).matches("")) {
            holder.bidPrice.setText("₪ " + ((List) failures.get(idx)).get(3));
            holder.bidProf.setText("" + ((List) failures.get(idx)).get(4));
        } else {
            holder.bidPrice.setText(R.string.failures_none);
            holder.bidProf.setText("");
        }
        holder.date.setText("" + ((List) failures.get(idx)).get(6));
        holder.image.setImageBitmap((Bitmap) ((List) failures.get(idx)).get(8));
        holder.status.setText("" + ((List) failures.get(idx)).get(5));
        //color the status
        if (("" + ((List) failures.get(idx)).get(5)).matches(context.getString(R.string.notTreated)))
            holder.status.setTextColor(Color.parseColor("#640000"));
        else {
            holder.status.setTextColor(Color.parseColor("#006400"));
        }
        holder.statusBtn.setText(countApprovers(idx));

        //incharge of closing or opening the box
        holder.failureContent.setVisibility(this.state.get(idx) ? View.VISIBLE : View.GONE);
        holder.divider2.setVisibility(this.state.get(idx) ? View.VISIBLE : View.GONE);
        holder.bidText.setVisibility(this.state.get(idx) ? View.VISIBLE : View.GONE);
        holder.bidPrice.setVisibility(this.state.get(idx) ? View.VISIBLE : View.GONE);
        holder.bidProf.setVisibility(this.state.get(idx) ? View.VISIBLE : View.GONE);

        //if there's no offer, show add button, else show edit, status & delete buttons
        if (("" + ((List) failures.get(idx)).get(3)).matches("")) {
            holder.add.setVisibility(this.state.get(idx) ? View.VISIBLE : View.GONE);
            holder.deleteBtn.setVisibility(this.state.get(idx) ? View.VISIBLE : View.GONE);
            holder.edit.setVisibility(View.GONE);
            holder.statusBtn.setVisibility(View.GONE);
            holder.cancelBtn.setVisibility(View.GONE);
            holder.approveBtn.setVisibility(View.GONE);
        } else {
            holder.edit.setVisibility(this.state.get(idx) ? View.VISIBLE : View.GONE);
            holder.statusBtn.setVisibility(this.state.get(idx) ? View.VISIBLE : View.GONE);
            holder.deleteBtn.setVisibility(this.state.get(idx) ? View.VISIBLE : View.GONE);
            holder.add.setVisibility(View.GONE);
        }
        //buttons for dayarim ok and cancel of offer
        if (!isAdmin) {
            holder.add.setVisibility(View.GONE);
            holder.edit.setVisibility(View.GONE);
            holder.deleteBtn.setVisibility(View.GONE);
            holder.cancelBtn.setVisibility(View.GONE);
            holder.approveBtn.setVisibility(View.GONE);

            //if there's offer show ok/cancel button
            if (!("" + ((List) failures.get(idx)).get(3)).matches(""))
                //if user hasn't approve yet, show approve, else show cancel
                if (getApprovers(idx).indexOf(familyName) == -1) {
                    holder.approveBtn.setVisibility(this.state.get(idx) ? View.VISIBLE : View.GONE);
                    holder.cancelBtn.setVisibility(View.GONE);
                } else {
                    holder.cancelBtn.setVisibility(this.state.get(idx) ? View.VISIBLE : View.GONE);
                    holder.approveBtn.setVisibility(View.GONE);
                }
        }

        if (addBtnListener != null) {
            holder.add.setOnClickListener(addBtnListener);
        }

        if (editBtnListener != null) {
            holder.edit.setOnClickListener(editBtnListener);
        }

        if (approvalBtnListener != null) {
            holder.statusBtn.setOnClickListener(approvalBtnListener);
        }

        if (deleteBtnListener != null) {
            holder.deleteBtn.setOnClickListener(deleteBtnListener);
        }

        if (approveOkBtnListener != null) {
            holder.approveBtn.setOnClickListener(approveOkBtnListener);
        }

        if (approveCancelBtnListener != null) {
            holder.cancelBtn.setOnClickListener(approveCancelBtnListener);
        }

        return convertView;
    }


}
