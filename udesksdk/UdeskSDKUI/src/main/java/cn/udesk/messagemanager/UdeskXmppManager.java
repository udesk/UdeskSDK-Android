package cn.udesk.messagemanager;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import cn.udesk.UdeskSDKManager;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import udesk.core.UdeskConst;
import udesk.core.event.InvokeEventContainer;
import udesk.core.model.MessageInfo;
import udesk.core.utils.UdeskUtils;
import udesk.core.xmpp.XmppInfo;
import udesk.org.jivesoftware.smack.ConnectionConfiguration;
import udesk.org.jivesoftware.smack.ConnectionListener;
import udesk.org.jivesoftware.smack.PacketListener;
import udesk.org.jivesoftware.smack.SmackException;
import udesk.org.jivesoftware.smack.XMPPConnection;
import udesk.org.jivesoftware.smack.filter.OrFilter;
import udesk.org.jivesoftware.smack.filter.PacketFilter;
import udesk.org.jivesoftware.smack.filter.PacketTypeFilter;
import udesk.org.jivesoftware.smack.packet.IQ;
import udesk.org.jivesoftware.smack.packet.Message;
import udesk.org.jivesoftware.smack.packet.Packet;
import udesk.org.jivesoftware.smack.packet.Presence;
import udesk.org.jivesoftware.smack.provider.ProviderManager;
import udesk.org.jivesoftware.smack.tcp.XMPPTCPConnection;
import udesk.org.jivesoftware.smack.util.StringUtils;
import udesk.org.jivesoftware.smackx.delay.packet.DelayInfo;
import udesk.org.jivesoftware.smackx.receipts.DeliveryReceipt;
import udesk.org.jivesoftware.smackx.receipts.DeliveryReceiptManager;

public class UdeskXmppManager implements ConnectionListener, PacketListener {

    private XMPPTCPConnection xmppConnection = null;
    private Message xmppMsg;
    private PacketFilter msgfilter = new PacketTypeFilter(Message.class);
    private PacketFilter presenceFilter = new PacketTypeFilter(Presence.class);
    private PacketFilter iQFilter = new PacketTypeFilter(IQ.class);
    ConnectionConfiguration mConfiguration;
    private Handler handler = new Handler();

    volatile boolean isConnecting = false;
    private static long heartSpaceTime = 0;

    /**
     * 基于插入和删除元素是不会产生和销毁额外的对象实例
     * 并且 在此处场景 插入和删除就是互斥的， xmpp正常使用的时候是会不加入的
     * 缓存30条了， 如果太大了几乎就是是xmpp服务出问题了，在缓存反而让费内存
     */
    private ArrayBlockingQueue<MessageInfo> queue = new ArrayBlockingQueue(30);

    //xmpp没有连接或连接上 或 发送异常加入队列
    private void addQueue(MessageInfo messageInfo) {
        queue.add(messageInfo);
    }

    //xmpp 链接上后调用
    private void xmppConnectionRetrySend() {

        while (sendQueueMessage(queue.poll()))
            ;

    }

    private boolean sendQueueMessage(MessageInfo msg) {
        if (msg == null) {
            return false;
        } else {
            sendMessage(msg);
            return true;
        }
    }


    public UdeskXmppManager() {

    }


    public synchronized boolean startLoginXmpp() {

        if (TextUtils.isEmpty(XmppInfo.getInstance().getLoginServer())
                || TextUtils.isEmpty(XmppInfo.getInstance().getLoginPassword())
                || TextUtils.isEmpty(XmppInfo.getInstance().getLoginName())) {
            return false;
        }
        return this.startLoginXmpp(XmppInfo.getInstance().getLoginName(),
                XmppInfo.getInstance().getLoginPassword(),
                XmppInfo.getInstance().getLoginServer(),
                XmppInfo.getInstance().getLoginPort());
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
                    init(loginServer, loginPort);
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
                    xmppConnection.removePacketListener(this);
                    xmppConnection.addPacketListener(this, new OrFilter(msgfilter,
                            presenceFilter, iQFilter));
                    xmppConnection.removeConnectionListener(this);
                    xmppConnection.addConnectionListener(this);
                    return connectXMPPServer(loginName, loginPassword);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            } finally {
                isConnecting = false;
            }
        }
        return false;
    }

    private void init(String domain, int port) {
        mConfiguration = new ConnectionConfiguration(domain, port, domain);
        mConfiguration
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        mConfiguration.setDebuggerEnabled(UdeskConst.xmppDebug);
        xmppConnection = new XMPPTCPConnection(mConfiguration);

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
                    xmppConnection.login(xmppLoginName, xmppLoginPassword, UdeskSDKManager.getInstance().getAppId());
                } else {
                    xmppConnection.login(xmppLoginName, xmppLoginPassword, UUID.randomUUID().toString());
                }
                xmppConnection.sendPacket(new Presence(Presence.Type.available));
                if (handler != null) {
                    handler.post(runnable);
                }
                UdeskUtils.resetTime();
                UdeskConst.sdk_xmpp_statea = UdeskConst.ALREADY_CONNECTED;
                xmppConnectionRetrySend();
            }
        } catch (Exception e) {
            UdeskConst.sdk_xmpp_statea = UdeskConst.CONNECTION_FAILED;
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

    private void sendSelfStatus() {
        try {
            if (TextUtils.isEmpty(UdeskBaseInfo.sendMsgTo)) {
                return;
            }
            Presence statusPacket = new Presence(Presence.Type.available);
            statusPacket.setStatus("online");
            statusPacket.setTo(UdeskBaseInfo.sendMsgTo);
            if (xmppConnection != null) {
                xmppConnection.sendPacket(statusPacket);
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
                xmppMsg = new Message(to, Message.Type.chat);
                text = StringUtils.escapeForXML(text).toString();
                ProductXmpp product = new ProductXmpp();
                product.setBody(text);
                xmppMsg.addExtension(product);
                xmppConnection.sendPacket(xmppMsg);
            } catch (Exception e) {
                e.printStackTrace();
                UdeskUtils.resetTime();
                UdeskConst.sdk_xmpp_statea = UdeskConst.CONNECTING;
                reConnected();
            }
        }
    }

    public synchronized void sendActionMessage(String to) {
        if (TextUtils.isEmpty(to)) {
            return;
        }
        if (xmppConnection != null) {
            try {
                xmppMsg = new Message(to, Message.Type.chat);
                xmppMsg.setPacketID(" ");
                ActionMsgXmpp actionMsgXmpp = new ActionMsgXmpp();
                actionMsgXmpp.setActionText("overready");
                actionMsgXmpp.setType("isover");
                xmppMsg.addExtension(actionMsgXmpp);
                xmppConnection.sendPacket(xmppMsg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void sendVCCallMessage(String type, String to, String text) {
        if (TextUtils.isEmpty(to)) {
            return;
        }
        if (xmppConnection != null) {
            try {
                xmppMsg = new Message(to, Message.Type.chat);
                xmppMsg.setPacketID(" ");
                ActionMsgXmpp actionMsgXmpp = new ActionMsgXmpp();
                actionMsgXmpp.setActionText(text);
                actionMsgXmpp.setType(type);
                xmppMsg.addExtension(actionMsgXmpp);
                xmppConnection.sendPacket(xmppMsg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public synchronized void sendPreMessage(String type, String text, String to) {
        try {
            if (TextUtils.isEmpty(to)) {
                return;
            }

            if (xmppConnection != null) {
                xmppMsg = new Message(to, Message.Type.chat);
                text = StringUtils.escapeForXML(text).toString();
                PreMsgXmpp preMsgXmpp = new PreMsgXmpp();
                xmppMsg.addExtension(preMsgXmpp);
                xmppMsg.setPacketID(" ");
                JSONObject json = new JSONObject();
                json.put("type", type);
                JSONObject data = new JSONObject();
                data.put("content", text);
                json.put("data", data);
                json.put("platform", "android");
                json.put("version", UdeskConst.sdkversion);
                xmppMsg.setBody(json.toString());
                xmppConnection.sendPacket(xmppMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public synchronized void sendMessage(MessageInfo msg) {
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
                reConnected();
                UdeskUtils.resetTime();
                UdeskConst.sdk_xmpp_statea = UdeskConst.CONNECTING;
                InvokeEventContainer.getInstance().event_OnSendMessageFail.invoke(msg);
                return;
            }

            xmppMsg = new Message(to, Message.Type.chat);

            xmppMsg.setPacketID(msgId);
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
                DeliveryReceiptManager.addDeliveryReceiptRequest(xmppMsg);
                xmppConnection.sendPacket(xmppMsg);
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
                        xmppConnection.sendPacket(presencePacket);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (pre.getType().equals(Presence.Type.unavailable)) {
                InvokeEventContainer.getInstance().event_OnNewPresence.invoke(pre.getFrom(), UdeskConst.OFFLINEFLAG);
            } else if (!TextUtils.isEmpty(pre.getStatus())) {
                InvokeEventContainer.getInstance().event_OnNewPresence.invoke(pre.getFrom(), UdeskConst.ONLINEFLAG);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processMessage(Message message) {
        // 收到回执消息
        if (message.getExtension("received", "urn:xmpp:receipts") != null) {
            DeliveryReceipt received = message.getExtension("received", "urn:xmpp:receipts");
            if (received != null && !TextUtils.isEmpty(received.getId())) {
                InvokeEventContainer.getInstance().event_OnMessageReceived.invoke(received.getId());
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
                InvokeEventContainer.getInstance().event_OnActionMsg.invoke(actionMsgXmpp.getType(), actionMsgXmpp.getActionText(), message.getFrom());
            }
            return;
        }

        sendReceivedMsg(message);
        String id = message.getPacketID();
        if (id == null || TextUtils.isEmpty(id.trim())) {
            return;
        }
        DelayInfo delayInfo = null;
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
            int seq_num = 0;
            String fileName = "";
            String fileSize = "";
            //离线消息设置服务端带过来的时间， 在线消息，以手机时间为准
            long receiveMsgTime = 0;
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

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!TextUtils.isEmpty(type) && !TextUtils.isEmpty(content)) {
                UdeskMessageManager.getInstance().event_OnNewMessage.invoke(message, message.getFrom(), type, id, content,
                        duration, send_status, im_sub_session_id, seq_num, fileName, fileSize, receiveMsgTime);
            }
        }
    }

    public synchronized void sendReceivedMsg(Message message) {
        if (message.getExtension("request", "urn:xmpp:receipts") != null) {
            try {
                ReceivedXmpp newUserInfoXmpp = new ReceivedXmpp();
                newUserInfoXmpp.setMsgId(message.getPacketID());
                xmppMsg = new Message(message.getFrom(), Message.Type.chat);
                xmppMsg.addExtension(newUserInfoXmpp);
                if (xmppConnection != null) {
                    xmppConnection.sendPacket(xmppMsg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                UdeskUtils.resetTime();
                UdeskConst.sdk_xmpp_statea = UdeskConst.CONNECTING;
                reConnected();
            }
        }
    }

    private synchronized void reConnected() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    cancel();
                    startLoginXmpp();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }


    /**
     * 断开连接
     */
    public synchronized boolean cancel() {
        try {
            if (xmppConnection != null) {
                xmppConnection.removePacketListener(UdeskXmppManager.this);
                xmppConnection.removeConnectionListener(UdeskXmppManager.this);
                handler.removeCallbacks(runnable);
                xmppConnection.disconnect();
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


    }

    @Override
    public void authenticated(XMPPConnection connection) {

    }

    @Override
    public void connectionClosed() {

    }

    @Override
    public void connectionClosedOnError(Exception arg0) {

    }

    @Override
    public void reconnectingIn(int arg0) {

    }

    @Override
    public void reconnectionFailed(Exception arg0) {

    }

    @Override
    public void reconnectionSuccessful() {

    }

    @Override
    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
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
}
