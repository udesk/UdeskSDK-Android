package cn.udesk.itemview;

import android.content.Context;
import android.content.Intent;
import android.text.style.ClickableSpan;
import android.view.View;

import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.activity.UdeskWebViewUrlAcivity;
import udesk.core.UdeskConst;

import static android.util.Patterns.PHONE;
import static android.util.Patterns.WEB_URL;

/**
 * 重写ClickableSpan 实现富文本点击事件跳转到UdeskWebViewUrlAcivity界面
 */
public class MyURLSpan  extends ClickableSpan {

    private final String mUrl;

    private Context mContext;

    public MyURLSpan(String url,Context context) {
        this.mUrl = url;
        this.mContext = context;
    }

    @Override
    public void onClick(View widget) {
        try {
            if (WEB_URL.matcher(mUrl).find()) {
                Intent intent = new Intent(mContext, UdeskWebViewUrlAcivity.class);
                intent.putExtra(UdeskConst.WELCOME_URL, mUrl);
                mContext.startActivity(intent);
            } else if (PHONE.matcher(mUrl).find()) {
                String phone = mUrl.toLowerCase();
                if (!phone.startsWith("tel:")) {
                    phone = "tel:" + mUrl;
                }
                ((UdeskChatActivity) mContext).callphone(phone);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }
}
