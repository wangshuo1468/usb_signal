package com.auto.car.usb_signal.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.auto.car.usb_signal.util.PinValue;

public class RunAppService implements Runnable {

    private static final long TRIGGER_TIMEOUT_MS = 5000;

    private long lastAnyTriggerTime = 0;

    private boolean systemTriggered = false;
    private boolean hasEverTriggered = false;

    private final Context context;

    private static final String TAG = "RunAppService";

    public RunAppService(Context context) {
        this.context = context;
        start();
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        int a = 0;
        while (true) {
            checkAndUpdate(PinValue.pin0, PinValue.pin1, PinValue.pin2);
            try {
                Thread.sleep(200);
                a++;
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void checkAndUpdate(int... pinValues) {
        boolean anyHigh = false;

        for (int value : pinValues) {
            if (value == 1) {
                anyHigh = true;
                lastAnyTriggerTime = System.currentTimeMillis();
                hasEverTriggered = true;

                if (!systemTriggered) {
                    systemTriggered = true;
                    onTriggerStart();
                }
                break;
            }
        }

        // 如果引脚当前都为低电平，且处于触发中 → 设置为未触发
        if (!anyHigh && systemTriggered) {
            systemTriggered = false;
        }

        // 如果已经触发过，当前未处于触发状态，并且 0.5 秒内没有新触发 → 认为事件结束
        if (!anyHigh && hasEverTriggered &&
                !systemTriggered &&
                System.currentTimeMillis() - lastAnyTriggerTime > TRIGGER_TIMEOUT_MS) {

            hasEverTriggered = false; // 防止重复结束回调
            onTriggerEnd();
        }
    }

    private void onTriggerStart() {
        Log.d(TAG, "🚀 触发事件开始！");
        launchUsbCameraApp();
    }

    private void onTriggerEnd() {
        Log.d(TAG, "🛑 触发事件结束！");
        launchGaoDeMapApp();
    }

    private void launchUsbCameraApp() {
        Intent intent = new Intent();
        intent.setClassName("com.shenyaocn.android.usbcamera", "com.shenyaocn.android.usbcamera.MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }



    private void launchGaoDeMapApp() {
        Intent intent = new Intent();
        intent.setClassName("com.autonavi.amapauto", "com.autonavi.amapauto.MainMapActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    public boolean isSystemTriggered() {
        return systemTriggered;
    }
}
