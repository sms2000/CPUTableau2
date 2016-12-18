package com.ogp.cputableau2.providers;

import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.ogp.cputableau2.su.RootCaller;


public class CPUTemperatureProvider extends HWProvider {
    private static final String TAG = "CPUTemperatureProvider";

    private static final String[] tempFiles = new String[]{
            "/sys/devices/virtual/thermal/thermal_zone1/temp", // Nougat 7.0
            // Nexus 6
            "/sys/devices/system/cpu/cpu0/cpufreq/cpu_temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/FakeShmoo_cpu_temp",
            "/sys/class/thermal/thermal_zone1/temp", // HTC Evo 3D
            "/sys/class/i2c-adapter/i2c-4/4-004c/temperature",
            "/sys/devices/platform/omap/omap_temp_sensor.0/temperature",
            "/sys/devices/platform/tegra_tmon/temp1_input", // Atrix 4G
            "/sys/devices/platform/tegra-i2c.3/i2c-3/3-004c/temp2_input", // Atrix
            // 4G
            // 4.4.4
            "/sys/kernel/debug/tegra_thermal/temp_tj",
            "/sys/devices/platform/s5p-tmu/temperature", // Galaxy S3, Note 2
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq",};

    private static Integer tempIndex = -1;
    private static final Object lock = new Object();

    public CPUTemperatureProvider() {
    }


    public void init(RootCaller.RootExecutor rootExecutor) {
        if (null != rootExecutor) {
            this.rootExecutor = rootExecutor;
        }

        synchronized (lock) {
            try {
                if (-1 == tempIndex) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {        // Nougat? Root required!
                        for (tempIndex = 0; tempIndex < tempFiles.length; tempIndex++) {
                            String result = readFileStringRoot(tempFiles[tempIndex]);
                            if (null == result) {
                                continue;
                            }

                            int data = Integer.valueOf(result);
                            if (0 < data) {
                                break;
                            }
                        }
                    } else {
                        for (tempIndex = 0; tempIndex < tempFiles.length; tempIndex++) {
                            int data = readFileInt(tempFiles[tempIndex]);
                            if (0 < data) {
                                break;
                            }
                        }
                    }


                    if (tempIndex >= tempFiles.length) {
                        tempIndex = -1;
                        Log.e(TAG, "CPUTemperatureProvider::initiator. Temperature file not found.");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "CPUTemperatureProvider::initiator. EXC(1)");
            }
        }
    }


    @Override
    public void clear() {
    }


    @Override
    public String getData() {
        synchronized (lock) {
            if (0 > tempIndex) {
                init(null);
                return null;
            }
        }

        try {
            int result;

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {        // Nougat? Root required!
                String resultT = readFileStringRoot(tempFiles[tempIndex]);
                result = Integer.valueOf(resultT);
            } else {
                result = readFileInt(tempFiles[tempIndex]);
            }

            if (result <= 0) {
                Log.e(TAG, "Error recognizing CPU temp.");
            } else {
                // Normalizing
                double dres = (double) result;

                while (dres >= 200) {
                    dres *= 0.1;
                }

                return temperatureDouble2StringString(dres);
            }
        } catch (Exception e) {
            Log.e(TAG, "Result: exception.");
            init(null);
        }

        return null;
    }
}
