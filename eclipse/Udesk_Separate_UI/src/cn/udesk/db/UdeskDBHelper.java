package cn.udesk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UdeskDBHelper extends SQLiteOpenHelper {

	public static String DATABASE_NAME = "udesk_sdk";
	public final static int DATABASE_VERSION = 1;

	
	public static String UdeskMessage = "udeskMessageInfo";

	public static String UdeskSendIngMsgs = "udesksendIngMsgs";

	public UdeskDBHelper(Context context, String sdktoken) {
		super(context, DATABASE_NAME + sdktoken, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ UdeskMessage
				+ "(MsgID TEXT primary key,Time BIGINT,MsgContent TEXT,"
				+ "MsgType TEXT, ReadFlag INTEGER,SendFlag INTEGER,"
				+ "PlayedFlag INTEGER,Direction INTEGER,LocalPath Text,Duration INTEGER)");
	

		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ UdeskSendIngMsgs
				+ "( MsgID TEXT, SendFlag INTEGER, Time BIGINT, primary key(MsgID))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}




}
