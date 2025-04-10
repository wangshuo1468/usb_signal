package com.auto.car.usb_signal.rockmong;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface UsbDevice extends Library {

    UsbDevice INSTANTCE = (UsbDevice) Native.loadLibrary("rockmong", UsbDevice.class);

    void UsbDevice_Clear();

    int UsbDevice_GetVid();
    int UsbDevice_GetPid();

    //Set fd
    int UsbDevice_SetFd(int FileDescription);

    //扫描USB设备
    //返回值如果大于0，代表获取到设备的个数。如果等于0，代表未插入设备。如果小于0，代表发生错误
    int UsbDevice_Scan(int[] SerialNumbers);
}

