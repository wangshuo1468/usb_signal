package com.auto.car.usb_signal.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.widget.Toast;


import androidx.core.content.ContextCompat;

import com.auto.car.usb_signal.rockmong.UsbDevice;

import java.util.HashMap;

public class UsbManagerActivity {

    private Context context;
    private UsbManager usbManager;
    private static final String ACTION_USB_PERMISSION = "com.rockmong.USB_PERMISSION";
    private Intent ioService;

    public UsbManagerActivity(Context context) {
        this.context = context;
        //注册USB插拔检测服务
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        filter.setPriority(Integer.MAX_VALUE); //设置级别
        UsbReceiver usbReceiver = new UsbReceiver();
        ContextCompat.registerReceiver(context, usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        ioService = new Intent(context, IOService.class);
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, android.hardware.usb.UsbDevice> deviceList = usbManager.getDeviceList();
        Scan();
    }

    /**
     * 扫描并尝试连接USB设备
     * 该方法首先清除当前USB设备的配置，然后重新启动IO服务以准备扫描新的USB设备
     * 接着，它获取系统中的USB设备列表，并遍历查找目标USB设备
     * 目标USB设备是根据VID和PID匹配预定义值的设备
     * 如果找到目标设备，方法将请求访问权限，如果权限已授予，则打开设备并设置文件描述符
     *
     * @return int 返回操作结果，0表示成功，非0表示失败
     */
    public int Scan() {
        // 初始化返回值
        int ret = 0;

        // 清除当前USB设备的配置
        UsbDevice.INSTANTCE.UsbDevice_Clear();
        context.stopService(ioService);
        context.startService(ioService);
        // 停止并重新启动IO服务，以准备扫描新的USB设备
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        // 获取当前系统中的USB设备列表
        HashMap<String, android.hardware.usb.UsbDevice> deviceList = usbManager.getDeviceList();

        // 遍历设备列表，寻找匹配的目标USB设备
        for (android.hardware.usb.UsbDevice usbDevice : deviceList.values()) {
            // 检查设备的VID和PID是否与预定义的目标值匹配
            if (UsbDevice.INSTANTCE.UsbDevice_GetVid() == usbDevice.getVendorId() && UsbDevice.INSTANTCE.UsbDevice_GetPid() == usbDevice.getProductId()) {
                // 如果没有权限访问该设备，则请求权限
                if (!usbManager.hasPermission(usbDevice)) {
                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(usbDevice, mPermissionIntent);
                } else {
                    // 如果已有权限，打开USB设备并获取文件描述符
                    UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
                    int fileDescriptor = usbDeviceConnection.getFileDescriptor();

                    // 设置USB设备的文件描述符
                    ret = UsbDevice.INSTANTCE.UsbDevice_SetFd(fileDescriptor);
                    // 如果设置文件描述符失败，输出错误信息
                    if (0 > ret) {
                        System.out.println("Error: " + ret);
                    }
                }
            }
        }
        // 返回操作结果
        return ret;
    }

    //定义USB的插拔广播接收器
    private class UsbReceiver extends BroadcastReceiver {
        /**
         * 处理USB设备附加或分离的广播接收器
         * 该类继承自BroadcastReceiver，用于监听USB设备的附加和分离事件，并做出相应的处理
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            // 检查接收到的Intent动作，以确定USB设备的状态
            if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                // 当USB设备分离时，显示提示信息并执行扫描操作
                Toast.makeText(context, "Device Detached", Toast.LENGTH_SHORT).show();
                Scan();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                // 当USB设备附加时，显示提示信息并获取设备信息
                Toast.makeText(context, "Device Attached", Toast.LENGTH_SHORT).show();
                android.hardware.usb.UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                // 检查是否已获得该USB设备的权限
                if (!usbManager.hasPermission(usbDevice)) {
                    // 如果没有权限，请求权限
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(usbDevice, pendingIntent);
                }
            } else if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                // 处理USB权限请求的响应
                //UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    // 如果权限被授予，输出信息并执行扫描操作
                    System.out.println("Permission ok");
                    Scan();
                } else {
                    // 如果权限未被授予，输出信息
                    System.out.println("No permission");
                }
            }
        }
    }

}
