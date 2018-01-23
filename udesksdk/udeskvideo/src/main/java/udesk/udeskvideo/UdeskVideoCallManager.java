package udesk.udeskvideo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.SurfaceView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Response;
import udesk.udesksocket.SigtokenCallBack;
import udesk.udesksocket.UdeskSocketContants;
import udesk.udesksocket.MessageManager;
import udesk.udesksocket.Util;
import udesk.udesksocket.WebsocketCallBack;
import udesk.udesksocket.client.UdeskWebsocket;
import udesk.udesksocket.mode.event.EvtInvite;
import udesk.udeskvideo.floatview.FloatActionController;
import udesk.udeskvideo.mode.EventFinish;

/**
 * author : ${揭军平}
 * time   : 2017/11/14
 * desc   :
 * version: 1.0
 */

public class UdeskVideoCallManager {

    private static volatile UdeskVideoCallManager instance = null;

    private UdeskWorkerThread mWorkerThread;

    private Context mContext;

    private String customerJid = "";

    //    间隔时间分别为 1s 5s 5s 10s 10s，
    private volatile int reconenctCount = 1;

    private UdeskVideoCallManager() {
    }


    private SurfaceView remoteVideoView;


    public static UdeskVideoCallManager getInstance() {

        if (instance == null) {
            synchronized (UdeskVideoCallManager.class) {
                if (instance == null) {
                    instance = new UdeskVideoCallManager();
                }
            }
        }
        return instance;

    }

    //启动线程
    public synchronized void initWorkerThread(Context context) {
        mContext = context;
        if (mWorkerThread == null) {
            mWorkerThread = new UdeskWorkerThread(context);
            mWorkerThread.start();

            mWorkerThread.waitForReady();
        }
    }

    public synchronized UdeskWorkerThread getWorkerThread() {
        if (mWorkerThread == null) {
            initWorkerThread(mContext);
        }
        return mWorkerThread;
    }

    //退出线程
    public synchronized void deInitWorkerThread() {
        if (mWorkerThread != null) {
            mWorkerThread.exit();
            try {
                mWorkerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mWorkerThread = null;
        }
    }

    public void startVideo(Context context, String channeName, boolean isInivte) {
        Intent intent = new Intent();
        intent.setClass(context, UdeskVideoActivity.class);
        intent.putExtra(UdeskSocketContants.IsInivte, isInivte);
        intent.putExtra(UdeskSocketContants.ChannelName, channeName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
    }

    public synchronized void reConnectWebSocket() {

        Log.i(UdeskSocketContants.Tag,"reConnectWebSocket reconnectcount =" + reconenctCount);
        if (reconenctCount > 5) {
//            不在从连击,如果声网没断开，则断开
            EventBus.getDefault().post(new EventFinish());
            FloatActionController.getInstance().stopMonkServer(mContext);
            reconenctCount = 1;
            return;
        }
        long laterTime = 1000;
        switch (reconenctCount) {
            case 1:
                laterTime = 1000;
                break;
            case 2:
                laterTime = 5000;
                break;
            case 3:
                laterTime = 5000;
                break;
            case 4:
                laterTime = 10000;
                break;
            case 5:
                laterTime = 10000;
                break;
        }
        reconenctCount++;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                connectWebsocket(mContext);
            }
        }, laterTime);

    }

    public synchronized void connectWebsocket(final Context context) {
        mContext = context;

        if (UdeskWebsocket.getUdeskWebSocket().getWebSocket() != null && customerJid.equals(UdeskSocketContants.IMCustomerJid)) {
            return;
        }
        UdeskWebsocket.getUdeskWebSocket().close();
        Util.getSignToken(UdeskSocketContants.Subdomain, UdeskSocketContants.vc_app_id, new SigtokenCallBack() {
            @Override
            public void response(String string) {
                Log.i(UdeskSocketContants.Tag, "getSignToken =" + string);
                if (string.contains("sig_token")) {
                    try {
                        JSONObject object = new JSONObject(string);
                        final String sig_token = object.optString("sig_token");
                        MessageManager.getMessageManager().connectWebSocket(new WebsocketCallBack() {
                            @Override
                            public void connected() {
                                MessageManager.getMessageManager().sendPing();
                                Log.i(UdeskSocketContants.Tag, "Websocket connected");
                                customerJid = UdeskSocketContants.IMCustomerJid;
                                MessageManager.getMessageManager().login(UdeskSocketContants.vc_app_id, UdeskSocketContants.IMCustomerJid, sig_token, UdeskSocketContants.State.idle, Util.getUniqueId(context));
                            }

                            @Override
                            public void disconnected(int code, final String reason) {
                                Log.i(UdeskSocketContants.Tag, "Websocket disconnected");

                            }

                            @Override
                            public void onFailure(Throwable t, Response response) {
                                Log.i(UdeskSocketContants.Tag, "Websocket onFailure");
                                reConnectWebSocket();
                            }

                            @Override
                            public void evtInvite(EvtInvite evtInvite) {
                                if (mContext != null) {
                                    startVideo(mContext, Util.objectToString(evtInvite.getChannel_id()), false);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void failure() {
                if (reconenctCount>5){
                    EventBus.getDefault().post(new EventFinish());
                    FloatActionController.getInstance().stopMonkServer(mContext);
                }else{
                    reConnectWebSocket();
                }
            }
        });

    }

    public SurfaceView getRemoteVideoView() {
        return remoteVideoView;
    }

    public void setRemoteVideoView(SurfaceView remoteVideoView) {
        this.remoteVideoView = remoteVideoView;
    }

    public void setReconenctCount(int reconenctCount) {
        this.reconenctCount = reconenctCount;
    }
}
