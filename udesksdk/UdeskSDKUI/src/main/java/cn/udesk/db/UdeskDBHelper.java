package cn.udesk.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UdeskDBHelper extends SQLiteOpenHelper {

    public static String DATABASE_NAME = "udesk_sdk";
    public final static int DATABASE_VERSION = 8;


    public static String UdeskMessage = "udeskMessageInfo";

    public static String UdeskSendIngMsgs = "udesksendIngMsgs";

    public static String UdeskAgentMsg = "udeskAgentMsg";

    public static String SubSessionId = "sub_sessionid";

    public UdeskDBHelper(Context context, String sdktoken) {
        super(context, DATABASE_NAME + sdktoken, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS "
                    + UdeskMessage
                    + "(MsgID TEXT primary key,Time BIGINT,MsgContent TEXT,"
                    + "MsgType TEXT, ReadFlag INTEGER,SendFlag INTEGER,"
                    + "PlayedFlag INTEGER,Direction INTEGER,LocalPath Text,"
                    + "Duration INTEGER,Receive_AgentJid TEXT,created_at TEXT,"
                    + "updated_at TEXT,reply_user TEXT,reply_userurl TEXT,"
                    + "subsessionid TEXT,seqNum INTEGER,fileName TEXT,fileSize TEXT,"
                    + "switchStaffType INTEGER,switchStaffTips TEXT,topAsk TEXT,"
                    + "logId INTEGER,webConfig TEXT,sender TEXT,flowId INTEGER,"
                    + "flowTitle TEXT,flowContent TEXT,question_id TEXT,recommendationGuidance TEXT)");


            db.execSQL("CREATE TABLE IF NOT EXISTS "
                    + UdeskSendIngMsgs
                    + "( MsgID TEXT, SendFlag INTEGER, Time BIGINT, primary key(MsgID))");

            db.execSQL("CREATE TABLE IF NOT EXISTS "
                    + UdeskAgentMsg
                    + "( Receive_AgentJid TEXT, HeadUrl TEXT, AgentNick TEXT, primary key(Receive_AgentJid))");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + SubSessionId
                    + "( SUBID TEXT primary key, SEQNUM INTEGER)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            while (oldVersion <newVersion){
                onUpgradeDB(db, oldVersion);
                if (oldVersion <=4){
                    oldVersion = 4;
                }
                oldVersion++;
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }


    public void onUpgradeDB(SQLiteDatabase db, int oldVersion) {

        try {
            if (oldVersion <= 4) {

                db.execSQL("DROP TABLE IF EXISTS udesksendIngMsgs");
                db.execSQL("DROP TABLE IF EXISTS udeskAgentMsg");
                db.execSQL("DROP TABLE IF EXISTS sub_sessionid");

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
            } else {
                switch (oldVersion) {
                    case 5:
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  fileName TEXT ");
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  fileSize TEXT ");
                        break;
                    case 6:
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  switchStaffType INTEGER ");
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  switchStaffTips TEXT ");
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  topAsk TEXT ");
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  logId INTEGER ");
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  webConfig TEXT ");
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  sender TEXT ");
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  flowId INTEGER ");
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  flowTitle TEXT ");
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  flowContent TEXT ");
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  question_id TEXT ");
                        break;
                    case 7:
                        db.execSQL("ALTER TABLE udeskMessageInfo ADD COLUMN  recommendationGuidance TEXT ");
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


}
