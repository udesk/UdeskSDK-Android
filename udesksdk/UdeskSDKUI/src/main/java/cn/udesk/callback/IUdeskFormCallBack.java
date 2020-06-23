package cn.udesk.callback;

import android.content.Context;

/**
 * 留言界面的回调接口
 * 如果不使用udesk 表单留言页面 可以通过设置接口回调单独处理
 */

public interface IUdeskFormCallBack {

    void toLuachForm(Context context);
}
