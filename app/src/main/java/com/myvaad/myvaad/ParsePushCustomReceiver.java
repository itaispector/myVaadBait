package com.myvaad.myvaad;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;


public class ParsePushCustomReceiver extends ParsePushBroadcastReceiver {

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

            }
        } catch (JSONException e) {

            e.printStackTrace();
        }
    }


}
