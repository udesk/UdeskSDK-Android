package cn.udesk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UdeskDBHelper extends SQLiteOpenHelper {

	public static String DATABASE_NAME = "udesk_sdk";
	public final static int DATABASE_VERSION = 2;

	
	public static String UdeskMessage = "udeskMessageInfo";

	public static String UdeskSendIngMsgs = "udesksendIngMsgs";

	public static String UdeskAgentMsg = "udeskAgentMsg";

	public UdeskDBHelper(Context context, String sdktoken) {
		super(context, DATABASE_NAME + sdktoken, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ UdeskMessage
				+ "(MsgID TEXT primary key,Time BIGINT,MsgContent TEXT,"
				+ "MsgType TEXT, ReadFlag INTEGER,SendFlag INTEGER,"
				+ "PlayedFlag INTEGER,Direction INTEGER,LocalPath Text,Duration INTEGER,AgentJid TEXT)");
	

		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ UdeskSendIngMsgs
				+ "( MsgID TEXT, SendFlag INTEGER, Time BIGINT, primary key(MsgID))");

		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ UdeskAgentMsg
				+ "( AgentJid TEXT, HeadUrl TEXT, AgentNick TEXT, primary key(AgentJid))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.beginTransaction();
		try {
			while (oldVersion < newVersion) {
				upgradeDB(db, oldVersion, newVersion);
				oldVersion++;
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}

	//增加agentJid字段
	private void upgradeDB(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion) {
			case 2:
				db.execSQL("ALTER TABLE UdeskMessage ADD COLUMN  AgentJid TEXT ");
				db.execSQL("CREATE TABLE IF NOT EXISTS "
						+ UdeskAgentMsg
						+ "( AgentJid TEXT, HeadUrl TEXT, AgentNick TEXT, primary key(AgentJid))");
				break;
			default:
				break;
		}
	}


}
