package com.auto.car.usb_signal;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.auto.car.usb_signal.service.MyAccessibilityService;
import com.auto.car.usb_signal.service.RunAppService;
import com.auto.car.usb_signal.service.UsbManagerActivity;

0
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item);
        UsbManagerActivity usbManagerActivity = new UsbManagerActivity(this);
        Intent intent = new Intent(this, MyAccessibilityService.class);
        startService(intent);
        RunAppService runAppService = new RunAppService(this);
        finish();
    }


}