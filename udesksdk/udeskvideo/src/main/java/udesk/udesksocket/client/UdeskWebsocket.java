package udesk.udesksocket.client;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import udesk.udesksocket.OnMessageListener;
import udesk.udesksocket.UdeskSocketContants;


/**
 * author : ${揭军平}
 * time   : 2017/11/21
 * desc   :
 * version: 1.0
 */

public class UdeskWebsocket extends WebSocketListener {



    private static volatile UdeskWebsocket mUdeskWebsocket = null;
    private static OkHttpClient mOkHttpClient;

    private WebSocket mWebSocket;

    private OnMessageListener messageListener;


    private UdeskWebsocket() {
    }


    public static UdeskWebsocket getUdeskWebSocket() {

        if (mUdeskWebsocket == null) {
            synchronized (UdeskWebsocket.class) {
                if (mUdeskWebsocket == null) {
                    mOkHttpClient = new OkHttpClient.Builder().build();
                    mUdeskWebsocket = new UdeskWebsocket();
                }
            }
        }

        return mUdeskWebsocket;
    }

    public void setMessagerListener(OnMessageListener listener) {

        this.messageListener = listener;
    }

    public synchronized void connect() {
        try {
            Request request = new Request.Builder()
                    .url(UdeskSocketContants.server_url)
                    .build();
            mOkHttpClient.newWebSocket(request, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        mWebSocket = webSocket;
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onOpen ");
        }
        if (messageListener != null) {
            messageListener.onOpen();
        }


        //链接成功

    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onMessage code=" + "text=" + text);
        }
        if (text.equals("pong")) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendMessage("ping");
                }
            }, 45000);
            return;
        }


        if (messageListener != null) {
            messageListener.onMessage(text);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
        if (messageListener != null) {
            messageListener.onMessage(bytes);
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onClosing code=" + code + "reason=" + reason);
        }
        if (messageListener != null) {
            messageListener.onClosing(code, reason);
        }
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onClosed code=" + code + "reason=" + reason);
        }
        //断开链接
        if (messageListener != null) {
            messageListener.onClosed(code, reason);
        }
        mWebSocket = null;
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onFailure");
        }
        resetWebSocket();
        //链接失败
        if (messageListener != null) {
            messageListener.onFailure(t, response);
        }
    }

    public synchronized void sendMessage(String send) {

        if (mWebSocket != null) {
            Log.i(UdeskSocketContants.Tag, "sendMessage" + send);
            mWebSocket.send(send);
        }

    }

    public synchronized void close() {
        if (mWebSocket != null) {
            mWebSocket.close(1000, "");
        }
    }

    private synchronized void resetWebSocket() {
        if (mWebSocket != null) {
            mWebSocket = null;
        }

    }

    public WebSocket getWebSocket() {
        return mWebSocket;
    }
}
