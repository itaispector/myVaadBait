package dialogs;

import android.app.ProgressDialog;
import android.content.Context;

public class RingProgressDialog {

    private final Context mContext;
    ProgressDialog ringProgressDialog;
    public RingProgressDialog(Context context) {
        mContext = context;
        ringProgressDialog = ProgressDialog.show(mContext, null, "טוען...", true);
        ringProgressDialog.setCancelable(false);
    }
    public void dismiss(){
        ringProgressDialog.dismiss();
    }
}
