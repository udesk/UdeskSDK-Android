package cn.udesk.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.KeyGenerator;
import com.qiniu.android.storage.Recorder;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.storage.persistent.FileRecorder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import cn.udesk.JsonUtils;
import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.activity.UdeskChatActivity.MessageWhat;
import cn.udesk.config.UdeskConfig;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.messagemanager.Concurrents;
import cn.udesk.messagemanager.UdeskMessageManager;
import cn.udesk.model.LogMessage;
import cn.udesk.model.SDKIMSetting;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.model.TicketReplieMode;
import cn.udesk.model.UdeskCommodityItem;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.event.InvokeEventContainer;
import udesk.core.http.UdeskHttpCallBack;
import udesk.core.model.AgentInfo;
import udesk.core.model.MessageInfo;
import udesk.core.model.Product;
import udesk.core.utils.UdeskIdBuild;
import udesk.core.utils.UdeskUtils;
import udesk.core.xmpp.XmppInfo;

public class ChatActivityPresenter {

    private IChatActivityView mChatView;

    private String customerId;

    //处理七牛上传完成的回调
    MyUpCompletionHandler mMyUpCompletionHandler = null;
    UploadManager uploadManager = null;

    private List<MessageInfo> cachePreMsg = new ArrayList<>();

    private String leavMsgId = "";

    //保存发送中的消息， 收到回执后移除，或者messagesav两次成功后移除，
    // 之后再ui界面上展示发送成功，否则显示转圈
    // 会轮询重发
    private Map<String, MessageInfo> sendingMsgCache = Collections.synchronizedMap(new LinkedHashMap<String, MessageInfo>());

    public interface IUdeskHasSurvyCallBack {

        void hasSurvy(boolean hasSurvy);
    }

    public ChatActivityPresenter(IChatActivityView chatview) {
        this.mChatView = chatview;
        bindEevent();
    }

    public void clearMsg() {
        try {
            if (cachePreMsg != null) {
                cachePreMsg.clear();
            }
            if (sendingMsgCache != null) {
                sendingMsgCache.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        InvokeEventContainer.getInstance().event_OnSendMessageFail.bind(this, "onSendMessageFail");
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
        InvokeEventContainer.getInstance().event_OnSendMessageFail.unBind(this);
    }

    public synchronized void onSendMessageFail(final MessageInfo msg) {

//        xmpp发送失败后 再次调用messageSave  触发后端代发xmpp
        try {
            if (TextUtils.isEmpty(customerId) || msg == null) {
                return;
            }

            UdeskHttpFacade.getInstance().messageSave(UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                    customerId, mChatView.getAgentInfo().getAgent_id(),
                    msg.getSubsessionid(), UdeskConst.UdeskSendStatus.sending,
                    msg.getMsgtype(), msg.getMsgContent(), msg.getMsgId(),
                    msg.getDuration(), msg.getSeqNum(), msg.getFilename(),
                    msg.getFilesize(), UdeskUtils.getSecondTimestamp(new Date()) - UdeskConst.active_time,
                    UdeskConst.sdk_page_status + UdeskConst.sdk_xmpp_statea, new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            try {
                                MessageInfo cacheMsg = sendingMsgCache.get(msg.getMsgId());
//                       发送2次也算成功，有服务端代发通知客服
                                if (cacheMsg != null && (cacheMsg.getCount() + 1) >= 2) {
                                    onMessageReceived(msg.getMsgId());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String message) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onVideoEvent(String event, String id, String message, Boolean isInvite) {

        try {
            if (event.equals(UdeskConst.ReceiveType.StartMedio)) {
                MessageInfo messageInfo = buildVideoEventMsg(id, isInvite, message);
                saveMessage(messageInfo);
            } else if (event.equals(UdeskConst.ReceiveType.Cancle)) {
                //取消
                UdeskDBManager.getInstance().updateMsgContent(id,
                        message);
                MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                mChatView.addMessage(eventMsg);

            } else if (event.equals(UdeskConst.ReceiveType.Busy)) {
                message = mChatView.getAgentInfo().getAgentNick() + message;
                UdeskDBManager.getInstance().updateMsgContent(id, message);
                MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                mChatView.addMessage(eventMsg);
            } else if (event.equals(UdeskConst.ReceiveType.Timeout)) {
                message = mChatView.getAgentInfo().getAgentNick() + message;
                UdeskDBManager.getInstance().updateMsgContent(id,
                        message);
                MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                mChatView.addMessage(eventMsg);
            } else if (event.equals(UdeskConst.ReceiveType.Reject)) {
                UdeskDBManager.getInstance().updateMsgContent(id,
                        message);
                MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                mChatView.addMessage(eventMsg);
            } else if (event.equals(UdeskConst.ReceiveType.Over)) {
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
            msg.setCustomerId(UdeskUtils.objectToString(customerId));
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
            UdeskDBManager.getInstance().updateMsgSendFlag(msgId, UdeskConst.SendFlag.RESULT_SUCCESS);
            sendingMsgCache.remove(msgId);
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
        try {
            if (!TextUtils.isEmpty(customerId)) {
                getTicketReplies(customerId, 1, UdeskConst.UDESK_HISTORY_COUNT, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                    JSONObject resultJson = new JSONObject(string);
                    customerId = JsonUtils.parserCustomers(resultJson);
                    if (UdeskSDKManager.getInstance().getImSetting() != null && !UdeskSDKManager.getInstance().getImSetting().getIn_session()) {
                        boolean isShowPression = false;
                        String preTitle = "";
                        if (resultJson.has("pre_session")) {
                            JSONObject preJson = resultJson.getJSONObject("pre_session");
                            isShowPression = UdeskUtils.objectToBoolean(preJson.opt("show_pre_session"));
                            preTitle = UdeskUtils.objectToString(preJson.opt("pre_session_title"));
                        }
                        //在无消息对话过滤状态下,并且没有创建会话的情况下,先不请求agent,请求无消息会话创建接口
                        if (isShowPression) {
                            Message handlerMsg = mChatView.getHandler().obtainMessage(
                                    MessageWhat.pre_session_status);
                            handlerMsg.obj = preTitle;
                            mChatView.getHandler().sendMessage(handlerMsg);
                            getPressionInfo();
                            updateUserInfo(customerId, false);
                        } else {
                            updateUserInfo(customerId, true);
                        }
                    } else {
                        updateUserInfo(customerId, true);
                    }
                }
                if (!TextUtils.isEmpty(customerId)) {
                    //拉取工单回复的消息
                    getTicketReplies(customerId, 1, UdeskConst.UDESK_HISTORY_COUNT, "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------以上是注册方法，通过观察者模式通知处理的逻辑部分 注册的方法，必须是public,方法得参数必须是class-------------------


    // ---------------以下是http请求接口 及处理逻辑 -------------------
    //请求获取客户信息的入口， 请求处理完后  会通知到onCreateCustomer 方法
    public void createIMCustomerInfo() {
        try {
            final Context mContext = mChatView.getContext();
            UdeskHttpFacade.getInstance().getIMSettings(UdeskSDKManager.getInstance().getDomain(mContext),
                    UdeskSDKManager.getInstance().getAppkey(mContext),
                    UdeskSDKManager.getInstance().getSdkToken(mContext),
                    UdeskSDKManager.getInstance().getAppId(mContext), new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            try {
                                SDKIMSetting imSetting = JsonUtils.parserIMSettingJson(message);
                                if (imSetting!=null){
                                    UdeskSDKManager.getInstance().setImSetting(imSetting);

                                }
                                setUserInfo(mContext);
                            } catch (Exception e) {
                                setUserInfo(mContext);
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            setUserInfo(mContext);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUserInfo(Context mContext) {
        String sdkToken = UdeskSDKManager.getInstance().getSdkToken(mContext);
        UdeskHttpFacade.getInstance().setUserInfo(mContext, UdeskSDKManager.getInstance().getDomain(mContext),
                UdeskSDKManager.getInstance().getAppkey(mContext), sdkToken,
                UdeskSDKManager.getInstance().getUdeskConfig().defualtUserInfo,
                UdeskSDKManager.getInstance().getUdeskConfig().definedUserTextField,
                UdeskSDKManager.getInstance().getUdeskConfig().definedUserRoplist,
                UdeskSDKManager.getInstance().getAppId(mContext), UdeskSDKManager.getInstance().getUdeskConfig().channel, null);
    }


    //无消息会话创建
    private void getPressionInfo() {
        try {
            UdeskHttpFacade.getInstance().getPreSessionsInfo(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    mChatView.getAgentId(), mChatView.getGroupId(), false,
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                    new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            try {
                                if (!UdeskMessageManager.getInstance().isConnection()) {
                                    UdeskMessageManager.getInstance().connection();
                                }
                                JSONObject json = new JSONObject(message);
                                if (json.has("pre_session_id")) {
                                    mChatView.updatePreSessionStatus(json.optString("pre_session_id"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFail(String message) {
                            // 失败给出错误提示 结束流程
                            try {
                                mChatView.showFailToast(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //请求分配客服信息
    public void getAgentInfo(String preSessionId, JSONObject preMessage) {
        try {
            UdeskHttpFacade.getInstance().getAgentInfo(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    mChatView.getAgentId(), mChatView.getGroupId(), false,
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()), preSessionId,
                    preMessage, new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            try {
                                AgentInfo agentInfo = JsonUtils.parseAgentResult(message);
                                if (agentInfo.getAgentCode() == 2000) {
                                    getIMStatus(agentInfo);
                                } else {
                                    mChatView.dealAgentInfo(agentInfo);
                                    mChatView.updatePreSessionStatus("");
                                }
                                if (!UdeskMessageManager.getInstance().isConnection()) {
                                    UdeskMessageManager.getInstance().connection();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            try {
                                // 失败给出错误提示 结束流程
                                mChatView.showFailToast(message);
                                mChatView.updatePreSessionStatus("");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
                    mChatView.updatePreSessionStatus("");
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
                                    mChatView.updatePreSessionStatus("");
                                    return;
                                }
                                mChatView.setAgentInfo(agentInfo);
                                if (mChatView.getHandler() != null) {
                                    Message message = mChatView.getHandler().obtainMessage(
                                            MessageWhat.IM_STATUS);
                                    message.obj = imStatus;
                                    mChatView.getHandler().sendMessage(message);
                                }
                                mChatView.updatePreSessionStatus("");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String s) {
                            try {
                                if (mChatView.getHandler() != null) {
                                    Message message = mChatView.getHandler().obtainMessage(
                                            MessageWhat.IM_STATUS);
                                    message.obj = "off";
                                    mChatView.getHandler().sendMessage(message);
                                    mChatView.updatePreSessionStatus("");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
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
                    agent_id, group_id, true, UdeskSDKManager.getInstance().getAppId(mChatView.getContext()), null,
                    null, new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            // 获取客户成功，显示在线客服的信息，连接xmpp，进行会话
                            try {
                                AgentInfo agentInfo = JsonUtils.parseAgentResult(message);
                                mChatView.dealRedirectAgentInfo(agentInfo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            // 失败给出错误提示 结束流程
                            try {
                                mChatView.showFailToast(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
                            try {
                                TicketReplieMode replieMode = JsonUtils.parserTicketReplie(message);
                                if (replieMode != null && replieMode.getContents() != null) {
                                    List<MessageInfo> messageInfos = buildLeaveMsgByTicketReplies(replieMode.getContents());
                                    if (messageInfos != null && mChatView.getHandler() != null) {
                                        Message msg = Message.obtain();
                                        msg.what = MessageWhat.loadHistoryDBMsg;
                                        msg.arg1 = UdeskChatActivity.PullEventModel;
                                        msg.obj = messageInfos;
                                        mChatView.getHandler().sendMessage(msg);
                                    }
                                }
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
                            try {
                                UdeskDBManager.getInstance().updateMsgSendFlag(msgId, UdeskConst.SendFlag.RESULT_SUCCESS);
                                onMessageReceived(msgId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String msg) {
                            try {
                                UdeskDBManager.getInstance().updateMsgSendFlag(msgId, UdeskConst.SendFlag.RESULT_FAIL);
                                if (mChatView.getHandler() != null) {
                                    Message message = mChatView.getHandler().obtainMessage(
                                            MessageWhat.changeImState);
                                    message.obj = msgId;
                                    message.arg1 = UdeskConst.SendFlag.RESULT_FAIL;
                                    mChatView.getHandler().sendMessage(message);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //更新用户信息
    private void updateUserInfo(final String userId, final boolean isNeedGetAgent) {
        try {
            if (TextUtils.isEmpty(userId)) {
                if (isNeedGetAgent) {
                    getAgentInfo(null, null);
                }
                return;
            }
            UdeskConfig config = UdeskSDKManager.getInstance().getUdeskConfig();
            if (config.updateDefualtUserInfo != null
                    || config.updatedefinedUserTextField != null
                    || config.updatedefinedUserRoplist != null) {
                UdeskHttpFacade.getInstance().updateUserInfo(config.updateDefualtUserInfo,
                        config.updatedefinedUserTextField,
                        config.updatedefinedUserRoplist, userId,
                        UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                        UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                        UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                        new UdeskCallBack() {
                            @Override
                            public void onSuccess(String message) {
                                try {
                                    if (isNeedGetAgent) {
                                        getAgentInfo(null, null);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFail(String message) {
                                try {
                                    if (isNeedGetAgent) {
                                        getAgentInfo(null, null);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            } else {
                if (isNeedGetAgent) {
                    getAgentInfo(null, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (isNeedGetAgent) {
                getAgentInfo(null, null);
            }
        }

    }

    //客户主动发起满意度调查，先获取是否评价
    public void getHasSurvey(String agent_id, final IUdeskHasSurvyCallBack hasSurvyCallBack) {
        try {
            if (TextUtils.isEmpty(customerId)) {
                mChatView.setIsPermmitSurvy(true);
                return;
            }
            UdeskHttpFacade.getInstance().hasSurvey(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    agent_id, customerId,
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
                            try {
                                if (hasSurvyCallBack != null) {
                                    hasSurvyCallBack.hasSurvy(true);
                                } else {
                                    //出错给提示
                                    sendSurveyerror();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
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
            UdeskHttpFacade.getInstance().getIMSurveyOptionsNew(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()), new UdeskCallBack() {

                        @Override
                        public void onSuccess(String message) {
                            try {
                                SurveyOptionsModel model = JsonUtils.parseSurveyOptions(message);
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
                                    sendSurveyerror();
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
                sendSurveyerror();
            }
        }
    }

    //提交调查选项内容
    public void putIMSurveyResult(String optionId, String show_type, String survey_remark, String tags) {
        if (TextUtils.isEmpty(customerId)) {
            try {
                mChatView.setIsPermmitSurvy(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            UdeskHttpFacade.getInstance().putSurveyVote(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    mChatView.getAgentInfo().getAgent_id(), customerId,
                    optionId, UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                    mChatView.getAgentInfo().getIm_sub_session_id(), show_type,
                    survey_remark, tags, new UdeskCallBack() {

                        @Override
                        public void onSuccess(String string) {
                            try {
                                Message message = mChatView.getHandler().obtainMessage(
                                        MessageWhat.Survey_Success);
                                mChatView.getHandler().sendMessage(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            sendSurveyerror();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //客户端返回会话界面，在排队中通知移除排队
    public void quitQuenu(String quitMode) {
        try {
            UdeskHttpFacade.getInstance().quitQueue(
                    UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
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

    // ---------------以上是http请求接口 及处理逻辑 -------------------


    // ---------------以下是发送消息的业务逻辑 -------------------

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

    private boolean isNeedAddCachePre(MessageInfo msg) {
        try {
            if (mChatView.getPressionStatus(msg)) {
                cachePreMsg.add(msg);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //封装发送文本消息
    public void sendTxtMessage(String msgString) {
        try {
            MessageInfo msg = buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_TEXT,
                    System.currentTimeMillis(), msgString, "", "", "");
            saveMessage(msg);
            mChatView.clearInputContent();
            mChatView.addMessage(msg);
            if (isNeedAddCachePre(msg)) {
                return;
            }
            if (mChatView.isNeedQueueMessageSave()) {
                queueMessageSave(msg);
                return;
            }
            messageSave(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendProductMessage(Product mProduct) {

        if (mProduct == null) {
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            if (!TextUtils.isEmpty(mProduct.getName())) {
                jsonObject.put("name", mProduct.getName());
            }
            if (!TextUtils.isEmpty(mProduct.getUrl())) {
                jsonObject.put("url", mProduct.getUrl());
            }
            if (!TextUtils.isEmpty(mProduct.getImgUrl())) {
                jsonObject.put("imgUrl", mProduct.getImgUrl());
            }

            List<Product.ParamsBean> params = mProduct.getParams();
            if (params != null && params.size() > 0) {
                JSONArray jsonsArray = new JSONArray();
                for (Product.ParamsBean paramsBean : params) {
                    JSONObject param = new JSONObject();
                    param.put("text", paramsBean.getText());
                    param.put("color", paramsBean.getColor());
                    param.put("fold", paramsBean.isFold());
                    param.put("break", paramsBean.isBreakX());
                    param.put("size", paramsBean.getSize());
                    jsonsArray.put(param);
                }

                jsonObject.put("params", jsonsArray);
            }

            MessageInfo msg = buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_PRODUCT,
                    System.currentTimeMillis(), jsonObject.toString(), "", "", "");
            saveMessage(msg);
            mChatView.addMessage(msg);
            if (isNeedAddCachePre(msg)) {
                return;
            }
            if (mChatView.isNeedQueueMessageSave()) {
                queueMessageSave(msg);
                return;
            }
            messageSave(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送原图图片消息
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
            File scaleImageFile = new File(UdeskUtils.getDirectoryPath(mChatView.getContext(), UdeskConst.FileImg) + File.separator + UdeskConst.ORIGINAL_SUFFIX);
            if (scaleImageFile != null) {
                if (max > UdeskSDKManager.getInstance().getUdeskConfig().ScaleMax) {
                    factoryOptions.inSampleSize = max / UdeskSDKManager.getInstance().getUdeskConfig().ScaleMax;
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
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                bitmap.recycle();
                bitmap = null;
                if (TextUtils.isEmpty(scaleImageFile.getPath())) {
                    UdeskUtils.showToast(mChatView.getContext(), mChatView.getContext().getString(R.string.udesk_upload_img_error));
                    return;
                }
                MessageInfo msg = buildSendMessage(
                        UdeskConst.ChatMsgTypeString.TYPE_IMAGE,
                        System.currentTimeMillis(), "", scaleImageFile.getPath(), "", "");
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

    //发送文件类的消息( 包含视频 文件 图片)

    /**
     * @param filepath
     * @param msgType  图片:UdeskConst.ChatMsgTypeString.TYPE_IMAGE
     *                 文件:UdeskConst.ChatMsgTypeString.TYPE_File
     *                 MP4视频: UdeskConst.ChatMsgTypeString.TYPE_VIDEO
     */
    public void sendFileMessage(String filepath, String msgType) {
        try {
            if (TextUtils.isEmpty(filepath)) {
                return;
            }
            String fileName = (UdeskUtils.getFileName(filepath, msgType));
            String fileSzie = UdeskUtils.getFileSizeByLoaclPath(filepath);
            MessageInfo msg = buildSendMessage(
                    msgType,
                    System.currentTimeMillis(), "", filepath, fileName, fileSzie);
            saveMessage(msg);
            mChatView.addMessage(msg);
            upLoadFile(filepath, msg);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * 发送地理位置信息
     *
     * @param lat
     * @param longitude
     * @param localvalue
     * @param bitmapDir
     */
    public void sendLocationMessage(double lat, double longitude, String localvalue, String bitmapDir) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(lat).append(";").append(longitude).append(";").append("16;").append(localvalue);
            MessageInfo msg = buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_Location,
                    System.currentTimeMillis(), builder.toString(), bitmapDir, "", "");
            saveMessage(msg);
            mChatView.addMessage(msg);
            if (isNeedAddCachePre(msg)) {
                return;
            }
            if (mChatView.isNeedQueueMessageSave()) {
                queueMessageSave(msg);
                return;
            }
            messageSave(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 发送录音信息
    public void sendRecordAudioMsg(String audiopath, long duration) {
        try {
            String fileName = (UdeskUtils.getFileName(audiopath, UdeskConst.FileAduio));
            MessageInfo msg = buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_AUDIO,
                    System.currentTimeMillis(), "", audiopath, fileName, "");
            duration = duration / 1000 + 1;
            msg.setDuration(duration);
            saveMessage(msg);
            mChatView.addMessage(msg);
            upLoadFile(audiopath, msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送商品链接广告
    public void sendCommodityMessage(UdeskCommodityItem commodityItem) {
        try {
            UdeskMessageManager.getInstance().sendComodityMessage(buildCommodityMessage(commodityItem),
                    mChatView.getAgentInfo().getAgentJid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //构造广告消息的格式
    private String buildCommodityMessage(UdeskCommodityItem item) {
        JSONObject root = new JSONObject();
        try {
            JSONObject dataJson = new JSONObject();
            JSONObject paramsJson = new JSONObject();
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


    //发送留言消息
    public void sendLeaveMessage() {
        try {
            if (TextUtils.isEmpty(customerId)) {
                return;
            }
            if (!TextUtils.isEmpty(mChatView.getInputContent().toString().trim())) {
                String msgString = mChatView.getInputContent().toString();
                MessageInfo msg = buildSendMessage(
                        UdeskConst.ChatMsgTypeString.TYPE_LEAVEMSG,
                        System.currentTimeMillis(), msgString, "", "", "");
                saveMessage(msg);
                mChatView.clearInputContent();
                mChatView.addMessage(msg);
                putLeavesMsg(customerId, msgString, msg.getMsgId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送留言消息
    public void sendLeaveMessage(String message) {
        try {
            if (TextUtils.isEmpty(customerId)) {
                return;
            }
            MessageInfo msg = buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_LEAVEMSG,
                    System.currentTimeMillis(), message, "", "", "");
            saveMessage(msg);
            mChatView.addMessage(msg);
            putLeavesMsg(customerId, message, msg.getMsgId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //拉取消息
    public void pullMessages(final int seqNum, final String subSessionId) {
        try {
            if (TextUtils.isEmpty(customerId)) {
                return;
            }
            UdeskHttpFacade.getInstance().getMessages(UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                    customerId, subSessionId, seqNum, new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            try {
                                JSONObject root = new JSONObject(message);
                                String code = UdeskUtils.objectToString(root.opt("code"));
                                if (code.equals("1002")) {
                                    int request_delay_time = UdeskUtils.objectToInt(root.opt("request_delay_time"));
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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onFail(String message) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //htpp 方式保存消息到后台
    public synchronized void messageSave(final MessageInfo msg) {
        try {
            if (TextUtils.isEmpty(customerId) || msg == null) {
                return;
            }
            if (mChatView != null && mChatView.getAgentInfo() != null){
                if (!TextUtils.isEmpty(mChatView.getAgentInfo().getAgentJid())){
                    msg.setmAgentJid(mChatView.getAgentInfo().getAgentJid());
                }
                if (!TextUtils.isEmpty(mChatView.getAgentInfo().getIm_sub_session_id())){
                    msg.setSubsessionid(mChatView.getAgentInfo().getIm_sub_session_id());
                }
            }
            sendingMsgCache.put(msg.getMsgId(), msg);
            UdeskHttpFacade.getInstance().messageSave(UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                    customerId, mChatView.getAgentInfo().getAgent_id(),
                    msg.getSubsessionid(), UdeskConst.UdeskSendStatus.sending,
                    msg.getMsgtype(), msg.getMsgContent(), msg.getMsgId(),
                    msg.getDuration(), msg.getSeqNum(), msg.getFilename(), msg.getFilesize(),
                    UdeskUtils.getSecondTimestamp(new Date()) - UdeskConst.active_time,
                    UdeskConst.sdk_page_status + UdeskConst.sdk_xmpp_statea, new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            try {
                                MessageInfo messageInfo = sendingMsgCache.get(msg.getMsgId());
                                if (messageInfo != null) {
                                    messageInfo.setCount();
                                }
                                // 发给当前的客服
                                msg.setNoNeedSave(true);
                                UdeskMessageManager.getInstance().sendMessage(msg);

                                JSONObject jsonObject = new JSONObject(message);
                                if (jsonObject.has("agent_seq_num")) {
                                    int agent_seq_num = jsonObject.optInt("agent_seq_num");
                                    //返回客服消息序列  大于本地存储的, 有丢失消息, 需要拉取消息
                                    if (agent_seq_num > mChatView.getAgentSeqNum()) {
                                        pullMessages(0, msg.getSubsessionid());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            // 发给当前的客服
                            try {
                                if (TextUtils.equals("8002", message)) {
                                    sendingMsgCache.remove(msg.getMsgId());
                                    updateFailureStatus(msg);
                                    if (mChatView.getHandler() != null) {
                                        mChatView.getHandler().sendEmptyMessage(MessageWhat.RECREATE_CUSTOMER_INFO);
                                    }
                                    return;
                                }
                                if (mChatView != null && mChatView.getAgentInfo() != null) {
                                    msg.setmAgentJid(mChatView.getAgentInfo().getAgentJid());
                                }
                                msg.setNoNeedSave(false);
                                UdeskMessageManager.getInstance().sendMessage(msg);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //htpp 方式保存消息到后台
    public void queueMessageSave(final MessageInfo msg) {
        try {
            if (TextUtils.isEmpty(customerId)) {
                return;
            }

            UdeskHttpFacade.getInstance().queueMessageSave(UdeskSDKManager.getInstance().getDomain(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppkey(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getSdkToken(mChatView.getContext()),
                    UdeskSDKManager.getInstance().getAppId(mChatView.getContext()),
                    customerId, msg.getMsgtype(), msg.getMsgContent(), msg.getMsgId(),
                    msg.getDuration(), msg.getSeqNum(), msg.getFilename(), msg.getFilesize(), new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {

                            try {
                                JSONObject resultJson = new JSONObject(message);
                                if (resultJson.has("code")) {
                                    int status = resultJson.getInt("code");
                                    if (status == 1000) {
                                        //保存成功
                                        UdeskDBManager.getInstance().updateMsgSendFlag(msg.getMsgId(), UdeskConst.SendFlag.RESULT_SUCCESS);
                                        onMessageReceived(msg.getMsgId());
                                    } else if (status == 9200) {
                                        String tipMsg = resultJson.getString("message");
                                        mChatView.isMoreThan(true, tipMsg);
                                        updateFailureStatus(msg);
                                    } else {
                                        updateFailureStatus(msg);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }

                        @Override
                        public void onFail(String error) {

                            updateFailureStatus(msg);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFailureStatus(MessageInfo msg) {
        try {
            if (msg == null) {
                return;
            }
            UdeskDBManager.getInstance().updateMsgSendFlag(msg.getMsgId(), UdeskConst.SendFlag.RESULT_FAIL);
            if (mChatView.getHandler() != null) {
                Message message = mChatView.getHandler().obtainMessage(
                        MessageWhat.changeImState);
                message.obj = msg.getMsgId();
                message.arg1 = UdeskConst.SendFlag.RESULT_FAIL;
                mChatView.getHandler().sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //构建消息模型
    public MessageInfo buildSendMessage(String msgType, long time, String text,
                                        String location, String fileName, String fileSize) {
        MessageInfo msg = new MessageInfo();
        try {
            msg.setCustomerId(UdeskUtils.objectToString(customerId));
            msg.setMsgtype(msgType);
            msg.setTime(time);
            msg.setmAgentJid(mChatView.getAgentInfo().getAgentJid());
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
            msg.setFilename(fileName);
            msg.setFilesize(fileSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            for (LogMessage logMessage : logMessages) {
                if (UdeskUtils.objectToString(logMessage.getSend_status()).equals("rollback") || UdeskUtils.objectToString(logMessage.getStatus()).equals("system")) {
                    continue;
                }
                MessageInfo messageInfo = new MessageInfo();
                messageInfo.setMsgtype(UdeskUtils.objectToString(logMessage.getType()));
                messageInfo.setTime(UdeskUtil.stringToLong(UdeskUtils.objectToString(logMessage.getCreated_at())));
                messageInfo.setMsgId(UdeskUtils.objectToString(logMessage.getMessage_id()));
                if (UdeskUtils.objectToString(logMessage.getReply_user_type()).equals("agent")) {
                    messageInfo.setDirection(UdeskConst.ChatMsgDirection.Recv);
                } else {
                    messageInfo.setDirection(UdeskConst.ChatMsgDirection.Send);
                }
                messageInfo.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
                messageInfo.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
                messageInfo.setMsgContent(UdeskUtils.objectToString(logMessage.getContent()));
                messageInfo.setPlayflag(UdeskConst.PlayFlag.NOPLAY);
                messageInfo.setLocalPath("");
                messageInfo.setDuration(UdeskUtils.objectToInt(logMessage.getDuration()));
                messageInfo.setSeqNum(UdeskUtils.objectToInt(logMessage.getSeq_num()));
                messageInfo.setSubsessionid(UdeskUtils.objectToString(logMessage.getIm_sub_session_id()));
                if (messageInfo.getInviterAgentInfo() != null){
                    messageInfo.setReplyUser(messageInfo.getInviterAgentInfo().getNick_name());
                    messageInfo.setUser_avatar(messageInfo.getInviterAgentInfo().getAvatar());
                    messageInfo.setmAgentJid(messageInfo.getInviterAgentInfo().getJid());
                }else {
                    messageInfo.setReplyUser(UdeskUtils.objectToString(logMessage.getAgent_nick_name()));
                    messageInfo.setUser_avatar(UdeskUtils.objectToString(logMessage.getAgent_avatar()));
                    messageInfo.setmAgentJid(UdeskUtils.objectToString(logMessage.getAgentJId()));
                }

                messageInfo.setFilesize(UdeskUtils.objectToString(logMessage.getFileSize()));
                messageInfo.setFilename(UdeskUtils.objectToString(logMessage.getFileName()));
                msgInfos.add(messageInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msgInfos;
    }

    //增加一条客户留言事件
    public void addCustomerLeavMsg() {
        try {
            MessageInfo msg = buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_EVENT,
                    System.currentTimeMillis(), mChatView.getContext().getString(R.string.udesk_customer_leavemsg), "", "", "");
            msg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
            saveMessage(msg);
            addEventMsg(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addLeavMsgWeclome(String content) {
        try {
            if (TextUtils.isEmpty(content)) {
                return;
            }
            MessageInfo msg = new MessageInfo();
            msg.setMsgtype(UdeskConst.ChatMsgTypeString.TYPE_RICH);
            if (TextUtils.isEmpty(leavMsgId)) {
                leavMsgId = UdeskIdBuild.buildMsgId();
            } else {
                return;
            }
            msg.setTime(System.currentTimeMillis());
            msg.setMsgId(leavMsgId);
            msg.setDirection(UdeskConst.ChatMsgDirection.Recv);
            msg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
            msg.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
            msg.setMsgContent(content);
            msg.setPlayflag(UdeskConst.PlayFlag.NOPLAY);
            //本地伪装成收到一条直接留言的欢迎语
            onNewMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addEventMsg(MessageInfo msgInfo) {
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
        try {
            UdeskDBManager.getInstance().addMessageInfo(msg);
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
//                                message.arg1 = new Float(percent * 100).intValue();
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

    protected volatile ConcurrentHashMap<String, Boolean> isCancleUpLoad = new ConcurrentHashMap<>();

    public void cancleUploadFile(MessageInfo message) {
        try {
//            if (UdeskConst.isLocal) {
//                UdeskUploadManager.cancleRequest(message.getMsgId());
//            } else {
            isCancleUpLoad.put(message.getMsgId(), true);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //上传文件
    private void upLoadFile(final String filePath, final MessageInfo message) {

//        if (UdeskConst.isLocal) {
//            uploadFileByStrategy(filePath, message);
//        } else {
        try {
            UdeskUtils.printStackTrace();
            if (isCancleUpLoad.containsKey(message.getMsgId())) {
                isCancleUpLoad.put(message.getMsgId(), false);
                return;
            }
            isCancleUpLoad.put(message.getMsgId(), false);
            Recorder recorder = null;
            try {
                recorder = new FileRecorder(UdeskUtils.getDirectoryPath(mChatView.getContext(), UdeskConst.File_File));
            } catch (Exception e) {
            }
            KeyGenerator keyGenerator = new KeyGenerator() {
                @Override
                public String gen(String key, File file) {
                    String name = key + "_._" + new StringBuffer(file.getAbsolutePath()).reverse();
                    return name;
                }
            };
            Configuration config = new Configuration.Builder()
                    .putThreshhold(512 * 1024)
                    .connectTimeout(10)
                    .recorder(recorder, keyGenerator)
                    .useHttps(true)
                    .build();

            // 实例化一个上传的实例
            if (uploadManager == null) {
                uploadManager = new UploadManager(config);
            }
            if (mMyUpCompletionHandler == null) {
                mMyUpCompletionHandler = new MyUpCompletionHandler();
            }
            String key = message.getMsgId();
//            if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_IMAGE) && !TextUtils.isEmpty(message.getFilename())) {
//                key = key + "_" + message.getFilename();
//            }
            key = key + "_" + message.getFilename();
            mMyUpCompletionHandler.putCacheMessage(key, message);
            uploadManager.put(filePath, key,
                    XmppInfo.getInstance().getQiniuToken(),
                    mMyUpCompletionHandler,
                    new UploadOptions(null, null, false,
                            mUpProgressHandler, new UpCancellationSignal() {
                        @Override
                        public boolean isCancelled() {
                            return isCancleUpLoad.get(message.getMsgId());
                        }
                    }));
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
//        }

    }

    //提交满意度调查出错
    private void sendSurveyerror() {
        try {
            if (mChatView.getHandler() != null) {
                Message message = mChatView.getHandler().obtainMessage(
                        MessageWhat.Survey_error);
                mChatView.getHandler().sendMessage(message);
                mChatView.setIsPermmitSurvy(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 七牛上传进度
     */
    private final com.qiniu.android.storage.UpProgressHandler mUpProgressHandler = new com.qiniu.android.storage.UpProgressHandler() {
        public void progress(String key, double percent) {
            try {
                if (mChatView != null && mChatView.getHandler() != null) {
                    Message message = mChatView.getHandler().obtainMessage(
                            MessageWhat.ChangeFielProgress);
                    message.obj = key;
                    message.arg1 = new Double(percent * 100).intValue();
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

        private final Map<String, MessageInfo> mToMsgMap = new HashMap<String, MessageInfo>();

        public MyUpCompletionHandler() {

        }

        public void putCacheMessage(String md5, MessageInfo message) {
            mToMsgMap.put(md5, message);
        }

        @Override
        public void complete(String key, ResponseInfo info, JSONObject response) {
            try {
                MessageInfo msg = mToMsgMap.get(key);
                isCancleUpLoad.remove(msg.getMsgId());
                if (key != null && null != response && (response.has("key")
                        || response.optString("error").contains("file exists"))
                        && msg != null) {
                    if (UdeskConst.isDebug) {
                        Log.i("DialogActivityPresenter", "UpCompletion : key="
                                + key + "\ninfo=" + info.toString() + "\nresponse="
                                + response.toString());
                    }
                    String qiniuKey = response.optString("key");
                    String qiniuUrl = UdeskConst.UD_QINIU_UPLOAD + qiniuKey;
                    UdeskDBManager.getInstance().updateMsgContent(msg.getMsgId(),
                            qiniuUrl);
                    msg.setMsgContent(qiniuUrl);
                    mToMsgMap.remove(key);
                    if (isNeedAddCachePre(msg)) {
                        return;
                    }
                    if (mChatView.isNeedQueueMessageSave()) {
                        queueMessageSave(msg);
                        return;
                    }
                    messageSave(msg);

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
                    if (info != null && info.error != null && info.error.equals("file or data size is zero")) {
                        mChatView.showFailToast(info.error);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
        }
    }

    //发送对话过滤消息
    public void sendPrefilterMsg(boolean isRetry) {
        if (cachePreMsg.size() > 0) {
            for (MessageInfo messageInfo : cachePreMsg) {
                if (isRetry) {
                    startRetryMsg(messageInfo);
                } else {
                    updateFailureStatus(messageInfo);
                }
            }
            cachePreMsg.clear();
        }

    }

    //点击失败按钮 重试发送消息
    public void startRetryMsg(MessageInfo message) {
        try {
            if (isNeedAddCachePre(message)) {
                return;
            }
            if (mChatView.isNeedQueueMessageSave()) {
                queueMessageSave(message);
                return;
            }
            if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_TEXT) || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_Location)
                    || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_PRODUCT)) {
                messageSave(message);
            } else if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_IMAGE)
                    || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_AUDIO)
                    || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_File)
                    || message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_VIDEO)) {
                upLoadFile(message.getLocalPath(), message);
            } else if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_LEAVEMSG)) {
                if (TextUtils.isEmpty(customerId)) {
                    putLeavesMsg(customerId, message.getMsgContent(), message.getMsgId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下载语言
    public void downAudio(final MessageInfo info) {
        try {
            final File file = new File(UdeskUtils.getDirectoryPath(mChatView.getContext(), UdeskConst.FileAduio),
                    UdeskUtils.getFileName(info.getMsgContent(), UdeskConst.FileAduio));

            UdeskHttpFacade.getInstance().downloadFile(file.getAbsolutePath(), info.getMsgContent(), new UdeskHttpCallBack() {

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下载视频
    public void downVideo(final MessageInfo info) {
        try {
            final File file = new File(UdeskUtils.getDirectoryPath(mChatView.getContext(), UdeskConst.FileVideo),
                    UdeskUtils.getFileName(info.getMsgContent(), UdeskConst.FileVideo));

            UdeskHttpFacade.getInstance().downloadFile(file.getAbsolutePath(), info.getMsgContent(), new UdeskHttpCallBack() {

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下载文件
    public void downFile(final MessageInfo info) {
        try {
            final File file = new File(UdeskUtils.getDirectoryPath(mChatView.getContext(), UdeskConst.File_File),
                    UdeskUtils.getFileName(info.getMsgContent(), UdeskConst.File_File));
            UdeskHttpFacade.getInstance().downloadFile(file.getAbsolutePath(), info.getMsgContent(), new UdeskHttpCallBack() {


                @Override
                public void onSuccess(byte[] t) {
                    try {
                        UdeskDBManager.getInstance().updateMsgLoaclUrl(info.getMsgId(), file.getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                        message.arg1 = new Double(percent * 100).intValue();
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
                        byte[] data;
                        int max;
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
                            sendFileMessage(path, UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
                            return;
                        }
                        String imageName = UdeskUtils.MD5(data);
                        File scaleImageFile = new File(UdeskUtils.getDirectoryPath(mChatView.getContext(), UdeskConst.FileImg) + File.separator + imageName + UdeskConst.ORIGINAL_SUFFIX);
                        if (scaleImageFile != null && !scaleImageFile.exists()) {
                            // 缩略图不存在，生成上传图
                            if (max > UdeskSDKManager.getInstance().getUdeskConfig().ScaleMax) {
                                options.inSampleSize = max / UdeskSDKManager.getInstance().getUdeskConfig().ScaleMax;
                            } else {
                                options.inSampleSize = 1;
                            }
                            FileOutputStream fos = new FileOutputStream(scaleImageFile);
                            scaleImage = BitmapFactory.decodeByteArray(data, 0,
                                    data.length, options);
                            scaleImage.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                            fos.close();
                        }

                        if (scaleImage != null) {
                            scaleImage.recycle();
                        }
                        if (TextUtils.isEmpty(scaleImageFile.getPath())) {
                            sendFileMessage(path, UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
                        } else {
                            sendFileMessage(scaleImageFile.getPath(), UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
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
    private byte[] readStream(InputStream inStream) throws Exception {
        byte[] data = new byte[0];
        try {
            byte[] buffer = new byte[1024];
            int len;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            data = outStream.toByteArray();
            outStream.close();
            inStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;

    }

    //3秒检查下是否需要重发消息
    public void selfretrySendMsg() {
        try {
            if (mChatView.getHandler() != null) {
                mChatView.getHandler().removeCallbacks(runnable);
                mChatView.getHandler().postDelayed(runnable, 3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeCallBack() {
        try {
            if (mChatView != null && mChatView.getHandler() != null && runnable != null) {
                mChatView.getHandler().removeCallbacks(runnable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mChatView != null && mChatView.getHandler() != null) {
                    retrySendMsg();
                    mChatView.getHandler().postDelayed(this, 5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //自动重发消息 和 销毁界面会调用
    private void retrySendMsg() {
        try {
            Set<String> retryMsgIds = sendingMsgCache.keySet();
            if (retryMsgIds.size() > 0) {
                for (String msgID : retryMsgIds) {
                    MessageInfo msg = sendingMsgCache.get(msgID);
                    if (msg != null && !UdeskUtils.isNetworkConnected(mChatView.getContext())) {
                        updateFailureStatus(msg);
                        sendingMsgCache.remove(msgID);
                    } else if (msg != null) {
                        onSendMessageFail(msg);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Map<String, MessageInfo> getSendingMsgCache() {
        return sendingMsgCache;
    }
}
