package udesk.udesksocket;

import okhttp3.Response;
import okio.ByteString;

/**
 * author : ${揭军平}
 * time   : 2017/11/21
 * desc   :
 * version: 1.0
 */

public interface OnMessageListener {

    void onOpen();
    void onMessage(String msg);
    void onMessage(ByteString bytes);
    void onFailure(Throwable t, Response response);
    void onClosed(int code, String reason);
    void onClosing(int code, String reason);
}
