package udesk.udesksocket.mode.manager;

import udesk.udesksocket.mode.RepHead;

/**
 * author : ${揭军平}
 * time   : 2017/11/22
 * desc   :
 * version: 1.0
 */

public class GetAttrRep extends RepHead {

    //login
    private Object method;
    private Object attr_key;
    private Object attr_val;


    public Object getMethod() {
        return method;
    }

    public void setMethod(Object method) {
        this.method = method;
    }

    public Object getAttr_key() {
        return attr_key;
    }

    public void setAttr_key(Object attr_key) {
        this.attr_key = attr_key;
    }

    public Object getAttr_val() {
        return attr_val;
    }

    public void setAttr_val(Object attr_val) {
        this.attr_val = attr_val;
    }
}
