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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class NoticesAdapter extends BaseAdapter {
	private Context context;
	
	List notices;

	public NoticesAdapter(Context context, List notices) {
		this.context = context;
		this.notices = notices;
		notifyDataSetChanged();
	}

	public int getCount() {
		return notices.size();
	}

	@Override
	public List getItem(int idx) {
		return (List) this.notices.get(idx);
	}

	@Override
	public long getItemId(int idx) {
		return 0;
	}
		
	public void refresh(List notices){
		this.notices=notices;
		notifyDataSetChanged();
	}
	
	public class ViewHolder{
		TextView fullname;
		ImageView image;
		SurfaceView divider;
		TextView content;
		SurfaceView divider2;
		TextView date;
		
	}
	
	@Override
	public View getView(int idx, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		LayoutInflater myInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null){
            convertView = myInflater.inflate(R.layout.notice_board_list_view_row, null);
            holder = new ViewHolder();
            holder.fullname = (TextView)convertView.findViewById(R.id.noticeBoardRowFullName);
            holder.image = (ImageView)convertView.findViewById(R.id.noticeBoardRowUserImage);
            holder.divider = (SurfaceView)convertView.findViewById(R.id.noticeBoardRowDivider);
            holder.content = (TextView)convertView.findViewById(R.id.noticeBoardRowContent);
            holder.divider2 = (SurfaceView)convertView.findViewById(R.id.noticeBoardRowDivider2);
            holder.date = (TextView)convertView.findViewById(R.id.noticeBoardRowDate);
            
            
            convertView.setTag(holder);
        } else
            holder = (ViewHolder)convertView.getTag();
		
		//List container = (List)notices.get(idx);
		holder.fullname.setText(((List)notices.get(idx)).get(3)+" משפחת");
		holder.content.setText(""+((List)notices.get(idx)).get(1));
		holder.date.setText(""+((List)notices.get(idx)).get(2));
		holder.image.setImageBitmap((Bitmap) ((List)notices.get(idx)).get(4));
		return convertView;
	}

}
