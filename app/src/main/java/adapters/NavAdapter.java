package adapters;


/**
 * The List view row layout is : navigation_drawer_items.xml
 */


import com.myvaad.myvaad.R;

import android.content.Context;
import android.view.*;
import android.widget.*;

public class NavAdapter extends BaseAdapter{

    int[] menu = {R.drawable.notices_nav_regular,R.drawable.failures_nav_regular,R.drawable.outcomes_nav_regular,R.drawable.payments_nav_regular,R.drawable.nav_dayarim_up};
    int[] selected = {R.drawable.notices_nav_selected,R.drawable.failures_nav_selected,R.drawable.outcomes_nav_selected,R.drawable.payments_nav_selected,R.drawable.nav_dayarim_down};
    boolean[] checked = {false,false,false,false,false};
    Context context;
    boolean isUserAdmin = false;

    public NavAdapter(Context context, boolean isUserAdmin){
        this.context = context;
        this.isUserAdmin = isUserAdmin;
    }

    @Override
    public int getCount() {
        return (isUserAdmin)?menu.length:menu.length-1;

    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    public void selected(int position){
        for(int i=0;i<checked.length;i++){
            checked[i]=false;
        }
        checked[position]=true;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView img,selectorImg;
        LayoutInflater myInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = myInflater.inflate(R.layout.navigation_drawer_items, null);
        selectorImg=(ImageView)view.findViewById(R.id.selectorImg);
        img = (ImageView)view.findViewById(R.id.drawer_item_icon);

        img.setImageResource(menu[i]);
        if (checked[i]) img.setImageResource(selected[i]);
        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (50 * scale + 0.5f);
        img.getLayoutParams().height=pixels;
        selectorImg.getLayoutParams().height=pixels;
        return view;
    }
}