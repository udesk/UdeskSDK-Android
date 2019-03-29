package cn.udesk.model;

import udesk.core.utils.UdeskUtils;

public class ImSetting {

    private Object enable_im_group;//导航组是否开启
    private Object leave_message_type;//留言模式
    private Object is_worktime;//是否工作时间 不是的话进入留言
    private Object enable_web_im_feedback;//是否开启留言
    private Object enable_im_survey;//是否开启满意度调查
    private Object im_survey_show_type;//满意度调查样式 （文本 表情 五星）
    private Object investigation_when_leave;//离开时是否开启满意度调查
    private Object no_reply_hint;//无留言文案
    private Object vcall;
    private Object vc_app_id;
    private Object sdk_vcall;
    private Object vcall_token_url;
    private Object agora_app_id;
    private Object server_url;
    private Object leave_message_guide;//有留言 引导语

    private Robot robot;

    public boolean getEnable_im_group() {
        return UdeskUtils.objectToBoolean(enable_im_group);
    }

    public void setEnable_im_group(Object enable_im_group) {
        this.enable_im_group = enable_im_group;
    }

    public String getLeave_message_type() {
        return UdeskUtils.objectToString(leave_message_type);
    }

    public void setLeave_message_type(Object leave_message_type) {
        this.leave_message_type = leave_message_type;
    }

    public boolean getIs_worktime() {
        return UdeskUtils.objectToBoolean(is_worktime);
    }

    public void setIs_worktime(Object is_worktime) {
        this.is_worktime = is_worktime;
    }

    public boolean getEnable_web_im_feedback() {
        return UdeskUtils.objectToBoolean(enable_web_im_feedback);
    }

    public void setEnable_web_im_feedback(Object enable_web_im_feedback) {
        this.enable_web_im_feedback = enable_web_im_feedback;
    }

    public boolean getEnable_im_survey() {
        return UdeskUtils.objectToBoolean(enable_im_survey);
    }

    public void setEnable_im_survey(Object enable_im_survey) {
        this.enable_im_survey = enable_im_survey;
    }

    public Object getIm_survey_show_type() {
        return im_survey_show_type;
    }

    public void setIm_survey_show_type(Object im_survey_show_type) {
        this.im_survey_show_type = im_survey_show_type;
    }

    public boolean getInvestigation_when_leave() {
        return UdeskUtils.objectToBoolean(investigation_when_leave);
    }

    public void setInvestigation_when_leave(Object investigation_when_leave) {
        this.investigation_when_leave = investigation_when_leave;
    }

    public String getNo_reply_hint() {
        return UdeskUtils.objectToString(no_reply_hint);
    }

    public void setNo_reply_hint(Object no_reply_hint) {
        this.no_reply_hint = no_reply_hint;
    }

    public boolean getVcall() {
        return UdeskUtils.objectToBoolean(vcall);
    }

    public void setVcall(Object vcall) {
        this.vcall = vcall;
    }

    public String getVcall_token_url() {
        return UdeskUtils.objectToString(vcall_token_url);
    }

    public void setVcall_token_url(Object vcall_token_url) {
        this.vcall_token_url = vcall_token_url;
    }

    public String getVc_app_id() {
        return UdeskUtils.objectToString(vc_app_id);
    }

    public void setVc_app_id(Object vc_app_id) {
        this.vc_app_id = vc_app_id;
    }

    public boolean getSdk_vcall() {
        return UdeskUtils.objectToBoolean(sdk_vcall);
    }

    public void setSdk_vcall(Object sdk_vcall) {
        this.sdk_vcall = sdk_vcall;
    }

    public String getAgora_app_id() {
        return UdeskUtils.objectToString(agora_app_id);
    }

    public void setAgora_app_id(Object agora_app_id) {
        this.agora_app_id = agora_app_id;
    }

    public String getServer_url() {
        return UdeskUtils.objectToString(server_url);
    }

    public void setServer_url(Object server_url) {
        this.server_url = server_url;
    }

    public String getLeave_message_guide() {
        return UdeskUtils.objectToString(leave_message_guide);
    }

    public void setLeave_message_guide(Object leave_message_guide) {
        this.leave_message_guide = leave_message_guide;
    }

    public Robot getRobot() {
        return robot;
    }

    public void setRobot(Robot robot) {
        this.robot = robot;
    }
}
