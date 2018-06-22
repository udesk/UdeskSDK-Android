package udesk.udeskvideo.presenter;

import android.content.Context;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import io.agora.rtc.RtcEngine;
import udesk.udesksocket.MessageManager;
import udesk.udesksocket.UdeskSocketContants;
import udesk.udeskvideo.UdeskVideoCallManager;

/**
 * author : ${揭军平}
 * time   : 2017/12/06
 * desc   :
 * version: 1.0
 */

public class VideoPresenter {

    //设置本地预览
    public void setupLocalVideo(Context context ,FrameLayout frameLayout) {
        SurfaceView surfaceView = RtcEngine.CreateRendererView(context);
        frameLayout.removeAllViews();
        frameLayout.addView(surfaceView);
        surfaceView.setZOrderMediaOverlay(true);
        UdeskVideoCallManager.getInstance().getWorkerThread().preview(true, surfaceView, 0);
    }

    //加载远程视图,本地视图切换
    public void setupRemoteVideo(Context context,FrameLayout remoteframeLayout,FrameLayout loaclframeLayout,int uid) {
        remoteframeLayout.removeAllViews();
        SurfaceView surfaceView = RtcEngine.CreateRendererView(context);
        remoteframeLayout.addView(surfaceView);
        UdeskVideoCallManager.getInstance().getWorkerThread().setupRemoteVideo(surfaceView, uid);
        setupLocalVideo(context,loaclframeLayout);
    }

    //加入频道
    public void joinChannel(String channelname,int uid,String chanelkey) {
        UdeskVideoCallManager.getInstance().getWorkerThread().joinChannel(channelname, uid, chanelkey);
    }

    //离开频道
    public void leaveChanmel(String channelname){
        UdeskVideoCallManager.getInstance().getWorkerThread().leaveChannel(channelname);
    }

    //获取加入频道的token
    public void  getChannelToken(String channelname){
        MessageManager.getMessageManager().getChannelToken(channelname);
    }

    //设置客户的状态为空闲
    public  void setState(){
        MessageManager.getMessageManager().setState(UdeskSocketContants.State.idle);
    }

    //离开会话再见
    public void bye(String channelname){
        MessageManager.getMessageManager().bye(channelname);
    }

    //应答
    public void answer(String channelname){
        MessageManager.getMessageManager().answer(channelname);
    }
    //取消
    public void cancel(String channelname){
        MessageManager.getMessageManager().cancel(channelname, "");
    }


    public void startMedia(String channelname){
        MessageManager.getMessageManager().startMedia(channelname);
    }
   public void stopMedia(String channelname){
       MessageManager.getMessageManager().stopMedia(channelname);
    }

}
