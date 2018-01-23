package udesk.udesksocket;

import okhttp3.Response;
import udesk.udesksocket.mode.event.EvtInvite;
import udesk.udesksocket.mode.invite.GetChannelTokenRep;

/**
 * websoket消息的回家
 */
public interface WebsocketCallBack {


    void connected();
    void disconnected(int code, String reason);
    void onFailure(Throwable t, Response response);
    //收到邀请事件
    void evtInvite( EvtInvite evtInvite);
}
