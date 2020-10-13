package cn.udesk.aac.livedata;

import androidx.lifecycle.MutableLiveData;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.util.UUID;

import cn.udesk.JsonUtils;
import cn.udesk.UdeskSDKManager;
import cn.udesk.aac.MergeMode;
import cn.udesk.aac.MergeModeManager;
import cn.udesk.callback.IUdeskHasSurvyCallBack;
import cn.udesk.messagemanager.UdeskXmppManager;
import udesk.core.model.AllMessageMode;
import cn.udesk.model.InitCustomerBean;
import cn.udesk.model.SurveyOptionsModel;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.model.AgentInfo;

public class APILiveData<M> extends MutableLiveData<MergeMode> {

    private String domain = "";
    private String secretKey = "";
    private String sdktoken = "";
    private String appid = "";
    private String customerId = "";
    private AgentInfo agentInfo;
    //指定的客服ID
    private String specifyAgentID = "";
    //指定的客服组ID
    private String specifyGroupId = "";
    private String menu_id = "";

    public void setBaseValue(String domain, String secretKey, String sdktoken,
                             String appid) {
        this.domain = domain;
        this.secretKey = secretKey;
        this.sdktoken = sdktoken;
        this.appid = appid;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setAgentInfo(AgentInfo agentInfo) {
        this.agentInfo = agentInfo;
    }

    public void setSpecifyAgentID(String specifyAgentID) {
        this.specifyAgentID = specifyAgentID;
    }

    public void setSpecifyGroupId(String specifyGroupId) {
        this.specifyGroupId = specifyGroupId;
    }

    public void setMenu_id(String menu_id) {
        this.menu_id = menu_id;
    }

    public void initCustomer(Context context){
        try{
            UdeskHttpFacade.getInstance().customerInit(context, domain, secretKey, sdktoken,
                    UdeskSDKManager.getInstance().getPrimaryKey(),
                    UdeskSDKManager.getInstance().getUdeskConfig().defaultUserInfo,
                    UdeskSDKManager.getInstance().getUdeskConfig().definedUserTextField,
                    UdeskSDKManager.getInstance().getUdeskConfig().definedUserRoplist,
                    appid, UdeskSDKManager.getInstance().getUdeskConfig().channel,
                    new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {

                            InitCustomerBean initCustomerBean = JsonUtils.parseInitCustomer(message);
                            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.CustomerInitSuccess, initCustomerBean,UUID.randomUUID().toString());
                            MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);
                        }

                        @Override
                        public void onFail(String message) {
                            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.CustomerInitFailure, message,UUID.randomUUID().toString());
                            MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);
                        }
                    }
            );
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void messages(String more_marking, final String from){
        try{
            UdeskHttpFacade.getInstance().v4Messages(domain, secretKey, sdktoken,appid,more_marking,new UdeskCallBack(){
                @Override
                public void onSuccess(String message) {
                    MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.V4PullMessagesSuccess, message,UUID.randomUUID().toString(),from);
                    MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);
                }

                @Override
                public void onFail(String message) {
                    MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.V4PullMessagesFailure, message,UUID.randomUUID().toString());
                    MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //无消息会话创建
    public void getPressionInfo() {
        try {
            UdeskHttpFacade.getInstance().getPreSessionsInfo(domain, secretKey, sdktoken, specifyAgentID, specifyGroupId,
                    false, appid, new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            try {
                                JSONObject json = new JSONObject(message);
                                if (json.has("pre_session_id")) {
                                    MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.SetPreSessionStatus,
                                            json.optString("pre_session_id"),UUID.randomUUID().toString());
                                    MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFail(String message) {
                            // 失败给出错误提示 结束流程
                            failEnd(message);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //请求分配客服信息
    public void getAgentInfo(String preSessionId, JSONObject preMessage) {
        try {
            UdeskHttpFacade.getInstance().getAgentInfo(domain, secretKey, sdktoken, specifyAgentID, specifyGroupId,
                    false, appid, preSessionId, preMessage,menu_id, new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            try {
                                AgentInfo agentInfo = JsonUtils.parseAgentResult(message);
                                if (agentInfo.getAgentCode() == 2000) {
                                    getIMStatus(agentInfo);
                                } else {
                                    dealAgentInfo(agentInfo);
                                }
                                if (!UdeskXmppManager.getInstance().isConnection()) {
                                    UdeskXmppManager.getInstance().connection();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            try {
                                // 失败给出错误提示 结束流程
                                failEnd(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //收到转移客服消息后,请求被转移后的客服信息
    public void getRedirectAgentInfo(String agent_id, String group_id) {
        try {
            UdeskHttpFacade.getInstance().getAgentInfo(domain, secretKey, sdktoken, agent_id, group_id, true, appid, null,
                    null,"", new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            // 获取客户成功，显示在线客服的信息，连接xmpp，进行会话
                            try {
                                AgentInfo agentInfo = JsonUtils.parseAgentResult(message);
                                MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.RedirectAgentInfo, agentInfo,UUID.randomUUID().toString());
                                MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            // 失败给出错误提示 结束流程
                            failEnd(message);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void dealAgentInfo(AgentInfo agentInfo) {
        try {
            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.DealAgentInfo, agentInfo,UUID.randomUUID().toString());
            MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取客服的在线状态，在线连接会话，离线显示客服离线提醒
     */
    public void getIMStatus(final AgentInfo agentInfo) {
        try {
            UdeskHttpFacade.getInstance().getIMstatus(
                    domain, secretKey, sdktoken, agentInfo.getAgentJid(), appid,
                    new UdeskCallBack() {
                        @Override
                        public void onSuccess(String string) {
                            String imStatus = "off";
                            try {
                                try {
                                    JSONObject resultJson = new JSONObject(string);
                                    if (resultJson.has("status")) {
                                        imStatus = resultJson.getString("status");
                                    }
                                } catch (Exception e) {
                                    imStatus = "off";
                                }
//                                if (imStatus.equals("on")) {
//                                    dealAgentInfo(agentInfo);
//                                    return;
//                                }
                                imStatus(imStatus,agentInfo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String s) {
                            imStatus("off",agentInfo);
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void imStatus(String imStatus,AgentInfo agentInfo) {
        try {
            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.IMSTATUS, agentInfo,UUID.randomUUID().toString(),imStatus);
            MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void failEnd(String message) {
        try {
            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.FailEnd, message,UUID.randomUUID().toString());
            MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //客户主动发起满意度调查，先获取是否评价
    public void getHasSurvey(final IUdeskHasSurvyCallBack hasSurvyCallBack) {
        try {
            if (agentInfo == null) {
                if (hasSurvyCallBack != null) {
                    hasSurvyCallBack.hasSurvy(true);
                } else {
                    //出错给提示
                    surveyResult(false);
                }
                return;
            }
            UdeskHttpFacade.getInstance().hasSurvey(domain, secretKey, sdktoken, agentInfo.getAgent_id(), customerId, appid,agentInfo.getIm_sub_session_id(),
                    new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            try {
                                JSONObject result = new JSONObject(message);
                                if (result.has("code") && result.getInt("code") == 1000) {
                                    if (result.has("has_survey")) {
                                        if (TextUtils.equals(result.getString("has_survey"), "false")) {
                                            //未评价，可以发起评价
                                            if (hasSurvyCallBack != null) {
                                                MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.FINISH_SURVEY, true, UUID.randomUUID().toString());
                                                MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);

                                            } else {
                                                MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.click_Survey, true,UUID.randomUUID().toString());
                                                MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);

                                            }
                                        } else {
                                            if (hasSurvyCallBack != null) {
                                                hasSurvyCallBack.hasSurvy(true);
                                            } else {
                                                MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.HasSurvey, true,UUID.randomUUID().toString());
                                                MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);

                                            }

                                        }
                                    }
                                } else {
                                    if (hasSurvyCallBack != null) {
                                        hasSurvyCallBack.hasSurvy(true);
                                    } else {
                                        //出错给提示
                                        surveyResult(false);
                                    }

                                }
                            } catch (Exception e) {
                                if (hasSurvyCallBack != null) {
                                    hasSurvyCallBack.hasSurvy(true);
                                } else {
                                    surveyResult(false);
                                }
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            try {
                                if (hasSurvyCallBack != null) {
                                    hasSurvyCallBack.hasSurvy(true);
                                } else {
                                    surveyResult(false);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            surveyResult(false);
        }
    }


    //请求满意度调查选项的内容
    public void getIMSurveyOptions(final IUdeskHasSurvyCallBack hasSurvyCallBack) {
        try {
            UdeskHttpFacade.getInstance().getIMSurveyOptionsNew(
                    domain,
                    secretKey,
                    sdktoken,
                    appid,
                    agentInfo.getIm_sub_session_id(),
                    new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            try {
                                SurveyOptionsModel model = JsonUtils.parseSurveyOptions(message);
                                if (model != null && (model.getOptions() == null || model.getOptions().isEmpty()) && hasSurvyCallBack != null) {
                                    hasSurvyCallBack.hasSurvy(true);
                                } else {
                                    MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.toLaunchSurveyView, model,UUID.randomUUID().toString());
                                    MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            try {
                                if (hasSurvyCallBack != null) {
                                    hasSurvyCallBack.hasSurvy(true);
                                } else {
                                    surveyResult(false);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            if (hasSurvyCallBack != null) {
                hasSurvyCallBack.hasSurvy(true);
            } else {
                surveyResult(false);
            }
        }
    }


    //提交调查选项内容
    public void putIMSurveyResult(String optionId, String show_type, String survey_remark, String tags) {
        try {
            if (agentInfo == null) {
                surveyResult(false);
            }
            UdeskHttpFacade.getInstance().putSurveyVote(domain, secretKey, sdktoken, agentInfo.getAgent_id(),
                    customerId, optionId, appid, agentInfo.getIm_sub_session_id(), show_type, survey_remark, tags,
                    new UdeskCallBack() {

                        @Override
                        public void onSuccess(String string) {
                            surveyResult(true);
                        }

                        @Override
                        public void onFail(String message) {
                            surveyResult(false);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //客户端返回会话界面，在排队中通知移除排队,不处理返回结果
    public void quitQueue(String quitMode) {
        try {
            UdeskHttpFacade.getInstance().quitQueue(domain, secretKey, sdktoken, appid,
                    quitMode, new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                        }

                        @Override
                        public void onFail(String message) {
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 返回指定客户的当前工单回复列表
    public void getTicketReplies(int page, int perPage) {
        try {
            UdeskHttpFacade.getInstance().getTicketReplies(domain,
                    secretKey, sdktoken, appid, page, perPage,
                    new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            try {
                                AllMessageMode replieMode = JsonUtils.parseMessage(message);
                                MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.PullEventModel, replieMode,UUID.randomUUID().toString());
                                MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFail(String message) {

                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void surveyResult(boolean isSuccess) {
        try {
            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.Survey_Result, isSuccess,UUID.randomUUID().toString());
            MergeModeManager.getmInstance().putMergeMode(mergeMode,APILiveData.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if (UdeskConst.isDebug) {
            Log.i("aac", " APILiveData onInactive");
        }
    }

    @Override
    protected void onActive() {
        super.onActive();
        if (UdeskConst.isDebug) {
            Log.i("aac", " APILiveData onActive");
        }
    }
}
