package com.myvaad.myvaad;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.Parse;

import java.net.URI;

import dialogs.RingProgressDialog;

public class Login extends Fragment {
    View rootView;
    EditText userNameInput, passwordInput;
    ParseDB db;
    RingProgressDialog dialog;
    Toast toast;
    Button ok, loginBtn, signUpBtn;
    TextView forgotPass;
    Button cancel;
    View dialogLayout;
    Dialog resPassDialog;
    WebView webView;
    DrawerLayout mDrawerLayout;
    protected ListView mDrawerList;
    String[] mPagesTitles;
    private ActionBarDrawerToggle mDrawerToggle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.main_login_screen, container, false);
        //Initialize with keys
        Parse.initialize(getActivity());
        db = ParseDB.getInstance(getActivity());

        final SignUp signUpFragment = new SignUp();
        userNameInput = (EditText) rootView.findViewById(R.id.MainLoginScreenUserName);
        passwordInput = (EditText) rootView.findViewById(R.id.MainLoginScreenPassword);
        forgotPass = (TextView) rootView.findViewById(R.id.forgotPassword);
        loginBtn = (Button) rootView.findViewById(R.id.MainLoginScreenLoginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkAvailable()) {
                    toast = Toast.makeText(getActivity(), R.string.no_internet_connection_msg, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    String userName = userNameInput.getText().toString();
                    String password = passwordInput.getText().toString();
                    if (userName.matches("")) {
                        toast = Toast.makeText(getActivity(), R.string.login_username_input_verify, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else if (password.matches("")) {
                        toast = Toast.makeText(getActivity(), R.string.login_password_input_verify, Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    } else {
                        db.LogInUser(userName, password, getActivity());
                    }
                }
            }
        });
        signUpBtn = (Button) rootView.findViewById(R.id.MainLoginScreenSignUpBtn);
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check if internet is available
                if (!isNetworkAvailable()) {
                    toast = Toast.makeText(getActivity(), R.string.no_internet_connection_msg, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    getFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.gla_there_come, R.anim.gla_there_gone, R.anim.gla_back_gone, R.anim.gla_back_come)
                            .replace(R.id.content_frame, signUpFragment)
                            .addToBackStack("LoginFragment")
                            .commit();
                }
            }
        });
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPass();
            }
        });
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwordInput.getText().length() == 0) {
                    passwordInput.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                } else {
                    passwordInput.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return rootView;
    }

    //Check if internet is available method
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void forgotPass() {
        new MaterialDialog.Builder(getActivity())
                .titleGravity(GravityEnum.END)
                .contentGravity(GravityEnum.END)
                .positiveColorRes(R.color.colorPrimary)
                .neutralColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .title("איפוס סיסמה")
                .content("אנא הכנס/י כתובת אימייל")
                .positiveText("אפס סיסמה")
                .negativeText("ביטול")
                .btnStackedGravity(GravityEnum.START)
                .buttonsGravity(GravityEnum.END)
                .alwaysCallInputCallback() // this forces the callback to be invoked with every input change
                .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                .input(R.string.email, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        materialDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);

                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String text = dialog.getInputEditText().getText().toString();
                        if (!text.matches("^(([_a-zA-Z0-9][.]?){1,})([^.][@]{1})(([a-zA-Z0-9]?){1,})([^.][.]{1})(([a-zA-Z0-9]?){1,})$")) {
                            toast = Toast.makeText(getActivity(), R.string.empty_email, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            //Exit from keyboard when the dialog dismissed
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        } else {
                            db.resetUserPassword(text);
                            //Exit from keyboard when the dialog dismissed
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                            toast = Toast.makeText(getActivity(), R.string.email_sent, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        //Exit from keyboard when the dialog dismissed
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(dialog.getInputEditText().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                })
                .show();
    }
}
