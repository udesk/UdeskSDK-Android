package udesk.udesksocket;

/**
 * websoket消息的回家
 */
public interface SigtokenCallBack {


    void response(String string);

    void failure();
}
