package cn.udesk.callback;

import android.content.Context;

/**
 * 点击事件
 */

public interface IUdeskStructMessageCallBack {

    /**
     * @param context   上下文
     * @param josnValue 接口配置的字符串
     */
    void structMsgCallBack(Context context, String josnValue);
}
