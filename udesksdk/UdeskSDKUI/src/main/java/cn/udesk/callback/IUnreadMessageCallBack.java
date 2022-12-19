package cn.udesk.callback;

import udesk.core.model.MessageInfo;

public interface IUnreadMessageCallBack {
    /**
     * 获取当前未读消息
     * @param unreadMessage
     */
    void onReceiveUnreadMessage(MessageInfo unreadMessage);

    /**
     * 消息状态更改
     */
    void onUnreadMessagesStatusChange();
}
