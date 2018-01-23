package udesk.udesksocket;

/**
 * author : ${揭军平}
 * time   : 2017/11/21
 * desc   :
 * version: 1.0
 */

public class UdeskSocketContants {

    public static boolean isDebug = true;
    public static final String Tag = "UdeskSdk";

//    public static String getSignToenUrl = "http://vcall.udeskb3.com/v1/token";
//    public static String getSignToenUrl = "http://app.vcall.udeskb3.com/vcall/v1/token";
    public static String Subdomain = "";


    public static String Ver = "1.0";
    public static String ToResId = "";


    public static class MsgType {

        public static String Req = "REQ"; //请求消息
        public static String Rep = "REP"; //响应消息
        public static String Ntf = "NTF"; // 通知消息：NTF
        public static String Evt = "EVT"; // 事件消息：EVT
        public static String Ack = "ACK"; // 事件响应
    }

    public static class State {

        public static String idle = "idle";
        public static String busy = "busy";
        public static String outgoing_call = "outgoing_call";
        public static String incoming_call = "incoming_call";
        public static String talking = "talking";
    }

    //
    public static class WebSocketStatus {

        public static String onFailure = "onFailure";
        public static String connected = "connected";
    }


    public static class CallType {

        public static String video = "video";
        public static String audio = "audio";

    }

    public static final String Udesk_NOTIFICATION = "com.udesk.video.event";
    public static final String Udesk_STARTVIDEO = "com.udesk.startvideo.event";
    public static String VideoEvent = "VideoEvent";
    public static String VideoIsInvite = "VideoIsInvite";
    public static String VideoChannelId = "VideoChannelId";
    public static String VideoMessage = "VideoMessage";
    public static String Receive_WebsocketConnect = "websocket_connect";
    public static String Receive_BusseniessId = "bizSessionId";
    public static String Receive_AgentJid = "agentJid";
    public static String Receive_CustomerJid = "CustomerJid";
    public static String Receive_AgentName = "agentName";
    public static String Receive_VcAppId = "vc_app_id";
    public static String Receive_Agora_app_id = "agora_app_id";
    public static String Receive_SocketServer_url = "server_url";
    public static String Receiver_Vcall_token_url = "vcall_token_url";
    public static String Receive_Subdomain = "subdomain";
    public static class ReceiveType {

        //发起视频
        public static String StartMedio = "startmedia";
        //对方忙
        public static String Busy = "busy";
        //对方拒绝
        public static String Reject = "reject";
        //超时
        public static String Timeout = "timeout";
        //发起方取消
        public static String Cancle = "cancle";
        //通话结束
        public static String Over = "over";
    }


    public static String IsInivte = "isinivte";
    public static String ChannelName = "udesk_chanel_name";


    public static String UID = "uid";
    public static String REASON = "reason";


    public static String IMBusseniessId = "";
    public static String IMAgentJid = "";
    public static String IMCustomerJid = "";
    public static String IMAgentName = "";
    public static String vc_app_id = "";


    public static String CalledReject = "001";
    public static String CallerHangup = "002";
    public static String TimeoutCancel = "003";


    public static String CalChannelTokenError = "000100"; // 计算 channelToken 错误
    public static String ClientNotLoginError = "000101";// 客户端为登录错误
    public static String ClientTargetStateWrong = "000102"; // 设置坐席目标状态错误
    public static String ClientNotInIdleStateError = "000103"; // 目标坐席非空闲
    public static String ACKTimeOut = "000104"; // ACK 超时（000104）

    public static String agora_app_id = "";
    public static String server_url = "";
    public static String signToenUrl = "";


}
