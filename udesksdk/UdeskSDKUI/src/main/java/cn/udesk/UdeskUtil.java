package cn.udesk;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.udesk.activity.UdeskZoomImageActivty;
import cn.udesk.config.UdeskConfig;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.imageloader.UdeskImage;
import cn.udesk.imageloader.UdeskImageLoader;
import cn.udesk.model.ImSetting;
import cn.udesk.model.OptionsModel;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.model.TicketReplieMode;
import cn.udesk.provider.UdeskExternalCacheProvider;
import cn.udesk.provider.UdeskExternalFileProvider;
import cn.udesk.provider.UdeskExternalProvider;
import cn.udesk.provider.UdeskInternalCacheProvider;
import cn.udesk.provider.UdeskInternalFileProvider;
import udesk.core.JsonObjectUtils;
import udesk.core.LocalManageUtil;
import udesk.core.UdeskConst;
import udesk.core.event.InvokeEventContainer;
import udesk.core.model.AgentInfo;
import udesk.core.model.Content;
import udesk.core.model.LogBean;
import udesk.core.model.MessageInfo;
import udesk.core.model.ProductListBean;
import udesk.core.model.RobotInit;
import udesk.core.utils.UdeskIdBuild;
import udesk.core.utils.UdeskUtils;

import static cn.udesk.emotion.LQREmotionKit.getContext;


public class UdeskUtil {

    /**
     * 检查网络是否是连接
     */
    public static boolean isGpsNet(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

        return gprs == NetworkInfo.State.CONNECTED || gprs == NetworkInfo.State.CONNECTING;

    }

    public static File cameraFile(Context context) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File imageFile = new File(getDirectoryPath(context, UdeskConst.FileImg) + File.separator + timeStamp + UdeskConst.IMG_SUF);
            return imageFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 获取存储路径目录
     *
     * @param context
     * @param folderName
     * @return
     */
    public static String getDirectoryPath(Context context, String folderName) {
        String directoryPath = "";
        if (context == null) {
            return "";
        }
        try {
            if (UdeskUtils.checkSDcard() && context.getExternalFilesDir(UdeskConst.EXTERNAL_FOLDER) != null) {
                directoryPath = context.getExternalFilesDir(UdeskConst.EXTERNAL_FOLDER).getAbsolutePath() + File.separator + folderName;
            } else {
                directoryPath = context.getFilesDir() + File.separator + UdeskConst.EXTERNAL_FOLDER + File.separator + folderName;
            }
        } catch (Exception e) {
            directoryPath = context.getFilesDir() + File.separator + UdeskConst.EXTERNAL_FOLDER + File.separator + folderName;
        }
        File file = new File(directoryPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        return directoryPath;
    }

    /**
     * 提供给拍照的Uri
     *
     * @param context
     * @param file
     * @return
     */
    public static Uri getOutputMediaFileUri(Context context, File file) {
        if (file == null) {
            return null;
        }
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                if (context.getExternalFilesDir("") != null
                        && file.getAbsolutePath().contains(context.getExternalFilesDir("").getAbsolutePath())) {
                    return UdeskExternalFileProvider.getUriForFile(context, getExternalFileProviderName(context), file);
                } else if (file.getAbsolutePath().contains(context.getExternalCacheDir().getAbsolutePath())) {
                    return UdeskExternalCacheProvider.getUriForFile(context, getExternalCacheProviderName(context), file);
                } else if (file.getAbsolutePath().contains(context.getFilesDir().getAbsolutePath())) {
                    return UdeskInternalFileProvider.getUriForFile(context, getInternalFileProviderName(context), file);
                } else if (file.getAbsolutePath().contains(context.getCacheDir().getAbsolutePath())) {
                    return UdeskInternalCacheProvider.getUriForFile(context, getInternalCacheProviderName(context), file);
                } else if (file.getAbsolutePath().contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    return UdeskExternalProvider.getUriForFile(context, getExternalProviderName(context), file);
                }
            } else {
                return Uri.fromFile(file);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * 解析拍照返回的路径
     *
     * @param uri
     * @return
     */
    public static String parseOwnUri(Uri uri, Context context, File cameraFile) {
        if (uri == null) {
            return "";
        }
        if (isAndroidQ()) {
            return uri.toString();
        }
        String path = "";
        try {
            if (TextUtils.equals(uri.getAuthority(), getExternalFileProviderName(context))) {
                if (cameraFile != null) {
                    return cameraFile.getAbsolutePath();
                } else {
                    path = new File(context.getExternalFilesDir(""), uri.getPath().replace("my_external/", "")).getAbsolutePath();
                }
            } else if (TextUtils.equals(uri.getAuthority(), getExternalCacheProviderName(context))) {
                if (cameraFile != null) {
                    return cameraFile.getAbsolutePath();
                } else {
                    path = new File(context.getExternalCacheDir(), uri.getPath().replace("my_external/", "")).getAbsolutePath();
                }
            } else if (TextUtils.equals(uri.getAuthority(), getInternalFileProviderName(context))) {
                if (cameraFile != null) {
                    return cameraFile.getAbsolutePath();
                } else {
                    path = new File(context.getFilesDir(), uri.getPath().replace("my_external/", "")).getAbsolutePath();
                }
            } else if (TextUtils.equals(uri.getAuthority(), getInternalCacheProviderName(context))) {
                if (cameraFile != null) {
                    return cameraFile.getAbsolutePath();
                } else {
                    path = new File(context.getCacheDir(), uri.getPath().replace("my_external/", "")).getAbsolutePath();
                }
            } else if (TextUtils.equals(uri.getAuthority(), getExternalProviderName(context))) {
                if (cameraFile != null) {
                    return cameraFile.getAbsolutePath();
                } else {
                    path = new File(Environment.getExternalStorageDirectory(), uri.getPath().replace("my_external/", "")).getAbsolutePath();
                }
            } else {
                path = uri.getEncodedPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    /**
     * 本地输出文件路径
     *
     * @param context
     * @param filePath
     * @return
     */
    public static String getFilePathQ(Context context, String filePath) {
        try {
            if (context == null) {
                return "";
            }
            if (!filePath.startsWith("http")
                    && !filePath.startsWith("https")
                    && !filePath.startsWith("content")
                    && isAndroidQ()) {
                File file;
                if (filePath.startsWith("file")) {
                    file = new File(new URI(filePath));
                } else {
                    file = new File(filePath);
                }
                if (context.getExternalFilesDir("") != null
                        && filePath.contains(context.getExternalFilesDir("").getAbsolutePath())) {
                    return UdeskExternalFileProvider.getUriForFile(context, getExternalFileProviderName(context), file).toString();
                } else if (file.getAbsolutePath().contains(context.getExternalCacheDir().getAbsolutePath())) {
                    return UdeskExternalCacheProvider.getUriForFile(context, getExternalCacheProviderName(context), file).toString();
                } else if (filePath.contains(context.getFilesDir().getAbsolutePath())) {
                    return UdeskInternalFileProvider.getUriForFile(context, getInternalFileProviderName(context), file).toString();
                } else if (filePath.contains(context.getCacheDir().getAbsolutePath())) {
                    return UdeskInternalCacheProvider.getUriForFile(context, getInternalCacheProviderName(context), file).toString();
                } else if (file.getAbsolutePath().contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                    return UdeskExternalProvider.getUriForFile(context, getExternalProviderName(context), file).toString();
                }
            }
        } catch (Exception e) {
            return "";
        }
        return filePath;
    }

    public static Uri getUriFromPathBelowQ(Context context, String path) {
        try {
            if (TextUtils.isEmpty(path)) {
                return null;
            }
            if (path.startsWith("http") || path.startsWith("https") || path.startsWith("file") || path.startsWith("content")) {
                return Uri.parse(path);
            } else {
                return Uri.fromFile(new File(path));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri getUriFromPath(Context context, String path) {
        try {
            if (isAndroidQ()) {
                return Uri.parse(getFilePathQ(context, path));
            } else {
                return getUriFromPathBelowQ(context, path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getExternalFileProviderName(Context context) {
        return context.getPackageName() + ".udesk_external_file_provider";
    }

    public static String getExternalCacheProviderName(Context context) {
        return context.getPackageName() + ".udesk_external_cache_provider";
    }

    public static String getInternalFileProviderName(Context context) {
        return context.getPackageName() + ".udesk_internal_file_provider";
    }

    public static String getInternalCacheProviderName(Context context) {
        return context.getPackageName() + ".udesk_internal_cache_provider";
    }

    public static String getExternalProviderName(Context context) {
        return context.getPackageName() + ".udesk_external_provider";
    }

    /**
     * 根据文件类型和路径 创建目录和判断文件是否存在
     *
     * @param context
     * @param fileType
     * @param url
     * @return
     */
    public static boolean fileIsExitByUrl(Context context, String fileType, String url) {
        try {
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            String fileName = getFileName(context, url, fileType);
            String filepath = getDirectoryPath(context.getApplicationContext(), fileType) + File.separator + fileName;
            File file = new File(filepath);
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据本地路径判断文件是否存在
     *
     * @param context
     * @param path
     * @return
     */
    public static boolean isExitFileByPath(Context context, String path) {
        try {
            if (null == context) {
                return false;
            }
            if (isAndroidQ()) {
                ContentResolver cr = context.getContentResolver();
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(Uri.parse(getFilePathQ(context, path)), "r");
                if (null == afd) {
                    return false;
                } else {
                    afd.close();
                    return true;
                }
            } else {
                File file = new File(path);
                return file.exists();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据文件类型和路径 返回本地文件路径
     *
     * @param context
     * @param fileType
     * @param url
     * @return
     */
    public static String getPathByUrl(Context context, String fileType, String url) {
        String fileName = getFileName(context, url, fileType);
        try {
            return getDirectoryPath(context.getApplicationContext(), fileType) + File.separator + fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * 根据文件类型和路径创建本地文件
     *
     * @param context
     * @param fileType
     * @param url
     * @return
     */
    public static File getFileByUrl(Context context, String fileType, String url) {
        String fileName = getFileName(context, url, fileType);
        try {
            String filepath = getDirectoryPath(context.getApplicationContext(), fileType) + File.separator + fileName;
            File file = new File(filepath);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFileName(Context context, String filePath, String type) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        try {
            String filename = getFileName(context, filePath);
            if (filePath.startsWith("http") || filePath.startsWith("https")) {
                return filename;
            }
            if (type.equals(UdeskConst.FileAudio) && !filename.contains(UdeskConst.AUDIO_SUF_WAV)) {
                filename = filename + UdeskConst.AUDIO_SUF_WAV;
            } else if (type.equals(UdeskConst.FileImg) && !filename.contains(UdeskConst.IMG_SUF)) {
                filename = filename + UdeskConst.IMG_SUF;
            } else if (type.equals(UdeskConst.FileVideo) && !filename.contains(UdeskConst.VIDEO_SUF)) {
                filename = filename + UdeskConst.VIDEO_SUF;
            }
            return filename;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getFileName(Context context, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        try {
            if ((isAndroidQ() && filePath.startsWith("content"))) {
                return getFileName(context, Uri.parse(filePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getName(filePath);
    }

    public static String getFileName(@NonNull Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        String filename = null;
        if (mimeType == null) {
            filename = getName(uri.toString());
        } else {
            Cursor returnCursor = context.getContentResolver().query(uri, null,
                    null, null, null);
            if (returnCursor != null) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                filename = returnCursor.getString(nameIndex);
                returnCursor.close();
            }
        }
        return filename;
    }

    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf('/');
        return filename.substring(index + 1);
    }

    public static String saveBitmap(Context context, String url, Bitmap b) {
        String jpegName = getPathByUrl(context, UdeskConst.FileImg, url);
        try {
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return jpegName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String saveBitmap(Context context, Bitmap b) {
        try {
            String path = getDirectoryPath(context, UdeskConst.FileImg);
            long dataTake = System.currentTimeMillis();
            String jpegName = path + File.separator + "picture_" + dataTake + ".jpg";
            FileOutputStream fout = new FileOutputStream(jpegName);
            BufferedOutputStream bos = new BufferedOutputStream(fout);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return jpegName;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getFormUrlPara(Context context) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append("?sdk_token=").append(UdeskSDKManager.getInstance().getSdkToken(context))
                    .append("&sdk_version=").append(UdeskConst.sdkversion).append("&app_id=").append(UdeskSDKManager.getInstance().getAppId(context));
            if (!isZh(context)) {
                builder.append("&language=en-us");
            }
            Map<String, String> userinfo = UdeskSDKManager.getInstance().getUdeskConfig().defaultUserInfo;
            Map<String, String> textField = UdeskSDKManager.getInstance().getUdeskConfig().definedUserTextField;
            if (userinfo != null && !userinfo.isEmpty()) {
                Set<String> keySet = userinfo.keySet();
                for (String key : keySet) {
                    if (!TextUtils.isEmpty(userinfo.get(key))) {
                        if (key.equals("sdk_token")) {
                            continue;
                        }
                        if (key.equals(UdeskConst.UdeskUserInfo.NICK_NAME)) {
                            builder.append("&c_name=").append(userinfo.get(key));
                        } else if (key.equals(UdeskConst.UdeskUserInfo.CELLPHONE)) {
                            builder.append("&c_phone=").append(userinfo.get(key));
                        } else if (key.equals(UdeskConst.UdeskUserInfo.EMAIL)) {
                            builder.append("&c_email=").append(userinfo.get(key));
                        } else if (key.equals(UdeskConst.UdeskUserInfo.DESCRIPTION)) {
                            builder.append("&c_desc=").append(userinfo.get(key));
                        }
                    }
                }
            }
            if (textField != null && !textField.isEmpty()) {
                Set<String> textFieldSet = textField.keySet();
                for (String key : textFieldSet) {
                    if (!TextUtils.isEmpty(textField.get(key))) {
                        builder.append("&c_cf_").append(key).append("=").append(textField.get(key));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static HashMap<String, String> buildGetParams(String sdkToken, String mSecretKey, String appid) {
        HashMap<String, String> params = new HashMap<>();
        long timestamp = System.currentTimeMillis();
        long nonce = System.currentTimeMillis() * JsonObjectUtils.getRandom();
        String echostr = UUID.randomUUID().toString();
        params.put("nonce", String.valueOf(nonce));
        params.put("timestamp", String.valueOf(timestamp));
        params.put("sdk_token", sdkToken);
        params.put("echostr", echostr);
        params.put("sdk_version", UdeskConst.sdkversion);
        params.put("platform_name", "android");
        params.put("platform", "android");
        params.put("language", LocalManageUtil.getSetLanguageLocale());
        if (!TextUtils.isEmpty(appid)) {
            params.put("app_id", appid);
        }
        params.put("signature", UdeskUtils.getSignature(mSecretKey, sdkToken, timestamp, nonce));
        return params;
    }

    public static String formatLongTypeTimeToString(Context context, long time) {

        StringBuilder build = new StringBuilder();

        try {
            long OFFSET_DAY = 3600 * 24;
            String timeYes = context.getString(R.string.udesk_im_time_format_yday);
            String timeQt = context.getString(R.string.udesk_im_time_format_dby);
            String timeDate = "yyyy/MM/dd";
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            // 解析需要转化时间
            calendar.setTimeInMillis(time);
            int year = calendar.get(Calendar.YEAR);
            int day = calendar.get(Calendar.DAY_OF_YEAR);

            // 拼接 转化结果
            build.append(" ").append(sdf.format(calendar.getTime()));// 先添加

            // 先解析当前时间。取出当前年，日 等信息
            calendar.setTimeInMillis(System.currentTimeMillis());
            int nowYear = calendar.get(Calendar.YEAR);
            int nowDay = calendar.get(Calendar.DAY_OF_YEAR);

            if (year != nowYear) {// 不是一年内
                calendar.set(Calendar.HOUR_OF_DAY, 0); // 凌晨1点
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);

                if ((calendar.getTimeInMillis() - time) <= OFFSET_DAY) {// 昨天
                    return timeYes;
                } else if ((calendar.getTimeInMillis() - time) <= (OFFSET_DAY << 2)) {// 前天
                    // 。这里不用判断是否大于OFFSET_DAY
                    return timeQt;
                } else {
                    sdf.applyLocalizedPattern(timeDate);
                    return sdf.format(time);
                }

            } else if (day == nowDay) {// 这里是一年内的当天
                // 当天的话 就不用管了
            } else {// 一年内
                int dayOffset = (nowDay - day);// nowDay要大一些
                if (dayOffset == 0) {
                    // 同一天不用 添加日期判断
                } else if (dayOffset == 1) {// 1表示差一天，即昨天
                    return timeYes;
                } else if (dayOffset == 2) {// 1表示差两天，即前天
                    return timeQt;
                } else {
                    timeDate = "MM/dd";
                    sdf.applyLocalizedPattern(timeDate);
                    return sdf.format(time);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return build.toString();
    }


    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.startsWith("zh");
    }


    public static String getAppName(Context context) {
        String appName = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info;
            info = manager.getPackageInfo(context.getPackageName(), 0);
            appName = info.applicationInfo.loadLabel(manager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }


    public static String parseEventTime(String strTime) {
        if (strTime == null) {
            return "";
        }
        try {
            if (strTime.length() <= 10) {
                return strTime;
            }
            long time = stringToLong(strTime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            return sdf.format(new Date(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String parseEventTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    public static String getCurrentDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }

    public static long stringToLong(String strTime) {
        Date date = stringToDate(strTime); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            return dateToLong(date);
        }
    }

    public static Date stringToDate(String strTime) {
        SimpleDateFormat formatter;
        Date date = null;
        if (strTime.contains("T") && strTime.contains("+")) {
            if (strTime.length() > 26) {
                formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            } else {
                formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            }
        } else if (strTime.contains("T")) {
            formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        } else {
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        try {
            date = formatter.parse(strTime);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
    }

    public static long dateToLong(Date date) {
        return date.getTime();
    }

    // 暂停图片请求
    public static void imagePause(Context context) {
        Glide.with(context).pauseRequests();
    }

    // 恢复图片请求
    public static void imageResume(Context context) {
        Glide.with(context).resumeRequests();
    }

    /**
     * 获取图像的宽高
     **/

    public static int[] getImageWH(Context context, String path) {
        int[] wh = {-1, -1};
        if (path == null) {
            return wh;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            if (isAndroidQ()) {
                AssetFileDescriptor parcelFileDescriptor = context.getContentResolver().openAssetFileDescriptor(Uri.parse(getFilePathQ(context, path)), "r");
                if (parcelFileDescriptor != null) {
                    BitmapFactory.decodeStream(parcelFileDescriptor.createInputStream(), null, options);
                }
            } else {
                InputStream is = new FileInputStream(path);
                BitmapFactory.decodeStream(is, null, options);
            }
            wh[0] = options.outWidth;
            wh[1] = options.outHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wh;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        } catch (Exception e) {
            bitmap = null;
        }
        return bitmap;
    }

    /**
     * 本地缓存文件
     */
    public static void getFileFromDiskCache(Context context, String path, UdeskImageLoader.UdeskDownloadImageListener udeskDownloadImageListener) {
        try {
            UdeskImage.loadImageFile(context, getUriFromPath(context, path), udeskDownloadImageListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadImage(Context context, final ImageView imageView, String path) {
        try {
            loadViewBySize(context, imageView, path, imageView.getWidth(), imageView.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadViewBySize(Context context, ImageView imageView, String path, int width, int height) {
        try {
            UdeskImage.loadImage(context, imageView, getUriFromPath(context, path), R.drawable.udesk_defalut_image_loading, R.drawable.udesk_defualt_failure, width, height, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scaleImageView(Context context, ImageView imageView, boolean isfixScale, int reqWidth, int reqHeight) {
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        int imgWidth = dip2px(context, 140);
        if (isfixScale) {
            //固定宽度缩放
            double bitScalew = (double) reqWidth / imgWidth;
            layoutParams.height = (int) (reqHeight / bitScalew);
            layoutParams.width = (int) (reqWidth / bitScalew);
        } else {
            int imgHight = dip2px(context, 220);
            double bitScalew = getRatioSize(reqWidth, reqHeight, imgHight, imgWidth);
            if (bitScalew >= 1) {
                layoutParams.height = (int) (reqHeight / bitScalew);
                layoutParams.width = (int) (reqWidth / bitScalew);
            } else if (bitScalew >= 0.5) {
                layoutParams.height = reqHeight;
                layoutParams.width = reqWidth;
            } else {
                layoutParams.height = imgWidth / 2;
                layoutParams.width = imgWidth / 2;
            }
        }
        imageView.requestLayout();
    }

    public static void loadScaleImage(final Context context, final ImageView imageView, String path, final boolean isfixScale) {

        try {
            UdeskImageLoader.UdeskDisplayImageListener udeskDisplayImageListener = new UdeskImageLoader.UdeskDisplayImageListener() {
                @Override
                public void onSuccess(View view, Uri uri, int width, int height) {
                    scaleImageView(context, imageView, isfixScale, width, height);
                }
            };
            UdeskImage.loadImage(context, imageView, getUriFromPath(context, path), R.drawable.udesk_defalut_image_loading, R.drawable.udesk_defualt_failure, imageView.getWidth(), imageView.getHeight(), udeskDisplayImageListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadDontAnimateImage(Context context, ImageView imageView, String path) {
        try {
            loadDontAnimateAndResizeImage(context, imageView, path, imageView.getWidth(), imageView.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadDontAnimateAndResizeImage(Context context, ImageView imageView, String path, int width, int height) {
        try {
            UdeskImage.loadDontAnimateAndResizeImage(context, imageView, getUriFromPath(context, path), R.drawable.udesk_defalut_image_loading, R.drawable.udesk_defualt_failure, width, height, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getRatioSize(int bitWidth, int bitHeight, int imageHeight, int imageWidth) {

        // 缩放比
        double ratio = 1.0;
        // 缩放比,由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        if (bitWidth >= bitHeight && bitWidth > imageWidth) {
            // 如果图片宽度比高度大,以宽度为基准
            ratio = (double) bitWidth / imageWidth;
        } else if (bitWidth < bitHeight && bitHeight > imageHeight) {
            // 如果图片高度比宽度大，以高度为基准
            ratio = (double) bitHeight / imageHeight;
        }
//        // 最小比率为1
//        if (ratio <= 1.0)
//            ratio = 1.0;
        return ratio;
    }

    public static int dip2px(Context context, int dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static String getFilePath(Context context, Uri uri) {
        String path = "";
        try {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                Cursor cursor = null;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        path = getPathQ(context, uri);
                    } else {
                        String[] projection = {MediaStore.Images.Media.DATA};
                        cursor = context.getContentResolver().query(uri, projection, null, null, null);
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        cursor.moveToFirst();
                        path = cursor.getString(column_index);
                    }
                } catch (Exception e) {
                    return uri.getPath();
                } finally {

                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (TextUtils.isEmpty(path.trim())) {
                path = uri.getPath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return path;
    }


    public static String getPathQ(final Context context, final Uri uri) {
        try {
            if (isAndroid_19()) {
                if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if (isAndroidQ()) {
                        if ("image".equals(type)) {
                            return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.valueOf(split[1])).toString();
                        } else if ("video".equals(type)) {
                            return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, Long.valueOf(split[1])).toString();
                        } else if ("audio".equals(type)) {
                            return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.valueOf(split[1])).toString();
                        }
                    }
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                } else {
                    if (isAndroidQ()) {
                        return uri.toString();
                    } else {
                        if (DocumentsContract.isDocumentUri(context, uri)) {
                            // ExternalStorageProvider
                            if (isExternalStorageDocument(uri)) {
                                final String docId = DocumentsContract.getDocumentId(uri);
                                final String[] split = docId.split(":");
                                final String type = split[0];

                                if ("primary".equalsIgnoreCase(type)) {
                                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                                }

                            }
                            // DownloadsProvider
                            else if (isDownloadsDocument(uri)) {
                                final String id = DocumentsContract.getDocumentId(uri);

                                if (id != null && id.startsWith("raw:")) {
                                    return id.substring(4);
                                }

                                String[] contentUriPrefixesToTry = new String[]{
                                        "content://downloads/public_downloads",
                                        "content://downloads/my_downloads"
                                };

                                for (String contentUriPrefix : contentUriPrefixesToTry) {
                                    Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                                    try {
                                        String path = getDataColumn(context, contentUri, null, null);
                                        if (path != null && !path.equals("")) {
                                            return path;
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                                return getCopyFilePath(context, uri);
                            }
                        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                            String path = getDataColumn(context, uri, null, null);
                            if (path != null && !path.equals("")) {
                                return path;
                            }
                            return getCopyFilePath(context, uri);
                        }
                        // File
                        else if ("file".equalsIgnoreCase(uri.getScheme())) {
                            return uri.getPath();
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getCopyFilePath(Context context, Uri uri) {
        String fileName = getFileName(context, uri);
        File cacheDir = getDocumentCacheDir(context);
        File file = generateFileName(fileName, cacheDir);
        String destinationPath = "";
        if (file != null) {
            destinationPath = file.getAbsolutePath();
            saveFileFromUri(context, uri, destinationPath);
        }

        return destinationPath;
    }


    public static File getDocumentCacheDir(@NonNull Context context) {
        File dir = (context.getExternalFilesDir(UdeskConst.EXTERNAL_FOLDER));
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    @Nullable
    public static File generateFileName(@Nullable String name, File directory) {
        if (name == null) {
            return null;
        }

        File file = new File(directory, name);

        if (file.exists()) {
            String fileName = name;
            String extension = "";
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) {
                fileName = name.substring(0, dotIndex);
                extension = name.substring(dotIndex);
            }

            int index = 0;

            while (file.exists()) {
                index++;
                name = fileName + '(' + index + ')' + extension;
                file = new File(directory, name);
            }
        }

        try {
            if (!file.createNewFile()) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        return file;
    }

    private static void saveFileFromUri(Context context, Uri uri, String destinationPath) {
        InputStream is = null;
        BufferedOutputStream bos = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            bos = new BufferedOutputStream(new FileOutputStream(destinationPath, false));
            byte[] buf = new byte[1024];
            is.read(buf);
            do {
                bos.write(buf);
            } while (is.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isAndroid_19() {
        return Build.VERSION.SDK_INT >= 19;
    }

    public static boolean isAndroidQ() {
        return Build.VERSION.SDK_INT >= 29;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public static File getExternalCacheDir(final Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return createFile(Environment.getExternalStorageDirectory().getPath() + cacheDir, "");
    }

    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static File createFile(String folderPath, String fileName) {
        File destDir = new File(folderPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return new File(folderPath, fileName);
    }

    public static String getMIMEType(File file) {
        String type = "*/*";
        try {
            String fName = file.getName();
            //获取后缀名前的分隔符"."在fName中的位置。
            int dotIndex = fName.lastIndexOf(".");
            if (dotIndex < 0) {
                return type;
            }
            /* 获取文件的后缀名*/
            String end = fName.substring(dotIndex, fName.length()).toLowerCase();
            if (end.equals("")) {
                return type;
            }
            //在MIME和文件类型的匹配表中找到对应的MIME类型。
            for (int i = 0; i < MIME_MapTable.length; i++) {
                if (end.equals(MIME_MapTable[i][0])) {
                    type = MIME_MapTable[i][1];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return type;
    }

    public static String getPngName(String fileName) {
        try {
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex < 0) {
                return fileName + ".png";
            } else {
                return fileName.substring(0, dotIndex) + ".png";
            }
        } catch (Exception e) {
        }
        return fileName;
    }

    public static String getMIMEType(Context context, Uri uri) {
        String mimeType = "*/*";
        try {
            mimeType = context.getContentResolver().getType(uri);
            if (mimeType != null) {
                return mimeType;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mimeType;
    }


    public static final String[][] MIME_MapTable = {
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
    };

    public final static int TYPE_IMAGE = 1;
    public final static int TYPE_SHORT_VIDEO = 2;
    public final static int TYPE_AUDIO = 3;

    public static int isPictureType(String pictureType) {
        if (TextUtils.isEmpty(pictureType)) {
            return TYPE_IMAGE;
        }
        switch (pictureType) {
            case "image/png":
            case "image/PNG":
            case "image/jpeg":
            case "image/JPEG":
            case "image/webp":
            case "image/WEBP":
            case "image/gif":
            case "image/bmp":
            case "image/GIF":
            case "imagex-ms-bmp":
                return TYPE_IMAGE;
            case "video/3gp":
            case "video/3gpp":
            case "video/3gpp2":
            case "video/avi":
            case "video/mp4":
            case "video/quicktime":
            case "video/x-msvideo":
            case "video/x-matroska":
            case "video/mpeg":
            case "video/webm":
            case "video/mp2ts":
                return TYPE_SHORT_VIDEO;
            case "audio/mpeg":
            case "audio/x-ms-wma":
            case "audio/x-wav":
            case "audio/amr":
            case "audio/wav":
            case "audio/aac":
            case "audio/mp4":
            case "audio/quicktime":
            case "audio/lamr":
            case "audio/3gpp":
                return TYPE_AUDIO;
        }
        return TYPE_IMAGE;
    }


    public static String getFileSizeByLoaclPath(Context context, String filePath) {
        try {
            long blockSize = 0L;
            if (UdeskUtil.isAndroidQ()) {
                AssetFileDescriptor assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(Uri.parse(getFilePathQ(context, filePath)), "r");
                if (assetFileDescriptor != null) {
                    blockSize = assetFileDescriptor.getLength();
                }
            } else {
                File file = new File(filePath);
                if (file.exists()) {
                    blockSize = getFileSize(file);
                }
            }
            return formetFileSize(blockSize);
        } catch (Exception e) {
            return "0B";
        }
    }

    public static long getFileSizeQ(Context context, String filePath) {
        long blockSize = 0L;
        try {
            if (UdeskUtil.isAndroidQ()) {
                AssetFileDescriptor assetFileDescriptor = context.getContentResolver().openAssetFileDescriptor(Uri.parse(getFilePathQ(context, filePath)), "r");
                if (assetFileDescriptor != null) {
                    blockSize = assetFileDescriptor.getLength();
                }
            } else {
                File file = new File(filePath);
                if (file.exists()) {
                    blockSize = getFileSize(file);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blockSize;
    }

    public static long getFileSize(File file) {
        long size = 0;
        if (file == null) {
            return size;
        }
        if (file.exists()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(file);
                size = fis.available();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return size;
    }


    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1000) {
            DecimalFormat dfb = new DecimalFormat("#");
            fileSizeString = dfb.format((double) fileS) + "B";
        } else if (fileS < 1000000) {
            fileSizeString = df.format((double) fileS / 1000) + "KB";
        } else {
            fileSizeString = df.format((double) fileS / 1000000) + "MB";
        }

        return fileSizeString;
    }


    public static String timeParse(long duration) {
        String time = "";
        if (duration > 1000) {
            time = timeParseMinute(duration);
        } else {
            long minute = duration / 60000;
            long seconds = duration % 60000;
            long second = Math.round((float) seconds / 1000);
            if (minute < 10) {
                time += "0";
            }
            time += minute + ":";
            if (second < 10) {
                time += "0";
            }
            time += second;
        }
        return time;
    }

    private static SimpleDateFormat msFormat = new SimpleDateFormat("mm:ss");

    public static String timeParseMinute(long duration) {
        try {
            return msFormat.format(duration);
        } catch (Exception e) {
            e.printStackTrace();
            return "0:00";
        }
    }

    public static void modifyTextViewDrawable(TextView v, Drawable drawable, int index) {
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        //index 0:左 1：上 2：右 3：下
        if (index == 0) {
            v.setCompoundDrawables(drawable, null, null, null);
        } else if (index == 1) {
            v.setCompoundDrawables(null, drawable, null, null);
        } else if (index == 2) {
            v.setCompoundDrawables(null, null, drawable, null);
        } else {
            v.setCompoundDrawables(null, null, null, drawable);
        }
    }

    public static Bitmap getVideoThumbnail(String url) {
        Bitmap bitmap = null;
        // MediaMetadataRetriever 是android中定义好的一个类，提供了统一
        // 的接口，用于从输入的媒体文件中取得帧和元数据；
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(url, new HashMap());
            //获得第一帧图片
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    //过滤掉字符串中的特殊字符
    public static String stringFilter(String str) {
        String regEx = "[/=]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }


    public static boolean isClassExists(String classFullName) {

        try {
            Class.forName(classFullName);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }


//    public static void initCrashReport(Context context) {
//        try {
//            CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
//            strategy.setAppVersion(UdeskConst.sdkversion + UdeskUtil.getAppName(context));
//            CrashReport.initCrashReport(context, UdeskConst.buglyAppid, false, strategy);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    public static void setOrientation(Activity context) {

        String orientation = UdeskSDKManager.getInstance().getUdeskConfig().Orientation;
        if (orientation.equals(UdeskConfig.OrientationValue.portrait)) {
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (orientation.equals(UdeskConfig.OrientationValue.landscape)) {
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (orientation.equals(UdeskConfig.OrientationValue.user)) {
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    //预览大图
    public static void previewPhoto(Context context, Uri uri) {
        try {
            if (uri == null) {
                return;
            }
            Intent intent = new Intent(context,
                    UdeskZoomImageActivty.class);
            Bundle data = new Bundle();
            data.putParcelable("image_path", uri);
            intent.putExtras(data);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param inStream
     * @return byte[]
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        byte[] data = new byte[0];
        try {
            byte[] buffer = new byte[1024];
            int len;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            data = outStream.toByteArray();
            outStream.close();
            inStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;

    }


    public static MessageInfo addLeavMsgWeclome(String content, String leavMsgId) {
        MessageInfo msg = new MessageInfo();
        try {
            msg.setMsgtype(UdeskConst.ChatMsgTypeString.TYPE_RICH);
            msg.setTime(System.currentTimeMillis());
            msg.setMsgId(leavMsgId);
            msg.setDirection(UdeskConst.ChatMsgDirection.Recv);
            msg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
            msg.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
            msg.setMsgContent(content);
            msg.setPlayflag(UdeskConst.PlayFlag.NOPLAY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return msg;
    }

    public static MessageInfo buildSendMessage(String msgType, long time, String text) {
        return buildSendMessage(msgType, time, text, "", "", "");
    }

    //构建消息模型
    public static MessageInfo buildSendMessage(String msgType, long time, String text,
                                               String location, String fileName, String fileSize) {
        MessageInfo msg = new MessageInfo();
        try {
            msg.setMsgtype(msgType);
            msg.setTime(time);
            msg.setMsgId(UdeskIdBuild.buildMsgId());
            msg.setDirection(UdeskConst.ChatMsgDirection.Send);
            msg.setSendFlag(UdeskConst.SendFlag.RESULT_SEND);
            msg.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
            msg.setMsgContent(text);
            msg.setPlayflag(UdeskConst.PlayFlag.NOPLAY);
            msg.setLocalPath(location);
            msg.setFilename(fileName);
            msg.setFilesize(fileSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    public static SurveyOptionsModel buildSurveyOptionsModel(Context context) {
        SurveyOptionsModel model = new SurveyOptionsModel();
        model.setEnabled(true);
        model.setName(context.getResources().getString(R.string.udesk_satisfy_evaluation));
        model.setTitle(context.getResources().getString(R.string.udesk_satisfy_evaluation_title));
        model.setRemark_enabled(true);
        model.setRemark(context.getResources().getString(R.string.udesk_satisfy_evaluation_remark));
        model.setType("text");
        model.setDefault_option_id(0);
        model.setRobot(true);
        List<OptionsModel> options = new ArrayList<>();
        //评价选项ID (1：未评价 2：满意 3：一般 4：不满意)
        int id = 1;
        options.add(new OptionsModel(++id, true, context.getResources().getString(R.string.udesk_statify), context.getResources().getString(R.string.udesk_statify), UdeskConst.REMARK_OPTION_OPTIONAL));
        options.add(new OptionsModel(++id, true, context.getResources().getString(R.string.udesk_common), context.getResources().getString(R.string.udesk_common), UdeskConst.REMARK_OPTION_OPTIONAL));
        options.add(new OptionsModel(++id, true, context.getResources().getString(R.string.udesk_unstatify), context.getResources().getString(R.string.udesk_unstatify), UdeskConst.REMARK_OPTION_OPTIONAL));
        model.setOptions(options);
        return model;
    }

    public static MessageInfo buildSendChildMsg(String content) {
        return buildMsg("", "", System.currentTimeMillis(), UdeskIdBuild.buildMsgId(), UdeskConst.ChatMsgTypeString.TYPE_TEXT, content, UdeskConst.ChatMsgReadFlag.read,
                UdeskConst.SendFlag.RESULT_SUCCESS, UdeskConst.PlayFlag.NOPLAY, UdeskConst.ChatMsgDirection.Send, "", 0, "", "", "",
                0, "");
    }

    public static MessageInfo buildRobotTransferMsg(String content) {
        return buildMsg("", "", System.currentTimeMillis(), UdeskIdBuild.buildMsgId(), UdeskConst.ChatMsgTypeString.TYPE_ROBOT_TRANSFER, content, UdeskConst.ChatMsgReadFlag.read,
                UdeskConst.SendFlag.RESULT_SUCCESS, UdeskConst.PlayFlag.NOPLAY, UdeskConst.ChatMsgDirection.Recv, "", 0, "", "", "",
                0, "");
    }

    public static MessageInfo buildReplyProductMsg(ProductListBean bean) {
        return buildMsg("", "", System.currentTimeMillis(), UdeskIdBuild.buildMsgId(), UdeskConst.ChatMsgTypeString.TYPE_REPLY_PRODUCT, JsonUtils.getReplyProductJson(bean).toString().replace("\\", ""), UdeskConst.ChatMsgReadFlag.read,
                UdeskConst.SendFlag.RESULT_SUCCESS, UdeskConst.PlayFlag.NOPLAY, UdeskConst.ChatMsgDirection.Send, "", 0, "", "", "",
                0, "");
    }

    public static MessageInfo buildRobotInitRelpy(RobotInit robotInit) {
        MessageInfo messageInfo = buildMsg(robotInit.getWebConfig().getRobotName(), robotInit.getWebConfig().getLogoUrl(), System.currentTimeMillis(), UdeskIdBuild.buildMsgId(), UdeskConst.ChatMsgTypeString.TYPE_ROBOT_CLASSIFY,
                "", UdeskConst.ChatMsgReadFlag.read, UdeskConst.SendFlag.RESULT_SUCCESS, UdeskConst.PlayFlag.NOPLAY, UdeskConst.ChatMsgDirection.Recv, "",
                0, "", "", "", UdeskUtils.objectToInt(robotInit.getSwitchStaffType()), UdeskUtils.objectToString(robotInit.getSwitchStaffTips()));
        messageInfo.setWebConfig(robotInit.getWebConfig());
        messageInfo.setTopAsk(robotInit.getTopAsk());
        messageInfo.setLogId(robotInit.getLogId());
        messageInfo.setFAQ(true);
        return messageInfo;

    }

    public static MessageInfo buildWelcomeRelpy(RobotInit robotInit) {
        MessageInfo messageInfo = buildMsg(robotInit.getWebConfig().getRobotName(), robotInit.getWebConfig().getLogoUrl(), System.currentTimeMillis(), UdeskIdBuild.buildMsgId(), UdeskConst.ChatMsgTypeString.TYPE_RICH,
                robotInit.getWebConfig().getHelloWord(), UdeskConst.ChatMsgReadFlag.read, UdeskConst.SendFlag.RESULT_SUCCESS, UdeskConst.PlayFlag.NOPLAY, UdeskConst.ChatMsgDirection.Recv,
                "", 0, "", "", "", UdeskUtils.objectToInt(robotInit.getSwitchStaffType()), UdeskUtils.objectToString(robotInit.getSwitchStaffTips()));
        messageInfo.setLogId(robotInit.getLogId());
        return messageInfo;

    }

    public static MessageInfo buildAllMessage(LogBean message) {
        if (message.getContent() != null) {
            Content content = message.getContent();
            if (content.getData() != null) {
                MessageInfo info = buildMsg(message.getAgent_nick_name(), message.getAgent_avatar(), stringToLong(message.getCreated_at()),
                        UdeskUtils.objectToString(message.getMessage_id()), message.getContent().getType(), content.getData().getContent(),
                        UdeskConst.ChatMsgReadFlag.read, UdeskConst.SendFlag.RESULT_SUCCESS, UdeskConst.PlayFlag.NOPLAY, UdeskConst.ChatMsgDirection.Recv,
                        message.getContent().getLocalPath(), UdeskUtils.objectToLong(message.getContent().getData().getDuration()),
                        message.getAgent_jid(), message.getContent().getFilename(), message.getContent().getFilesize(),
                        content.getData().getSwitchStaffType(), content.getData().getSwitchStaffTips());
                if (message.getInviterAgentInfo() != null) {
                    info.setReplyUser(message.getInviterAgentInfo().getNick_name());
                    info.setUser_avatar(message.getInviterAgentInfo().getAvatar());
                    info.setmAgentJid(message.getInviterAgentInfo().getJid());
                }
                info.setTopAsk(content.getData().getTopAsk());
                info.setLogId(message.getLogId());
                info.setSeqNum(message.getContent().getSeq_num());
                info.setSender(message.getSender());
                info.setFlowContent(content.getData().getFlowContent());
                info.setFlowId(content.getData().getFlowId());
                info.setFlowTitle(content.getData().getFlowTitle());
                info.setQuestion_id(UdeskUtils.objectToString(content.getData().getQuesition_id()));
                if (message.getSender().equals(UdeskConst.Sender.customer)) {
                    info.setDirection(UdeskConst.ChatMsgDirection.Send);
                }
                return info;
            }
        }
        return null;
    }

    public static MessageInfo buildMsg(String name, String logoUrl, long time, String msgId, String msgtype, String msgContent,
                                       int readFlag, int sendFlag, int playflag, int direction,
                                       String localPath, long duration, String agentJid, String fileName, String fileSize, int switchStaffType, String switchStaffTips) {
        MessageInfo msg = new MessageInfo();
        try {
            msg.setReplyUser(name);
            msg.setUser_avatar(logoUrl);
            msg.setMsgtype(msgtype);
            msg.setTime(time);
            msg.setMsgId(msgId);
            msg.setDirection(direction);
            msg.setSendFlag(sendFlag);
            msg.setReadFlag(readFlag);
            msg.setMsgContent(msgContent);
            msg.setPlayflag(playflag);
            msg.setLocalPath(localPath);
            msg.setFilename(fileName);
            msg.setFilesize(fileSize);
            msg.setDuration(duration);
            msg.setmAgentJid(agentJid);
            msg.setSwitchStaffTips(switchStaffTips);
            msg.setSwitchStaffType(switchStaffType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }

    public static MessageInfo buildVideoEventMsg(String id, Boolean isInvite, String text,
                                                 String customerId, String agentJid, String agentNickName,
                                                 String subSessionId) {
        MessageInfo msg = new MessageInfo();
        try {
            msg.setCustomerId(customerId);
            msg.setMsgtype(UdeskConst.ChatMsgTypeString.TYPE_LIVE_VIDEO);
            msg.setTime(System.currentTimeMillis());
            msg.setMsgId(id);
            if (isInvite) {
                msg.setDirection(UdeskConst.ChatMsgDirection.Send);
                msg.setSender(UdeskConst.Sender.customer);
            } else {
                msg.setDirection(UdeskConst.ChatMsgDirection.Recv);
                msg.setmAgentJid(agentJid);
                msg.setSender(UdeskConst.Sender.agent);
            }
            msg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
            msg.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
            if (isInvite) {
                msg.setMsgContent(text);
            } else {
                msg.setMsgContent(agentNickName + text);
            }
            msg.setPlayflag(UdeskConst.PlayFlag.NOPLAY);
            msg.setLocalPath("");
            msg.setDuration(0);
            msg.setSubsessionid(subSessionId);
            msg.setSeqNum(UdeskDBManager.getInstance().getSubSessionId(subSessionId));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }


    public static void sendVideoMessage(ImSetting sdkimSetting, AgentInfo mAgentInfo, Context context) {
        try {

            if (sdkimSetting != null) {
                //分配到客服后。建立websocket连接
                String domain = UdeskSDKManager.getInstance().getDomain(context);
                String[] domains = domain.split("\\.");
                if (domains.length > 0) {
                    domain = domains[0];
                }
                if (!TextUtils.isEmpty(mAgentInfo.getIm_sub_session_id())) {
                    UdeskConst.IMBussinessId = mAgentInfo.getIm_sub_session_id();
                }
                if (!TextUtils.isEmpty(mAgentInfo.getAgentJid())) {
                    UdeskConst.IMAgentJid = mAgentInfo.getAgentJid();
                }
                if (!TextUtils.isEmpty(mAgentInfo.getAgentNick())) {
                    UdeskConst.IMAgentName = mAgentInfo.getAgentNick();
                }
                if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getImInfo().getUsername())) {
                    UdeskConst.IMCustomerJid = UdeskSDKManager.getInstance().getImInfo().getUsername();
                }
                if (!TextUtils.isEmpty(sdkimSetting.getVc_app_id())) {
                    UdeskConst.vc_app_id = sdkimSetting.getVc_app_id();
                }
                if (!TextUtils.isEmpty(sdkimSetting.getAgora_app_id())) {
                    UdeskConst.agora_app_id = sdkimSetting.getAgora_app_id();
                }
                if (!TextUtils.isEmpty(sdkimSetting.getServer_url())) {
                    UdeskConst.server_url = sdkimSetting.getServer_url();
                }

                if (!TextUtils.isEmpty(sdkimSetting.getVcall_token_url())) {
                    UdeskConst.signToenUrl = sdkimSetting.getVcall_token_url();
                }
                if (!TextUtils.isEmpty(domain)) {
                    UdeskConst.Subdomain = domain;
                }

//                InvokeEventContainer.getInstance().event_OnConnectWebsocket.invoke(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object connectVideoWebSocket(Context context) {
        try {
            if (isClassExists("udesk.udeskvideo.UdeskVideoActivity")){
                Class c = Class.forName("udesk.udeskvideo.ReflectManager");
                Constructor declaredConstructor = c.getDeclaredConstructor();
                declaredConstructor.setAccessible(true);
                Object o = declaredConstructor.newInstance();
                Method declaredMethod = c.getDeclaredMethod("OnConnectWebsocket",Context.class);
                declaredMethod.setAccessible(true);
                declaredMethod.invoke(o,context);
                return o;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 原图
     *
     * @param bitmap
     * @param context
     * @return
     */
    public static File getScaleFile(Bitmap bitmap, Context context) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int max = Math.max(width, height);

            BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
            factoryOptions.inJustDecodeBounds = false;
            factoryOptions.inPurgeable = true;
            // 获取原图数据
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] data = stream.toByteArray();
            File scaleImageFile = new File(getDirectoryPath(context, UdeskConst.FileImg) + File.separator + UdeskConst.ORIGINAL_SUFFIX);
            if (scaleImageFile != null) {
                if (max > UdeskSDKManager.getInstance().getUdeskConfig().ScaleMax) {
                    factoryOptions.inSampleSize = max / UdeskSDKManager.getInstance().getUdeskConfig().ScaleMax;
                } else {
                    factoryOptions.inSampleSize = 1;
                }
                FileOutputStream fos;
                try {
                    fos = new FileOutputStream(scaleImageFile);
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
                            factoryOptions);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                bitmap.recycle();
                if (TextUtils.isEmpty(scaleImageFile.getPath())) {
                    UdeskUtils.showToast(context, context.getString(R.string.udesk_upload_img_error));
                    return null;
                }
                return scaleImageFile;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

        return null;
    }

    /**
     * 缩略图
     *
     * @param path
     * @param context
     * @return
     */
    public static File getScaleFile(final Context context, String path, int orientation) {
        try {
            Bitmap scaleImage = null;
            byte[] data;
            int max;
            BitmapFactory.Options options = new BitmapFactory.Options();
            /**
             * 在不分配空间状态下计算出图片的大小
             */
            options.inJustDecodeBounds = true;
            decodeFileAndContent(context, path, options);
            int width = options.outWidth;
            int height = options.outHeight;
            // 取得图片旋转角度
            if (orientation == 90 || orientation == 270) {
                options.outWidth = height;
                options.outHeight = width;
            }
            max = Math.max(width, height);
            options.inTempStorage = new byte[100 * 1024];
            options.inJustDecodeBounds = false;
            options.inPurgeable = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            InputStream inStream;
            if (UdeskUtil.isAndroidQ()) {
                inStream = context.getContentResolver().openInputStream(Uri.parse(UdeskUtil.getFilePathQ(context, path)));
            } else {
                inStream = new FileInputStream(path);
            }
            data = UdeskUtil.readStream(inStream);
            if (data == null || data.length <= 0) {
                return null;
            }
            String imageName = UdeskUtils.MD5(data);
            File scaleImageFile = new File(getDirectoryPath(context, UdeskConst.FileImg) + File.separator + imageName + UdeskConst.ORIGINAL_SUFFIX);
            if (!scaleImageFile.exists()) {
                // 缩略图不存在，生成上传图
                if (max > UdeskSDKManager.getInstance().getUdeskConfig().ScaleMax) {
                    options.inSampleSize = max / UdeskSDKManager.getInstance().getUdeskConfig().ScaleMax;
                } else {
                    options.inSampleSize = 1;
                }
                FileOutputStream fos = new FileOutputStream(scaleImageFile);
                scaleImage = BitmapFactory.decodeByteArray(data, 0,
                        data.length, options);
                // 取得图片旋转角度
                if (orientation != 0) {
                    scaleImage = rotaingImageView(orientation, scaleImage);
                }
                scaleImage.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.close();
            }

            if (scaleImage != null) {
                scaleImage.recycle();
            }
            if (TextUtils.isEmpty(scaleImageFile.getPath())) {
                return null;
            } else {
                return scaleImageFile;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
        return null;
    }

    public static void decodeFileAndContent(Context context, String path, BitmapFactory.Options options) {
        try {
            if (isAndroidQ()) {
                AssetFileDescriptor parcelFileDescriptor = context.getContentResolver().openAssetFileDescriptor(Uri.parse(getFilePathQ(context, path)), "r");
                if (parcelFileDescriptor != null) {
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
                    parcelFileDescriptor.close();
                }
            } else {
                BitmapFactory.decodeFile(path, options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /* 旋转图片
     *
     * @param angle  被旋转角度
     * @param bitmap 图片对象
     * @return 旋转后的图片
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        Bitmap returnBm = null;
        if (angle == 0) {
            return bitmap;
        }
        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bitmap;
        }
        if (bitmap != returnBm) {
            bitmap.recycle();
        }
        return returnBm;
    }

    //构建工单回复的消息转换消息模型
    public static List<MessageInfo> buildLeaveMsgByTicketReplies(List<TicketReplieMode.ContentsBean> contents, Context context) {
        List<MessageInfo> msgInfos = new ArrayList<MessageInfo>();
        try {
            if (contents != null && contents.size() > 0) {
                MessageInfo eventMsg = new MessageInfo();
                eventMsg.setMsgtype(UdeskConst.ChatMsgTypeString.TYPE_EVENT);
                eventMsg.setTime(System.currentTimeMillis());
                eventMsg.setMsgId(UdeskIdBuild.buildMsgId());
                eventMsg.setDirection(UdeskConst.ChatMsgDirection.Recv);
                eventMsg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
                eventMsg.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
                eventMsg.setMsgContent(context.getString(R.string.udesk_offline_reply_msg));
                eventMsg.setCreatedTime(contents.get(0).getReply_created_at());
                Collections.reverse(contents);
                boolean isAddEvent = false;
                for (TicketReplieMode.ContentsBean contentsBean : contents) {
                    if (contentsBean != null && !UdeskDBManager.getInstance().hasReceviedMsg(String.valueOf(contentsBean.getReply_id()))) {
                        MessageInfo msg = new MessageInfo();
                        msg.setMsgtype(UdeskConst.ChatMsgTypeString.TYPE_LEAVEMSG);
                        msg.setTime(System.currentTimeMillis());
                        msg.setMsgId(String.valueOf(contentsBean.getReply_id()));
                        msg.setDirection(UdeskConst.ChatMsgDirection.Recv);
                        msg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
                        msg.setReadFlag(UdeskConst.ChatMsgReadFlag.read);
                        msg.setMsgContent(contentsBean.getReply_content());
                        msg.setPlayflag(UdeskConst.PlayFlag.NOPLAY);
                        msg.setLocalPath("");
                        msg.setDuration(0);
                        msg.setUser_avatar(contentsBean.getUser_avatar());
                        msg.setCreatedTime(contentsBean.getReply_created_at());
                        msg.setUpdateTime(contentsBean.getReply_updated_at());
                        msg.setReplyUser(contentsBean.getReply_user());
                        msgInfos.add(msg);
                        UdeskDBManager.getInstance().addMessageDB(msg);
                        isAddEvent = true;
                    }
                }
                if (isAddEvent) {
                    msgInfos.add(0, eventMsg);
                    UdeskDBManager.getInstance().addMessageDB(eventMsg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msgInfos;
    }

    public static int[] getImageWidthHeight(int[] rect,int width){
        try {
            int sampleSize = 1;
            int originWidth = rect[0];
            int originHeight = rect[1];
            float defaultHeight = 240f;
            float defaultWidth = 160f;
            if (width > 0 && originWidth > width) {
                sampleSize = (int) (rect[0] / width);
            } else {
                if (originWidth > originHeight && originWidth > defaultWidth) {
                    sampleSize = (int) (rect[0] / defaultWidth);
                } else if (originWidth < originHeight && originHeight > defaultHeight) {
                    sampleSize = (int) (rect[1] / defaultHeight);
                }
            }
            if (sampleSize <= 0) {
                rect[0] = (int) defaultWidth;
                rect[1] = (int) defaultHeight;
            } else if (sampleSize > 1) {
                rect[0] = originWidth /sampleSize;
                rect[1]=originHeight/sampleSize;
            }
            return new int[]{rect[0],rect[1]};
        }catch (Exception e){
            e.printStackTrace();
        }
        return new int[]{0,0};
    }

    /**
     * 图片按比例大小压缩方法（根据bitmap图片压缩）
     *
     * @param image
     * @return
     */
    public static Bitmap compressRatio(Bitmap image) {

        if (image == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(byteArrayInputStream, null, options);
        options.inJustDecodeBounds = false;
        int originWidth = options.outWidth;
        int originHeight = options.outHeight;
        float defaultHeight = 240f;
        float defaultWidth = 160f;
        int sampleSize = 1;
        if (originWidth > originHeight && originWidth > defaultWidth) {
            sampleSize = (int) (options.outWidth / defaultWidth);
        } else if (originWidth < originHeight && originHeight > defaultHeight) {
            sampleSize = (int) (options.outHeight / defaultHeight);
        }
        if (sampleSize <= 0) {
            sampleSize = 1;
        }
        options.inSampleSize = sampleSize;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        byteArrayInputStream = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(byteArrayInputStream, null, options);
        return bitmap;
    }
    public static Bitmap compressRatio(String url, int width) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(UdeskUtil.getPathByUrl(getContext(), UdeskConst.FileImg,
                    url), options);
            options.inJustDecodeBounds = false;
            int originWidth = options.outWidth;
            int originHeight = options.outHeight;
            float defaultHeight = 240f;
            float defaultWidth = 160f;
            int sampleSize = 1;
            if (width > 0 && originWidth > width) {
                sampleSize = (int) (originWidth / width);
            }else {
                if (originWidth > originHeight && originWidth > defaultWidth) {
                    sampleSize = (int) (originWidth / defaultWidth);
                } else if (originWidth < originHeight && originHeight > defaultHeight) {
                    sampleSize = (int) (originHeight / defaultHeight);
                }
            }
            if (sampleSize <= 0) {
                sampleSize = 1;
            }
            options.inSampleSize = sampleSize;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeFile(UdeskUtil.getPathByUrl(getContext(), UdeskConst.FileImg,
                    url), options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 按质量压缩，并将图像生成指定的路径
     */
    public static Bitmap compressImage(Context context, String url, Bitmap image) {
        if (image == null || TextUtils.isEmpty(url)) {
            return null;
        }
        FileOutputStream fos = null;
        try {
            String outpath = UdeskUtil.getPathByUrl(context, UdeskConst.FileImg, url);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            int options = 100;
            image.compress(Bitmap.CompressFormat.JPEG, options, os);
            while (os.toByteArray().length / 1024 > 50) {
                os.reset();
                options -= 10;
                image.compress(Bitmap.CompressFormat.JPEG, options, os);
            }
            fos = new FileOutputStream(outpath);
            fos.write(os.toByteArray());
            fos.flush();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(os.toByteArray());
            return BitmapFactory.decodeStream(inputStream, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }

    public static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
    }

    public static List<Integer> getRandomNum(int count, int size) {
        Random random = new Random();
        List<Integer> list = new ArrayList<>();
        while (list.size() < count) {
            int num = random.nextInt(size);
            if (!list.contains(num)) {
                list.add(num);
            }
        }
        return list;
    }

    public static SpannableString setSpan(String info, String color, int bold) {
        SpannableString spannableString = new SpannableString(info);
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor(color)), 0, info.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        if (bold == 1) {
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, info.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }
}
