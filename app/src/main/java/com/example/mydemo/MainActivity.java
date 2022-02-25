package com.example.mydemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mydemo.utils.CalculateUtils;
import com.example.mydemo.utils.LogUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

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
    private ReceiveThread receiveThread;
    private String sendStringData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
                    dpSend = new DatagramPacket(data, data.length, inetAddress,BROADCAST_PORT);
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


    private class ReceiveThread extends Thread {

        private DatagramSocket datagramSocket;

        @Override
        public void run() {
            byte[] receiveData = new byte[1024];
            DatagramSocket receiveSockett = null;
            {
                try {
//                    receiveSockett = new DatagramSocket(BROADCAST_PORT);
                    receiveSockett = new DatagramSocket(0);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }

            DatagramPacket dpReceive = new DatagramPacket(receiveData, receiveData.length);
            // 建立Socket连接
            try {
                datagramSocket = new DatagramSocket(BROADCAST_PORT);
//                datagramSocket = new DatagramSocket(0);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            LogUtils.e("======ReceiveThread=====启动接收线程==");

            while (true) {
                if (isRuning) {
                    try {
                        LogUtils.e("======ReceiveThread=====000==");
                        datagramSocket.receive(dpReceive);
                        LogUtils.e("======ReceiveThread==0000000111111==");
                        LogUtils.e("======ReceiveThread=====111==");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String rec = CalculateUtils.byteArrayToHexString(dpReceive.getData()).trim();
                    int dd = rec.indexOf("DD");
                    String recIp = rec.substring(0, dd + 2);
                    LogUtils.e("======ReceiveThread=====222==" + recIp);

//                    String recIp = dpReceive.getData().toString();   //后去发送的数据
//                    String recIp = dpReceive.getAddress().toString().substring(1);   //获取发送的ip
//                    if (dpReceive != null) {
                        Message revMessage = Message.obtain();
                        revMessage.what = 1;
                        revMessage.obj = recIp;
                        Log.i(TAG, "handleMessage: ReceiveThread receive ip" + recIp);

                        myHandler.sendMessage(revMessage);
//                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        receiveThread = new ReceiveThread();
        receiveThread.start();
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

    private void initView() {
        ipInfo = (TextView) findViewById(R.id.ip_info);
        sendUDPBrocast = (Button) findViewById(R.id.sendUDPBrocast);
        tv_receive = findViewById(R.id.tv_receive);
        et_sendInfo = findViewById(R.id.et_sendContent);
        btn_send = findViewById(R.id.btn_sendInfo);

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
                tv_receive.setText("");
            }
        });
        sendUDPBrocast.setOnClickListener(new SendUDPBrocastListener());
    }

    private void sendMessageToThread(Handler mhandler) {
        Message msg = Message.obtain();
        sendContent = et_sendInfo.getText().toString();
        msg.obj = sendContent;
        msg.what = 1;
        mhandler.sendMessage(msg);
    }

}