package udesk.udesksocket;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import udesk.core.UdeskConst;

/**
 * author : ${揭军平}
 * time   : 2017/11/23
 * desc   :
 * version: 1.0
 */

public class Util {


    private static int mId = 1;
    private static final Object lock = new Object();

    public static int getNextId() {
        synchronized (lock) {
            return ++mId;
        }
    }

    public static synchronized String buildMsgId() {
        long serTime = System.currentTimeMillis() / 1000;
        serTime = (serTime << 32) | getNextId();
        return String.valueOf(serTime);
    }

    public static String getUniqueId(Context context) {
        String androidID = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.CUPCAKE) {
            androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return androidID + Build.SERIAL;
    }

    public static String getUa() {
        StringBuilder builder = new StringBuilder();
        builder.append(Build.MODEL).append(Build.VERSION.RELEASE).append(";").append(UdeskSocketContants.Ver);
        return builder.toString();
    }

    public static int toInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int objectToInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Integer) {
            return (int) obj;
        }
        if (obj instanceof Double) {
            return Double.valueOf((Double) obj).intValue();
        }
        if (obj instanceof Float) {
            return Float.valueOf((Float) obj).intValue();
        }
        if (isNumeric(obj.toString())) {
            return toInt(obj.toString());
        }
        return 0;
    }

    private static Pattern NumberPattern = Pattern.compile("[0-9]*");
    public static boolean isNumeric(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        Matcher isNum = NumberPattern.matcher(str);
        return isNum.matches();
    }

    public static String objectToString(Object obj) {
        if (obj == null) {
            return "";
        }
        String string = "";
        if (obj instanceof String) {
            string = (String) obj;
        }
        try {
            string = String.valueOf(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (string.equals("null")) {
            string = "";
        }
        return string;
    }

    public static boolean objectToBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        return false;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

//    private static String toMD5(String text) throws NoSuchAlgorithmException {
//        //获取摘要器 MessageDigest
//        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
//        //通过摘要器对字符串的二进制字节数组进行hash计算
//        byte[] digest = messageDigest.digest(text.getBytes());
//
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < digest.length; i++) {
//            //循环每个字符 将计算结果转化为正整数;
//            int digestInt = digest[i] & 0xff;
//            //将10进制转化为较短的16进制
//            String hexString = Integer.toHexString(digestInt);
//            //转化结果如果是个位数会省略0,因此判断并补0
//            if (hexString.length() < 2) {
//                sb.append(0);
//            }
//            //将循环结果添加到缓冲区
//            sb.append(hexString);
//        }
//        //返回整个结果
//        return sb.toString();
//    }

    public static String secToTime(int time) {
        String timeStr;
        int hour;
        int minute;
        int second;
        if (time <= 0) {
            return "00:00";
        } else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 23) {
                    return "23:59:59";
                }
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }


    public static String unitFormat(int i) {
        String retStr;
        if (i >= 0 && i < 10) {
            retStr = "0" + Integer.toString(i);
        } else {
            retStr = "" + i;
        }
        return retStr;
    }

    /**
     * 获取精确到秒的时间戳
     *
     * @return
     */
    public static String getSecondTimestamp(Date date) {
        if (null == date) {
            return "";
        }
        String timestamp = String.valueOf(date.getTime());
        int length = timestamp.length();
        if (length > 3) {
            return timestamp.substring(0, length - 3);
        } else {
            return "";
        }
    }

    public static void getSignToken(String domain, String appid, final SigtokenCallBack sigtokenCallBack) {

        try {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            String time = getSecondTimestamp(new Date());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("subdomain", domain);
            jsonObject.put("timestamp", time);
            jsonObject.put("app_id", appid);
            jsonObject.put("token", MD5(domain + time));
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(JSON, jsonObject.toString());
            Log.i(UdeskSocketContants.Tag, jsonObject.toString());
            Request request = new Request.Builder()
                    .url(UdeskConst.signToenUrl)
                    .post(requestBody)
                    .build();
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    sigtokenCallBack.failure();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    sigtokenCallBack.response(response.body().string());
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String MD5(String s) {
        return MD5(s.getBytes());
    }

    /**
     * @return
     * @params
     */
    public static String MD5(byte[] btInput) {
        char hexDigits[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

}
