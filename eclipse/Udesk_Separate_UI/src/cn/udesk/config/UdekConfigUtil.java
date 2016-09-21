package cn.udesk.config;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

/**
 * Created by user on 2016/8/12.
 */
public class UdekConfigUtil {
    /**
     * 设置字体的颜色
     * @param codeResId
     * @param textViews
     */
    public static void setUITextColor( int codeResId, TextView... textViews) {
        Context context = null;
        if (textViews != null && textViews.length > 0) {
            context = textViews[0].getContext();
        }
        if (context != null) {
            if (UdeskConfig.DEFAULT != codeResId) {
                int color = context.getResources().getColor(codeResId);
                if (textViews != null) {
                    for (TextView textView : textViews) {
                        if (textView != null){
                            textView.setTextColor(color);
                        }
                    }
                }
            }
        }
    }


    /**
     * 处理自定义颜色背景色
     *
     * @param view          包含背景图片的控件
     * @param codeResId     通过java代码的方式自定义的id
     */
    public static void setUIbgDrawable( int codeResId , View view) {
        Context context = view.getContext();
        if (UdeskConfig.DEFAULT != codeResId) {
            Drawable drawable=context.getResources().getDrawable(codeResId);
            setBackground(view, drawable);
        }

    }

    private static void setBackground(View v, Drawable bgDrawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            v.setBackground(bgDrawable);
        } else {
            v.setBackgroundDrawable(bgDrawable);
        }
    }

}
