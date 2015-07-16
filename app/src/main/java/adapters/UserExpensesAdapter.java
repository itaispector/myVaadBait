package adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myvaad.myvaad.ParseDB;
import com.myvaad.myvaad.R;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UserExpensesAdapter extends ParseQueryAdapter<ParseObject> {
    Context context;

    public UserExpensesAdapter(Context context, final String buildingCode, final String currentUserObjectId) {

        // Use the QueryFactory to construct a PQA
        super(context, new QueryFactory<ParseObject>() {
            public ParseQuery create() {
                //Define query
                ParseQuery query = new ParseQuery("payments");
                query.whereEqualTo("buildingCode", buildingCode);
                query.whereEqualTo("paidBy", currentUserObjectId);
                query.whereNotEqualTo("paymentType", "regular");
                query.orderByDescending("createdAt");

                return query;
            }
        });
        this.context = context;

    }

    public UserExpensesAdapter(Context context,final String buildingCode, final String currentUserObjectId, final int startYear, final int startMonthOfYear, final int startDayOfMonth ,final int endYear, final int endMonthOfYear, final int endDayOfMonth) {

        // Use the QueryFactory to construct a PQA
        super(context, new QueryFactory<ParseObject>() {
            public ParseQuery create() {
                Calendar calendar = Calendar.getInstance();
                calendar.set(startYear,startMonthOfYear,startDayOfMonth);
                Date startDate = calendar.getTime();
                calendar.set(endYear, endMonthOfYear, endDayOfMonth);
                Date endDate = calendar.getTime();
                //Define query
                ParseQuery query = new ParseQuery("payments");
                query.whereEqualTo("buildingCode", buildingCode);
                query.whereEqualTo("paidBy", currentUserObjectId);
                query.whereNotEqualTo("paymentType", "regular");
                query.whereGreaterThanOrEqualTo("createdAt", startDate);
                query.whereLessThanOrEqualTo("createdAt", endDate);
                query.orderByDescending("createdAt");

                return query;
            }
        });
        this.context = context;

    }

    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.expenses_adapter_item, null);
        }
        // Do additional configuration before returning the View.
        TextView descriptionView = (TextView) v.findViewById(R.id.expenseDescription);
        TextView amountView = (TextView) v.findViewById(R.id.expenseAmount);
        TextView createdTimeView = (TextView) v.findViewById(R.id.expenseCreatTime);

        Date dateObj = object.getCreatedAt();
        //Creating instance of SimpleDateFormat
        SimpleDateFormat postFormatter = new SimpleDateFormat("HH:mm  dd.MM.yy");
        //Changing Date and time format up to SimpleDateFormat
        String newDateStr = postFormatter.format(dateObj);

        String thisExpense = object.getString("amount");

        String paymentType = object.getString("paymentType");

        if(paymentType.equals("extra")){
            descriptionView.setText(object.getString("description"));

            amountView.setText(context.getString(R.string.shekel) + Integer.parseInt(thisExpense)/object.getInt("houses"));

        }else{
            descriptionView.setText(object.getString("description")+ " " +object.getString("period") + "-" + object.getString("year") );

            amountView.setText(context.getString(R.string.shekel) + thisExpense);
        }



        createdTimeView.setText(newDateStr);

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
