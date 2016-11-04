package com.ogp.cputableau2;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;


public class StartActivity extends Activity {
    private static final int SETTINGS_RESULT_EXPECTED = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();

        checkAlertPermission();
    }


    private boolean isAlertGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Settings.canDrawOverlays(this);
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PackageManager.PERMISSION_GRANTED;
        }
    }


    private void killMe() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    private void checkAlertPermission() {
        if (isAlertGranted()) {
            Intent startIntent = new Intent(this, CPUTableauActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
            finish();
        } else {
            String command = String.format("appops set %s SYSTEM_ALERT_WINDOW allow\n", getPackageName());
            RootShell.executeOnRoot(command);

            if (isAlertGranted()) {
                Intent startIntent = new Intent(this, CPUTableauActivity.class);
                startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startIntent);
                finish();
            } else {
                Toast.makeText(this, R.string.have_to_kill, Toast.LENGTH_LONG).show();
                killMe();
            }
        }
    }
}
