package cn.udesk.model;

import java.util.List;

import udesk.core.model.BaseMode;
import udesk.core.utils.UdeskUtils;

public class InitCustomerBean extends BaseMode {

    private IMInfo im;//xmpp链接使用

    private UploadService uploadService;

    private SurveyOptionsModel im_survey;

    private ImSetting ImSetting;

    private List<AgentGroupNode> im_group;

    private Object black_list_notice;

    private Customer customer;

    private PreSession pre_session;

    //  status: chatting|queuing|pre_session|init
    // - init 状态下,客户端看是进机器人还是无消息
    // - 客服信息 当status为 chatting,直接返回 agent 的结果,如果没有,还是需要请求 agent(超时重新请求)
    // - chatting 状态为原来 in_session: true
    // - pre_session 为当前状态已经处在无消息,如果有 pre_session_id,可以直接用此id来往下
    // - init 状态下,show_pre_session 为true,需要进无消息
    private Object status;

    private Agent agent;

    private Object im_sub_session_id;

    public IMInfo getIm() {
        return im;
    }

    public void setIm(IMInfo im) {
        this.im = im;
    }

    public UploadService getUploadService() {
        return uploadService;
    }

    public void setUploadService(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    public SurveyOptionsModel getIm_survey() {
        return im_survey;
    }

    public void setIm_survey(SurveyOptionsModel im_survey) {
        this.im_survey = im_survey;
    }

    public ImSetting getImSetting() {
        return ImSetting;
    }

    public void setImSetting(cn.udesk.model.ImSetting imSetting) {
        ImSetting = imSetting;
    }

    public List<AgentGroupNode> getIm_group() {
        return im_group;
    }

    public void setIm_group(List<AgentGroupNode> im_group) {
        this.im_group = im_group;
    }

    public String getBlack_list_notice() {
        return UdeskUtils.objectToString(black_list_notice);
    }

    public void setBlack_list_notice(Object black_list_notice) {
        this.black_list_notice = black_list_notice;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public PreSession getPre_session() {
        return pre_session;
    }

    public void setPre_session(PreSession pre_session) {
        this.pre_session = pre_session;
    }

    public String getStatus() {
        return UdeskUtils.objectToString(status);
    }

    public void setStatus(Object status) {
        this.status = status;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Object getIm_sub_session_id() {
        return im_sub_session_id;
    }

    public void setIm_sub_session_id(Object im_sub_session_id) {
        this.im_sub_session_id = im_sub_session_id;
    }
}
