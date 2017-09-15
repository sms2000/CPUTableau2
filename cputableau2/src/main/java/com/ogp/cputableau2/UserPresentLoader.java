package com.ogp.cputableau2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ogp.cputableau2.settings.LocalSettings;


public class UserPresentLoader extends BroadcastReceiver {
    private static final String TAG = "UserPresentLoader";


    public UserPresentLoader() {
        super();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive. Entry...");

        try {
            String str = intent.getAction();
            if (str.equals("android.intent.action.USER_PRESENT")) {
                LocalSettings.init(context);

                Log.d(TAG, "onReceive. User activated. Starting service if destroyed.");
                CPUTableauService.loadService(context);
            }

        } catch (Exception ignored) {
        }

        Log.v(TAG, "onReceive. Exit.");
    }
}
