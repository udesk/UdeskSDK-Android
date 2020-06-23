package udesk.udesksocket.mode.manager;

import udesk.udesksocket.mode.RepHead;

/**
 * author : ${揭军平}
 * time   : 2017/11/22
 * desc   :
 * version: 1.0
 */

public class GetAttrAllRep extends RepHead {

    //login
    private Object method;
    private Object attrs;

    public Object getMethod() {
        return method;
    }

    public void setMethod(Object method) {
        this.method = method;
    }

    public Object getAttrs() {
        return attrs;
    }

    public void setAttrs(Object attrs) {
        this.attrs = attrs;
    }
}
