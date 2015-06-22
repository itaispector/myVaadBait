package com.myvaad.myvaad;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;

import dialogs.AboutDialog;

public class SignUpScreen extends Activity {
    private static int RESULT_LOAD_IMAGE = 1;
    private String picturePath;
    EditText fullNameInput,userNameInput,emailInput,passwordInput,passwordInput2;
    TextView passwordVerifyText;
    ImageView imageView,userVerifyImage;
    Bitmap bm,bmp;
    Toast toast;
    ParseDB db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_screen);
        fullNameInput=(EditText)findViewById(R.id.SignUpScreenFullName);
        userNameInput=(EditText)findViewById(R.id.SignUpScreenUserName);
        emailInput=(EditText)findViewById(R.id.SignUpScreenEmail);
        passwordInput=(EditText)findViewById(R.id.SignUpScreenPassword);
        passwordInput2=(EditText)findViewById(R.id.SignUpScreenPassword2);
        imageView = (ImageView) findViewById(R.id.imgView);
        userVerifyImage = (ImageView) findViewById(R.id.userVerifyImage);
        passwordVerifyText=(TextView)findViewById(R.id.passwordVerifyText);
        bm = BitmapFactory.decodeResource(getResources(),R.drawable.no_image);
        imageView.setImageBitmap(getCircularBitmap(100,bm));

        //Action bar background
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor("#007ca2"));
        getActionBar().setBackgroundDrawable(colorDrawable);

        // Enable Local Datastore.
        //Parse.enableLocalDatastore(this);
        db=ParseDB.getInstance(this);
        //To connect with parse - we need to provide 2 keys: appId & clientId
        String appId="QdwF666zm76ORQcn4KF6JNwDfsb6cj97QunbpT1s";
        String clientId="OiJI3KdONEN9jML6Mi6r6iQTpR8mIOBv3YgsUhdv";
        //Initialize with keys
        Parse.initialize(this, appId, clientId);
        Button buttonLoadImage = (Button) findViewById(R.id.buttonLoadPicture);
        buttonLoadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        passwordInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    String username=userNameInput.getText().toString();
                    if(!username.matches("[_a-zA-Z0-9]*")){
                        toast=Toast.makeText(getApplicationContext(),R.string.cant_use_hebrew_username,Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 150);
                        toast.show();
                    }else{
                        boolean checkuser=db.isUserExists(username);
                        if(checkuser){
                            userVerifyImage.setImageResource(R.drawable.x);
                        }else{
                            userVerifyImage.setImageResource(R.drawable.v);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

            bm = BitmapFactory.decodeFile(picturePath);
            int nh = (int) ( bm.getHeight() * (222.0 / bm.getWidth()) );
            bm = Bitmap.createScaledBitmap(bm, 222, nh, true);
            imageView.setImageBitmap(getCircularBitmap(222,bm));
        }
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
        bm=bmp;
        return bmp;
    }
    public void signup(View v){
        String passin1=passwordInput.getText().toString();
        String passin2=passwordInput2.getText().toString();
        String fullName=fullNameInput.getText().toString();
        String userName=userNameInput.getText().toString();
        String email=emailInput.getText().toString();

        if(fullName.matches("")||email.matches("")||userName.matches("")||passin1.matches("")||passin2.matches("")){
            toast=Toast.makeText(this,R.string.empty_edittext_msg,Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }else if(!passin1.matches(passin2)){
            passwordVerifyText.setVisibility(View.VISIBLE);
            passwordVerifyText.setText(R.string.passwordVerifyText);
        }else{
            passwordVerifyText.setVisibility(View.GONE);
            db.signUpUser(userName,passin1,email,fullName,bm,this);
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
