package cn.udesk.presenter;

import android.content.Context;
import android.os.Handler;

import java.util.List;

import udesk.core.model.AgentInfo;
import udesk.core.model.MessageInfo;

public interface IChatActivityView {
	
	Context getContext();
	
	CharSequence getInputContent();
	
	void showFailToast(String failMsg);
	
	void dealAgentInfo(AgentInfo  agentInfo);
	
	void clearInputContent();
	
	void addMessage(MessageInfo message);
	
	AgentInfo getAgentInfo();
	void setAgentInfo(AgentInfo agentInfo);

	Handler getHandler();
	
	void refreshInputEmjio(String s) ;
	
	List<String> getEmotionStringList();
	
	void onRecordSuccess(String filePath , long duration);
	
	void dealRedirectAgentInfo(AgentInfo  agentInfo);

	String getGroupId();
	String getAgentId();
	void changgeiSSurvyOperate();
	void setIsPermmitSurvy( boolean isPermmitSurvy);

}
