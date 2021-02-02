package cn.udesk.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import cn.udesk.JsonUtils;
import cn.udesk.UdeskSDKManager;
import udesk.core.UdeskConst;
import udesk.core.model.AgentInfo;
import udesk.core.model.InviterAgentInfo;
import udesk.core.model.MessageInfo;
import udesk.core.utils.UdeskUtils;


public class UdeskDBManager {

    private static UdeskDBHelper helper;
    private SQLiteDatabase mDatabase;
    private String mSdktoken;

    private static UdeskDBManager instance = new UdeskDBManager();

    private UdeskDBManager() {

    }

    public static UdeskDBManager getInstance() {
        return instance;
    }

    /**
     * 初始化，需要在使用数据库之前调用此方法
     *
     * @param context
     */
    public synchronized void init(Context context, String sdktoken) {
        try {
            if (context == null) {
                return;
            }
            if (TextUtils.isEmpty(sdktoken)) {
                sdktoken = UdeskSDKManager.getInstance().getSdkToken(context);
            }
            mSdktoken = sdktoken;
            if (helper == null) {
                helper = new UdeskDBHelper(context, mSdktoken);
            }
            mDatabase = helper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isNeedInit(String sdktoken) {

        try {
            if (mSdktoken != null && mSdktoken.equals(sdktoken) && mDatabase != null) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 退出时，释放
     */
    public synchronized void release() {
        if (helper != null) {
            helper.close();
            helper = null;
        }

        if (mSdktoken != null) {
            mSdktoken = null;
        }
    }

    public synchronized SQLiteDatabase getSQLiteDatabase() {
        return mDatabase;
    }


    public boolean addAllMessageInfo(List<MessageInfo> messages) {
        if (messages == null || getSQLiteDatabase() == null) {
            return false;
        }
        getSQLiteDatabase().beginTransaction();
        try {
            for (MessageInfo msg : messages) {
//                if (isExitMessage(msg.getMsgId()) == null) {
                    addMessageInfo(msg);
//                }
            }
            getSQLiteDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            return false;
        } finally {
            getSQLiteDatabase().endTransaction();
        }
        return true;
    }

    //保存消息
    public Future<Boolean> addMessageDB(final MessageInfo msg) {
        try {
            Future<Boolean> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return addMessageInfo(msg);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * db中增加一条消息的语句
     *
     * @param msg
     */

    public synchronized boolean addMessageInfo(MessageInfo msg) {
        try {

            if (getSQLiteDatabase() == null || msg == null) {
                return false;
            }

            String sql = "replace into "
                    + UdeskDBHelper.UdeskMessage
                    + "(MsgID ,Time ,MsgContent,MsgType,ReadFlag,SendFlag,PlayedFlag,"
                    + "Direction,LocalPath,Duration,Receive_AgentJid,created_at,"
                    + "updated_at,reply_user,reply_userurl,subsessionid,seqNum,fileName,fileSize,"
                    + "switchStaffType,switchStaffTips,topAsk,logId,webConfig,sender,flowId,"
                    + "flowTitle,flowContent,question_id,recommendationGuidance)"
                    + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            getSQLiteDatabase().execSQL(
                    sql,
                    new Object[]{msg.getMsgId(), msg.getTime(),
                            msg.getMsgContent(), msg.getMsgtype(),
                            msg.getReadFlag(), msg.getSendFlag(),
                            msg.getPlayflag(), msg.getDirection(),
                            msg.getLocalPath(), msg.getDuration(),
                            msg.getmAgentJid(), msg.getCreatedTime(),
                            msg.getUpdateTime(), msg.getReplyUser(),
                            msg.getUser_avatar(), msg.getSubsessionid(),
                            msg.getSeqNum(), msg.getFilename(), msg.getFilesize(),
                            msg.getSwitchStaffType(),msg.getSwitchStaffTips(), JsonUtils.parseTopAskToJson(msg.getTopAsk()),
                            msg.getLogId(),JsonUtils.parseWebConfigBeanToJson(msg.getWebConfig()),msg.getSender(),
                            msg.getFlowId(),msg.getFlowTitle(),msg.getFlowContent(),msg.getQuestion_id(),msg.getRecommendationGuidance()
                    });
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public Future<Boolean> updateMsgContentDB(final String msgId, final String text) {
        try {
            Future<Boolean> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return updateMsgContent(msgId,text);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //更新消息的内容
    public synchronized boolean updateMsgContent(String msgId, String text) {

        String sql = "update " + UdeskDBHelper.UdeskMessage + " set " + "MsgContent= ?"
                + " where MsgID = ? ";
        try {
            if (getSQLiteDatabase() == null) {
                return false;
            }
            getSQLiteDatabase().execSQL(sql, new Object[]{text, msgId});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    public Future<Boolean> updateMsgLoaclUrlDB(final String msgId, final String text) {
        try {
            Future<Boolean> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return updateMsgLoaclUrl(msgId,text);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //更新缓存的路径
    public boolean updateMsgLoaclUrl(String msgId, String text) {

        String sql = "update " + UdeskDBHelper.UdeskMessage + " set " + "LocalPath= ?"
                + " where MsgID = ? ";
        try {
            if (getSQLiteDatabase() == null) {
                return false;
            }
            getSQLiteDatabase().execSQL(sql, new Object[]{text, msgId});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    public Future<Boolean> updateMsgSendFlagDB(final String msgId, final int sendflag) {
        try {
            Future<Boolean> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return updateMsgSendFlag(msgId,sendflag);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //更新消息发送的状态
    public synchronized boolean updateMsgSendFlag(String msgId, int sendflag) {

        String sql = "update " + UdeskDBHelper.UdeskMessage + " set "
                + "SendFlag= ?" + " where  MsgID = ? ";
        try {
            if (getSQLiteDatabase() == null) {
                return false;
            }
            getSQLiteDatabase().execSQL(sql, new Object[]{sendflag, msgId});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public Future<Boolean> updateSendFlagToFailDB() {
        try {
            Future<Boolean> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return updateSendFlagToFail();
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //更新消息发送中的为发送失败
    public boolean updateSendFlagToFail() {
        String sql = "update " + UdeskDBHelper.UdeskMessage
                + " set  SendFlag = " + UdeskConst.SendFlag.RESULT_FAIL
                + " where  SendFlag = " + UdeskConst.SendFlag.RESULT_SEND;
        if (getSQLiteDatabase() == null) {
            return false;
        }
        try {
            getSQLiteDatabase().execSQL(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Future<MessageInfo> isExitMessageDB(final String msgId) {
        try {
            Future<MessageInfo> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<MessageInfo>() {
                @Override
                public MessageInfo call() throws Exception {
                    return isExitMessage(msgId);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //根据消息的ID查询这条消息
    private synchronized MessageInfo isExitMessage(String msgid) {
        String sql = "select MsgID from " + UdeskDBHelper.UdeskMessage
                + " where MsgID = ?";

        MessageInfo msg = null;
        Cursor cursor = null;
        try {
            if (getSQLiteDatabase() == null) {
                return null;
            }
            cursor = getSQLiteDatabase().rawQuery(sql, new String[]{msgid});
            if (cursor.moveToFirst()) {
                String msgId = cursor.getString(0);
                msg = new MessageInfo();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return msg;
    }

    public Future<MessageInfo> getMessageDB(final String msgId) {
        try {
            Future<MessageInfo> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<MessageInfo>() {
                @Override
                public MessageInfo call() throws Exception {
                    return getMessage(msgId);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //根据消息的ID查询这条消息
    public synchronized MessageInfo getMessage(String msgid) {
        String sql = "select * from " + UdeskDBHelper.UdeskMessage
                + " where MsgID = ?";

        MessageInfo msg = null;
        Cursor cursor = null;
        try {
            if (getSQLiteDatabase() == null) {
                return null;
            }
            cursor = getSQLiteDatabase().rawQuery(sql, new String[]{msgid});
            if (cursor.moveToFirst()) {
                String msgId = cursor.getString(0);
                long time = cursor.getLong(1);
                String msgContent = cursor.getString(2);
                String msgtype = cursor.getString(3);
                int readFlag = cursor.getInt(4);
                int sendFlag = cursor.getInt(5);
                int playFlag = cursor.getInt(6);
                int direction = cursor.getInt(7);
                String localPath = cursor.getString(8);
                long duration = cursor.getLong(9);
                String agentJid = cursor.getString(10);
                String replyUser = cursor.getString(13);
                String reply_userurl = cursor.getString(14);
                String subsessionid = cursor.getString(15);
                int seqNum = cursor.getInt(16);
                String fileName = UdeskUtils.objectToString(cursor.getString(17));
                String fileSize = UdeskUtils.objectToString(cursor.getString(18));
                int switchStaffType = UdeskUtils.objectToInt(cursor.getInt(19));
                String switchStaffTips = UdeskUtils.objectToString(cursor.getString(20));
                String topAsk = UdeskUtils.objectToString(cursor.getString(21));
                int logId = UdeskUtils.objectToInt(cursor.getInt(22));
                String webConfig = UdeskUtils.objectToString(cursor.getString(23));
                String sender = UdeskUtils.objectToString(cursor.getString(24));
                int flowId = UdeskUtils.objectToInt(cursor.getInt(25));
                String flowTitle = UdeskUtils.objectToString(cursor.getString(26));
                String flowContent = UdeskUtils.objectToString(cursor.getString(27));
                String question_id = UdeskUtils.objectToString(cursor.getString(28));
                String recommendationGuidance = UdeskUtils.objectToString(cursor.getString(29));

                msg = new MessageInfo(time, msgId, msgtype, msgContent,
                        readFlag, sendFlag, playFlag, direction, localPath,
                        duration, agentJid);
                msg.setSubsessionid(subsessionid);
                msg.setSeqNum(seqNum);
                msg.setUser_avatar(reply_userurl);
                msg.setReplyUser(replyUser);
                msg.setFilename(fileName);
                msg.setFilesize(fileSize);
                msg.setSwitchStaffType(switchStaffType);
                msg.setSwitchStaffTips(switchStaffTips);
                if (!TextUtils.isEmpty(topAsk)){
                    msg.setTopAsk(JsonUtils.parseTopAsk(new JSONObject(topAsk)));
                }
                msg.setLogId(logId);
                if (!TextUtils.isEmpty(webConfig)){
                    msg.setWebConfig(JsonUtils.parseWebConfigBean(webConfig));
                }
                msg.setSender(sender);
                msg.setFlowId(flowId);
                msg.setFlowTitle(flowTitle);
                msg.setFlowContent(flowContent);
                msg.setQuestion_id(question_id);
                msg.setRecommendationGuidance(recommendationGuidance);
                if (!TextUtils.isEmpty(agentJid.trim())) {
                    String[] urlAndNick = getAgentUrlAndNick(agentJid);
                    if (urlAndNick != null) {
                        try {
                            msg.setUser_avatar(urlAndNick[0]);
                            msg.setReplyUser(urlAndNick[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return msg;
    }

    public Future<List<MessageInfo>> getMessagesDB(final int offset, final int pageNum) {
        try {
            Future<List<MessageInfo>> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<List<MessageInfo>>() {
                @Override
                public List<MessageInfo> call() throws Exception {
                    return getMessages(offset,pageNum);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 获取指定条数的聊天记录
     *
     * @param offset  偏移量
     * @param pageNum 默认每次查询的数量 见UdeskConst.UDESK_HISTORY_COUNT值
     * @return
     */
    public synchronized List<MessageInfo> getMessages(int offset, int pageNum) {

        String sql = "select * from " + UdeskDBHelper.UdeskMessage
                + " order by Time limit " + pageNum
                + " offset " + offset;
        List<MessageInfo> list = new ArrayList<MessageInfo>();
        SQLiteDatabase db = getSQLiteDatabase();
        Cursor cursor = null;
        if (db == null) {
            return list;
        }
        try {
            cursor = db.rawQuery(sql, null);
            int count = cursor.getCount();
            if (count < 1) {
                return list;
            }
            while (cursor.moveToNext()) {
                String msgId = cursor.getString(0);
                long time = cursor.getLong(1);
                String msgContent = cursor.getString(2);
                String msgtype = cursor.getString(3);
                int readFlag = cursor.getInt(4);
                int sendFlag = cursor.getInt(5);
                int playFlag = cursor.getInt(6);
                int direction = cursor.getInt(7);
                String localPath = cursor.getString(8);
                long duration = cursor.getLong(9);
                String agentJid = UdeskUtils.objectToString(cursor.getString(10));
                String createdTime = UdeskUtils.objectToString(cursor.getString(11));
                String updatedTime = UdeskUtils.objectToString(cursor.getString(12));
                String replyUser = UdeskUtils.objectToString(cursor.getString(13));
                String reply_userurl = UdeskUtils.objectToString(cursor.getString(14));
                String subSeessionId = UdeskUtils.objectToString(cursor.getString(15));
                int seqNum = cursor.getInt(16);
                String fileName = UdeskUtils.objectToString(cursor.getString(17));
                String fileSize = UdeskUtils.objectToString(cursor.getString(18));
                int switchStaffType = UdeskUtils.objectToInt(cursor.getInt(19));
                String switchStaffTips = UdeskUtils.objectToString(cursor.getString(20));
                String topAsk = UdeskUtils.objectToString(cursor.getString(21));
                int logId = UdeskUtils.objectToInt(cursor.getInt(22));
                String webConfig = UdeskUtils.objectToString(cursor.getString(23));
                String sender = UdeskUtils.objectToString(cursor.getString(24));
                int flowId = UdeskUtils.objectToInt(cursor.getInt(25));
                String flowTitle = UdeskUtils.objectToString(cursor.getString(26));
                String flowContent = UdeskUtils.objectToString(cursor.getString(27));
                String question_id = UdeskUtils.objectToString(cursor.getString(28));
                String recommendationGuidance = UdeskUtils.objectToString(cursor.getString(29));


                if (sendFlag == UdeskConst.SendFlag.RESULT_SEND && System.currentTimeMillis() - time > 30 * 1000) {
                    sendFlag = UdeskConst.SendFlag.RESULT_FAIL;
                }
                MessageInfo message = new MessageInfo(time, msgId, msgtype,
                        msgContent, readFlag, sendFlag, playFlag, direction,
                        localPath, duration, agentJid);
                message.setFilename(fileName);
                message.setFilesize(fileSize);
                message.setSeqNum(seqNum);
                message.setSubsessionid(subSeessionId);
                message.setCreatedTime(createdTime);
                message.setUpdateTime(updatedTime);
                message.setReplyUser(replyUser);
                message.setUser_avatar(reply_userurl);
                message.setSwitchStaffType(switchStaffType);
                message.setSwitchStaffTips(switchStaffTips);
                if (!TextUtils.isEmpty(topAsk)){
                    message.setTopAsk(JsonUtils.parseTopAsk(new JSONObject(topAsk)));
                }
                message.setLogId(logId);
                if (!TextUtils.isEmpty(webConfig)){
                    message.setWebConfig(JsonUtils.parseWebConfigBean(webConfig));
                }
                message.setSender(sender);
                message.setFlowId(flowId);
                message.setFlowTitle(flowTitle);
                message.setFlowContent(flowContent);
                message.setQuestion_id(question_id);
                message.setRecommendationGuidance(recommendationGuidance);
                if (!TextUtils.isEmpty(agentJid.trim())) {
                    String[] urlAndNick = getAgentUrlAndNick(agentJid);
                    if (urlAndNick != null) {
                        try {
                            message.setUser_avatar(urlAndNick[0]);
                            message.setReplyUser(urlAndNick[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                list.add(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }


    public Future<MessageInfo> getLastMessageDB() {
        try {
            Future<MessageInfo> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<MessageInfo>() {
                @Override
                public MessageInfo call() throws Exception {
                    return getLastMessage();
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //获取聊天记录的最后一条
    public synchronized MessageInfo getLastMessage() {

        String sql = "select * from " + UdeskDBHelper.UdeskMessage
                + " order by Time desc limit 1";
        MessageInfo msgInfo = new MessageInfo();
        SQLiteDatabase db = getSQLiteDatabase();
        Cursor cursor = null;
        if (db == null) {
            return msgInfo;
        }
        try {
            cursor = db.rawQuery(sql, null);
            int count = cursor.getCount();
            if (count < 1) {
                return msgInfo;
            }
            while (cursor.moveToNext()) {
                String msgId = cursor.getString(0);
                long time = cursor.getLong(1);
                String msgContent = cursor.getString(2);
                String msgtype = cursor.getString(3);
                int readFlag = cursor.getInt(4);
                int sendFlag = cursor.getInt(5);
                int playFlag = cursor.getInt(6);
                int direction = cursor.getInt(7);
                String localPath = cursor.getString(8);
                long duration = cursor.getLong(9);
                String agentJid = UdeskUtils.objectToString(cursor.getString(10));
                String replyUser = UdeskUtils.objectToString(cursor.getString(13));
                String reply_userurl = UdeskUtils.objectToString(cursor.getString(14));
                String subSeessionId = UdeskUtils.objectToString(cursor.getString(15));
                int seqNum = cursor.getInt(16);
                String fileName = UdeskUtils.objectToString(cursor.getString(17));
                String fileSize = UdeskUtils.objectToString(cursor.getString(18));
                int switchStaffType = UdeskUtils.objectToInt(cursor.getInt(19));
                String switchStaffTips = UdeskUtils.objectToString(cursor.getString(20));
                String topAsk = UdeskUtils.objectToString(cursor.getString(21));
                int logId = UdeskUtils.objectToInt(cursor.getInt(22));
                String webConfig = UdeskUtils.objectToString(cursor.getString(23));
                String sender = UdeskUtils.objectToString(cursor.getString(24));
                int flowId = UdeskUtils.objectToInt(cursor.getInt(25));
                String flowTitle = UdeskUtils.objectToString(cursor.getString(26));
                String flowContent = UdeskUtils.objectToString(cursor.getString(27));
                String question_id = UdeskUtils.objectToString(cursor.getString(28));
                String recommendationGuidance = UdeskUtils.objectToString(cursor.getString(29));
                msgInfo = new MessageInfo(time, msgId, msgtype,
                        msgContent, readFlag, sendFlag, playFlag, direction,
                        localPath, duration, agentJid);
                msgInfo.setFilename(fileName);
                msgInfo.setFilesize(fileSize);
                msgInfo.setSubsessionid(subSeessionId);
                msgInfo.setSeqNum(seqNum);
                msgInfo.setReplyUser(replyUser);
                msgInfo.setUser_avatar(reply_userurl);
                msgInfo.setSwitchStaffType(switchStaffType);
                msgInfo.setSwitchStaffTips(switchStaffTips);
                if (!TextUtils.isEmpty(topAsk)){
                    msgInfo.setTopAsk(JsonUtils.parseTopAsk(new JSONObject(topAsk)));
                }
                msgInfo.setLogId(logId);
                if (!TextUtils.isEmpty(webConfig)){
                    msgInfo.setWebConfig(JsonUtils.parseWebConfigBean(webConfig));
                }
                msgInfo.setSender(sender);
                msgInfo.setFlowId(flowId);
                msgInfo.setFlowTitle(flowTitle);
                msgInfo.setFlowContent(flowContent);
                msgInfo.setQuestion_id(question_id);
                msgInfo.setRecommendationGuidance(recommendationGuidance);
                if (!TextUtils.isEmpty(agentJid.trim())) {
                    String[] urlAndNick = getAgentUrlAndNick(agentJid);
                    if (urlAndNick != null) {
                        try {
                            msgInfo.setUser_avatar(urlAndNick[0]);
                            msgInfo.setReplyUser(urlAndNick[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return msgInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return msgInfo;
    }

    public Future<MessageInfo> getAgentLastMessageDB() {
        try {
            Future<MessageInfo> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<MessageInfo>() {
                @Override
                public MessageInfo call() throws Exception {
                    return getAgentLastMessage();
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //获取客服聊天记录最后一条
    public synchronized MessageInfo getAgentLastMessage() {


        String sql = "select * from " + UdeskDBHelper.UdeskMessage
                + " where Direction = ? "
                + " order by Time desc limit 1";
        MessageInfo msgInfo = new MessageInfo();
        if (getSQLiteDatabase() == null) {
            return msgInfo;
        }
        SQLiteDatabase db = getSQLiteDatabase();
        Cursor cursor = null;
        if (db == null) {
            return msgInfo;
        }
        try {
            cursor = db.rawQuery(sql, new String[]{String.valueOf(UdeskConst.ChatMsgDirection.Recv)});
            int count = cursor.getCount();
            if (count < 1) {
                return msgInfo;
            }
            while (cursor.moveToNext()) {
                String msgId = cursor.getString(0);
                long time = cursor.getLong(1);
                String msgContent = cursor.getString(2);
                String msgtype = cursor.getString(3);
                int readFlag = cursor.getInt(4);
                int sendFlag = cursor.getInt(5);
                int playFlag = cursor.getInt(6);
                int direction = cursor.getInt(7);
                String localPath = cursor.getString(8);
                long duration = cursor.getLong(9);
                String agentJid = UdeskUtils.objectToString(cursor.getString(10));
                String replyUser = UdeskUtils.objectToString(cursor.getString(13));
                String reply_userurl = UdeskUtils.objectToString(cursor.getString(14));
                String subSeessionId = UdeskUtils.objectToString(cursor.getString(15));
                int seqNum = cursor.getInt(16);
                String fileName = UdeskUtils.objectToString(cursor.getString(17));
                String fileSize = UdeskUtils.objectToString(cursor.getString(18));
                int switchStaffType = UdeskUtils.objectToInt(cursor.getInt(19));
                String switchStaffTips = UdeskUtils.objectToString(cursor.getString(20));
                String topAsk = UdeskUtils.objectToString(cursor.getString(21));
                int logId = UdeskUtils.objectToInt(cursor.getInt(22));
                String webConfig = UdeskUtils.objectToString(cursor.getString(23));
                String sender = UdeskUtils.objectToString(cursor.getString(24));
                int flowId = UdeskUtils.objectToInt(cursor.getInt(25));
                String flowTitle = UdeskUtils.objectToString(cursor.getString(26));
                String flowContent = UdeskUtils.objectToString(cursor.getString(27));
                String question_id = UdeskUtils.objectToString(cursor.getString(28));
                String recommendationGuidance = UdeskUtils.objectToString(cursor.getString(29));
                msgInfo = new MessageInfo(time, msgId, msgtype,
                        msgContent, readFlag, sendFlag, playFlag, direction,
                        localPath, duration, agentJid);
                msgInfo.setFilename(fileName);
                msgInfo.setFilesize(fileSize);
                msgInfo.setSeqNum(seqNum);
                msgInfo.setSubsessionid(subSeessionId);
                msgInfo.setReplyUser(replyUser);
                msgInfo.setUser_avatar(reply_userurl);
                msgInfo.setSwitchStaffType(switchStaffType);
                msgInfo.setSwitchStaffTips(switchStaffTips);
                if (!TextUtils.isEmpty(topAsk)){
                    msgInfo.setTopAsk(JsonUtils.parseTopAsk(new JSONObject(topAsk)));
                }
                msgInfo.setLogId(logId);
                if (!TextUtils.isEmpty(webConfig)){
                    msgInfo.setWebConfig(JsonUtils.parseWebConfigBean(webConfig));
                }
                msgInfo.setSender(sender);
                msgInfo.setFlowId(flowId);
                msgInfo.setFlowTitle(flowTitle);
                msgInfo.setFlowContent(flowContent);
                msgInfo.setQuestion_id(question_id);
                msgInfo.setRecommendationGuidance(recommendationGuidance);
                if (!TextUtils.isEmpty(agentJid.trim())) {
                    String[] urlAndNick = getAgentUrlAndNick(agentJid);
                    if (urlAndNick != null) {
                        try {
                            msgInfo.setUser_avatar(urlAndNick[0]);
                            msgInfo.setReplyUser(urlAndNick[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return msgInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return msgInfo;
    }

    public Future<Integer> getMessageCountDB() {
        try {
            Future<Integer> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return getMessageCount();
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 获取消息总数
     *
     * @return
     */
    public synchronized int getMessageCount() {
        int count = 0;
        String sql = "select count (*) as count  from "
                + UdeskDBHelper.UdeskMessage;
        SQLiteDatabase db = getSQLiteDatabase();
        if (db == null) {
            return 0;
        }
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public Future<Boolean> hasReceviedMsgDB(final String msgId) {
        try {
            Future<Boolean> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return hasReceviedMsg(msgId);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断消息是否已经存在
     *
     * @param msgId
     * @return
     */
    public synchronized boolean hasReceviedMsg(String msgId) {
        String sql = "select * from " + UdeskDBHelper.UdeskMessage + " where  MsgID = ? ";
        Cursor cursor = null;
        if (getSQLiteDatabase() == null) {
            return false;
        }
        try {
            cursor = getSQLiteDatabase().rawQuery(sql, new String[]{msgId});
            if (cursor != null && cursor.getCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public Future<Boolean> deleteAllMsgDB() {
        try {
            Future<Boolean> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return deleteAllMsg();
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 清楚所有的消息记录
     *
     * @return
     */
    public synchronized boolean deleteAllMsg() {
        try {
            if (getSQLiteDatabase() == null) {
                return false;
            }
            String sql = "delete from " + UdeskDBHelper.UdeskMessage;
            getSQLiteDatabase().execSQL(sql);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Future<Boolean> deleteMsgByIdDB(final String msgId) {
        try {
            Future<Boolean> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return deleteMsgById(msgId);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public synchronized boolean deleteMsgById(String msgId) {
        try {
            if (getSQLiteDatabase() == null) {
                return false;
            }
            String sql = "delete from " + UdeskDBHelper.UdeskMessage
                    + " where MsgID = ? ";
            getSQLiteDatabase().execSQL(sql, new Object[]{msgId});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void updateMsgHasReadDB(final String msgId) {
        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    updateMsgHasRead(msgId);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public synchronized void updateMsgHasRead(String msgId) {

        String sql = "update " + UdeskDBHelper.UdeskMessage + " set " + "ReadFlag= ?"
                + " where MsgID = ? ";
        try {
            if (getSQLiteDatabase() == null) {
                return;
            }
            getSQLiteDatabase().execSQL(sql, new Object[]{UdeskConst.ChatMsgReadFlag.read, msgId});
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateAllMsgReadDB() {
        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    updateAllMsgRead();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateAllMsgRead() {


        String sql = "update " + UdeskDBHelper.UdeskMessage + " set " + "ReadFlag= ?";
        try {
            if (getSQLiteDatabase() == null) {
                return;
            }
            getSQLiteDatabase().execSQL(sql, new Object[]{UdeskConst.ChatMsgReadFlag.read});
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public Future<List<MessageInfo>>  getUnReadMessagesDB() {
        try {
            Future<List<MessageInfo>> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<List<MessageInfo>>() {
                @Override
                public List<MessageInfo> call() throws Exception {
                    return getUnReadMessages();
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 获取最近的10条未读消息
     */
    public synchronized List<MessageInfo> getUnReadMessages() {

        String sql = "select * from " + UdeskDBHelper.UdeskMessage
                + " where  ReadFlag = ?"
                + " order by Time DESC limit 10 ";

        List<MessageInfo> list = new ArrayList<MessageInfo>();
        SQLiteDatabase db = getSQLiteDatabase();
        Cursor cursor = null;
        if (db == null) {
            return list;
        }
        try {
            cursor = db.rawQuery(sql, new String[]{UdeskConst.ChatMsgReadFlag.unread + ""});
            int count = cursor.getCount();
            if (count < 1) {
                return list;
            }
            while (cursor.moveToNext()) {
                String msgId = cursor.getString(0);
                String msgContent = cursor.getString(2);
                String msgtype = cursor.getString(3);
                MessageInfo message = new MessageInfo();
                message.setMsgId(msgId);
                message.setMsgContent(msgContent);
                message.setMsgtype(msgtype);
                list.add(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }

    public Future<Integer>  getUnReadMessageCountDB() {
        try {
            Future<Integer> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return getUnReadMessageCount();
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    //获取未读消息数
    public synchronized int getUnReadMessageCount() {
        int count;
        if (getSQLiteDatabase() == null) {
            return 0;
        }
        String sql = "select count(*) from " + UdeskDBHelper.UdeskMessage
                + " where  ReadFlag = ?";
        Cursor cursor = null;
        try {
            cursor = getSQLiteDatabase().rawQuery(sql, new String[]{UdeskConst.ChatMsgReadFlag.unread + ""});
            if (cursor.getCount() < 1) {
                return 0;
            }
            cursor.moveToFirst();
            count = cursor.getInt(0);
        } catch (Exception e) {
            throw e;
        } finally {
            if (cursor != null
                    && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return count;
    }

    public Future<String[]>  getAgentUrlAndNickDB(final String agentJId) {
        try {
            Future<String[]> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<String[]>() {
                @Override
                public String[] call() throws Exception {
                    return getAgentUrlAndNick(agentJId);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized String[] getAgentUrlAndNick(String agentJId) {
        String sql = "select * from " + UdeskDBHelper.UdeskAgentMsg
                + " where Receive_AgentJid = ?";
        if (getSQLiteDatabase() == null) {
            return null;
        }
        String[] urlAndNick = new String[2];
        Cursor cursor = null;
        try {
            cursor = getSQLiteDatabase().rawQuery(sql, new String[]{agentJId});
            if (cursor.moveToFirst()) {
                urlAndNick[0] = cursor.getString(1);
                urlAndNick[1] = cursor.getString(2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return urlAndNick;
    }

    public Future<Boolean> addAgentInfoDB(final AgentInfo agentInfo) {
        try {
            Future<Boolean> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return addAgentInfo(agentInfo);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public synchronized boolean addAgentInfo(AgentInfo agentInfo) {
        try {

            if (getSQLiteDatabase() == null) {
                return false;
            }

            String sql = "replace into "
                    + UdeskDBHelper.UdeskAgentMsg
                    + "(Receive_AgentJid ,HeadUrl ,AgentNick )"
                    + " values (?,?,?)";

            getSQLiteDatabase().execSQL(
                    sql,
                    new Object[]{agentInfo.getAgentJid(), agentInfo.getHeadUrl(), agentInfo.getAgentNick()});
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Future<Boolean> addInviterAgentInfoDB(final InviterAgentInfo agentInfo) {
        try {
            Future<Boolean> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return addInviterAgentInfo(agentInfo);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized boolean addInviterAgentInfo(InviterAgentInfo agentInfo) {
        try {

            if (getSQLiteDatabase() == null) {
                return false;
            }

            String sql = "replace into "
                    + UdeskDBHelper.UdeskAgentMsg
                    + "(Receive_AgentJid ,HeadUrl ,AgentNick )"
                    + " values (?,?,?)";

            getSQLiteDatabase().execSQL(
                    sql,
                    new Object[]{agentInfo.getJid(), agentInfo.getAvatar(), agentInfo.getNick_name()});
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addSubSessionIdDB(final String im_sub_session_id, final int seqNum) {
        try {
            UdeskSDKManager.getInstance().getSingleExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    addSubSessionId(im_sub_session_id,seqNum);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 本地保存序号字段
     *
     * @param im_sub_session_id
     * @param seqNum
     */
    public synchronized void addSubSessionId(String im_sub_session_id, int seqNum) {

        try {
            if (getSQLiteDatabase() == null) {
                return;
            }
            String sql = " replace into " + UdeskDBHelper.SubSessionId + "(SUBID,SEQNUM)  values (?,?)";
            getSQLiteDatabase().execSQL(sql, new Object[]{im_sub_session_id, seqNum});
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Future<Integer>  getSubSessionIdDB(final String im_sub_session_id) {
        try {
            Future<Integer> future = UdeskSDKManager.getInstance().getSingleExecutor().submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return getSubSessionId(im_sub_session_id);
                }
            });
            return future;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized int getSubSessionId(String im_sub_session_id) {
        try {
            String sql = "select SEQNUM from " + UdeskDBHelper.SubSessionId
                    + " where SUBID =?";
            Cursor cursor = null;
            int count = 0;
            try {
                if (getSQLiteDatabase() == null) {
                    return 1;
                }
                cursor = getSQLiteDatabase().rawQuery(sql, new String[]{im_sub_session_id});
                if (cursor.getCount() < 1) {
                    addSubSessionId(im_sub_session_id, count + 1);
                    return count + 1;
                }
                cursor.moveToFirst();
                count = cursor.getInt(0);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null
                        && !cursor.isClosed()) {
                    cursor.close();
                }
            }
            addSubSessionId(im_sub_session_id, count + 1);
            return count + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 1;
    }


}
