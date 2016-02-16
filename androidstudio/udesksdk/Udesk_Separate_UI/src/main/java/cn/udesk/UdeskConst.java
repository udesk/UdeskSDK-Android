package cn.udesk;



public class UdeskConst {

	public static final String UDESKARTICLEID = "udesk_article_id";
	public static final String UDESKTRANSFER = "transfer";
	public static final String UDESKHTMLURL = "html_url";
	
	/**
	 * 每次读取消息表的记录数
	 */
	public final static int UDESK_HISTORY_COUNT = 20;
	
	public final static String ORIGINAL_SUFFIX = "_upload.jpg" ;
	public final static String AUDIO_SUF = ".aac";
	
	public final static int recordStateNum = 8;
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
	}
	
	public static class SharePreParams{
		public static final String Udesk_Sharepre_Name = "udesk_sdk"; 
		public static final String Udesk_Domain = "udesk_domain";
		public static final String Udesk_SecretKey = "udesk_secretkey";
		public static final String Udesk_SdkToken = "udesk_sdktoken";
		public static final String Udesk_Transfer = "udesk_transfer";
		public static final String Udesk_h5url = "udesk_h5url";
		public static final String Udesk_userid = "udesk_userId";
	}
	
	public static class AgentFlag{
		public static final int NoAgent = 1;
		public static final int WaitAgent = 2;
		public static final int HasAgent = 3;
	}
}
