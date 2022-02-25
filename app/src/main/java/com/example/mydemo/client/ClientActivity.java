package com.example.mydemo.client;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mydemo.R;
import com.example.mydemo.utils.CalculateUtils;
import com.example.mydemo.utils.LogUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *发送端界面
 *
 * //这里只是开启两个普通线程
 * 发送数据线程
 * 接收数据线程
 */
public class ClientActivity extends AppCompatActivity {
    private static final String TAG = "SocketConnectActivity";
    private TextView ipInfo;
    private Button btnClear;
    private Button btnBack;
    private static int BroadCast_Port = 9999;
    private boolean isRunning = true;
    private DatagramSocket receiveSocket = null;
    private DatagramSocket sendSocket = null;
    private DatagramPacket dpReceive = null;
    private static String CurrentIP;
    private String receiveIp;
    private SendThread sendThread;
    private String sendStringData;
    private String sendIp;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    Log.i(TAG, "run handleMessage: ");
                    ipInfo.append("服务器发来的数据:" + msg.obj.toString() + "\n");
//                    ipInfo.append(msg.obj.toString() + "\n");
                    /**通过确认该客户端已经接收到信息后，再将自己的ip号码发送出去*/
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1500);
                                /**开一个将前一次接收的内容置为空的线程，解决发送端两次发送相同的信息，接收端他可能就不会做处理的问题*/
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    sendFeedBackToServer(sendThread.mSendHandler, msg.obj.toString());
                    break;
            }
        }

    };


    private void sendFeedBackToServer(Handler mHandler, String ip) {
        Message msg = new Message();
        msg.obj = ip;
        msg.what = 1;
        mHandler.sendMessage(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        initView();
    }

    private void initView() {
        ipInfo = findViewById(R.id.ip_info);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ipInfo.setText("");
            }
        });

        //创建接收数据的线程
        ReceiveThread receiveThread = new ReceiveThread();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            CurrentIP = getIpString(wifiInfo.getIpAddress());
            LogUtils.e("本机IP is :" + CurrentIP);
        }
        //创建发送数据的线程
        try {
            receiveSocket = new DatagramSocket(BroadCast_Port);
            sendThread = new SendThread();
            //开启发送线程,和接受线程
            sendThread.start();
            receiveThread.start();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    private class SendThread extends Thread {
        private Handler mSendHandler;

        @SuppressLint("HandlerLeak")
        @Override
        public void run() {
            super.run();
            Looper.prepare();
            mSendHandler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    DatagramPacket dpSend = null;
                    //模拟发送数据
                    byte[] ip = getSendData("21");  //AAC5000008000001211597DD
                    try {
                        LogUtils.e("TAG==send==receiveIp==" + receiveIp);
                        LogUtils.e("TAG==send==CurrentIP==" + CurrentIP);
                        LogUtils.e("TAG==send==sendIp==" + sendIp);
                        if (receiveIp != CurrentIP) {
                            InetAddress inetAddress = InetAddress.getByName(sendIp);
                            dpSend = new DatagramPacket(ip, ip.length, inetAddress, BroadCast_Port);
//
                            String sendData = CalculateUtils.byteArrayToHexString(ip).trim();
//                            int dd = sendData.indexOf("DD");
//                            String recIp = sendData.substring(0, dd + 2);
                            sendSocket = new DatagramSocket();
                            sendSocket.send(dpSend);
                            Log.i(TAG, "run: send message : " + sendData);
                            //每次必须关闭sendSocket,需要发送消息在从新创建不然会堵塞
                            sendSocket.close();

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();

        }
    }

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                if (isRunning) {
                    try {
                        byte[] buff = new byte[1024];
                        dpReceive = new DatagramPacket(buff, buff.length);
                        receiveSocket.receive(dpReceive);
                        receiveIp = dpReceive.getAddress().toString().substring(1);

                        LogUtils.e("TAG==receive==receiveIp==" + receiveIp);
                        LogUtils.e("TAG==receive==CurrentIP==" + CurrentIP);
                        LogUtils.e("TAG==receive==sendIp=00=" + sendIp);
                        if (receiveIp != CurrentIP) {
                            sendIp = receiveIp;
                        }
                        LogUtils.e("TAG==receive==sendIp=01=" + sendIp);

                        //把接收到的--字节数组-->16进制字串
                        String sendData = CalculateUtils.byteArrayToHexString(dpReceive.getData()).trim();
                        int dd = sendData.indexOf("DD");
                        String recIp = sendData.substring(0, dd + 2);
                        //获取服务器ip地址
                        receiveIp = dpReceive.getAddress().toString().substring(1);

                        //发送消息更新界面UI-->然后在发送消息给服务器
                        Message message = mHandler.obtainMessage();
                        message.what = 1;
                        message.obj = recIp;
                        mHandler.sendMessage(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

            }


        }
    }

    /**
     * 将获取到的int型ip转成string类型
     */
    private String getIpString(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
                + ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        receiveSocket.close();
        LogUtils.e("TAG==UDP Client程序退出,关掉socket,停止广播=");
        isRunning = false;
    }
}