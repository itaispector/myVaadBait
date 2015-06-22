package com.myvaad.myvaad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.parse.Parse;

public class MainPreloader extends Activity {
    ParseDB db;
    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 1800;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_preloader);
        String appId="QdwF666zm76ORQcn4KF6JNwDfsb6cj97QunbpT1s";
        String clientId="OiJI3KdONEN9jML6Mi6r6iQTpR8mIOBv3YgsUhdv";
        //Initialize with keys
        Parse.initialize(this, appId, clientId);
        db=ParseDB.getInstance(this);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                if (db.isUserSignIn()) {
                    if(db.getCurrentUserBuildingCode()==null){
                        Intent i = new Intent(getApplicationContext(), NotInBuilding.class);
                        startActivity(i);
                        MainPreloader.this.finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }else{
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                        MainPreloader.this.finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                } else {
                    Intent i = new Intent(getApplicationContext(), MainLoginScreen.class);
                    startActivity(i);
                    MainPreloader.this.finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            }
        }, SPLASH_DISPLAY_LENGTH);

    }
    @Override
    public void onBackPressed() {
        //Disable the back button
    }

    @Override
    protected void onStart() {
        final ImageView homeAnim=(ImageView)findViewById(R.id.main_loader_image);

        ImageView textAnim=(ImageView)findViewById(R.id.main_text_image);
        textAnim.setBackgroundResource(R.drawable.fbf_anim);
        AnimationDrawable frameAnimation = (AnimationDrawable) textAnim.getBackground();
        frameAnimation.start();

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                homeAnim.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                final int targetHeight = homeAnim.getMeasuredHeight();

                homeAnim.getLayoutParams().height = 0;
                homeAnim.setVisibility(View.VISIBLE);
                Animation a = new Animation()
                {
                    @Override
                    protected void applyTransformation(float interpolatedTime, Transformation t) {
                        homeAnim.getLayoutParams().height = interpolatedTime == 1
                                ? RelativeLayout.LayoutParams.WRAP_CONTENT
                                : (int)(targetHeight * interpolatedTime);
                        homeAnim.requestLayout();
                    }

                    @Override
                    public boolean willChangeBounds() {
                        return true;
                    }
                };

                // 1dp/ms
                a.setDuration(((int)(targetHeight / homeAnim.getContext().getResources().getDisplayMetrics().density))*6);
                homeAnim.startAnimation(a);
            }
        }, 1160);
        super.onStart();
    }
}

