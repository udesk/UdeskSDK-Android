package udesk.udesksocket.mode.manager;

import udesk.udesksocket.mode.RepHead;

/**
 * author : ${揭军平}
 * time   : 2017/11/22
 * desc   :
 * version: 1.0
 */

public class GetUserStateRep extends RepHead {

    //login
    private Object method;
    private Object cur_state;



    public Object getMethod() {
        return method;
    }

    public void setMethod(Object method) {
        this.method = method;
    }

    public Object getCur_state() {
        return cur_state;
    }

    public void setCur_state(Object cur_state) {
        this.cur_state = cur_state;
    }


}
