package com.auto.car.usb_signal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.auto.car.usb_signal.service.IOService;
import com.auto.car.usb_signal.service.MyAccessibilityService;
import com.auto.car.usb_signal.service.UsbMonitorService;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 启动后台服务
        Intent serviceIntent = new Intent(this, UsbMonitorService.class);
        startService(serviceIntent);

        // 启动后台服务
        Intent intent = new Intent(this, MyAccessibilityService.class);
        startService(intent);

        // 启动后台服务
        Intent ioService = new Intent(this, IOService.class);
        startService(ioService);
        finish();
    }


}