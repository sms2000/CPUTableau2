package com.ogp.cputableau2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ogp.cputableau2.settings.LocalSettings;


public class CPUTableauActivity extends Activity {
    @SuppressWarnings("unused")
    private static final String TAG = "CPUTableauActivity";

    private CheckBox cbEnableOverlay = null;
    private CheckBox cbEnableDebug = null;
    private CheckBox cbEnableNotify = null;
    private CheckBox cbEnablePWL = null;
    private CheckBox cbEnableBTSL = null;
    private CheckBox cbUseFaherenheit = null;
    private CheckBox cbShowCurrent = null;

    private SeekBar sbTransparency = null;
    private SeekBar sbFontSize = null;
    private SeekBar sbRefreshMs = null;
    private SeekBar sbClickTimeMs = null;
    private SeekBar sbLongPressTimeMs = null;
    private SeekBar sbTapRadiusPC = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalSettings.init(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater li = getLayoutInflater();

        @SuppressLint("InflateParams") ViewGroup viewGroup = (ViewGroup) li.inflate(R.layout.setup,
                null);
        setContentView(viewGroup);

        Button btOK = (Button) viewGroup.findViewById(R.id.btOK);
        btOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                saveAndFinish();
            }
        });


        TextView headline = (TextView) findViewById(R.id.TextView01);
        Linkify.addLinks(headline,
                Linkify.ALL);


        cbEnableOverlay = (CheckBox) viewGroup.findViewById(R.id.cbEnable);
        cbEnableOverlay.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                reloadOverlay();
            }
        });

        cbEnableDebug = (CheckBox) viewGroup.findViewById(R.id.cbDebug);
        cbEnableNotify = (CheckBox) viewGroup.findViewById(R.id.cbNotify);
        cbEnablePWL = (CheckBox) viewGroup.findViewById(R.id.cbWakelock);
        cbEnableBTSL = (CheckBox) viewGroup.findViewById(R.id.cbBTScreenLock);

        cbUseFaherenheit = (CheckBox) viewGroup.findViewById(R.id.cbFaherenheit);
        cbUseFaherenheit.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                changeFaherenheit();
            }
        });

        cbShowCurrent = (CheckBox) viewGroup.findViewById(R.id.cbShowCurrent);
        cbShowCurrent.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                changeShowCurrent();
            }
        });


        sbTransparency = (SeekBar) viewGroup.findViewById(R.id.sbTransparecy);
        sbTransparency.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    changeTransparency();
                }
            }


            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sbFontSize = (SeekBar) viewGroup.findViewById(R.id.sbFontSize);
        sbFontSize.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    changeFontSize();
                }
            }


            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sbRefreshMs = (SeekBar) viewGroup.findViewById(R.id.sbrefreshMs);
        sbRefreshMs.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    changeRefreshMs();
                }
            }


            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sbClickTimeMs = (SeekBar) viewGroup.findViewById(R.id.sbclickTimeMs);
        sbClickTimeMs.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    changeClickTimeMs();
                }
            }


            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sbLongPressTimeMs = (SeekBar) viewGroup.findViewById(R.id.sblongPressMs);
        sbLongPressTimeMs.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    changeLongPressMs();
                }
            }


            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        sbTapRadiusPC = (SeekBar) viewGroup.findViewById(R.id.sbtapRadiusPercent);
        sbTapRadiusPC.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    changeTapRadiusPC();
                }
            }


            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        CPUTableauService.loadService(this);
    }


    @Override
    public void onResume() {
        super.onResume();

        LocalSettings.setActivityRun(true);

        cbEnableOverlay.setChecked(LocalSettings.getOverlay());
        cbEnableDebug.setChecked(LocalSettings.getExtensiveDebug());
        cbEnableNotify.setChecked(LocalSettings.getUseNotify());
        cbEnablePWL.setChecked(LocalSettings.getPWL());
        cbEnableBTSL.setChecked(LocalSettings.getBTSL());
        cbUseFaherenheit.setChecked(LocalSettings.isFahrenheit());
        cbShowCurrent.setChecked(LocalSettings.getChargeCurrent());

        sbTransparency.setProgress(LocalSettings.getTransparency());

        sbFontSize.setMax(LocalSettings.MAX_FONT_SIZE - LocalSettings.MIN_FONT_SIZE);
        sbFontSize.setProgress(LocalSettings.getFontSize() - LocalSettings.MIN_FONT_SIZE);

        sbRefreshMs.setMax(LocalSettings.MAX_REFRESH_MS - LocalSettings.MIN_REFRESH_MS);
        sbRefreshMs.setProgress(LocalSettings.getRefreshMs() - LocalSettings.MIN_REFRESH_MS);

        sbClickTimeMs.setMax(LocalSettings.MAX_CLICK_TIME_MS - LocalSettings.MIN_CLICK_TIME_MS);
        sbClickTimeMs.setProgress(LocalSettings.getClickTimeMs() - LocalSettings.MIN_CLICK_TIME_MS);

        sbLongPressTimeMs.setMax(LocalSettings.MAX_LONG_PRESS_MS - LocalSettings.MIN_LONG_PRESS_MS);
        sbLongPressTimeMs.setProgress(LocalSettings.getLongPressTimeMs() - LocalSettings.MIN_LONG_PRESS_MS);

        sbTapRadiusPC.setMax(LocalSettings.MAX_TAP_RADIUS_PC - LocalSettings.MIN_TAP_RADIUS_PC);
        sbTapRadiusPC.setProgress(LocalSettings.getTapRadiusPercent() - LocalSettings.MIN_TAP_RADIUS_PC);
    }


    @Override
    public void onPause() {
        super.onPause();

        LocalSettings.setOverlay(cbEnableOverlay.isChecked());

        LocalSettings.setExtensiveDebug(cbEnableDebug.isChecked());
        LocalSettings.setUseNotify(cbEnableNotify.isChecked());
        LocalSettings.setPWL(cbEnablePWL.isChecked());
        LocalSettings.setBTSL(cbEnableBTSL.isChecked());
        LocalSettings.setTransparency(sbTransparency.getProgress());
        LocalSettings.setTransparency(sbTransparency.getProgress());
        LocalSettings.setFontSize(sbFontSize.getProgress() + LocalSettings.MIN_FONT_SIZE);
        LocalSettings.setFahrenheit(cbUseFaherenheit.isChecked());
        LocalSettings.setChargeCurrent(cbShowCurrent.isChecked());
        LocalSettings.setClickTimeMs(sbClickTimeMs.getProgress() + LocalSettings.MIN_CLICK_TIME_MS);
        LocalSettings.setLongPressTimeMs(sbLongPressTimeMs.getProgress() + LocalSettings.MIN_LONG_PRESS_MS);
        LocalSettings.setRefreshMs(sbRefreshMs.getProgress() + LocalSettings.MIN_REFRESH_MS);
        LocalSettings.setTapRadiusPercent(sbTapRadiusPC.getProgress() + LocalSettings.MIN_TAP_RADIUS_PC);

        LocalSettings.writeToPersistantStorage();

        LocalSettings.setActivityRun(false);
    }


    protected void changeTapRadiusPC() {
        LocalSettings.setTapRadiusPercent(sbTapRadiusPC.getProgress() + LocalSettings.MIN_TAP_RADIUS_PC);

        CPUTableauService.quickUpdate();
    }


    protected void changeClickTimeMs() {
        LocalSettings.setClickTimeMs(sbClickTimeMs.getProgress() + LocalSettings.MIN_CLICK_TIME_MS);

        CPUTableauService.quickUpdate();
    }


    protected void changeLongPressMs() {
        LocalSettings.setClickTimeMs(sbLongPressTimeMs.getProgress() + LocalSettings.MIN_LONG_PRESS_MS);

        CPUTableauService.quickUpdate();
    }


    protected void changeRefreshMs() {
        LocalSettings.setRefreshMs(sbRefreshMs.getProgress() + LocalSettings.MIN_REFRESH_MS);

        CPUTableauService.quickUpdate();
    }


    protected void changeFaherenheit() {
        LocalSettings.setFahrenheit(cbUseFaherenheit.isChecked());

        CPUTableauService.quickUpdate();
    }


    protected void changeShowCurrent() {
        LocalSettings.setChargeCurrent(cbShowCurrent.isChecked());

        CPUTableauService.quickUpdate();
    }


    protected void changeTransparency() {
        LocalSettings.setTransparency(sbTransparency.getProgress());

        CPUTableauService.quickUpdate();
    }


    protected void changeFontSize() {
        LocalSettings.setFontSize(sbFontSize.getProgress() + LocalSettings.MIN_FONT_SIZE);

        CPUTableauService.fullUpdate();
    }


    protected void reloadOverlay() {
        LocalSettings.setOverlay(cbEnableOverlay.isChecked());

        CPUTableauService.setOverlayPane();            // Order of calls is critical!
        CPUTableauService.reloadForeground();
    }


    protected void saveAndFinish() {
        finish();
    }
}
