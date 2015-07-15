package adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myvaad.myvaad.R;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.paypal.android.sdk.Z;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class BuildingExpensesAdapter extends ParseQueryAdapter<ParseObject> {
    Context context;


    public BuildingExpensesAdapter(Context context , final int startYear, final int startMonthOfYear, final int startDayOfMonth ,final int endYear, final int endMonthOfYear, final int endDayOfMonth) {
        // Use the QueryFactory to construct a PQA

        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                //Define query
                Calendar calendar = Calendar.getInstance();
                calendar.set(startYear,startMonthOfYear,startDayOfMonth);
                Date startDate = calendar.getTime();
                calendar.set(endYear,endMonthOfYear,endDayOfMonth);
                Date endDate = calendar.getTime();

                ParseQuery query = new ParseQuery("payments");
                query.whereEqualTo("buildingCode", "239250");
                query.whereNotEqualTo("paymentType", "vaad");
                query.whereGreaterThanOrEqualTo("createdAt", startDate);
                query.whereLessThanOrEqualTo("createdAt", endDate);
                query.orderByDescending("createdAt");
                return query;
            }
        });
        this.context=context;
    }

    public BuildingExpensesAdapter(Context context) {
        // Use the QueryFactory to construct a PQA

        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                //Define queryS
                ParseQuery query = new ParseQuery("payments");
                query.whereEqualTo("buildingCode", "239250");
                query.whereNotEqualTo("paymentType", "vaad");
                query.orderByDescending("createdAt");
                return query;
            }
        });
        this.context=context;
    }

    @Override
    public ParseObject getItem(int index) {
        return super.getItem(index);
    }



    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.expenses_adapter_item, null);
        }

       // super.getItemView(object, v, parent);

        // Do additional configuration before returning the View.
        TextView descriptionView = (TextView) v.findViewById(R.id.expenseDescription);
        descriptionView.setText(object.getString("description"));
        TextView amountView = (TextView)v.findViewById(R.id.expenseAmount);
        String thisExpense = object.getString("amount");
        amountView.setText(context.getString(R.string.shekel) + thisExpense);

        TextView createTimeView = (TextView)v.findViewById(R.id.expenseCreatTime);

        Date dateObj = object.getCreatedAt();
        //Creating instance of SimpleDateFormat
        SimpleDateFormat postFormatter = new SimpleDateFormat("HH:mm  dd.MM.yy");
        //Changing Date and time format up to SimpleDateFormat
        String newDateStr = postFormatter.format(dateObj);

        createTimeView.setText(newDateStr);


        return v;

    }
    // Next page clicked view to load more rows
    @Override
    public View getNextPageView(View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.expenses_next_page_adapter_item, null);
        }
        TextView textView1 = (TextView) v.findViewById(R.id.nextPageTitleText1);
        textView1.setText("נטענו " + (getCount() - 1) + " שורות" );
        TextView textView2 = (TextView) v.findViewById(R.id.nextPageTitleText2);
        textView2.setText( "לחץ לטעינת עוד שורות");
        return v;
    }
}
