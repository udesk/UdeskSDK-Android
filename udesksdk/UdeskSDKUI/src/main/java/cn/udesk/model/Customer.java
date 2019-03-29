package cn.udesk.model;

import cn.udesk.UdeskUtil;
import udesk.core.utils.UdeskUtils;

public class Customer {

    private Object id;
    private Object nick_name;
    private Object is_blocked;

    public String getId() {
        return UdeskUtils.objectToString(id);
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getNick_name() {
        return nick_name;
    }

    public void setNick_name(Object nick_name) {
        this.nick_name = nick_name;
    }

    public boolean getIs_blocked() {
        return UdeskUtils.objectToBoolean(is_blocked);
    }

    public void setIs_blocked(Object is_blocked) {
        this.is_blocked = is_blocked;
    }
}
