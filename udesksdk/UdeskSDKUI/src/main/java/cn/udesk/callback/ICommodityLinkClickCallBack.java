package cn.udesk.callback;

import android.content.Context;

import cn.udesk.aac.UdeskViewMode;
import cn.udesk.model.UdeskCommodityItem;

/**
 * 支持自定义功能按钮后 点击事件回调 和直接发送文本,图片,视频,文件,地理位置,商品信息
 */

public interface ICommodityLinkClickCallBack {

    void callBack(Context context, UdeskViewMode viewMode, UdeskCommodityItem item);
}
