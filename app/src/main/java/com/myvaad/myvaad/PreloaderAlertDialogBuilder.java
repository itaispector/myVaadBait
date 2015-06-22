package com.myvaad.myvaad;


import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class PreloaderAlertDialogBuilder extends AlertDialog {

    private final Context mContext;

    public PreloaderAlertDialogBuilder(Context context) {
        super(context);
        mContext = context;

        View customMessage = View.inflate(mContext, R.layout.preloader_layout, null);
        ImageView image=(ImageView) customMessage.findViewById(R.id.loader_image);
        Animation rotate= AnimationUtils.loadAnimation(context, R.anim.rotate);
        image.startAnimation(rotate);
        setView(customMessage);
    }
}
