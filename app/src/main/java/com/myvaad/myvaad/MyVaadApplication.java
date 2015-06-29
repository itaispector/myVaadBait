package com.myvaad.myvaad;

import android.app.Application;
import android.view.View;

import com.parse.Parse;
import com.parse.PushService;

public class MyVaadApplication extends Application{
    @Override
    public void onCreate() {
        Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
        PushService.setDefaultPushCallback(this, MainPreloader.class);
    }
}