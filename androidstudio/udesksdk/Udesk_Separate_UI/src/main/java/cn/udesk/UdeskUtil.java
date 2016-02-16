package cn.udesk;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import udesk.core.model.MessageInfo;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

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



}
