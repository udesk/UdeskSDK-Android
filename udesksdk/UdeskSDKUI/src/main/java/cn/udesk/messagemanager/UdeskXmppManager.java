package cn.udesk.messagemanager;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.jxmpp.jid.impl.JidCreate;

import java.net.InetAddress;
import java.util.concurrent.ArrayBlockingQueue;

import cn.udesk.UdeskSDKManager;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.model.IMInfo;
import cn.udesk.model.MsgNotice;
import udesk.core.UdeskConst;
import udesk.core.event.InvokeEventContainer;
import udesk.core.model.InviterAgentInfo;
import udesk.core.model.MessageInfo;
import udesk.core.utils.UdeskUtils;


public class UdeskXmppManager implements ConnectionListener, StanzaListener {

    private XMPPTCPConnection xmppConnection = null;
    private Message xmppMsg;
    private StanzaFilter msgfilter = new StanzaTypeFilter(Message.class);
    private StanzaFilter presenceFilter = new StanzaTypeFilter(Presence.class);
    private StanzaFilter iQFilter = new StanzaTypeFilter(IQ.class);
    XMPPTCPConnectionConfiguration.Builder mConfiguration;
    private Handler handler = new Handler(Looper.getMainLooper());

    volatile boolean isConnecting = false;
    private static long heartSpaceTime = 0;


    /**
     * 基于插入和删除元素是不会产生和销毁额外的对象实例
     * 并且 在此处场景 插入和删除就是互斥的， xmpp正常使用的时候是会不加入的
     * 缓存30条了， 如果太大了几乎就是是xmpp服务出问题了，在缓存反而让费内存
     */
    private ArrayBlockingQueue<MessageInfo> queue = new ArrayBlockingQueue(30);
    private boolean isCancel;

    private static class LazyHolder {
        private static final UdeskXmppManager INSTANCE = new UdeskXmppManager();
    }

    private UdeskXmppManager() {
    }

    public static final UdeskXmppManager getInstance() {
        return LazyHolder.INSTANCE;
    }


    //xmpp没有连接或连接上 或 发送异常加入队列
    private void addQueue(MessageInfo messageInfo) {
        queue.add(messageInfo);
    }

    //xmpp 链接上后调用
    private void xmppConnectionRetrySend() {

        while (sendQueueMessage(queue.poll())) {
            ;
        }

    }

    private boolean sendQueueMessage(MessageInfo msg) {
        if (msg == null) {
            return false;
        } else {
            sendMessage(msg);
            return true;
        }
    }


    public synchronized boolean startLoginXmpp() {

        IMInfo info = UdeskSDKManager.getInstance().getImInfo();
        if (info == null || TextUtils.isEmpty(info.getServer())
                || TextUtils.isEmpty(info.getPassword())
                || TextUtils.isEmpty(info.getUsername())) {
            return false;
        }
        return this.startLoginXmpp(info.getUsername(),
                info.getPassword(),
                info.getServer(),
                info.getPort());
    }

    /**
     * @param loginName
     * @param loginPassword
     * @param loginServer
     * @param loginPort
     */
    public synchronized boolean startLoginXmpp(String loginName,
                                               String loginPassword, String loginServer, int loginPort) {
        if (!isConnecting) {
            UdeskUtils.resetTime();
            UdeskConst.sdk_xmpp_statea = UdeskConst.CONNECTING;
            if (loginName.contains("@" + loginServer)) {
                int index = loginName.indexOf("@");
                loginName = loginName.substring(0, index);
            }
            isConnecting = true;
            try {
                if (TextUtils.isEmpty(loginServer) || TextUtils.isEmpty(loginName)
                        || TextUtils.isEmpty(loginPassword)) {
                    return false;
                }
                if (mConfiguration == null) {
                    mConfiguration = XMPPTCPConnectionConfiguration.builder();
                }
                if (mConfiguration != null) {
                    mConfiguration.setUsernameAndPassword(loginName, loginPassword);
                    mConfiguration.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled).setCompressionEnabled(false);
                    mConfiguration.setResource(UdeskSDKManager.getInstance().getAppId());
                    mConfiguration.setDebuggerEnabled(UdeskConst.xmppDebug);
                    mConfiguration.setXmppDomain(loginServer);
                    mConfiguration.setHost(loginServer);
                    mConfiguration.setHostAddress(InetAddress.getByName(loginServer));
                    mConfiguration.setPort(loginPort);
                }
                if (xmppConnection == null) {
                    xmppConnection = new XMPPTCPConnection(mConfiguration.build());
                }
                if (xmppConnection != null && !xmppConnection.isConnected()) {
                    ProviderManager.addExtensionProvider(
                            "action",
                            "udesk:action",
                            new ActionMsgReceive());
                    ProviderManager.addExtensionProvider(
                            "ignored",
                            "urn:xmpp:ignored",
                            new IgnoredMsgReceive());
                    xmppConnection.removeAsyncStanzaListener(this);
                    xmppConnection.addAsyncStanzaListener(this, new OrFilter(msgfilter,
                            presenceFilter, iQFilter));
                    xmppConnection.removeConnectionListener(this);
                    xmppConnection.addConnectionListener(this);
                    return connectXMPPServer(loginName, loginPassword);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (handler != null){
                    handler.removeCallbacks(reconnectRunnable);
                    handler.postDelayed(reconnectRunnable,10000);
                }
            } finally {
                isConnecting = false;
            }
        }
        return false;
    }

    private void init(String domain, int port) {
//        mConfiguration = new ConnectionConfiguration(domain, port, domain);
//        mConfiguration
//                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
//        mConfiguration.setDebuggerEnabled(UdeskConst.xmppDebug);
//        xmppConnection = new XMPPTCPConnection(mConfiguration);

    }

    /**
     * 连接xmpp服务器
     */
    private synchronized boolean connectXMPPServer(final String xmppLoginName,
                                                   final String xmppLoginPassword) {
        try {
            if (xmppConnection != null) {
                xmppConnection.connect();
                if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getAppId())) {
                    xmppConnection.login(xmppLoginName, xmppLoginPassword);
                } else {
                    UdeskConst.sdk_xmpp_statea = UdeskConst.CONNECTION_FAILED;
                    return false;
                }
                xmppConnection.sendStanza(new Presence(Presence.Type.available));
                if (handler != null) {
                    handler.post(runnable);
                }
                UdeskUtils.resetTime();
                UdeskConst.sdk_xmpp_statea = UdeskConst.ALREADY_CONNECTED;
                xmppConnectionRetrySend();
            }
        } catch (Exception e) {
            UdeskConst.sdk_xmpp_statea = UdeskConst.CONNECTION_FAILED;
            if (handler != null){
                handler.removeCallbacks(reconnectRunnable);
                handler.postDelayed(reconnectRunnable,10000);
            }
            return false;

        }
        return true;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            sendSelfStatus();
            if (handler != null) {
                handler.postDelayed(this, 15000);
            }

        }
    };
    Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isConnection() && !isCancel){
                Log.i("smack","xmpp reconnect");
                connection();
            }
        }
    };

    private void sendSelfStatus() {
        try {
            if (TextUtils.isEmpty(UdeskBaseInfo.sendMsgTo)) {
                return;
            }
            Presence statusPacket = new Presence(Presence.Type.available);
            statusPacket.setStatus("online");
//            statusPacket.setTo(JidCreate.from(UdeskBaseInfo.sendMsgTo));
            if (xmppConnection != null && xmppConnection.isConnected()) {
                xmppConnection.sendStanza(statusPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送广告信息 :
     *
     * @param text
     * @param to
     */
    public synchronized void sendComodityMessage(String text, String to) {
        if (TextUtils.isEmpty(to)) {
            return;
        }
        if (xmppConnection != null) {
            try {
                xmppMsg = new Message(JidCreate.from(to), Message.Type.chat);
                text = StringUtils.escapeForXml(text).toString();
                ProductXmpp product = new ProductXmpp();
                product.setBody(text);
                xmppMsg.addExtension(product);
                xmppConnection.sendPacket(xmppMsg);
            } catch (Exception e) {
                e.printStackTrace();
                connection();
            }
        }
    }

    public synchronized void sendActionMessage(String to) {
        if (TextUtils.isEmpty(to)) {
            return;
        }
        if (xmppConnection != null) {
            try {
                xmppMsg = new Message(JidCreate.from(to), Message.Type.chat);
                xmppMsg.setStanzaId(" ");
                ActionMsgXmpp actionMsgXmpp = new ActionMsgXmpp();
                actionMsgXmpp.setActionText("overready");
                actionMsgXmpp.setType("isover");
                xmppMsg.addExtension(actionMsgXmpp);
                xmppConnection.sendStanza(xmppMsg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendPreMsg(final String type, final String text, final String to) {
        //防止堵塞 导致anr 无须关注结果
        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    sendPre(type, text, to);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void sendPre(String type, String text, String to) {
        try {
            if (TextUtils.isEmpty(to)) {
                return;
            }

            if (xmppConnection != null) {
                xmppMsg = new Message(JidCreate.from(to), Message.Type.chat);
                text = StringUtils.escapeForXml(text).toString();
                PreMsgXmpp preMsgXmpp = new PreMsgXmpp();
                xmppMsg.addExtension(preMsgXmpp);
                xmppMsg.setStanzaId(" ");
                JSONObject json = new JSONObject();
                json.put("type", type);
                JSONObject data = new JSONObject();
                data.put("content", text);
                json.put("data", data);
                json.put("platform", "android");
                json.put("version", UdeskConst.sdkversion);
                xmppMsg.setBody(json.toString());
                xmppConnection.sendStanza(xmppMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void sendMessage(final MessageInfo messageInfo) {

        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    send(messageInfo);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private synchronized void send(MessageInfo msg) {
        try {
            String type = msg.getMsgtype();
            String text = msg.getMsgContent();
            String msgId = msg.getMsgId();
            String to = msg.getmAgentJid();
            long duration = msg.getDuration();
            String im_sub_session_id = msg.getSubsessionid();
            boolean no_need_save = msg.isNoNeedSave();
            int seqNum = msg.getSeqNum();
            String fileName = msg.getFilename();
            String filesize = msg.getFilesize();

            if (TextUtils.isEmpty(to)) {
                return;
            }
            if (System.currentTimeMillis() - heartSpaceTime > 30 * 1000 || !isConnection()) {
                heartSpaceTime = System.currentTimeMillis();
                addQueue(msg);
                connection();
                UdeskUtils.resetTime();
                UdeskConst.sdk_xmpp_statea = UdeskConst.CONNECTING;
                InvokeEventContainer.getInstance().event_OnSendMessageFail.invoke(msg);
                return;
            }

            xmppMsg = new Message(JidCreate.from(to), Message.Type.chat);

            xmppMsg.setStanzaId(msgId);
            JSONObject json = new JSONObject();
            json.put("type", type);
            if (type.equals("location")) {
                json.put("map_type", UdeskSDKManager.getInstance().getUdeskConfig().useMapType);
            }
            JSONObject data = new JSONObject();
            if (type.equals(UdeskConst.ChatMsgTypeString.TYPE_PRODUCT)) {
                data.put("content", new JSONObject(text));
            } else {
                text = StringUtils.escapeForXML(text).toString();
                data.put("content", text);
            }

            data.put("duration", duration);
            if (!TextUtils.isEmpty(fileName)) {
                data.put("filename", fileName);
            }
            if (!TextUtils.isEmpty(filesize)) {
                data.put("filesize", filesize);
            }
            json.put("data", data);
            json.put("platform", "android");
            json.put("version", UdeskConst.sdkversion);
            if (!TextUtils.isEmpty(im_sub_session_id)) {
                try {
                    json.put("im_sub_session_id", Integer.valueOf(im_sub_session_id));
                } catch (Exception e) {
                    json.put("im_sub_session_id", im_sub_session_id);
                }
            }
            json.put("no_need_save", no_need_save);
            json.put("seq_num", seqNum);
            xmppMsg.setBody(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (xmppConnection != null) {
            try {
//                DeliveryReceiptManager.addDeliveryReceiptRequest(xmppMsg);
                DeliveryReceiptRequest.addTo(xmppMsg);
                xmppConnection.sendStanza(xmppMsg);
            } catch (Exception e) {
                e.printStackTrace();
                addQueue(msg);
                InvokeEventContainer.getInstance().event_OnSendMessageFail.invoke(msg);
            }
        } else {
            addQueue(msg);
            InvokeEventContainer.getInstance().event_OnSendMessageFail.invoke(msg);
        }

    }

    private void processPresence(Presence pre) {

        try {
            if (pre.getType().equals(Presence.Type.subscribe)) {
                Presence presencePacket = new Presence(Presence.Type.subscribed);
                presencePacket.setTo(pre.getFrom());
                try {
                    if (xmppConnection != null) {
                        xmppConnection.sendStanza(presencePacket);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (pre.getType().equals(Presence.Type.unavailable)) {
                InvokeEventContainer.getInstance().event_OnNewPresence.invoke(pre.getFrom().toString(), UdeskConst.OFFLINEFLAG);
            } else if (!TextUtils.isEmpty(pre.getStatus())) {
                InvokeEventContainer.getInstance().event_OnNewPresence.invoke(pre.getFrom().toString(), UdeskConst.ONLINEFLAG);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void messageReceived(final String msgId) {

        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    UdeskDBManager.getInstance().updateMsgSendFlagDB(msgId, UdeskConst.SendFlag.RESULT_SUCCESS);
                    InvokeEventContainer.getInstance().event_OnMessageReceived.invoke(msgId);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMessage(Message message) {
        // 收到回执消息
        if (message.getExtension("received", "urn:xmpp:receipts") != null) {
            DeliveryReceipt received = message.getExtension("received", "urn:xmpp:receipts");
            if (received != null && !TextUtils.isEmpty(received.getId())) {
                messageReceived(received.getId());
            }
            return;
        }

        if (message.getExtension("isreqsurvey", "survey") != null && message.getExtension("delay", "urn:xmpp:delay") == null) {
            InvokeEventContainer.getInstance().event_OnReqsurveyMsg.invoke(true);
            return;
        }

        if (message.getExtension("action", "udesk:action") != null && message.getExtension("delay", "urn:xmpp:delay") == null) {
            ActionMsgXmpp actionMsgXmpp = message.getExtension("action", "udesk:action");
            if (actionMsgXmpp != null) {
                InvokeEventContainer.getInstance().event_OnActionMsg.invoke(actionMsgXmpp.getType(), actionMsgXmpp.getActionText(), message.getFrom().toString());
            }
            return;
        }

        sendReceivedMsg(message);
        String id = message.getPacketID();
        if (id == null || TextUtils.isEmpty(id.trim())) {
            return;
        }
        DelayInformation delayInfo = null;
        if (message.getExtension("delay", "urn:xmpp:delay") == null) {
//            MIA-558 SDK客户查看离线消息显示实际时间
            delayInfo = message.getExtension("delay", "urn:xmpp:delay");
        }
        if (message.getBody() != null) {
            String type = "";
            String content = "";
            long duration = 0;
            String send_status = "";
            String im_sub_session_id = "";
            String new_agent_name = "";
            int seq_num = 0;
            String fileName = "";
            String fileSize = "";
            //离线消息设置服务端带过来的时间， 在线消息，以手机时间为准
            long receiveMsgTime = 0;
            InviterAgentInfo inviterAgentInfo = new InviterAgentInfo();
            try {
                receiveMsgTime = delayInfo != null ? delayInfo.getStamp().getTime() : System.currentTimeMillis();
                JSONObject json = new JSONObject(message.getBody());
                if (json.has("type")) {
                    type = json.optString("type");
                }
                if (json.has("data")) {
                    JSONObject data = json.getJSONObject("data");
                    if (data.has("content")) {
                        content = data.optString("content");
                    }
                    if (data.has("duration")) {
                        duration = data.optLong("duration");
                    }

                    if (data.has("filename")) {
                        fileName = data.optString("filename");
                    }

                    if (data.has("filesize")) {
                        fileSize = data.optString("filesize");
                    }

                }
                if (json.has("send_status")) {
                    send_status = json.optString("send_status");
                }

                if (json.has("seq_num")) {
                    seq_num = json.getInt("seq_num");
                }

                if (json.has("im_sub_session_id")) {
                    im_sub_session_id = json.getString("im_sub_session_id");
                }
                if (json.has("new_agent_name")) {
                    new_agent_name = json.getString("new_agent_name");
                }
                if (json.has("inviter")){
                    JSONObject inviter = json.getJSONObject("inviter");
                    inviterAgentInfo.setId(inviter.opt("id"));
                    inviterAgentInfo.setAvatar(inviter.opt("avatar"));
                    inviterAgentInfo.setJid(inviter.opt("jid"));
                    inviterAgentInfo.setNick_name(inviter.opt("nick_name"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!TextUtils.isEmpty(type) && !TextUtils.isEmpty(content)) {
                newMessage(message, message.getFrom().toString(), type, id, content,
                        duration, send_status, im_sub_session_id, seq_num, fileName, fileSize, receiveMsgTime,inviterAgentInfo,new_agent_name);
            }
        }
    }

    private void newMessage(final Message message, String agentJid, final String type, final String msgId, final String content,
                            final Long duration, final String send_status, String imsessionId,Integer seqNum,
                            String fileName, String fileSize, Long receiveMsgTime,InviterAgentInfo inviterAgentInfo,String new_agent_name) {
        try {
            String inviterJid = inviterAgentInfo.getJid();
            String jid[] = agentJid.split("/");
            if (!TextUtils.isEmpty(inviterJid)){
                UdeskDBManager.getInstance().addInviterAgentInfoDB(inviterAgentInfo);
                jid  = inviterJid.split("/");
            }
            MessageInfo msginfo = null;
            //判断是否是撤回消息
            if (send_status.equals("rollback")) {
                if (UdeskDBManager.getInstance().deleteMsgById(msgId)) {
                    String[] urlAndNick = UdeskDBManager.getInstance().getAgentUrlAndNick(jid[0]);
                    String agentName = "";
                    if (urlAndNick != null) {
                        agentName = urlAndNick[1];
                    }
                    String buildrollBackMsg = agentName;
                    msginfo = buildReceiveMessage(jid[0], UdeskConst.ChatMsgTypeString.TYPE_EVENT, msgId, buildrollBackMsg,
                            duration, send_status, imsessionId, seqNum, fileName, fileSize, receiveMsgTime,new_agent_name);
                }
            } else {
                //消息在本地数据库存在，则结束后续流程
                if (UdeskDBManager.getInstance().hasReceviedMsg(msgId)) {
                    return;
                }
                msginfo = buildReceiveMessage(jid[0], type, msgId, content, duration, send_status,
                        imsessionId, seqNum, fileName, fileSize, receiveMsgTime,new_agent_name);
            }
            if (!TextUtils.isEmpty(inviterJid)){
                msginfo.setInviterAgentInfo(inviterAgentInfo);
            }

            if (!type.equals(UdeskConst.ChatMsgTypeString.TYPE_REDIRECT)) {
                boolean isSaveSuccess = UdeskDBManager.getInstance().addMessageInfo(msginfo);
                if (isSaveSuccess) {
                    sendReceivedMsg(message);
                }
            } else {
                sendReceivedMsg(message);
            }
            if (UdeskConst.isDebug && msginfo != null) {
                Log.i("newMessage", msginfo.toString());
            }
            InvokeEventContainer.getInstance().eventui_OnNewMessage.invoke(msginfo);
            if (type.equals(UdeskConst.ChatMsgTypeString.TYPE_REDIRECT)) {
                return;
            }
            if (UdeskBaseInfo.isNeedMsgNotice && UdeskSDKManager.getInstance().getNewMessage() != null) {
                MsgNotice msgNotice = new MsgNotice(msgId, type, content);
                UdeskSDKManager.getInstance().getNewMessage().onNewMessage(msgNotice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private MessageInfo buildReceiveMessage(String agentJid, String msgType, String msgId,
                                            String content, long duration, String send_status,
                                            String imsessionId, Integer seqNum, String fileName, String fileSize, Long receiveMsgTime,String new_agent_name) {
        MessageInfo msg = new MessageInfo();
        try {
            msg.setMsgtype(msgType);
            msg.setTime(receiveMsgTime);
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
            msg.setSubsessionid(imsessionId);
            msg.setSeqNum(seqNum);
            msg.setFilename(fileName);
            msg.setFilesize(fileSize);
            msg.setReplyUser(new_agent_name);
            msg.setSender(UdeskConst.Sender.agent);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    private synchronized void sendReceivedMsg(Message message) {
        if (message.getExtension("request", "urn:xmpp:receipts") != null) {
            try {
                ReceivedXmpp newUserInfoXmpp = new ReceivedXmpp();
                newUserInfoXmpp.setMsgId(message.getPacketID());
                xmppMsg = new Message(message.getFrom(), Message.Type.chat);
                xmppMsg.addExtension(newUserInfoXmpp);
                if (xmppConnection != null && xmppConnection.isConnected()) {
                    xmppConnection.sendStanza(xmppMsg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                connection();
            }
        }
    }


    /**
     * 断开连接
     */
    public synchronized boolean cancel() {
        try {
            if (xmppConnection != null) {
                xmppConnection.disconnect();
                xmppConnection.removeAsyncStanzaListener(UdeskXmppManager.this);
                xmppConnection.removeConnectionListener(UdeskXmppManager.this);
                handler.removeCallbacks(runnable);
                xmppConnection = null;
            }
            if (mConfiguration != null) {
                mConfiguration = null;
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void connected(XMPPConnection arg0) {
        Log.i("smack","connected");


    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        Log.i("smack","authenticated");

    }

    @Override
    public void connectionClosed() {
        Log.i("smack","connectionClosed");

    }

    @Override
    public void connectionClosedOnError(Exception arg0) {
        Log.i("smack","connectionClosedOnError");
        if (handler != null){
            handler.removeCallbacks(reconnectRunnable);
            handler.postDelayed(reconnectRunnable,10000);
        }
    }

    @Override
    public void reconnectingIn(int arg0) {
        Log.i("smack","connectionClosed");

    }

    @Override
    public void reconnectionFailed(Exception arg0) {
        Log.i("smack","reconnectionFailed");
        if (handler != null){
            handler.removeCallbacks(reconnectRunnable);
            handler.postDelayed(reconnectRunnable,10000);
        }
    }

    @Override
    public void reconnectionSuccessful() {

    }

//    @Override
//    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
//        try {
//            heartSpaceTime = System.currentTimeMillis();
//            if (packet instanceof Message) {
//                Message message = (Message) packet;
//                processMessage(message);
//
//            } else if (packet instanceof Presence) {
//                Presence pre = (Presence) packet;
//                processPresence(pre);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    @Override
    public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException {
        try {
            heartSpaceTime = System.currentTimeMillis();
            if (packet instanceof Message) {
                Message message = (Message) packet;
                processMessage(message);

            } else if (packet instanceof Presence) {
                Presence pre = (Presence) packet;
                processPresence(pre);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否与服务器连接上
     *
     * @return
     */
    public synchronized boolean isConnection() {
        try {
            if (xmppConnection != null) {
                return (xmppConnection.isConnected() && xmppConnection.isAuthenticated());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized void connection() {
        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    isCancel = false;
                    UdeskUtils.resetTime();
                    UdeskConst.sdk_xmpp_statea = UdeskConst.CONNECTING;
                    cancel();
                    startLoginXmpp();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void cancleXmpp() {
        try {
            isCancel = true;
            UdeskUtils.resetTime();
            UdeskConst.sdk_xmpp_statea = UdeskConst.CONNECTION_FAILED;
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    cancel();
                }
            });
            handler.removeCallbacksAndMessages(runnable);
            handler.removeCallbacksAndMessages(reconnectRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
