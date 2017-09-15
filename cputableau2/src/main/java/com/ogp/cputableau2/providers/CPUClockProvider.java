package com.ogp.cputableau2.providers;

import com.ogp.cputableau2.ShellInterface;
import com.ogp.cputableau2.su.RootCaller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

public class CPUClockProvider extends HWProvider {
    private static final String TAG = "CPUClockProvider";

    private static final String freqFiles = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    private static final int KHZ2MHZ = 1000;


    public CPUClockProvider() {
    }


    @Override
    public void init(RootCaller.RootExecutor rootExecutor) {
        super.init(rootExecutor);

        try {
            String output = readFileStringRoot(freqFiles);
            if (null != output && !output.isEmpty()) {
                Log.w(TAG, "CPUClockProvider. CPU clock file found.");
            } else {
                Log.e(TAG, "CPUClockProvider. CPU clock file not found.");
            }
        } catch (Exception e) {
            Log.e(TAG, "CPUClockProvider. EXC(1)");
        }
    }


    @Override
    public void clear() {
    }


    @SuppressLint("DefaultLocale")
    public String getData() {
        try {
            String output = readFileStringRoot(freqFiles);
            if (null != output && !output.isEmpty()) {
                int result = Integer.decode(output) / KHZ2MHZ;

                if (result <= 0) {
                    Log.e(TAG, "getData. Error recognizing CPU clock.");
                } else {
                    Log.v(TAG, String.format("getData. CPU clock recognized: %d MHz", result));
                    return String.format("%d MHz", result);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "getData. EXC(1)");
        }

        return null;
    }
}
