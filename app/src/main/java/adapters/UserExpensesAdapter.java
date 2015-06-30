package adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myvaad.myvaad.ParseDB;
import com.myvaad.myvaad.R;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

public class UserExpensesAdapter extends ParseQueryAdapter<ParseObject> {
    Context context;
    ParseDB db;


    public UserExpensesAdapter(Context context, final String currentUserObjectId) {

        // Use the QueryFactory to construct a PQA
        super(context, new QueryFactory<ParseObject>() {
            public ParseQuery create() {
                //Define query
                ParseQuery query = new ParseQuery("payments");
                query.whereEqualTo("buildingCode", "239250");
                query.whereEqualTo("paymentType", "vaad");
                query.whereEqualTo("paymentApproved", true);
                query.whereEqualTo("paidBy", currentUserObjectId);
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

       // super.getItemView(object, v, parent);

        // Do additional configuration before returning the View.
        TextView descriptionView = (TextView) v.findViewById(R.id.expenseDescription);
        descriptionView.setText(object.getString("description"));
        TextView amountView = (TextView) v.findViewById(R.id.expenseAmount);
        String thisExpense = object.getString("amount");
        amountView.setText(context.getString(R.string.shekel) + thisExpense);


        TextView createTimeView = (TextView) v.findViewById(R.id.expenseCreatTime);
        String time = object.getCreatedAt().toLocaleString();
        createTimeView.setText(time);


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
