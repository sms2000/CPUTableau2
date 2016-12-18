package com.ogp.cputableau2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ogp.cputableau2.settings.LocalSettings;


public class BootLoader extends BroadcastReceiver {
    private static final String TAG = "BootLoader";


    public BootLoader() {
        super();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive. Entry...");

        try {
            String str = intent.getAction();
            if (str.equals("android.intent.action.BOOT_COMPLETED")) {
                LocalSettings.init(context);

                Log.d(TAG, "onReceive. Boot complete. Starting service...");
                CPUTableauService.loadService(context);
            }

        } catch (Exception ignored) {
        }

        Log.v(TAG, "onReceive. Exit.");
    }
}
