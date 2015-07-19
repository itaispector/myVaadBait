package com.myvaad.myvaad;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.rey.material.widget.ProgressView;
import com.rey.material.widget.Switch;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

public class UserProfile extends AppCompatActivity {

    ParseDB db;
    private static int RESULT_LOAD_IMAGE = 1;
    private String picturePath;
    ImageView goBackBtn, userProfileImg;
    RelativeLayout userPaypal;
    EditText password, password2;
    Bitmap bm, bmp;
    ProgressView loader;
    Switch getPushes;
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
        getPushes = (Switch) findViewById(R.id.user_push_on_off);
        loader = (ProgressView) findViewById(R.id.progress_loader);


        bm = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);


        getPushes.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch aSwitch, boolean b) {
                db.saveUserInstallationPushActive(b);
            }
        });


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


        getPushes.setChecked(ParseInstallation.getCurrentInstallation().getBoolean("active"));


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
                                        ParseUser currentUser = ParseUser.getCurrentUser();
                                        currentUser.setPassword("123456");
                                        currentUser.saveInBackground();
                                        currentUser.logOut();
                                        Intent i = new Intent(getApplicationContext(), MainLoginScreen.class);
                                        startActivity(i);
                                        UserProfile.this.finish();
                                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
                .title("יציאה מהבניין")
                .positiveText("כן")
                .neutralText("לא")
                .content("האם את/ה בטוח/ה שברצונך לצאת מהבניין?")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        db.userLeaveBuildingCode();
                        Intent i = new Intent(getApplicationContext(), NotInBuilding.class);
                        startActivity(i);
                        UserProfile.this.finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    //Circle image method
    private Bitmap getCircularBitmap(int radius, Bitmap bitmap) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        bmp = Bitmap.createBitmap(radius, radius, conf);
        Canvas canvas = new Canvas(bmp);
        // creates a centered bitmap of the desired size
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, radius, radius, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);
        RectF rect = new RectF(0, 0, radius, radius);
        canvas.drawRoundRect(rect, radius, radius, paint);
        bm = bmp;
        return bmp;
    }

    protected byte[] convertImageToByteArray(Bitmap bitmap) {
        byte[] byteImage = null;
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.androidbegin);
        //Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
        // Convert it to byte
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Compress image to lower quality scale 1 - 100
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteImage = stream.toByteArray();

        return byteImage;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            loader.setVisibility(View.VISIBLE);
            loader.start();
            if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);
                cursor.close();
                bm = BitmapFactory.decodeFile(picturePath);
                int nh = (int) (bm.getHeight() * (222.0 / bm.getWidth()));
                bm = Bitmap.createScaledBitmap(bm, 222, nh, true);

                final Bitmap bbb=getCircularBitmap(222, bm);


                byte[] image = convertImageToByteArray(bbb);
                final ParseFile file = new ParseFile("userImage" + ".png", image);

                ParseUser object = db.getcurrentUser();
                object.put("picture",file);
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        loader.stop();
                        userProfileImg.setImageBitmap(bbb);
                        Toast.makeText(getApplicationContext(), "סיים", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }catch (Exception e){
            loader.stop();
            Toast.makeText(this, "משהו השתבש אנא נסה/י שנית!", Toast.LENGTH_LONG)
                    .show();
            Log.v("***Image Pick Error***",e.getMessage());
        }

    }

    public void changeImage(View view) {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    public void deleteImage(View view) {
        MaterialDialog.Builder buildingDialog = new MaterialDialog.Builder(this);
        buildingDialog.titleGravity(GravityEnum.END)
                .contentGravity(GravityEnum.END)
                .positiveColorRes(R.color.colorPrimary)
                .neutralColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorPrimary)
                .widgetColorRes(R.color.colorPrimary)
                .title("מחיקת תמונה")
                .positiveText("כן")
                .neutralText("לא")
                .content("האם את/ה בטוח/ה שברצונך למחוק את התמונה?")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        loader.setVisibility(View.VISIBLE);
                        loader.start();
                        bm = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);

                        byte[] image = convertImageToByteArray(bm);
                        final ParseFile file = new ParseFile("userImage" + ".png", image);

                        ParseUser object = db.getcurrentUser();
                        object.put("picture",file);
                        object.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                loader.stop();
                                userProfileImg.setImageBitmap(getCircularBitmap(100, bm));
                            }
                        });
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
