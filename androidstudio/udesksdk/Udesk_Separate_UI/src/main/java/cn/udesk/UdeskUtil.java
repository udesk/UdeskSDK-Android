package cn.udesk;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import udesk.core.UdeskCoreConst;
import udesk.core.model.MessageInfo;

public class UdeskUtil {
	public static final String  ImgFolderName = "UDeskIMg";
	public static final String  AudioFolderName = "UDeskAudio";
	public static final String  SaveImg = "saveImg";
	
    public static Uri getOutputMediaFileUri() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return Uri.fromFile(getOutputMediaFile("IMG_" + timeStamp + ".jpg"));
    }

    public static File getOutputMediaFile(String mediaName) {
        File mediaStorageDir = null;
        try {
            mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ImgFolderName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + mediaName);
        return mediaFile;
    }
    
    public static boolean isExitFile(String path) {
		File file = new File(path);
		if (file.exists()) {
			return true;
		}
		return false;
	}
    
    
	public static boolean audiofileIsDown(String url) {
		if (TextUtils.isEmpty(url)) {
			return false;
		}
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES),
						AudioFolderName);
		if (!mediaStorageDir.exists()) {
			return false;
		}
		String filepath = mediaStorageDir.getPath() + File.separator + fileName;
		File file = new File(filepath);
		if (!file.exists()) {
			return false;
		} else {
			return true;
		}
	}
	
	
	public static String getDownAudioPath(String url) {
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES),
						AudioFolderName);

		return mediaStorageDir.getPath() + File.separator + fileName;
	}
	
	
	public static String getOutputAudioPath() {
		return getOutputAudioPath("audio_"
				+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
	}

	public static File getOutputAudioFile() {
		return getOutputAudioFile("audio_"
				+ new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
	}

	public static File getOutputAudioFile(String mediaName) {
		String path = getOutputAudioPath(mediaName);
		if (TextUtils.isEmpty(path)) {
			return null;
		} else {
			return new File(path);
		}
	}
	
	public static String getOutputAudioPath(String mediaName) {
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES),
						AudioFolderName);

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}

		File noMediaFile = new File(mediaStorageDir, ".nomedia");
		if (!noMediaFile.exists()) {
			try {
				noMediaFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return mediaStorageDir.getPath() + File.separator + mediaName;
	}


	public static File getAudioFile(String url) {
		String fileName = url.substring(url.lastIndexOf("/") + 1);
		String path = getOutputAudioPath(fileName);
		if (TextUtils.isEmpty(path)) {
			return null;
		} else {
			return new File(path);
		}
	}
	
	
	public static String getSaveImgPath(String url) {
//		String fileName = url.substring(url.lastIndexOf("/") + 1);
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES),
						SaveImg);

		return mediaStorageDir.getPath() ;
//				+ File.separator + fileName;
	}
	
	
	public static String buildImageLoaderImgUrl(MessageInfo message){
		
		if(!TextUtils.isEmpty(message.getLocalPath()) && isExitFile(message.getLocalPath())){
			return "file:///" + message.getLocalPath();
		}else{
			return message.getMsgContent();
		}
	}



	public static String getFormUrlPara(Context context){
		StringBuilder builder = new StringBuilder();
		builder.append("?sdk_token=").append(UdeskSDKManager.getInstance().getSdkToken(context))
				.append("&sdk_version=").append(UdeskCoreConst.sdkversion);
		Map<String, String> userinfo = UdeskSDKManager.getInstance().getUserinfo();
		Map<String,String> textField = UdeskSDKManager.getInstance().getTextField();
		if(userinfo != null && !userinfo.isEmpty()){
			Set<String> keySet = userinfo.keySet();
			for (String key : keySet) {
				if(!TextUtils.isEmpty(userinfo.get(key))){
					if(key.equals("sdk_token")){
						continue;
					}
					if(key.equals(UdeskConst.UdeskUserInfo.NICK_NAME)){
						builder.append("&c_name=").append(userinfo.get(key));
					}else if(key.equals(UdeskConst.UdeskUserInfo.CELLPHONE)){
						builder.append("&c_phone=").append(userinfo.get(key));
					}else if(key.equals(UdeskConst.UdeskUserInfo.EMAIL)){
						builder.append("&c_email=").append(userinfo.get(key));
					}else if(key.equals(UdeskConst.UdeskUserInfo.DESCRIPTION)){
						builder.append("&c_desc=").append(userinfo.get(key));
					}else if(key.equals(UdeskConst.UdeskUserInfo.QQ)){
						builder.append("&c_qq=").append(userinfo.get(key));
					}else if (key.equals(UdeskConst.UdeskUserInfo.WEIXIN_ID)){
						builder.append("&c_wx=").append(userinfo.get(key));
					}else if (key.equals(UdeskConst.UdeskUserInfo.WEIBO_NAME)){
						builder.append("&c_wb=").append(userinfo.get(key));
					}

				}
			}
		}
		if(textField != null && !textField.isEmpty()){
			Set<String> textFieldSet = textField.keySet();
			for (String key : textFieldSet) {
				if(!TextUtils.isEmpty(textField.get(key))){
					builder.append("&c_cf_").append(key).append("=").append(textField.get(key));
				}
			}
		}
		return builder.toString();
	}


	public static void initImageLoaderConfig(Context context){
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

	public static  void initCrashReport(Context context){
		CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
		strategy.setAppVersion(UdeskCoreConst.sdkversion);
		CrashReport.initCrashReport(context, UdeskCoreConst.buglyAppid, false, strategy);
	}

	public static void closeCrashReport(){
		try{
			CrashReport.closeCrashReport();
		}catch (Exception e){

		}

	}

}
