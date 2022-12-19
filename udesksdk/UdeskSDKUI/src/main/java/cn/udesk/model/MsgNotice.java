package cn.udesk.model;

import udesk.core.model.MessageInfo;

/**
 * Created by sks on 2016/6/8.
 */
public class MsgNotice extends MessageInfo {
    /**
     * 消息的ID
     */
    String msgId  = "";
    /**
     *  消息类型
     *  图片消息：image
     *  语音消息：audio
     *  文本消息：message
     */
    String msgType = "";
    /**
     * 消息的内容
     */
    String content = "";

    public MsgNotice() {

    }

    public MsgNotice(String msgId, String msgType, String content) {
        this.msgId = msgId;
        this.msgType = msgType;
        this.content = content;
    }

    @Override
    public String getMsgId() {
        return msgId;
    }

    @Override
    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
