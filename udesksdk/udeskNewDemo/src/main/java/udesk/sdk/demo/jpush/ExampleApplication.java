package udesk.sdk.demo.jpush;

import android.app.Application;
import android.util.Log;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import cn.jpush.android.api.JPushInterface;
import cn.udesk.messagemanager.UdeskMessageManager;
import cn.udesk.model.MsgNotice;
import udesk.sdk.demo.activity.NotificationUtils;

/**
 * For developer startup JPush SDK
 * 
 * 一般建议在自定义 Application 类里初始化。也可以在主 Activity 里。
 */
public class ExampleApplication extends Application {
    private static final String TAG = "JPush";



    @Override
    public void onCreate() {    	     
    	 Log.d(TAG, "[ExampleApplication] onCreate");
         super.onCreate();
         JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
         JPushInterface.init(this);     		// 初始化 JPush



//        /**
//         * 注册接收消息提醒事件
//         */
//        UdeskMessageManager.getInstance().event_OnNewMsgNotice.bind(this, "OnNewMsgNotice");
    }


//    public void OnNewMsgNotice(MsgNotice msgNotice) {
//        if (msgNotice != null) {
//            Log.i("xxx","UdeskCaseActivity 中收到msgNotice");
//            NotificationUtils.getInstance().notifyMsg(this.getApplicationContext(), msgNotice.getContent());
//        }
//
//    }

}
