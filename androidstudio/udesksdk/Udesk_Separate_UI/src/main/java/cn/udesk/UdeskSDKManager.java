package cn.udesk;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.activity.UdeskHelperActivity;
import cn.udesk.activity.UdeskRobotActivity;
import cn.udesk.widget.UdeskDialog;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskCoreConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.model.RobotInfo;
import udesk.core.utils.UdeskUtils;

public class UdeskSDKManager {

	private String domain = null;
	private String secretKey = null;
	private String userId = null;
	private String transfer = null;
	private String h5Url = null;

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
	private UdeskDialog dialog;

	private volatile static UdeskSDKManager instance;
	//	private Context mContext = null;
	private UdeskSDKManager() {
//		this.mContext = context;
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
		return PreferenceHelper.readString(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_userid);
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

	private void showLoading(Context context) {
		try {
			dialog = new UdeskDialog(context, R.style.udesk_dialog);
			dialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void dismiss() {
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	public void initApiKey(Context context,String domain, String secretKey){
		this.domain = domain;
		this.secretKey = secretKey;
		PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
				UdeskConst.SharePreParams.Udesk_Domain, domain);
		PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
				UdeskConst.SharePreParams.Udesk_SecretKey, secretKey);
	}
	public void toLanuchChatAcitvity(Context context){
		Intent intent = new Intent(context, UdeskChatActivity.class);
		context.startActivity(intent);
	}
	public void toLanuchRobotAcitivty(Context context,String url,String tranfer){
		Intent intent = new Intent(context, UdeskRobotActivity.class);
		intent.putExtra(UdeskConst.UDESKTRANSFER, tranfer);
		intent.putExtra(UdeskConst.UDESKHTMLURL, url);
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
		UdeskHttpFacade.getInstance().getRobotJsonApi(getDomain(context),getSecretKey(context),new UdeskCallBack() {

			@Override
			public void onSuccess(String message) {
				dismiss();
				RobotInfo item = JsonUtils.parseRobotJsonResult(message);
				if (item != null && !TextUtils.isEmpty(item.h5_url)) {
					toLanuchRobotAcitivty(context,item.h5_url, item.transfer);
					PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
							UdeskConst.SharePreParams.Udesk_Transfer, item.transfer);
					PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
							UdeskConst.SharePreParams.Udesk_h5url, item.h5_url);
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
		UdeskHttpFacade.getInstance().getRobotJsonApi(getDomain(context),getSecretKey(context),new UdeskCallBack() {

			@Override
			public void onSuccess(String message) {
				dismiss();
				RobotInfo item = JsonUtils.parseRobotJsonResult(message);
				if (item != null && !TextUtils.isEmpty(item.h5_url)) {
					toLanuchRobotAcitivty(context,item.h5_url, item.transfer);
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

	public void initImageLoaderConfig(Context context){
		File cacheDir = StorageUtils.getOwnCacheDirectory(
				context, "udesksdk/img/cache");
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context)
				.threadPoolSize(3)
						// 线程池内加载的数量
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new WeakMemoryCache())
				.memoryCacheSize(2 * 1024 * 1024)
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.discCacheFileCount(100) // 缓存的文件数量
				.discCache(new UnlimitedDiskCache(cacheDir))// 自定义缓存路径
				.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
				.imageDownloader(
						new BaseImageDownloader(context,
								5 * 1000, 30 * 1000))
				.build();// 开始构建
		ImageLoader.getInstance().init(config);
	}

	public void setUserInfo(Context context,String sdkToken,Map<String, String> info){
		this.setUserInfo(context,sdkToken, info, null);
	}

	public void setUserInfo(Context context,String sdkToken,Map<String, String> info,Map<String, String> textField){
		this.setUserInfo(context,sdkToken, info, textField, null);
	}

	public void setUserInfo(final Context context,String sdkToken,Map<String, String> info,Map<String, String> textField,Map<String, String> roplist){
		this.sdkToken = sdkToken;
		UdeskConst.SharePreParams.Udesk_Sharepre_Name = sdkToken;
		PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
				UdeskConst.SharePreParams.Udesk_SdkToken, sdkToken);
		if(info == null){
			info = new HashMap<String, String>();
		}
		info.put(UdeskCoreConst.UdeskUserInfo.USER_SDK_TOKEN,sdkToken);
		this.userinfo = info;
		this.textField = textField;
		this.roplist = roplist;
		UdeskHttpFacade.getInstance().setUserInfo(getDomain(context),getSecretKey(context),sdkToken, info, textField, roplist, new UdeskCallBack() {

			@Override
			public void onSuccess(String string) {
				parserCustomersJson(context,string);
			}

			@Override
			public void onFail(String string) {
				UdeskUtils.showToast(context, string);
			}
		});
	}

	public void  parserCustomersJson(Context context,String jsonString){
		try {
			JSONObject resultJson = new JSONObject(jsonString);
			if(resultJson.has("customer")){
				JSONObject customerJson = resultJson.getJSONObject("customer");
				if(customerJson.has("id")){
					this.userId = customerJson.getString("id");
					PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
							UdeskConst.SharePreParams.Udesk_userid, userId);
				}
			}
			if(resultJson.has("robot")){
				String robotString = resultJson.getString("robot");
				if(!TextUtils.isEmpty(robotString)){
					JSONObject robotJson = new JSONObject(robotString);
					if(robotJson.has("transfer")){
						this.transfer = robotJson.getString("transfer");
						PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
								UdeskConst.SharePreParams.Udesk_Transfer, transfer);
					}
					if(robotJson.has("h5_url")){
						this.h5Url = robotJson.getString("h5_url");
						PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
								UdeskConst.SharePreParams.Udesk_h5url, h5Url);
					}
				}
			}
		} catch (JSONException e) {
		}
	}

}
