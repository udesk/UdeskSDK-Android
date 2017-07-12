package cn.udesk;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.model.AgentGroupNode;
import cn.udesk.model.OptionsModel;
import cn.udesk.model.SDKIMSetting;
import cn.udesk.model.StructModel;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.model.TicketReplieMode;
import udesk.core.model.AgentInfo;
import udesk.core.model.UDHelperArticleContentItem;
import udesk.core.model.UDHelperItem;

public class JsonUtils {

	//解析帮助中心接口  请求获取到文章列表字符串
	public static List<UDHelperItem> parseListArticlesResult(String result) {
		List<UDHelperItem> mList = new ArrayList<UDHelperItem>();
		try {
			JSONObject json = new JSONObject(result);
			if (json.has("contents")) {
				JSONArray kownlegeArray = json.optJSONArray("contents");
				if (kownlegeArray != null && kownlegeArray.length() > 0) {
					for (int i = 0; i < kownlegeArray.length(); i++) {
						JSONObject data = kownlegeArray.optJSONObject(i);
						UDHelperItem helpItem = new UDHelperItem();
						helpItem.id = data.optInt("id");
						helpItem.subject = data.optString("subject");
						mList.add(helpItem);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return mList;
	}

	//解析帮助中心文章内容
	public static UDHelperArticleContentItem parseArticleContentItem(
			String result) {
		UDHelperArticleContentItem item = null;
		try {
			JSONObject json = new JSONObject(result);
			if (json.has("contents")) {
				JSONObject contents = json.optJSONObject("contents");
				if (contents != null) {
					item = new UDHelperArticleContentItem();
					item.subject = contents.optString("subject");
					item.content = contents.optString("content");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return item;
	}

	//解析请求到客服的信息
	public static AgentInfo parseAgentResult(String response) {
		AgentInfo agentInfo = new AgentInfo();
		if (TextUtils.isEmpty(response)) {
			return agentInfo;
		}
		try {
			JSONObject json = new JSONObject(response);
			if(json.has("result")){
				JSONObject result = json.getJSONObject("result");
				if (result.has("code")) {
					agentInfo.setAgentCode(result.getInt("code"));
				}
				if (result.has("message")) {
					agentInfo.setMessage( result.getString("message"));
				}
				if (result.has("im_sub_session_id")){
					agentInfo.setIm_sub_session_id(result.getString("im_sub_session_id"));
				}
				if (result.has("agent")) {
					JSONObject agentJson = result.getJSONObject("agent");

					if (agentJson.has("nick")) {
						agentInfo.setAgentNick(agentJson.getString("nick"));
					}
					if (agentJson.has("jid")) {
						agentInfo.setAgentJid(agentJson.getString("jid"));
						UdeskBaseInfo.sendMsgTo = agentJson.getString("jid");
					}
					if (agentJson.has("agent_id")) {
						agentInfo.setAgent_id(agentJson.getString("agent_id"));
					}
					if (agentJson.has("avatar")){
						agentInfo.setHeadUrl(agentJson.getString("avatar"));
					}
				}
			}else{
				if (json.has("code")) {
					agentInfo.setAgentCode(json.getInt("code"));
				}
				if (json.has("message")) {
					agentInfo.setMessage( json.getString("message"));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			agentInfo.setMessage("当前没有客服在线");
		}
		return agentInfo;

	}

	//解析设置的满意度调查选项
	public static SurveyOptionsModel parseSurveyOptions(String response){
		
		SurveyOptionsModel optionsMode = new SurveyOptionsModel();
		if (TextUtils.isEmpty(response)) {
			return optionsMode;
		}
		
		try {
			JSONObject json = new JSONObject(response);
			JSONObject result = json.getJSONObject("result");
			if (result.has("title")) {
				optionsMode.setTitle(result.getString("title"));
			}
			if (result.has("desc")) {
				optionsMode.setDesc(result.getString("desc"));
			}
			if (result.has("options")) {
				
				List<OptionsModel> options = new ArrayList<OptionsModel>();
				JSONArray optionsArray = result.optJSONArray("options");
				if (optionsArray != null && optionsArray.length() > 0) {
					for (int i = 0; i < optionsArray.length(); i++) {
						JSONObject data = optionsArray.optJSONObject(i);
						OptionsModel optionItem = new OptionsModel();
						optionItem.setId(data.optString("id"));
						optionItem.setText(data.getString("text"));
						options.add(optionItem);
					}
				}
				optionsMode.setOptions(options);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return optionsMode;
		
	}


	//解析请求导航页选项返回的结果
	public static List<AgentGroupNode> parseIMGroup(String response){

		List<AgentGroupNode> groupsModel = null;
		if (TextUtils.isEmpty(response)) {
			return groupsModel;
		}

		try {
			JSONObject json = new JSONObject(response);
			if (json.has("result")) {
				groupsModel = new ArrayList<AgentGroupNode>();
				JSONArray optionsArray = json.optJSONArray("result");
				if (optionsArray != null && optionsArray.length() > 0) {
					for (int i = 0; i < optionsArray.length(); i++) {
						JSONObject data = optionsArray.optJSONObject(i);
						AgentGroupNode model = new AgentGroupNode();
						model.setId(data.optString("id"));
						model.setGroup_id(data.optString("group_id"));
						model.setHas_next(data.optString("has_next"));
						model.setItem_name(data.optString("item_name"));
						model.setLink(data.optString("link"));
						model.setParentId(data.optString("parentId"));
						groupsModel.add(model);
					}
				}

			}
		} catch (JSONException e) {
			return null;
		}
		return groupsModel;

	}


	public static void  parserCustomersJson(String jsonString){
		try {
			JSONObject resultJson = new JSONObject(jsonString);
			if(resultJson.has("customer")){
				JSONObject customerJson = resultJson.getJSONObject("customer");
				if(customerJson.has("id")){
					UdeskBaseInfo.customerId = customerJson.getString("id");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	//解析获取配置选项

	public static SDKIMSetting  parserIMSettingJson(String jsonString){
		SDKIMSetting sdkimSetting = new SDKIMSetting();
		try {
			JSONObject rootJson = new JSONObject(jsonString);
			if (rootJson != null){
				if (rootJson.has("code")){
					sdkimSetting.setCode(rootJson.getInt("code"));
				}
				if (rootJson.has("message")){
					sdkimSetting.setMessage(rootJson.get("message"));
				}
				if (rootJson.has("result")){
					JSONObject resultJson = new JSONObject(rootJson.getString("result"));
					if (resultJson != null){
						if (resultJson.has("enable_im_group")){
							sdkimSetting.setEnable_im_group(resultJson.get("enable_im_group"));
						}
						if (resultJson.has("in_session")){
							sdkimSetting.setIn_session(resultJson.get("in_session"));
						}
						if (resultJson.has("is_worktime")){
							sdkimSetting.setIs_worktime(resultJson.get("is_worktime"));
						}
						if (resultJson.has("has_robot")){
							sdkimSetting.setHas_robot(resultJson.get("has_robot"));
						}
						if (resultJson.has("enable_robot")){
							sdkimSetting.setEnable_robot(resultJson.get("enable_robot"));
						}
						if (resultJson.has("enable_sdk_robot")){
							sdkimSetting.setEnable_sdk_robot(resultJson.get("enable_sdk_robot"));
						}
						if (resultJson.has("enable_agent")){
							sdkimSetting.setEnable_agent(resultJson.get("enable_agent"));
						}
						if (resultJson.has("enable_web_im_feedback")){
							sdkimSetting.setEnable_web_im_feedback(resultJson.get("enable_web_im_feedback"));
						}
						if (resultJson.has("no_reply_hint")){
							sdkimSetting.setNo_reply_hint(resultJson.get("no_reply_hint"));
						}
						if (resultJson.has("investigation_when_leave")){
							sdkimSetting.setInvestigation_when_leave(resultJson.get("investigation_when_leave"));
						}
						if(resultJson.has("leave_message_type")){
							sdkimSetting.setLeave_message_type(resultJson.getString("leave_message_type"));
						}
						if (resultJson.has("enable_im_survey")){
							sdkimSetting.setEnable_im_survey(resultJson.get("enable_im_survey"));
						}
						if(resultJson.has("robot")){
							sdkimSetting.setRobot(resultJson.get("robot"));
						}
					}
				}
			}


		} catch (Exception e) {
		}

		return  sdkimSetting;
	}

	public static StructModel parserStructMsg(String jsonString){
		StructModel structModel = new StructModel();
		try {
			JSONObject rootJson = new JSONObject(jsonString);
			if (rootJson.has("title")){
				structModel.setTitle(rootJson.getString("title"));
			}
			if (rootJson.has("description")){
				structModel.setDescription(rootJson.getString("description"));
			}
			if (rootJson.has("img_url")){
				structModel.setImg_url(rootJson.getString("img_url"));
			}

			if (rootJson.has("buttons")){
				JSONArray btnArray = rootJson.optJSONArray("buttons");
				List<StructModel.ButtonsBean> structBtns = new ArrayList<StructModel.ButtonsBean>();
				if (btnArray != null && btnArray.length() > 0) {
					for (int i = 0; i < btnArray.length(); i++) {
						JSONObject data = btnArray.optJSONObject(i);
						StructModel.ButtonsBean  buttonsBean = new StructModel.ButtonsBean();
						if (data.has("type")){
							buttonsBean.setType(data.getString("type"));
						}
						if (data.has("text")){
							buttonsBean.setText(data.getString("text"));
						}
						if (data.has("value")){
							buttonsBean.setValue(data.getString("value"));
						}
						structBtns.add(buttonsBean);
					}
				}
				structModel.setButtons(structBtns);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return  structModel;
	}

	public  static TicketReplieMode parserTicketReplie(String jsonString){
		TicketReplieMode replieMode = new TicketReplieMode();
		try {
			JSONObject rootJson = new JSONObject(jsonString);


			if (rootJson.has("size")){
				replieMode.setSize(rootJson.getInt("size"));
			}
			if (rootJson.has("total")){
				replieMode.setTotal(rootJson.getInt("total"));
			}
			if (rootJson.has("total_pages")){
				replieMode.setTotal_pages(rootJson.getInt("total_pages"));
			}

			if (rootJson.has("contents")){
				JSONArray contentsArray = rootJson.optJSONArray("contents");
				List<TicketReplieMode.ContentsBean> contents = new ArrayList<TicketReplieMode.ContentsBean >();
				if (contentsArray != null && contentsArray.length() > 0){
					for (int i = 0; i < contentsArray.length(); i++) {
						JSONObject data = contentsArray.optJSONObject(i);
						TicketReplieMode.ContentsBean  contentsBean = new TicketReplieMode.ContentsBean();
						if (data.has("reply_id")){
							contentsBean.setReply_id(data.get("reply_id"));
						}
						if (data.has("user_avatar")){
							contentsBean.setUser_avatar(data.get("user_avatar"));
						}
						if (data.has("reply_content")){
							contentsBean.setReply_content(data.get("reply_content"));
						}
						if (data.has("reply_content_type")){
							contentsBean.setReply_content_type(data.get("reply_content_type"));
						}
						if (data.has("reply_type")){
							contentsBean.setReply_type(data.get("reply_type"));
						}
						if (data.has("reply_user")){
							contentsBean.setReply_user(data.get("reply_user"));
						}
						if (data.has("reply_created_at")){
							contentsBean.setReply_created_at(data.get("reply_created_at"));
						}
						if (data.has("reply_updated_at")){
							contentsBean.setReply_updated_at(data.get("reply_updated_at"));
						}
						contents.add(contentsBean);
					}
				}
				replieMode.setContents(contents);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return  replieMode;
	}



}
