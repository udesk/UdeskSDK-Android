package udesk.udesksocket;

import org.json.JSONException;
import org.json.JSONObject;

import udesk.udesksocket.mode.event.EvtCancel;
import udesk.udesksocket.mode.event.EvtStateChange;
import udesk.udesksocket.mode.invite.AnswerRep;
import udesk.udesksocket.mode.invite.ByeRep;
import udesk.udesksocket.mode.invite.CancelRep;
import udesk.udesksocket.mode.EventAck;
import udesk.udesksocket.mode.event.EvtAnswer;
import udesk.udesksocket.mode.event.EvtBye;
import udesk.udesksocket.mode.event.EvtInvite;
import udesk.udesksocket.mode.event.EvtPreAnswer;
import udesk.udesksocket.mode.manager.GetAttrAllRep;
import udesk.udesksocket.mode.manager.GetAttrRep;
import udesk.udesksocket.mode.invite.GetChannelTokenRep;
import udesk.udesksocket.mode.manager.GetStateRep;
import udesk.udesksocket.mode.manager.GetUserStateRep;
import udesk.udesksocket.mode.invite.InviteRep;
import udesk.udesksocket.mode.manager.LoginRep;
import udesk.udesksocket.mode.manager.LogoutRep;
import udesk.udesksocket.mode.invite.PreAnswerlRep;
import udesk.udesksocket.mode.RepHead;
import udesk.udesksocket.mode.manager.SetAttrRep;
import udesk.udesksocket.mode.manager.SetStateRep;

/**
 * author : ${揭军平}
 * time   : 2017/11/22
 * desc   :
 * version: 1.0
 */

public class MsgUtil {


    /**
     * 请求消息标准头部
     *
     * @return
     */
    public static JSONObject getReqHead(String msgid,int seqid) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ver", UdeskSocketContants.Ver);
            jsonObject.put("ua", Util.getUa());
            jsonObject.put("seq_id",seqid);
            jsonObject.put("msg_id", msgid);
            jsonObject.put("msg_type", UdeskSocketContants.MsgType.Req);
            jsonObject.put("timestamp", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    /**
     * 响应消息标准头部
     *
     * @param message
     * @return
     */
    public static RepHead parseRepHead(String message) {

        RepHead repHead = new RepHead();
        try {
            JSONObject jsonObject = new JSONObject(message);
            repHead.setVer(jsonObject.opt("ver"));
            repHead.setMsg_id(jsonObject.opt("msg_id"));
            repHead.setMsg_type(jsonObject.opt("msg_type"));
            repHead.setTimestamp(jsonObject.opt("timestamp"));
            repHead.setRcode(jsonObject.opt("rcode"));
            repHead.setRdesc(jsonObject.opt("rdesc"));
            repHead.setRdesc(jsonObject.opt("seq_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return repHead;
    }


    /**
     * 通知消息标准头部
     *
     * @param msgid
     * @return
     */
    public static JSONObject getNotifyHead(String msgid,int seqid) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ver", UdeskSocketContants.Ver);
            jsonObject.put("msg_id", msgid);
            jsonObject.put("msg_type", UdeskSocketContants.MsgType.Ntf);
            jsonObject.put("seq_id", seqid);
            jsonObject.put("timestamp", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    /**
     * 事件消息标准头部
     *
     * @param msgid
     * @return
     */
    public static JSONObject getEvtHead(String msgid,int seqid) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ver", UdeskSocketContants.Ver);
            jsonObject.put("msg_id", msgid);
            jsonObject.put("seq_id", seqid);
            jsonObject.put("msg_type", UdeskSocketContants.MsgType.Evt);
            jsonObject.put("timestamp", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    /**
     * 事件响应消息
     * @param msgid
     * @param method
     * @return
     */
    public static JSONObject getAckHead(String msgid,int seqid,String method) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("ver", UdeskSocketContants.Ver);
            jsonObject.put("msg_id", msgid);
            jsonObject.put("seq_id", seqid);
            jsonObject.put("msg_type", UdeskSocketContants.MsgType.Ack);
            jsonObject.put("timestamp", System.currentTimeMillis());
            jsonObject.put("method", method);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    /**
     * 响应消息标准头部
     *
     * @param message
     * @return
     */
    public static EventAck parseEventAckHead(String message) {

        EventAck eventAck = new EventAck();
        try {
            JSONObject jsonObject = new JSONObject(message);
            eventAck.setVer(jsonObject.opt("ver"));
            eventAck.setMsg_id(jsonObject.opt("msg_id"));
            eventAck.setMsg_id(jsonObject.opt("seq_id"));
            eventAck.setMsg_type(jsonObject.opt("msg_type"));
            eventAck.setTimestamp(jsonObject.opt("timestamp"));
            eventAck.setMethod(jsonObject.opt("method"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return eventAck;
    }


    /**
     * @param appid
     * @param userId
     * @param token
     * @param state  user的初始状态，可选，默认为idle
     * @return 所以接入到信令系统中的客户端需要首先通过login认证。login指令应该在websocket连接成
     * 功后一定时间内发送（10秒），否则服务器会断开连接，客户端会收到websokcet disconnect事件
     * （此是websocket的事件，不是server发送给client的消息）
     *
     *
     */
    public static JSONObject longin(int seqid,  String appid, String userId, String token, String state,String resId) {

        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "login");
            jsonObject.put("app_id", appid);
            jsonObject.put("user_id", userId);
            jsonObject.put("res_id", resId);
            jsonObject.put("token", token);
            jsonObject.put("state", state);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;

    }


    /**
     * 响应消息
     *
     * @param message
     * @return
     */
    public static LoginRep parseLoginRep(String message) {
        LoginRep loginRep = new LoginRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            loginRep.setVer(jsonObject.opt("ver"));
            loginRep.setMsg_id(jsonObject.opt("msg_id"));
            loginRep.setMsg_type(jsonObject.opt("msg_type"));
            loginRep.setTimestamp(jsonObject.opt("timestamp"));
            loginRep.setRcode(jsonObject.opt("rcode"));
            loginRep.setRdesc(jsonObject.opt("rdesc"));
            loginRep.setSeq_id(jsonObject.opt("seq_id"));
            loginRep.setMethod(jsonObject.opt("method"));
            loginRep.setUid(jsonObject.opt("uid"));
            loginRep.setCur_state(jsonObject.opt("cur_state"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return loginRep;
    }

    /**
     * @return 客户端退出时，需要发送logout指令，如果没有发送logout就直接断开的websocket连接，客户端会在一定时间内处理离线状态，但不会退出
     */
    public static JSONObject logout(int seqid) {
        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "logout");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }


    /**
     * 退出响应消息
     *
     * @param message
     * @return
     */
    public static LogoutRep parseLogoutRep(String message) {
        LogoutRep logoutRep = new LogoutRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            logoutRep.setVer(jsonObject.opt("ver"));
            logoutRep.setMsg_id(jsonObject.opt("msg_id"));
            logoutRep.setMsg_type(jsonObject.opt("msg_type"));
            logoutRep.setTimestamp(jsonObject.opt("timestamp"));
            logoutRep.setRcode(jsonObject.opt("rcode"));
            logoutRep.setRdesc(jsonObject.opt("rdesc"));
            logoutRep.setSeq_id(jsonObject.opt("seq_id"));
            logoutRep.setMethod(jsonObject.opt("method"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return logoutRep;
    }

    /**
     * @param state
     * @return 客户端可以通过这个指令消息设置当前的状态
     */
    public static JSONObject setSate(int seqid,  String state) {
        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "set_state");
            jsonObject.put("state", state);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * 设置状态的响应消息
     *
     * @param message
     * @return
     */
    public static SetStateRep parseSetStateRep(String message) {
        SetStateRep setStateRep = new SetStateRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            setStateRep.setVer(jsonObject.opt("ver"));
            setStateRep.setMsg_id(jsonObject.opt("msg_id"));
            setStateRep.setMsg_type(jsonObject.opt("msg_type"));
            setStateRep.setTimestamp(jsonObject.opt("timestamp"));
            setStateRep.setRcode(jsonObject.opt("rcode"));
            setStateRep.setRdesc(jsonObject.opt("rdesc"));
            setStateRep.setMethod(jsonObject.opt("method"));
            setStateRep.setCur_state(jsonObject.opt("cur_state"));
            setStateRep.setOld_state(jsonObject.opt("old_stat"));
            setStateRep.setSeq_id(jsonObject.opt("seq_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return setStateRep;
    }

    /**
     * 客户端通过此接口获取当前状态
     * @return
     */
    public static JSONObject getSate(int seqid) {
        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "get_state");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * 获取状态的响应消息
     *
     * @param message
     * @return
     */
    public static GetStateRep parseGetStateRep(String message) {
        GetStateRep getStateRep = new GetStateRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            getStateRep.setVer(jsonObject.opt("ver"));
            getStateRep.setMsg_id(jsonObject.opt("msg_id"));
            getStateRep.setMsg_type(jsonObject.opt("msg_type"));
            getStateRep.setTimestamp(jsonObject.opt("timestamp"));
            getStateRep.setRcode(jsonObject.opt("rcode"));
            getStateRep.setRdesc(jsonObject.opt("rdesc"));
            getStateRep.setMethod(jsonObject.opt("method"));
            getStateRep.setCur_state(jsonObject.opt("cur_state"));
            getStateRep.setSeq_id(jsonObject.opt("seq_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getStateRep;
    }


    /**
     * 获取同一个app_id中其它用户的当前状态
     * @return
     */
    public static JSONObject getUserSate(int seqid,String userid) {
        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "get_user_state");
            jsonObject.put("user_id", userid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 获取同一个app_id中其它用户的当前状态的响应消息
     *
     * @param message
     * @return
     */
    public static GetUserStateRep parseGetUserStateRep(String message) {
        GetUserStateRep getUserStateRep = new GetUserStateRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            getUserStateRep.setVer(jsonObject.opt("ver"));
            getUserStateRep.setMsg_id(jsonObject.opt("msg_id"));
            getUserStateRep.setMsg_type(jsonObject.opt("msg_type"));
            getUserStateRep.setTimestamp(jsonObject.opt("timestamp"));
            getUserStateRep.setRcode(jsonObject.opt("rcode"));
            getUserStateRep.setRdesc(jsonObject.opt("rdesc"));
            getUserStateRep.setMethod(jsonObject.opt("method"));
            getUserStateRep.setCur_state(jsonObject.opt("cur_state"));
            getUserStateRep.setSeq_id(jsonObject.opt("seq_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getUserStateRep;
    }


    /**
     * @param attr_key
     * @param attr_val
     * @return 客户端通过些接口设置用户的自定义属性
     */
    public static JSONObject setAttr(int seqid,  String attr_key, String attr_val) {
        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "set_attr");
            jsonObject.put("attr_key", attr_key);
            jsonObject.put("attr_val", attr_val);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    /**
     * 设置用户的自定义属性 响应消息
     *
     * @param message
     * @return
     */
    public static SetAttrRep parseSetAttrRep(String message) {
        SetAttrRep setAttrRep = new SetAttrRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            setAttrRep.setVer(jsonObject.opt("ver"));
            setAttrRep.setMsg_id(jsonObject.opt("msg_id"));
            setAttrRep.setMsg_type(jsonObject.opt("msg_type"));
            setAttrRep.setTimestamp(jsonObject.opt("timestamp"));
            setAttrRep.setRcode(jsonObject.opt("rcode"));
            setAttrRep.setRdesc(jsonObject.opt("rdesc"));
            setAttrRep.setMethod(jsonObject.opt("method"));
            setAttrRep.setSeq_id(jsonObject.opt("seq_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return setAttrRep;
    }

    /**
     * 获取用户自定义属性

     * @param attr_key
     * @return
     */
    public static JSONObject getAttr(int seqid, String attr_key) {
        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "get_attr");
            jsonObject.put("attr_key", attr_key);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 获取用户自定义属性 响应消息
     * @param message
     * @return
     */
    public static GetAttrRep parseGetAttrRep(String message) {
        GetAttrRep getAttrRep = new GetAttrRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            getAttrRep.setVer(jsonObject.opt("ver"));
            getAttrRep.setMsg_id(jsonObject.opt("msg_id"));
            getAttrRep.setMsg_type(jsonObject.opt("msg_type"));
            getAttrRep.setTimestamp(jsonObject.opt("timestamp"));
            getAttrRep.setRcode(jsonObject.opt("rcode"));
            getAttrRep.setRdesc(jsonObject.opt("rdesc"));
            getAttrRep.setMethod(jsonObject.opt("method"));
            getAttrRep.setAttr_key(jsonObject.opt("attr_key"));
            getAttrRep.setAttr_val(jsonObject.opt("attr_val"));
            getAttrRep.setSeq_id(jsonObject.opt("seq_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getAttrRep;
    }

    /**
     * 获取用户所有自定义消息列表
     * @return
     */
    public static JSONObject getAttrAll(int seqid) {
        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "get_attr_all");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    /**
     * 获取用户所有自定义消息列表 响应消息
     * @param message
     * @return
     */
    public static GetAttrAllRep parseGetAttrAllRep(String message) {
        GetAttrAllRep getAttrAllRep = new GetAttrAllRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            getAttrAllRep.setVer(jsonObject.opt("ver"));
            getAttrAllRep.setMsg_id(jsonObject.opt("msg_id"));
            getAttrAllRep.setMsg_type(jsonObject.opt("msg_type"));
            getAttrAllRep.setTimestamp(jsonObject.opt("timestamp"));
            getAttrAllRep.setRcode(jsonObject.opt("rcode"));
            getAttrAllRep.setRdesc(jsonObject.opt("rdesc"));
            getAttrAllRep.setMethod(jsonObject.opt("method"));
            getAttrAllRep.setAttrs(jsonObject.opt("attrs"));
            getAttrAllRep.setSeq_id(jsonObject.opt("seq_id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getAttrAllRep;
    }


    /**
     * 呼叫消息
     * @param toUserId
     * @param toResId 业务会话ID
     * @param callType   // video | audio
     * @param channelId  客户端SDK自己生成的channel_id
     * @return
     */
    public static JSONObject invite(int seqid, String toUserId,String toResId,String callType,String channelId,int sessionId){

        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "invite");
            jsonObject.put("to_user_id", toUserId);
            jsonObject.put("to_res_id", toResId);
            jsonObject.put("call_type", callType);
            jsonObject.put("channel_id", channelId);
            jsonObject.put("biz_session_id", sessionId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    /**
     * 呼叫消息 相应消息
     * @param message
     * @return
     */
    public static InviteRep parseInviteRep(String message) {
        InviteRep inviteRep = new InviteRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            inviteRep.setVer(jsonObject.opt("ver"));
            inviteRep.setMsg_id(jsonObject.opt("msg_id"));
            inviteRep.setMsg_type(jsonObject.opt("msg_type"));
            inviteRep.setTimestamp(jsonObject.opt("timestamp"));
            inviteRep.setRcode(jsonObject.opt("rcode"));
            inviteRep.setRdesc(jsonObject.opt("rdesc"));
            inviteRep.setMethod(jsonObject.opt("method"));
            inviteRep.setChannel_id(jsonObject.opt("channel_id"));
            inviteRep.setSeq_id(jsonObject.opt("seq_id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return inviteRep;
    }

    /**
     *
     * @param channelId
     * @return
     * 呼叫建立通过中，如果某一方的某个操作失败，应该发送cancel消息，否则系统会的超时
     * 后按呼叫超时取消呼叫
     */
    public static JSONObject cancel(int seqid,String channelId,String reason){

        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "cancel");
            jsonObject.put("channel_id", channelId);
            jsonObject.put("reason", reason);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    /**
     * 取消响应消息
     * @param message
     * @return
     */
    public static CancelRep parseCancelRep(String message) {
        CancelRep cancelRep = new CancelRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            cancelRep.setVer(jsonObject.opt("ver"));
            cancelRep.setMsg_id(jsonObject.opt("msg_id"));
            cancelRep.setMsg_type(jsonObject.opt("msg_type"));
            cancelRep.setTimestamp(jsonObject.opt("timestamp"));
            cancelRep.setRcode(jsonObject.opt("rcode"));
            cancelRep.setRdesc(jsonObject.opt("rdesc"));
            cancelRep.setMethod(jsonObject.opt("method"));
            cancelRep.setChannel_id(jsonObject.opt("channel_id"));
            cancelRep.setSeq_id(jsonObject.opt("seq_id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cancelRep;
    }

    /**
     * @param channelId
     * @return
     */
    public static JSONObject preAnswer(int seqid, String dev,String channelId){

        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "pre_answer");
            jsonObject.put("channel_id", channelId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    /**
     * 预应答消息  响应
     * @param message
     * @return
     */
    public  static PreAnswerlRep parsePreAnswerRep(String message) {
        PreAnswerlRep preAnswerlRep = new PreAnswerlRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            preAnswerlRep.setVer(jsonObject.opt("ver"));
            preAnswerlRep.setMsg_id(jsonObject.opt("msg_id"));
            preAnswerlRep.setMsg_type(jsonObject.opt("msg_type"));
            preAnswerlRep.setTimestamp(jsonObject.opt("timestamp"));
            preAnswerlRep.setRcode(jsonObject.opt("rcode"));
            preAnswerlRep.setRdesc(jsonObject.opt("rdesc"));
            preAnswerlRep.setMethod(jsonObject.opt("method"));
            preAnswerlRep.setChannel_id(jsonObject.opt("channel_id"));
            preAnswerlRep.setSeq_id(jsonObject.opt("seq_id"));


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return preAnswerlRep;
    }

    /**
     * 获取加入媒体channel的认证token
     * @param channelId
     * @return
     */
    public static JSONObject getChannelToken(int seqid, String channelId){

        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "get_channel_token");
            jsonObject.put("channel_id", channelId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    /**
     * 获取加入媒体channel的认证token 响应
     * @param message
     * @return
     */
    public static GetChannelTokenRep parsegetGetChannelTokenRep(String message) {
        GetChannelTokenRep getChannelTokenRep = new GetChannelTokenRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            getChannelTokenRep.setVer(jsonObject.opt("ver"));
            getChannelTokenRep.setMsg_id(jsonObject.opt("msg_id"));
            getChannelTokenRep.setMsg_type(jsonObject.opt("msg_type"));
            getChannelTokenRep.setTimestamp(jsonObject.opt("timestamp"));
            getChannelTokenRep.setRcode(jsonObject.opt("rcode"));
            getChannelTokenRep.setRdesc(jsonObject.opt("rdesc"));
            getChannelTokenRep.setMethod(jsonObject.opt("method"));
            getChannelTokenRep.setChannel_id(jsonObject.opt("channel_id"));
            getChannelTokenRep.setChannel_token(jsonObject.opt("channel_token"));
            getChannelTokenRep.setSeq_id(jsonObject.opt("seq_id"));
            getChannelTokenRep.setAgora_uid(jsonObject.opt("agora_uid"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getChannelTokenRep;
    }

    /**
     * 客户端应答呼叫
     * @param channelId
     * @return
     */
    public static JSONObject answer(int seqid,String channelId){

        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "answer");
            jsonObject.put("channel_id", channelId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    public static AnswerRep parseAnswerRep(String message) {
        AnswerRep answerRep = new AnswerRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            answerRep.setVer(jsonObject.opt("ver"));
            answerRep.setMsg_id(jsonObject.opt("msg_id"));
            answerRep.setMsg_type(jsonObject.opt("msg_type"));
            answerRep.setTimestamp(jsonObject.opt("timestamp"));
            answerRep.setRcode(jsonObject.opt("rcode"));
            answerRep.setRdesc(jsonObject.opt("rdesc"));
            answerRep.setMethod(jsonObject.opt("method"));
            answerRep.setChannel_id(jsonObject.opt("channel_id"));
            answerRep.setSeq_id(jsonObject.opt("seq_id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return answerRep;
    }

    /**
     *
     * @param channelId
     * @return
     * 客户端结束呼叫
     */
    public static JSONObject bye(int seqid,String channelId){

        JSONObject jsonObject = getReqHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "bye");
            jsonObject.put("channel_id", channelId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    public static ByeRep parseByeRep(String message) {
        ByeRep byeRep = new ByeRep();
        try {
            JSONObject jsonObject = new JSONObject(message);
            byeRep.setVer(jsonObject.opt("ver"));
            byeRep.setMsg_id(jsonObject.opt("msg_id"));
            byeRep.setMsg_type(jsonObject.opt("msg_type"));
            byeRep.setTimestamp(jsonObject.opt("timestamp"));
            byeRep.setRcode(jsonObject.opt("rcode"));
            byeRep.setRdesc(jsonObject.opt("rdesc"));
            byeRep.setMethod(jsonObject.opt("method"));
            byeRep.setChannel_id(jsonObject.opt("channel_id"));
            byeRep.setSeq_id(jsonObject.opt("seq_id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return byeRep;
    }


    /**
     * 通话消息。对话建立完成，双方都加入媒体频道时发送。
     * @param channelId
     * @return
     */
    public static JSONObject startMedia(int seqid,String channelId){

        JSONObject jsonObject = getNotifyHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "start_media");
            jsonObject.put("channel_id", channelId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    /**
     * 通知消息。对话结束，双方都退出媒体频道时发送
     * @param channelId
     * @return
     */
    public static JSONObject stopMedia(int seqid,String channelId){

        JSONObject jsonObject = getNotifyHead(Util.buildMsgId(), seqid);
        try {
            jsonObject.put("method", "stop_media");
            jsonObject.put("channel_id", channelId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;

    }

    /**
     *  s -> c
     * 通知消息。当客户端状态发生变化时发送
     * @param message
     * @return
     */
    public  static EvtStateChange parseEvtStateChange(String message){
        EvtStateChange stateChange = new EvtStateChange();
        try {
            JSONObject jsonObject = new JSONObject(message);
            stateChange.setVer(jsonObject.opt("ver"));
            stateChange.setMsg_id(jsonObject.opt("msg_id"));
            stateChange.setMsg_type(jsonObject.opt("msg_type"));
            stateChange.setTimestamp(jsonObject.opt("timestamp"));
            stateChange.setMethod(jsonObject.opt("method"));
            stateChange.setCur_state(jsonObject.opt("cur_state"));
            stateChange.setSeq_id(jsonObject.opt("seq_id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stateChange;
    }

    /**
     * 当有呼叫到达时，服务器发送给客户的消息
     * @param message
     * @return
     */
    public static  EvtInvite parseEvtInvite(String message) {
        EvtInvite evtInvite = new EvtInvite();
        try {
            JSONObject jsonObject = new JSONObject(message);
            evtInvite.setVer(jsonObject.opt("ver"));
            evtInvite.setMsg_id(jsonObject.opt("msg_id"));
            evtInvite.setMsg_type(jsonObject.opt("msg_type"));
            evtInvite.setTimestamp(jsonObject.opt("timestamp"));
            evtInvite.setMethod(jsonObject.opt("method"));
            evtInvite.setChannel_id(jsonObject.opt("channel_id"));
            evtInvite.setFrom_user_id(jsonObject.opt("from_user_id"));
            evtInvite.setTo_user_id(jsonObject.opt("to_user_id"));
            evtInvite.setSeq_id(jsonObject.opt("seq_id"));
            evtInvite.setRcode(jsonObject.opt("rcode"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return evtInvite;
    }


    /**
     * 被叫用户振铃时，主叫方收到此事件
     * @param message
     * @return
     */
    public static EvtPreAnswer parseEvtPreAnswer(String message) {
        EvtPreAnswer evtPreAnswer = new EvtPreAnswer();
        try {
            JSONObject jsonObject = new JSONObject(message);
            evtPreAnswer.setVer(jsonObject.opt("ver"));
            evtPreAnswer.setMsg_id(jsonObject.opt("msg_id"));
            evtPreAnswer.setMsg_type(jsonObject.opt("msg_type"));
            evtPreAnswer.setTimestamp(jsonObject.opt("timestamp"));
            evtPreAnswer.setMethod(jsonObject.opt("method"));
            evtPreAnswer.setChannel_id(jsonObject.opt("channel_id"));
            evtPreAnswer.setFrom_user_id(jsonObject.opt("from_user_id"));
            evtPreAnswer.setTo_user_id(jsonObject.opt("to_user_id"));
            evtPreAnswer.setSeq_id(jsonObject.opt("seq_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return evtPreAnswer;
    }

    /**
     * 被叫用户振铃时，主叫方收到此事件
     * @param message
     * @return
     */
    public static EvtAnswer parseEvtAnswer(String message) {
        EvtAnswer evtAnswer = new EvtAnswer();
        try {
            JSONObject jsonObject = new JSONObject(message);
            evtAnswer.setVer(jsonObject.opt("ver"));
            evtAnswer.setMsg_id(jsonObject.opt("msg_id"));
            evtAnswer.setMsg_type(jsonObject.opt("msg_type"));
            evtAnswer.setTimestamp(jsonObject.opt("timestamp"));
            evtAnswer.setMethod(jsonObject.opt("method"));
            evtAnswer.setChannel_id(jsonObject.opt("channel_id"));
            evtAnswer.setFrom_user_id(jsonObject.opt("from_user_id"));
            evtAnswer.setTo_user_id(jsonObject.opt("to_user_id"));
            evtAnswer.setSeq_id(jsonObject.opt("seq_id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return evtAnswer;
    }

    /**
     *一方挂机时，另一方收到此消息
     * @param message
     * @return
     */
    public static EvtBye parseEvtBye(String message) {
        EvtBye evtBye = new EvtBye();
        try {
            JSONObject jsonObject = new JSONObject(message);
            evtBye.setVer(jsonObject.opt("ver"));
            evtBye.setMsg_id(jsonObject.opt("msg_id"));
            evtBye.setMsg_type(jsonObject.opt("msg_type"));
            evtBye.setTimestamp(jsonObject.opt("timestamp"));
            evtBye.setMethod(jsonObject.opt("method"));
            evtBye.setChannel_id(jsonObject.opt("channel_id"));
            evtBye.setFrom_user_id(jsonObject.opt("from_user_id"));
            evtBye.setTo_user_id(jsonObject.opt("to_user_id"));
            evtBye.setSeq_id(jsonObject.opt("reason"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return evtBye;
    }

    /**
     * 说明：通话建立过程中，一方取消呼叫，另一方收到此消息。系统取消呼叫，双方都会收到此消息。
     * @param message
     * @return
     */
    public static EvtCancel parseEvtCancel(String message) {
        EvtCancel evtCancel = new EvtCancel();
        try {
            JSONObject jsonObject = new JSONObject(message);
            evtCancel.setVer(jsonObject.opt("ver"));
            evtCancel.setMsg_id(jsonObject.opt("msg_id"));
            evtCancel.setMsg_type(jsonObject.opt("msg_type"));
            evtCancel.setTimestamp(jsonObject.opt("timestamp"));
            evtCancel.setMethod(jsonObject.opt("method"));
            evtCancel.setChannel_id(jsonObject.opt("channel_id"));
            evtCancel.setFrom_user_id(jsonObject.opt("from_user_id"));
            evtCancel.setTo_user_id(jsonObject.opt("to_user_id"));
            evtCancel.setSeq_id(jsonObject.opt("seq_id"));
            evtCancel.setReason(jsonObject.opt("reason"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return evtCancel;
    }


}
