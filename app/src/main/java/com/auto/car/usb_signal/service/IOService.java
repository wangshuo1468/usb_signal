package com.auto.car.usb_signal.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.auto.car.usb_signal.rockmong.IO;
import com.auto.car.usb_signal.rockmong.UsbDevice;
import com.auto.car.usb_signal.util.PinValue;
import com.sun.jna.ptr.IntByReference;

public class IOService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        new Thread(() -> {
            while (true) {
                int[] SerialNumbers = new int[16];
                UsbDevice.INSTANTCE.UsbDevice_Scan(SerialNumbers);
                IO.INSTANTCE.IO_InitPin(SerialNumbers[0], 0, 0, 2);
                IO.INSTANTCE.IO_InitPin(SerialNumbers[0], 1, 0, 2);
                IO.INSTANTCE.IO_InitPin(SerialNumbers[0], 2, 0, 2);
                //      脚0
                IntByReference pin0 = new IntByReference();
                IO.INSTANTCE.IO_ReadPin(SerialNumbers[0], 0, pin0);
                PinValue.pin0 = pin0.getValue();
                //      脚1
                IntByReference pin1 = new IntByReference();
                IO.INSTANTCE.IO_ReadPin(SerialNumbers[0], 1, pin1);
                PinValue.pin1 = pin1.getValue();
                //      脚2
                IntByReference pin2 = new IntByReference();
                IO.INSTANTCE.IO_ReadPin(SerialNumbers[0], 2, pin2);
                PinValue.pin2 = pin2.getValue();
                try {
                    Thread.sleep(100); // 100ms 检测一次
                } catch (InterruptedException ignored) {
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // 不绑定
    }
}

