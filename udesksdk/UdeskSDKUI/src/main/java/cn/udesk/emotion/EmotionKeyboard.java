package cn.udesk.emotion;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.lang.reflect.Field;

import udesk.core.event.InvokeEventContainer;

import static android.view.View.NO_ID;

/**
 * 表情键盘协调工具
 */

public class EmotionKeyboard {

    private static final String SHARE_PREFERENCE_NAME = "EmotionKeyBoard";
    private static final String FULLSCREEN_SHARE_PREFERENCE_SOFT_INPUT_HEIGHT = "fullscreen_sofe_input_height";
    private static final String CLASSICSCREEN_SHARE_PREFERENCE_SOFT_INPUT_HEIGHT = "Classicscreen_sofe_input_height";
    private Activity mActivity;
    private InputMethodManager mInputManager;//软键盘管理类
    private SharedPreferences mSp;
    private View mEmotionLayout;//表情布局
    private EditText mEditText;
    private View mContentView;//内容布局view,即除了表情布局或者软键盘布局以外的布局，用于固定bar的高度，防止跳闪
    private int tempContentViewHeight;
    private int tempInputHeight;

    public EmotionKeyboard() {
    }

    public static EmotionKeyboard with(Activity activity) {
        EmotionKeyboard emotionInputDetector = new EmotionKeyboard();
        emotionInputDetector.mActivity = activity;
        emotionInputDetector.mInputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        emotionInputDetector.mSp = activity.getSharedPreferences(SHARE_PREFERENCE_NAME, Context.MODE_PRIVATE);
        return emotionInputDetector;
    }

    /**
     * 绑定内容view，此view用于固定bar的高度，防止跳闪
     */
    public EmotionKeyboard bindToContent(View contentView) {
        mContentView = contentView;
        return this;
    }

    /**
     * 绑定编辑框
     */
    public EmotionKeyboard bindToEditText(EditText editText) {
        try {
            mEditText = editText;
            mEditText.requestFocus();
            mEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP && mEmotionLayout.isShown()) {
                        lockContentHeight();//显示软件盘时，锁定内容高度，防止跳闪。
                        hideEmotionLayout(true);//隐藏表情布局，显示软件盘
                        unlockContentHeightDelayed();
                        InvokeEventContainer.getInstance().eventui_OnHideLayout.invoke(true);
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
    /**
     * 绑定表情按钮（可以有多个表情按钮）
     *
     * @param emotionButton
     * @return
     */
    public EmotionKeyboard bindToEmotionButton(View... emotionButton) {
        for (View view : emotionButton) {
            view.setOnClickListener(getOnEmotionButtonOnClickListener());
        }
        return this;
    }

    private View.OnClickListener getOnEmotionButtonOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mOnEmotionButtonOnClickListener != null) {
                        if (mOnEmotionButtonOnClickListener.onEmotionButtonOnClickListener(v)) {
                            return;
                        }
                    }

                    if (mEmotionLayout.isShown()) {
                        lockContentHeight();//显示软件盘时，锁定内容高度，防止跳闪。
                        hideEmotionLayout(true);//隐藏表情布局，显示软件盘
                        unlockContentHeightDelayed();//软件盘显示后，释放内容高度
                    } else {
                        if (isSoftInputShown()) {//同上
                            lockContentHeight();
                            showEmotionLayout();
                            unlockContentHeightDelayed();
                        } else {
                            showEmotionLayout();//两者都没显示，直接显示表情布局
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /*================== 表情按钮点击事件回调 begin ==================*/
    public interface OnEmotionButtonOnClickListener {
        /**
         * @param view
         * @return true:拦截切换输入法，false:让输入法正常切换
         */
        boolean onEmotionButtonOnClickListener(View view);
    }

    OnEmotionButtonOnClickListener mOnEmotionButtonOnClickListener;

    public void setOnEmotionButtonOnClickListener(OnEmotionButtonOnClickListener onEmotionButtonOnClickListener) {
        mOnEmotionButtonOnClickListener = onEmotionButtonOnClickListener;
    }
    /*================== 表情按钮点击事件回调 end ==================*/

    /**
     * 设置表情内容布局
     *
     * @param emotionLayout
     * @return
     */
    public EmotionKeyboard setEmotionLayout(View emotionLayout) {
        mEmotionLayout = emotionLayout;
        return this;
    }

    public EmotionKeyboard build() {
        try {
            //设置软件盘的模式：SOFT_INPUT_ADJUST_RESIZE  这个属性表示Activity的主窗口总是会被调整大小，从而保证软键盘显示空间。
            //从而方便我们计算软件盘的高度
            mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN |
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            //隐藏软件盘
            hideSoftInput();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 点击返回键时先隐藏表情布局
     *
     * @return
     */
    public boolean interceptBackPress() {
        try {
            if (mEmotionLayout.isShown()) {
                hideEmotionLayout(false);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showEmotionLayout() {
        try {
            int softInputHeight = getSupportSoftInputHeight();
            if (softInputHeight <= 0) {
                if (!isNavigationBarExist(mActivity)) {
                    // 是全屏
                    softInputHeight = mSp.getInt(FULLSCREEN_SHARE_PREFERENCE_SOFT_INPUT_HEIGHT, dip2Px(294));
                } else {
                    softInputHeight = mSp.getInt(CLASSICSCREEN_SHARE_PREFERENCE_SOFT_INPUT_HEIGHT, dip2Px(294));
                }
            }
            softInputHeight = softInputHeight >= dip2Px(240) ? softInputHeight : dip2Px(294);
            tempInputHeight = softInputHeight;
            hideSoftInput();
            mEmotionLayout.getLayoutParams().height = softInputHeight;
            mEmotionLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int dip2Px(int dip) {
        float density = mActivity.getApplicationContext().getResources().getDisplayMetrics().density;
        int px = (int) (dip * density + 0.5f);
        return px;
    }

    /**
     * 隐藏表情布局
     *
     * @param showSoftInput 是否显示软件盘
     */
    public void hideEmotionLayout(boolean showSoftInput) {
        try {
            if (mEmotionLayout.isShown()) {
                mEmotionLayout.setVisibility(View.GONE);
                if (showSoftInput) {
                    showSoftInput();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 锁定内容高度，防止跳闪
     */
    public void lockContentHeight() {
        try {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContentView.getLayoutParams();
            tempContentViewHeight = mContentView.getHeight();
            params.height = mContentView.getHeight();
            params.weight = 0.0F;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放被锁定的内容高度
     */
    public void unlockContentHeightDelayed() {
        try {
            mEditText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((LinearLayout.LayoutParams) mContentView.getLayoutParams()).weight = 1.0F;
                }
            }, 200L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 编辑框获取焦点，并显示软件盘
     */
    public void showSoftInput() {
        try {
            mEditText.requestFocus();
            InvokeEventContainer.getInstance().eventui_OnHideLayout.invoke(true);
            mEditText.post(new Runnable() {
                @Override
                public void run() {
                    mInputManager.showSoftInput(mEditText, 0);
                }
            });
            mEditText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    int softInputHeight;
                    if(isNavigationBarExist(mActivity)){
                        if (mSp.getInt(CLASSICSCREEN_SHARE_PREFERENCE_SOFT_INPUT_HEIGHT, 0) == 0){
                            getSupportSoftInputHeight();
                        }
                        softInputHeight = mSp.getInt(CLASSICSCREEN_SHARE_PREFERENCE_SOFT_INPUT_HEIGHT, 0);
                    }else {
                        if (mSp.getInt(FULLSCREEN_SHARE_PREFERENCE_SOFT_INPUT_HEIGHT, 0) == 0){
                            getSupportSoftInputHeight();
                        }
                        // 是全屏
                        softInputHeight = mSp.getInt(FULLSCREEN_SHARE_PREFERENCE_SOFT_INPUT_HEIGHT, 0);
                    }
                    if (softInputHeight > 0){
                        int diff = tempInputHeight-softInputHeight;
                        if (diff != 0){
                            startAnimation(diff);
                        }
                    }
                }
            },150L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAnimation(int diff) {
        //属性动画对象
        ValueAnimator va;
        va = ValueAnimator.ofInt(tempContentViewHeight, tempContentViewHeight+diff);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //获取当前的height值
                int h =(Integer)valueAnimator.getAnimatedValue();
                //动态更新view的高度
                mContentView.getLayoutParams().height = h;
                mContentView.requestLayout();
            }
        });
        va.setDuration(2000);
        //开始动画
        va.start();
    }
    /**
     * 隐藏软件盘
     */
    public void hideSoftInput() {
        try {
            mInputManager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否显示软件盘
     *
     * @return
     */
    public boolean isSoftInputShown() {
        return getSupportSoftInputHeight() > 0;
    }

    /**
     * 获取软件盘的高度
     *
     * @return
     */
    private int getSupportSoftInputHeight() {
        int softInputHeight = 0;
        try {
            Rect r = new Rect();
            /**
             * decorView是window中的最顶层view，可以从window中通过getDecorView获取到decorView。
             * 通过decorView获取到程序显示的区域，包括标题栏，但不包括状态栏。
             */
            mActivity.getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
            //获取屏幕的高度
            int screenHeight = mActivity.getWindow().getDecorView().getRootView().getHeight();
            //计算软件盘的高度
            softInputHeight = screenHeight - r.bottom;
            /**
             * 某些Android版本下，没有显示软键盘时减出来的高度总是144，而不是零，
             * 这是因为高度是包括了虚拟按键栏的(例如华为系列)，所以在API Level高于20时，
             * 我们需要减去底部虚拟按键栏的高度（如果有的话）
             */
            if (!isNavigationBarExist(mActivity)) {
                mSp.edit().putInt(CLASSICSCREEN_SHARE_PREFERENCE_SOFT_INPUT_HEIGHT, 0).apply();
                // 是全屏
                //存一份到本地
                if (softInputHeight > 400) {
                    mSp.edit().putInt(FULLSCREEN_SHARE_PREFERENCE_SOFT_INPUT_HEIGHT, softInputHeight).apply();
                }
            } else {
                mSp.edit().putInt(FULLSCREEN_SHARE_PREFERENCE_SOFT_INPUT_HEIGHT, 0).apply();
                if (Build.VERSION.SDK_INT >= 20 && softInputHeight > 0) {
                    // When SDK Level >= 20 (Android L), the softInputHeight will contain the height of softButtonsBar (if has)
                    softInputHeight = softInputHeight - isNavigationHeight(mActivity);
                }
                //存一份到本地
                if (softInputHeight > 400) {
                    mSp.edit().putInt(CLASSICSCREEN_SHARE_PREFERENCE_SOFT_INPUT_HEIGHT, softInputHeight).apply();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return softInputHeight;
    }
    private static final String NAVIGATION = "navigationBarBackground";

    private boolean isNavigationBarExist(Activity activity) {
        try {
            ViewGroup vp = (ViewGroup) activity.getWindow().getDecorView();
            if (vp != null) {
                for (int i = 0; i < vp.getChildCount(); i++) {
                    vp.getChildAt(i).getContext().getPackageName();
                    if (vp.getChildAt(i).getId() != NO_ID && NAVIGATION.equals(activity.getResources().getResourceEntryName(vp.getChildAt(i).getId()))) {
                        if (vp.getChildAt(i).getHeight() != 0){
                            return true;
                        }
                    }
                }
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int isNavigationHeight(Activity activity) {
        try {
            ViewGroup vp = (ViewGroup) activity.getWindow().getDecorView();
            if (vp != null) {
                for (int i = 0; i < vp.getChildCount(); i++) {
                    vp.getChildAt(i).getContext().getPackageName();
                    if (vp.getChildAt(i).getId() != NO_ID && NAVIGATION.equals(activity.getResources().getResourceEntryName(vp.getChildAt(i).getId()))) {
                        return vp.getChildAt(i).getHeight();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 底部虚拟按键栏的高度
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int getSoftButtonsBarHeight() {
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            //这个方法获取可能不是真实屏幕的高度
            mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int usableHeight = metrics.heightPixels;
            //获取当前屏幕的真实高度
            mActivity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            int realHeight = metrics.heightPixels;
            if (realHeight > usableHeight) {
                return realHeight - usableHeight;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void destory() {
        try {
            if (mActivity != null) {
                fixInputMethodManagerLeak(mActivity);
                mActivity = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fixInputMethodManagerLeak(Context destContext) {
        try {
            if (destContext == null) {
                return;
            }
            InputMethodManager imm = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm == null) {
                return;
            }
            String[] arr = new String[]{"mCurRootView", "mServedView", "mNextServedView"};
            Field f = null;
            Object obj_get = null;
            for (int i = 0; i < arr.length; i++) {
                String param = arr[i];
                try {
                    f = imm.getClass().getDeclaredField(param);
                    if (f.isAccessible() == false) {
                        f.setAccessible(true);
                    } // author: sodino mail:sodino@qq.com
                    obj_get = f.get(imm);
                    if (obj_get != null && obj_get instanceof View) {
                        View v_get = (View) obj_get;
                        if (v_get.getContext() == destContext) { // 被InputMethodManager持有引用的context是想要目标销毁的
                            f.set(imm, null); // 置空，破坏掉path to gc节点
                        } else {
                            // 不是想要目标销毁的，即为又进了另一层界面了，不要处理，避免影响原逻辑,也就不用继续for循环了
                            break;
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
