package udesk.udeskvideo.floatview;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import udesk.udesksocket.UdeskSocketContants;
import udesk.udeskvideo.R;
import udesk.udeskvideo.UdeskVideoActivity;
import udesk.udeskvideo.UdeskVideoCallManager;


public class FloatLayout extends FrameLayout {
    private final WindowManager mWindowManager;
    private final FrameLayout container;
    private long startTime;
    private float mTouchStartX;
    private float mTouchStartY;
    private boolean isclick;
    private WindowManager.LayoutParams mWmParams;
    private Context mContext;
    private long endTime;

    private int  widthPixels;

    public FloatLayout(Context context) {
        this(context, null);
        mContext = context;
    }

    public FloatLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.float_littlemonk_layout, this);
        //浮动窗口按钮
        container = (FrameLayout) findViewById(R.id.remote_video_view_container);
        if (UdeskVideoCallManager.getInstance().getRemoteVideoView() != null) {
            container.removeAllViews();
            container.addView(UdeskVideoCallManager.getInstance().getRemoteVideoView());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 获取相对屏幕的坐标，即以屏幕左上角为原点
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        //下面的这些事件，跟图标的移动无关，为了区分开拖动和点击事件
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startTime = System.currentTimeMillis();
                mTouchStartX = event.getX();
                mTouchStartY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //图标移动的逻辑在这里
                float mMoveStartX = event.getX();
                float mMoveStartY = event.getY();
                Log.i(UdeskSocketContants.Tag,"x ="+x +";y="+y);
                Log.i(UdeskSocketContants.Tag,"mTouchStartX ="+mTouchStartX +";mTouchStartY="+mTouchStartY);
                Log.i(UdeskSocketContants.Tag,"mMoveStartX ="+mMoveStartX +";mMoveStartY="+mMoveStartY);
                // 如果移动量大于3才移动
                if (Math.abs(mTouchStartX - mMoveStartX) > 3
                        && Math.abs(mTouchStartY - mMoveStartY) > 3) {
                    // 更新浮动窗口位置参数

//                    if (x>(int)(deviceWidth/2)){
//                        mWmParams.x = (int)(deviceWidth - x);
//                    }else {
//
//                    }

                    mWmParams.x = (int)(widthPixels - x);

                    mWmParams.y = (int) (y - mTouchStartY);
                    Log.i(UdeskSocketContants.Tag,"mWmParams.x ="+mWmParams.x +";mWmParams.y="+mWmParams.y);
                    mWindowManager.updateViewLayout(this, mWmParams);
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                endTime = System.currentTimeMillis();
                //当从点击到弹起小于半秒的时候,则判断为点击,如果超过则不响应点击事件
                if ((endTime - startTime) > 0.1 * 1000L) {
                    isclick = false;
                } else {
                    isclick = true;
                }
                break;
        }
        //响应点击事件
        if (isclick) {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(mContext, UdeskVideoActivity.class);
            mContext.startActivity(intent);
        }
        return true;
    }

    public void setDeviceWidth(int width){
          this.widthPixels = width;
    }


    /**
     * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
     *
     * @param params 小悬浮窗的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mWmParams = params;
    }


}
