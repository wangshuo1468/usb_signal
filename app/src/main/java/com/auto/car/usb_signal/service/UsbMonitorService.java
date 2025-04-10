package com.auto.car.usb_signal.service;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

public class UsbMonitorService extends Service {

    private UsbManager usbManager;
    private Handler handler;
    private Runnable returnToHomeRunnable;
    private static final int VID = 0x1A86; // Vendor ID
    private static final int PID = 0x7523; // Product ID


    @Override
    public void onCreate() {
        super.onCreate();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        handler = new Handler();
        setupUsbReceiver();
    }

    private void setupUsbReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && device.getVendorId() == VID && device.getProductId() == PID) {
                    launchUsbCameraApp(context);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && device.getVendorId() == VID && device.getProductId() == PID) {
                    scheduleReturnToHome(context);
                }
            }
        }
    };

    private void launchUsbCameraApp(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.shenyaocn.android.usbcamera", "com.shenyaocn.android.usbcamera.MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "目标应用未安装或无法启动", Toast.LENGTH_LONG).show();
        }
    }

    private void scheduleReturnToHome(final Context context) {
        if (returnToHomeRunnable != null) {
            handler.removeCallbacks(returnToHomeRunnable);
        }

        returnToHomeRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setClassName("com.autonavi.amapauto", "com.autonavi.amapauto.MainMapActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "目标应用未安装或无法启动", Toast.LENGTH_LONG).show();
                }
            }
        };

        handler.postDelayed(returnToHomeRunnable, 5000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
    }
}