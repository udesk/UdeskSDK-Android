package cn.udesk.model;

import java.io.Serializable;
import java.util.List;

import cn.udesk.UdeskUtil;
import udesk.core.utils.UdeskUtils;

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
        return UdeskUtils.objectToBoolean(enabled);
    }

    public void setEnabled(Object enabled) {
        this.enabled = enabled;
    }

    public boolean getRemark_enabled() {
        return UdeskUtils.objectToBoolean(remark_enabled);
    }

    public void setRemark_enabled(Object remark_enabled) {
        this.remark_enabled = remark_enabled;
    }

    public String getRemark() {
        return UdeskUtils.objectToString(remark);
    }

    public void setRemark(Object remark) {
        this.remark = remark;
    }

    public String getName() {
        return UdeskUtils.objectToString(name);
    }

    public void setName(Object name) {
        this.name = name;
    }

    public String getTitle() {
        return UdeskUtils.objectToString(title);
    }

    public void setTitle(Object title) {
        this.title = title;
    }

    public String getDesc() {
        return UdeskUtils.objectToString(desc);
    }

    public void setDesc(Object desc) {
        this.desc = desc;
    }

    public String getType() {
        return UdeskUtils.objectToString(show_type);
    }

    public void setType(Object type) {
        this.show_type = type;
    }

    public int getDefault_option_id() {
        return UdeskUtils.objectToInt(default_option_id);
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
