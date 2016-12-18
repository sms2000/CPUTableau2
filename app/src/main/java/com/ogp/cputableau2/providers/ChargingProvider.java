package com.ogp.cputableau2.providers;

import com.ogp.cputableau2.settings.LocalSettings;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

public class ChargingProvider extends HWProvider {
    private static final String TAG = "ChargingProvider";

    private static final String chargeFiles = "/sys/class/power_supply/battery/current_now";
    private static final String statusFiles = "/sys/class/power_supply/battery/status";

    private static int savedCurrent = -1;

    public ChargingProvider() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {        // Nougat? Root required!
                String resultT = readFileStringRoot(chargeFiles);
                if (0 < Integer.valueOf(resultT)) {
                    Log.w(TAG, "ChargingProvider. CPU clock file found. Root required.");
                } else {
                    Log.e(TAG, "ChargingProvider. CPU clock file not found.");
                }
            } else {
                if (0 < readFileInt(chargeFiles)) {
                    Log.w(TAG, "ChargingProvider. CPU clock file found.");
                } else {
                    Log.e(TAG, "ChargingProvider. CPU clock file not found.");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "ChargingProvider. EXC(1)");
        }
    }


    @SuppressLint("DefaultLocale")
    @Override
    public String getData() {
        if (!LocalSettings.getChargeCurrent()) {
            return null;
        }


        try {
            String data;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {        // Nougat? Root required!
                data = readFileStringRoot(statusFiles);
            } else {
                data = readFileString(statusFiles);
            }

            if (null == data || 'D' == data.getBytes()[0]) {
                savedCurrent = -1;
                Log.v(TAG, "getData. No charging now...");

                return "Discharge";
            } else if ('F' == data.getBytes()[0]) {
                savedCurrent = -1;
                Log.v(TAG, "getData. Full battery...");

                return "Full";
            } else {
                int result;

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {        // Nougat? Root required!
                    data = readFileStringRoot(chargeFiles);
                    result = Integer.valueOf(data);
                } else {
                    result = readFileInt(chargeFiles);
                }

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
        if (value > 3000)        // 3 Amperes???? Maybe it's uA???
        {
            value /= 1000;
        }

        return value;
    }
}
