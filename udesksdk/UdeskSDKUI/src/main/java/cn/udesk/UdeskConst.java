package cn.udesk;



public class UdeskConst {

	public static class UdeskUserInfo {
		public static final String USER_SDK_TOKEN = "sdk_token";
		public static final String NICK_NAME = "nick_name";
		public static final String CELLPHONE = "cellphone";
		public static final String EMAIL = "email";
		public static final String DESCRIPTION = "description";

	}
	public static final String UDESKARTICLEID = "udesk_article_id";
	public static final String UDESKTRANSFER = "transfer";
	public static final String UDESKHTMLURL = "html_url";
	public static final String UDESKGROUPID = "udesk_groupid";
	public static final String UDESKISTRANFERSESSION= "udesk_tranfer_session";
	public static final String UDESKAGENTID = "udesk_agentid";
	public static  final  String  WELCOME_URL = "welcome_url";
	public static  final  String  WELCOME_URL_TITLE = "welcome_url_title";

	/**
	 * 每次读取消息表的记录数
	 */
	public final static int UDESK_HISTORY_COUNT = 20;
	
	public final static String ORIGINAL_SUFFIX = "_upload.jpg" ;
	public final static String AUDIO_SUF = ".aac";
	
	public final static int recordStateNum = 8;
	
	public static String SurvyDialogKey = "survydialogkey";
	public static String SurvyOptionIDKey = "survyoptionidkey";
	
	public static class SendFlag {
		public final static int RESULT_SEND = 0;// 这个是默认值 发送中
		public final static int RESULT_SUCCESS = 1;// 发送成功
		public final static int RESULT_RETRY =2;
		public final static int RESULT_FAIL = 3;// 发送失败
	}
	
    public static class ChatMsgDirection
	{
		public static final int Send = 1;
		public static final int Recv = 2;
	}
    public static class ChatMsgReadFlag
    {
    	public static final int read = 0;
    	public static final int unread = 1;
    }
    
	public static class ChatMsgTypeInt
	{
		public static final int TYPE_IMAGE = 0;
		public static final int TYPE_AUDIO = 1;
		public static final int TYPE_TEXT = 2;
		public static final int TYPE_REDIRECT = 3;
		public static final int TYPE_RICH = 4;
	}
	
	public static int parseTypeForMessage(String type) {
		if("message".equalsIgnoreCase(type)){
			return ChatMsgTypeInt.TYPE_TEXT;
		}else if ("image".equalsIgnoreCase(type)) {
			return ChatMsgTypeInt.TYPE_IMAGE;
		} else if ("audio".equalsIgnoreCase(type)) {
			return ChatMsgTypeInt.TYPE_AUDIO;
		} else if("redirect".equalsIgnoreCase(type)){
			return ChatMsgTypeInt.TYPE_REDIRECT;
		}else if("rich".equalsIgnoreCase(type)){
			return ChatMsgTypeInt.TYPE_RICH;
		}
		return ChatMsgTypeInt.TYPE_TEXT;
	}
	
	public static class ChatMsgTypeString{
		
		public static final String TYPE_IMAGE = "image";
		public static final String TYPE_AUDIO = "audio";
		public static final String TYPE_TEXT = "message";
		public static final String TYPE_REDIRECT = "redirect";
	}
	
	public static class PlayFlag{
		public static final int NOPLAY = -1;
		public static final int PLAYED = 0;
    	public static final int UNPLAYED = 1;
	}
	
	public static class AgentReponseCode{
		public static final int HasAgent = 2000; //  2000指 返回合适的客服JID
    	public static final int WaitAgent = 2001; // 2001表示 当前终端用户需要排队
    	public static final int NoAgent = 2002; // // 2002指 当前没有客服在线
		public static final int NonExistentAgent = 5050; //客服不存在
		public static final int NonExistentGroupId = 5060; //客服组不存在
	}
	
	public static class SharePreParams{
		public static  String RegisterIdName = "registeridname";
		public static  String Udesk_Sharepre_Name = "udesk_sdk";
		public static final String Udesk_SdkToken = "udesk_sdktoken";
//		public static final String Udesk_Transfer = "udesk_transfer";
//		public static final String Udesk_h5url = "udesk_h5url";
//		public static final String Udesk_userid = "udesk_userId";
		public static final String Udesk_Push_RegisterId = "udesk_push_registerid";
	}
	
	public static class AgentFlag{
		public static final int NoAgent = 1;
		public static final int WaitAgent = 2;
		public static final int HasAgent = 3;
	}

}
