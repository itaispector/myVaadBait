package dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import com.myvaad.myvaad.R;

public class AboutDialog {
    private Context context;

    public AboutDialog(Context context) {
        this.context=context;
    }
    public void showAboutDialog(){
        AlertDialog.Builder dialog=new AlertDialog.Builder(context);
        dialog.setTitle(R.string.about_dialog_title);
        View view = View.inflate(context, R.layout.about_dialog, null);
        dialog.setView(view);
        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
