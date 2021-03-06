package com.ogp.cputableau2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.ogp.cputableau2.settings.LocalSettings;
import com.ogp.cputableau2.su.RootCaller;


@SuppressLint("ViewConstructor")
public class TransparentFrame extends RelativeLayout implements View.OnTouchListener, TransparentContentInterface {
    private static final String TAG = "TransparentFrame";


    private TransparentContent transparentClient;
    private Point coords = new Point();
    private boolean motionHappen = false;
    private boolean viewAttached = false;

    private final Object lock = new Object();
    private ServiceInterface service;
    private Context context;
    private WindowManager windowManager = null;
    private WindowManager.LayoutParams layoutParams = null;
    private Point displaySize = new Point();
    private Point displayHalfSize = new Point();
    private int lesserDisplay;
    private Point downPoint = new Point();
    private Point moveBegin = new Point();
    private Point oldContentHalfSize = null;
    private long downTime = -1;
    private long lastClickTime = 0;
    private Handler handler = new Handler();
    private Point halfSize = new Point();


    private class VerifySingleClick implements Runnable {
        public void run() {
            verifySingleClick();
        }
    }


    private class VerifyLongPress implements Runnable {
        public void run() {
            verifyLongPress();
        }
    }


    private class ActivateMove implements Runnable {
        private WindowManager.LayoutParams params;
        private int paddingX;
        private int paddingY;


        private ActivateMove(WindowManager.LayoutParams params, int paddingX, int paddingY) {
            this.params = params;
            this.paddingX = paddingX;
            this.paddingY = paddingY;
        }


        public void run() {
            setPadding(paddingX, paddingY, 0, 0);

            synchronized (lock) {
                if (viewAttached) {
                    windowManager.updateViewLayout(TransparentFrame.this, params);
                }

                refresh();
            }
        }
    }

    private class InitiateClick implements Runnable {
        public void run() {
            initiateActivity();
        }
    }


    @SuppressWarnings("deprecation")
    @SuppressLint("ClickableViewAccessibility")
    public TransparentFrame(Context context, ServiceInterface service) {
        super(context);

        this.context = context;
        this.service = service;
        this.windowManager = service.getWindowManager();

        transparentClient = new TransparentContent(context, this);

        displaySize.x = windowManager.getDefaultDisplay().getWidth();
        displaySize.y = windowManager.getDefaultDisplay().getHeight();
        lesserDisplay = displaySize.x < displaySize.y ? displaySize.x : displaySize.y;

        displayHalfSize.x = displaySize.x >> 1;
        displayHalfSize.y = displaySize.y >> 1;

        setOnTouchListener(this);

        setContentWindow();
        updateFontSize();
    }


    public void init(RootCaller.RootExecutor rootExecutor) {
        transparentClient.init(rootExecutor);
    }


    public void clear() {
        removeView(transparentClient);
        transparentClient.clear();
        transparentClient = null;

        try {
            synchronized (lock) {
                viewAttached = false;
            }

            windowManager.removeView(this);
        } catch (Exception e) {
            Log.e(TAG, "dismiss(). EXC(1)");
        }
    }


    private void setContentWindow() {
        Log.v(TAG, "setContentWindow. Entry...");

        PointF xyPoint = service.loadDefaultXY();

        int type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        layoutParams = new WindowManager.LayoutParams(2, 2, (int) (xyPoint.X * displaySize.x) + 1, (int) (xyPoint.Y * displaySize.y) + 1,
                type,
                WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        try {
            windowManager.addView(this, layoutParams);
        } catch (Throwable e) {
            Intent intent = new Intent(context, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }

        synchronized (lock) {
            viewAttached = true;
        }

        LayoutParams frameParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        addView(transparentClient, frameParams);

        reposition();

        Log.v(TAG, "setContentWindow. ... Exit.");
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (windowManager.getDefaultDisplay().getWidth() != displaySize.x) {
            displaySize.x = windowManager.getDefaultDisplay().getWidth();
            displaySize.y = windowManager.getDefaultDisplay().getHeight();
            displayHalfSize.x = displaySize.x >> 1;
            displayHalfSize.y = displaySize.y >> 1;

            lesserDisplay = displaySize.x < displaySize.y ? displaySize.x : displaySize.y;

            reposition();
        }

        halfSize.x = (r - l) >> 1;
        halfSize.y = (b - t) >> 1;
    }


    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {
        WindowManager.LayoutParams layoutParams;

        if (LocalSettings.getExtensiveDebug()) {
            Log.v(TAG, String.format("onTouch. A: %d  X/Y:%d/%d", event.getAction(), (int) event.getX(), (int) event.getY()));
        }

        int locationX = (int) event.getRawX() - displayHalfSize.x;
        int locationY = (int) event.getRawY() - displayHalfSize.y;


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                motionHappen = false;
                downTime = System.currentTimeMillis();

                layoutParams = (WindowManager.LayoutParams) getLayoutParams();

                moveBegin.x = layoutParams.x;
                moveBegin.y = layoutParams.y;

                coords.x = locationX;
                coords.y = locationY;

                synchronized (lock) {
                    downPoint.x = locationX;
                    downPoint.y = locationY;
                }


                Rect bounds = new Rect(0, 0, displaySize.x, displaySize.y);
                TouchDelegate touchDelegade = new TouchDelegate(bounds, this);

                setTouchDelegate(touchDelegade);

                new Handler().postDelayed(new VerifyLongPress(), LocalSettings.getLongPressTimeMs());
                break;


            case MotionEvent.ACTION_MOVE:
                int X = locationX;
                int Y = locationY;

                int tapRadius = lesserDisplay * LocalSettings.getTapRadiusPercent() / 100;

                if (Math.abs(X - downPoint.x) > tapRadius || Math.abs(Y - downPoint.y) > tapRadius) {
                    motionHappen = true;
                }


                layoutParams = (WindowManager.LayoutParams) getLayoutParams();

                X += moveBegin.x - coords.x;
                Y += moveBegin.y - coords.y;


                if (X < -displayHalfSize.x + halfSize.x) X = -displayHalfSize.x + halfSize.x;
                else if (X > displayHalfSize.x - halfSize.x) X = displayHalfSize.x - halfSize.x;

                if (Y < -displayHalfSize.y + halfSize.y) Y = -displayHalfSize.y + halfSize.y;
                else if (Y > displayHalfSize.y - halfSize.y) Y = displayHalfSize.y - halfSize.y;

                layoutParams.x = X;
                layoutParams.y = Y;

                windowManager.updateViewLayout(this, layoutParams);


                break;


            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!LocalSettings.isActivityRun()) {
                    long timeNow = System.currentTimeMillis();
                    if (-1 < downTime
                            &&
                            timeNow - downTime < LocalSettings.getClickTimeMs()) {
                        if (timeNow - lastClickTime < LocalSettings.getClickTimeMs()) {
                            Log.d(TAG, "onTouch. Double click encountered. Do whatever... Here: open activity.");

                            lastClickTime = 0;

                            new Handler().post(new InitiateClick());
                        } else {
                            Log.d(TAG, "onTouch. Single click suspected...");

                            lastClickTime = timeNow;

                            new Handler().postDelayed(new VerifySingleClick(),
                                    LocalSettings.getClickTimeMs());
                        }
                    }

                }

                downTime = -1;


                setTouchDelegate(null);

                motionHappen = false;

                layoutParams = (WindowManager.LayoutParams) getLayoutParams();

                service.saveDefaultXY((float) layoutParams.x / displaySize.x,
                        (float) layoutParams.y / displaySize.y);
                break;

        }

        return true;
    }


    private void verifySingleClick() {
        if (0 < lastClickTime
                &&
                !motionHappen) {
            lastClickTime = 0;

            Log.d(TAG, "verifySingleClick. Single click encountered. Do whatever... Here: do nothing.");
//
//  TODO: use single click if required...
//			

        }
    }


    private void verifyLongPress() {
        if (0 < downTime
                &&
                !motionHappen) {
            lastClickTime = 0;

            Log.d(TAG, "verifyLongPress. Long press encountered. Do whatever... Here: do nothing.");

            simulateHomePress();
        }
    }


    private void initiateActivity() {
        Intent intent = new Intent(context,
                CPUTableauActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    private void simulateHomePress() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);

        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);
    }


    private void reposition() {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();

        PointF xyPoint = service.loadDefaultXY();

        params.x = (int) (xyPoint.X * displaySize.x);
        params.y = (int) (xyPoint.Y * displaySize.y);

        handler.post(new ActivateMove(params, 0, 0));
    }


    public void contentSizeChanged() {
        Log.d(TAG, "contentSizeChanged. TransparentFrame size changed.");

        Point contentSize = transparentClient.getContentSize();

        int xHalf = contentSize.x >> 1;
        int yHalf = contentSize.y >> 1;

        if (null == oldContentHalfSize) {
            oldContentHalfSize = new Point(xHalf, yHalf);
        }

        int xPos = layoutParams.x - oldContentHalfSize.x;
        int yPos = layoutParams.y - oldContentHalfSize.y;


        layoutParams.width = contentSize.x;
        layoutParams.height = contentSize.y;

        int X = xPos + xHalf;
        int Y = yPos + yHalf;

        if (X < -displayHalfSize.x + halfSize.x) X = -displayHalfSize.x + halfSize.x;
        else if (X > displayHalfSize.x - halfSize.x) X = displayHalfSize.x - halfSize.x;

        if (Y < -displayHalfSize.y + halfSize.y) Y = -displayHalfSize.y + halfSize.y;
        else if (Y > displayHalfSize.y - halfSize.y) Y = displayHalfSize.y - halfSize.y;

        layoutParams.x = X;
        layoutParams.y = Y;

        oldContentHalfSize.x = xHalf;
        oldContentHalfSize.y = yHalf;

        windowManager.updateViewLayout(this,
                layoutParams);

        transparentClient.refresh();

        Log.w(TAG, "adjustContentWindow. Adjusted.");
        Log.v(TAG, "adjustContentWindow. ... Exit.");
    }


    public void refresh() {
        try {
            transparentClient.refresh();
        } catch (Exception ignored) {
        }
    }


    public void updateFontSize() {
        try {
            transparentClient.updateFontSize();

            contentSizeChanged();
        } catch (Exception ignored) {
        }
    }
}
