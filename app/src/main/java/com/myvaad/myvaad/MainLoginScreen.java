package com.myvaad.myvaad;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;

import dialogs.AboutDialog;
import dialogs.RingProgressDialog;

public class MainLoginScreen extends Activity{
    EditText userNameInput,passwordInput;
    ParseDB db;
    RingProgressDialog dialog;
    Toast toast;
    Button ok;
    Button cancel;
    View dialogLayout;
    Dialog resPassDialog;
    DrawerLayout mDrawerLayout;
    protected ListView mDrawerList;
    String[] mPagesTitles;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_login_screen);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#007ca2"));
        getActionBar().setBackgroundDrawable(colorDrawable);
        userNameInput=(EditText)findViewById(R.id.MainLoginScreenUserName);
        passwordInput=(EditText)findViewById(R.id.MainLoginScreenPassword);
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(passwordInput.getText().length()==0){
                    passwordInput.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                }else{
                    passwordInput.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        //Initialize with keys
        Parse.initialize(this);
        db=ParseDB.getInstance(this);
    }
    //Check if internet is available method
    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo=connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    public void login(View v){
        if(!isNetworkAvailable()){
            toast=Toast.makeText(this, R.string.no_internet_connection_msg, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }else{
            String userName=userNameInput.getText().toString();
            String password=passwordInput.getText().toString();
            if(userName.matches("")){
                toast=Toast.makeText(this,R.string.login_username_input_verify,Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }else if(password.matches("")){
                toast=Toast.makeText(this,R.string.login_password_input_verify,Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }else{
                db.LogInUser(userName,password,this);
            }
        }
    }
    public void signup(View v){
        //Check if internet is available
        if(!isNetworkAvailable()){
            toast=Toast.makeText(this,R.string.no_internet_connection_msg,Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }else{
            Intent i = new Intent(this, SignUpScreen.class);
            startActivity(i);
        }
    }
    public void forgotPass(View v){
        dialogLayout = View.inflate(this, R.layout.login_screen_forgot_password_dialog, null);
        resPassDialog = new Dialog(this);
        resPassDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        resPassDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        resPassDialog.setContentView(dialogLayout);
        resPassDialog.show();

        ok = (Button)dialogLayout.findViewById(R.id.forgotPasswordOkBtn);
        cancel = (Button)dialogLayout.findViewById(R.id.forgotPasswordCancelBtn);

        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogButtons(v);

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogButtons(v);

            }
        });
    }
    public void dialogButtons(View v) {
        EditText msg=(EditText)dialogLayout.findViewById(R.id.forgotPasswordEmail);
        String text=msg.getText().toString();
        if (v.getId() == R.id.forgotPasswordCancelBtn) {
            //Exit from keyboard when the dialog dismissed
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(msg.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            resPassDialog.dismiss();
        }else {
            if(!text.matches("")){
                db.resetUserPassword(text);
                //Exit from keyboard when the dialog dismissed
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(msg.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                resPassDialog.dismiss();
            }else{
                toast=Toast.makeText(this,R.string.empty_email,Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                //Exit from keyboard when the dialog dismissed
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(msg.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about:
                AboutDialog dialog=new AboutDialog(this);
                dialog.showAboutDialog();
        }
        return super.onOptionsItemSelected(item);
    }
}
