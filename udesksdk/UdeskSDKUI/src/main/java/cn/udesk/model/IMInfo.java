package cn.udesk.model;

import cn.udesk.UdeskUtil;
import udesk.core.utils.UdeskUtils;

public class IMInfo {

    private Object username;
    private Object password;
    private Object server;
    private Object port = 5222;


    public String getUsername() {
        return UdeskUtils.objectToString(username);
    }

    public void setUsername(Object username) {
        this.username = username;
    }

    public String getPassword() {
        return UdeskUtils.objectToString(password);
    }

    public void setPassword(Object password) {
        this.password = password;
    }

    public String getServer() {
        return UdeskUtils.objectToString(server);
    }

    public void setServer(Object server) {
        this.server = server;
    }

    public int getPort() {
        return UdeskUtils.objectToInt(port);
    }

    public void setPort(Object port) {
        this.port = port;
    }
}
