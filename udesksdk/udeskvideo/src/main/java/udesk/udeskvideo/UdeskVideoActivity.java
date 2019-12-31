package udesk.udeskvideo;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import io.agora.rtc.RtcEngine;
import udesk.core.UdeskConst;
import udesk.core.event.InvokeEventContainer;
import udesk.udesksocket.EventScoketMode;
import udesk.udesksocket.MessageManager;
import udesk.udesksocket.MethodEnum;
import udesk.udesksocket.UdeskSocketContants;
import udesk.udesksocket.Util;
import udesk.udesksocket.mode.event.EvtCancel;
import udesk.udesksocket.mode.invite.GetChannelTokenRep;
import udesk.udesksocket.mode.invite.InviteRep;
import udesk.udeskvideo.floatview.FloatActionController;
import udesk.udeskvideo.floatview.permission.FloatPermissionManager;
import udesk.udeskvideo.mode.EventAgoraMode;
import udesk.udeskvideo.mode.EventFinish;
import udesk.udeskvideo.presenter.VideoPresenter;

public class UdeskVideoActivity extends Activity implements View.OnClickListener {

    private View rootView, video_view, videoReceiveView;

    private FrameLayout big_video_view_container, small_video_view_container;
    private ImageView switch_camera;
    private View udesk_btn_mute, udesk_btn_camera, udesk_btn_speaker, udesk_btn_putway, udesk_rejept, udesk_answer;
    private ImageView muteImg, cameraImg, speakerImg, hang_up;
    private TextView muteText, cameraText, speakerText, invite_tips, agent_name;

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;
    private static final int PERMISSION_REQ_ID_CAMERA = 23;

    private boolean isHasJoinChannel = false;
    private int remoteUid;

    //是否是主动发起方
    private boolean isInvete = true;
    private boolean isPutaway = false;
    private String channelID = "";

    //对answer的消息处理，但是没有joinchannel成功的 在离开时也需要发送bye消息
    private boolean isNeedSendByeOndestory = false;

    private VideoPresenter videoPresenter;
    private MediaPlayer mMediaPlayer;

    private TextView timeView;
    private final static int HandleTypeTimeOver = 0;
    private int videoTime = 0;
    private String text = "00:00:00";
    private boolean isDestroyed = false;


    private TimeHandler mTimeHandler;

    private boolean isCountTime = false;

    private static class TimeHandler extends Handler {
        WeakReference<UdeskVideoActivity> mWeakActivity;

        public TimeHandler(UdeskVideoActivity activity) {
            mWeakActivity = new WeakReference<UdeskVideoActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                UdeskVideoActivity activity = mWeakActivity.get();
                switch (msg.what) {
                    case HandleTypeTimeOver:
                        activity.videoTime++;
                        this.sendEmptyMessageDelayed(HandleTypeTimeOver, 1000);//1秒更新一次
                        activity.text = Util.secToTime(activity.videoTime);
                        activity.timeView.setText(activity.text);
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void startTime() {
        isCountTime = true;
        videoTime = 0;
        mTimeHandler.removeMessages(HandleTypeTimeOver);
        mTimeHandler.sendEmptyMessage(HandleTypeTimeOver);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
            UdeskVideoCallManager.getInstance().initWorkerThread(getApplicationContext());
            videoPresenter = new VideoPresenter();
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
            setContentView(R.layout.udesk_video_chat_view);
            mTimeHandler = new TimeHandler(this);
            Intent intent = getIntent();
            channelID = intent.getStringExtra(UdeskSocketContants.ChannelName);
            isInvete = intent.getBooleanExtra(UdeskSocketContants.IsInivte, true);
            sendVideoBroadcast(UdeskSocketContants.ReceiveType.StartMedio, getResources().getString(R.string.udesk_video_start));
            initview();
            if (isInvete) {
                initinvite();
                if (UdeskConst.FIRST_OPEN){
                    UdeskVideoCallManager.getInstance().connectWebsocket(this);
                }else {
                    MessageManager.getMessageManager().invite(UdeskConst.IMAgentJid, UdeskSocketContants.ToResId, UdeskSocketContants.CallType.video, channelID, Util.objectToInt(UdeskConst.IMBussinessId));
                }
            } else {
                initReceiveVideo();
                startAlarm();
            }
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
                if (isInvete) {
                    videoPresenter.setupLocalVideo(getApplicationContext(), big_video_view_container);
                }
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
            finish();
        }
    }

    private void initview() {
        rootView = findViewById(R.id.activity_video_chat_view);
        video_view = findViewById(R.id.video_view);
        videoReceiveView = findViewById(R.id.video_receive_view);

        big_video_view_container = (FrameLayout) findViewById(R.id.big_video_view_container);
        small_video_view_container = (FrameLayout) findViewById(R.id.small_video_view_container);
        switch_camera = (ImageView) findViewById(R.id.switch_camera);
        switch_camera.setOnClickListener(this);
        udesk_btn_mute = findViewById(R.id.udesk_btn_mute);
        udesk_btn_mute.setOnClickListener(this);
        udesk_btn_camera = findViewById(R.id.udesk_btn_camera);
        udesk_btn_camera.setOnClickListener(this);
        udesk_btn_speaker = findViewById(R.id.udesk_btn_speaker);
        udesk_btn_speaker.setOnClickListener(this);
        udesk_btn_putway = findViewById(R.id.udesk_btn_putway);
        udesk_btn_putway.setOnClickListener(this);

        udesk_rejept = findViewById(R.id.udesk_rejept);
        udesk_rejept.setOnClickListener(this);
        udesk_answer = findViewById(R.id.udesk_answer);
        udesk_answer.setOnClickListener(this);

        muteImg = (ImageView) findViewById(R.id.udesk_btn_mute_img);
        cameraImg = (ImageView) findViewById(R.id.udesk_btn_camera_img);
        speakerImg = (ImageView) findViewById(R.id.udesk_btn_speaker_img);


        muteText = (TextView) findViewById(R.id.tab_mute_text);
        cameraText = (TextView) findViewById(R.id.tab_camera_text);
        speakerText = (TextView) findViewById(R.id.tab_speaker_text);
        invite_tips = (TextView) findViewById(R.id.invite_tips);
        agent_name = (TextView) findViewById(R.id.agent_name);
        timeView = (TextView) findViewById(R.id.udesk_time);

        hang_up = (ImageView) findViewById(R.id.hang_up);
        hang_up.setOnClickListener(this);
    }

    private void initinvite() {
        rootView.setBackgroundColor(getResources().getColor(R.color.color000000));
        switch_camera.setVisibility(View.GONE);
        small_video_view_container.setVisibility(View.GONE);
        big_video_view_container.setVisibility(View.VISIBLE);
        invite_tips.setVisibility(View.VISIBLE);
        video_view.setVisibility(View.VISIBLE);
        videoReceiveView.setVisibility(View.GONE);
    }

    private void initReceiveVideo() {
        rootView.setBackgroundColor(getResources().getColor(R.color.color555555));
        switch_camera.setVisibility(View.GONE);
        small_video_view_container.setVisibility(View.GONE);
        big_video_view_container.setVisibility(View.GONE);
        invite_tips.setVisibility(View.GONE);
        video_view.setVisibility(View.GONE);
        videoReceiveView.setVisibility(View.VISIBLE);
    }

    //收到邀请后切换得UI
    private void receiveTojoinView() {
        videoReceiveView.setVisibility(View.GONE);
        invite_tips.setVisibility(View.GONE);
        rootView.setBackgroundColor(getResources().getColor(R.color.color000000));
        switch_camera.setVisibility(View.VISIBLE);
        small_video_view_container.setVisibility(View.VISIBLE);
        big_video_view_container.setVisibility(View.VISIBLE);
        video_view.setVisibility(View.VISIBLE);
        videoPresenter.setupLocalVideo(getApplicationContext(), small_video_view_container);
    }

    private void showVideoView() {
        invite_tips.setVisibility(View.GONE);
        rootView.setBackgroundColor(getResources().getColor(R.color.color000000));
        switch_camera.setVisibility(View.VISIBLE);
        small_video_view_container.setVisibility(View.VISIBLE);
        big_video_view_container.setVisibility(View.VISIBLE);
        video_view.setVisibility(View.VISIBLE);
        videoReceiveView.setVisibility(View.GONE);
    }


    public boolean checkSelfPermission(String permission, int requestCode) {
        try {
            if (ContextCompat.checkSelfPermission(this,
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{permission},
                        requestCode);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

        try {
            switch (requestCode) {
                case PERMISSION_REQ_ID_RECORD_AUDIO: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA);
                    } else {
                        showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                        finish();
                    }
                    break;
                }
                case PERMISSION_REQ_ID_CAMERA: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (isInvete) {
                            videoPresenter.setupLocalVideo(getApplicationContext(), big_video_view_container);
                        }
                    } else {
                        showLongToast("No permission for " + Manifest.permission.CAMERA);
                        finish();
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendVideoBroadcast(String type, String message) {

        try {
            InvokeEventContainer.getInstance().event_OnVideoEventReceived.invoke(type, channelID, message, isInvete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

        try {
            if (v.getId() == R.id.switch_camera) {
                UdeskVideoCallManager.getInstance().getWorkerThread().switchCamear();
            } else if (v.getId() == R.id.udesk_btn_mute) {
                //本地禁音设置
                onLocalAudioMuteClicked();
            } else if (v.getId() == R.id.udesk_btn_camera) {
                //显示和隐藏自己显示
                onShowSelfCamera();
            } else if (v.getId() == R.id.udesk_btn_speaker) {
                //本地扬声器设置
                onLocalSpeakerClicked();
            } else if (v.getId() == R.id.udesk_btn_putway) {
                if (!isHasJoinChannel) {
                    return;
                }
                putaway();
            } else if (v.getId() == R.id.hang_up) {
                if (isHasJoinChannel) {
                    UdeskVideoCallManager.getInstance().getWorkerThread().leaveChannel(channelID);
                } else {
                    sendVideoBroadcast(UdeskSocketContants.ReceiveType.Cancle, getResources().getString(R.string.udesk_video_cancle));
                    videoPresenter.cancel(channelID);
                    finish();
                }
            } else if (v.getId() == R.id.udesk_answer) {
                //获取获取加入媒体channel的认证token
                //加载本地的预览，直到token取到后，调用joinchannel
                videoPresenter.getChannelToken(channelID);
                receiveTojoinView();
                stopAlarm();
            } else if (v.getId() == R.id.udesk_rejept) {
                videoPresenter.cancel(channelID);
                finish();
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    //将自己静音,对方听不到自己这边的声音
    public void onLocalAudioMuteClicked() {
        try {
            if (!isHasJoinChannel) {
                return;
            }
            if (udesk_btn_mute.isSelected()) {
                udesk_btn_mute.setSelected(false);
                muteImg.setImageResource(R.drawable.udesk_mute_unselected);
                muteText.setTextColor(getResources().getColor(R.color.color1086ff));
            } else {
                udesk_btn_mute.setSelected(true);
                muteImg.setImageResource(R.drawable.udesk_mute_selected);
                muteText.setTextColor(getResources().getColor(R.color.colorffffff));
            }
//        True: 麦克风静音  False: 取消静音
            UdeskVideoCallManager.getInstance().getWorkerThread().getRtcEngine().muteLocalAudioStream(udesk_btn_mute.isSelected());
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    public void onShowSelfCamera() {
        try {
            if (!isHasJoinChannel) {
                return;
            }
            if (udesk_btn_camera.isSelected()) {
                udesk_btn_camera.setSelected(false);
                cameraImg.setImageResource(R.drawable.udesk_camera_unselected);
                cameraText.setTextColor(getResources().getColor(R.color.color1086ff));
            } else {
                udesk_btn_camera.setSelected(true);
                cameraImg.setImageResource(R.drawable.udesk_camera_selected);
                cameraText.setTextColor(getResources().getColor(R.color.colorffffff));
            }
            UdeskVideoCallManager.getInstance().getWorkerThread().getRtcEngine().muteLocalVideoStream(udesk_btn_camera.isSelected());
            //选中隐藏自己视频流
            if (udesk_btn_camera.isSelected()) {
                small_video_view_container.removeAllViews();
                small_video_view_container.setVisibility(View.GONE);
            } else {
                small_video_view_container.removeAllViews();
                small_video_view_container.setVisibility(View.VISIBLE);
                videoPresenter.setupLocalVideo(getApplicationContext(), small_video_view_container);
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }


    public void onLocalSpeakerClicked() {
        try {
            if (!isHasJoinChannel) {
                return;
            }
            if (udesk_btn_speaker.isSelected()) {
                udesk_btn_speaker.setSelected(false);
                speakerImg.setImageResource(R.drawable.udesk_speaker_unselected);
                speakerText.setTextColor(getResources().getColor(R.color.colorffffff));
            } else {
                udesk_btn_speaker.setSelected(true);
                speakerImg.setImageResource(R.drawable.udesk_speaker_selected);
                speakerText.setTextColor(getResources().getColor(R.color.color1086ff));
            }
//        True: 用该 API 后均会默认切换到从外放(扬声器)出声  False: 语音会根据默认路由出声
            UdeskVideoCallManager.getInstance().getWorkerThread().getRtcEngine().setEnableSpeakerphone(udesk_btn_speaker.isSelected());
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }


    private void putaway() {
        try {
            isPutaway = true;
            boolean isPermission = FloatPermissionManager.getInstance().applyFloatWindow(UdeskVideoActivity.this, true);
            //有对应权限或者系统版本小于7.0
            if (isPermission) {
                moveTaskToBack(true);
                //开启悬浮窗
                //            overridePendingTransition(0, R.anim.fade_out);
                final SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
                UdeskVideoCallManager.getInstance().getWorkerThread().setupRemoteVideo(surfaceView, remoteUid);
                UdeskVideoCallManager.getInstance().setRemoteVideoView(surfaceView);
                FloatActionController.getInstance().startMonkServer(UdeskVideoActivity.this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
//        overridePendingTransition(R.anim.fade_in, 0);
        super.onRestart();
        isPutaway = false;
        FloatActionController.getInstance().stopMonkServer(getApplicationContext());
        big_video_view_container.removeAllViews();
        small_video_view_container.removeAllViews();
        videoPresenter.setupRemoteVideo(getApplicationContext(), big_video_view_container, small_video_view_container, remoteUid);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFinishEvent(EventFinish eventFinish) {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSendNoticeEvent(EventScoketMode event) {
        try {
            MethodEnum methodEnum = MethodEnum.getByValue(event.getMethod());
            if (methodEnum == null) {
                return;
            }
            switch (methodEnum) {
                case LOGIN:
                    if (UdeskConst.FIRST_OPEN){
                        UdeskConst.FIRST_OPEN= false;
                        MessageManager.getMessageManager().invite(UdeskConst.IMAgentJid, UdeskSocketContants.ToResId, UdeskSocketContants.CallType.video, channelID, Util.objectToInt(UdeskConst.IMBussinessId));
                    }
                    break;
                case INVITE:
                    if (event.getMsg_type().equals(UdeskSocketContants.MsgType.Rep)) {
                        InviteRep inviteRep = (InviteRep) event.getData();
                        if (Util.objectToString(inviteRep.getRcode()).equals(UdeskSocketContants.ClientNotLoginError)) {
                            sendVideoBroadcast(UdeskSocketContants.ReceiveType.Busy, getResources().getString(R.string.udesk_video_timeout));
                            finish();
                        } else if (Util.objectToString(inviteRep.getRcode()).equals(UdeskSocketContants.ClientNotInIdleStateError)) {
                            sendVideoBroadcast(UdeskSocketContants.ReceiveType.Busy, getResources().getString(R.string.udesk_video_not_loggin));
                            finish();
                        } else if (Util.objectToString(inviteRep.getRcode()).equals(UdeskSocketContants.ACKTimeOut)) {
                            sendVideoBroadcast(UdeskSocketContants.ReceiveType.Busy, getResources().getString(R.string.udesk_video_not_loggin));
                            finish();
                        }

                    }
                    break;
                case GETCHANNELTOKEN:
                    //收到getchannelToke的响应,加入频道

                    if (event.getMsg_type().equals(UdeskSocketContants.MsgType.Rep)) {
                        GetChannelTokenRep getChannelTokenRep = (GetChannelTokenRep) event.getData();
                        channelID = Util.objectToString(getChannelTokenRep.getChannel_id());
                        videoPresenter.joinChannel(Util.objectToString(getChannelTokenRep.getChannel_id()), Util.objectToInt(getChannelTokenRep.getAgora_uid()),
                                Util.objectToString(getChannelTokenRep.getChannel_token()));
                        //answer
                        if (!isInvete) {
                            MessageManager.getMessageManager().answer(channelID);
                            isNeedSendByeOndestory = true;
                        }
                    }
                    break;
                case BYE:
                    if (event.getMsg_type().equals(UdeskSocketContants.MsgType.Evt)) {
                        videoPresenter.leaveChanmel(channelID);
                    }
                    break;
                case ANSWER:
                    //收到对方的应答
                    showVideoView();
                    isNeedSendByeOndestory = true;
                    videoPresenter.getChannelToken(channelID);
                    break;
                case CANCEL:
                    //对方拒接收到的事件
                    if (event.getMsg_type().equals(UdeskSocketContants.MsgType.Evt)) {

                        EvtCancel evtCancel = (EvtCancel) event.getData();
                        if (Util.objectToString(evtCancel.getReason()).equals(UdeskSocketContants.TimeoutCancel)) {
                            sendVideoBroadcast(UdeskSocketContants.ReceiveType.Timeout, UdeskConst.IMAgentName + getResources().getString(R.string.udesk_video_timeout));
                        } else if (Util.objectToString(evtCancel.getReason()).equals(UdeskSocketContants.CalledReject)) {
                            sendVideoBroadcast(UdeskSocketContants.ReceiveType.Reject, UdeskConst.IMAgentName + getResources().getString(R.string.udesk_video_reject));
                        }
                        finish();
                    }
                    break;
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAgoraMethoEvent(EventAgoraMode eventAgoraMode) {
        try {
            AgoraMethodEnum methodEnum = AgoraMethodEnum.getByValue(eventAgoraMode.getMethod());
            if (methodEnum == null) {
                return;
            }
            switch (methodEnum) {
                //用户加入频道,本地加入频道
                case onUserJoined:
                    UdeskVideoCallManager.getInstance().getWorkerThread().getRtcEngine().setEnableSpeakerphone(false);
                    if (!isCountTime) {
                        startTime();
                    }
                    break;
                //加载远程视图
                case onFirstRemoteVideoDecoded:
                    //startMedia
                    if (videoPresenter != null) {
                        Map<String, Object> maps = eventAgoraMode.getValues();
                        remoteUid = Util.objectToInt(maps.get(UdeskSocketContants.UID));
                        isHasJoinChannel = true;
                        videoPresenter.setupRemoteVideo(getApplicationContext(), big_video_view_container, small_video_view_container, remoteUid);

                    }
                    break;
                case onUserOffline:
                    if (videoPresenter != null) {
                        Map<String, Object> maps = eventAgoraMode.getValues();
                        int reson = Util.objectToInt(maps.get(UdeskSocketContants.REASON));
                        //                    if (reson == 0) {
                        //                        FloatActionController.getInstance().stopMonkServer(getApplicationContext());
                        //                        videoPresenter.leaveChanmel(channelID);
                        //                    }
                    }
                    break;
                case onLeaveChannel:
                    if (videoPresenter != null) {
                        isNeedSendByeOndestory = false;
                        videoPresenter.stopMedia(channelID);
                        videoPresenter.bye(channelID);
                        sendVideoBroadcast(UdeskSocketContants.ReceiveType.Over, getResources().getString(R.string.udesk_video_over) + timeView.getText().toString());
                        finish();
                    }
                    break;
                //本地加入频道成功
                case onJoinChannelSuccess:
                    videoPresenter.startMedia(channelID);
                    if (!isInvete) {
                        videoPresenter.answer(channelID);
                    }
                    break;
                case onError:
                    Map<String, Object> maps = eventAgoraMode.getValues();
                    int err = Util.objectToInt(maps.get("err"));
                    if (err == 18) {
                        finish();
                    }
                    break;
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }


    private void startAlarm() {

        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(UdeskVideoActivity.this, alert);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopAlarm() {
        try {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        if (isFinishing()) {
            disPose();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {

        try {
            if (!isFinishing()) {
                if (!isPutaway) {
                    boolean isPermission = FloatPermissionManager.getInstance().applyFloatWindow(UdeskVideoActivity.this, false);
                    //有对应权限或者系统版本小于7.0
                    if (isPermission) {
                        moveTaskToBack(true);
                        final SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
                        UdeskVideoCallManager.getInstance().getWorkerThread().setupRemoteVideo(surfaceView, remoteUid);
                        UdeskVideoCallManager.getInstance().setRemoteVideoView(surfaceView);
                        FloatActionController.getInstance().startMonkServer(UdeskVideoActivity.this);
                    } else {
                        disPose();
                        finish();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        disPose();
        super.onDestroy();

    }

    private void disPose() {
        try {
            if (isDestroyed) {
                return;
            }
            // 回收资源
            isDestroyed = true;

            stopAlarm();
            if (isNeedSendByeOndestory && videoPresenter != null) {
                videoPresenter.bye(channelID);
            }
            if (videoPresenter != null) {
                videoPresenter.leaveChanmel(channelID);
            }
            if (mTimeHandler != null) {
                mTimeHandler.removeMessages(HandleTypeTimeOver);
                mTimeHandler = null;
            }
            FloatActionController.getInstance().stopMonkServer(getApplicationContext());
            EventBus.getDefault().unregister(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
