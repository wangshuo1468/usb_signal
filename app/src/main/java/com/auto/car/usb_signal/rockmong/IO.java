package com.auto.car.usb_signal.rockmong;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;

public interface IO extends Library {

    IO INSTANTCE = (IO) Native.loadLibrary("rockmong", IO.class);

    //初始化引脚工作模式
    //SerialNumber: 设备序号
    //Pin：引脚编号。0，P0. 1, P1...
    //Mode：输入输出模式。0，输入。1，输出。2，开漏
    //Pull：上拉下拉电阻。0，无。1，使能内部上拉。2，使能内部下拉
	//函数返回：0，正常；<0，异常
    int IO_InitPin(int SerialNumber, int Pin, int Mode, int Pull);
	
    //读取引脚状态
    //SerialNumber: 设备序号
    //Pin：引脚编号。0，P0. 1, P1...
    //PinState：返回引脚状态。0，低电平。1，高电平
	//函数返回：0，正常；<0，异常
    int IO_ReadPin(int SerialNumber, int Pin, IntByReference PinState);
	
    //控制引脚输出状态
    //SerialNumber: 设备序号
    //Pin：引脚编号。0，P0. 1, P1...
    //PinState：引脚状态。0，低电平。1，高电平
	//函数返回：0，正常；<0，异常
    int IO_WritePin(int SerialNumber, int Pin, int PinState);

}
