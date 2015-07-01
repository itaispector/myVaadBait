package com.myvaad.myvaad;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import com.parse.Parse;
import dialogs.AboutDialog;

public class UserProfile extends AppCompatActivity {

    ParseDB db;
    TextView userName;
    EditText userNameEdit;
    InputMethodManager imm;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        if (getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        // enable ToolBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back_btn);


        //Initialize with keys
        Parse.initialize(this);
        db=ParseDB.getInstance(this);

        /*
        userName=(TextView)findViewById(R.id.user_propile_name);
        userImg=(ImageView)findViewById(R.id.userProimgView);
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
        });*/
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
