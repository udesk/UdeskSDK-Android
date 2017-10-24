package cn.udesk.messagemanager;

import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.ExecutorService;

import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.model.MsgNotice;
import udesk.core.event.InvokeEventContainer;
import udesk.core.event.ReflectInvokeMethod;
import udesk.core.model.MessageInfo;
import udesk.org.jivesoftware.smack.packet.Message;


public class UdeskMessageManager {

    private UdeskXmppManager mUdeskXmppManager;
    private ExecutorService messageExecutor;
    public ReflectInvokeMethod event_OnNewMessage = new ReflectInvokeMethod(new Class<?>[]{Message.class, String.class,
            String.class, String.class, String.class, Long.class, String.class});
    public ReflectInvokeMethod eventui_OnMessageReceived = new ReflectInvokeMethod(new Class<?>[]{String.class});
    public ReflectInvokeMethod eventui_OnNewMessage = new ReflectInvokeMethod(new Class<?>[]{MessageInfo.class});
    public ReflectInvokeMethod eventui_OnNewPresence = new ReflectInvokeMethod(new Class<?>[]{String.class, Integer.class});
    public ReflectInvokeMethod eventui_OnReqsurveyMsg = new ReflectInvokeMethod(new Class<?>[]{Boolean.class});
    public ReflectInvokeMethod event_OnNewMsgNotice = new ReflectInvokeMethod(new Class<?>[]{MsgNotice.class});
    public ReflectInvokeMethod event_OnTicketReplayNotice = new ReflectInvokeMethod(new Class<?>[]{Boolean.class});


    private static UdeskMessageManager instance = new UdeskMessageManager();

    public static UdeskMessageManager getInstance() {
        return instance;
    }

    private UdeskMessageManager() {
        bindEvent();
        mUdeskXmppManager = new UdeskXmppManager();
        ensureMessageExecutor();
    }

    private void ensureMessageExecutor() {
        if (messageExecutor == null) {
            messageExecutor = Concurrents
                    .newSingleThreadExecutor("messageExecutor");
        }
    }

    public boolean isConnection(){
        if (mUdeskXmppManager != null){
            return  mUdeskXmppManager.isConnection();
        }
        return  false;
    }

    public void connection() {
        try {
            ensureMessageExecutor();
            messageExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    if (mUdeskXmppManager != null) {
                        mUdeskXmppManager.cancel();
                        mUdeskXmppManager.startLoginXmpp();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendMessage(String type, String text, String msgId, String to, long duration, String subsessionId) {
        mUdeskXmppManager.sendMessage(type, text, msgId, to, duration, subsessionId);
    }

    public void sendComodityMessage(String text, String to) {
        mUdeskXmppManager.sendComodityMessage(text, to);
    }

    public void sendPreMsg(String type, String text, String to) {
        mUdeskXmppManager.sendPreMessage(type, text, to);
    }

    private void bindEvent() {
        event_OnNewMessage.bind(this, "onNewMessage");
        InvokeEventContainer.getInstance().event_OnMessageReceived.bind(this, "onMessageReceived");
        InvokeEventContainer.getInstance().event_OnNewPresence.bind(this, "onNewPresence");
        InvokeEventContainer.getInstance().event_OnReqsurveyMsg.bind(this, "onReqsurveyMsg");
        InvokeEventContainer.getInstance().event_OnActionMsg.bind(this, "onActionMsg");
    }

    public void onMessageReceived(final String msgId) {

        try {
            ensureMessageExecutor();
            messageExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    UdeskDBManager.getInstance().updateMsgSendFlag(msgId, UdeskConst.SendFlag.RESULT_SUCCESS);
                    UdeskDBManager.getInstance().deleteSendingMsg(msgId);
                    eventui_OnMessageReceived.invoke(msgId);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onNewMessage(final Message message, String agentJid, final String type, final String msgId, final String content,
                             final Long duration, final String send_status) {
        try {
            String jid[] = agentJid.split("/");
            MessageInfo msginfo = null;
            //判断是否是撤回消息
            if (send_status.equals("rollback")) {
                if (UdeskDBManager.getInstance().deleteMsgById(msgId)) {
                    String[] urlAndNick = UdeskDBManager.getInstance().getAgentUrlAndNick(jid[0]);
                    String agentName = "";
                    if (urlAndNick != null) {
                        agentName = urlAndNick[1];
                    }
                    String buildrollBackMsg = "客服" + agentName + "撤回一条消息";
                    msginfo = buildReceiveMessage(jid[0], UdeskConst.ChatMsgTypeString.TYPE_EVENT, msgId, buildrollBackMsg, duration, send_status);
                }
            } else {
                msginfo = buildReceiveMessage(jid[0], type, msgId, content, duration, send_status);  //消息在本地数据库存在，则结束后续流程
                if (UdeskDBManager.getInstance().hasReceviedMsg(msgId)) {
                    return;
                }

            }
            if (!type.equals(UdeskConst.ChatMsgTypeString.TYPE_REDIRECT)) {
                boolean isSaveSuccess = UdeskDBManager.getInstance().addMessageInfo(msginfo);
                if (isSaveSuccess) {
                    if (mUdeskXmppManager != null) {
                        mUdeskXmppManager.sendReceivedMsg(message);
                    }
                }
            } else {
                if (mUdeskXmppManager != null) {
                    mUdeskXmppManager.sendReceivedMsg(message);
                }
            }
            eventui_OnNewMessage.invoke(msginfo);
            if (type.equals(UdeskConst.ChatMsgTypeString.TYPE_REDIRECT)) {
                return;
            }
            if (UdeskBaseInfo.isNeedMsgNotice) {
                MsgNotice msgNotice = new MsgNotice(msgId, type, content);
                event_OnNewMsgNotice.invoke(msgNotice);
                if (msgNotice != null && UdeskSDKManager.getInstance().getOnlineMessage() != null && !UdeskConfig.isUserSDkPush) {
                    UdeskSDKManager.getInstance().getOnlineMessage().onlineMessageReceive(msgNotice);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public MessageInfo buildReceiveMessage(String agentJid, String msgType, String msgId,
                                           String content, long duration, String send_status) {
        MessageInfo msg = new MessageInfo();
        msg.setMsgtype(msgType);
        msg.setTime(System.currentTimeMillis());
        msg.setMsgId(msgId);
        msg.setDirection(UdeskConst.ChatMsgDirection.Recv);
        msg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
        msg.setReadFlag(UdeskConst.ChatMsgReadFlag.unread);
        msg.setMsgContent(content);
        msg.setPlayflag(UdeskConst.PlayFlag.NOPLAY);
        msg.setLocalPath("");
        msg.setDuration(duration);
        msg.setmAgentJid(agentJid);
        msg.setSend_status(send_status);
        return msg;
    }

    public void onNewPresence(String jid, Integer onlineflag) {
        try {
            eventui_OnNewPresence.invoke(jid, onlineflag);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onReqsurveyMsg(Boolean isSurvey) {
        try {
            eventui_OnReqsurveyMsg.invoke(isSurvey);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onActionMsg(String type, String actionText, String agentJId) {
        try {
            if (TextUtils.isEmpty(actionText)) {
                return;
            }
            if (type.equals("ticket_reply")) {
                //调用获取工单离线消息
                event_OnTicketReplayNotice.invoke(true);
                return;
            }
            if (actionText.equals("overtest")) {
                mUdeskXmppManager.sendActionMessage(agentJId);
            } else if (actionText.equals("over")) {
                InvokeEventContainer.getInstance().event_IsOver.invoke(true);
                try {
                    Thread.sleep(2000);
                    cancleXmpp();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void cancleXmpp() {
        try {
            ensureMessageExecutor();
            messageExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    if (mUdeskXmppManager != null) {
                        mUdeskXmppManager.cancel();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
