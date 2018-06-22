package udesk.sdk.demo.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

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

    /**
     * @param context
     * @param message 状态栏显示的内容
     */
    public void notifyMsg(Context context, String message) {
        String notify_serivice = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(notify_serivice);
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
