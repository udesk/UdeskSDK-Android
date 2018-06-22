package udesk.udesksocket.mode.invite;

import udesk.udesksocket.mode.RepHead;

/**
 * author : ${揭军平}
 * time   : 2017/11/22
 * desc   :
 * version: 1.0
 */

public class AnswerRep extends RepHead {

    //login
    private Object method;
    private Object channel_id;


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
}
