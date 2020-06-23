package udesk.udesksocket;

/**
 * author : ${揭军平}
 * time   : 2017/12/04
 * desc   :
 * version: 1.0
 */

public class EventScoketMode<T> {
    private String method;
    private String msg_type;
    private T data;

    public EventScoketMode(String method, String msg_type, T data) {
        this.method = method;
        this.msg_type = msg_type;
        this.data = data;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
