package com.myvaad.myvaad;

import android.app.Fragment;
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
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class SignUp extends Fragment {
    private static int RESULT_LOAD_IMAGE = 1;
    private String picturePath;
    EditText fullNameInput, userNameInput, emailInput, passwordInput, passwordInput2;
    TextView passwordVerifyText;
    ImageView imageView, userVerifyImage,userNameCheckLoader;
    Bitmap bm, bmp;
    Toast toast;
    Button signupBtn;
    View rootView;
    ParseDB db;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.signup_screen, container, false);
        fullNameInput = (EditText) rootView.findViewById(R.id.SignUpScreenFullName);
        userNameInput = (EditText) rootView.findViewById(R.id.SignUpScreenUserName);
        emailInput = (EditText) rootView.findViewById(R.id.SignUpScreenEmail);
        passwordInput = (EditText) rootView.findViewById(R.id.SignUpScreenPassword);
        passwordInput2 = (EditText) rootView.findViewById(R.id.SignUpScreenPassword2);
        imageView = (ImageView) rootView.findViewById(R.id.imgView);
        userNameCheckLoader = (ImageView) rootView.findViewById(R.id.userVerifyImageLoader);
        userVerifyImage = (ImageView) rootView.findViewById(R.id.userVerifyImage);
        passwordVerifyText = (TextView) rootView.findViewById(R.id.passwordVerifyText);
        signupBtn = (Button) rootView.findViewById(R.id.SignUpScreenSignUpBtn);
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
        bm = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);
        imageView.setImageBitmap(getCircularBitmap(100, bm));

        // Enable Local Datastore.
        //Parse.enableLocalDatastore(this);
        db = ParseDB.getInstance(getActivity());
        //Initialize with keys
        Parse.initialize(getActivity());

        Button buttonLoadImage = (Button) rootView.findViewById(R.id.buttonLoadPicture);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });


        userNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (userNameInput.getText().toString().length() == 0) {
                    userNameCheckLoader.clearAnimation();
                    userNameCheckLoader.setVisibility(View.GONE);
                    userVerifyImage.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String username = userNameInput.getText().toString();
                if (!username.matches("[_a-zA-Z0-9]*")) {
                    if (toast == null || toast.getView().getWindowVisibility() != View.VISIBLE) {
                        toast = Toast.makeText(getActivity(), R.string.cant_use_hebrew_username, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 150);
                        toast.show();
                    }
                 } else {
                    userVerifyImage.setVisibility(View.GONE);
                    userNameCheckLoader.setVisibility(View.VISIBLE);
                    Animation rotation = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate);
                    userNameCheckLoader.startAnimation(rotation);
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.cancel();
                    query.whereEqualTo("username", username);
                    query.getFirstInBackground(new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            if (e == null) {
                                userNameCheckLoader.clearAnimation();
                                userNameCheckLoader.setVisibility(View.GONE);
                                userVerifyImage.setImageResource(R.drawable.x);
                                userVerifyImage.setVisibility(View.VISIBLE);
                            } else {
                                userNameCheckLoader.clearAnimation();
                                userNameCheckLoader.setVisibility(View.GONE);
                                userVerifyImage.setImageResource(R.drawable.v);
                                userVerifyImage.setVisibility(View.VISIBLE);
                            }
                            if (username.length() == 0) {
                                userNameCheckLoader.clearAnimation();
                                userNameCheckLoader.setVisibility(View.GONE);
                                userVerifyImage.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                if (userNameInput.getText().toString().length() == 0) {
                    userNameCheckLoader.clearAnimation();
                    userNameCheckLoader.setVisibility(View.GONE);
                    userVerifyImage.setVisibility(View.GONE);
                }
            }
        });

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwordInput.getText().length() == 0) {
                    passwordInput.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                } else {
                    passwordInput.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        passwordInput2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwordInput2.getText().length() == 0) {
                    passwordInput2.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                } else {
                    passwordInput2.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });



        return rootView;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == getActivity().RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
            bm = BitmapFactory.decodeFile(picturePath);
            int nh = (int) (bm.getHeight() * (222.0 / bm.getWidth()));
            bm = Bitmap.createScaledBitmap(bm, 222, nh, true);
            imageView.setImageBitmap(getCircularBitmap(222, bm));
        }
    }

    public void signup() {
        String passin1 = passwordInput.getText().toString();
        String passin2 = passwordInput2.getText().toString();
        String fullName = fullNameInput.getText().toString();
        String userName = userNameInput.getText().toString();
        String email = emailInput.getText().toString();
        if (fullName.matches("") || email.matches("") || userName.matches("") || passin1.matches("") || passin2.matches("")) {
            toast = Toast.makeText(getActivity(), R.string.empty_edittext_msg, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else if (!passin1.matches(passin2)) {
            passwordVerifyText.setVisibility(View.VISIBLE);
            passwordVerifyText.setText(R.string.passwordVerifyText);
        } else {
            passwordVerifyText.setVisibility(View.GONE);
            db.signUpUser(userName, passin1, email, fullName, bm, getActivity());
        }
    }
}
