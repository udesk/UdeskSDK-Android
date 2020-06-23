package cn.udesk.callback;

import android.content.Context;

import cn.udesk.aac.UdeskViewMode;
import cn.udesk.model.NavigationMode;

/**
 *  支持客户在导航处添加自定义按钮的点击回调事件
 */

public interface INavigationItemClickCallBack {

    /**
     * @param context
     * @param udeskViewMode 支持直接后续操作发送  文本,图片,视频,文件,地理位置,商品信息
     */
    void callBack(Context context, UdeskViewMode udeskViewMode, NavigationMode navigationMode,String currentView);
}
