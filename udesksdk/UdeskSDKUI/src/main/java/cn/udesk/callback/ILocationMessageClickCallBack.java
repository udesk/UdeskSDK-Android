package cn.udesk.callback;

import android.content.Context;

/**
 *  点击地理位置信息的回调接口
 */

public interface ILocationMessageClickCallBack {

    void luanchMap(Context context, double latitude, double longitude, String selctLoactionValue);
}
