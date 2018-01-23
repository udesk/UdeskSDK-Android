package udesk.udeskvideo.floatview;

import android.content.Context;
import android.content.Intent;
import android.view.SurfaceView;

import udesk.udeskvideo.floatview.service.FloatMonkService;


public class FloatActionController {

    private FloatActionController() {
    }

    public static FloatActionController getInstance() {
        return LittleMonkProviderHolder.sInstance;
    }

    // 静态内部类
    private static class LittleMonkProviderHolder {
        private static final FloatActionController sInstance = new FloatActionController();
    }

    private FloatCallBack mFloatCallBack;



    public FloatCallBack getmFloatCallBack() {
        return mFloatCallBack;
    }

    public void setmFloatCallBack(FloatCallBack mFloatCallBack) {
        this.mFloatCallBack = mFloatCallBack;
    }


    /**
     * 开启服务悬浮窗
     */
    public void startMonkServer(Context context) {
        Intent intent = new Intent(context, FloatMonkService.class);
        context.startService(intent);
    }

    /**
     * 关闭悬浮窗
     */
    public void stopMonkServer(Context context) {
        Intent intent = new Intent(context, FloatMonkService.class);
        context.stopService(intent);
    }

    /**
     * 注册监听
     */
    public void registerCallLittleMonk(FloatCallBack callLittleMonk) {
        mFloatCallBack = callLittleMonk;
    }


    /**
     * 悬浮窗的显示
     */
    public void show() {
        if (mFloatCallBack == null) return;
        mFloatCallBack.show();
    }

    /**
     * 悬浮窗的隐藏
     */
    public void hide() {
        if (mFloatCallBack == null) return;
        mFloatCallBack.hide();
    }

}
