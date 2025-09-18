package com.auto.car.usb_signal.service;
import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/* loaded from: classes5.dex */
public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "AutoUSBClick";

    @Override // android.accessibilityservice.AccessibilityService
    public void onAccessibilityEvent(AccessibilityEvent event) {
        AccessibilityNodeInfo rootNode;
        if (event != null && event.getSource() != null && (rootNode = getRootInActiveWindow()) != null && containsUSBPermissionText(rootNode)) {
            Log.d(TAG, "检测到USB授权弹窗，开始查找按钮...");
            clickAllowButton(rootNode);
        }
    }

    @Override // android.accessibilityservice.AccessibilityService
    public void onInterrupt() {
    }

    private boolean containsUSBPermissionText(AccessibilityNodeInfo node) {
        if (node == null) {
            return false;
        }
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
        if (node == null) {
            return false;
        }
        CharSequence text = node.getText();
        CharSequence className = node.getClassName();
        if (text != null && className != null) {
            Log.d(TAG, "节点 text: " + ((Object) text) + " class: " + ((Object) className));
        }
        if ("android.widget.Button".contentEquals(className) && ("确定".contentEquals(text) || "Allow".contentEquals(text))) {
            Log.d(TAG, "找到确定按钮，开始点击");
            node.performAction(16);
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