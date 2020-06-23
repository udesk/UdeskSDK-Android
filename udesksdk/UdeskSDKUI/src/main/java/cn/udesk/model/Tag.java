package cn.udesk.model;

import java.io.Serializable;

import cn.udesk.UdeskUtil;
import udesk.core.utils.UdeskUtils;

/**
 * Created by user on 2018/3/26.
 */

public class Tag implements Serializable {

    private Object text;
    private boolean isCheck;

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public String getText() {
        return UdeskUtils.objectToString(text);
    }

    public void setText(Object text) {
        this.text = text;
    }
}
