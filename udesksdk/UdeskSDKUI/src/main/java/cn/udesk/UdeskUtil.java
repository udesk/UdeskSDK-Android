package cn.udesk;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.LocaleList;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.datasource.DataSource;
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
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.provider.UdeskFileProvider;
import me.relex.photodraweeview.PhotoDraweeView;
import udesk.core.JsonObjectUtils;
import udesk.core.LocalManageUtil;
import udesk.core.UdeskConst;
import udesk.core.utils.UdeskUtils;

public class UdeskUtil {

    /**
     * 检查网络是否是连接
     */
    public static boolean isGpsNet(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

        return gprs == NetworkInfo.State.CONNECTED || gprs == NetworkInfo.State.CONNECTING;

    }

    public static File cameaFile(Context context) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File imageFile = new File(UdeskUtils.getDirectoryPath(context, UdeskConst.FileImg) + File.separator + timeStamp + UdeskConst.IMG_SUF);
            return imageFile;
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

    public static String getFileProviderName(Context context) {
        return context.getPackageName() + ".udeskfileprovider";
    }

    /**
     * 提供的Uri 解析出文件绝对路径
     *
     * @param uri
     * @return
     */
    public static String parseOwnUri(Uri uri, Context context, File cameraFile) {
        if (uri == null) return "";
        String path = "";
        try {
            if (TextUtils.equals(uri.getAuthority(), getFileProviderName(context))) {
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

    public static String getFormUrlPara(Context context) {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append("?sdk_token=").append(UdeskSDKManager.getInstance().getSdkToken(context))
                    .append("&sdk_version=").append(UdeskConst.sdkversion).append("&app_id=").append(UdeskSDKManager.getInstance().getAppId(context));
            if (!isZh(context)) {
                builder.append("&language=en-us");
            }
            Map<String, String> userinfo = UdeskSDKManager.getInstance().getUdeskConfig().defualtUserInfo;
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

    /**
     * 图片是否已经存在了
     */
    public static boolean isCached(Context context, Uri uri) {
        try {
            ImagePipeline imagePipeline = Fresco.getImagePipeline();
            DataSource<Boolean> dataSource = imagePipeline.isInDiskCache(uri);
            if (dataSource == null) {
                return false;
            }
            ImageRequest imageRequest = ImageRequest.fromUri(uri);
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                    .getEncodedCacheKey(imageRequest, context);
            BinaryResource resource = ImagePipelineFactory.getInstance()
                    .getMainFileCache().getResource(cacheKey);
            return resource != null && dataSource.getResult() != null && dataSource.getResult();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 本地缓存文件
     */
    public static File getFileFromDiskCache(Context context, Uri uri) {
        try {
            if (!isCached(context, uri))
                return null;
            ImageRequest imageRequest = ImageRequest.fromUri(uri);
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                    .getEncodedCacheKey(imageRequest, context);
            BinaryResource resource = ImagePipelineFactory.getInstance()
                    .getMainFileCache().getResource(cacheKey);
            return ((FileBinaryResource) resource).getFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void loadImage(Context context, final PhotoDraweeView mPhotoDraweeView, Uri uri) {

        try {
            File file = getFileFromDiskCache(context, uri);
            if (file != null) {
                uri = Uri.fromFile(file);
            }
            PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
            controller.setUri(uri);
            controller.setAutoPlayAnimations(true);
            controller.setOldController(mPhotoDraweeView.getController());
            controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    if (imageInfo == null) {
                        return;
                    }
                    mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }
            });
            mPhotoDraweeView.setController(controller.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadHeadView(Context context, SimpleDraweeView simpleDraweeView, Uri httpUri) {

        try {
            File file = getFileFromDiskCache(context, httpUri);
            if (file != null) {
                httpUri = Uri.fromFile(file);
            }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadFileFromSdcard(final Context context, final SimpleDraweeView draweeView, Uri loackUri, final int reqWidth, final int reqHeight) {

        try {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(loackUri)
                    .setRotationOptions(RotationOptions.autoRotate())
                    .setLocalThumbnailPreviewsEnabled(true)
                    .setResizeOptions(new ResizeOptions(dip2px(context, 140), dip2px(context, 220)))
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
                            int imgWidth = dip2px(context, 140);
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

                            draweeView.requestLayout();
                        }
                    })
                    .setAutoPlayAnimations(true)
                    .build();
            draweeView.setController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadImageView(final Context context, final SimpleDraweeView simpleDraweeView, Uri httpUri) {

        try {
            File file = getFileFromDiskCache(context, httpUri);
            if (file != null) {
                httpUri = Uri.fromFile(file);
            }
            final ViewGroup.LayoutParams layoutParams = simpleDraweeView.getLayoutParams();
            ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable anim) {
                    if (imageInfo == null) {
                        return;
                    }
                    int height = imageInfo.getHeight();
                    int width = imageInfo.getWidth();
                    int imgWidth = dip2px(context, 140);
                    int imgHight = dip2px(context, 220);
                    double bitScalew = getRatioSize(width, height, imgHight, imgWidth);
                    if (bitScalew >= 1) {
                        layoutParams.height = (int) (height / bitScalew);
                        layoutParams.width = (int) (width / bitScalew);
                    } else if (bitScalew >= 0.5) {
                        layoutParams.height = height;
                        layoutParams.width = width;
                    } else {
                        layoutParams.height = imgWidth / 2;
                        layoutParams.width = imgWidth / 2;
                    }
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
                    setResizeOptions(new ResizeOptions(dip2px(context, 140), dip2px(context, 220))).
                    setRotationOptions(RotationOptions.disableRotation()).build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setTapToRetryEnabled(true)
                    .setOldController(simpleDraweeView.getController())
                    .setAutoPlayAnimations(true)
                    .setControllerListener(controllerListener)
                    .build();
            simpleDraweeView.setController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadNoChangeView(Context context, SimpleDraweeView simpleDraweeView, Uri httpUri) {
        try {
            File file = getFileFromDiskCache(context, httpUri);
            if (file != null) {
                httpUri = Uri.fromFile(file);
            }
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(httpUri)
                    .setTapToRetryEnabled(true)
                    .setOldController(simpleDraweeView.getController())
                    .build();
            simpleDraweeView.setController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void loadViewBySize(Context context, SimpleDraweeView simpleDraweeView, Uri httpUri, int width, int height) {
        try {
            File file = getFileFromDiskCache(context, httpUri);
            if (file != null) {
                httpUri = Uri.fromFile(file);
            }
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(httpUri)
                    //根据View的尺寸放缩图片
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();


            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(simpleDraweeView.getController())
                    .setImageRequest(request)
                    .setTapToRetryEnabled(true)
                    .build();
            simpleDraweeView.setController(controller);
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

    public static String getPath(final Context context, final Uri uri) {

        try {
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
        } catch (NumberFormatException e) {
            e.printStackTrace();
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
        if (end.equals("")) return type;
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

    public final static int TYPE_IMAGE = 1;
    public final static int TYPE_VIDEO = 2;
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
                return TYPE_VIDEO;
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

    public static void loadEmojiView(Context context, SimpleDraweeView simpleDraweeView, Uri uri, final int reqWidth, final int reqHeight) {


        try {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setResizeOptions(new ResizeOptions(reqWidth, dip2px(context, reqHeight)))
                    .build();

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setTapToRetryEnabled(true)
                    .setOldController(simpleDraweeView.getController())
                    .build();

            simpleDraweeView.setController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFileName(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        return url.substring(url.lastIndexOf("/") + 1);
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


    public static void initCrashReport(Context context) {
        try {
            CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
            strategy.setAppVersion(UdeskConst.sdkversion + UdeskUtil.getAppName(context));
            CrashReport.initCrashReport(context, UdeskConst.buglyAppid, false, strategy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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

    public static void frescoInit(final Context context) {
        try {
            if (!Fresco.hasBeenInitialized()) {
                final int MAX_HEAP_SIZE = (int) Runtime.getRuntime().maxMemory();
                final int MAX_DISK_CACHE_SIZE = 400 * ByteConstants.MB;
                final int MAX_MEMORY_CACHE_SIZE = MAX_HEAP_SIZE / 3;
                final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
                        MAX_MEMORY_CACHE_SIZE,
                        Integer.MAX_VALUE,
                        MAX_MEMORY_CACHE_SIZE,
                        Integer.MAX_VALUE,
                        Integer.MAX_VALUE);

                DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                        .setMaxCacheSize(MAX_DISK_CACHE_SIZE)//最大缓存
                        .setBaseDirectoryName("udesk_im_sdk")//子目录
                        .setBaseDirectoryPathSupplier(new Supplier<File>() {
                            @Override
                            public File get() {
                                return UdeskUtil.getExternalCacheDir(context);
                            }
                        })
                        .build();
                ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
                        .setBitmapMemoryCacheParamsSupplier(
                                new Supplier<MemoryCacheParams>() {
                                    public MemoryCacheParams get() {
                                        return bitmapCacheParams;
                                    }
                                })
                        .setMainDiskCacheConfig(diskCacheConfig)
                        .setDownsampleEnabled(true)
                        .setBitmapsConfig(Bitmap.Config.RGB_565)
                        .build();

                Fresco.initialize(context, config);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Fresco.initialize(context);
        }
    }
    public static HashMap<String,String> buildGetParams(String sdkToken, String mSecretKey, String appid){
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

}
