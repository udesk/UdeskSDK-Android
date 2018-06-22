package cn.udesk.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import udesk.core.UdeskConst;
import udesk.core.model.AgentInfo;
import udesk.core.model.MessageInfo;


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

    public  boolean isNeedInit(String sdktoken){

        try {
            if (mSdktoken != null  && mSdktoken.equals(sdktoken) && mDatabase != null){
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  true;
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
                if (msg.getDirection() == UdeskConst.ChatMsgDirection.Send) {
                    if (isExitMessage(msg.getMsgId()) == null) {
                        addMessageInfo(msg);
                    }
                } else {
                    addMessageInfo(msg);
                }
            }
            getSQLiteDatabase().setTransactionSuccessful();
        } catch (Exception e) {

            return false;
        } finally {
            getSQLiteDatabase().endTransaction();
        }
        return true;
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
                    + "Direction,LocalPath,Duration,Receive_AgentJid,created_at," +
                    "updated_at,reply_user,reply_userurl,subsessionid,seqNum,fileName,fileSize)"
                    + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

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
                            msg.getSeqNum(),msg.getFilename(),msg.getFilesize()
                    });
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //更新消息的内容
    public synchronized boolean updateMsgContent(String msgid, String text) {

        String sql = "update " + UdeskDBHelper.UdeskMessage + " set " + "MsgContent= ?"
                + " where MsgID = ? ";
        try {
            if (getSQLiteDatabase() == null) {
                return false;
            }
            getSQLiteDatabase().execSQL(sql, new Object[]{text, msgid});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    //更新缓存的路径
    public boolean updateMsgLoaclUrl(String msgid, String text) {

        String sql = "update " + UdeskDBHelper.UdeskMessage + " set " + "LocalPath= ?"
                + " where MsgID = ? ";
        try {
            if (getSQLiteDatabase() == null) {
                return false;
            }
            getSQLiteDatabase().execSQL(sql, new Object[]{text, msgid});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

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

    //更新消息发送中的为发送失败
    public synchronized boolean updateSendFlagToFail() {
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
                String fileName = UdeskUtil.objectToString(cursor.getString(17));
                String fileSize = UdeskUtil.objectToString(cursor.getString(18));
                msg = new MessageInfo(time, msgId, msgtype, msgContent,
                        readFlag, sendFlag, playFlag, direction, localPath,
                        duration, agentJid);
                msg.setSubsessionid(subsessionid);
                msg.setSeqNum(seqNum);
                msg.setUser_avatar(reply_userurl);
                msg.setReplyUser(replyUser);
                msg.setFilename(fileName);
                msg.setFilesize(fileSize);
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
                String agentJid = UdeskUtil.objectToString(cursor.getString(10));
                String createdTime = UdeskUtil.objectToString(cursor.getString(11));
                String updatedTime = UdeskUtil.objectToString(cursor.getString(12));
                String replyUser = UdeskUtil.objectToString(cursor.getString(13));
                String reply_userurl = UdeskUtil.objectToString(cursor.getString(14));
                String subSeessionId = UdeskUtil.objectToString(cursor.getString(15));
                int seqNum = cursor.getInt(16);
                String fileName = UdeskUtil.objectToString(cursor.getString(17));
                String fileSize = UdeskUtil.objectToString(cursor.getString(18));

                if (sendFlag == UdeskConst.SendFlag.RESULT_SEND && System.currentTimeMillis() -time > 30 * 1000) {
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
                String agentJid = UdeskUtil.objectToString(cursor.getString(10));
                String replyUser = UdeskUtil.objectToString(cursor.getString(13));
                String reply_userurl = UdeskUtil.objectToString(cursor.getString(14));
                String subSeessionId = UdeskUtil.objectToString(cursor.getString(15));
                int seqNum = cursor.getInt(16);
                String fileName = UdeskUtil.objectToString(cursor.getString(17));
                String fileSize = UdeskUtil.objectToString(cursor.getString(18));
                msgInfo = new MessageInfo(time, msgId, msgtype,
                        msgContent, readFlag, sendFlag, playFlag, direction,
                        localPath, duration, agentJid);
                msgInfo.setFilename(fileName);
                msgInfo.setFilesize(fileSize);
                msgInfo.setSubsessionid(subSeessionId);
                msgInfo.setSeqNum(seqNum);
                msgInfo.setReplyUser(replyUser);
                msgInfo.setUser_avatar(reply_userurl);

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
                String agentJid = UdeskUtil.objectToString(cursor.getString(10));
                String replyUser = UdeskUtil.objectToString(cursor.getString(13));
                String reply_userurl = UdeskUtil.objectToString(cursor.getString(14));
                String subSeessionId = UdeskUtil.objectToString(cursor.getString(15));
                int seqNum = cursor.getInt(16);
                String fileName = UdeskUtil.objectToString(cursor.getString(17));
                String fileSize = UdeskUtil.objectToString(cursor.getString(18));
                msgInfo = new MessageInfo(time, msgId, msgtype,
                        msgContent, readFlag, sendFlag, playFlag, direction,
                        localPath, duration, agentJid);
                msgInfo.setFilename(fileName);
                msgInfo.setFilesize(fileSize);
                msgInfo.setSeqNum(seqNum);
                msgInfo.setSubsessionid(subSeessionId);
                msgInfo.setReplyUser(replyUser);
                msgInfo.setUser_avatar(reply_userurl);
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

    /**
     * 增加一条在发送的消息
     *
     * @param msgId
     * @param sendFlag
     * @param time
     * @return
     */
    public synchronized boolean addSendingMsg(String msgId, int sendFlag, long time) {
        if (getSQLiteDatabase() == null) {
            return false;
        }
        String sql = "replace into " + UdeskDBHelper.UdeskSendIngMsgs
                + "(MsgID,SendFlag,Time) values(?,?,?)";
        try {
            getSQLiteDatabase().execSQL(sql,
                    new Object[]{msgId, sendFlag, time});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除一条发送中的消息
     *
     * @param msgId
     * @return
     */
    public boolean deleteSendingMsg(String msgId) {
        if (getSQLiteDatabase() == null) {
            return false;
        }
        String sql = "delete from " + UdeskDBHelper.UdeskSendIngMsgs + " where MsgID=?";
        try {
            getSQLiteDatabase().execSQL(sql, new Object[]{msgId});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除所有的发送中的消息
     */
    public synchronized boolean delAllSendingMsg() {
        if (getSQLiteDatabase() == null) {
            return false;
        }
        String sql = "delete from " + UdeskDBHelper.UdeskSendIngMsgs;
        try {
            getSQLiteDatabase().execSQL(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取5秒到半分钟之间都没发送成功的所有消息的MsgID
     *
     * @param currentTime
     * @return
     */
    public synchronized List<String> getNeedRetryMsg(long currentTime) {
        String sql = "select MsgID from " + UdeskDBHelper.UdeskSendIngMsgs + " where (" + currentTime + " - Time >= 5000 )" + " And (" + currentTime + " - Time <= 30000 )";
        List<String> listItems = null;
        Cursor cursor = null;
        if (getSQLiteDatabase() == null) {
            return null;
        }
        try {
            cursor = getSQLiteDatabase().rawQuery(sql, null);
            int count = cursor.getCount();
            if (count < 1) {
                return null;
            }
            listItems = new ArrayList<String>();
            while (cursor.moveToNext()) {
                listItems.add(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return listItems;
    }

    /**
     * 获取大于半分钟都未发送成功的消息
     *
     * @param currentTime
     * @return
     */
    public synchronized List<String> getNeedUpdateFailedMsg(long currentTime) {
        String sql = "select MsgID from " + UdeskDBHelper.UdeskSendIngMsgs + " where (" + currentTime + " - Time > 30000 )";
        List<String> listItems = null;
        Cursor cursor = null;
        if (getSQLiteDatabase() == null) {
            return null;
        }
        try {
            cursor = getSQLiteDatabase().rawQuery(sql, null);
            int count = cursor.getCount();
            if (count < 1) {
                return null;
            }
            listItems = new ArrayList<String>();
            while (cursor.moveToNext()) {
                listItems.add(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return listItems;
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

    /**
     * 清楚所有的消息记录
     *
     * @return
     */
    public boolean deleteAllMsg() {
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

    public boolean deleteMsgById(String msgId) {
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
            if (cursor.getCount() < 1)
                return 0;
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
