package udesk.udeskvideo;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import io.agora.rtc.IRtcEngineEventHandler;
import udesk.udesksocket.UdeskSocketContants;
import udesk.udeskvideo.mode.EventAgoraMode;

/**
 * author : ${揭军平}
 * time   : 2017/11/28
 * desc   :
 * version: 1.0
 */

public class UdeskRtcEngineEventHandler extends IRtcEngineEventHandler {


    /**
     * 远端视频接收解码回调  收到第一帧远程视频流并解码成功时，触发此调用。应用程序可以在此回调中设置该用户的视图
     *
     * @param uid
     * @param width
     * @param height
     * @param elapsed
     */
    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        super.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onFirstRemoteVideoDecoded uid=" + uid + ";elapased = " + elapsed);
        }
//        if (UdeskVideoCallManager.getInstance().getEventHandlerCallBack() != null) {
//            UdeskVideoCallManager.getInstance().getEventHandlerCallBack().onFirstRemoteVideoDecoded(uid, width, height, elapsed);
//        }
        Map<String,Object> maps = new HashMap<>();
        maps.put("uid",uid);
        EventBus.getDefault().post(new EventAgoraMode("onFirstRemoteVideoDecoded",maps));
    }

    /**
     * 其他用户加入当前频道回调(onUserJoined)  提示有用户加入了频道。如果该客户端加入频道时已经有人在频道中，SDK 也会向应用程序上报这些已在频道中的用户。
     *
     * @param uid     用户 ID
     * @param elapsed 从调用 joinChannel() 到触发该回调的延迟(毫秒)
     */
    @Override
    public void onUserJoined(int uid, int elapsed) {
        super.onUserJoined(uid, elapsed);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onUserJoined uid=" + uid + ";elapsed = " + elapsed);
        }
//        if (UdeskVideoCallManager.getInstance().getEventHandlerCallBack() != null){
//            UdeskVideoCallManager.getInstance().getEventHandlerCallBack().onUserJoined(uid,elapsed);
//        }
        Map<String,Object> maps = new HashMap<>();
        maps.put("uid",uid);
        EventBus.getDefault().post(new EventAgoraMode("onUserJoined",maps));
    }


    /**
     * 其他用户离开当前频道回调
     *
     * @param uid
     * @param reason User_OFFLINE_QUIT(0): 用户主动离开 USER_OFFLINE_DROPPED(1): 因过长时间收不到对方数据包，超时掉线 由于 SDK 使用的是不可靠通道，也有可能对方主动离开而本地未收到离开消息而误判为超时掉线
     *               提示有用户离开了频道（或掉线）。SDK 判断用户离开频道（或掉线）的依据是超时: 在一定时间内（15 秒）没有收到对方的任何数据包，判定为对方掉线。在网络较差的情况下，可能会有误报。
     */
    @Override
    public void onUserOffline(int uid, int reason) {
        super.onUserOffline(uid, reason);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onUserOffline uid=" + uid + ";reason = " + reason);
        }
//        if (UdeskVideoCallManager.getInstance().getEventHandlerCallBack() != null){
//            UdeskVideoCallManager.getInstance().getEventHandlerCallBack().onUserOffline(uid,reason);
//        }

        Map<String,Object> maps = new HashMap<>();
        maps.put("uid",uid);
        maps.put("reason",reason);
        EventBus.getDefault().post(new EventAgoraMode("onUserOffline",maps));
    }

    /**
     * 离开频道回调(onLeaveChannel)
     *
     * @param stats totalDuration: 通话时长(秒)，累计值
     *              txBytes: 发送字节数(bytes), 累计值
     *              rxBytes: 接收字节数(bytes)，累计值
     *              txKBitRate: 发送码率(kbps)，瞬时值
     *              rxKBitRate: 接受码率(kbps)，瞬时值
     *              lastmileQuality: 客户端接入网络质量
     *              cpuTotalQuality: 当前系统的 CPU 使用率(%)
     *              cpuAppQuality: 当前应用程序的 CPU 使用率(%)
     */
    @Override
    public void onLeaveChannel(RtcStats stats) {
        super.onLeaveChannel(stats);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onLeaveChannel ");
        }
//        if (UdeskVideoCallManager.getInstance().getEventHandlerCallBack() != null){
//            UdeskVideoCallManager.getInstance().getEventHandlerCallBack().onLeaveChannel(stats);
//        }
        Map<String,Object> maps = new HashMap<>();
        maps.put("stats",stats);
        EventBus.getDefault().post(new EventAgoraMode("onLeaveChannel",maps));
    }


    /**
     * 加入频道回调  表示客户端已经登入服务器，且分配了频道 ID 和用户 ID
     *
     * @param channel 频道名
     * @param uid     用户 ID 。如果 joinChannel() 中指定了 uid，则此处返回该 ID；否则使用 Agora 服务器自动分配的 ID
     * @param elapsed 从 joinChannel() 开始到该事件产生的延迟（毫秒)
     */
    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        super.onJoinChannelSuccess(channel, uid, elapsed);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onJoinChannelSuccess channel=" + channel + ";uid=" + uid + ";elapased = " + elapsed);
        }
        Map<String,Object> maps = new HashMap<>();
        maps.put("uid",uid);
        maps.put("channel",channel);
        EventBus.getDefault().post(new EventAgoraMode("onJoinChannelSuccess",maps));
    }

    /**
     * 重新加入频道回调  有时候由于网络原因，客户端可能会和服务器失去连接，SDK 会进行自动重连，自动重连成功后触发此回调方法
     *
     * @param channel
     * @param uid
     * @param elapsed
     */
    @Override
    public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
        super.onRejoinChannelSuccess(channel, uid, elapsed);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onRejoinChannelSuccess channel=" + channel + ";uid=" + uid + ";elapased = " + elapsed);
        }
    }

    /**
     * 其他用户已停发/已重发视频流回调
     *
     * @param uid
     * @param muted True: 该用户已暂停发送视频流 False: 该用户已恢复发送视频流
     */
    @Override
    public void onUserMuteVideo(int uid, boolean muted) {
        super.onUserMuteVideo(uid, muted);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onUserMuteVideo uid=" + uid + ";muted = " + muted);
        }
    }





    /**
     * 其他用户启用/关闭视频  提示有其他用户启用/关闭了视频功能。关闭视频功能是指该用户只能进行语音通话，不能显示、发送自己的视频，也不能接收、显示别人的视频。
     *
     * @param uid
     * @param enabled True: 该用户已启用视频功能
     *                False: 该用户已关闭视频功能
     */
    @Override
    public void onUserEnableVideo(int uid, boolean enabled) {
        super.onUserEnableVideo(uid, enabled);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onUserEnableVideo uid =  " + uid + ";enabled =" + enabled);
        }
    }

    /**
     * 发生警告回调
     *
     * @param warn 通常情况下，SDK 上报的警告信息应用程序可以忽略，SDK 会自动恢复。 例如和服务器失去连接时，SDK 可能会上报 ERR_OPEN_CHANNEL_TIMEOUT 警告，同时自动尝试重连
     */
    @Override
    public void onWarning(int warn) {
        super.onWarning(warn);
    }

    /**
     * 发生错误回调   通常情况下，SDK 上报的错误意味着 SDK 无法自动恢复，需要 APP 干预或提示用户。
     *
     * @param err ERR_INVALID_VENDOR_KEY(101): 无效的 App ID
     *            ERR_INVALID_CHANNEL_NAME(102): 无效的频道名
     *            ERR_LOOKUP_CHANNEL_REJECTED(105): 查找频道失败，意味着服务器主动拒绝了请求
     *            ERR_OPEN_CHANNEL_REJECTED(107): 加入频道失败，意味着媒体服务器主动拒绝了请求
     *            ERR_LOAD_MEDIA_ENGINE(1001): 加载媒体引擎失败
     *            ERR_START_CALL（1002）: 打开本地音视频设备、启动通话失败
     *            ERR_START_CAMERA(1003): 打开本地摄像头失败
     */
    @Override
    public void onError(int err) {
        super.onError(err);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onError err=" + err);
        }
        Map<String,Object> maps = new HashMap<>();
        maps.put("err",err);
        EventBus.getDefault().post(new EventAgoraMode("onError",maps));
    }

    /**
     * 摄像头启用回调  提示已成功打开摄像头，可以开始捕获视频。如果摄像头打开失败，可在 onError() 中处理相应错误
     */
    @Override
    public void onCameraReady() {
        super.onCameraReady();
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onCameraReady");
        }
    }

    /**
     * 视频功能停止回调   提示视频功能已停止。应用程序如需在停止视频后对 view 做其他处理（例如显示其他画面），可以在这个回调中进行
     */
    @Override
    public void onVideoStopped() {
        super.onVideoStopped();
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onVideoStopped");
        }
    }

    /**
     * 音频质量回调  在通话中，该回调方法每两秒触发一次，报告当前通话的音频质量(嘴到耳)。默认启用
     *
     * @param uid
     * @param quality
     * @param delay
     * @param lost
     */
    @Override
    public void onAudioQuality(int uid, int quality, short delay, short lost) {
        super.onAudioQuality(uid, quality, delay, lost);
    }


    /**
     * Rtc Engine统计数据回调   该回调定期上报 Rtc Engine 的运行时的状态，每两秒触发一次。
     *
     * @param stats
     */
    @Override
    public void onRtcStats(RtcStats stats) {
        super.onRtcStats(stats);
    }

    /**
     * 说话声音音量提示回调  提示谁在说话及其音量。默认禁用。可以通过 enableAudioVolumeIndication 方法设置。
     *
     * @param speakers    说话者(数组)。每个 speaker() 包括:uid: 说话者的用户 ID volume: 说话者的音量(0~255)
     * @param totalVolume (混音后的)总音量 (0~255)
     */
    @Override
    public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
        super.onAudioVolumeIndication(speakers, totalVolume);

    }

    /**
     * 频道内网络质量报告回调  该回调定期触发，向 APP 报告频道内用户当前的上行、下行网络质量
     *
     * @param uid
     * @param txQuality 该用户的上行网络质量:
     * @param rxQuality 该用户的下行网络质量:
     */
    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        super.onNetworkQuality(uid, txQuality, rxQuality);
//        if (UdeskVideoConstant.isDebug) {
//            Log.i(UdeskVideoConstant.Tag, "onNetworkQuality uid = " + uid + ";uid= " + uid + ";txQuality = " + txQuality + ";rxQuality=" + rxQuality);
//        }
    }

    /**
     * 网络质量报告回调  报告本地用户的网络质量，该回调函数每 2 秒触发一次。
     *
     * @param quality QUALITY_UNKNOWN( = 0)
     *                QUALITY_EXCELLENT(1)
     *                QUALITY_GOOD(2)
     *                QUALITY_POOR(3)
     *                QUALITY_BAD(4)
     *                QUALITY_VBAD(5)
     *                QUALITY_DOWN(6)
     */
    @Override
    public void onLastmileQuality(int quality) {
        super.onLastmileQuality(quality);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onLastmileQuality quality=" + quality);
        }
    }


    /**
     * 用户静音回调  提示有其他用户将他的音频流静音/取消静音。
     *
     * @param uid
     * @param muted True: 该用户已静音音频 False: 该用户已取消音频静音
     */
    @Override
    public void onUserMuteAudio(int uid, boolean muted) {
        super.onUserMuteAudio(uid, muted);
    }


    @Override
    public void onRemoteVideoStats(RemoteVideoStats stats) {
        super.onRemoteVideoStats(stats);
    }

    @Override
    public void onLocalVideoStats(LocalVideoStats stats) {
        super.onLocalVideoStats(stats);
    }

    /**
     * 远端视频显示回调  第一帧远程视频显示在视图上时，触发此调用。应用程序可在此调用中获知出图时间（elapsed）
     *
     * @param uid
     * @param width
     * @param height
     * @param elapsed
     */
    @Override
    public void onFirstRemoteVideoFrame(int uid, int width, int height, int elapsed) {
        super.onFirstRemoteVideoFrame(uid, width, height, elapsed);
    }

    /**
     * 本地视频显示回调  提示第一帧本地视频画面已经显示在屏幕上
     *
     * @param width
     * @param height
     * @param elapsed
     */
    @Override
    public void onFirstLocalVideoFrame(int width, int height, int elapsed) {
        super.onFirstLocalVideoFrame(width, height, elapsed);
    }

    /**
     * 连接丢失回调 该回调方法表示 SDK 和服务器失去了网络连接，并且尝试自动重连一段时间（默认 10 秒）后仍未连上。该回调触发后，SDK 仍然会尝试重连，重连成功后会触发 onRejoinChannelSuccess 回调
     */
    @Override
    public void onConnectionLost() {
        super.onConnectionLost();
    }

    /**
     * 连接中断回调  该回调方法表示 SDK 和服务器失去了网络连接。
     * 与 onConnectionLost 回调的区别是: onConnectionInterrupted 回调在 SDK 刚失去和服务器连接时触发，onConnectionLost 在失去连接且尝试自动重连失败后才触发。
     * 失去连接后，除非 APP 主动调用 leaveChannel()，不然 SDK 会一直自动重连
     */
    @Override
    public void onConnectionInterrupted() {
        super.onConnectionInterrupted();
    }


    /**
     * 接收到对方数据流消息的回调  该回调表示已在 5 秒内按照顺序收到了对方发送的数据包。
     *
     * @param uid
     * @param streamId
     * @param data
     */
    @Override
    public void onStreamMessage(int uid, int streamId, byte[] data) {
        super.onStreamMessage(uid, streamId, data);
    }

    /**
     * 接收对方数据流消息错误的回调(
     *
     * @param uid
     * @param streamId
     * @param error
     * @param missed
     * @param cached
     */
    @Override
    public void onStreamMessageError(int uid, int streamId, int error, int missed, int cached) {
        super.onStreamMessageError(uid, streamId, error, missed, cached);
    }

    @Override
    public void onMediaEngineLoadSuccess() {
        super.onMediaEngineLoadSuccess();
    }

    @Override
    public void onMediaEngineStartCallSuccess() {
        super.onMediaEngineStartCallSuccess();
    }

    /**
     * 伴奏播放已结束回调
     */
    @Override
    public void onAudioMixingFinished() {
        super.onAudioMixingFinished();
    }

    /**
     * Channel Key 已过期回调
     * <p>
     * 在调用 joinChannel() 时如果指定了 Channel Key，由于 Channel Key 具有一定的时效，在通话过程中 SDK 可能由于网络原因和服务器失去连接，
     * 重连时可能需要新的 Channel Key。该回调通知 APP 需要生成新的 Channel Key，
     * 并需调用 renewChannelKey() 为SDK指定新的 Channel Key。
     * 之前的处理方法是在 onError() 回调报告 ERR_CHANNEL_KEY_EXPIRED(109)、ERR_INVALID_CHANNEL_KEY(110) 时，
     * APP 需要生成新的 Key。在新版本中，原来的处理仍然有效，但建议把相关逻辑放进该回调里
     */
    @Override
    public void onRequestChannelKey() {
        super.onRequestChannelKey();
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onRequestChannelKey");
        }
    }

    /**
     * 语音路由已变更回调
     * 当调用 setEnableSpeakerphone 成功时， SDK 会通过该回调通知 App 语音路由状态已发生变化。该回调返回当前的语音路由已切换至听筒，外放(扬声器)，耳机或蓝牙
     *
     * @param routing
     */
    @Override
    public void onAudioRouteChanged(int routing) {
        super.onAudioRouteChanged(routing);
        if (UdeskSocketContants.isDebug) {
            Log.i(UdeskSocketContants.Tag, "onAudioRouteChanged routing = " + routing);
        }
    }

    @Override
    public void onFirstLocalAudioFrame(int elapsed) {
        super.onFirstLocalAudioFrame(elapsed);
    }

    @Override
    public void onFirstRemoteAudioFrame(int uid, int elapsed) {
        super.onFirstRemoteAudioFrame(uid, elapsed);
    }

    @Override
    public void onActiveSpeaker(int uid) {
        super.onActiveSpeaker(uid);
    }

    @Override
    public void onAudioEffectFinished(int soundId) {
        super.onAudioEffectFinished(soundId);
    }

    @Override
    public void onClientRoleChanged(int oldRole, int newRole) {
        super.onClientRoleChanged(oldRole, newRole);
    }
}
