package com.myvaad.myvaad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;

import dialogs.AboutDialog;

public class UserProfile extends Activity{
    ParseDB db;
    ImageView userImg,userNameEditIcon,userNameEditVIcon;
    TextView userName;
    EditText userNameEdit;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#007ca2"));
        getActionBar().setBackgroundDrawable(colorDrawable);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        //To connect with parse - we need to provide 2 keys: appId & clientId
        String appId="QdwF666zm76ORQcn4KF6JNwDfsb6cj97QunbpT1s";
        String clientId="OiJI3KdONEN9jML6Mi6r6iQTpR8mIOBv3YgsUhdv";
        //Initialize with keys
        Parse.initialize(this, appId, clientId);
        db=ParseDB.getInstance(this);
        userName=(TextView)findViewById(R.id.user_propile_name);
        userImg=(ImageView)findViewById(R.id.userProimgView);
        userNameEditIcon=(ImageView)findViewById(R.id.edit_user_name_icon);
        userNameEditVIcon=(ImageView)findViewById(R.id.edit_user_name_v_icon);
        userNameEdit=(EditText)findViewById(R.id.user_propile_name_edit_text);
        imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        this.setTitle(R.string.UserProfileTitle);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/GISHA.TTF");
        userName.setTypeface(font);
        String famText=getString(R.string.family);
        userName.setText(famText+" "+db.getcurrentUserFamilyName());
        userImg.setImageBitmap(db.getcurrentUserPicture());
        userNameEdit.setTypeface(font);
        userNameEditIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userNameEditIcon.setVisibility(View.GONE);
                userNameEditVIcon.setVisibility(View.VISIBLE);
                userName.setVisibility(View.GONE);
                userNameEdit.setVisibility(View.VISIBLE);
                userNameEdit.setText(db.getcurrentUserFamilyName());
                userNameEdit.requestFocus();
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            }
        });
        userNameEditVIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userNameEditIcon.setVisibility(View.VISIBLE);
                userNameEditVIcon.setVisibility(View.GONE);
                db.updateUserFamilyName(userNameEdit.getText().toString());
                userNameEdit.setVisibility(View.GONE);
                userName.setVisibility(View.VISIBLE);
                userName.setText(db.getcurrentUserFamilyName());
                imm.hideSoftInputFromWindow(userNameEdit.getWindowToken(), 0);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about:
                AboutDialog dialog=new AboutDialog(this);
                dialog.showAboutDialog();
                break;
            case android.R.id.home:
                finish();
                imm.hideSoftInputFromWindow(userNameEdit.getWindowToken(), 0);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                break;
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        imm.hideSoftInputFromWindow(userNameEdit.getWindowToken(), 0);
        overridePendingTransition(R.anim.anim_slide_in_right,R.anim.anim_slide_out_right);
    }

    @Override
    protected void onPause() {
        imm.hideSoftInputFromWindow(userNameEdit.getWindowToken(), 0);
        super.onPause();
    }

    @Override
    protected void onStop() {
        imm.hideSoftInputFromWindow(userNameEdit.getWindowToken(), 0);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        imm.hideSoftInputFromWindow(userNameEdit.getWindowToken(), 0);
        super.onDestroy();
    }
}
