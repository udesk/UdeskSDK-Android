package cn.udesk.itemview;

import android.content.Context;
import android.content.Intent;
import android.text.style.ClickableSpan;
import android.view.View;

import cn.udesk.UdeskSDKManager;
import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.activity.UdeskWebViewUrlAcivity;
import udesk.core.UdeskConst;

import static android.util.Patterns.PHONE;
import static android.util.Patterns.WEB_URL;

/**
 * 文本消息的url事件拦截处理。  客户设置了事件则走客户的事件，没走默认弹出界面
 */
public class TxtURLSpan extends ClickableSpan {

    private final String mUrl;
    private Context mContext;

    public TxtURLSpan(String url,Context context) {
        this.mUrl = url;
        this.mContext = context;
    }


    @Override
    public void onClick(View widget) {
        try {
            if (UdeskSDKManager.getInstance().getUdeskConfig().txtMessageClick != null) {
                UdeskSDKManager.getInstance().getUdeskConfig().txtMessageClick.txtMsgOnclick(mUrl);
            } else if (WEB_URL.matcher(mUrl).find()) {
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
        }

    }
}
