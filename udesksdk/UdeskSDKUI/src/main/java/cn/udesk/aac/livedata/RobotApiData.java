package cn.udesk.aac.livedata;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.UUID;

import cn.udesk.JsonUtils;
import cn.udesk.UdeskSDKManager;
import cn.udesk.aac.MergeMode;
import cn.udesk.aac.MergeModeManager;
import cn.udesk.aac.QuestionMergeMode;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.event.InvokeEventContainer;
import udesk.core.model.BaseMode;
import udesk.core.model.MessageInfo;
import udesk.core.model.ProductListBean;
import udesk.core.model.RobotTipBean;
import udesk.core.utils.UdeskUtils;

public class RobotApiData<M> extends MutableLiveData<MergeMode> {

    private String domain = "";
    private String secretKey = "";
    private String sdktoken = "";
    private String appid = "";
    private String customerId = "";
    private String sessionId = "";
    private String robotUrl = "";

    public void setBaseValue(String domain, String secretKey, String sdktoken,
                             String appid) {
        this.domain = domain;
        this.secretKey = secretKey;
        this.sdktoken = sdktoken;
        this.appid = appid;
    }

    //机器人初始化接口
    public void initRobot(Context context) {
        try {
            UdeskHttpFacade.getInstance().robotInit(context, domain, secretKey, sdktoken,
                    UdeskSDKManager.getInstance().getPrimaryKey(),
                    UdeskSDKManager.getInstance().getUdeskConfig().defaultUserInfo,
                    UdeskSDKManager.getInstance().getUdeskConfig().definedUserTextField,
                    UdeskSDKManager.getInstance().getUdeskConfig().definedUserRoplist,
                    appid, UdeskSDKManager.getInstance().getUdeskConfig().channel,robotUrl,UdeskSDKManager.getInstance().getUdeskConfig().robot_modelKey,
                    new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.RobotInitSuccess, message,UUID.randomUUID().toString());
                            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

                        }

                        @Override
                        public void onFail(String message) {
                            requestFail(UdeskConst.LiveDataType.RobotInitFailure, message);
                        }
                    }
            );
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //机器人发送消息
    public void robotMessage(final MessageInfo info) {
        try {
            UdeskHttpFacade.getInstance().robotMessage(domain, secretKey, sdktoken, appid, robotUrl,customerId, UdeskUtils.objectToLong(sessionId),info.getMsgtype(),
                    info.getMsgContent(), info.getMsgId(), info.getDuration(), info.getSeqNum(), info.getFilename(), info.getFilename(), info.getLocalPath(),new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.RobotMessageSuccess, message,UUID.randomUUID().toString(),info.getMsgId());
                            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

                        }

                        @Override
                        public void onFail(String message) {
                            requestFail(UdeskConst.LiveDataType.RobotMessageFailure, info.getMsgId());
                        }
                    }
            );
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    //机器人智能提示 输入联想
    public void robotTips(final String content) {
        try {
            UdeskHttpFacade.getInstance().robotTips(domain, secretKey, sdktoken, appid, robotUrl,customerId, UdeskUtils.objectToLong(sessionId),
                    content, new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            QuestionMergeMode mergeMode = new QuestionMergeMode(UdeskConst.LiveDataType.RobotTipsSuccess, message,UUID.randomUUID().toString());
                            mergeMode.setQuestion(content);
                            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

                        }

                        @Override
                        public void onFail(String message) {
                            requestFail(UdeskConst.LiveDataType.RobotTipsFailure, message);
                        }
                    }
            );
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //机器人问答评价
    public void robotAnswerSurvey(String logId,String option_id) {
        try {
            if (TextUtils.isEmpty(logId)){
                logId = "0";
            }
            UdeskHttpFacade.getInstance().robotAnswerSurvey(domain, secretKey, sdktoken, appid, robotUrl,sessionId,logId, option_id, new UdeskCallBack() {
                @Override
                public void onSuccess(String message) {
                    //执行结果码，1000代表成功
                    BaseMode baseMode = JsonUtils.parseAnswerSurvey(message);
                    MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.RobotAnswerSurveySuccess, baseMode,UUID.randomUUID().toString());
                    MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

                }

                @Override
                public void onFail(String message) {
                    requestFail(UdeskConst.LiveDataType.RobotAnswerSurveyFailure, message);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //机器人会话评价
    public void robotSessionSurvey(String option_id, String remark) {
        try {
            UdeskHttpFacade.getInstance().robotSessionSurvey(domain, secretKey, sdktoken, appid,robotUrl,
                    sessionId, option_id, remark, new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            //执行结果码，1000代表成功
                            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.ROBOT_SURVEY_RESULT, true,UUID.randomUUID().toString());
                            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

                        }

                        @Override
                        public void onFail(String message) {
                            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.ROBOT_SURVEY_RESULT, false,UUID.randomUUID().toString());
                            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //机器人会话是否已评价
    public void robotSessionHasSurvey() {
        try {
            UdeskHttpFacade.getInstance().robotSessionHasSurvey(domain, secretKey, sdktoken, appid, robotUrl,
                    sessionId, new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            try {
                                JSONObject result = new JSONObject(message);
                                if (result.has("code") && result.getInt("code") == 1000) {
                                    if (result.has("has_survey")) {
                                        if (TextUtils.equals(result.getString("has_survey"), "false")) {
                                            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.RobotSessionHasSurvey, false,UUID.randomUUID().toString());
                                            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

                                        }else {
                                            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.RobotSessionHasSurvey, true,UUID.randomUUID().toString());
                                            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

                                        }
                                    }
                                }

                            } catch (Exception e) {
                                MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.ROBOT_SURVEY_RESULT, false,UUID.randomUUID().toString());
                                MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

                            }
                        }

                        @Override
                        public void onFail(String message) {
                            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.ROBOT_SURVEY_RESULT, false,UUID.randomUUID().toString());
                            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //机器人点击
    public void robotHit(final String message_id, String logId,String question, Object question_id, int query_type) {
        try {
            if (TextUtils.isEmpty(logId)){
                logId = "0";
            }
            UdeskHttpFacade.getInstance().robotHit(domain, secretKey, sdktoken, appid,robotUrl,message_id, sessionId,
                    logId, question, UdeskUtils.objectToString(question_id), UdeskUtils.objectToString(query_type), new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.RobotHitSuccess, message,UUID.randomUUID().toString());
                            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

                        }

                        @Override
                        public void onFail(String message) {
                            requestFail(UdeskConst.LiveDataType.RobotHitFailure, message_id);
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    //机器人流程点击
    public void robotFlow(final String message_id, String logId,long flowId, String flowContent){
        try {
            if (TextUtils.isEmpty(logId)){
                logId = "0";
            }
            UdeskHttpFacade.getInstance().robotFlow(domain, secretKey, sdktoken, appid, robotUrl, message_id,sessionId,
                    logId, UdeskUtils.objectToString(flowId), flowContent, new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.RobotFlowSuccess, message,UUID.randomUUID().toString());
                            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

                        }

                        @Override
                        public void onFail(String message) {
                            requestFail(UdeskConst.LiveDataType.RobotHitFailure, message_id);
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void requestFail(int flag, String message) {
        try {
            MergeMode mergeMode = new MergeMode(flag, message,UUID.randomUUID().toString());
            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void robotTransfer() {
        try {
            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.ROBOT_TRANSFER,UUID.randomUUID().toString());
            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void onQueClick(String msgId,String logId,String question,Long questionId,Boolean isFromFlow,Boolean isFAQ){
        try {
            QuestionMergeMode mergeMode=new QuestionMergeMode(UdeskConst.LiveDataType.RobotChildHit,UUID.randomUUID().toString());
            mergeMode.setQuestion(question);
            mergeMode.setQuestionId(questionId);
            if (isFromFlow){
                mergeMode.setQueryType(8);
            }else {
                if (isFAQ){
                    mergeMode.setQueryType(6);
                }else {
                    mergeMode.setQueryType(7);
                }
            }
            mergeMode.setMsgId(msgId);
            mergeMode.setLogId(logId);
            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void onRobotJumpMessageClick(String content){
        try {
            if (!TextUtils.isEmpty(content)){
                QuestionMergeMode mergeMode=new QuestionMergeMode(UdeskConst.LiveDataType.ROBOT_JUMP_MESSAGE,content,UUID.randomUUID().toString());
                MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void onRichTextTransferClick(String content){
        try {
            if (!TextUtils.isEmpty(content)){
                QuestionMergeMode mergeMode=new QuestionMergeMode(UdeskConst.LiveDataType.ROBOT_RICHTEXT_TRANSFER_CLICK,content,UUID.randomUUID().toString());
                MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onTipClick(RobotTipBean.ListBean bean){
        try {
            if (bean!=null){
                QuestionMergeMode mergeMode=new QuestionMergeMode(UdeskConst.LiveDataType.RobotTipHit,bean,UUID.randomUUID().toString());
                mergeMode.setQueryType(9);
                MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void onTableClick(String value){
        try {
            if (!TextUtils.isEmpty(value)){
                MergeMode mergeMode=new MergeMode(UdeskConst.LiveDataType.ROBOT_TABLE_CLICK,value,UUID.randomUUID().toString());
                MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void onTransferClick(MessageInfo messageInfo) {
        try {
            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.ROBOT_TRANSFER_CLICK,messageInfo,UUID.randomUUID().toString());
            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void onShowProductClick(ProductListBean bean) {
        try {
            MergeMode mergeMode = new MergeMode(UdeskConst.LiveDataType.ROBOT_SHOW_PRODUCT_CLICK,bean,UUID.randomUUID().toString());
            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void onAnswerClick(String logId,String useful) {
        try {
            robotAnswerSurvey(logId,useful);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onFlowClick(MessageInfo info,String dataId,String content) {
        try {
            if (info!=null){
                QuestionMergeMode mergeMode=new QuestionMergeMode(UdeskConst.LiveDataType.ROBOT_FLOW_HIT,info,UUID.randomUUID().toString());
                mergeMode.setQuestionId(UdeskUtils.objectToLong(dataId));
                mergeMode.setQuestion(content);
                MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void sendTxtMsg(String content){
        try {
            MergeMode mergeMode=new MergeMode(UdeskConst.LiveDataType.ROBOT_SEND_TXT_MSG,content,UUID.randomUUID().toString());
            MergeModeManager.getmInstance().putMergeMode(mergeMode,RobotApiData.this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onActive() {
        super.onActive();
        InvokeEventContainer.getInstance().event_OnQueClick.bind(this,"onQueClick");
        InvokeEventContainer.getInstance().event_OnTipClick.bind(this,"onTipClick");
        InvokeEventContainer.getInstance().event_OnTableClick.bind(this,"onTableClick");
        InvokeEventContainer.getInstance().event_OnTransferClick.bind(this,"onTransferClick");
        InvokeEventContainer.getInstance().event_OnShowProductClick.bind(this,"onShowProductClick");
        InvokeEventContainer.getInstance().event_OnAnswerClick.bind(this,"onAnswerClick");
        InvokeEventContainer.getInstance().event_OnFlowClick.bind(this,"onFlowClick");
        InvokeEventContainer.getInstance().event_OnRobotJumpMessageClick.bind(this,"onRobotJumpMessageClick");
        InvokeEventContainer.getInstance().event_OnRichTextTransferClick.bind(this,"onRichTextTransferClick");
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        InvokeEventContainer.getInstance().event_OnQueClick.unBind(this);
        InvokeEventContainer.getInstance().event_OnTipClick.unBind(this);
        InvokeEventContainer.getInstance().event_OnTableClick.unBind(this);
        InvokeEventContainer.getInstance().event_OnTransferClick.unBind(this);
        InvokeEventContainer.getInstance().event_OnShowProductClick.unBind(this);
        InvokeEventContainer.getInstance().event_OnAnswerClick.unBind(this);
        InvokeEventContainer.getInstance().event_OnFlowClick.unBind(this);
        InvokeEventContainer.getInstance().event_OnRobotJumpMessageClick.unBind(this);
        InvokeEventContainer.getInstance().event_OnRichTextTransferClick.unBind(this);
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = UdeskUtils.objectToString(sessionId);
    }
    public void setRobotUrl(String robotUrl) {
        this.robotUrl = robotUrl;
    }
}
