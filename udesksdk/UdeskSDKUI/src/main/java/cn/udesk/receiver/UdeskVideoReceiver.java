package cn.udesk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import udesk.core.UdeskCoreConst;
import udesk.core.event.InvokeEventContainer;



public class UdeskVideoReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String event = intent.getStringExtra(UdeskCoreConst.VideoEvent);
            String channeid = intent.getStringExtra(UdeskCoreConst.VideoChannelId);
            String message = intent.getStringExtra(UdeskCoreConst.VideoMessage);
            Boolean isinvite = intent.getBooleanExtra(UdeskCoreConst.VideoIsInvite, true);
            Log.i("UdeskSdk", event);
            InvokeEventContainer.getInstance().event_OnVideoEventReceived.invoke(event, channeid, message,isinvite);

        }
    }


}
