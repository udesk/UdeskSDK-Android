package udesk.udesksocket;

import android.content.Intent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;
import okio.ByteString;
import udesk.udesksocket.client.UdeskWebsocket;
import udesk.udesksocket.mode.event.EvtAnswer;
import udesk.udesksocket.mode.event.EvtBye;
import udesk.udesksocket.mode.event.EvtCancel;
import udesk.udesksocket.mode.event.EvtInvite;
import udesk.udesksocket.mode.event.EvtPreAnswer;
import udesk.udesksocket.mode.invite.AnswerRep;
import udesk.udesksocket.mode.invite.ByeRep;
import udesk.udesksocket.mode.invite.CancelRep;
import udesk.udesksocket.mode.invite.GetChannelTokenRep;
import udesk.udesksocket.mode.invite.InviteRep;
import udesk.udesksocket.mode.manager.GetAttrAllRep;
import udesk.udesksocket.mode.manager.GetAttrRep;
import udesk.udesksocket.mode.manager.GetStateRep;
import udesk.udesksocket.mode.manager.GetUserStateRep;
import udesk.udesksocket.mode.manager.LoginRep;
import udesk.udesksocket.mode.manager.LogoutRep;
import udesk.udesksocket.mode.manager.SetAttrRep;
import udesk.udesksocket.mode.manager.SetStateRep;

/**
 * author : ${揭军平}
 * time   : 2017/11/23
 * desc   :
 * version: 1.0
 */

public class MessageManager {

    private static volatile MessageManager messageManager = null;

    private WebsocketCallBack websocketCallBack;

    private MessageManager() {
    }

    public static MessageManager getMessageManager() {

        if (messageManager == null) {
            synchronized (MessageManager.class) {
                if (messageManager == null) {
                    messageManager = new MessageManager();
                }
            }
        }

        return messageManager;
    }


    public synchronized void parseOnMessage(String msg) {

        try {
            JSONObject jsonObject = new JSONObject(msg);
            String method = jsonObject.optString("method");
            String msg_type = jsonObject.optString("msg_type");
            MethodEnum methodEnum = MethodEnum.getByValue(method);
            if (methodEnum == null) {
                return;
            }
            switch (methodEnum) {
                case LOGIN:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        LoginRep loginRep = MsgUtil.parseLoginRep(msg);
                        EventBus.getDefault().post(new EventScoketMode<>(method, msg_type, loginRep));
                    }
                    break;
                case LOGOUT:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        LogoutRep logoutRep = MsgUtil.parseLogoutRep(msg);
                    }
                    break;
                case SETSTATE:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        SetStateRep setStateRep = MsgUtil.parseSetStateRep(msg);
                    }
                    break;
                case GETSTATE:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        GetStateRep getStateRep = MsgUtil.parseGetStateRep(msg);
                    }
                    break;
                case GETUSERSTATE:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        GetUserStateRep getUserStateRep = MsgUtil.parseGetUserStateRep(msg);
                    }
                    break;
                case SETATTR:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        SetAttrRep setAttrRep = MsgUtil.parseSetAttrRep(msg);
                    }
                    break;
                case GETATTR:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        GetAttrRep getAttrRep = MsgUtil.parseGetAttrRep(msg);
                    }
                    break;
                case GETATTRALL:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        GetAttrAllRep getAttrAllRep = MsgUtil.parseGetAttrAllRep(msg);
                    }
                    break;
                case INVITE:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        InviteRep inviteRep = MsgUtil.parseInviteRep(msg);
                        EventBus.getDefault().post(new EventScoketMode<>(method, msg_type, inviteRep));
                    } else if (msg_type.equals(UdeskSocketContants.MsgType.Evt)) {
                        //收到视频呼叫的邀请事件，回个evtAck
                        EvtInvite evtInvite = MsgUtil.parseEvtInvite(msg);
                        if (websocketCallBack != null) {
                            websocketCallBack.evtInvite(evtInvite);
                            evtInviteAck(Util.objectToString(evtInvite.getMsg_id()));
                        }
                    }
                    break;
                case CANCEL:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        CancelRep cancelRep = MsgUtil.parseCancelRep(msg);
                    } else if (msg_type.equals(UdeskSocketContants.MsgType.Evt)) {
                        EvtCancel evtCancel = MsgUtil.parseEvtCancel(msg);
                        evtCancelAck(Util.objectToString(evtCancel.getMsg_id()));
                        //对方拒接
                        EventBus.getDefault().post(new EventScoketMode<>(method, msg_type, evtCancel));
                    }
                    break;

                case GETCHANNELTOKEN:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        GetChannelTokenRep channelTokenRep = MsgUtil.parsegetGetChannelTokenRep(msg);
                        EventBus.getDefault().post(new EventScoketMode<>(method, msg_type, channelTokenRep));
                    }
                    break;
                case ANSWER:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        AnswerRep answerRep = MsgUtil.parseAnswerRep(msg);
                    } else if (msg_type.equals(UdeskSocketContants.MsgType.Evt)) {
                        EvtAnswer evtAnswer = MsgUtil.parseEvtAnswer(msg);
                        evtAnswerAck(Util.objectToString(evtAnswer.getMsg_id()));
                        EventBus.getDefault().post(new EventScoketMode<>(method, msg_type, evtAnswer));
                    }
                    break;
                case BYE:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Rep)) {
                        ByeRep byeRep = MsgUtil.parseByeRep(msg);
                        EventBus.getDefault().post(new EventScoketMode<>(method, msg_type, byeRep));
                    } else if (msg_type.equals(UdeskSocketContants.MsgType.Evt)) {
                        EvtBye evtAnswer = MsgUtil.parseEvtBye(msg);
                        evtByeAck(Util.objectToString(evtAnswer.getMsg_id()));
                        EventBus.getDefault().post(new EventScoketMode<>(method, msg_type, evtAnswer));
                    }
                    break;
                case PRE_ANSWER:
                    if (msg_type.equals(UdeskSocketContants.MsgType.Evt)) {
                        EvtPreAnswer evtPreAnswer = MsgUtil.parseEvtPreAnswer(msg);
                        evtPreAnswerAck(Util.objectToString(evtPreAnswer.getMsg_id()));
                    }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void connectWebSocket() {
        UdeskWebsocket.getUdeskWebSocket().connect();
    }

    public void connectWebSocket(WebsocketCallBack callBack) {
        this.websocketCallBack = callBack;
        UdeskWebsocket.getUdeskWebSocket().connect();
        UdeskWebsocket.getUdeskWebSocket().setMessagerListener(new OnMessageListener() {
            @Override
            public void onOpen() {
                if (websocketCallBack != null) {
                    websocketCallBack.connected();
                }
            }

            @Override
            public void onMessage(String msg) {
                parseOnMessage(msg);
            }

            @Override
            public void onMessage(ByteString bytes) {

            }

            @Override
            public void onFailure(Throwable t, Response response) {
                if (websocketCallBack != null) {
                    websocketCallBack.onFailure(t, response);
                }
            }

            @Override
            public void onClosed(int code, String reason) {
                if (websocketCallBack != null) {
                    websocketCallBack.disconnected(code, reason);
                }
            }

            @Override
            public void onClosing(int code, String reason) {

            }
        });
    }


    /**
     * 访问信令系统的认证token
     * 所以接入到信令系统中的客户端需要首先通过login认证。login指令应该在websocket连接成
     * 功后一定时间内发送（10秒），否则服务器会断开连接，客户端会收到websokcet disconnect事件
     * （此是websocket的事件，不是server发送给client的消息）
     *
     * @param appid
     * @param userId
     * @param token
     */
//    public void login(String appid, String userId, String token) {
//        login(appid, userId, token, "idle");
//    }

    /**
     * 访问信令系统的认证token
     * 所以接入到信令系统中的客户端需要首先通过login认证。login指令应该在websocket连接成
     * 功后一定时间内发送（10秒），否则服务器会断开连接，客户端会收到websokcet disconnect事件
     * （此是websocket的事件，不是server发送给client的消息）
     *
     * @param appid
     * @param userId
     * @param token
     * @param state
     */
    public void login(String appid, String userId, String token, String state,String resId) {
        JSONObject logins = MsgUtil.longin(Util.getNextId(), appid, userId, token, state,resId);
        UdeskWebsocket.getUdeskWebSocket().sendMessage(logins.toString());
    }

    /**
     * 客户端退出时，需要发送logout指令，如果没有发送logout就直接断开的websocket连接，客
     * 户端会在一定时间内处理离线状态，但不会退出[TODO]
     */
    public void logout() {

        JSONObject logout = MsgUtil.logout(Util.getNextId());
        UdeskWebsocket.getUdeskWebSocket().sendMessage(logout.toString());

    }

    /**
     * 客户端可以通过这个指令消息设置当前的状态
     *
     * @param state
     */
    public void setState(String state) {

        JSONObject setSate = MsgUtil.setSate(Util.getNextId(), state);
        UdeskWebsocket.getUdeskWebSocket().sendMessage(setSate.toString());

    }

    /**
     * 客户端通过此接口获取当前状态
     */
    public void getState() {

        JSONObject getstate = MsgUtil.getSate(Util.getNextId());
        UdeskWebsocket.getUdeskWebSocket().sendMessage(getstate.toString());

    }

    /**
     * 获取用户的当前状态
     */
    public void getUserState(String uaerId) {

        JSONObject getUserState = MsgUtil.getUserSate(Util.getNextId(), uaerId);
        UdeskWebsocket.getUdeskWebSocket().sendMessage(getUserState.toString());

    }

    /**
     * 客户端通过些接口设置用户的自定义属性，每个用户的属性限制为[TODO]。key的长度限制为[TODO]，value的长度限制为[TODO]
     *
     * @param attr_key
     * @param attr_val
     */
    public void setAttr(String attr_key, String attr_val) {

        JSONObject setAttr = MsgUtil.setAttr(Util.getNextId(), attr_key, attr_val);
        UdeskWebsocket.getUdeskWebSocket().sendMessage(setAttr.toString());

    }

    /**
     * 获取用户自定义属性
     *
     * @param attr_key
     */
    public void getAttr(String attr_key) {

        JSONObject getAttr = MsgUtil.getAttr(Util.getNextId(), attr_key);
        UdeskWebsocket.getUdeskWebSocket().sendMessage(getAttr.toString());

    }

    /**
     * 获取用户所有自定义消息列表
     */
    public void getAttrAll() {

        JSONObject getAttrAll = MsgUtil.getAttrAll(Util.getNextId());
        UdeskWebsocket.getUdeskWebSocket().sendMessage(getAttrAll.toString());

    }

    /**
     * 当前用户向同app_id的另一用户发送呼叫请求
     *
     * @param toUserId
     * @param toResId
     * @param callType
     * @param channelId
     */
    public void invite(String toUserId, String toResId, String callType, String channelId) {

        invite(toUserId, toResId, callType, channelId, 0);
    }

    /**
     * @param toUserId
     * @param toResId
     * @param callType
     * @param channelId
     * @param bizSessionId 业务会话ID，透传到应用层，可选
     */
    public void invite(String toUserId, String toResId, String callType, String channelId, int bizSessionId) {

        JSONObject invite = MsgUtil.invite(Util.getNextId(), toUserId, toResId, callType, channelId, bizSessionId);
        UdeskWebsocket.getUdeskWebSocket().sendMessage(invite.toString());

    }

    /**
     * cancel
     *
     * @param channelId
     * @param reason
     */
    public void cancel(String channelId, String reason) {

        JSONObject cancel = MsgUtil.cancel(Util.getNextId(), channelId, reason);
        UdeskWebsocket.getUdeskWebSocket().sendMessage(cancel.toString());

    }

    /**
     * 获取加入媒体channel的认证token
     *
     * @param channelId
     */
    public void getChannelToken(String channelId) {

        JSONObject getChannelToken = MsgUtil.getChannelToken(Util.getNextId(), channelId);
        UdeskWebsocket.getUdeskWebSocket().sendMessage(getChannelToken.toString());

    }

    /**
     * 客户端应答呼叫
     *
     * @param channelId
     */
    public void answer(String channelId) {
        JSONObject answer = MsgUtil.answer(Util.getNextId(), channelId);
        UdeskWebsocket.getUdeskWebSocket().sendMessage(answer.toString());
    }

    /**
     * 客户端结束呼叫
     *
     * @param channelId
     */
    public void bye(String channelId) {
        JSONObject bye = MsgUtil.bye(Util.getNextId(), channelId);
        UdeskWebsocket.getUdeskWebSocket().sendMessage(bye.toString());
    }

    /**
     * 通话消息。对话建立完成，双方都加入媒体频道时发送。
     *
     * @param channelId
     */
    public void startMedia(String channelId) {
        JSONObject startMedia = MsgUtil.startMedia(Util.getNextId(), channelId);
        UdeskWebsocket.getUdeskWebSocket().sendMessage(startMedia.toString());
    }

    /**
     * 通知消息。对话结束，双方都退出媒体频道时发送。
     *
     * @param channelId
     */
    public void stopMedia(String channelId) {
        JSONObject stopMedia = MsgUtil.stopMedia(Util.getNextId(), channelId);
        UdeskWebsocket.getUdeskWebSocket().sendMessage(stopMedia.toString());
    }


    /**
     * 呼叫事件 事件响应消息
     */
    public void evtInviteAck(String msgId) {
        JSONObject inviteAck = MsgUtil.getAckHead(msgId, Util.getNextId(), "invite");
        UdeskWebsocket.getUdeskWebSocket().sendMessage(inviteAck.toString());
    }

    /**
     * 被叫用户振铃时，主叫方收到此事件 响应
     */
    public void evtPreAnswerAck(String msgId) {
        JSONObject evtPreAnswer = MsgUtil.getAckHead(msgId, Util.getNextId(), "pre_answer");
        UdeskWebsocket.getUdeskWebSocket().sendMessage(evtPreAnswer.toString());
    }

    /**
     * 被叫用户应答时，主叫方收到此事件 响应事件
     */
    public void evtAnswerAck(String msgId) {
        JSONObject evtanswer = MsgUtil.getAckHead(msgId, Util.getNextId(), "answer");
        UdeskWebsocket.getUdeskWebSocket().sendMessage(evtanswer.toString());
    }

    /**
     * 一方挂机时，另一方收到此消息 响应事件
     */
    public void evtByeAck(String msgId) {
        JSONObject evtanswer = MsgUtil.getAckHead(msgId, Util.getNextId(), "bye");
        UdeskWebsocket.getUdeskWebSocket().sendMessage(evtanswer.toString());
    }

    /**
     * 通话建立过程中，一方取消呼叫，另一方收到此消息。系统取消呼叫，双方都会收到此消息  的响应事件
     */
    public void evtCancelAck(String msgId) {
        JSONObject evtanswer = MsgUtil.getAckHead(msgId, Util.getNextId(), "cancel");
        UdeskWebsocket.getUdeskWebSocket().sendMessage(evtanswer.toString());
    }

    public void sendPing() {
        UdeskWebsocket.getUdeskWebSocket().sendMessage("ping");
    }
}
