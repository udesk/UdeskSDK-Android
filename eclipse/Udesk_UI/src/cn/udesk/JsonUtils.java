package cn.udesk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import udesk.core.model.AgentInfo;
import udesk.core.model.RobotInfo;
import udesk.core.model.UDHelperArticleContentItem;
import udesk.core.model.UDHelperItem;
import android.text.TextUtils;

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
			JSONObject result = json.getJSONObject("result");
			if (result.has("code")) {
				agentInfo.agentCode = result.getInt("code");
			}
			if (result.has("message")) {
				agentInfo.message = result.getString("message");
			}

			if (result.has("agent")) {
				JSONObject agentJson = result.getJSONObject("agent");

				if (agentJson.has("nick")) {
					agentInfo.agentNick = agentJson.getString("nick");
				}
				if (agentJson.has("jid")) {
					agentInfo.agentJid = agentJson.getString("jid");
				}
				if (agentJson.has("agent_id")) {
					agentInfo.agent_id = agentJson.getString("agent_id");
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
			agentInfo.message = "当前没有客服在线";
		}
		return agentInfo;

	}


}
