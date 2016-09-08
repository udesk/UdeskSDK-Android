package cn.udesk.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.udesk.R;

/**
 * 加载控件
 *
 */
public class UdeskLoadingView extends LinearLayout {

	private TextView loadingTxt;
  
    public UdeskLoadingView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public UdeskLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UdeskLoadingView(Context context) {
        super(context);
        init(context);
    }
    
    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.udesk_loading_view, this);
        loadingTxt = (TextView) findViewById(R.id.udesk_loading_txt);
        
    }

    public void setLoadingTxt(String string){
    	if(loadingTxt != null){
    		loadingTxt.setText(string);
    	}
    }
    
}
