package cn.udesk.model;

import java.io.Serializable;
import java.util.List;

import udesk.core.utils.UdeskUtils;

public class OptionsModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Object id;
    private Object enabled; //是否启用
    private Object text; //名称
    private Object desc;//选项（文本 表情 五星 有区别）
    private Object remark_option;//备注
    private List<Tag> tags;//标签

//    boolean isCheck = false;


    public OptionsModel() {
    }

    public OptionsModel(Object id, Object enabled, Object text, Object desc, Object remark_option) {
        this.id = id;
        this.enabled = enabled;
        this.text = text;
        this.desc = desc;
        this.remark_option = remark_option;
    }

    public int getId() {
        return UdeskUtils.objectToInt(id);
    }

    public void setId(Object id) {
        this.id = id;
    }

    public boolean getEnabled() {
        return UdeskUtils.objectToBoolean(enabled);
    }

    public void setEnabled(Object enabled) {
        this.enabled = enabled;
    }

    public String getText() {
        return UdeskUtils.objectToString(text);
    }

    public void setText(Object text) {
        this.text = text;
    }

    public String getDesc() {
        return UdeskUtils.objectToString(desc);
    }

    public void setDesc(Object desc) {
        this.desc = desc;
    }

    public String getRemark_option() {
        return UdeskUtils.objectToString(remark_option);
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
