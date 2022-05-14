package com.example.mydemo.utils;

import android.content.Context;
import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * company：江西神州医疗设备有限公司
 * author： LoveLin
 * time：2021/7/16 8:31
 * desc：各种进制计算的工具类
 */
public class CalculateUtils {
    private static String sendCommandString;

    public static byte[] getSendByteData(Context mContext, String bean, String Received_Type,
                                         String Received_ID, String CMD) {

//      BroadCastDataBean bean = new BroadCastDataBean();
//      bean.setBroadcaster("szcme");                              //设备名字
//      bean.setRamdom(CalculateUtils.getCurrentTimeString());
//      广播发起随机时间戳:20220127104645
        //不转换的时候最后在CalculateUtils.hexString2Bytes(sendCommandString);回返回null,因为我那边模了不是偶数就会返回null

        LogUtils.e("SocketManage回调==哇哈哈==getSendByteData====Received_Type==" + Received_Type);
        if ("9".equals(Received_Type)) {
            Received_Type = "09";
        }
        if ("8".equals(Received_Type)) {
            Received_Type = "08";
        }
        if ("7".equals(Received_Type)) {
            Received_Type = "07";
        }
        String mSend_IDBy32 = MD5ChangeUtil.Md5_32(DeviceIdUtil.getDeviceId(mContext));
        String mData = CalculateUtils.str2HexStr(bean);                   //data  json字符串转16进制
        String mHead = "AAC5";                                             //帧头    ---2字节
        String mVer = "01";                                                //版本号  ---1字节
        String mLength = CalculateUtils.getLength(mData);                  //长度   ---2字节   40加data的长度 字符串的长度转成hex进制
        String mRandom = CalculateUtils.getRandomHexString(2);         // //随机数  ---1字节
        String mCMD_ID = "FF";                                              //命令ID   ---2字节-暂时规定,主动发起方为FF 接收方为随机值,PS--移动端目前交互写死值=FF
        String mSend_Type = "A1";                                           //发送方设备类型。--1字节-Android=A1  FF为所有设备
        String mSend_ID = mSend_IDBy32.toUpperCase();                       //发送方设备唯一标识。   --16字节
        String mReceived_Type = Received_Type;                              //接收方设备类型。   --FF是是所有设备
        String mReceived_ID = Received_ID;                                  //接收方设备唯一标识。   --16字节--目前暂时给32个0,模拟后台给的数据
        String mCMD = CMD;                                                  //UDP广播   --一个字节
        // 校验和，0xAA 依次与“Length、Random、CMD_ID、Send_Type、Send_ID、Received_Type、Received_ID、CMD、Data” 异或运算后的结果
        String CSString = "AA" + mLength + mRandom + mCMD_ID + mSend_Type + mSend_ID + mReceived_Type + mReceived_ID + mCMD + mData;
        String mCheck_Sum = CalculateUtils.get16HexXORData(CSString).toUpperCase();

        sendCommandString = mHead + mVer + mLength + mRandom + mCMD_ID + mSend_Type + mSend_ID + mReceived_Type +
                mReceived_ID + mCMD + mData + mCheck_Sum + "DD";
        LogUtils.e("UDP==命令===mData===" + mData);
        LogUtils.e("UDP==命令===mLength===" + mLength);
        LogUtils.e("UDP==命令===mRandom===" + mRandom);
        LogUtils.e("UDP==命令===mSend_ID===" + mSend_IDBy32);
        LogUtils.e("UDP==命令===mReceived_Type===" + mReceived_Type);
        LogUtils.e("UDP==命令===mReceived_ID===" + mReceived_ID);
        LogUtils.e("UDP==命令===异或的CSString===" + CSString);
        LogUtils.e("UDP==命令===异或的结果===" + mCheck_Sum);

        LogUtils.e("UDP==命令===最后发送的String===" + sendCommandString.length());
        LogUtils.e("UDP==命令===最后发送的String===异或的CSString==" + mCheck_Sum);
        LogUtils.e("UDP==命令===最后发送的String===" + sendCommandString);
        LogUtils.e("UDP==命令===最后发送的String===============================");
        byte[] bytes = CalculateUtils.hexString2Bytes(mData);
//        byte[] bytes = CalculateUtils.hexString2Bytes(sendCommandString);
        //AAC5 01 0059 EE22 FF A1 f9432b11b93e8bb4ae34539b7472c20e FF 00000000000000000000000000000000
        //FD 7B2262726F6164636173746572223A22737A636D65222C2272616D646F6D223A223230323230313237313132373535227D
        // F8 DD
        return bytes;
    }

    /**
     * 获取协议Length   的长度
     *
     * @return
     */
    public static String getLength(String data) {
//        40(固定长度)加变化data的长度然后-->字符串的长度转成hex进制
        LogUtils.e("SocketUtils===1111==点对点====data==" + data);   //7B22636F6D6546726F6D223A22222C2268656C6C6F5063223A22227D
        LogUtils.e("SocketUtils===1111==点对点====getLength==" + data.length());
        byte[] bytes = hexString2Bytes(data);

        int i = bytes.length + 40;
        //10进制转换成16进制  --保留 4位，不足补0
        String s = hex10To16Result4(i);
        LogUtils.e("SocketUtils===1111==点对点====getLength转换后==" + s.length());

        return s.toUpperCase();
    }


    /**
     * 16进制字符串--转--字节数组-----UDP---发包
     */
    public static byte[] hexString2Bytes(String data) {
        LogUtils.e("SocketUtils===2222==点对点====hex.length()==" + data.length());

        if ((data == null) || (data.equals(""))) {
            LogUtils.e("SocketUtils===2222==点对点====AAAA==" + data.length());

            return null;
        } else if (data.length() % 2 != 0) {
            LogUtils.e("SocketUtils===2222==点对点====BBBB==" + data.length());
            LogUtils.e("SocketUtils===2222==点对点====BBBB==" + data);


            return null;
        } else {
            data = data.toUpperCase();
            int len = data.length() / 2;
            byte[] b = new byte[len];
            char[] charDate = data.toCharArray();
            for (int i = 0; i < len; i++) {
                int p = 2 * i;
                b[i] = (byte) (charToByte(charDate[p]) << 4 | charToByte(charDate[p + 1]));

            }

            return b;
        }

    }

    /**
     * 10进制转换成16进制  --保留 4位，不足补0
     */
    public static String hex10To16Result4(int intValue) {

        return String.format("%04x", intValue);
    }


    /**
     * 获取十六进制的随机数
     */

    /**
     * 获取16进制随机数
     *
     * @param len len是指你要生成几位，
     * @return
     * @throws
     */
    public static String getRandomHexString(int len) {
        try {
            StringBuffer result = new StringBuffer();
            for (int i = 0; i < len; i++) {
                result.append(Integer.toHexString(new Random().nextInt(16)));
            }
            return result.toString().toUpperCase();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();

        }
        return null;

    }

    /**
     * 获取当前时间字符串
     */
    public static String getCurrentTimeString() {
        Date date = new Date(System.currentTimeMillis());
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String format = dateFormat.format(date);
//        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyyMdHms");
//        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//        String format1 = LocalDateTime.parse(format, inputFormat).format(outputFormat);
        return format;
    }

    /**
     * 字符串转换成为16进制(无需Unicode编码)
     *
     * @param str
     * @return
     */
    public static String str2HexStr(String str) {
        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            // sb.append(' ');
        }
        return sb.toString().trim();
    }


    /**
     * 16进制直接转换成为字符串(无需Unicode解码)
     *
     * @param hexStr
     * @return
     */
    public static String hexStr2Str(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }
/**
 *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *  *
 */


    /**
     * 十六进制异或运算
     * //16 to 10
     * String string = Integer.toHexString(10);
     * //10  to 16
     * int a = Integer.parseInt("A", 16);
     *
     * @param para
     * @return 获取异或值  校验结果
     */
//    校验和，0xAA 依次与“Length、Random、CMD_ID、Send_Type、Send_ID、Received_Type、Received_ID、CMD、Data” 异或运算后的结果
    public static String get16HexXORData(String para) {
        LogUtils.e("UDP==命令===获取校验和方法==para=" + para);
        LogUtils.e("UDP==命令===获取校验和方法==length=" + para.length());
        int length = para.length() / 2;
        String[] dateArr = new String[length];

        for (int i = 0; i < length; i++) {
            dateArr[i] = para.substring(i * 2, i * 2 + 2);
        }
        String code = "00";
        for (int i = 0; i < dateArr.length; i++) {
            code = xor(code, dateArr[i]);
        }
        Log.e("code", code);
        String result =code;
        if (result.length()==1){
            LogUtils.e("UDP==命令===最后发送的String===异或的CSString==长度=1==错误数据"+code);

            result = "0" + code;
            LogUtils.e("UDP==命令===最后发送的String===异或的CSString==长度=1==修正数据"+result);

        }else {
            result =code;
            LogUtils.e("UDP==命令===最后发送的String===异或的CSString==长度=2==计算数据"+code);

        }
        return result;
    }

    private static String xor(String strHex_X, String strHex_Y) {
        //将x、y转成二进制形式
        String anotherBinary = Integer.toBinaryString(Integer.valueOf(strHex_X, 16));
        String thisBinary = Integer.toBinaryString(Integer.valueOf(strHex_Y, 16));
        String result = "";
        //判断是否为8位二进制，否则左补零
        if (anotherBinary.length() != 8) {
            for (int i = anotherBinary.length(); i < 8; i++) {
                anotherBinary = "0" + anotherBinary;
            }
        }
        if (thisBinary.length() != 8) {
            for (int i = thisBinary.length(); i < 8; i++) {
                thisBinary = "0" + thisBinary;
            }
        }
        //异或运算
        for (int i = 0; i < anotherBinary.length(); i++) {
            //如果相同位置数相同，则补0，否则补1
            if (thisBinary.charAt(i) == anotherBinary.charAt(i))
                result += "0";
            else {
                result += "1";
            }
        }
//        byte[] bytes = result.getBytes();
//        //另一种转换，可以针对负数与正数的byte都可以以完整的单字节输出
//        byte positiveByte = 2;
//        String substring = Integer.toHexString((positiveByte & 0x000000ff) | 0xffffff00)
//                .substring(6);
//
//
//        LogUtils.e(substring.toUpperCase());//02
//
//        //        Log.e("code", result);
//
//
//        substring.toUpperCase();

//        String string = Integer.toHexString(Integer.parseInt(result, 2));
//        Log.e("UDP==命令===最后发送的String===string===", string);//aa
//
//        String s = numToHex16(Integer.parseInt(string));
//        Log.e("UDP==命令===最后发送的String===s===", s);

//        return s;
        return Integer.toHexString(Integer.parseInt(result, 2));
    }


    /**
     * 如何把udp返回的无符号byte数组,转换成string 类型?
     * 1:用byte数组接收，因为unsigned char范围是0-255，byte是-128-127，所有用byte类型数组接收后将byte转为int类型接收
     * 2:再将int类型转化为十六进制。
     */

    public static String getByteData2StringHexDate(byte[] data) {
        String str = "";
        if (null != data) {
            //创建等长度的int bate
            int[] intData = new int[data.length];
            for (int i = 0; i < data.length; i++) {
                //按位与，将signed类型转化为int的数字。因为Java中没有
                intData[i] = data[i] & 0xff;
                str = str + numToHex8(data[i]);
                if ("dd".equals(numToHex8(data[i]))) {
                    break;
                }
            }
            return str;
        } else {
            return "getStringHexDate  byte[] data is null";

        }

    }


    /**
     * 已下是把int类型转换成16进制
     *
     * @param b
     * @return 返回 使用1字节就可以表示b
     */
    //使用1字节就可以表示b  //15
    public static String numToHex8(int b) {
        return String.format("%02x", b);//2表示需要两个16进制数
    }

    //需要使用2字节表示b   //00 15
    public static String numToHex16(int b) {
        return String.format("%04x", b);
    }

    //需要使用4字节表示b   //00 00 00 15
    public static String numToHex32(int b) {
        return String.format("%08x", b);
    }


//    /**
//     * 16进制字符串--转--字节数组-----UDP---发包
//     */
//    public static byte[] hexString2Bytes(String hex) {
//
//        if ((hex == null) || (hex.equals(""))) {
//            return null;
//        } else if (hex.length() % 2 != 0) {
//            return null;
//        } else {
//            hex = hex.toUpperCase();
//            int len = hex.length() / 2;
//            byte[] b = new byte[len];
//            char[] charDate = hex.toCharArray();
//            for (int i = 0; i < len; i++) {
//                int p = 2 * i;
//                b[i] = (byte) (charToByte(charDate[p]) << 4 | charToByte(charDate[p + 1]));
//
//            }
//
//            return b;
//        }
//
//    }

    /**
     * 字符转换为字节
     */
    private static byte charToByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
//        LogUtils.e("TAG==字符转换为字节=" +b);
//        aac5000008000001211597dd
//        AAC5000008000001211597DD
        return b;
    }


    /**
     * 16进制转换成10进制
     */

    public static int hex16To10(String strHex) {
        BigInteger bigInteger = new BigInteger(strHex, 16);

        return bigInteger.intValue();
    }

    /**
     * 10进制转换成16进制
     */
    public static String hex10To16(int intValue) {

        return String.format("%02x", intValue);// 2表示需要两个16进制数
    }


    /**********************************转换字节数组为16进制字串****************第一种方式*******************************/
    /**
     * 字节数组转16进制字符串-----UDP---接受包数据
     */


//没有分隔符
    public static String getHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder("");

        if (bytes != null && bytes.length > 0) {
            for (int i = 0; i < bytes.length; ++i) {
                int v = bytes[i] & 255;
                String hv = Integer.toHexString(v);
                if (hv.length() < 2) {
                    stringBuilder.append(0);
                }

                stringBuilder.append(hv);
            }

            return stringBuilder.toString();
        } else {
            return null;
        }
    }

    public static String bytes2HexString(byte[] b) {
        String r = "";
        int count = 0;


        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }

            //如果自定义接收的byte字节是1024 就需要手动判断,提高性能,如果一开始就约定好协议是12个字节,就不需要判断
//            if ("dd".equals(hex) || "DD".equals(hex)) {
//                count++;
//            }
            r = r + hex.toUpperCase();
//如果自定义接收的byte字节是1024 就需要手动判断,提高性能,如果一开始就约定好协议是12个字节,就不需要判断
//            if (count == 1) {
//                return r;
//            }
//            r += hex.toUpperCase();
        }
        int dd = r.indexOf("DD");
        String recIp = r.substring(0, dd + 2);

        return r;
    }
    /**********************************转换字节数组为16进制字串****************第二种方式*******************************/
    /**
     * 转换字节数组为16进制字串
     *
     * @param b 字节数组
     * @return 16进制字串
     */
    public static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }

        return resultSb.toString();
    }

    public static String MD5Encode(String origin) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(resultString
                    .getBytes()));
        } catch (Exception ex) {
        }
        return resultString;
    }

    public static final String EMPTY_STRING = "";
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    /**
     * 转换字节数组为16进制字串
     *
     * @param b 字节数组
     * @return 16进制字串
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**********************************转换字节数组为16进制字串****************第二种方式*******************************/
    private static byte[] getSendDDData(String trim) {
        LogUtils.e("TAG==输入光源数值=trim===" + trim);

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
        LogUtils.e("TAG==异或结果=" + hexXORData);
        //AA+截取数据命令+输入光源16进制    之后再做异或校验值+DD结尾
        String sendStringData = str.substring(0, str.length() - 6) + inputSumLightData + hexXORData + "dd";
        LogUtils.e("TAG==发送的结果=" + sendStringData);
        //16进制String转换成byte字节数组
        byte[] bytes = CalculateUtils.hexString2Bytes(sendStringData);
        return bytes;
    }


}
