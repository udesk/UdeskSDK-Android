package cn.udesk.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.udesk.R;


/**
 * udesk 标题栏
 */
public class UdeskTitleBar extends RelativeLayout {

    protected RelativeLayout udeskRootView;//根布局
    protected LinearLayout udeskbackll;//返回
    protected ImageView udeskBackImg;//返回图标
    //    protected TextView udeskLeft;
    protected TextView udeskRight;//右侧文字 转人工
    //    protected ImageView udeskStateImg;
    protected ImageView udeskTransferImg;//右侧图片 转人工图片
    protected ImageView udeskRobotImg;// 机器人头像
    protected LinearLayout udeskMiddlell;//中间布局
    protected TextView udeskTopText;//中部上边的文字
    protected TextView udeskBottomText;//中部底部的文字
    protected LinearLayout udeskRightll;//右侧布局


    public UdeskTitleBar(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public UdeskTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UdeskTitleBar(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
//        LayoutInflater.from(context).inflate(R.layout.udesk_title_bar, this);
        LayoutInflater.from(context).inflate(R.layout.udesk_title_bar_new, this);
        udeskRootView = (RelativeLayout) findViewById(R.id.udesk_root);
//        udeskLeft = (TextView) findViewById(R.id.udesk_content);
        udeskRight = (TextView) findViewById(R.id.udesk_titlebar_right);
        udeskbackll = (LinearLayout) findViewById(R.id.udesk_back_linear);
        udeskBackImg = (ImageView) findViewById(R.id.udesk_back_img);
//        udeskStateImg = (ImageView) findViewById(R.id.udesk_status);
        udeskTransferImg = (ImageView) findViewById(R.id.udesk_transfer_agent);
        udeskRobotImg = (ImageView) findViewById(R.id.udesk_robot_img);
        udeskMiddlell = (LinearLayout) findViewById(R.id.udesk_titlebar_middle_linear);
        udeskTopText = (TextView) findViewById(R.id.udesk_titlebar_middle_top);
        udeskBottomText = (TextView) findViewById(R.id.udesk_titlebar_middle_bottom);
        udeskRightll = (LinearLayout) findViewById(R.id.udesk_titlebar_right_linear);

    }

    @Override
    public RelativeLayout getRootView() {
        if (udeskRootView != null) {
            return udeskRootView;
        }
        return null;
    }

//    public  TextView getLeftTextView(){
//        if (udeskLeft != null){
//            return  udeskLeft;
//        }
//        return null;
//    }

    public TextView getUdeskTopText() {
        return udeskTopText;
    }

    public TextView getUdeskBottomText() {
        return udeskBottomText;
    }

    public TextView getRightTextView() {
        if (udeskRight != null) {
            return udeskRight;
        }
        return null;
    }

    public ImageView getUdeskRobotImg() {
        return udeskRobotImg;
    }

    public ImageView getUdeskBackImg() {
        return udeskBackImg;
    }

    /**
     * 设置titlebar 左边的点击事件
     *
     * @param listener
     */
    public void setLeftViewClick(OnClickListener listener) {
        if (udeskbackll != null) {
            udeskbackll.setOnClickListener(listener);
        }
    }

    /**
     * 设置titlebar 右边TextView的点击事件
     *
     * @param listener
     */
    public void setRightViewClick(OnClickListener listener) {
        if (udeskRightll != null) {
            udeskRightll.setOnClickListener(listener);
        }
    }

    /**
     * 设置titlebar  左边的显隐藏
     *
     * @param vis View.VISIBLE   View.GONE  View.INVISIBLE
     */
    public void setLeftLinearVis(int vis) {
        if (udeskbackll != null) {
            udeskbackll.setVisibility(vis);
        }
    }

    /**
     * 设置titlebar  机器人头像的显隐藏
     *
     * @param vis View.VISIBLE   View.GONE  View.INVISIBLE
     */
    public void setUdeskRobotImgVis(int vis) {
        if (udeskRobotImg != null) {
            udeskRobotImg.setVisibility(vis);
        }
    }

    /**
     * 设置titlebar  中间人工布局的的显隐藏
     *
     * @param vis View.VISIBLE   View.GONE  View.INVISIBLE
     */
    public void setUdeskMiddlellVis(int vis) {
        if (udeskMiddlell != null) {
            udeskMiddlell.setVisibility(vis);
        }
    }

    /**
     * 设置titlebar  中间顶部文字的的显隐藏
     *
     * @param vis View.VISIBLE   View.GONE  View.INVISIBLE
     */
    public void setUdeskTopTextVis(int vis) {
        if (udeskTopText != null) {
            udeskTopText.setVisibility(vis);
        }
    }

    /**
     * 设置titlebar  中间底部文字的的显隐藏
     *
     * @param vis View.VISIBLE   View.GONE  View.INVISIBLE
     */
    public void setUdeskBottomTextVis(int vis) {
        if (udeskBottomText != null) {
            udeskBottomText.setVisibility(vis);
        }
    }

    /**
     * 设置titlebar右边的布局显隐藏
     *
     * @param vis View.VISIBLE   View.GONE  View.INVISIBLE
     */
    public void setRightViewVis(int vis) {
        if (udeskRightll != null) {
            udeskRightll.setVisibility(vis);
        }
    }

    /**
     * 设置titlebar右边的TextView显隐藏
     *
     * @param vis View.VISIBLE   View.GONE  View.INVISIBLE
     */
    public void setRightTextVis(int vis) {
        if (udeskRight != null) {
            udeskRight.setVisibility(vis);
        }
    }

    /**
     * 设置titlebar右边的ImageView显隐藏
     *
     * @param vis View.VISIBLE   View.GONE  View.INVISIBLE
     */
    public void setudeskTransferImgVis(int vis) {
        if (udeskTransferImg != null) {
            udeskTransferImg.setVisibility(vis);
        }
    }

    /**
     * 设置titlebar 中间底部文字显示的内容
     *
     * @param string
     */
    public void setTopTextSequence(String string) {
        if (udeskTopText != null) {
            udeskTopText.setText(string);
        }
    }

    /**
     * 设置titlebar 中间底部文字显示的内容
     *
     * @param string
     */
    public void setBottomTextSequence(String string) {
        if (udeskBottomText != null) {
            udeskBottomText.setText(string);
        }
    }


    /**
     * 设置titlebar右边的TextView 显示的内容
     *
     * @param string
     */
    public void setRightTextSequence(String string) {
        if (udeskRight != null) {
            udeskRight.setText(string);
        }
    }


//    /**
//     * 设置titlebar左边的TextView 显示的内容
//     * @param string
//     */
//    public void setLeftTextSequence(String string){
//    	if(udeskLeft != null){
//    		udeskLeft.setText(string);
//    	}
//    }


//    public ImageView getudeskStateImg(){
//        udeskStateImg.setVisibility(VISIBLE);
//        return udeskStateImg;
//    }

}
