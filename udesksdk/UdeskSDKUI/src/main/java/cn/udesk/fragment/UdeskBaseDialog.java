package cn.udesk.fragment;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import cn.udesk.R;
import cn.udesk.activity.UdeskBaseActivity;

public abstract class UdeskBaseDialog extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (setDialogStyle() == 0) {
                setStyle(DialogFragment.STYLE_NO_FRAME, R.style.udesk_default_dialog_style);
            } else {
                setStyle(DialogFragment.STYLE_NO_FRAME, setDialogStyle());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            if (setWindowAnimationsStyle() != 0) {
                getDialog().getWindow().setWindowAnimations(setWindowAnimationsStyle());
            }
            View mView = null;
            if (mView == null) {
                mView = inflater.inflate(getLayoutId(), container, false);
            } else {
                if (mView != null) {
                    ViewGroup mViewGroup = (ViewGroup) mView.getParent();
                    if (mViewGroup != null) {
                        mViewGroup.removeView(mView);
                    }
                }
            }
            return mView;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData(getArguments());
    }

    protected abstract void loadData(Bundle arguments);


    /**
     * 设置Dialog点击外部区域是否隐藏
     *
     * @param cancel
     */
    protected void setCanceledOnTouchOutside(boolean cancel) {
        try {
            if (getDialog() != null) {
                getDialog().setCanceledOnTouchOutside(cancel);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置Dialog gravity
     *
     * @param gravity
     */
    protected void setGravity(int gravity) {
        try {
            if (getDialog() != null) {
                Window mWindow = getDialog().getWindow();
                WindowManager.LayoutParams params = mWindow.getAttributes();
                params.gravity = gravity;
                mWindow.setAttributes(params);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置Dialog窗口width
     *
     * @param width
     */
    protected void setDialogWidth(int width) {
        try {
            setDialogWidthAndHeight(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        }catch (Exception e){

        }
    }

    /**
     * 设置Dialog窗口height
     *
     * @param height
     */
    protected void setDialogHeight(int height) {
        try {
            setDialogWidthAndHeight(LinearLayout.LayoutParams.WRAP_CONTENT, height);
        }catch (Exception e){

        }
    }

    /**
     * 设置Dialog窗口width，height
     *
     * @param width
     * @param height
     */
    protected void setDialogWidthAndHeight(int width, int height) {
        try {
            if (getDialog() != null) {
                getDialog().getWindow().setLayout(width, height);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 显示Dialog
     *
     * @param activity
     * @param tag      设置一个标签用来标记Dialog
     */
    public void show(UdeskBaseActivity activity, String tag) {
        try {
            show(activity, null, tag);
        }catch (Exception e){

        }
    }

    /**
     * 显示Dialog
     *
     * @param activity
     * @param bundle   要传递给Dialog的Bundle对象
     * @param tag      设置一个标签用来标记Dialog
     */

    public void show(UdeskBaseActivity activity, Bundle bundle, String tag) {
        try {
            if (activity == null && isShowing()) {
                return;
            }
            FragmentTransaction mTransaction = activity.getSupportFragmentManager().beginTransaction();
            Fragment mFragment = activity.getSupportFragmentManager().findFragmentByTag(tag);
            if (mFragment != null) {
                //为了不重复显示dialog，在显示对话框之前移除正在显示的对话框
                mTransaction.remove(mFragment);
            }
            if (bundle != null) {
                setArguments(bundle);
            }
            show(mTransaction, tag);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 是否显示
     *
     * @return false:isHidden  true:isShowing
     */
    protected boolean isShowing() {
        try {
            if (this.getDialog() != null) {
                return this.getDialog().isShowing();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 初始化
     *
     * @param view
     */
    protected abstract void initView(View view);

    /**
     * 自定义时添加layout
     *
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 设置窗口转场动画
     *
     * @return
     */
    protected int setWindowAnimationsStyle() {
        return 0;
    }

    /**
     * 设置弹出框样式
     *
     * @return
     */
    protected int setDialogStyle() {
        return 0;
    }
}
