package com.auto.car.usb_signal.service;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "AutoUSBClick";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getSource() == null) return;
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;
        // 判断是否是 USB 授权弹窗
        if (containsUSBPermissionText(rootNode)) {
            Log.d(TAG, "检测到USB授权弹窗，开始查找按钮...");
            // 找到“确定”按钮并点击
            clickAllowButton(rootNode);
        }
    }

    @Override
    public void onInterrupt() {
    }

    private boolean containsUSBPermissionText(AccessibilityNodeInfo node) {
        if (node == null) return false;
        CharSequence text = node.getText();
        if (text != null && (text.toString().contains("允许应用") || text.toString().contains("访问该USB设备"))) {
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (containsUSBPermissionText(node.getChild(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean clickAllowButton(AccessibilityNodeInfo node) {
        if (node == null) return false;

        // 打印 className + text 看看是什么样的
        CharSequence text = node.getText();
        CharSequence className = node.getClassName();
        if (text != null && className != null) {
            Log.d("AutoUSBClick", "节点 text: " + text + " class: " + className);
        }

        if ("android.widget.Button".contentEquals(className) && ("确定".contentEquals(text) || "Allow".contentEquals(text))) {
            Log.d("AutoUSBClick", "找到确定按钮，开始点击");
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return true;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            if (clickAllowButton(node.getChild(i))) {
                return true;
            }
        }

        return false;
    }

}