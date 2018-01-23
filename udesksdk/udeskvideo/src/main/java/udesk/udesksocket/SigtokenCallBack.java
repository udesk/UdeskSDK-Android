package udesk.udesksocket;

import okhttp3.Response;
import udesk.udesksocket.mode.event.EvtInvite;

/**
 * websoket消息的回家
 */
public interface SigtokenCallBack {


    void response(String string);

    void failure();
}
