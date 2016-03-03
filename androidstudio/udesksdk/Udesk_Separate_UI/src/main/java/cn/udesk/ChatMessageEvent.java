package cn.udesk;

import udesk.core.model.MessageInfo;

public interface ChatMessageEvent {
	
	/**
	 * 收到对发送消息的回执消息
	 * @param msgId  对应于之前发送消息的msgId
	 */
	void onMessageReceived(String msgId);
	
	/**
	 * 收到一条新消息
	 */
	void onNewMessage(MessageInfo messge);
	
	
	/**
	 * 收到客服在线或下线的通知
	 * @param jid
	 * @param onlineFlag
	 */
	void onPrenseMessage(String jid , int onlineFlag);
	
	/**
	 * 收到评价邀请的通知
	 * @param isSurvey
	 */
	void onReqsurveyMsg(boolean isSurvey);

}
