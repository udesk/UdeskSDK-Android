package udesk.udesksocket.mode.manager;

import udesk.udesksocket.mode.RepHead;

/**
 * author : ${揭军平}
 * time   : 2017/11/22
 * desc   :
 * version: 1.0
 */

public class LoginRep extends RepHead {

    //login
    private Object method;
    private Object uid; // server分配给客户端的全局唯标识
    private Object cur_state;

    public Object getMethod() {
        return method;
    }

    public void setMethod(Object method) {
        this.method = method;
    }

    public Object getUid() {
        return uid;
    }

    public void setUid(Object uid) {
        this.uid = uid;
    }

    public Object getCur_state() {
        return cur_state;
    }

    public void setCur_state(Object cur_state) {
        this.cur_state = cur_state;
    }

}
