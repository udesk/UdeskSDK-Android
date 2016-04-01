package cn.udesk;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import cn.udesk.activity.OptionsAgentGroupActivity;
import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.activity.UdeskHelperActivity;
import cn.udesk.activity.UdeskRobotActivity;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.widget.UdeskDialog;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskCoreConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.UdeskLogUtil;
import udesk.core.model.RobotInfo;
import udesk.core.utils.UdeskUtils;


public class UdeskSDKManager {
	

	
	/**
	 * 用户唯一的标识
	 */
	private String sdkToken = null;
	/**
	 * 用户的基本信息
	 */
	private Map<String, String> userinfo = null;
	/**
	 * 用户自定义字段文本信息
	 */
	private Map<String, String> textField = null;
	
	/**
	 * 用户自定义字段的列表信息
	 */
	private Map<String, String> roplist = null;
	private String domain = null;
	private String secretKey = null;
	private String userId = null;
	private String transfer = null;
	private String h5Url = null;
	private UdeskDialog dialog;
	private static UdeskSDKManager instance;
	private UdeskSDKManager() {
	}
	public static UdeskSDKManager getInstance() {
		if (instance == null) {
			synchronized (UdeskSDKManager.class) {
				if (instance == null) {
					instance = new UdeskSDKManager();
				}
			}
		}
		return instance;
	}

	/**
	 *
	 * @param context
	 * @param domain
	 * @param secretKey
	 */
	public void initApiKey(Context context,String domain, String secretKey){
		this.domain = domain;
		this.secretKey = secretKey;
		isShowLog(false);
		PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
				UdeskConst.SharePreParams.Udesk_Domain, domain);
		PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
				UdeskConst.SharePreParams.Udesk_SecretKey, secretKey);
		UdeskUtil.initImageLoaderConfig(context);
	}

	public void setUserInfo(Context context,String sdkToken,Map<String, String> info){
		this.setUserInfo(context, sdkToken, info, null);
	}

	public void setUserInfo(Context context,String sdkToken,Map<String, String> info,Map<String, String> textField){
		this.setUserInfo(context, sdkToken, info, textField, null);
	}

	public void setUserInfo(final  Context context,String token,Map<String, String> info,Map<String, String> textField,Map<String, String> roplist){
		clean();
		this.sdkToken = token;
		initDB(context,sdkToken);
		PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
				UdeskConst.SharePreParams.Udesk_SdkToken, sdkToken);
		if(info == null){
			info = new HashMap<String,String>();
		}
		this.userinfo = info;
		userinfo.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, sdkToken);
		this.textField = textField;
		this.roplist = roplist;
	}


	public void toLanuchChatAcitvity(Context context){
		Intent intent = new Intent(context, UdeskChatActivity.class);
		context.startActivity(intent);
	}


	public void toLanuchHelperAcitivty(Context context){
		Intent intent = new Intent(context, UdeskHelperActivity.class);
		context.startActivity(intent);
	}

	public void showRobot(final Context context) {
		if(!TextUtils.isEmpty(getH5Url(context))){
			toLanuchRobotAcitivty(context,getH5Url(context), getTransfer(context));
			return;
		}

		showLoading(context);
		UdeskHttpFacade.getInstance().getRobotJsonApi(getDomain(context), getSecretKey(context), new UdeskCallBack() {

			@Override
			public void onSuccess(String message) {
				dismiss();
				RobotInfo item = JsonUtils.parseRobotJsonResult(message);
				if (item != null && !TextUtils.isEmpty(item.h5_url)) {
					PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
							UdeskConst.SharePreParams.Udesk_Transfer, item.transfer);
					PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
							UdeskConst.SharePreParams.Udesk_h5url, item.h5_url);
					toLanuchRobotAcitivty(context, item.h5_url, item.transfer);
				} else {
					UdeskUtils.showToast(context, context.getString(R.string.udesk_has_not_open_robot));
				}
			}

			@Override
			public void onFail(String message) {
				dismiss();
				UdeskUtils.showToast(context, message);
			}
		});
	}

	public void showRobotOrConversation(final Context context) {
		if(!TextUtils.isEmpty(getH5Url(context))){
			toLanuchRobotAcitivty(context,getH5Url(context), getTransfer(context));
			return;
		}
		showLoading(context);
		UdeskHttpFacade.getInstance().getRobotJsonApi(getDomain(context), getSecretKey(context), new UdeskCallBack() {

			@Override
			public void onSuccess(String message) {
				dismiss();
				RobotInfo item = JsonUtils.parseRobotJsonResult(message);
				if (item != null && !TextUtils.isEmpty(item.h5_url)) {
					toLanuchRobotAcitivty(context, item.h5_url, item.transfer);
					PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
							UdeskConst.SharePreParams.Udesk_Transfer, item.transfer);
					PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
							UdeskConst.SharePreParams.Udesk_h5url, item.h5_url);
				} else {
					toLanuchChatAcitvity(context);
				}
			}

			@Override
			public void onFail(String message) {
				dismiss();
				toLanuchChatAcitvity(context);
			}
		});
	}

	/**
	 * 指引客户选择客服组
	 * @param context
	 */
	public void showConversationByImGroup( Context context) {
		Intent intent = new Intent(context, OptionsAgentGroupActivity.class);
		context.startActivity(intent);
	}


	/**
	 * 指定客服 id 进行分配 进行会话
	 * @param context
	 * @param agentId  客服ID
	 */
	public void lanuchChatByAgentId(Context context,String agentId){
		lanuchChatByConfirmId(context, "", agentId);
	}

	/**
	 *
	 * @param context
	 * @param groupId 指定客服组 id 进行分配
	 */
	public void lanuchChatByGroupId(Context context, String groupId){
		lanuchChatByConfirmId(context, groupId, "");
	}

	/**
	 * 控制控制台日志的开关
	 * @param isShow
	 */
	public void isShowLog(boolean isShow){
		UdeskLogUtil.DEBUG = isShow;
		UdeskCoreConst.xmppDebug =isShow;
		UdeskCoreConst.isDebug = isShow;
	}

	/**
	 * 删除聊天数据
	 */
	public void deleteMsg(){
		UdeskDBManager.getInstance().deleteAllMsg();
	}

	public Map<String, String> getUserinfo() {
		return userinfo;
	}

	public Map<String, String> getTextField() {
		return textField;
	}

	public Map<String, String> getRoplist() {
		return roplist;
	}
	
	public String getSdkToken(Context context) {
		if(!TextUtils.isEmpty(sdkToken)){
			return sdkToken;
		}
		return PreferenceHelper.readString(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_SdkToken);
	}

	public String getDomain(Context context) {
		if(!TextUtils.isEmpty(domain)){
			return domain;
		}
		return PreferenceHelper.readString(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_Domain);
	}

	public String getSecretKey(Context context) {
		if(!TextUtils.isEmpty(secretKey)){
			return secretKey;
		}
		return PreferenceHelper.readString(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_SecretKey);
	}
	
	public String getUserId(Context context) {
		if(!TextUtils.isEmpty(userId)){
			return userId;
		}
		return "";
	}
	
	public String getTransfer(Context context) {
		
		if(!TextUtils.isEmpty(transfer)){
			return transfer;
		}
		return PreferenceHelper.readString(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_Transfer);
	}

	public String getH5Url(Context context) {
		if(!TextUtils.isEmpty(h5Url)){
			return h5Url;
		}
		return PreferenceHelper.readString(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_h5url);
	}

	public void setH5Url(String h5Url) {
		this.h5Url = h5Url;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setTransfer(String transfer) {
		this.transfer = transfer;
	}

	private void showLoading(Context context) {
		try {
			dialog = new UdeskDialog(context, R.style.udesk_dialog);
			dialog.show();
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	private void toLanuchRobotAcitivty(Context context,String url,String tranfer){
		Intent intent = new Intent(context, UdeskRobotActivity.class);
		intent.putExtra(UdeskConst.UDESKTRANSFER, tranfer);
		intent.putExtra(UdeskConst.UDESKHTMLURL, url);
		context.startActivity(intent);
	}

	private void dismiss() {
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	private void lanuchChatByConfirmId(Context context,String groupId,String agentId){
		Intent intent = new Intent(context, UdeskChatActivity.class);
		intent.putExtra(UdeskConst.UDESKGROUPID,groupId);
		intent.putExtra(UdeskConst.UDESKAGENTID, agentId);
		context.startActivity(intent);
	}

	/**
	 * 初始话DB
	 * @param context
	 * @param sdkToken
	 */
	private void initDB(Context context , String sdkToken) {
		releaseDB();
		UdeskDBManager.getInstance().init(context, sdkToken);
	}

	/**
	 * 销毁DB
	 */
	public void releaseDB(){
		UdeskDBManager.getInstance().release();
	}



	private void clean(){
		this.userId = null;
		this.transfer = null;
		this.h5Url = null;
		this.sdkToken = null;
		this.userinfo = null;
		this.textField = null;
		this. roplist = null;
	}

}
