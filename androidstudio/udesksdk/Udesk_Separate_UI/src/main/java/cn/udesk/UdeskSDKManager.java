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
import java.util.Map;

import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.activity.UdeskHelperActivity;
import cn.udesk.activity.UdeskRobotActivity;
import cn.udesk.widget.UdeskDialog;
import udesk.core.UdeskCallBack;
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
	private Context mContext = null;
	private UdeskSDKManager(Context context) {
		this.mContext = context;
	}

	public static UdeskSDKManager getInstance(Context context) {
		if (instance == null) {
			synchronized (UdeskSDKManager.class) {
				if (instance == null) {
					instance = new UdeskSDKManager(context);
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
	
	public String getSdkToken() {
		if(!TextUtils.isEmpty(sdkToken)){
			return sdkToken;
		}
		return PreferenceHelper.readString(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_SdkToken);
	}

	public String getDomain() {
		if(!TextUtils.isEmpty(domain)){
			return domain;
		}
		return PreferenceHelper.readString(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_Domain);
	}
	

	public String getSecretKey() {
		if(!TextUtils.isEmpty(secretKey)){
			return secretKey;
		}
		return PreferenceHelper.readString(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_SecretKey);
	}
	
	

	public String getUserId() {
		if(!TextUtils.isEmpty(userId)){
			return userId;
		}
		return PreferenceHelper.readString(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_userid);
	}
	
	

	public String getTransfer() {
		
		if(!TextUtils.isEmpty(transfer)){
			return transfer;
		}
		return PreferenceHelper.readString(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_Transfer);
	}

	public String getH5Url() {
		if(!TextUtils.isEmpty(h5Url)){
			return h5Url;
		}
		return PreferenceHelper.readString(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_h5url);
	}

	private void showLoading() {
		dialog = new UdeskDialog(mContext, R.style.udesk_dialog);
		dialog.show();
	}

	private void dismiss() {
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	public void initApiKey(String domain, String secretKey){
		this.domain = domain;
		this.secretKey = secretKey;
		PreferenceHelper.write(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
				UdeskConst.SharePreParams.Udesk_Domain, domain);
		PreferenceHelper.write(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
				UdeskConst.SharePreParams.Udesk_SecretKey, secretKey);
	}
	public void toLanuchChatAcitvity(){
		Intent intent = new Intent(mContext, UdeskChatActivity.class);
		mContext.startActivity(intent);
	}
	public void toLanuchRobotAcitivty(String url,String tranfer){
		Intent intent = new Intent(mContext, UdeskRobotActivity.class);
		intent.putExtra(UdeskConst.UDESKTRANSFER, tranfer);
		intent.putExtra(UdeskConst.UDESKHTMLURL, url);
		mContext.startActivity(intent);
	}
	public void toLanuchHelperAcitivty(){
		Intent intent = new Intent(mContext, UdeskHelperActivity.class);
		mContext.startActivity(intent);
	}
	
	public void showRobot() {
		if(!TextUtils.isEmpty(getH5Url())){
			toLanuchRobotAcitivty(getH5Url(), getTransfer());
			return;
		}
		
		showLoading();
		UdeskHttpFacade.getInstance().getRobotJsonApi(getDomain(),getSecretKey(),new UdeskCallBack() {

			@Override
			public void onSuccess(String message) {
				dismiss();
				RobotInfo item = JsonUtils.parseRobotJsonResult(message);
				if (item != null && !TextUtils.isEmpty(item.h5_url)) {
					toLanuchRobotAcitivty(item.h5_url, item.transfer);
					PreferenceHelper.write(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
							UdeskConst.SharePreParams.Udesk_Transfer, item.transfer);
					PreferenceHelper.write(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
							UdeskConst.SharePreParams.Udesk_h5url, item.h5_url);
				} else {
					UdeskUtils.showToast(mContext, mContext.getString(R.string.udesk_has_not_open_robot));
				}
			}

			@Override
			public void onFail(String message) {
				dismiss();
				UdeskUtils.showToast(mContext, message);
			}
		});
	}
	
	public void showRobotOrConversation() {
		if(!TextUtils.isEmpty(getH5Url())){
			toLanuchRobotAcitivty(getH5Url(), getTransfer());
			return;
		}
		showLoading();
		UdeskHttpFacade.getInstance().getRobotJsonApi(getDomain(),getSecretKey(),new UdeskCallBack() {
			
			@Override
			public void onSuccess(String message) {
				dismiss();
				RobotInfo item = JsonUtils.parseRobotJsonResult(message);
				if (item != null && !TextUtils.isEmpty(item.h5_url)) {
					toLanuchRobotAcitivty(item.h5_url, item.transfer);
					PreferenceHelper.write(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
							UdeskConst.SharePreParams.Udesk_Transfer, item.transfer);
					PreferenceHelper.write(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
							UdeskConst.SharePreParams.Udesk_h5url, item.h5_url);
				} else {
					toLanuchChatAcitvity();
				}
			}
			
			@Override
			public void onFail(String message) {
				dismiss();
				toLanuchChatAcitvity();
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
	
	public void setUserInfo(String sdkToken,Map<String, String> info){
		this.setUserInfo(sdkToken, info, null);
	}
	
	public void setUserInfo(String sdkToken,Map<String, String> info,Map<String, String> textField){
		this.setUserInfo(sdkToken, info, textField, null);
	}
	
	public void setUserInfo(String sdkToken,Map<String, String> info,Map<String, String> textField,Map<String, String> roplist){
		this.sdkToken = sdkToken;
		PreferenceHelper.write(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
				UdeskConst.SharePreParams.Udesk_SdkToken, sdkToken);
		this.userinfo = info;
		this.textField = textField;
		this.roplist = roplist;
		UdeskHttpFacade.getInstance().setUserInfo(getDomain(),getSecretKey(),sdkToken, info, textField, roplist, new UdeskCallBack() {
			
			@Override
			public void onSuccess(String string) {
				parserCustomersJson(string);
			}
			
			@Override
			public void onFail(String string) {
				UdeskUtils.showToast(mContext, string);
			}
		});
	}
	
	public void  parserCustomersJson(String jsonString){
		try {
			JSONObject resultJson = new JSONObject(jsonString);
			if(resultJson.has("customer")){
				JSONObject customerJson = resultJson.getJSONObject("customer");
				if(customerJson.has("id")){
					this.userId = customerJson.getString("id");
					PreferenceHelper.write(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
							UdeskConst.SharePreParams.Udesk_userid, userId);
				}
			}
			if(resultJson.has("robot")){
				String robotString = resultJson.getString("robot");
				if(!TextUtils.isEmpty(robotString)){
					JSONObject robotJson = new JSONObject(robotString);
					if(robotJson.has("transfer")){
						this.transfer = robotJson.getString("transfer");
						PreferenceHelper.write(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
								UdeskConst.SharePreParams.Udesk_Transfer, transfer);
					}
					if(robotJson.has("h5_url")){
						this.h5Url = robotJson.getString("h5_url");
						PreferenceHelper.write(mContext, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
								UdeskConst.SharePreParams.Udesk_h5url, h5Url);
					}
				}
			}
		} catch (JSONException e) {
		}
	}
	

}
