package com.myvaad.myvaad;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class UserProfile extends AppCompatActivity {

    ParseDB db;
    ImageView goBackBtn,userProfileImg;
    RelativeLayout userPaypal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);


        if (getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        db=ParseDB.getInstance(this);

        userPaypal = (RelativeLayout)findViewById(R.id.user_paypal_profile_layout);
        if(db.isCurrentUserAdmin()){
            userPaypal.setVisibility(View.VISIBLE);
        }

        userProfileImg = (ImageView)findViewById(R.id.user_profile_img);

        userProfileImg.setImageBitmap(db.getcurrentUserPicture());

        goBackBtn = (ImageView)findViewById(R.id.go_back_btn);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_right);
    }

    public void changeUserName(View view) {

    }

    public void changeUserEmail(View view) {
    }

    public void changeUserFamily(View view) {
    }

    public void changeUserPassword(View view) {
    }

    public void changeUserPaypal(View view) {
    }

    public void changeUserBuildingCode(View view) {
    }
}
