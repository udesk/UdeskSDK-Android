package cn.udesk.model;

import cn.udesk.UdeskUtil;
import udesk.core.utils.UdeskUtils;

public class PreSession {
    //公司是否开启
    private Object pre_session;
    // 是否开启无消息过滤
    private Object show_pre_session;
    // 无消息过滤显示标题
    private Object pre_session_title;
    // 无消息过滤会话的id
    private Object pre_session_id;

    public boolean getShow_pre_session() {
        return UdeskUtils.objectToBoolean(show_pre_session);
    }

    public void setShow_pre_session(Object show_pre_session) {
        this.show_pre_session = show_pre_session;
    }

    public String getPre_session_title() {
        return UdeskUtils.objectToString(pre_session_title);
    }

    public void setPre_session_title(Object pre_session_title) {
        this.pre_session_title = pre_session_title;
    }

    public String getPre_session_id() {
        return UdeskUtils.objectToString(pre_session_id);
    }

    public void setPre_session_id(Object pre_session_id) {
        this.pre_session_id = pre_session_id;
    }

    public boolean getPre_session() {
        return UdeskUtils.objectToBoolean(pre_session);
    }

    public void setPre_session(Object pre_session) {
        this.pre_session = pre_session;
    }
}
