package udesk.sdk.demo.activity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import cn.udesk.activity.UdeskChatActivity;
import udesk.sdk.demo.R;


/**
 * 状态栏通知显示的样例
 */
public class NotificationUtils {
    private NotificationUtils() {
    }

    private static NotificationUtils instance = null;

    public static NotificationUtils getInstance() {

        if (instance == null) {
            instance = new NotificationUtils();
        }
        return instance;
    }

    //获取系统服务
    private NotificationManager mNotificationManager;

    /**
     * @param context
     * @param message 状态栏显示的内容
     */
    public void notifyMsg(Context context, String message) {
        String notify_serivice = Context.NOTIFICATION_SERVICE;
        if (mNotificationManager == null){
            mNotificationManager = (NotificationManager) context.getSystemService(notify_serivice);
        }
        int icon = R.mipmap.ic_launcher;
        CharSequence tickerText = "你有新消息了";
        long when = System.currentTimeMillis();
        CharSequence contentTitle = "UdeskDemo";
        CharSequence contentText = message;
        Intent notificationIntent = null;
        notificationIntent = new Intent(context, UdeskChatActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            builder.setChannelId("udesk_sdk");
            NotificationChannel channel = new NotificationChannel("udesk_sdk","sdkdemo",
                    NotificationManager.IMPORTANCE_DEFAULT);

            channel.setBypassDnd(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            channel.shouldShowLights();//是否会闪光
            channel.enableLights(true);//闪光
            channel.canShowBadge();//桌面launcher消息角标
            channel.enableVibration(true);//是否允许震动
            channel.getAudioAttributes();//获取系统通知响铃声音的配置
            channel.getGroup();//获取通知渠道组
            mNotificationManager.createNotificationChannel(channel);
        }

        Notification noti = builder.setSmallIcon(icon)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setTicker(tickerText)
                .setContentIntent(contentIntent)
                .setWhen(when).build();
        noti.flags = Notification.FLAG_AUTO_CANCEL;
        noti.defaults |= Notification.DEFAULT_VIBRATE;
        noti.defaults |= Notification.DEFAULT_LIGHTS;
        noti.defaults = Notification.DEFAULT_SOUND;
        mNotificationManager.notify(1, noti);
    }


}
