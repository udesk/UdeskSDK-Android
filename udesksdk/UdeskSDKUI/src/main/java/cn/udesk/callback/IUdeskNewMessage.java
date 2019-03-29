package cn.udesk.callback;

import android.content.Context;

import cn.udesk.model.MsgNotice;

public interface IUdeskNewMessage {

    void onNewMessage(MsgNotice msgNotice );
}
