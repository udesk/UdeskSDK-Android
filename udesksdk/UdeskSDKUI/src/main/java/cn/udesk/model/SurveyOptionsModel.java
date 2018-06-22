package cn.udesk.model;

import java.io.Serializable;
import java.util.List;

import cn.udesk.UdeskUtil;

public class SurveyOptionsModel implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Object enabled;
    private Object remark_enabled;
    private Object remark;
    private Object name;
    private Object title;
    private Object desc;
    // text|expression|star
    private Object show_type;
    private Object default_option_id;

    List<OptionsModel> options;

    public boolean getEnabled() {
        return UdeskUtil.objectToBoolean(enabled);
    }

    public void setEnabled(Object enabled) {
        this.enabled = enabled;
    }

    public boolean getRemark_enabled() {
        return UdeskUtil.objectToBoolean(remark_enabled);
    }

    public void setRemark_enabled(Object remark_enabled) {
        this.remark_enabled = remark_enabled;
    }

    public String getRemark() {
        return UdeskUtil.objectToString(remark);
    }

    public void setRemark(Object remark) {
        this.remark = remark;
    }

    public String getName() {
        return UdeskUtil.objectToString(name);
    }

    public void setName(Object name) {
        this.name = name;
    }

    public String getTitle() {
        return UdeskUtil.objectToString(title);
    }

    public void setTitle(Object title) {
        this.title = title;
    }

    public String getDesc() {
        return UdeskUtil.objectToString(desc);
    }

    public void setDesc(Object desc) {
        this.desc = desc;
    }

    public String getType() {
        return UdeskUtil.objectToString(show_type);
    }

    public void setType(Object type) {
        this.show_type = type;
    }

    public int getDefault_option_id() {
        return UdeskUtil.objectToInt(default_option_id);
    }

    public void setDefault_option_id(Object default_option_id) {
        this.default_option_id = default_option_id;
    }

    public List<OptionsModel> getOptions() {
        return options;
    }

    public void setOptions(List<OptionsModel> options) {
        this.options = options;
    }
}
