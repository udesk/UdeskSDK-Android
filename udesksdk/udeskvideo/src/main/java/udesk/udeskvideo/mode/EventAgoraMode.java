package udesk.udeskvideo.mode;

import java.util.Map;

/**
 * author : ${揭军平}
 * time   : 2017/12/06
 * desc   :
 * version: 1.0
 */

public class EventAgoraMode {
    private String method;
    private Map<String,Object> values;


    public EventAgoraMode(String method, Map<String, Object> values) {
        this.method = method;
        this.values = values;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, Object> getValues() {
        return values;
    }
}
