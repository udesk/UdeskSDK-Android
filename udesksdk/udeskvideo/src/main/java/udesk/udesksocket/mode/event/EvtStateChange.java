package udesk.udesksocket.mode.event;

import udesk.udesksocket.mode.EventHead;

/**
 * author : ${揭军平}
 * time   : 2017/11/23
 * desc   :
 * version: 1.0
 */

public class EvtStateChange extends EventHead {

    private  Object method;
    private  Object cur_state;


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
