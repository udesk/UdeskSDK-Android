package udesk.udesksocket.mode.invite;

import udesk.udesksocket.mode.RepHead;

/**
 * author : ${揭军平}
 * time   : 2017/11/22
 * desc   :
 * version: 1.0
 */

public class GetChannelTokenRep extends RepHead {

    //login
    private Object method;
    private Object channel_id;
    private Object channel_token;
    private Object agora_uid;


    public Object getMethod() {
        return method;
    }

    public void setMethod(Object method) {
        this.method = method;
    }

    public Object getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(Object channel_id) {
        this.channel_id = channel_id;
    }

    public Object getChannel_token() {
        return channel_token;
    }

    public void setChannel_token(Object channel_token) {
        this.channel_token = channel_token;
    }

    public Object getAgora_uid() {
        return agora_uid;
    }

    public void setAgora_uid(Object agora_uid) {
        this.agora_uid = agora_uid;
    }
}
