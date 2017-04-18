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
 *
 */
public class UdeskTitleBar extends RelativeLayout{

    protected RelativeLayout udeskRootView;
    protected LinearLayout udeskbackll;
    protected ImageView udeskBackImg;
    protected TextView udeskLeft;
    protected TextView udeskRight;
    protected ImageView udeskStateImg;
    protected ImageView udeskTransferImg;


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
    
    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.udesk_title_bar, this);
        udeskRootView = (RelativeLayout) findViewById(R.id.udesk_root);
        udeskLeft = (TextView) findViewById(R.id.udesk_content);
        udeskRight = (TextView) findViewById(R.id.udesk_titlebar_right);
        udeskbackll = (LinearLayout) findViewById(R.id.udesk_back_linear);
        udeskBackImg = (ImageView) findViewById(R.id.udesk_back_img);
        udeskStateImg = (ImageView) findViewById(R.id.udesk_status);
        udeskTransferImg =(ImageView)findViewById(R.id.udesk_transfer_agent);
    }

    public  RelativeLayout  getRootView(){
        if (udeskRootView != null){
            return  udeskRootView;
        }
        return null;
    }

    public  TextView getLeftTextView(){
        if (udeskLeft != null){
            return  udeskLeft;
        }
        return null;
    }

    public  TextView getRightTextView(){
        if (udeskRight != null){
            return  udeskRight;
        }
        return null;
    }



    public ImageView getUdeskBackImg() {
        return udeskBackImg;
    }

    /**
     * 设置titlebar 左边的点击事件
     * @param listener
     */
    public void setLeftViewClick(OnClickListener listener){
    	if(udeskbackll != null){
            udeskbackll.setOnClickListener(listener);
    	}
    }
    
    /**
     * 设置titlebar 右边TextView的点击事件
     * @param listener
     */
    public void setRightViewClick(OnClickListener listener){
    	if(udeskRight != null){
    		udeskRight.setOnClickListener(listener);
    	}
    }
    /**
     * 设置titlebar  左边的显隐藏
     * @param vis  View.VISIBLE   View.GONE  View.INVISIBLE
     */
    public void setLeftLinearVis(int vis){
    	if(udeskbackll != null){
            udeskbackll.setVisibility(vis);
    	}
    }
    
    /**
     * 设置titlebar右边的TextView显隐藏
     * @param vis  View.VISIBLE   View.GONE  View.INVISIBLE
     */
    public void setRightTextVis(int vis){
    	if(udeskRight != null){
    		udeskRight.setVisibility(vis);
    	}
    }

    /**
     * 设置titlebar左边的TextView 显示的内容
     * @param string
     */
    public void setLeftTextSequence(String string){
    	if(udeskLeft != null){
    		udeskLeft.setText(string);
    	}
    }
    
    /**
     * 设置titlebar右边的TextView 显示的内容
     * @param string
     */
    public void setRightTextSequence(String string){
    	if(udeskRight != null){
    		udeskRight.setText(string);
    	}
    }
    

    public void setudeskTransferImgVis(int vis){
        if(udeskTransferImg != null){
            udeskTransferImg.setVisibility(vis);
        }
    }

    public ImageView getudeskStateImg(){
        udeskStateImg.setVisibility(VISIBLE);
        return udeskStateImg;
    }
    
}
