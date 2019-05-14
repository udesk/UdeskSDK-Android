package cn.udesk.upload;

import udesk.core.model.MessageInfo;

/**
 * author : ${揭军平}
 * time   : 2018/01/05
 * desc   :
 * version: 1.0
 */

public interface UdeskUploadCallBack {
    //上传文件成功后的 可下载的url地址
    void onSuccess(MessageInfo messageInfo, String url);

    void progress(MessageInfo messageInfo, String key, float percent);

    void onFailure(MessageInfo messageInfo, String key);
}
