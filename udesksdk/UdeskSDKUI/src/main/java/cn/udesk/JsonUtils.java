package cn.udesk;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.model.AgentGroupNode;
import cn.udesk.model.OptionsModel;
import cn.udesk.model.SurveyOptionsModel;
import udesk.core.model.AgentInfo;
import udesk.core.model.RobotInfo;
import udesk.core.model.UDHelperArticleContentItem;
import udesk.core.model.UDHelperItem;

public class JsonUtils {

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

	public static RobotInfo parseRobotJsonResult(String jsonString) {
		RobotInfo item = null;
		try {
			JSONObject resultJson = new JSONObject(jsonString);
			if (resultJson.has("robot")) {
				String robotString = resultJson.getString("robot");
				if (!TextUtils.isEmpty(robotString)) {
					JSONObject robotJson = new JSONObject(robotString);
					item = new RobotInfo();
					if (robotJson.has("transfer")) {
						item.transfer = robotJson.getString("transfer");
					}
					if (robotJson.has("h5_url")) {
						item.h5_url = robotJson.getString("h5_url");
					}
				}
			}

		} catch (Exception e) {

		}
		return item;
	}

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
//			agentInfo.message = "当前没有客服在线";
			agentInfo.setMessage("当前没有客服在线");
		}
		return agentInfo;

	}
	
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


	public static String  parserCustomersJson(Context context,String jsonString){
		String robotUrl = "";
		try {
			JSONObject resultJson = new JSONObject(jsonString);
			if(resultJson.has("customer")){
				JSONObject customerJson = resultJson.getJSONObject("customer");
				if(customerJson.has("id")){
					UdeskSDKManager.getInstance().setUserId( customerJson.getString("id"));
				}
//				if(customerJson.has("is_blocked")){
//					UdeskSDKManager.getInstance().setIsBolcked(customerJson.getString("is_blocked"));
//				}
			}
			if(resultJson.has("robot")){
				String robotString = resultJson.getString("robot");
				if(!TextUtils.isEmpty(robotString)){
					JSONObject robotJson = new JSONObject(robotString);
					if(robotJson.has("transfer")){
						UdeskSDKManager.getInstance().setTransfer(robotJson.getString("transfer"));
//						PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
//								UdeskConst.SharePreParams.Udesk_Transfer, robotJson.getString("transfer"));
					}
					if(robotJson.has("h5_url")){
						UdeskSDKManager.getInstance().setH5Url(robotJson.getString("h5_url"));
//						PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
//								UdeskConst.SharePreParams.Udesk_h5url, robotJson.getString("h5_url"));
						return robotJson.getString("h5_url");
					}
				}
			}
		} catch (JSONException e) {
		}

		return  robotUrl;
	}

}
