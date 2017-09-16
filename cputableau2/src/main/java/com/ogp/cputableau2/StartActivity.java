package com.ogp.cputableau2;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.ogp.cputableau2.settings.LocalSettings;
import com.ogp.cputableau2.su.RootCaller;


public class StartActivity extends Activity {
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.Theme_AppCompat);
        setContentView(R.layout.activity_start);

        LocalSettings.init(getApplicationContext());
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onResume() {
        super.onResume();

        if (!LocalSettings.isRootObtained()) {
            RootCaller.RootStatus status = RootCaller.ifRootAvailable();
            if (status == RootCaller.RootStatus.NO_ROOT) {
                Toast.makeText(this, R.string.no_root, Toast.LENGTH_LONG).show();
                finish();
                return;
            } else if (status == RootCaller.RootStatus.ROOT_FAILED) {
                Toast.makeText(this, R.string.no_root_granted, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                resumeAll();
            }
        });
    }


    public void onCancel(View _) {
        finish();
    }


    public void onOK(View _) {
        boolean granted = RootCaller.grantMeRoot();
        if (granted) {
            LocalSettings.rootObtained();
            resumeAll();
        } else {
            Toast.makeText(this, R.string.no_root_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }


    private void resumeAll() {
        if (LocalSettings.isRootObtained()) {
            checkAlertPermission();
        }
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
            finishAffinity();
            startActivity(startIntent);
        } else {
            RootCaller.RootExecutor rootProcess = RootCaller.createRootProcess();
            if (null != rootProcess) {
                String command = String.format("appops set %s SYSTEM_ALERT_WINDOW allow\n", getPackageName());
                rootProcess.executeOnRoot(command);

                RootCaller.terminateRootProcess(rootProcess);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }

                if (isAlertGranted()) {
                    Intent startIntent = new Intent(this, CPUTableauActivity.class);
                    startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startIntent);
                    finish();
                    return;
                }


                Toast.makeText(this, R.string.have_to_kill, Toast.LENGTH_LONG).show();
                killMe();
            }
        }
    }
}