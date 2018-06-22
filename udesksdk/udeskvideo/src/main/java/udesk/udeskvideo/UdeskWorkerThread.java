package udesk.udeskvideo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import udesk.core.UdeskConst;
import udesk.udesksocket.UdeskSocketContants;

public class UdeskWorkerThread extends Thread {


    private final Context mContext;

    private static final int ACTION_WORKER_THREAD_QUIT = 1; // quit this thread

    private static final int ACTION_WORKER_JOIN_CHANNEL = 2;

    private static final int ACTION_WORKER_LEAVE_CHANNEL = 3;

    private static final int ACTION_WORKER_PREVIEW = 4;

    private static final int ACTION_WORKER_REMOTE_PREVIEW = 5;

    private WorkerThreadHandler mWorkerHandler;

    private RtcEngine mRtcEngine;

    private final UdeskRtcEngineEventHandler mEngineEventHandler;

    private static final class WorkerThreadHandler extends Handler {

        private UdeskWorkerThread mWorkerThread;

        WorkerThreadHandler(UdeskWorkerThread thread) {
            this.mWorkerThread = thread;
        }

        public void release() {
            mWorkerThread = null;
        }

        @Override
        public void handleMessage(Message msg) {
            if (this.mWorkerThread == null) {
                return;
            }

            switch (msg.what) {
                case ACTION_WORKER_THREAD_QUIT:
                    mWorkerThread.exit();
                    break;
                case ACTION_WORKER_JOIN_CHANNEL:
                    String[] data = (String[]) msg.obj;
                    mWorkerThread.joinChannel(data[0], msg.arg1, data[1]);
                    break;
                case ACTION_WORKER_LEAVE_CHANNEL:
                    String channel = (String) msg.obj;
                    mWorkerThread.leaveChannel(channel);
                    break;
                case ACTION_WORKER_PREVIEW:
                    Object[] previewData = (Object[]) msg.obj;
                    mWorkerThread.preview((boolean) previewData[0], (SurfaceView) previewData[1], (int) previewData[2]);
                    break;
                case ACTION_WORKER_REMOTE_PREVIEW:
                    Object[] remotepreviewData = (Object[]) msg.obj;
                    mWorkerThread.setupRemoteVideo((SurfaceView) remotepreviewData[0], (int) remotepreviewData[1]);
                    break;
            }
        }
    }

    private boolean mReady;

    public UdeskWorkerThread(Context context) {
        this.mContext = context;
        this.mEngineEventHandler = new UdeskRtcEngineEventHandler();
    }

    public final void waitForReady() {
        while (!mReady) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (UdeskSocketContants.isDebug) {
                Log.i(UdeskSocketContants.Tag, "wait for " + UdeskWorkerThread.class.getSimpleName());
            }
        }
    }

    @Override
    public void run() {
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "start to run");
        }
        Looper.prepare();
        mWorkerHandler = new WorkerThreadHandler(this);
        ensureRtcEngineReadyLock();
        mReady = true;
        // enter thread looper
        Looper.loop();
    }

    /**
     * Agora Native SDK 只支持一个 RtcEngine 实例，每个应用程序仅创建一个 RtcEngine 对象 。
     * RtcEngine 类的所有接口函数，如无特殊说明，都是异步调用，对接口的调用建议在同一个线程进行。所有返回值为 int 型的 API，
     * 如无特殊说明，返回值 0 为调用成功，返回值小于 0 为调用失败。
     *
     * @return
     */
    private RtcEngine ensureRtcEngineReadyLock() {
        if (mRtcEngine == null) {
            //Agora 为应用程序开发者签发的 App ID
            String appId = UdeskConst.agora_app_id;
            if (TextUtils.isEmpty(appId)) {
                throw new RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/");
            }
            try {
                // 创建 RtcEngine 对象
                mRtcEngine = RtcEngine.create(mContext, appId, mEngineEventHandler);
            } catch (Exception e) {
                Log.getStackTraceString(e);
                throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
            }
            //设置频道属性 (setChannelProfile)  同一频道内只能同时设置一种模式。 该方法必须在加入频道前调用和进行设置，进入频道后无法再设置。
//            CHANNEL_PROFILE_COMMUNICATION: 通信模式 (默认)
//            CHANNEL_PROFILE _LIVE_BROADCASTING: 直播模式
//            CHANNEL_PROFILE _GAME: 游戏语音模式
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
            //打开音频 (enableAudio) 默认开启  disableAudio() 关闭音频
            mRtcEngine.enableVideo();
            mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P_4, false);
//            启用说话者音量提示 该方法允许 SDK 定期向应用程序反馈当前谁在说话以及说话者的音量。
            mRtcEngine.enableAudioVolumeIndication(200, 3); // 200 ms
        }
        return mRtcEngine;
    }


    //设置本地预览
    public final void preview(boolean start, SurfaceView view, int uid) {
        if (Thread.currentThread() != this) {
            if (UdeskSocketContants.isDebug) {

                Log.i(UdeskSocketContants.Tag, "preview() - worker thread asynchronously " + start + " " + view + " " + (uid & 0XFFFFFFFFL));
            }
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_PREVIEW;
            envelop.obj = new Object[]{start, view, uid};
            mWorkerHandler.sendMessage(envelop);
            return;
        }

        ensureRtcEngineReadyLock();
        if (start) {
//            setupLocalVideo( VideoCanvas local );
//            该方法设置本地视频显示信息。应用程序通过调用此接口绑定本地视频流的显示视窗(view)，并设置视频显示模式。
//            在应用程序开发中，通常在初始化后调用该方法进行本地视频设置，然后再加入频道。退出频道后，
//            绑定仍然有效，如果需要解除绑定，可以指定空(NULL)View 调用 setupLocalVideo() 。
//            view: 视频显示视窗
//            renderMode: 视频显示模式
//            RENDER_MODE_HIDDEN (1): 如果视频尺寸与显示视窗尺寸不一致，则视频流会按照显示视窗的比例进行周边裁剪或图像拉伸后填满视窗。
//            RENDER_MODE_FIT(2): 如果视频尺寸与显示视窗尺寸不一致，在保持长宽比的前提下，将视频进行缩放后填满视窗。
//            uid: 本地用户 ID，与 joinChannel 方法中的 uid 保持一致
            mRtcEngine.setupLocalVideo(new VideoCanvas(view, VideoCanvas.RENDER_MODE_HIDDEN, uid));

//            该方法用于启动本地视频预览。
//            在开启预览前，必须先调用 setupLocalVideo 设置预览窗口及属性，且必须调用 enableVideo() 开启视频功能。
//            如果在调用 joinChannel() 进入频道之前调用了 startPreview() 启动本地视频预览，在调用 leaveChannel() 退出频道之后本地预览仍然处于启动状态，
//            如需要关闭本地预览，需要调用 stopPreview()。
            mRtcEngine.startPreview();
        } else {
            mRtcEngine.stopPreview();
        }
    }


    //如果已在通话中，用户必须调用 leaveChannel() 退出当前通话，才能进入下一个频道
    public final void joinChannel(final String channel, int uid, String channelkey) {
        if (Thread.currentThread() != this) {
            if (UdeskSocketContants.isDebug) {
                Log.i(UdeskSocketContants.Tag, "joinChannel() - worker thread asynchronously " + channel + " " + uid);
            }
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_JOIN_CHANNEL;
            envelop.obj = new String[]{channel, channelkey};
            envelop.arg1 = uid;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        ensureRtcEngineReadyLock();
//      optionalInfo  (非必选项) 开发者需加入的任何附加信息。一般可设置为空字符串，或频道相关信息。该信息不会传递给频道内的其他用户。
//        用户ID，32位无符号整数。建议设置范围：1到(2^32-1)，并保证唯一性。如果不指定（即设为0），
//        SDK 会自动分配一个，并在 onJoinChannelSuccess回调方法中返回，App层必须记住该返回值并维护，SDK不对该返回值进行维护。
//        uid 在 SDK 内部用 32 位无符号整数表示，由于 Java 不支持无符号整数，uid 被当成 32 位有符号整数处理，对于过大的整数，
//        Java 会表示为负数，如有需要可以用(uid&0xffffffffL)转换成 64 位整数。
        mRtcEngine.joinChannel(channelkey, channel, "OpenVCall", uid);


        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "joinChannel " + channel + ";uid " + uid + "channelkey:" + channelkey);
        }
    }

    public void joinChannel() {
        mRtcEngine.joinChannel(null, "demoChannel1", "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
    }

    public void switchCamear() {
        if (mRtcEngine != null) {
            mRtcEngine.switchCamera();
        }
    }

    /**
     * 设置远程的视频显示属性
     *
     * @param surfaceView
     * @param uid
     */
    public void setupRemoteVideo(SurfaceView surfaceView, int uid) {
        if (Thread.currentThread() != this) {

            Message envelop = new Message();
            envelop.what = ACTION_WORKER_REMOTE_PREVIEW;
            envelop.obj = new Object[]{surfaceView, uid};
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        ensureRtcEngineReadyLock();
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_ADAPTIVE, uid));
    }


    public final void leaveChannel(String channel) {
        if (Thread.currentThread() != this) {
            if (UdeskSocketContants.isDebug) {
                Log.i(UdeskSocketContants.Tag, "leaveChannel() - worker thread asynchronously " + channel);
            }
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_LEAVE_CHANNEL;
            envelop.obj = channel;
            mWorkerHandler.sendMessage(envelop);
            return;
        }

        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            mRtcEngine.enableVideo();
        }

        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "leaveChannel " + channel);
        }
    }

    public RtcEngine getRtcEngine() {
        return mRtcEngine;
    }

    /**
     * call this method to exit
     * should ONLY call this method when this thread is running
     */
    public final void exit() {
        if (Thread.currentThread() != this) {
            if (UdeskSocketContants.isDebug) {
                Log.i("udeskcall", "exit() - exit app thread asynchronously");
            }
            mWorkerHandler.sendEmptyMessage(ACTION_WORKER_THREAD_QUIT);
            return;
        }

        mReady = false;
        if (UdeskSocketContants.isDebug) {
            Log.i("udeskcall", "exit() > start");
        }

        // exit thread looper
        Looper.myLooper().quit();

        mWorkerHandler.release();
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            mRtcEngine.destroy();
            mRtcEngine = null;
        }

        if (UdeskSocketContants.isDebug) {
            Log.i("udeskcall", "exit() > end");
        }
    }


    public WorkerThreadHandler getmWorkerHandler() {
        return mWorkerHandler;
    }


}
