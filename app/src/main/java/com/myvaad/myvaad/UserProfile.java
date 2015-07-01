package com.myvaad.myvaad;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.HashMap;

public class UserProfile extends AppCompatActivity {

    ParseDB db;
    ImageView goBackBtn, userProfileImg;
    RelativeLayout userPaypal;
    EditText password, password2;
    TextView userEmailTxt, userFamilyTxt, userPaypalTxt, userBuildingTxt;
    String userEmailString, userFamilyString, userPaypalString, userBuildingCodeString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        userEmailTxt = (TextView) findViewById(R.id.user_propile_email);
        userFamilyTxt = (TextView) findViewById(R.id.user_propile_family);
        userPaypalTxt = (TextView) findViewById(R.id.user_propile_paypal);
        userBuildingTxt = (TextView) findViewById(R.id.user_propile_building);


        if (getWindow().getDecorView().getLayoutDirection() == View.LAYOUT_DIRECTION_LTR) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        db = ParseDB.getInstance(this);

        updatePage();

        goBackBtn = (ImageView) findViewById(R.id.go_back_btn);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void updatePage() {
        userPaypal = (RelativeLayout) findViewById(R.id.user_paypal_profile_layout);
        if (db.isCurrentUserAdmin()) {
            userPaypal.setVisibility(View.VISIBLE);
            userPaypalTxt.setText(db.getVaadPayPalAccount());
        }

        userEmailString = db.getcurrentUserEmail();
        userFamilyString = getResources().getString(R.string.family) + " " + db.getcurrentUserFamilyName();
        userBuildingCodeString = db.getCurrentUserBuildingCode();

        userFamilyTxt.setText(userFamilyString);
        userEmailTxt.setText(userEmailString);
        userBuildingTxt.setText(userBuildingCodeString);

        userProfileImg = (ImageView) findViewById(R.id.user_profile_img);
        userProfileImg.setImageBitmap(db.getcurrentUserPicture());
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 150);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    private void hideKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void changeUserEmail(View view) {
        new MaterialDialog.Builder(this)
                .titleGravity(GravityEnum.END)
                .contentGravity(GravityEnum.END)
                .positiveColorRes(R.color.colorPrimary)
                .neutralColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .title("עריכת כתובת מייל")
                .positiveText("שנה")
                .neutralText("ביטול")
                .input(userEmailString, null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        materialDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String text = dialog.getInputEditText().getText().toString();
                        if (text == userEmailString) {
                        } else {
                            ParseUser currentUser = db.getcurrentUser();
                            currentUser.put("email", text);
                            currentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        int errorCode = e.getCode();
                                        switch (errorCode) {
                                            case 203:
                                                Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.emailTaken), Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER, 0, 150);
                                                toast.show();
                                                break;
                                            case 125:
                                                toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.InvalidEmail), Toast.LENGTH_SHORT);
                                                toast.setGravity(Gravity.CENTER, 0, 150);
                                                toast.show();
                                                break;
                                            default:
                                                Log.e("**Exception CODE**", errorCode + "");
                                                Log.e("*****Exception*****", e.getLocalizedMessage());
                                                break;
                                        }
                                    } else {
                                        updatePage();
                                    }
                                }
                            });

                        }
                        hideKeyboard(dialog.getInputEditText());
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        hideKeyboard(dialog.getInputEditText());
                    }
                })
                .show();
    }

    public void changeUserFamily(View view) {
        new MaterialDialog.Builder(this)
                .titleGravity(GravityEnum.END)
                .contentGravity(GravityEnum.END)
                .positiveColorRes(R.color.colorPrimary)
                .neutralColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .title("עריכת שם משפחה")
                .positiveText("שנה")
                .neutralText("ביטול")
                .input(userFamilyString, null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        materialDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String text = dialog.getInputEditText().getText().toString();
                        if (text == userFamilyString) {
                        } else {
                            db.updateUserFamilyName(text);
                            db.updateUserFamilyNameInNotices(db.getcurrentUser(), text);
                            updatePage();
                        }
                        hideKeyboard(dialog.getInputEditText());
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        hideKeyboard(dialog.getInputEditText());
                    }
                })
                .show();
    }

    public void changeUserPassword(View view) {
        new MaterialDialog.Builder(this)
                .titleGravity(GravityEnum.END)
                .contentGravity(GravityEnum.END)
                .positiveColorRes(R.color.colorPrimary)
                .neutralColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .title("עריכת סיסמה")
                .positiveText("המשך")
                .neutralText("ביטול")
                .input("סיסמה נוכחית", null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {

                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(final MaterialDialog dialog) {
                        hideKeyboard(dialog.getInputEditText());
                        final String pass = dialog.getInputEditText().getText().toString();
                        HashMap<String, Object> params = new HashMap<String, Object>();
                        params.put("password", pass);
                        params.put("objectId", db.getcurrentUserName());
                        ParseCloud.callFunctionInBackground("checkIfPasswordMatch", params, new FunctionCallback<Boolean>() {
                            @Override
                            public void done(Boolean result, ParseException e) {
                                if (e == null) {
                                    if (result) {
                                        toast("תואם!!!");
                                    } else {
                                        toast("טעות");
                                    }
                                } else {
                                    toast("שגיאה!@");
                                }
                            }
                        });
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        hideKeyboard(dialog.getInputEditText());
                    }
                })
                .show();
    }

    public void changeUserPaypal(View view) {
    }

    public void changeUserBuildingCode(View view) {
        MaterialDialog.Builder buildingDialog = new MaterialDialog.Builder(this);
        buildingDialog.titleGravity(GravityEnum.END)
                .contentGravity(GravityEnum.END)
                .positiveColorRes(R.color.colorPrimary)
                .neutralColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .title("עריכת קוד בניין")
                .positiveText("שנה")
                .negativeText("צא מהבניין")
                .neutralText("ביטול")
                .input(userBuildingCodeString, null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        materialDialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        new MaterialDialog.Builder(getApplicationContext())
                                .titleGravity(GravityEnum.END)
                                .contentGravity(GravityEnum.END)
                                .positiveColorRes(R.color.colorPrimary)
                                .neutralColorRes(R.color.colorPrimary)
                                .negativeColorRes(R.color.colorPrimary)
                                .widgetColorRes(R.color.colorPrimary)
                                .content("האם את/ה בטוח/ה שאת/ה רוצה לצאת מהבניין?")
                                .positiveText(R.string.yes)
                                .negativeText(R.string.no)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        toast("יצאת מהבניין");
                                        dialog.dismiss();
                                    }

                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                })
                .show();
    }

}
