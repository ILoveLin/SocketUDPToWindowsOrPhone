package com.example.mydemo.utils;

import android.util.Log;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2021/6/21 11:24
 * desc：
 */
public class LogUtils {

    private static final String TAG = "sem";

    public static final boolean DEBUG = true;

    public static void i(String msg){
        if(!DEBUG){
            return;
        }
        String componName = Thread.currentThread().getStackTrace()[3].getClassName();
        String className = componName.substring(componName.lastIndexOf(".")+1, componName.length());
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        Log.i(TAG, TAG+" class:"+className+" called:"+methodName+" "+msg);
    }

    public static void e(String msg){
        if(!DEBUG){
            return;
        }
        String componName = Thread.currentThread().getStackTrace()[3].getClassName();
        String className = componName.substring(componName.lastIndexOf(".")+1, componName.length());
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        Log.e(TAG, TAG+" class:"+className+" called:"+methodName+" "+msg);
    }
}