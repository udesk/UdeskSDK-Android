package udesk.udesksocket.mode.event;

import udesk.udesksocket.mode.EventHead;

/**
 * author : ${揭军平}
 * time   : 2017/11/23
 * desc   :
 * version: 1.0
 */

public class EvtPreAnswer extends EventHead {

    private  Object method;
    private  Object from_user_id; // 主叫方
    private  Object to_user_id;// 被叫方
    private  Object channel_id;

    public Object getMethod() {
        return method;
    }

    public void setMethod(Object method) {
        this.method = method;
    }

    public Object getFrom_user_id() {
        return from_user_id;
    }

    public void setFrom_user_id(Object from_user_id) {
        this.from_user_id = from_user_id;
    }

    public Object getTo_user_id() {
        return to_user_id;
    }

    public void setTo_user_id(Object to_user_id) {
        this.to_user_id = to_user_id;
    }

    public Object getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(Object channel_id) {
        this.channel_id = channel_id;
    }
}
