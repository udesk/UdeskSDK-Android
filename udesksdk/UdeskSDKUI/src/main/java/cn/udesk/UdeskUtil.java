package cn.udesk;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.udesk.activity.UdeskZoomImageActivty;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.provider.UdeskFileProvider;
import me.relex.photodraweeview.PhotoDraweeView;
import udesk.core.UdeskCoreConst;

public class UdeskUtil {
    public static final String ImgFolderName = "UDeskIMg";
    public static final String AudioFolderName = "UDeskAudio";


    /**
     * 检查网络是否是GPRS连接
     */
    public static boolean isGpsNet(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

        if (gprs == NetworkInfo.State.CONNECTED || gprs == NetworkInfo.State.CONNECTING) {
            return true;
        }
        return false;

    }

    public static File cameaFile(Context context) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            return getOutputMediaFile(context, "IMG_" + timeStamp + ".jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public static Uri getOutputMediaFileUri(Context context, File cameaFile) {
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                return UdeskFileProvider.getUriForFile(context, getFileProviderName(context), cameaFile);
            } else {
                if (cameaFile != null) {
                    return Uri.fromFile(cameaFile);
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        }

    }
    public final static String getFileProviderName(Context context) {
        return context.getPackageName() + ".fileprovider";
    }

    /**
     * 提供的Uri 解析出文件绝对路径
     *
     * @param uri
     * @return
     */
    public static String parseOwnUri(Uri uri, Context context,File cameraFile) {
        if (uri == null) return "";
        String path;
        if (TextUtils.equals(uri.getAuthority(), getFileProviderName(context))) {
            if (cameraFile != null){
                return  cameraFile.getAbsolutePath();
            }else{
                path = new File(Environment.getExternalStorageDirectory(), uri.getPath().replace("my_external/", "")).getAbsolutePath();
            }
        } else {
            path = uri.getEncodedPath();
        }
        return path;
    }

    public static File getOutputMediaFile(Context context, String mediaName) {
        File mediaFile = null;
        try {
            File mediaStorageDir = null;
            try {
                mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), ImgFolderName);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }

            mediaFile = new File(mediaStorageDir.getPath() + File.separator + mediaName);
        } catch (Exception e) {
            return null;
        }
        return mediaFile;
    }

    public static boolean isExitFileByPath(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static boolean isExitFileByMsgIdAndUrl(String msgId, String url) {
        try {
            File file = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), buildFileName(msgId, url));
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getFileSizeByLoaclPath(String filePath) {
        try {
            File file = new File(filePath);
            if (file != null && file.exists()) {
                long blockSize = getFileSize(file);
                return formetFileSize(blockSize);
            }
        } catch (Exception e) {
            return "0B";
        }
        return "0B";
    }


    public static String getFileSizeByMsgIdAndUrl(String msgId, String url) {
        File file = getLoaclpathByMsgIdAndUrl(msgId, url);
        if (file == null) {
            return "0B";
        } else {
            long blockSize = getFileSize(file);
            return formetFileSize(blockSize);
        }
    }

    public static long getFileSize(File file) {
        long size = 0;
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
        if (fileS < 1024) {
            DecimalFormat dfb = new DecimalFormat("#");
            fileSizeString = dfb.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        }

        return fileSizeString;
    }


    public static File getLoaclpathByMsgIdAndUrl(String msgId, String url) {
        try {
            File file = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), buildFileName(msgId, url));
            if (file.exists()) {
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String buildFileName(String msgId, String url) {
        return msgId + "_" + getFileName(url);
    }

    public static String getFileName(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public static boolean audiofileIsDown(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        File mediaStorageDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_RINGTONES),
                AudioFolderName);
        if (!mediaStorageDir.exists()) {
            return false;
        }
        String filepath = mediaStorageDir.getPath() + File.separator + fileName;
        File file = new File(filepath);
        return file.exists();
    }


    public static String getDownAudioPath(Context context, String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        File mediaStorageDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_RINGTONES),
                AudioFolderName);

        return mediaStorageDir.getPath() + File.separator + fileName;
    }


    public static String getOutputAudioPath(Context context) {
        return getOutputAudioPath(context, "audio_"
                + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
    }


    public static File getOutputAudioFile(Context context, String mediaName) {
        String path = getOutputAudioPath(context, mediaName);
        if (TextUtils.isEmpty(path)) {
            return null;
        } else {
            return new File(path);
        }
    }

    public static String getOutputAudioPath(Context context, String mediaName) {
        File mediaStorageDir = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_RINGTONES),
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


    public static String getFormUrlPara(Context context) {
        StringBuilder builder = new StringBuilder();
        builder.append("?sdk_token=").append(UdeskSDKManager.getInstance().getSdkToken(context))
                .append("&sdk_version=").append(UdeskCoreConst.sdkversion).append("&app_id=").append(UdeskSDKManager.getInstance().getAppId(context));
        if (!isZh(context)) {
            builder.append("&language=en-us");
        }
        Map<String, String> userinfo = UdeskBaseInfo.userinfo;
        Map<String, String> textField = UdeskBaseInfo.textField;
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
        return builder.toString();
    }




    public static int getDisplayWidthPixels(Activity activity) {
        DisplayMetrics dMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
        return dMetrics.widthPixels;
    }

    public static String formatLongTypeTimeToString(Context context, long time) {
        long OFFSET_DAY = 3600 * 24;
        String timeYes = context.getString(R.string.udesk_im_time_format_yday);
        String timeQt = context.getString(R.string.udesk_im_time_format_dby);
        String timeDate = "yyyy/MM/dd";
        Calendar calendar = Calendar.getInstance();
        StringBuilder build = new StringBuilder();
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
                timeDate = "MM月dd日";
                sdf.applyLocalizedPattern(timeDate);
                return sdf.format(time);
            }
        }

        return build.toString();
    }


    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.endsWith("zh");
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

    public static String getAppName(Context context) {
        String appName = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = null;
            info = manager.getPackageInfo(context.getPackageName(), 0);
            appName = info.applicationInfo.loadLabel(manager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defValue;
    }

    public static int objectToInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Integer) {
            return (int) obj;
        }
        if (obj instanceof Double) {
            return Double.valueOf((Double) obj).intValue();
        }
        if (obj instanceof Float) {
            return Float.valueOf((Float) obj).intValue();
        }
        if (isNumeric(obj.toString())) {
            return toInt(obj.toString(), 0);
        }
        return 0;
    }

    //
    public static boolean isNumeric(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static String objectToString(Object obj) {
        if (obj == null) {
            return "";
        }
        String string = "";
        if (obj instanceof String) {
            string = (String) obj;
        }
        try {
            string = String.valueOf(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (string.equals("null")) {
            string = "";
        }
        return string;
    }

    public static boolean objectToBoolean(Object obj) {
        if (obj instanceof Boolean) {
            return (boolean) obj;
        }
        return false;
    }

    public static String parseEventTime(String strTime) {
        if (strTime == null) {
            return "";
        }
        if (strTime.length() <= 10) {
            return strTime;
        }
        long time = stringToLong(strTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    public static String parseEventTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return sdf.format(new Date(time));
    }

    public static long stringToLong(String strTime) {
        Date date = stringToDate(strTime); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }

    public static Date stringToDate(String strTime) {
        SimpleDateFormat formatter = null;
        Date date = null;
        if (strTime.contains("T") && strTime.contains("+")) {
//            2017-02-14T00:00:00+08:00
//            2017-04-12T20:22:40.000+08:00
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
            e.printStackTrace();
        }
        return date;
    }

    public static long dateToLong(Date date) {
        return date.getTime();
    }

    // 暂停图片请求
    public static void imagePause() {
        Fresco.getImagePipeline().pause();
    }

    // 恢复图片请求
    public static void imageResume() {
        Fresco.getImagePipeline().resume();
    }

    /**
     * 获取图像的宽高
     **/

    public static int[] getImageWH(String path) {
        int[] wh = {-1, -1};
        if (path == null) {
            return wh;
        }
        File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                InputStream is = new FileInputStream(path);
                BitmapFactory.decodeStream(is, null, options);
                wh[0] = options.outWidth;
                wh[1] = options.outHeight;
            } catch (Exception e) {

            }
        }
        return wh;
    }

    public static File getFileFromDiskCache(Uri url) {
        File localFile = null;
        if (url != null) {
            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(url).build();
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(imageRequest, new Object());
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getSmallImageFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            }
        }
        return localFile;
    }

    public static void loadImage(final PhotoDraweeView mPhotoDraweeView,
                                 Uri uri) {
        PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
        controller.setUri(uri);
        controller.setAutoPlayAnimations(true);
        controller.setOldController(mPhotoDraweeView.getController());
        controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (imageInfo == null || mPhotoDraweeView == null) {
                    return;
                }
                mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
            }
        });
        mPhotoDraweeView.setController(controller.build());
    }

    public static void loadHeadView(Context context,SimpleDraweeView simpleDraweeView, Uri httpUri) {
        //初始化圆角圆形参数对象
        RoundingParams rp = new RoundingParams();
        //设置图像是否为圆形
        rp.setRoundAsCircle(true);

        final GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(context.getResources())
                .setRoundingParams(rp)
                .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(httpUri)
                .setTapToRetryEnabled(true)
                .setOldController(simpleDraweeView.getController())
                .build();
        simpleDraweeView.setHierarchy(hierarchy);
        simpleDraweeView.setController(controller);
    }

    public static void loadFileFromSdcard(final Context context, final SimpleDraweeView draweeView, Uri loackUri, final int reqWidth, final int reqHeight) {
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(loackUri)
                .setRotationOptions(RotationOptions.autoRotate())
                .setLocalThumbnailPreviewsEnabled(true)
                .setResizeOptions(new ResizeOptions(dip2px(context,140), dip2px(context,220)))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(draweeView.getController())
                .setTapToRetryEnabled(true)
                .setControllerListener(new BaseControllerListener<ImageInfo>() {
                    @Override
                    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable anim) {
                        if (imageInfo == null) {
                            return;
                        }

                        ViewGroup.LayoutParams layoutParams = draweeView.getLayoutParams();
                        int width = reqWidth;
                        int height = reqHeight;
                        int imgWidth = dip2px(context,140) ;
                        int imgHight = dip2px(context,220);
                        int bitScalew = getRatioSize(width, height, imgHight, imgWidth);
                        layoutParams.height = height / bitScalew;
                        layoutParams.width = width / bitScalew;
                        draweeView.requestLayout();
                    }
                })
                .setAutoPlayAnimations(true)
                .build();
        draweeView.setController(controller);
    }

    public static void loadImageView(final Context context, final SimpleDraweeView simpleDraweeView, Uri httpUri) {

        final ViewGroup.LayoutParams layoutParams = simpleDraweeView.getLayoutParams();
        ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable anim) {
                if (imageInfo == null) {
                    return;
                }
                int height = imageInfo.getHeight();
                int width = imageInfo.getWidth();
                int imgWidth = dip2px(context,140) ;
                int imgHight = dip2px(context,220);
                int bitScalew = getRatioSize(width, height, imgHight, imgWidth);
                layoutParams.height = height / bitScalew;
                layoutParams.width = width / bitScalew;
                simpleDraweeView.setLayoutParams(layoutParams);
                simpleDraweeView.invalidate();
            }

            @Override
            public void onIntermediateImageSet(String id, ImageInfo imageInfo) {

            }

            @Override
            public void onFailure(String id, Throwable throwable) {
                throwable.printStackTrace();
            }
        };
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(httpUri).
                setProgressiveRenderingEnabled(true).
                setResizeOptions(new ResizeOptions(dip2px(context,140), dip2px(context,220))).
                setRotationOptions(RotationOptions.disableRotation()).build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setTapToRetryEnabled(true)
                .setOldController(simpleDraweeView.getController())
                .setAutoPlayAnimations(true)
                .setControllerListener(controllerListener)
                .build();
        simpleDraweeView.setController(controller);
    }

    public static void loadNoChangeView(SimpleDraweeView simpleDraweeView, Uri httpUri) {
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(httpUri)
                .setTapToRetryEnabled(true)
                .setOldController(simpleDraweeView.getController())
                .build();
        simpleDraweeView.setController(controller);
    }



    public static int getRatioSize(int bitWidth, int bitHeight, int imageHeight, int imageWidth) {

        // 缩放比
        int ratio = 1;
        // 缩放比,由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        if (bitWidth > bitHeight && bitWidth > imageWidth) {
            // 如果图片宽度比高度大,以宽度为基准
            ratio = bitWidth / imageWidth;
        } else if (bitWidth < bitHeight && bitHeight > imageHeight) {
            // 如果图片高度比宽度大，以高度为基准
            ratio = bitHeight / imageHeight;
        }
        // 最小比率为1
        if (ratio <= 0)
            ratio = 1;
        return ratio;
    }

    public static int dip2px(Context context, int dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int getScreenWith(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getWidth();
    }

    public static int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        return display.getHeight();
    }

    public static String getFilePath(Activity context, Uri uri) {
        String path = "";
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor = null;
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    path = getPath(context, uri);
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
                    cursor = null;
                }
            }
        }
        if (TextUtils.isEmpty(path.trim())) {
            path = uri.getPath();
        }

        return path;
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
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
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

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
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }
        }

        return null;
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
        } finally {
            if (cursor != null)
                cursor.close();
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
        if (hasExternalCacheDir())
            return context.getExternalCacheDir();

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
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
    /* 获取文件的后缀名*/
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) {
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
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

}
