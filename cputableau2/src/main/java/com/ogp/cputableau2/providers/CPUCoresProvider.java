package com.ogp.cputableau2.providers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.annotation.SuppressLint;
import android.util.Log;

import com.ogp.cputableau2.su.RootCaller;

public class CPUCoresProvider extends HWProvider {
    private static final String TAG = "CPUCoresProvider";

    private static final String onlineFilesFormat = "/sys/devices/system/cpu/cpu%d/online";

    private static final int MAX_CORES = 16;


    private static String[] onlineFiles = new String[MAX_CORES];
    private static Integer maxCore = -1;
    private static Boolean coresError = false;


    @SuppressLint("DefaultLocale")
    public CPUCoresProvider() {
    }


    @Override
    public void init(RootCaller.RootExecutor rootExecutor) {
        super.init(rootExecutor);

        synchronized (this) {
            if (0 > maxCore) {
                for (int i = 0; i < MAX_CORES; i++) {
                    String cpu = String.format(onlineFilesFormat, i);
                    String output = readFileStringRoot(cpu);
                    if (null != output && !output.isEmpty()) {
                        onlineFiles[i] = String.format(onlineFilesFormat, ++maxCore);
                    }
                }
            }
        }

        Log.w(TAG, String.format("CPUCoresProvider. Found %d cores.", maxCore + 1));
    }


    @Override
    public void clear() {
    }


    @Override
    public String getData() {
        String coreData = "";
        boolean discoveredCores = false;


        for (int i = 0; i <= maxCore; i++) {
            String output = readFileStringRoot(onlineFiles[i]);
            if (null != output && !output.isEmpty()) {
                if (0 < i) {
                    coreData += "-";
                }

                coreData += output;
                discoveredCores = true;
            }
        }

        Log.v(TAG, String.format("getData. Cores activity found: [%s].", coreData));
        return discoveredCores ? coreData : "1";
    }
}
