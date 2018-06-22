package udesk.udesksocket.mode;

/**
 * author : ${揭军平}
 * time   : 2017/11/22
 * desc   : 响应消息标准头部
 * version: 1.0
 */

public class EventAck {

    private Object ver;//协议版本号
    private Object msg_id;// 响应消息ID与发送的消息ID相同
    private Object msg_type;
    private Object timestamp;
    private Object method;
    private Object seq_id;


    public Object getVer() {
        return ver;
    }

    public void setVer(Object ver) {
        this.ver = ver;
    }

    public Object getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(Object msg_id) {
        this.msg_id = msg_id;
    }

    public Object getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(Object msg_type) {
        this.msg_type = msg_type;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public Object getMethod() {
        return method;
    }

    public void setMethod(Object method) {
        this.method = method;
    }

    public Object getSeq_id() {
        return seq_id;
    }

    public void setSeq_id(Object seq_id) {
        this.seq_id = seq_id;
    }
}
