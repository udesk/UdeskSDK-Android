package cn.udesk.model;

import java.io.Serializable;
import java.util.List;

import cn.udesk.UdeskUtil;

public class OptionsModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Object id;
    private Object enabled;
    private Object text;
    private Object desc;
    private Object remark_option;
    private List<Tag> tags;

//    boolean isCheck = false;


    public int getId() {
        return UdeskUtil.objectToInt(id);
    }

    public void setId(Object id) {
        this.id = id;
    }

    public boolean getEnabled() {
        return UdeskUtil.objectToBoolean(enabled);
    }

    public void setEnabled(Object enabled) {
        this.enabled = enabled;
    }

    public String getText() {
        return UdeskUtil.objectToString(text);
    }

    public void setText(Object text) {
        this.text = text;
    }

    public String getDesc() {
        return UdeskUtil.objectToString(desc);
    }

    public void setDesc(Object desc) {
        this.desc = desc;
    }

    public String getRemark_option() {
        return UdeskUtil.objectToString(remark_option);
    }

    public void setRemark_option(Object remark_option) {
        this.remark_option = remark_option;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

//    public boolean isCheck() {
//        return isCheck;
//    }
//
//    public void setCheck(boolean isCheck) {
//        this.isCheck = isCheck;
//    }


}
