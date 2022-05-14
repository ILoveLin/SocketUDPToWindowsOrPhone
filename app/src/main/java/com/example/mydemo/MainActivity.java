package com.example.mydemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mydemo.bean.HandBean;
import com.example.mydemo.ok.FileUtil;
import com.example.mydemo.utils.CalculateUtils;
import com.example.mydemo.utils.LogUtils;
import com.google.gson.Gson;
import com.hjq.gson.factory.GsonFactory;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.liulishuo.okdownload.DownloadContext;
import com.liulishuo.okdownload.DownloadContextListener;
import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.StatusUtil;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static String CurrentIP;
    private static int BROADCAST_PORT = 7006;
    private static int RECEIVE_PORT = 7003;
    //    private static String BROADCAST_IP = "192.168.64.13";
    private static String BROADCAST_IP = "192.168.130.102";
    //    private static String BROADCAST_IP = "192.168.1.21";
//        private static String BROADCAST_IP = "255.255.255.255";
    private InetAddress inetAddress = null;
    private BroadcastThread broadcastThread;
    private DatagramSocket sendSocket = null;
    private DatagramSocket receiveSocket = null;
    private Button sendUDPBrocast;
    private volatile boolean isRuning = true;
    private TextView ipInfo;
    private Button btn_send;
    private EditText et_sendInfo;
    private String sendContent;
    private TextView tv_receive;
    private List<String> ipList = new ArrayList<>();
    private Button btnClear;
    //    private ReceiveThread receiveThread;
    private String sendStringData;
    private Button btnText;
    private DownloadListener listener;
    private DownloadTask task;
    private String dirName;
    private String dirName2;
    private String fileame;
    private TextView mStatusTv;
    private ProgressBar mProgressBar;
    private DownloadListener4WithSpeed listener04;
    private ProgressBar mProgressBar02;
    private ProgressBar mProgressBar03;
    private ProgressBar mProgressBar04;
    private ProgressBar mProgressBar05;
    private long mCcurrentOffset;
    private Button btnText_stop;
    private DownloadContext context;
    private LinearLayout linear_all;
    private RecyclerView mRecycleView;
    private ArrayList<ItemBean> mDataList;
    private ItemAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        initView();
        initIp();
        initThread();
        try {
            inetAddress = InetAddress.getByName(BROADCAST_IP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class SendUDPBrocastListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            task.cancel();


            if (isRuning) {
                isRuning = false;
                sendUDPBrocast.setText("发送广播");
                System.out.println("现在停止广播..");
            } else {
                isRuning = true;
                sendUDPBrocast.setText("停止广播");
                System.out.println("现在发送广播..");
            }
        }
    }

    private int count = 1;
    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1: {
//                    if (!msg.obj.equals(IP)) {
//                        if (!isExistIp(msg.obj.toString())) {  //  java.util.ConcurrentModificationException   并发异常
//                            ipList.add(msg.obj.toString());
//                        }
//                        Log.i(TAG, "handleMessage: ReceiveThread receive ip接收到信息===" + msg.obj.toString());
//
//                        tv_receive.append(msg.obj.toString() + " 接收到信息 " + "\n");
//                    }
                    tv_receive.append(msg.obj.toString() + " 接收到信息 " + "\n");
                    LogUtils.e("======ReceiveThread===myHandler==接收到信息==" + msg.obj.toString());
//                    LogUtils.e("======ReceiveThread==Type===444==textFlag==" + count);
//                    if ("AAC5000008000001211597DD".equals(msg.obj.toString())) {
//                        if (count < 3) {
//                            sendMessageToThread(broadcastThread.mhandler);
//                            tv_receive.append(msg.obj.toString() + " 接收到信息 " + "\n");
//                            count++;
//                        }
//
//
//                    }

                }
                break;
                default:
                    break;
            }
        }

    };

    public class BroadcastThread extends Thread {
        private Handler mhandler = null;

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            //获取当前线程的Looper，并prepare();
            Looper.prepare();
            mhandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    String message = (String) msg.obj;
//                    byte[] data = message.getBytes();
                    byte[] data = getSendData("55");//AAC50000080000012137B5DD
                    DatagramPacket dpSend = null;
                    dpSend = new DatagramPacket(data, data.length, inetAddress, BROADCAST_PORT);
                    try {
                        double start = System.currentTimeMillis();
                        for (int i = 0; i < 2; i++) {
                            sendSocket = new DatagramSocket();
//                            sendSocket = new DatagramSocket(RECEIVE_PORT);
                            sendSocket.send(dpSend);
                            sendSocket.close();
                            Thread.sleep(80);
                            Log.i(TAG, "sendMessage: data " + dpSend);
                        }
                        double end = System.currentTimeMillis();
                        double times = end - start;
                        Log.i(TAG, "receive: executed time is : " + times + "ms");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            //looper开始处理消息。
            Looper.loop();
        }
    }


//    private class ReceiveThread extends Thread {
//
//        private DatagramSocket datagramSocket;
//
//        @Override
//        public void run() {
//            byte[] receiveData = new byte[1024];
//            DatagramSocket receiveSockett = null;
//            {
//                try {
////                    receiveSockett = new DatagramSocket(BROADCAST_PORT);
//                    receiveSockett = new DatagramSocket(0);
//                } catch (SocketException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            DatagramPacket dpReceive = new DatagramPacket(receiveData, receiveData.length);
//            // 建立Socket连接
//            try {
//                datagramSocket = new DatagramSocket(BROADCAST_PORT);
////                datagramSocket = new DatagramSocket(0);
//            } catch (SocketException e) {
//                e.printStackTrace();
//            }
//            LogUtils.e("======ReceiveThread=====启动接收线程==");
//
//            while (true) {
//                if (isRuning) {
//                    try {
//                        LogUtils.e("======ReceiveThread=====000==");
////                        datagramSocket.receive(dpReceive);
//                        LogUtils.e("======ReceiveThread==0000000111111==");
//                        LogUtils.e("======ReceiveThread=====111==");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    String rec = CalculateUtils.byteArrayToHexString(dpReceive.getData()).trim();
//                    int dd = rec.indexOf("DD");
//                    String recIp = rec.substring(0, dd + 2);
//                    LogUtils.e("======ReceiveThread=====222==" + recIp);
//
////                    String recIp = dpReceive.getData().toString();   //后去发送的数据
////                    String recIp = dpReceive.getAddress().toString().substring(1);   //获取发送的ip
////                    if (dpReceive != null) {
//                    Message revMessage = Message.obtain();
//                    revMessage.what = 1;
//                    revMessage.obj = recIp;
//                    Log.i(TAG, "handleMessage: ReceiveThread receive ip" + recIp);
//
//                    myHandler.sendMessage(revMessage);
////                    }
//                }
//            }
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        isRuning = false;
        receiveSocket.close();
        System.out.println("UDP Server程序退出,关掉socket,停止广播");
        finish();
    }


    private byte[] getSendData(String trim) {
        LogUtils.e("TAG==输入光源数值=trim===" + trim);
        String letterAndNumber = "1234abcdABCD56789";


//        CharMatcher.javaDigit().matches('1');
        //非空等等校验
        if ("".equals(trim)) {
            trim = "50";
        }
        int iData = Integer.parseInt(trim);
        if (iData <= 0) {
            iData = 0;
        } else if (iData >= 63) {
            iData = 63;
        }

        String inputSumLightData = CalculateUtils.numToHex8(iData);
//      aa c5 00 -00 08 00 00 01 21-- 15 --97 dd
        String str = "aac5000008000001211597dd";   //该亮度   21

        /**
         * 计算异或校验值
         * 先截取需要做校验的字符串,再计算校验值
         */
        //AA+截取数据命令+输入光源16进制    之后再做异或校验值
        LogUtils.e("TAG==输入光源数值==16进制==" + inputSumLightData);

        String checkData = "AA" + str.substring(6, str.length() - 6);
        LogUtils.e("TAG==截取的长度=" + checkData);                            //AA000800000121
        LogUtils.e("TAG==需要计算异或的数据=" + checkData + inputSumLightData); //AA00080000012115
        String hexXORData = CalculateUtils.get16HexXORData(checkData + inputSumLightData);
        LogUtils.e("TAG==发送的异或结果=" + hexXORData);
        //AA+截取数据命令+输入光源16进制    之后再做异或校验值+DD结尾
        sendStringData = str.substring(0, str.length() - 6) + inputSumLightData + hexXORData + "dd";
        LogUtils.e("TAG==发送的结果=" + sendStringData);
        //16进制String转换成byte字节数组
        byte[] bytes = CalculateUtils.hexString2Bytes(sendStringData);
        return bytes;
    }

    private void initThread() {
//        receiveThread = new ReceiveThread();
//        receiveThread.start();
        broadcastThread = new BroadcastThread();
        broadcastThread.start();

    }

    private void initIp() {
        //Wifi状态判断
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            CurrentIP = getIpString(wifiInfo.getIpAddress());
            ipInfo.append(CurrentIP);
            System.out.println("IP IP:" + CurrentIP);
        }
    }

    /**
     * 将获取到的int型ip转成string类型
     */
    private String getIpString(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
                + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    private void sendMessageToThread(Handler mhandler) {
        Message msg = Message.obtain();
        sendContent = et_sendInfo.getText().toString();
        msg.obj = sendContent;
        msg.what = 1;
        mhandler.sendMessage(msg);
    }

    private void initView() {
        ipInfo = (TextView) findViewById(R.id.ip_info);
        linear_all = (LinearLayout) findViewById(R.id.linear_all);
        sendUDPBrocast = (Button) findViewById(R.id.sendUDPBrocast);
        tv_receive = findViewById(R.id.tv_receive);
        et_sendInfo = findViewById(R.id.et_sendContent);
        mRecycleView = findViewById(R.id.recycle);
        btn_send = findViewById(R.id.btn_sendInfo);
        btnText = findViewById(R.id.btnText);
        mStatusTv = findViewById(R.id.statusTv);
        btnText_stop = findViewById(R.id.btnText_stop);
        mProgressBar = findViewById(R.id.progressBar01);
        mProgressBar02 = findViewById(R.id.progressBar02);
        mProgressBar03 = findViewById(R.id.progressBar03);
        mProgressBar04 = findViewById(R.id.progressBar04);
        mProgressBar05 = findViewById(R.id.progressBar05);

        mDataList = new ArrayList<>();
        mAdapter = new ItemAdapter(mDataList);
        mRecycleView.setAdapter(mAdapter);


        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageToThread(broadcastThread.mhandler);
            }
        });
        btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                StatusUtil.Status status = StatusUtil.getStatus(task);
                StatusUtil.Status completedOrUnknown = StatusUtil.isCompletedOrUnknown(task);
                BreakpointInfo currentInfo = StatusUtil.getCurrentInfo(path, dirName + "/CME_01", "DDD.mp4");

                tv_receive.setText("status.name()==" + status.name() + "completedOrUnknown==" + completedOrUnknown + "+currentInfo==" + currentInfo.getFilename());

            }
        });
        sendUDPBrocast.setOnClickListener(new SendUDPBrocastListener());


        btnText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "开始~", Toast.LENGTH_SHORT).show();

//                getTextData();
                getDownData();
            }
        });
        btnText_stop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "暂停~", Toast.LENGTH_SHORT).show();

                context.stop();
            }
        });

    }

    private void getDownData() {

        XXPermissions.with(this)
                // 不适配 Android 11 可以这样写
//                .permission(Permission.Group.STORAGE)
                // 适配 Android 11 需要这样写，这里无需再写 Permission.Group.STORAGE
//                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
//                .permission(Permission.WRITE_EXTERNAL_STORAGE)
//                .permission(Permission.READ_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            startDown01();
//
//                            startDown02();

                        }
                    }


                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(MainActivity.this, permissions);
                        } else {
                        }
                    }
                });


    }


    String path = "http://192.168.131.43:7001/1197/333333320220424151418164.mp4";
//    String path = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";


    /**
     * eventbus 下载任务==结束
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void DownEndEvent(DownEndEvent event) {
        LogUtils.e("DownSelectedVideoActivity02====下载任务==结束...==== ");

    }

    /**
     * eventbus 下载任务==下载中
     * <p>
     * 还需要做结束的监听,结束了 就查询数据库
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void DownLoadingEvent(DownLoadingEvent event) {


        LogUtils.e("DownSelectedVideoActivity02====下载任务==下载中...==== ");

        ArrayList<ItemBean> data = mAdapter.getData();
        if (null != data && data.size() != 0) {
//            mDataList.clear();
            for (int i = 0; i < data.size(); i++) {
                ItemBean bean = data.get(i);
                if (bean.getTag().equals(event.getTag())) {
                    bean.setMaxLength(event.maxLength);
                    bean.setMaxLength(event.currentLength);
                    mAdapter.setItem(i,bean);
                }
            }

        }



    }

    /**
     * 下载任务 开始,添加到列表中
     * <p>
     * <p>
     * 1,判断列表是否存在,存在不添加,
     * 2,不存在,添加
     */
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void DownStartEvent(DownStartEvent event) {
        LogUtils.e("DownSelectedVideoActivity02====下载任务==开始...==== ");

        ArrayList<ItemBean> data = mAdapter.getData();
        if (null != data && data.size() != 0) {
            mDataList.clear();
            for (int i = 0; i < data.size(); i++) {
                ItemBean bean = data.get(i);
                if (bean.getTag().equals(event.getTag())) {
                } else {
                    ItemBean itemBean = new ItemBean();
                    itemBean.setCurrentLength(event.getCurrentLength());
                    itemBean.setMaxLength(event.getMaxLength());
                    itemBean.setTag(event.getTag());
                    mDataList.add(itemBean);
                }
            }

            mAdapter.setData(mDataList);


        } else {
            mDataList.clear();
            ItemBean itemBean = new ItemBean();
            itemBean.setCurrentLength(event.getCurrentLength());
            itemBean.setMaxLength(event.getMaxLength());
            itemBean.setTag(event.getTag());
            mDataList.add(itemBean);
            mAdapter.setData(mDataList);
        }

    }


    private void startDown01() {
        LogUtils.e("DownloadListener==startDown01:startDown01 ");

        listener04 = new DownloadListener4WithSpeed() {
            @Override
            public void taskStart(@NonNull DownloadTask task) {

                //队列只有一个的时候,只会发一次

                LogUtils.e("DownloadListener==taskStart: " + task);
            }

            @Override
            public void fetchStart(@NonNull DownloadTask task, int blockIndex, long contentLength) {
                super.fetchStart(task, blockIndex, contentLength);
                LogUtils.e("DownloadListener==fetchStart==task.getTag==: " + task.getTag()); //02
                LogUtils.e("DownloadListener==fetchStart==task.getParentFile==: " + task.getParentFile());// /storage/emulated/0/CME_01
                LogUtils.e("DownloadListener==fetchStart==task.getFile==: " + task.getFile());///storage/emulated/0/CME_01/222.mp4
                LogUtils.e("DownloadListener==fetchStart==task.getFilename==: " + task.getFilename());//222.mp4
                LogUtils.e("DownloadListener==fetchStart==contentLength==: " + contentLength);

                //http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4
                LogUtils.e("DownloadListener==fetchStart==task.getUrl==: " + task.getUrl());
//                LogUtils.e("DownloadListener==fetchStart: " + contentLength);
                LogUtils.e("DownloadListener==fetchStart==contentLength== long -String KB/MB==: " + FileUtil.formatFileSizeMethod(contentLength));//字符串转换 long -String KB/MB


                DownStartEvent event = new DownStartEvent();
                event.setTag((String) task.getTag());
                event.setMaxLength(task.getInfo().getTotalLength());
                event.setCurrentLength(contentLength);
                EventBus.getDefault().postSticky(event);
            }

            @Override
            public void connectStart(@NonNull DownloadTask task, int blockIndex, @NonNull Map<String, List<String>> requestHeaderFields) {
                LogUtils.e("DownloadListener==connectStart: " + task.getTag());

            }

            @Override
            public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode, @NonNull Map<String, List<String>> responseHeaderFields) {
                LogUtils.e("DownloadListener==connectEnd:==responseCode " + responseCode);


            }

            @Override
            public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info, boolean fromBreakpoint, @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {

                LogUtils.e("DownloadListener==infoReady==task.getEtag(): " + task.getTag());
                LogUtils.e("DownloadListener==infoReady==info.getTotalLength(): " + info.getTotalLength());
                LogUtils.e("DownloadListener==infoReady==info.getTaskSpeed(): " + model.getTaskSpeed());

            }

            @Override
            public void progressBlock(@NonNull DownloadTask task, int blockIndex, long currentBlockOffset, @NonNull SpeedCalculator blockSpeed) {
                LogUtils.e("DownloadListener==progressBlock==task.toString(): " + task.toString());
                LogUtils.e("DownloadListener==progressBlock==task.getReadBufferSize(): " + task.getReadBufferSize());
                LogUtils.e("DownloadListener==progressBlock==task.getFlushBufferSize(): " + task.getFlushBufferSize());
                LogUtils.e("DownloadListener==progressBlock==task.getRedirectLocation(): " + task.getRedirectLocation());
                LogUtils.e("DownloadListener==progressBlock==task.getTag(): " + task.getTag());
                LogUtils.e("DownloadListener==progressBlock==task.getFilename(): " + task.getFilename());
                LogUtils.e("DownloadListener==progressBlock==blockSpeed: " + blockSpeed.speed());
                LogUtils.e("DownloadListener==progressBlock==getTotalLength: " + task.getInfo().getTotalLength());
                LogUtils.e("DownloadListener==progressBlock==getTotalOffset: " + task.getInfo().getTotalOffset());
                LogUtils.e("DownloadListener==progressBlock==getInstantSpeedDurationMillis: " + blockSpeed.getInstantSpeedDurationMillis());
                LogUtils.e("DownloadListener==progressBlock==currentBlockOffset: " + currentBlockOffset);
                DownLoadingEvent event = new DownLoadingEvent();
                event.setTag((String) task.getTag());
                event.setMaxLength(task.getInfo().getTotalLength());
                event.setCurrentLength(task.getInfo().getTotalOffset());
                EventBus.getDefault().postSticky(event);

//                mProgressBar.setProgress((int) currentBlockOffset);
                String tag = (String) task.getTag();
                switch (tag) {
                    case "01":
                        mProgressBar.setMax((int) task.getInfo().getTotalLength());
                        mProgressBar.setProgress((int) currentBlockOffset);
                        break;
                    case "02":
                        mProgressBar02.setProgress((int) currentBlockOffset);
                        break;
                    case "03":
                        mProgressBar03.setProgress((int) currentBlockOffset);
                        break;
                    case "04":
                        mProgressBar04.setProgress((int) currentBlockOffset);
                        break;
                    case "05":
                        mProgressBar05.setProgress((int) currentBlockOffset);
                        break;
                }
            }

            @Override
            public void progress(@NonNull DownloadTask task, long currentOffset, @NonNull SpeedCalculator taskSpeed) {
                LogUtils.e("DownloadListener==progress==currentOffset: " + currentOffset);
                LogUtils.e("DownloadListener==progress==task.getTag: " + task.getTag());
                LogUtils.e("DownloadListener==progress==task.getFilename: " + task.getFilename());
                LogUtils.e("DownloadListener==progress==taskSpeed: " + taskSpeed.speed());
//                mProgressBar.setProgress((int) currentOffset);
//                String tag = (String) task.getTag();
//
//                switch (tag) {
//                    case "01":
//                        mProgressBar.setProgress((int) currentOffset);
//                        break;
//                    case "02":
//                        mProgressBar02.setProgress((int) currentOffset);
//
//                        break;
//                    case "03":
//                        mProgressBar03.setProgress((int) currentOffset);
//                        break;
//                    case "04":
//                        mProgressBar04.setProgress((int) currentOffset);
//                        break;
//                    case "05":
//                        mProgressBar05.setProgress((int) currentOffset);
//                        break;
//                }

//                mStatusTv.setText(taskSpeed.speed());
            }

            @Override
            public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info, @NonNull SpeedCalculator blockSpeed) {
                LogUtils.e("DownloadListener==blockEnd==blockIndex: " + blockIndex);
                LogUtils.e("DownloadListener==blockEnd==blockSpeed: " + blockSpeed.speed());
                LogUtils.e("DownloadListener==blockEnd==blockIndex==getContentLength=: " + info.getContentLength());  //下载成功的前提下,这个就是总偏移量
                LogUtils.e("DownloadListener==blockEnd==blockIndex==getCurrentOffset=: " + info.getCurrentOffset());
                LogUtils.e("DownloadListener==blockEnd==blockIndex==getStartOffset=: " + info.getStartOffset());
                LogUtils.e("DownloadListener==blockEnd==blockIndex==getRangeLeft=: " + info.getRangeLeft());
            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull SpeedCalculator taskSpeed) {
                LogUtils.e("DownloadListener==taskEnd=1=cause.name(): " + cause.name());
                LogUtils.e("DownloadListener==taskEnd=1=task.getTag(): " + task.getTag());
                LogUtils.e("DownloadListener==taskEnd=1=task.getFilename(): " + task.getFilename());
                LogUtils.e("DownloadListener==taskEnd=1=task.getParentFile(): " + task.getParentFile().getAbsolutePath());//commonFolderName
                LogUtils.e("DownloadListener==taskEnd=1=task.taskSpeed(): " + taskSpeed.speed());

                LogUtils.e("DownloadListener==taskEnd=1=task.getFlushBufferSize(): " + task.getFlushBufferSize());
                LogUtils.e("DownloadListener==taskEnd=1=task.getReadBufferSize(): " + task.getReadBufferSize());
                LogUtils.e("DownloadListener==taskEnd=1=task.getTotalLength(): " + task.getInfo().getTotalLength());
                LogUtils.e("DownloadListener==taskEnd=1=task.getTotalOffset(): " + task.getInfo().getTotalOffset());

                LogUtils.e("DownloadListener==taskEnd=1=taskSpeed: " + taskSpeed.speed());
                LogUtils.e("DownloadListener==taskEnd=1=dirName: " + dirName);
                DownEndEvent event = new DownEndEvent();
                event.setTag((String) task.getTag());
                event.setMaxLength(task.getInfo().getTotalLength());
                event.setCurrentLength(task.getInfo().getTotalOffset());
                EventBus.getDefault().postSticky(event);

                if (cause.name().equals("COMPLETED")) {//CANCELED
                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{dirName + "/" + task.getFilename()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    //刷新成功的回调方法
                                    LogUtils.e("OkHttpUtils======资源刷新成功路径为===" + path);
                                    LogUtils.e("OkHttpUtils======资源刷新成功路径为===" + uri);

                                }
                            });
                    mStatusTv.setText("已完成");
                }

            }
        };
        String dirName = Environment.getExternalStorageDirectory() + "/CME_01";


        DownloadContext.Builder builderQueue = new DownloadContext.QueueSet()
                .setParentPathFile(new File(dirName))
                .setMinIntervalMillisCallbackProcess(150)
                .commit();
        builderQueue.setListener(new DownloadContextListener() {
            @Override
            public void taskEnd(@NonNull DownloadContext context, @NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause,
                                int remainCount) {
                LogUtils.e("DownloadListener==queue==taskEnd==taskSpeed==cause.name()==: " + cause.name());
                LogUtils.e("DownloadListener==queue==taskEnd==taskSpeed==remainCount==: " + remainCount);
                LogUtils.e("DownloadListener==queue==taskEnd==taskSpeed==task.getTag==: " + task.getTag());

            }

            @Override
            public void queueEnd(@NonNull DownloadContext context) {

            }
        });
        DownloadTask task1 = new DownloadTask.Builder(path, dirName, "111.mp4")
                .setConnectionCount(1)
                .setMinIntervalMillisCallbackProcess(30)
//                .setPassIfAlreadyCompleted(true)//已存在直接完成
                .setPassIfAlreadyCompleted(false)//会一直下载不管有没有缓存,
                .setPriority(1).build();
        task1.setTag("01");


        builderQueue.bindSetTask(task1);

        context = builderQueue.build();
        context.startOnSerial(listener04);//单个执行
//        context.startOnParallel(listener04);  //同时进行

// stop
//        context.stop();


//        LogUtils.e("OkHttpUtils======授权成功===");
//
//        String destFileDirName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CME_Down_Video";
//        String FileName = "测试.mp4";
//        OkHttpUtils//
//                .get()//
//                .url(path)//
//                .build()//
//                .execute(new FileCallBack(destFileDirName, FileName) {
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//                        LogUtils.e("OkHttpUtils======onError===" + e);
//
//                    }
//
//                    @Override
//                    public void inProgress(float progress, long total, int id) {
//                        super.inProgress(progress, total, id);
//                        LogUtils.e("OkHttpUtils======inProgress===" + progress);
//
//                    }
//
//                    @Override
//                    public void onResponse(File response, int id) {
//                        LogUtils.e("OkHttpUtils======response===" + response);
//                        String downFilePath = destFileDirName + "/" + FileName;
//                        LogUtils.e("OkHttpUtils==更新相册视频====最终地址==" + destFileDirName + "/" + FileName);
//
//                        //说明第一个参数上下文，第二个参数是文件路径例如
//                        ///storage/emulated/0/1621832516463_1181875151.mp4 第三个参数是文件类型，传空代表自行根据文件后缀判断刷新到相册
//                        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{downFilePath}, null,
//                                new MediaScannerConnection.OnScanCompletedListener() {
//                                    @Override
//                                    public void onScanCompleted(String path, Uri uri) {
//                                        //刷新成功的回调方法
//                                        LogUtils.e("OkHttpUtils======资源刷新成功路径为===" + path);
//                                        LogUtils.e("OkHttpUtils======资源刷新成功路径为===" + uri);
//
//                                    }
//                                });
//                    }
//
//
//                });
    }

    private void getTextData() {
        Gson mGson = GsonFactory.getSingletonGson();
        HandBean handBean = new HandBean();
        handBean.setHelloPc("");
        handBean.setComeFrom("");
        String s = mGson.toJson(handBean);
        for (int i = 0; i < 500000; i++) {
            byte[] sendByteData = CalculateUtils.getSendByteData(this, s, "08", "00000000000000000000000000000000",
                    "30");

            LogUtils.e("sendByteData===i==" + i + "====data==" + sendByteData);
        }


    }


}