package cn.udesk.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.udesk.R;


/**
 * udesk 标题栏
 *
 */
public class UdeskTitleBar extends RelativeLayout{

    protected RelativeLayout udeskRootView;
    protected TextView udeskLeft;
    protected TextView udeskRight;
    protected TextView udeskTitle;
    protected TextView udeskState;
  

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
        udeskLeft = (TextView) findViewById(R.id.udesk_titlebar_left);
        udeskRight = (TextView) findViewById(R.id.udesk_titlebar_right);
        udeskTitle = (TextView) findViewById(R.id.udesk_titlebar_title);
        udeskState = (TextView) findViewById(R.id.udesk_agent_state);
        
    }

    /**
     * 设置titlebar 左边TextView的点击事件
     * @param listener
     */
    public void setLeftViewClick(OnClickListener listener){
    	if(udeskLeft != null){
    		udeskLeft.setOnClickListener(listener);
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
     * 设置titlebar  左边的TextView显隐藏
     * @param vis  View.VISIBLE   View.GONE  View.INVISIBLE
     */
    public void setLeftTextVis(int vis){
    	if(udeskLeft != null){
    		udeskLeft.setVisibility(vis);
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
     * 设置titlebar  标题的TextView显隐藏
     * @param vis
     */
    public void setTitleTextVis(int vis){
    	if(udeskTitle != null){
    		udeskTitle.setVisibility(vis);
    	}
    }
    
   /**
    * 设置titlebar  在线状态的TextView的显隐藏
    * @param vis
    */
    public void setStateTextVis(int vis){
    	if(udeskState != null){
    		udeskState.setVisibility(vis);
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
    
    /**
     * 设置titlebar标题的TextView显示的内容
     * @param string
     */
    public void setTitleTextSequence(String string){
    	if(udeskTitle != null){
    		udeskTitle.setText(string);
    	}
    }
    
    /**
     * 设置titlebar状态的TextView显示的内容
     * @param string
     */
    public void setStateTextSequence(String string){
    	if(udeskState != null){
    		udeskState.setText(string);
    	}
    }
    
}
