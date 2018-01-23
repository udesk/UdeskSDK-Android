package udesk.udeskvideo.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.util.UUID;

import udesk.udesksocket.UdeskSocketContants;
import udesk.udeskvideo.UdeskVideoCallManager;


public class UdeskSDKReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {

            if (intent.getAction().equals(UdeskSocketContants.Udesk_STARTVIDEO)) {
                UdeskVideoCallManager.getInstance().startVideo(context, UUID.randomUUID().toString().replaceAll("-", ""), true);
                return;
            }

            String stirng = intent.getStringExtra(UdeskSocketContants.Receive_WebsocketConnect);
            String busseniessId = intent.getStringExtra(UdeskSocketContants.Receive_BusseniessId);
            String agentJid = intent.getStringExtra(UdeskSocketContants.Receive_AgentJid);
            String customerJid = intent.getStringExtra(UdeskSocketContants.Receive_CustomerJid);
            String agentName = intent.getStringExtra(UdeskSocketContants.Receive_AgentName);
            String vcAppId = intent.getStringExtra(UdeskSocketContants.Receive_VcAppId);
            String agora_app_id = intent.getStringExtra(UdeskSocketContants.Receive_Agora_app_id);
            String socketServer_url = intent.getStringExtra(UdeskSocketContants.Receive_SocketServer_url);
            String vcall_token_url = intent.getStringExtra(UdeskSocketContants.Receiver_Vcall_token_url);
            String domain = intent.getStringExtra(UdeskSocketContants.Receive_Subdomain);


            if (!TextUtils.isEmpty(busseniessId)) {
                UdeskSocketContants.IMBusseniessId = busseniessId;
                if (UdeskSocketContants.isDebug) {
                    Log.i(UdeskSocketContants.Tag, "Receive_BusseniessId = " + busseniessId);
                }
            }
            if (!TextUtils.isEmpty(agentJid)) {
                UdeskSocketContants.IMAgentJid = agentJid;
                if (UdeskSocketContants.isDebug) {
                    Log.i(UdeskSocketContants.Tag, "agentJid = " + agentJid);
                }
            }

            if (!TextUtils.isEmpty(customerJid)) {
                UdeskSocketContants.IMCustomerJid = customerJid;
                if (UdeskSocketContants.isDebug) {
                    Log.i(UdeskSocketContants.Tag, "customerJid = " + customerJid);
                }
            }

            if (!TextUtils.isEmpty(agentName)) {
                UdeskSocketContants.IMAgentName = agentName;
                if (UdeskSocketContants.isDebug) {
                    Log.i(UdeskSocketContants.Tag, "agentName = " + agentName);
                }
            }

            if (!TextUtils.isEmpty(vcAppId)) {
                UdeskSocketContants.vc_app_id = vcAppId;
                if (UdeskSocketContants.isDebug) {
                    Log.i(UdeskSocketContants.Tag, "vcAppId = " + vcAppId);
                }
            }

            if (!TextUtils.isEmpty(agora_app_id)) {
                UdeskSocketContants.agora_app_id = agora_app_id;
                if (UdeskSocketContants.isDebug) {
                    Log.i(UdeskSocketContants.Tag, "agora_app_id = " + agora_app_id);
                }
            }

            if (!TextUtils.isEmpty(socketServer_url)) {
                UdeskSocketContants.server_url = socketServer_url;
                if (UdeskSocketContants.isDebug) {
                    Log.i(UdeskSocketContants.Tag, "socketServer_url = " + socketServer_url);
                }
            }
            if (!TextUtils.isEmpty(vcall_token_url)) {
                UdeskSocketContants.signToenUrl = vcall_token_url;
                if (UdeskSocketContants.isDebug) {
                    Log.i(UdeskSocketContants.Tag, "vcall_token_url = " + vcall_token_url);
                }
            }

            if (!TextUtils.isEmpty(domain)) {
                UdeskSocketContants.Subdomain = domain;
                if (UdeskSocketContants.isDebug) {
                    Log.i(UdeskSocketContants.Tag, "domain = " + domain);
                }
            }

            if (stirng != null && stirng.equals(UdeskSocketContants.Receive_WebsocketConnect)) {
                UdeskVideoCallManager.getInstance().setReconenctCount(1);
                UdeskVideoCallManager.getInstance().connectWebsocket(context);
            }

        }
    }


}
