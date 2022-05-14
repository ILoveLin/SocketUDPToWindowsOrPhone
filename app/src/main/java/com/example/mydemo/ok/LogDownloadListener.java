package com.example.mydemo.ok;

import com.example.mydemo.utils.LogUtils;
import com.lzy.okgo.model.Progress;
import com.lzy.okserver.download.DownloadListener;

import java.io.File;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2022/4/21 9:08
 * desc：
 */
public class LogDownloadListener extends DownloadListener {

    public LogDownloadListener() {
        super("LogDownloadListener");
    }

    @Override
    public void onStart(Progress progress) {
        LogUtils.e("LogDownloadListener==onStart: " + progress);
    }

    @Override
    public void onProgress(Progress progress) {
        LogUtils.e("LogDownloadListener==onProgress: " + progress);
    }

    @Override
    public void onError(Progress progress) {
        LogUtils.e("LogDownloadListener==onError: " + progress);
        progress.exception.printStackTrace();
    }

    @Override
    public void onFinish(File file, Progress progress) {
        LogUtils.e("LogDownloadListener==onFinish: " + progress);
    }

    @Override
    public void onRemove(Progress progress) {
        LogUtils.e("LogDownloadListener==onRemove: " + progress);
    }
}
