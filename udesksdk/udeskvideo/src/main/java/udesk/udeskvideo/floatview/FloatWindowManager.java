package udesk.udeskvideo.floatview;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;

/**
 * author : ${揭军平}
 * time   : 2017/11/30
 * desc   :
 * version: 1.0
 */

public class FloatWindowManager {

    /**
     * 悬浮窗
     */
    private static FloatLayout mFloatLayout;
    private static WindowManager mWindowManager;
    private static WindowManager.LayoutParams wmParams;
    private static boolean mHasShown;

    /**
     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
     *
     * @param context 必须为应用程序的Context.
     */
    public static void createFloatWindow(Context context) {
        wmParams = new WindowManager.LayoutParams();
        WindowManager windowManager = getWindowManager(context);
        mFloatLayout = new FloatLayout(context);
        if (Build.VERSION.SDK_INT >= 24) { /*android7.0不能用TYPE_TOAST*/
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else { /*以下代码块使得android6.0之后的用户不必再去手动开启悬浮窗权限*/
            String packname = context.getPackageName();
            PackageManager pm = context.getPackageManager();
            boolean permission = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.SYSTEM_ALERT_WINDOW", packname));
            if (permission) {
                wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            } else {
                wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
        }

        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.TOP | Gravity.RIGHT;

        DisplayMetrics dm = new DisplayMetrics();
        //取得窗口属性
        mWindowManager.getDefaultDisplay().getMetrics(dm);
//        //窗口的宽度
        int screenWidth = dm.widthPixels;
        mFloatLayout.setDeviceWidth(screenWidth);
//        //窗口高度
//        int screenHeight = dm.heightPixels;
//        //以屏幕左上角为原点，设置x、y初始值，相对于gravity
//        wmParams.x = screenWidth;
//        wmParams.y = screenHeight;
        wmParams.x = 0;
        wmParams.y = 0;

        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mFloatLayout.setParams(wmParams);
        windowManager.addView(mFloatLayout, wmParams);
        mHasShown = true;
    }

    /**
     * 移除悬浮窗
     */
    public static void removeFloatWindowManager() {
        //移除悬浮窗口
        boolean isAttach = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            isAttach = mFloatLayout.isAttachedToWindow();
        }
        if (mHasShown && isAttach && mWindowManager != null)
            mWindowManager.removeView(mFloatLayout);
    }

    /**
     * 返回当前已创建的WindowManager。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    public static void hide() {
        if (mHasShown)
            mWindowManager.removeViewImmediate(mFloatLayout);
        mHasShown = false;
    }

    public static void show() {
        if (!mHasShown)
            mWindowManager.addView(mFloatLayout, wmParams);
        mHasShown = true;
    }
}
