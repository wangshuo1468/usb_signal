package com.auto.car.usb_signal.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.auto.car.usb_signal.util.PinValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RunAppService implements Runnable {
    private static final String TAG = "RunAppService";
    private static final long TRIGGER_TIMEOUT_MS = 5000;
    private final Context context;
    private long lastAnyTriggerTime = 0;
    private boolean systemTriggered = false;
    private boolean hasEverTriggered = false;

    public RunAppService(Context context) {
        this.context = context;
        Log.d(TAG, "RunAppService 初始化");
        start();
    }

    public void start() {
        Log.d(TAG, "RunAppService 启动线程");
        new Thread(this).start();
        Log.d(TAG, "启动 GPIO 模拟服务");
        // 如果你有模拟服务，调用这里启动
        // TestPinService.startSimulatePin();
    }

    @Override
    public void run() {
        int loopCount = 0;
        while (true) {
            Log.d(TAG, "循环检测 GPIO... 次数: " + loopCount +
                    "  pin0=" + PinValue.pin0 +
                    " pin1=" + PinValue.pin1 +
                    " pin2=" + PinValue.pin2);
            checkAndUpdate(PinValue.pin0, PinValue.pin1, PinValue.pin2);

            try {
                Thread.sleep(200L);
                loopCount++;
            } catch (InterruptedException e) {
                Log.e(TAG, "线程中断", e);
                // 这里可以考虑 break，退出线程
            }
        }
    }

    private void checkAndUpdate(int... pinValues) {
        boolean anyHigh = false;

        for (int i = 0; i < pinValues.length; i++) {
            int value = pinValues[i];
            Log.d(TAG, "检测 pin[" + i + "] 值: " + value);

            if (value == 1) {
                anyHigh = true;
                lastAnyTriggerTime = System.currentTimeMillis();
                hasEverTriggered = true;

                if (!systemTriggered) {
                    Log.d(TAG, "首次触发系统动作 → 启动 USB Camera App");
                    systemTriggered = true;
                    launchUsbCameraApp();
                }
                // 找到一个高电平即可，退出循环
                break;
            }
        }

        if (!anyHigh && systemTriggered) {
            Log.d(TAG, "GPIO 全部低电平，取消触发状态");
            systemTriggered = false;
        }

        if (!anyHigh && hasEverTriggered && !systemTriggered
                && System.currentTimeMillis() - lastAnyTriggerTime > TRIGGER_TIMEOUT_MS) {
            Log.d(TAG, "触发超时 → 启动高德地图 App");
            hasEverTriggered = false;
            afterStartApp();
        }
    }

    private void launchUsbCameraApp() {
        Log.d(TAG, "正在启动 USB Camera App...");
        Intent intent = new Intent();
        intent.setClassName("com.shenyaocn.android.usbcamera", "com.shenyaocn.android.usbcamera.MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            Log.d(TAG, "USB Camera App 启动成功");
        } catch (Exception e) {
            Log.e(TAG, "启动 USB Camera App 失败", e);
        }
    }

    private void afterStartApp() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "input keyevent 187"});
            process.waitFor();
            Log.d(TAG, "模拟最近任务键成功");
        } catch (Exception e) {
            Log.e(TAG, "模拟最近任务键失败", e);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            // 模拟点击第二个任务（坐标要自己测量）
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "input tap 1444.1 712.4"});
            process.waitFor();
            Log.d(TAG, "模拟点击最近任务第二项成功");
        } catch (Exception e) {
            Log.e(TAG, "模拟点击失败", e);
        }
        try {
            Thread.sleep(100); // 延时1秒，看情况调节
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "dumpsys window windows | grep mCurrentFocus"});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = br.readLine();
            String packageName = null;
            if (line != null) {
                // 例子: mCurrentFocus=Window{3e1a34 u0 com.example.app/com.example.app.MainActivity}
                int start = line.indexOf("u0 ");
                int end = line.indexOf("/", start);
                if (start != -1 && end != -1) {
                    packageName = line.substring(start + 3, end);
                }
            }
            p.waitFor();


            if (packageName != null) {
                String pkg = packageName.toLowerCase();
                if (pkg.contains("yamby") || pkg.contains("jellyfin") || pkg.contains("video")
                        || pkg.contains("player") || pkg.contains("media") || pkg.contains("vlc")
                        || pkg.contains("mxplayer") || pkg.contains("plex") || pkg.contains("kodi")
                        || pkg.contains("exoplayer") || pkg.contains("potplayer")) {
                    try {
                        int x = 964; // 四舍五入
                        int y = 614;
                        // 第一次点击
                        Process clickProcess1 = Runtime.getRuntime().exec(new String[]{"su", "-c", "input tap " + x + " " + y});
                        clickProcess1.waitFor();
                        Thread.sleep(100); // 等100毫秒
                        Process clickProcess2 = Runtime.getRuntime().exec(new String[]{"su", "-c", "input tap " + x + " " + y});
                        clickProcess2.waitFor();
                        Log.d(TAG, "模拟连续点击屏幕中心，触发播放");
                    } catch (Exception e) {
                        Log.e(TAG, "模拟点击失败", e);
                    }
                } else {
                    Log.d(TAG, "当前应用不是视频类，无需点击播放");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取当前应用包名或模拟点击失败", e);
        }
    }

}
