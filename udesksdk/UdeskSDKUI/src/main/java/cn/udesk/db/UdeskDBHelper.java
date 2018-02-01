package cn.udesk.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UdeskDBHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "udesk_sdk";
    public final static int DATABASE_VERSION = 5;


    public static String UdeskMessage = "udeskMessageInfo";

    public static String UdeskSendIngMsgs = "udesksendIngMsgs";

    public static String UdeskAgentMsg = "udeskAgentMsg";

    public static String SubSessionId = "sub_sessionid";

    public UdeskDBHelper(Context context, String sdktoken) {
        super(context, DATABASE_NAME + sdktoken, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + UdeskMessage
                + "(MsgID TEXT primary key,Time BIGINT,MsgContent TEXT,"
                + "MsgType TEXT, ReadFlag INTEGER,SendFlag INTEGER,"
                + "PlayedFlag INTEGER,Direction INTEGER,LocalPath Text,"
                + "Duration INTEGER,Receive_AgentJid TEXT,created_at TEXT,"
                + "updated_at TEXT,reply_user TEXT,reply_userurl TEXT,"
                + "subsessionid TEXT,seqNum INTEGER)");


        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + UdeskSendIngMsgs
                + "( MsgID TEXT, SendFlag INTEGER, Time BIGINT, primary key(MsgID))");

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + UdeskAgentMsg
                + "( Receive_AgentJid TEXT, HeadUrl TEXT, AgentNick TEXT, primary key(Receive_AgentJid))");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + SubSessionId
                + "( SUBID TEXT primary key, SEQNUM INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {


            db.execSQL("DROP TABLE IF EXISTS udesksendIngMsgs");
            db.execSQL("DROP TABLE IF EXISTS udeskAgentMsg");
            db.execSQL("DROP TABLE IF EXISTS sub_sessionid");
            onUpgradeDB(db, oldVersion);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }


    public void onUpgradeDB(SQLiteDatabase db, int oldVersion) {

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + UdeskSendIngMsgs
                + "( MsgID TEXT, SendFlag INTEGER, Time BIGINT, primary key(MsgID))");

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + UdeskAgentMsg
                + "( Receive_AgentJid TEXT, HeadUrl TEXT, AgentNick TEXT, primary key(Receive_AgentJid))");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + SubSessionId
                + "( SUBID TEXT primary key, SEQNUM INTEGER)");


        String tempMessageInfo = "TempMessageInfo";
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + tempMessageInfo
                + "(MsgID TEXT primary key,Time BIGINT,MsgContent TEXT,"
                + "MsgType TEXT, ReadFlag INTEGER,SendFlag INTEGER,"
                + "PlayedFlag INTEGER,Direction INTEGER,LocalPath Text,"
                + "Duration INTEGER,Receive_AgentJid TEXT,created_at TEXT,"
                + "updated_at TEXT,reply_user TEXT,reply_userurl TEXT,"
                + "subsessionid TEXT,seqNum INTEGER)");

        if (oldVersion < 3) {
            db.execSQL(" INSERT INTO TempMessageInfo "
                    + "(MsgID,Time,MsgContent,MsgType,ReadFlag,SendFlag,PlayedFlag,Direction,LocalPath,Duration) "
                    + "SELECT MsgID,Time,MsgContent,MsgType,ReadFlag,SendFlag,PlayedFlag,Direction,LocalPath,Duration "
                    + " FROM udeskMessageInfo ");
        } else if (oldVersion == 3) {
            db.execSQL(" INSERT INTO TempMessageInfo "
                    + "(MsgID,Time,MsgContent,MsgType,ReadFlag,SendFlag,PlayedFlag,Direction,LocalPath,Duration,Receive_AgentJid,created_at,reply_user,reply_userurl) "
                    + "SELECT MsgID,Time,MsgContent,MsgType,ReadFlag,SendFlag,PlayedFlag,Direction,LocalPath,Duration,AgentJid,created_at,reply_user,reply_userurl "
                    + " FROM udeskMessageInfo ");
        } else if (oldVersion == 4) {
            db.execSQL(" INSERT INTO TempMessageInfo "
                    + "(MsgID,Time,MsgContent,MsgType,ReadFlag,SendFlag,PlayedFlag,Direction,LocalPath,Duration) "
                    + "SELECT MsgID,Time,MsgContent,MsgType,ReadFlag,SendFlag,PlayedFlag,Direction,LocalPath,Duration "
                    + " FROM udeskMessageInfo ");
        }
        db.execSQL("DROP TABLE udeskMessageInfo");
        db.execSQL("ALTER TABLE TempMessageInfo RENAME TO udeskMessageInfo");
    }


}
