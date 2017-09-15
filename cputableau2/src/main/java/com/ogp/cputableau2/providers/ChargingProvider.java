package com.ogp.cputableau2.providers;

import com.ogp.cputableau2.settings.LocalSettings;
import com.ogp.cputableau2.su.RootCaller;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

public class ChargingProvider extends HWProvider {
    private static final String TAG = "ChargingProvider";

    private static final String chargeFiles = "/sys/class/power_supply/battery/current_now";
    private static final String statusFiles = "/sys/class/power_supply/battery/status";

    private static int savedCurrent = -1;

    public ChargingProvider() {
    }


    @Override
    public void init(RootCaller.RootExecutor rootExecutor) {
        super.init(rootExecutor);

        try {
            String resultT = readFileStringRoot(chargeFiles);
            if (0 < Integer.valueOf(resultT)) {
                Log.w(TAG, "ChargingProvider. CPU clock file found. Root required.");
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            Log.e(TAG, "ChargingProvider. CPU clock file not found.");
        }
    }


    @SuppressLint("DefaultLocale")
    @Override
    public String getData() {
        if (!LocalSettings.getChargeCurrent()) {
            return null;
        }


        try {
            String data = readFileStringRoot(statusFiles);
            if (null == data || 'D' == data.getBytes()[0]) {
                savedCurrent = -1;
                Log.v(TAG, "getData. No charging now...");

                return "Discharge";
            } else if ('F' == data.getBytes()[0]) {
                savedCurrent = -1;
                Log.v(TAG, "getData. Full battery...");

                return "Full";
            } else {
                data = readFileStringRoot(chargeFiles);

                int result = Integer.valueOf(data);
                if (0 < result) {
                    savedCurrent = deductLittleValues(result);
                }

                if (0 < savedCurrent) {
                    Log.v(TAG, String.format("getData. Charging current recognized: %d mA", result));
                    return String.format("%d mA", savedCurrent);
                }

                Log.v(TAG, "getData. Charging current could not be retrieved yet.");
            }
        } catch (Exception e) {
            Log.e(TAG, "getData. EXC(1)");
        }

        return null;
    }


    @Override
    public void clear() {
    }


    private int deductLittleValues(int value) {
        if (value >= 5000) {       // 5 Amperes???? Maybe it's uA???
            value /= 1000;
        }

        return value;
    }
}
