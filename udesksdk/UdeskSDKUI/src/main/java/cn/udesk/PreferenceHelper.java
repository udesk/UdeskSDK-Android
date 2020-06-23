package cn.udesk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import udesk.core.utils.AES;

import static javax.xml.transform.OutputKeys.ENCODING;


public class PreferenceHelper {


    public static void write(Context context, String fileName, String k,
                             String v) {

        try {
            SharedPreferences preference = context.getSharedPreferences(fileName,
                    Context.MODE_PRIVATE);
//            String encrypt = "";
//            if (TextUtils.isEmpty(v)) {
//                encrypt = "";
//            } else {
//                encrypt = AES.getInstance().encrypt(v.getBytes("UTF8"));
//            }

            Editor editor = preference.edit();
            editor.putString(k, v);
            editor.apply();
//            Log.i("xxxx", "key = " + k + "  ;value = " + v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String readString(Context context, String fileName, String k) {
        SharedPreferences preference = context.getSharedPreferences(fileName,
                Context.MODE_PRIVATE);
        String value = preference.getString(k, "");
        if (TextUtils.isEmpty(value)) {
            return "";
        }
//        String decrypt = AES.getInstance().decrypt(value);
//        Log.i("xxxx", "key = " + k + "  ;value = " + decrypt);
        return value;
    }


}
