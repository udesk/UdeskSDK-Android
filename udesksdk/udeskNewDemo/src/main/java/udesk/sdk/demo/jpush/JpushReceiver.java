package udesk.sdk.demo.jpush;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;
import cn.udesk.PreferenceHelper;
import cn.udesk.UdeskSDKManager;
import cn.udesk.config.UdeskConfig;
import udesk.core.UdeskConst;

/**
 * 自定义接收器
 * <p>
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class JpushReceiver extends JPushMessageReceiver {
    private static final String TAG = "udeskpush";

    @Override
    public void onRegister(Context context, String s) {
        super.onRegister(context, s);
        Log.d(TAG, "onRegister==" + s);
        UdeskSDKManager.getInstance().setRegisterId(context, s);
    }

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageArrived(context, notificationMessage);
        Log.d(TAG, "onNotifyMessageArrived==" + notificationMessage.toString());
    }

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage notificationMessage) {
        super.onNotifyMessageOpened(context, notificationMessage);
        Log.d(TAG, "onNotifyMessageOpened==" + notificationMessage.toString());
        String domian = PreferenceHelper.readString(context, "init_base_name", "domain");
        String appkey = PreferenceHelper.readString(context, "init_base_name", "appkey");
        String appid = PreferenceHelper.readString(context, "init_base_name", "appid");
        String sdktoken = PreferenceHelper.readString(context, "init_base_name", "sdktoken");
        Map<String, String> map = new HashMap<>();
        map.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, sdktoken);
        UdeskSDKManager.getInstance().initApiKey(context, domian, appkey, appid);
        UdeskConfig udeskConfig = new UdeskConfig.Builder().setDefaultUserInfo(map).build();
        UdeskSDKManager.getInstance().entryChat(context.getApplicationContext(), udeskConfig, sdktoken);
    }

    @Override
    public void onConnected(Context context, boolean b) {
        super.onConnected(context, b);
        Log.d(TAG, "onConnected==" + b);
    }

}
