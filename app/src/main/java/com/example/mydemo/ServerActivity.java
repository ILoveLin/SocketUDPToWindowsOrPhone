package com.example.mydemo;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mydemo.bean.BroadCastDataBean;
import com.example.mydemo.bean.SocketDataBean;
import com.example.mydemo.utils.CalculateUtils;
import com.example.mydemo.utils.DeviceIdUtil;
import com.example.mydemo.utils.LogUtils;
import com.example.mydemo.utils.MD5ChangeUtil;
import com.example.mydemo.utils.ThreadManager;
import com.google.gson.Gson;
import com.lzh.easythread.AsyncCallback;
import com.lzh.easythread.EasyThread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Callable;

/**
 * 服务端界面
 * 这里使用EasyThread线程池管理   效率大大滴
 * 有接收线程--接收数据---固定核心线程(FixedThreadPool )
 * 有发送线程--回写数据---缓存线程(CachedThreadPool )
 * * 备注:前提条件必须,电脑打开CME能ping通App的ip,才能进行socket通讯
 * * <p>
 * * 使用方式一:普通的Runnable任务,更新UI需要在主线程
 * * <p>
 * * 1:点对点使用
 * * a,设置inetAddress
 * * inetAddress = InetAddress.getByName("192.168.132.102");     //目标ip
 * * b,开启接收线程
 * * easyFixed2Thread.execute(getReceiveRunnable());      //RECEIVE_PORT  是本地App监听端口
 * * c,在点击事件中发送消息(多发几次可能发送数据丢失)
 * * easyCacheThread.execute(getSendRunnable(mSendContent.getText().toString().trim()));     //BROADCAST_PORT服务器端口
 * * <p>
 * * <p>
 * * 2:广播使用
 * * a,设置inetAddress
 * * inetAddress = InetAddress.getByName("255.255.255.255");     //广播目标ip
 * * b,开启接收线程
 * * easyFixed2Thread.execute(getReceiveRunnable());      //RECEIVE_PORT  是本地App监听端口
 * * c,在点击事件中发送消息(多发几次可能发送数据丢失)
 * * easyCacheThread.execute(getSendBroadcastRunnable(mSendContent.getText().toString().trim()));    //BROADCAST_PORT服务器端口
 * *
 * *
 * * 使用方式二:异步回调任务
 * *
 * *  备注,这个方式有个弊端就是,接收线程接受到一次消息就会关闭,适用于一次对话的使用场景
 * *  a,开启接收线程
 * *  startAsyncReceive();      //RECEIVE_PORT  是本地App监听端口
 * *  b,在点击事件中发送消息(多发几次可能发送数据丢失)
 * *  easyCacheThread.execute(getSendBroadcastRunnable(mSendContent.getText().toString().trim()));    //BROADCAST_PORT服务器端口
 * *
 */

public class ServerActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static String CurrentIP;
    private static int BROADCAST_PORT = 8005;
    private static int RECEIVE_PORT = 7006;
    //    private static String SEND_IP = "192.168.64.13";
    private static String SEND_IP = "255.255.255.255";
    //    private static String BROADCAST_IP = "192.168.64.13";
    private InetAddress inetAddress = null;
    private DatagramSocket mSendSocket = null;
    private DatagramSocket mReceiveSocket = null;
    private volatile boolean isRuning = true;
    private EditText mSendContent;
    private Button mSend;
    private Button mClear;
    private TextView mReceive;
    private TextView mMyIp;
    private String sendStringData;
    private Runnable mSendRunnable;
    private EasyThread easyCacheThread;
    private Runnable mReceiveRunnable;
    private EasyThread easyFixed2Thread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main02);
        easyCacheThread = ThreadManager.getCache();
        easyFixed2Thread = ThreadManager.getIO();
        initView();
        try {
//            inetAddress = InetAddress.getByName("192.168.132.102");
            inetAddress = InetAddress.getByName("255.255.255.255");
        } catch (Exception e) {
            e.printStackTrace();
        }
        initTask();

        //模拟获取到命令的-字节数组
        getUDPBroadCMD();


    }

    private void getUDPBroadCMD() {
        Gson gson = new Gson();
        BroadCastDataBean bean = new BroadCastDataBean();
        bean.setBroadcaster("szcme");
        bean.setRamdom("20220126143222503");

        //十进制的40+data的长度转16进制==Length
//
//AAC5 01 005C E0 00 A0 3399CBE9A32D4786ABF24E39D3CAD576 FF 00000000000000000000000000000000
//FD 7B2262726F6164636173746572223A22737A636D65222C2272616D646F6D223A223230323230313236313433323232353033227D EA DD
        String deviceId = DeviceIdUtil.getDeviceId(getApplicationContext());
        String MD32 = MD5ChangeUtil.Md5_32(deviceId);
        String s = MD32.toUpperCase();
        String mHead = "AAC5";  //帧头    ---2字节
        String mVer = "01";     //版本号  ---1字节
        String mLength = "005C"; //长度   ---2字节
        String mRandom = "E0";// //随机数  ---1字节
//        String mRandom = CalculateUtils.getRandomHexString(4);// //随机数  ---1字节
        String mCMD_ID = "00";   //命令ID   ---2字节-暂时规定,主动发起方为FF 接收方为随机值,PS--移动端目前交互写死值=FF
        String mSend_Type = "A0";   //发送方设备类型。   --1字节-Android=A1  FF为所有设备
        String mSend_ID = s;   //发送方设备唯一标识。   --16字节
        String mReceived_Type = "FF";   //接收方设备类型。   --FF是是所有设备
        String mReceived_ID = "00000000000000000000000000000000";   //接收方设备唯一标识。   --16字节--目前暂时给32个0,模拟后台给的数据
        String mCMD = "FD";   //UDP广播   --一个字节
        LogUtils.e("UDP==命令===mRandom===" + mRandom);
        LogUtils.e("UDP==命令===timeInMillis===" + CalculateUtils.getCurrentTimeString());
        String mData = "7B2262726F6164636173746572223A22737A636D65222C2272616D646F6D223A223230323230313236313433323232353033227D";
        // 校验和，0xAA 依次与“Length、Random、CMD_ID、Send_Type、Send_ID、Received_Type、Received_ID、CMD、Data” 异或运算后的结果
        String CSString = "AA" + mLength + mRandom + mCMD_ID + mSend_Type + mSend_ID + mReceived_Type + mReceived_ID + mCMD + mData;
        String hexXORData = CalculateUtils.get16HexXORData(CSString);
        String hexXORData1 = CalculateUtils.get16HexXORData("AA" + mLength);
        LogUtils.e("UDP==命令===异或的CSString===" + CSString);
        LogUtils.e("UDP==命令===异或的结果===" + hexXORData);
        LogUtils.e("UDP==命令===异或的结果===" + hexXORData1);
        String sendCommandString = "AAC5" + mVer + mLength + mRandom + mCMD_ID + mSend_Type + mSend_ID + mReceived_Type +
                mReceived_ID + mCMD + mData + CSString + "DD";
        LogUtils.e("UDP==命令===发送的String===" + sendCommandString);

    }

    private void initTask() {
        //异步回调任务
//        startAsyncReceive();
        //普通的Runnable任务,更新UI需要在主线程

        //方式二点对点  测试OK
        easyFixed2Thread.execute(getReceiveRunnable());
//        easyCacheThread.execute(getSendRunnable(mSendContent.getText().toString().trim()));

        //方式一:发送广播  测试OK
        easyCacheThread.execute(getSendBroadcastRunnable(mSendContent.getText().toString().trim()));

    }

    private void startAsyncReceive() {
        // 异步执行任务
        Callable<SocketDataBean> callable = new Callable<SocketDataBean>() {
            @Override
            public SocketDataBean call() throws Exception {
                // do something
                LogUtils.e("正在执行Runnable任务：%s" + Thread.currentThread().getName());
                byte[] receiveData = new byte[1024];
                DatagramPacket mReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    //这种写法容易多次进入退出这个界面,容易端口绑定异常:java.net.BindException: Address already in use
                    // mReceiveSocket = new DatagramSocket(RECEIVE_PORT);
                    //所以使用此方法,设置端口可重复使用,在绑定端口
                    if (mReceiveSocket == null) {
                        mReceiveSocket = new DatagramSocket(null);
                        //设置端口可重复使用
                        mReceiveSocket.setReuseAddress(true);
                        //在绑定端口
                        mReceiveSocket.bind(new InetSocketAddress(RECEIVE_PORT));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (true) {
                    if (isRuning) {
                        try {
                            LogUtils.e("======ReceiveThread=====000==");
                            mReceiveSocket.receive(mReceivePacket);
                            LogUtils.e("======ReceiveThread=====111==");
                            String rec = CalculateUtils.byteArrayToHexString(mReceivePacket.getData()).trim();
                            int dd = rec.indexOf("DD");
                            String recIp = rec.substring(0, dd + 2);
                            LogUtils.e("======ReceiveThread=====222==" + recIp);
                            if (mReceivePacket != null) {
//                                Message revMessage = Message.obtain();
//                                revMessage.what = 1;
//                                revMessage.obj = recIp;

                                SocketDataBean socketDataBean = new SocketDataBean();
                                socketDataBean.setData("" + recIp);
                                Log.i(TAG, "handleMessage: ReceiveThread receive ip" + recIp);
                                return socketDataBean;

//                                for (int i = 0; i < 500000; i++) {
//                                    int finalI = i;
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            mReceive.setText(recIp + "===" + finalI);
//                                        }
//                                    });
//                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

            }
        };

        // 异步回调
        AsyncCallback<SocketDataBean> async = new AsyncCallback<SocketDataBean>() {
            @Override
            public void onSuccess(SocketDataBean user) {
                // notify success;
                LogUtils.e("======ReceiveThread=====onSuccess==" + user.getData());
                for (int i = 0; i < 2; i++) {
                    mReceive.append(user.getData() + "=");
                }
            }

            @Override
            public void onFailed(Throwable t) {
                // notify failed.
                LogUtils.e("======ReceiveThread=====onFailed==");

            }
        };

        // 启动异步任务
        easyFixed2Thread.async(callable, async);
    }

    private Runnable getReceiveRunnable() {
        mReceiveRunnable = new Runnable() {
            @Override
            public void run() {
                LogUtils.e("正在执行Runnable任务：%s" + Thread.currentThread().getName());
                byte[] receiveData = new byte[1024];
                DatagramPacket mReceivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    //本地监听的端口   此种方法的弊端容易产生java.net.BindException: Address already in use
                    //mReceiveSocket = new DatagramSocket(RECEIVE_PORT);
                    //所以使用此处方法替换,设置为null,在设置端口可重复使用,再绑定端口
                    if (mReceiveSocket == null) {
                        mReceiveSocket = new DatagramSocket(null);
                        mReceiveSocket.setReuseAddress(true);
                        mReceiveSocket.bind(new InetSocketAddress(RECEIVE_PORT));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (true) {
                    if (isRuning) {
                        try {
                            LogUtils.e("======ReceiveThread=====000==");
                            mReceiveSocket.receive(mReceivePacket);
                            LogUtils.e("======ReceiveThread=====111==");
                            String rec = CalculateUtils.byteArrayToHexString(mReceivePacket.getData()).trim();
                            int dd = rec.indexOf("DD");
                            String recIp = rec.substring(0, dd + 2);
                            LogUtils.e("======ReceiveThread=====222==" + recIp);
                            if (mReceivePacket != null) {
//                                Message revMessage = Message.obtain();
//                                revMessage.what = 1;
//                                revMessage.obj = recIp;
                                for (int i = 0; i < 500; i++) {
                                    int finalI = i;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mReceive.setText(recIp + "===" + finalI);
                                        }
                                    });
                                }
                                Log.i(TAG, "handleMessage: ReceiveThread receive ip" + recIp);


                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        };
        return mReceiveRunnable;

    }

    //
    private Runnable getSendBroadcastRunnable(String data) {

        mSendRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] sendData = getSendData(data);
//                    byte[] sendData = data.getBytes();
                    DatagramPacket mSendPacket = new DatagramPacket(sendData, sendData.length, inetAddress, BROADCAST_PORT);
                    for (int i = 0; i < 5; i++) {
                        mSendSocket = new DatagramSocket();
                        mSendSocket.send(mSendPacket);
                        mSendSocket.setBroadcast(true);
                        mSendSocket.close();
                    }
                } catch (Exception e) {

                }
            }
        };
        return mSendRunnable;
    }

    private Runnable getSendRunnable(String data) {

        mSendRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] sendData = getSendData(data);
                    DatagramPacket mSendPacket = new DatagramPacket(sendData, sendData.length, inetAddress, BROADCAST_PORT);
                    for (int i = 0; i < 5; i++) {
                        mSendSocket = new DatagramSocket();
                        mSendSocket.send(mSendPacket);
                        mSendSocket.close();
                    }
                } catch (Exception e) {
                    LogUtils.e(e + "");
                }
            }
        };
        return mSendRunnable;
    }

    private void initView() {

        mSendContent = findViewById(R.id.et_sendContent);
        mSend = findViewById(R.id.btn_sendInfo);
        mClear = findViewById(R.id.btnClear);
        mReceive = findViewById(R.id.tv_receive);
        mMyIp = findViewById(R.id.ip_info);

        //Wifi状态判断
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            CurrentIP = getIpString(wifiInfo.getIpAddress());
            mMyIp.append(CurrentIP);
            LogUtils.e("mMyIp====:" + CurrentIP);
        }
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点对点
                easyCacheThread.execute(getSendRunnable(mSendContent.getText().toString().trim()));
                //广播
//                easyCacheThread.execute(getSendBroadcastRunnable(mSendContent.getText().toString().trim()));
            }
        });
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReceive.setText("");
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRuning = false;
        mReceiveSocket.close();
        LogUtils.e("UDP Server程序退出,关掉mReceiveSocket,接收消息");
        finish();
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
        LogUtils.e("TAG==输入光源数值==16进制==" + inputSumLightData);  //15
        LogUtils.e("TAG==输入光源数值==截取==" + str.substring(6, str.length() - 6));//000800000121

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


}