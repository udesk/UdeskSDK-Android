package cn.udesk.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import udesk.core.model.AgentInfo;
import udesk.core.model.MessageInfo;


public class UdeskDBManager {

	private static UdeskDBHelper helper;
//	private static UdeskDBManager instance;
	private SQLiteDatabase mDatabase;
//	private Context mContext;
	private String mSdktoken;

	private static UdeskDBManager instance = new UdeskDBManager();

	private UdeskDBManager() {

	}

	public static UdeskDBManager getInstance() {
		return instance;
	}

//	public static synchronized UdeskDBManager getInstance() {
//		if (instance == null) {
//			instance = new UdeskDBManager();
//		}
//		return instance;
//	}

	/**
	 * 初始化，需要在使用数据库之前调用此方法
	 * 
	 * @param context
	 */
	public synchronized void init(Context context, String sdktoken) {
		if(context == null){
			return;
		}
//		mContext = context;
		if(TextUtils.isEmpty(sdktoken)){
			sdktoken = UdeskSDKManager.getInstance().getSdkToken(context);
		}
		mSdktoken = sdktoken;
		if (helper == null) {
			helper = new UdeskDBHelper(context, mSdktoken);
		}
		mDatabase = helper.getWritableDatabase();
	}

	/**
	 * 退出时，释放
	 */
	public synchronized void release() {
		if (helper != null) {
			helper.close();
			helper = null;
		}

//		if (mContext != null) {
//			mContext = null;
//		}
		if (mSdktoken != null) {
			mSdktoken = null;
		}
	}

	public SQLiteDatabase getSQLiteDatabase() {
		return mDatabase;
//		if (mDatabase != null) {
//			return mDatabase;
//		} else {
//			try {
//				synchronized (this) {
//					if (mDatabase == null) {
//						init(mContext, mSdktoken);
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			return mDatabase;
//		}

	}

	/**
	 * db中增加一条消息的语句
	 * 
	 * @param msg
	 */

	public boolean addMessageInfo(MessageInfo msg) {
		try {

			if (getSQLiteDatabase() == null) {
				return false;
			}

			String sql = "replace into "
					+ UdeskDBHelper.UdeskMessage
					+ "(MsgID ,Time ,MsgContent,MsgType,ReadFlag,SendFlag,PlayedFlag,"
					+ "Direction,LocalPath,Duration,AgentJid)"
					+ " values (?,?,?,?,?,?,?,?,?,?,?)";

			getSQLiteDatabase().execSQL(
					sql,
					new Object[] { msg.getMsgId(), msg.getTime(),
							msg.getMsgContent(), msg.getMsgtype(),
							msg.getReadFlag(), msg.getSendFlag(),
							msg.getPlayflag(), msg.getDirection(),
							msg.getLocalPath(), msg.getDuration() ,msg.getmAgentJid()});
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	//更新消息的内容
	public boolean updateMsgContent(String msgid,String text){
		
		String sql =  "update " +  UdeskDBHelper.UdeskMessage + " set " + "MsgContent= ?"
				+ " where MsgID = ? ";
		try 
		{
			if (getSQLiteDatabase() == null) {
				return false;
			}
			getSQLiteDatabase().execSQL(sql, new Object[] { text ,msgid });
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
		
	}

	//更新消息发送的状态
	public boolean updateMsgSendFlag(String msgId, int sendflag) {

		String sql = "update " + UdeskDBHelper.UdeskMessage + " set "
				+ "SendFlag= ?" + " where  MsgID = ? ";
		try {
			if (getSQLiteDatabase() == null) {
				return false;
			}
			getSQLiteDatabase().execSQL(sql, new Object[] { sendflag, msgId });
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
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

	//根据消息的ID查询这条消息
	public MessageInfo getMessage(String msgid) {
		String sql = "select * from " + UdeskDBHelper.UdeskMessage
				+ " where MsgID = ?";

		MessageInfo msg = null;
		Cursor cursor = null;
		try {
			cursor = getSQLiteDatabase().rawQuery(sql, new String[] { msgid });
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
				msg = new MessageInfo(time, msgId, msgtype, msgContent,
						readFlag, sendFlag, playFlag, direction, localPath,
						duration,agentJid);
				if (!TextUtils.isEmpty(agentJid.trim())){
					String[] urlAndNick = getAgentUrlAndNick(agentJid);
					msg.setAgentUrl(urlAndNick[0]);
					msg.setNickName(urlAndNick[1]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return msg;
	}

	/**
	 * 获取指定条数的聊天记录
	 * 
	 * @param offset
	 *            偏移量
	 * @param pageNum
	 *            默认每次查询的数量 见UdeskConst.UDESK_HISTORY_COUNT值
	 * @return
	 */
	public List<MessageInfo> getMessages(int offset, int pageNum) {

		String sql = "select * from " + UdeskDBHelper.UdeskMessage
				+ " order by Time limit " + UdeskConst.UDESK_HISTORY_COUNT
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
				String agentJid = cursor.getString(10);
				MessageInfo message = new MessageInfo(time, msgId, msgtype,
						msgContent, readFlag, sendFlag, playFlag, direction,
						localPath, duration,agentJid);
				if (!TextUtils.isEmpty(agentJid.trim())){
					String[] urlAndNick = getAgentUrlAndNick(agentJid);
					message.setAgentUrl(urlAndNick[0]);
					message.setNickName(urlAndNick[1]);
				}
				list.add(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return list;
	}

	/**
	 * 获取消息总数
	 * 
	 * @return
	 */
	public int getMessageCount() {
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
				cursor = null;
			}
		}
		return count;
	}

	/**
	 * 增加一条在发送的消息
	 * @param msgId
	 * @param sendFlag
	 * @param time
	 * @return
	 */
	public boolean addSendingMsg(String msgId, int sendFlag, long time) {
		if (getSQLiteDatabase() == null) {
			return false;
		}
		String sql = "replace into " + UdeskDBHelper.UdeskSendIngMsgs
				+ "(MsgID,SendFlag,Time) values(?,?,?)";
		try {
			getSQLiteDatabase().execSQL(sql,
					new Object[] { msgId, sendFlag, time });
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 删除一条发送中的消息
	 * @param msgId
	 * @return
	 */
	public boolean deleteSendingMsg(String msgId)
	{
		if (getSQLiteDatabase() == null) {
			return false;
		}
		String sql = "delete from " + UdeskDBHelper.UdeskSendIngMsgs +" where MsgID=?";
		try {
			getSQLiteDatabase().execSQL(sql, new Object[] {msgId});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 删除所有的发送中的消息
	 */
	public boolean delAllSendingMsg()
	{
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
	 * 获取10秒到半分钟之间都没发送成功的所有消息的MsgID
	 * @param currentTime
	 * @return
	 */
	public List<String> getNeedRetryMsg(long currentTime){
		String sql = "select MsgID from " + UdeskDBHelper.UdeskSendIngMsgs + " where ("+ currentTime + " - Time >= 5000 )"  + " And (" + currentTime + " - Time <= 30000 )";
		List<String> listItems = null;
		Cursor cursor = null;
		if(getSQLiteDatabase() == null){
			return null;
		}
		try {
    		cursor = getSQLiteDatabase().rawQuery(sql, null);
    		int count = cursor.getCount();
		    if (count < 1){
			   return null;
		    }
		    listItems = new ArrayList<String>();
    		while(cursor.moveToNext()){
    			listItems.add(cursor.getString(0));
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally{
    		if(cursor != null){
    			cursor.close();
    			cursor = null;
    		}
    	}
		return listItems;
	}
	
	/**
	 * 获取大于半分钟都未发送成功的消息
	 * @param currentTime
	 * @return
	 */
	public List<String> getNeedUpdateFailedMsg(long currentTime){
		String sql = "select MsgID from " + UdeskDBHelper.UdeskSendIngMsgs + " where ("+ currentTime + " - Time > 30000 )";
		List<String> listItems = null;
		Cursor cursor = null;
		if(getSQLiteDatabase() == null){
			return null;
		}
		try {
    		cursor = getSQLiteDatabase().rawQuery(sql, null);
    		int count = cursor.getCount();
		    if (count < 1){
			   return null;
		    }
		    listItems = new ArrayList<String>();
    		while(cursor.moveToNext()){
    			listItems.add(cursor.getString(0));
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally{
    		if(cursor != null){
    			cursor.close();
    		}
    	}
		return listItems;
	}
	
	/**
	 * 判断消息是否已经存在
	 * @param msgId
	 * @return
	 */
	public boolean hasReceviedMsg(String msgId){
		String sql = "select * from " + UdeskDBHelper.UdeskMessage+ " where  MsgID = ? " ;
		Cursor cursor = null;
		if(getSQLiteDatabase() == null){
			return false;
		}
		try {
			cursor = getSQLiteDatabase().rawQuery(sql, new String[]{msgId});
			if(cursor !=null && cursor.getCount() > 0){
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (cursor !=null){
				cursor.close();
				cursor = null;
			}
		}
		return false;
	}
	
	/**
	 * 清楚所有的消息记录
	 * @return
	 */
	public boolean deleteAllMsg() {
		try {
			String sql =  "delete from " +  UdeskDBHelper.UdeskMessage ;
			getSQLiteDatabase().execSQL(sql);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

//	public void setContext(Context mContext) {
//		this.mContext = mContext;
//	}

	public void updateMsgHasRead(String msgId){

		String sql =  "update " +  UdeskDBHelper.UdeskMessage + " set " + "ReadFlag= ?"
				+ " where MsgID = ? ";
		try
		{
			getSQLiteDatabase().execSQL(sql, new Object[] { UdeskConst.ChatMsgReadFlag.read,msgId});
		} catch (Exception e) {

		}

	}

	public void updateAllMsgRead(){

		String sql =  "update " +  UdeskDBHelper.UdeskMessage + " set " + "ReadFlag= ?";
		try
		{
			getSQLiteDatabase().execSQL(sql, new Object[] { UdeskConst.ChatMsgReadFlag.read});
		} catch (Exception e) {

		}

	}


	/**
	 * 获取最近的10条未读消息
     */
	public List<MessageInfo> getUnReadMessages() {

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
			cursor = db.rawQuery(sql,  new String[]{UdeskConst.ChatMsgReadFlag.unread+""});
			int count = cursor.getCount();
			if (count < 1) {
				return list;
			}
			while (cursor.moveToNext()) {
				String msgId = cursor.getString(0);
//				long time = cursor.getLong(1);
				String msgContent = cursor.getString(2);
				String msgtype = cursor.getString(3);
//				int readFlag = cursor.getInt(4);
//				int sendFlag = cursor.getInt(5);
//				int playFlag = cursor.getInt(6);
//				int direction = cursor.getInt(7);
//				String localPath = cursor.getString(8);
//				long duration = cursor.getLong(9);
//				String agentJid = cursor.getString(10);
				MessageInfo message = new MessageInfo();
				message.setMsgId(msgId);
				message.setMsgContent(msgContent);
				message.setMsgtype(msgtype);
//				if (!TextUtils.isEmpty(agentJid.trim())){
//					String[] urlAndNick = getAgentUrlAndNick(agentJid);
//					message.setAgentUrl(urlAndNick[0]);
//					message.setNickName(urlAndNick[1]);
//				}
				list.add(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return list;
	}

	//获取未读消息数
	public int getUnReadMessageCount() {
		try {
			String sql = "select count(*) from " + UdeskDBHelper.UdeskMessage
					+ " where  ReadFlag = ?";
			Cursor cursor = null;
			int count = 0;
			try {
				cursor = getSQLiteDatabase().rawQuery(sql, new String[]{UdeskConst.ChatMsgReadFlag.unread+""});
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
		} catch (Exception e) {

		}

		return 0;
	}


	public String[] getAgentUrlAndNick(String agentJId) {
		String sql = "select * from " + UdeskDBHelper.UdeskAgentMsg
				+ " where AgentJid = ?";

		String[] urlAndNick = new String[2];
		Cursor cursor = null;
		try {
			cursor = getSQLiteDatabase().rawQuery(sql, new String[] { agentJId });
			if (cursor.moveToFirst()) {
				urlAndNick[0]  = cursor.getString(1);
				urlAndNick[1] =  cursor.getString(2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return urlAndNick;
	}


	public boolean addAgentInfo(AgentInfo agentInfo) {
		try {

			if (getSQLiteDatabase() == null) {
				return false;
			}

			String sql = "replace into "
					+ UdeskDBHelper.UdeskAgentMsg
					+ "(AgentJid ,HeadUrl ,AgentNick )"
					+ " values (?,?,?)";

			getSQLiteDatabase().execSQL(
					sql,
					new Object[] { agentInfo.getAgentJid(),agentInfo.getHeadUrl(),agentInfo.getAgentNick()});
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}


}
