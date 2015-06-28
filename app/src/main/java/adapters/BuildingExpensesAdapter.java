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

public class BuildingExpensesAdapter extends ParseQueryAdapter<ParseObject> {
    Context context;
    public BuildingExpensesAdapter(Context context) {
        // Use the QueryFactory to construct a PQA
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                //Define query
                ParseQuery query = new ParseQuery("payments");
                query.whereEqualTo("buildingCode", "239250");
                query.whereEqualTo("paymentType", "regular");
                query.orderByDescending("createdAt");
                return query;
            }
        });
        this.context=context;
    }

    @Override
    public View getItemView(ParseObject object, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.expenses_adapter_item, null);
        }

        // Take advantage of ParseQueryAdapter's getItemView logic for
        // populating the main TextView/ImageView.
        // The IDs in your custom layout must match what ParseQueryAdapter expects
        // if it will be populating a TextView or ImageView for you.
        super.getItemView(object, v, parent);

        // Do additional configuration before returning the View.
        TextView descriptionView = (TextView) v.findViewById(R.id.expenseDescription);
        descriptionView.setText(object.getString("description"));
        TextView amountView = (TextView)v.findViewById(R.id.expenseAmount);
        String thisExpense = object.getString("amount");
        amountView.setText(context.getString(R.string.shekel) + thisExpense);


        TextView createTimeView = (TextView)v.findViewById(R.id.expenseCreatTime);
        String time = object.getCreatedAt().toLocaleString();
        createTimeView.setText(time);


        return v;

    }


}
