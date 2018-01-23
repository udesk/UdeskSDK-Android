package cn.udesk.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.util.Log;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import cn.udesk.JsonUtils;
import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.activity.UdeskChatActivity.MessageWhat;
import cn.udesk.adapter.UDEmojiAdapter;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.messagemanager.Concurrents;
import cn.udesk.model.LogMessage;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.model.TicketReplieMode;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.voice.AudioRecordState;
import cn.udesk.voice.AudioRecordingAacThread;
import cn.udesk.voice.VoiceRecord;
import cn.udesk.messagemanager.UdeskMessageManager;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskCoreConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.event.InvokeEventContainer;
import udesk.core.http.UdeskHttpCallBack;
import udesk.core.model.AgentInfo;
import udesk.core.model.MessageInfo;
import udesk.core.utils.UdeskIdBuild;
import udesk.core.utils.UdeskUtils;
import udesk.core.xmpp.XmppInfo;


public class ChatActivityPresenter {

    private IChatActivityView mChatView;
    private VoiceRecord mVoiceRecord = null;
    private String mRecordTmpFile = "";
    //处理七牛上传完成的回调
    MyUpCompletionHandler mMyUpCompletionHandler = null;
    private IUdeskHasSurvyCallBack hasSurvyCallBack;

    public interface IUdeskHasSurvyCallBack {

        void hasSurvy(boolean hasSurvy);
    }

    public ChatActivityPresenter(IChatActivityView chatview) {
        this.mChatView = chatview;
        bindEevent();
    }

    public void setHasSurvyCallBack(IUdeskHasSurvyCallBack hasSurvyCallBack) {
        this.hasSurvyCallBack = hasSurvyCallBack;
    }

    // ---------------以下是注册方法，通过观察者模式通知处理的逻辑部分 ，注册的方法，必须是public,方法得参数必须是class-------------------
    private void bindEevent() {
        UdeskMessageManager.getInstance().eventui_OnNewPresence.bind(this, "onPrenseMessage");
        UdeskMessageManager.getInstance().eventui_OnMessageReceived.bind(this, "onMessageReceived");
        UdeskMessageManager.getInstance().eventui_OnNewMessage.bind(this, "onNewMessage");
        InvokeEventContainer.getInstance().event_OncreateCustomer.bind(this, "onCreateCustomer");
        InvokeEventContainer.getInstance().event_OnIsBolcked.bind(this, "onIsBolck");
        UdeskMessageManager.getInstance().event_OnTicketReplayNotice.bind(this, "onTicketReplay");
        InvokeEventContainer.getInstance().event_OnVideoEventReceived.bind(this, "onVideoEvent");
    }

    //独立开bindEevent    是为了满足满意度调查的弹出，在可见的的时候弹出，在后台或遮挡了不出理
    public void bindReqsurveyMsg() {
        UdeskMessageManager.getInstance().eventui_OnReqsurveyMsg.bind(this, "onReqsurveyMsg");
    }

    //独立开unBind
    public void unbindReqsurveyMsg() {
        UdeskMessageManager.getInstance().eventui_OnReqsurveyMsg.unBind(this);
    }

    public void unBind() {
        UdeskMessageManager.getInstance().eventui_OnNewPresence.unBind(this);
        UdeskMessageManager.getInstance().eventui_OnMessageReceived.unBind(this);
        UdeskMessageManager.getInstance().eventui_OnNewMessage.unBind(this);
        InvokeEventContainer.getInstance().event_OncreateCustomer.unBind(this);
        InvokeEventContainer.getInstance().event_OnIsBolcked.unBind(this);
        UdeskMessageManager.getInstance().event_OnTicketReplayNotice.unBind(this);
        InvokeEventContainer.getInstance().event_OnVideoEventReceived.unBind(this);
        mChatView = null;
    }

    public void onVideoEvent(String event, String id, String message, Boolean isInvite) {


        try {
            if (event.equals(UdeskCoreConst.ReceiveType.StartMedio)) {
                MessageInfo messageInfo = buildVideoEventMsg(id, isInvite, message);
                saveMessage(messageInfo);
            } else if (event.equals(UdeskCoreConst.ReceiveType.Cancle)) {
                //取消
                UdeskDBManager.getInstance().updateMsgContent(id,
                        message);
                MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                mChatView.addMessage(eventMsg);

            } else if (event.equals(UdeskCoreConst.ReceiveType.Busy)) {
                message = mChatView.getAgentInfo().getAgentNick() + message;
                UdeskDBManager.getInstance().updateMsgContent(id, message);
                MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                mChatView.addMessage(eventMsg);
            } else if (event.equals(UdeskCoreConst.ReceiveType.Timeout)) {
                message = mChatView.getAgentInfo().getAgentNick() + message;
                UdeskDBManager.getInstance().updateMsgContent(id,
                        message);
                MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                mChatView.addMessage(eventMsg);
            } else if (event.equals(UdeskCoreConst.ReceiveType.Reject)) {
                UdeskDBManager.getInstance().updateMsgContent(id,
                        message);
                MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                mChatView.addMessage(eventMsg);
            } else if (event.equals(UdeskCoreConst.ReceiveType.Over)) {
                UdeskDBManager.getInstance().updateMsgContent(id,
                        message);
                MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                mChatView.addMessage(eventMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public MessageInfo buildVideoEventMsg(String id, Boolean isInvite, String text) {
        MessageInfo msg = new MessageInfo();
        try {
            msg.setMsgtype(UdeskConst.ChatMsgTypeString.TYPE_Video_Txt);
            msg.setTime(System.currentTimeMillis());
            msg.setMsgId(id);
            if (isInvite) {
                msg.setDirection(UdeskConst.ChatMsgDirection.Send);
            } else {
                msg.setDirection(UdeskConst.ChatMsgDirection.Recv);
                msg.setmAgentJid(mChatView.getAgentInfo().getAgentJid());
            }
            msg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
            msg.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
            if (isInvite) {
                msg.setMsgContent(text);
            } else {
                msg.setMsgContent(mChatView.getAgentInfo().getAgentNick() + text);
            }
            msg.setPlayflag(UdeskConst.PlayFlag.NOPLAY);
            msg.setLocalPath("");
            msg.setDuration(0);
            msg.setSubsessionid(mChatView.getAgentInfo().getIm_sub_session_id());
            msg.setSeqNum(UdeskDBManager.getInstance().getSubSessionId(mChatView.getAgentInfo().getIm_sub_session_id()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * 收到消息回执
     */
    public void onMessageReceived(String msgId) {
        try {
            if (mChatView.getHandler() != null) {
                Message message = mChatView.getHandler().obtainMessage(
                        MessageWhat.changeImState);
                message.obj = msgId;
                message.arg1 = UdeskConst.SendFlag.RESULT_SUCCESS;
                mChatView.getHandler().sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 收到新消息
     */
    public void onNewMessage(MessageInfo msgInfo) {

        try {
            if (mChatView.getHandler() != null) {
                Message messge = mChatView.getHandler().obtainMessage(
                        MessageWhat.onNewMessage);
                messge.obj = msgInfo;
                mChatView.getHandler().sendMessage(messge);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 收到客服在线下线的通知
     */
    public void onPrenseMessage(String jid, Integer onlineFlag) {
        try {
            if (mChatView.getHandler() != null) {
                Message messge = mChatView.getHandler().obtainMessage(
                        MessageWhat.status_notify);
                messge.arg1 = onlineFlag;
                messge.obj = jid;
                mChatView.getHandler().sendMessage(messge);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //收到满意度调查消息
    public void onReqsurveyMsg(Boolean isSurvey) {
        try {
            if (mChatView != null) {
                mChatView.changgeiSSurvyOperate();
                getIMSurveyOptions(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //加入黑名单通知
    public void onIsBolck(String isBolcked, String notice) {
        try {
            if (isBolcked.equals("true")) {
                if (mChatView.getHandler() != null) {
                    Message messge = mChatView.getHandler().obtainMessage(
                            MessageWhat.IM_BOLACKED);
                    messge.obj = notice;
                    mChatView.getHandler().sendMessage(messge);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onTicketReplay(Boolean isTicketRepaly) {
        //拉取工单回复的消息
        getTicketReplies(UdeskBaseInfo.customerId, 1, UdeskConst.UDESK_HISTORY_COUNT, "");
    }

    //创建客户成功回调
    //说明 创建失败  stirng 是错误提示
    public void onCreateCustomer(String result, Boolean isJsonStr, String string) {
        try {
            if (result.equals("failure")) {
                mChatView.showFailToast(string);
            } else if (result.equals("succes")) {
                //创建用户成功连接xmpp服务器
                if (isJsonStr) {
                    JsonUtils.parserCustomersJson(string);
                    updateUserInfo(UdeskBaseInfo.customerId);
                }
                getAgentInfo(false);
                //拉取工单回复的消息
                getTicketReplies(UdeskBaseInfo.customerId, 1, UdeskConst.UDESK_HISTORY_COUNT, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mChatView.showFailToast(string);
        }
    }

    // ---------------以上是注册方法，通过观察者模式通知处理的逻辑部分 注册的方法，必须是public,方法得参数必须是class-------------------


    // ---------------以下是http请求接口 及处理逻辑 -------------------
    //请求获取客户信息的入口， 请求处理完后  会通知到onCreateCustomer 方法
    public void createIMCustomerInfo() {
        try {
            Context mContext = mChatView.getContext();
            String sdkToken = UdeskSDKManager.getInstance().getSdkToken(mContext);
            UdeskHttpFacade.getInstance().setUserInfo(mContext, UdeskSDKManager.getInstance().getDomain(mContext),
                    UdeskSDKManager.getInstance().getAppkey(mContext), sdkToken,
                    UdeskBaseInfo.userinfo, UdeskBaseInfo.textField, UdeskBaseInfo.roplist,
                    UdeskSDKManager.getInstance().getAppId(mContext), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //请求分配客服信息
    public void getAgentInfo(final boolean isWait) {
        try {
            UdeskHttpFacade.getInstance().getAgentInfo(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    mChatView.getAgentId(), mChatView.getGroupId(), false,
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                    new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            AgentInfo agentInfo = JsonUtils.parseAgentResult(message);
                            if (agentInfo.getAgentCode() == 2000) {
                                getIMStatus(agentInfo);
                            } else {
                                mChatView.dealAgentInfo(agentInfo);
                            }
                            if (!UdeskMessageManager.getInstance().isConnection()) {
                                UdeskMessageManager.getInstance().connection();
                            } else {
                                if (!isWait) {
                                    UdeskMessageManager.getInstance().connection();
                                }
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            // 失败给出错误提示 结束流程
                            mChatView.showFailToast(message);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取客服的在线状态，在线连接会话，离线显示客服离线提醒
     */
    public void getIMStatus(final AgentInfo agentInfo) {
        try {
            if (agentInfo == null) {
                if (mChatView.getHandler() != null) {
                    Message message = mChatView.getHandler().obtainMessage(
                            MessageWhat.IM_STATUS);
                    message.obj = "off";
                    mChatView.getHandler().sendMessage(message);
                }
                return;
            }
            UdeskHttpFacade.getInstance().getIMstatus(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    agentInfo.getAgentJid(), UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                    new UdeskCallBack() {
                        @Override
                        public void onSuccess(String string) {
                            String imStatus = "off";
                            try {
                                JSONObject resultJson = new JSONObject(string);
                                if (resultJson.has("status")) {
                                    imStatus = resultJson.getString("status");
                                }
                            } catch (Exception e) {
                                imStatus = "off";
                            }
                            if (imStatus.equals("on")) {
                                mChatView.dealAgentInfo(agentInfo);
                                return;
                            }
                            mChatView.setAgentInfo(agentInfo);
                            if (mChatView.getHandler() != null) {
                                Message message = mChatView.getHandler().obtainMessage(
                                        MessageWhat.IM_STATUS);
                                message.obj = imStatus;
                                mChatView.getHandler().sendMessage(message);
                            }
                        }

                        @Override
                        public void onFail(String s) {
                            if (mChatView.getHandler() != null) {
                                Message message = mChatView.getHandler().obtainMessage(
                                        MessageWhat.IM_STATUS);
                                message.obj = "off";
                                mChatView.getHandler().sendMessage(message);
                            }
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //收到转移客服消息后,请求被转移后的客服信息
    public void getRedirectAgentInfo(String agent_id, String group_id) {
        try {
            UdeskHttpFacade.getInstance().getAgentInfo(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    agent_id, group_id, true, UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                    new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            // 获取客户成功，显示在线客服的信息，连接xmpp，进行会话
                            AgentInfo agentInfo = JsonUtils.parseAgentResult(message);
                            mChatView.dealRedirectAgentInfo(agentInfo);
                        }

                        @Override
                        public void onFail(String message) {
                            // 失败给出错误提示 结束流程
                            mChatView.showFailToast(message);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 返回指定客户的当前工单回复列表
    public void getTicketReplies(String customerId, int page, int perPage, String createTime) {
        try {
            UdeskHttpFacade.getInstance().getTicketReplies(UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()), customerId,
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()), page, perPage, createTime,
                    new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            TicketReplieMode replieMode = JsonUtils.parserTicketReplie(message);
                            if (replieMode != null && replieMode.getContents() != null) {
                                List<MessageInfo> messageInfos = buildLeaveMsgByTicketReplies(replieMode.getContents());
                                if (messageInfos != null && mChatView.getHandler() != null) {
                                    Message msg = Message.obtain();
                                    msg.what = MessageWhat.loadHistoryDBMsg;
                                    msg.arg1 = 3;
                                    msg.obj = messageInfos;
                                    mChatView.getHandler().sendMessage(msg);
                                }
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

    //提交留言消息
    public void putLeavesMsg(String customerId, String msg, final String msgId) {
        try {
            UdeskHttpFacade.getInstance().putReplies(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()), customerId,
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()), msg,
                    new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            //修改消息状态
                            UdeskDBManager.getInstance().updateMsgSendFlag(msgId, UdeskConst.SendFlag.RESULT_SUCCESS);
                            onMessageReceived(msgId);
                        }

                        @Override
                        public void onFail(String msg) {
                            UdeskDBManager.getInstance().updateMsgSendFlag(msgId, UdeskConst.SendFlag.RESULT_FAIL);
                            if (mChatView.getHandler() != null) {
                                Message message = mChatView.getHandler().obtainMessage(
                                        MessageWhat.changeImState);
                                message.obj = msgId;
                                message.arg1 = UdeskConst.SendFlag.RESULT_FAIL;
                                mChatView.getHandler().sendMessage(message);
                            }
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //更新用户信息
    private void updateUserInfo(final String userId) {
        try {
            if (UdeskBaseInfo.updateUserinfo != null
                    || UdeskBaseInfo.updateTextField != null
                    || UdeskBaseInfo.updateRoplist != null) {
                UdeskHttpFacade.getInstance().updateUserInfo(UdeskBaseInfo.updateUserinfo,
                        UdeskBaseInfo.updateTextField,
                        UdeskBaseInfo.updateRoplist, userId,
                        UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                        UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                        UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                        new UdeskCallBack() {
                            @Override
                            public void onSuccess(String message) {

                            }

                            @Override
                            public void onFail(String message) {

                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //客户主动发起满意度调查，先获取是否评价
    public void getHasSurvey(String agent_id, final IUdeskHasSurvyCallBack hasSurvyCallBack) {
        try {
            if (TextUtils.isEmpty(UdeskBaseInfo.customerId)) {
                mChatView.setIsPermmitSurvy(true);
                return;
            }
            UdeskHttpFacade.getInstance().hasSurvey(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    agent_id, UdeskBaseInfo.customerId,
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                    new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            try {
                                JSONObject result = new JSONObject(message);
                                if (result.has("code") && result.getInt("code") == 1000) {
                                    if (result.has("has_survey")) {
                                        if (TextUtils.equals(result.getString("has_survey"), "false")) {
                                            //未评价，可以发起评价
                                            getIMSurveyOptions(hasSurvyCallBack);
                                        } else {
                                            //已评价，给出提示
                                            mChatView.setIsPermmitSurvy(true);
                                            if (hasSurvyCallBack != null) {
                                                hasSurvyCallBack.hasSurvy(true);
                                            } else {
                                                if (mChatView.getHandler() != null) {
                                                    Message messge = mChatView.getHandler().obtainMessage(
                                                            MessageWhat.Has_Survey);
                                                    mChatView.getHandler().sendMessage(messge);
                                                }
                                            }

                                        }
                                    }
                                } else {
                                    if (hasSurvyCallBack != null) {
                                        hasSurvyCallBack.hasSurvy(true);
                                    } else {
                                        //出错给提示
                                        sendSurveyerror();
                                    }

                                }
                            } catch (Exception e) {
                                if (hasSurvyCallBack != null) {
                                    hasSurvyCallBack.hasSurvy(true);
                                } else {
                                    //出错给提示
                                    sendSurveyerror();
                                }
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            if (hasSurvyCallBack != null) {
                                hasSurvyCallBack.hasSurvy(true);
                            } else {
                                //出错给提示
                                sendSurveyerror();
                            }
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            sendSurveyerror();
        }
    }

    //请求满意度调查选项的内容
    private void getIMSurveyOptions(final IUdeskHasSurvyCallBack hasSurvyCallBack) {
        try {
            UdeskHttpFacade.getInstance().getIMSurveyOptions(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()), new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            String SurveyMsg = message;
                            SurveyOptionsModel model = JsonUtils.parseSurveyOptions(SurveyMsg);
                            if (model != null && (model.getOptions() == null || model.getOptions().isEmpty()) && hasSurvyCallBack != null) {
                                hasSurvyCallBack.hasSurvy(true);
                            } else if (mChatView.getHandler() != null) {
                                Message messge = mChatView.getHandler().obtainMessage(
                                        MessageWhat.surveyNotify);
                                messge.obj = model;
                                mChatView.getHandler().sendMessage(messge);
                            } else if (hasSurvyCallBack != null) {
                                hasSurvyCallBack.hasSurvy(true);
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            if (hasSurvyCallBack != null) {
                                hasSurvyCallBack.hasSurvy(true);
                            } else {
                                sendSurveyerror();
                            }

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            if (hasSurvyCallBack != null) {
                hasSurvyCallBack.hasSurvy(true);
            } else {
                sendSurveyerror();
            }
        }
    }

    //提交调查选项内容
    public void putIMSurveyResult(String optionId) {

        try {
            UdeskHttpFacade.getInstance().putSurveyVote(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    mChatView.getAgentInfo().getAgent_id(),
                    UdeskBaseInfo.customerId,
                    optionId, UdeskSDKManager.getInstance().getAppId(mChatView.getContext()), new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            String SurveyMsg = message;

                        }

                        @Override
                        public void onFail(String message) {
                            mChatView.showFailToast(message);

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //客户端返回会话界面，在排队中通知移除排队
    public void quitQuenu() {
        try {
            UdeskHttpFacade.getInstance().quitQueue(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                    UdeskConfig.UdeskQuenuMode, new UdeskCallBack() {

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

    // ---------------以上是http请求接口 及处理逻辑 -------------------


    // ---------------以下是发送消息的业务逻辑 -------------------

    //发送商品链接广告
    public void sendCommodityMessage(UdeskCommodityItem commodityItem) {
        UdeskMessageManager.getInstance().sendComodityMessage(buildCommodityMessage(commodityItem),
                mChatView.getAgentInfo().getAgentJid());
    }

    //构造广告消息的格式
    private String buildCommodityMessage(UdeskCommodityItem item) {
        JSONObject root = new JSONObject();
        JSONObject dataJson = new JSONObject();
        JSONObject paramsJson = new JSONObject();
        try {
            paramsJson.put("detail", item.getSubTitle());
            dataJson.put("url", item.getCommodityUrl());
            dataJson.put("image", item.getThumbHttpUrl());
            dataJson.put("title", item.getTitle());
            dataJson.put("product_params", paramsJson);
            root.put("type", "product");
            root.put("platform", "android");
            root.put("data", dataJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root.toString();
    }


    //发送输入预支消息
    public void sendPreMessage() {
        try {
            UdeskMessageManager.getInstance().sendPreMsg(UdeskConst.ChatMsgTypeString.TYPE_TEXT,
                    mChatView.getInputContent().toString(), mChatView.getAgentInfo().getAgentJid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送文本消息
    public void sendTxtMessage() {
        try {
            if (!TextUtils.isEmpty(mChatView.getInputContent().toString().trim())) {
                sendTxtMessage(mChatView.getInputContent().toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送留言消息
    public void sendLeaveMessage() {
        try {
            if (!TextUtils.isEmpty(mChatView.getInputContent().toString().trim())) {
                String msgString = mChatView.getInputContent().toString();
                MessageInfo msg = buildSendMessage(
                        UdeskConst.ChatMsgTypeString.TYPE_LEAVEMSG,
                        System.currentTimeMillis(), msgString, "");
                saveMessage(msg);
                mChatView.clearInputContent();
                mChatView.addMessage(msg);
                putLeavesMsg(UdeskBaseInfo.customerId, msgString, msg.getMsgId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendLeaveMessage(String message) {
        try {
            MessageInfo msg = buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_LEAVEMSG,
                    System.currentTimeMillis(), message, "");
            saveMessage(msg);
            mChatView.addMessage(msg);
            putLeavesMsg(UdeskBaseInfo.customerId, message, msg.getMsgId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //封装发送文本消息
    public void sendTxtMessage(String msgString) {
        try {
            MessageInfo msg = buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_TEXT,
                    System.currentTimeMillis(), msgString, "");
            saveMessage(msg);
            mChatView.clearInputContent();
            mChatView.addMessage(msg);

            messageSave(msg);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pullMessages(final int seqNum, final String subSessionId) {
        UdeskHttpFacade.getInstance().getMessages(UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                UdeskBaseInfo.customerId, subSessionId, seqNum, new UdeskCallBack() {
                    @Override
                    public void onSuccess(String message) {
                        try {
                            JSONObject root = new JSONObject(message);
                            String code = UdeskUtil.objectToString(root.opt("code"));
                            if (code.equals("1002")) {
                                int request_delay_time = UdeskUtil.objectToInt(root.opt("request_delay_time"));
                                mChatView.getHandler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        pullMessages(seqNum, subSessionId);
                                    }
                                }, request_delay_time * 1000);
                            } else {
                                List<LogMessage> logMessages = JsonUtils.parseMessages(message);
                                if (logMessages != null && logMessages.size() > 0) {
                                    List<MessageInfo> msgInfos = tranferLogMessage(logMessages);
                                    if (msgInfos.size() > 0) {
                                        UdeskDBManager.getInstance().addAllMessageInfo(msgInfos);
                                        mChatView.initLoadData();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                    @Override
                    public void onFail(String message) {

                    }
                });
    }


    public void messageSave(final MessageInfo msg) {
        UdeskHttpFacade.getInstance().messageSave(UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                UdeskBaseInfo.customerId, mChatView.getAgentInfo().getAgent_id(),
                msg.getSubsessionid(), UdeskCoreConst.UdeskSendStatus.sending,
                msg.getMsgtype(), msg.getMsgContent(), msg.getMsgId(),
                msg.getDuration(), msg.getSeqNum(), new UdeskCallBack() {
                    @Override
                    public void onSuccess(String message) {
                        UdeskDBManager.getInstance().updateMsgSendFlag(msg.getMsgId(), UdeskConst.SendFlag.RESULT_SUCCESS);
                        UdeskMessageManager.getInstance().sendMessage(msg.getMsgtype(),
                                msg.getMsgContent(), msg.getMsgId(),
                                mChatView.getAgentInfo().getAgentJid(), msg.getDuration(), msg.getSubsessionid(), true, msg.getSeqNum());
                        onMessageReceived(msg.getMsgId());

                    }

                    @Override
                    public void onFail(String message) {
                        UdeskMessageManager.getInstance().sendMessage(msg.getMsgtype(),
                                msg.getMsgContent(), msg.getMsgId(),
                                mChatView.getAgentInfo().getAgentJid(), msg.getDuration(), msg.getSubsessionid(), false, msg.getSeqNum());
                        UdeskDBManager.getInstance().addSendingMsg(msg.getMsgId(),
                                UdeskConst.SendFlag.RESULT_SEND, System.currentTimeMillis());
                    }
                });
    }


    public void sendLocationMessage(double lat, double longitude, String localvalue, String bitmapDir) {
        StringBuilder builder = new StringBuilder();
        builder.append(lat).append(";").append(longitude).append(";").append("16;").append(localvalue);
        try {
            MessageInfo msg = buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_Location,
                    System.currentTimeMillis(), builder.toString(), bitmapDir);
            saveMessage(msg);
            mChatView.addMessage(msg);
            messageSave(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 发送录音信息
    public void sendRecordAudioMsg(String audiopath, long duration) {
        try {
            MessageInfo msg = buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_AUDIO,
                    System.currentTimeMillis(), "", audiopath);
            duration = duration / 1000 + 1;
            msg.setDuration(duration);
            saveMessage(msg);
            mChatView.addMessage(msg);
            upLoadFile(audiopath, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送图片消息
    public void sendBitmapMessage(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);

            BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
            factoryOptions.inJustDecodeBounds = false;
            factoryOptions.inPurgeable = true;
            // 获取原图数据
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] data = stream.toByteArray();

            String imageName = UdeskUtils.MD5(data);
            File scaleImageFile = UdeskUtil.getOutputMediaFile(mChatView.getContext(), imageName
                    + UdeskConst.ORIGINAL_SUFFIX);
            if (scaleImageFile != null) {
                if (max > UdeskConfig.ScaleMax) {
                    factoryOptions.inSampleSize = max / UdeskConfig.ScaleMax;
                } else {
                    factoryOptions.inSampleSize = 1;
                }
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(scaleImageFile);
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                            factoryOptions);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                    fos.close();
                    fos = null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (bitmap != null) {
                    bitmap.recycle();
                    bitmap = null;
                }
                data = null;
                if (TextUtils.isEmpty(scaleImageFile.getPath())) {
                    UdeskUtils.showToast(mChatView.getContext(), mChatView.getContext().getString(R.string.udesk_upload_img_error));
                    return;
                }
                MessageInfo msg = buildSendMessage(
                        UdeskConst.ChatMsgTypeString.TYPE_IMAGE,
                        System.currentTimeMillis(), "", scaleImageFile.getPath());
                saveMessage(msg);
                mChatView.addMessage(msg);
                upLoadFile(msg.getLocalPath(), msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    //发送图片消息
    public void sendBitmapMessage(String photoPath) {
        try {
            if (TextUtils.isEmpty(photoPath)) {
                UdeskUtils.showToast(mChatView.getContext(), mChatView.getContext().getString(R.string.udesk_upload_img_error));
                return;
            }
            MessageInfo msg = buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_IMAGE,
                    System.currentTimeMillis(), "", photoPath);
            saveMessage(msg);
            mChatView.addMessage(msg);
            upLoadFile(photoPath, msg);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    //发送文件类的消息
    public void sendFileMessage(String filepath, String msgType) {
        try {
            if (TextUtils.isEmpty(filepath)) {
                return;
            }
            MessageInfo msg = buildSendMessage(
                    msgType,
                    System.currentTimeMillis(), "", filepath);
            saveMessage(msg);
            mChatView.addMessage(msg);
            upLoadFile(filepath, msg);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }


    //构建消息模型
    public MessageInfo buildSendMessage(String msgType, long time, String text,
                                        String location) {
        MessageInfo msg = new MessageInfo();
        msg.setMsgtype(msgType);
        msg.setTime(time);
        msg.setMsgId(UdeskIdBuild.buildMsgId());
        msg.setDirection(UdeskConst.ChatMsgDirection.Send);
        msg.setSendFlag(UdeskConst.SendFlag.RESULT_SEND);
        msg.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
        msg.setMsgContent(text);
        msg.setPlayflag(UdeskConst.PlayFlag.NOPLAY);
        msg.setLocalPath(location);
        msg.setDuration(0);
        msg.setSubsessionid(mChatView.getAgentInfo().getIm_sub_session_id());
        msg.setSeqNum(UdeskDBManager.getInstance().getSubSessionId(mChatView.getAgentInfo().getIm_sub_session_id()));
        return msg;
    }

    //构建工单回复的消息转换消息模型
    public List<MessageInfo> buildLeaveMsgByTicketReplies(List<TicketReplieMode.ContentsBean> contents) {
        List<MessageInfo> msgInfos = new ArrayList<MessageInfo>();
        try {
            if (contents != null && contents.size() > 0) {
                MessageInfo eventMsg = new MessageInfo();
                eventMsg.setMsgtype(UdeskConst.ChatMsgTypeString.TYPE_EVENT);
                eventMsg.setTime(System.currentTimeMillis());
                eventMsg.setMsgId(UdeskIdBuild.buildMsgId());
                eventMsg.setDirection(UdeskConst.ChatMsgDirection.Recv);
                eventMsg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
                eventMsg.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
                eventMsg.setMsgContent(mChatView.getContext().getString(R.string.udesk_offline_reply_msg));
                eventMsg.setCreatedTime(contents.get(0).getReply_created_at());
                Collections.reverse(contents);
                boolean isAddEvent = false;
                for (TicketReplieMode.ContentsBean contentsBean : contents) {
                    if (contentsBean != null && !UdeskDBManager.getInstance().hasReceviedMsg(String.valueOf(contentsBean.getReply_id()))) {
                        MessageInfo msg = new MessageInfo();
                        msg.setMsgtype(UdeskConst.ChatMsgTypeString.TYPE_LEAVEMSG);
                        msg.setTime(System.currentTimeMillis());
                        msg.setMsgId(String.valueOf(contentsBean.getReply_id()));
                        msg.setDirection(UdeskConst.ChatMsgDirection.Recv);
                        msg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
                        msg.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
                        msg.setMsgContent(contentsBean.getReply_content());
                        msg.setPlayflag(UdeskConst.PlayFlag.NOPLAY);
                        msg.setLocalPath("");
                        msg.setDuration(0);
                        msg.setUser_avatar(contentsBean.getUser_avatar());
                        msg.setCreatedTime(contentsBean.getReply_created_at());
                        msg.setUpdateTime(contentsBean.getReply_updated_at());
                        msg.setReplyUser(contentsBean.getReply_user());
                        msgInfos.add(msg);
                        UdeskDBManager.getInstance().addMessageInfo(msg);
                        isAddEvent = true;
                    }
                }
                if (isAddEvent) {
                    msgInfos.add(0, eventMsg);
                    UdeskDBManager.getInstance().addMessageInfo(eventMsg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msgInfos;
    }

    public List<MessageInfo> tranferLogMessage(List<LogMessage> logMessages) {
        List<MessageInfo> msgInfos = new ArrayList<MessageInfo>();
        for (LogMessage logMessage : logMessages) {
            if (UdeskUtil.objectToString(logMessage.getSend_status()).equals("rollback")
                    || UdeskUtil.objectToString(logMessage.getStatus()).equals("system")) {
                break;
            }
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.setMsgtype(UdeskUtil.objectToString(logMessage.getType()));
            messageInfo.setTime(UdeskUtil.stringToLong(UdeskUtil.objectToString(logMessage.getCreated_at())));
            messageInfo.setMsgId(UdeskUtil.objectToString(logMessage.getMessage_id()));
            if (UdeskUtil.objectToString(logMessage.getReply_user_type()).equals("agent")) {
                messageInfo.setDirection(UdeskConst.ChatMsgDirection.Recv);
            } else {
                messageInfo.setDirection(UdeskConst.ChatMsgDirection.Send);
            }
            messageInfo.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
            messageInfo.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
            messageInfo.setMsgContent(UdeskUtil.objectToString(logMessage.getContent()));
            messageInfo.setPlayflag(UdeskConst.PlayFlag.NOPLAY);
            messageInfo.setLocalPath("");
            messageInfo.setDuration(UdeskUtil.objectToInt(logMessage.getDuration()));
            messageInfo.setSeqNum(UdeskUtil.objectToInt(logMessage.getSeq_num()));
            messageInfo.setSubsessionid(UdeskUtil.objectToString(logMessage.getIm_sub_session_id()));
            messageInfo.setReplyUser(UdeskUtil.objectToString(logMessage.getAgent_nick_name()));
            messageInfo.setUser_avatar(UdeskUtil.objectToString(logMessage.getAgent_avatar()));
            msgInfos.add(messageInfo);
        }
        return msgInfos;
    }

    //增加一条客户留言事件
    public void addCustomerLeavMsg() {
        try {
            MessageInfo msg = buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_EVENT,
                    System.currentTimeMillis(), mChatView.getContext().getString(R.string.udesk_customer_leavemsg), "");
            msg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
            saveMessage(msg);
            addEventMsg(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addEventMsg(MessageInfo msgInfo) {
        try {
            if (mChatView.getHandler() != null) {
                Message messge = mChatView.getHandler().obtainMessage(
                        MessageWhat.Add_UdeskEvent);
                messge.obj = msgInfo;
                mChatView.getHandler().sendMessage(messge);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //保存消息
    public void saveMessage(MessageInfo msg) {
        UdeskDBManager.getInstance().addMessageInfo(msg);
    }

    /**
     * 表情28个,最后一个标签显示删除了，只显示了27个
     *
     * @param id
     * @param emojiCount
     * @param emojiString
     */
    public void clickEmoji(long id, int emojiCount, String emojiString) {
        try {
            if (id == (emojiCount - 1)) {
                String str = mChatView.getInputContent().toString();
                CharSequence text = mChatView.getInputContent();
                int selectionEnd = Selection.getSelectionEnd(text);
                String string = str.substring(0, selectionEnd);
                if (string.length() > 0) {

                    String deleteLastEmotion = deleteLastEmotion(string);
                    if (deleteLastEmotion.length() > 0) {

                        mChatView.refreshInputEmjio(deleteLastEmotion
                                + str.substring(selectionEnd));
                    } else {
                        mChatView.refreshInputEmjio(""
                                + str.substring(selectionEnd));
                    }
                    CharSequence c = mChatView.getInputContent();
                    if (c instanceof Spannable) {
                        Spannable spanText = (Spannable) c;
                        Selection
                                .setSelection(spanText, deleteLastEmotion.length());
                    }
                }
            } else {
                CharSequence text = mChatView.getInputContent();
                int selectionEnd = Selection.getSelectionEnd(text);
                String editString = text.toString().substring(0, selectionEnd)
                        + emojiString + text.toString().substring(selectionEnd);
                mChatView.refreshInputEmjio(editString);
                CharSequence c = mChatView.getInputContent();
                if (c instanceof Spannable) {
                    Spannable spanText = (Spannable) c;
                    Selection.setSelection(spanText,
                            selectionEnd + emojiString.length());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //删除表情
    private String deleteLastEmotion(String str) {
        try {
            if (TextUtils.isEmpty(str)) {
                return "";
            }
            try {
                List<String> emotionList = mChatView.getEmotionStringList();
                int lastIndexOf = str.lastIndexOf(UDEmojiAdapter.EMOJI_PREFIX);
                if (lastIndexOf > -1) {
                    String substring = str.substring(lastIndexOf);
                    boolean contains = emotionList.contains(substring);
                    if (contains) {
                        return str.substring(0, lastIndexOf);
                    }
                }
                return str.substring(0, str.length() - 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // 开始录音
    public void recordStart() {
        // HorVoiceView负责界面。AudioRecordingAacThread负责具体录音。RecordTouchListener则负责手势判断
        // 在此之前，请确保SD卡是可以使用的
        // 后台录音开始
        try {
            mVoiceRecord = new AudioRecordingAacThread();
            mRecordTmpFile = UdeskUtil.getOutputAudioPath(mChatView.getContext());
            mVoiceRecord.initResource(mRecordTmpFile, new AudioRecordState() {
                @Override
                public void onRecordingError() {
                    if (mChatView.getHandler() != null) {
                        mChatView.getHandler().sendEmptyMessage(
                                MessageWhat.RECORD_ERROR);
                    }
                }

                @Override
                public void onRecordSuccess(final String resultFilePath,
                                            long duration) {
                    mChatView.onRecordSuccess(resultFilePath, duration);
                }

                @Override
                public void onRecordSaveError() {
                }

                @Override
                public void onRecordTooShort() {
                    if (mChatView.getHandler() != null) {
                        mChatView.getHandler().sendEmptyMessage(
                                MessageWhat.RECORD_Too_Short);
                    }
                }

                @Override
                public void onRecordCancel() {

                }

                @Override
                public void updateRecordState(int micAmplitude) {

                    if (mChatView.getHandler() != null) {
                        Message message = mChatView.getHandler().obtainMessage(
                                MessageWhat.UPDATE_VOCIE_STATUS);
                        message.arg1 = micAmplitude;
                        mChatView.getHandler().sendMessage(message);
                    }
                }

                @Override
                public void onRecordllegal() {
                    // 停止录音，提示开取录音权限
                    if (mChatView.getHandler() != null) {
                        mChatView.getHandler().sendEmptyMessage(
                                MessageWhat.recordllegal);
                    }

                }
            });
            mVoiceRecord.startRecord();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doRecordStop(boolean isCancel) {
        // 结束后台录音功能
        try {
            if (mVoiceRecord != null) {
                if (isCancel) {
                    mVoiceRecord.cancelRecord();

                } else {
                    mVoiceRecord.stopRecord();
                }
                mVoiceRecord = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    private void uploadFileByStrategy(String filePath, final MessageInfo message) {
//        File file = new File(filePath);
//        String fileName = file.getName();
//        UdeskUploadManager uploadManager = new UdeskUploadManager();
//        uploadManager.uploadFile(UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
//                UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
//                UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
//                UdeskSDKManager.getInstance().getAppId(mChatView.getContext()), fileName, filePath, message,
//                new UdeskUploadCallBack() {
//
//                    @Override
//                    public void onSuccess(MessageInfo info, String url) {
//                        UdeskDBManager.getInstance().updateMsgContent(info.getMsgId(),
//                                url);
//                        info.setMsgContent(url);
//                        messageSave(info);
//                    }
//
//                    @Override
//                    public void progress(MessageInfo info, String key, float percent) {
//                        try {
//                            if (mChatView != null && mChatView.getHandler() != null) {
//                                Message message = mChatView.getHandler().obtainMessage(
//                                        MessageWhat.ChangeFielProgress);
//                                message.obj = key;
//                                int progress = new Float(percent * 100).intValue();
//                                message.arg1 = progress;
//                                mChatView.getHandler().sendMessage(message);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(MessageInfo info, String key) {
//                        if (mChatView.getHandler() != null) {
//                            Message message = mChatView.getHandler().obtainMessage(
//                                    MessageWhat.changeImState);
//                            message.obj = info.getMsgId();
//                            message.arg1 = UdeskConst.SendFlag.RESULT_FAIL;
//                            mChatView.getHandler().sendMessage(message);
//                        }
//                        UdeskDBManager.getInstance().updateMsgSendFlag(info.getMsgId(),
//                                UdeskConst.SendFlag.RESULT_FAIL);
//                    }
//                });
//    }

    //上传图片文件
    private void upLoadFile(String filePath, MessageInfo message) {
//        uploadFileByStrategy(filePath, message);
        try {
            Configuration config = new Configuration.Builder()
                    .chunkSize(1024 * 1024)
                    .putThreshhold(1024 * 1024)
                    .connectTimeout(5)
                    .useHttps(true)
                    .build();
            // 实例化一个上传的实例
            com.qiniu.android.storage.UploadManager uploadManager = new com.qiniu.android.storage.UploadManager(config);
            if (mMyUpCompletionHandler == null) {
                mMyUpCompletionHandler = new MyUpCompletionHandler();
            }
            String key = message.getMsgId();
            mMyUpCompletionHandler.putCacheMessage(key, message);
            uploadManager.put(filePath, key,
                    XmppInfo.getInstance().getQiniuToken(),
                    mMyUpCompletionHandler,
                    new com.qiniu.android.storage.UploadOptions(null, null, false,
                            mUpProgressHandler, null));
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    //提交满意度调查出错
    private void sendSurveyerror() {
        if (mChatView.getHandler() != null) {
            Message message = mChatView.getHandler().obtainMessage(
                    MessageWhat.Survey_error);
            mChatView.getHandler().sendMessage(message);
            mChatView.setIsPermmitSurvy(true);
        }
    }

    /**
     * 七牛上传进度
     */
    private com.qiniu.android.storage.UpProgressHandler mUpProgressHandler = new com.qiniu.android.storage.UpProgressHandler() {
        public void progress(String key, double percent) {
            try {
                if (mChatView != null && mChatView.getHandler() != null) {
                    Message message = mChatView.getHandler().obtainMessage(
                            MessageWhat.ChangeFielProgress);
                    message.obj = key;
                    int progress = new Double(percent * 100).intValue();
                    message.arg1 = progress;
                    mChatView.getHandler().sendMessage(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 七牛上传完成
     */
    class MyUpCompletionHandler implements UpCompletionHandler {

        private Map<String, MessageInfo> mToMsgMap = new HashMap<String, MessageInfo>();

        public MyUpCompletionHandler() {

        }

        public void putCacheMessage(String md5, MessageInfo message) {
            mToMsgMap.put(md5, message);
        }

        @Override
        public void complete(String key, ResponseInfo info, JSONObject response) {

            try {
                MessageInfo msg = mToMsgMap.get(key);
                if (key != null && null != response && response.has("key")
                        && msg != null) {
                    if (UdeskCoreConst.isDebug) {
                        Log.i("DialogActivityPresenter", "UpCompletion : key="
                                + key + "\ninfo=" + info.toString() + "\nresponse="
                                + response.toString());
                    }
                    String qiniuKey = response.optString("key");
                    String qiniuUrl = UdeskCoreConst.UD_QINIU_UPLOAD + qiniuKey;
                    UdeskDBManager.getInstance().updateMsgContent(msg.getMsgId(),
                            qiniuUrl);
                    msg.setMsgContent(qiniuUrl);
                    messageSave(msg);
                    mToMsgMap.remove(key);
                } else {
                    if (mChatView.getHandler() != null) {
                        Message message = mChatView.getHandler().obtainMessage(
                                MessageWhat.changeImState);
                        message.obj = msg.getMsgId();
                        message.arg1 = UdeskConst.SendFlag.RESULT_FAIL;
                        mChatView.getHandler().sendMessage(message);
                    }
                    UdeskDBManager.getInstance().updateMsgSendFlag(msg.getMsgId(),
                            UdeskConst.SendFlag.RESULT_FAIL);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }
    }


//    //5秒检查下是否需要重发消息
//    public void selfretrySendMsg() {
//        try {
//            if (mChatView.getHandler() != null) {
//                Log.i("xxxxxxx","selfretrySendMsg");
//                mChatView.getHandler().postDelayed(runnable, 5000);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    Runnable runnable = new Runnable() {
//        @Override
//        public void run() {
//            try {
//                if (mChatView != null && mChatView.getHandler() != null) {
//                    updateSendFailedFlag();
//                    retrySendMsg();
//                    mChatView.getHandler().postDelayed(this, 5000);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    };

//    public void removeCallBack() {
//        try {
//            if (mChatView != null && mChatView.getHandler() != null && runnable != null) {
//                mChatView.getHandler().removeCallbacks(runnable);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    //自动重发消息
//    private void retrySendMsg() {
//        try {
//            if (!UdeskUtils.isNetworkConnected(mChatView.getContext())) {
//                return;
//            }
//            List<String> retryMsgIds = UdeskDBManager.getInstance()
//                    .getNeedRetryMsg(System.currentTimeMillis());
//            if (retryMsgIds == null || retryMsgIds.isEmpty()) {
//                return;
//            }
//            if (retryMsgIds != null) {
//                for (String msgID : retryMsgIds) {
//                    MessageInfo msg = UdeskDBManager.getInstance().getMessage(msgID);
//                    if (msg != null) {
//                        messageSave(msg);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    //点击失败按钮 重试发送消息
    public void startRetryMsg(MessageInfo message) {
        try {
            if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_TEXT) || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_Location)) {
                messageSave(message);
            } else if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_IMAGE)
                    || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_AUDIO)
                    || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_VIDEO)) {
                upLoadFile(message.getLocalPath(), message);
            } else if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_LEAVEMSG)) {
                putLeavesMsg(UdeskBaseInfo.customerId, message.getMsgContent(), message.getMsgId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

//    //更新发送中的消息为发送失败
//    private void updateSendFailedFlag() {
//        try {
//            if (!UdeskUtils.isNetworkConnected(mChatView.getContext())) {
//
//                return;
//            }
//            Log.i("xxxxxxx","updateSendFailedFlag");
//            List<String> msgIds = UdeskDBManager.getInstance()
//                    .getNeedUpdateFailedMsg(System.currentTimeMillis());
//            if (msgIds == null || msgIds.isEmpty()) {
//                return;
//            }
//            for (String msgId : msgIds) {
//                if (mChatView.getHandler() != null) {
//                    Message message = mChatView.getHandler().obtainMessage(
//                            MessageWhat.changeImState);
//                    message.obj = msgId;
//                    message.arg1 = UdeskConst.SendFlag.RESULT_FAIL;
//                    mChatView.getHandler().sendMessage(message);
//                }
//                UdeskDBManager.getInstance().deleteSendingMsg(msgId);
//                UdeskDBManager.getInstance().updateMsgSendFlag(msgId,
//                        UdeskConst.SendFlag.RESULT_FAIL);
//
//            }
//            createIMCustomerInfo();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    //下载文件
    public void downFile(final MessageInfo info) {

        try {
            final File file = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),
                    UdeskUtil.buildFileName(info.getMsgId(), info.getMsgContent()));
            UdeskHttpFacade.getInstance().downloadFile(file.getAbsolutePath(), info.getMsgContent(), new UdeskHttpCallBack() {


                @Override
                public void onSuccess(byte[] t) {
                    UdeskDBManager.getInstance().updateMsgLoaclUrl(info.getMsgId(), file.getAbsolutePath());
                }

                @Override
                public void onFailure(int errorNo, String strMsg) {
                    if (mChatView != null && mChatView.getHandler() != null) {
                        Message message = mChatView.getHandler().obtainMessage(
                                MessageWhat.ChangeFielProgress);
                        message.obj = info.getMsgId();
                        message.arg1 = 0;
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(UdeskConst.FileDownIsSuccess, false);
                        message.setData(bundle);
                        mChatView.getHandler().sendMessage(message);
                    }
                }

                @Override
                public void onLoading(long count, long current) {

                    if (mChatView != null && mChatView.getHandler() != null) {

                        double percent = current / (double) count;
                        Message message = mChatView.getHandler().obtainMessage(
                                MessageWhat.ChangeFielProgress);
                        message.obj = info.getMsgId();
                        int progress = new Double(percent * 100).intValue();
                        message.arg1 = progress;
                        Bundle bundle = new Bundle();
                        bundle.putLong(UdeskConst.FileSize, count);
                        bundle.putBoolean(UdeskConst.FileDownIsSuccess, true);
                        message.setData(bundle);
                        mChatView.getHandler().sendMessage(message);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private ExecutorService scaleExecutor;

    private void ensureMessageExecutor() {
        if (scaleExecutor == null) {
            scaleExecutor = Concurrents
                    .newSingleThreadExecutor("scaleExecutor");
        }
    }

    public void scaleBitmap(final String path) {
        if (!TextUtils.isEmpty(path)) {
            ensureMessageExecutor();
            scaleExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap scaleImage = null;
                        byte[] data = null;
                        int max = 0;
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        /**
                         * 在不分配空间状态下计算出图片的大小
                         */
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(path, options);
                        int width = options.outWidth;
                        int height = options.outHeight;
                        max = Math.max(width, height);
                        options.inTempStorage = new byte[100 * 1024];
                        options.inJustDecodeBounds = false;
                        options.inPurgeable = true;
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        InputStream inStream = new FileInputStream(path);
                        data = readStream(inStream);
                        if (data == null || data.length <= 0) {
                            sendBitmapMessage(path);
                            return;
                        }
                        String imageName = UdeskUtils.MD5(data);
                        File tempFile = new File(path.trim());
                        String fileName = tempFile.getName();
                        if (fileName.isEmpty()) {
                            fileName = UdeskConst.ORIGINAL_SUFFIX;
                        }
                        File scaleImageFile = UdeskUtil.getOutputMediaFile(mChatView.getContext(), imageName
                                + fileName);
                        if (!scaleImageFile.exists()) {
                            // 缩略图不存在，生成上传图
                            if (max > UdeskConfig.ScaleMax) {
                                options.inSampleSize = max / UdeskConfig.ScaleMax;
                            } else {
                                options.inSampleSize = 1;
                            }
                            FileOutputStream fos = new FileOutputStream(scaleImageFile);
                            scaleImage = BitmapFactory.decodeByteArray(data, 0,
                                    data.length, options);
                            scaleImage.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                            fos.close();
                            fos = null;
                        }

                        if (scaleImage != null) {
                            scaleImage.recycle();
                            scaleImage = null;
                        }
                        data = null;
                        if (TextUtils.isEmpty(scaleImageFile.getPath())) {
                            sendBitmapMessage(path);
                        } else {
                            sendBitmapMessage(scaleImageFile.getPath());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError error) {
                        error.printStackTrace();
                    }
                }
            });
        }
    }


    /**
     * @param inStream
     * @return byte[]
     * @throws Exception
     */
    public byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;

    }


}
