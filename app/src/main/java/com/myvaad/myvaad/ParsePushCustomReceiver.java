package com.myvaad.myvaad;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;


public class ParsePushCustomReceiver extends ParsePushBroadcastReceiver {

    Fragment fragment1 = new FailuresScreen();
    Fragment fragment2 = new ExpensesScreen();
    Fragment fragment3 = new PaymentsScreen();
    Fragment fragment4 = new UsersScreen();
    FragmentManager fragmentManager;
    protected  static  String pushTitle="";



    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);

        pushTitle="";
        try {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String jsonData = extras.getString("com.parse.Data");
                JSONObject json;
                json = new JSONObject(jsonData);
                pushTitle = json.getString("title");
                String pushContent = json.getString("alert");
                //Toast toast = Toast.makeText(context, "push title: "+pushTitle+" push content: "+pushContent , Toast.LENGTH_SHORT);
                //toast.setGravity(Gravity.CENTER, 0, 150);
               // toast.show();
            }
        } catch (JSONException e) {

            e.printStackTrace();
        }
    }


}
