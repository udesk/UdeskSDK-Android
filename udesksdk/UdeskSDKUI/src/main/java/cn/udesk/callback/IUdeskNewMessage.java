package cn.udesk.callback;

import cn.udesk.model.MsgNotice;

public interface IUdeskNewMessage {

    void onNewMessage(MsgNotice msgNotice );
}
