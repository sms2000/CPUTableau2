package com.ogp.cputableau2.servlets;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ogp.cputableau2.global.Constants;


public class WorkerThread extends Thread {
    private static final long WAIT_HANDLER = 100;
    private Handler handler = null;


    public WorkerThread() {
        setDaemon(true);
        start();
    }


    public void kill() {
        handler.getLooper().quit();
    }


    public void post(Runnable runnable) {
        while (null == handler) {
            try {
                Thread.sleep(WAIT_HANDLER);
            } catch (InterruptedException ignored) {
            }
        }

        handler.post(runnable);
    }


    @SuppressWarnings("WeakerAccess")
    public void postDelayed(Runnable runnable, int delay) {
        handler.postDelayed(runnable, delay);
    }


    @Override
    public void run() {
        Log.v(Constants.TAG, "WorkerThread::run. Entry...");

        Looper.prepare();
        handler = new Handler();
        Looper.loop();

        Log.v(Constants.TAG, "WorkerThread::run. Exit.");
    }
}


