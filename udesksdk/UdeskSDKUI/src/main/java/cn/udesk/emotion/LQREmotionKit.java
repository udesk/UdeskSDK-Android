package cn.udesk.emotion;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.udesk.UdeskUtil;
import cn.udesk.rich.LoaderTask;
import udesk.core.UdeskConst;

/**
 * 表情库Kit
 */

public class LQREmotionKit {

    private static String EMOTION_NAME_IN_ASSETS = "udeskemotion";
    private static Context mContext;
    private static float density;
    private static float scaleDensity;
    private static String EMOTION_PATH;


    private static void getAndSaveParameter(Context context) {
        mContext = context.getApplicationContext();

        DisplayMetrics dm = context.getApplicationContext().getResources().getDisplayMetrics();
        density = dm.density;
        scaleDensity = dm.scaledDensity;
    }

    public static void init(Context context, String path) {
        getAndSaveParameter(context);
        EMOTION_PATH = path;

        //将asset/sticker目录下默认的贴图复制到STICKER_PATH下
        copyStickerToStickerPath(EMOTION_NAME_IN_ASSETS);
    }

    public static void init(final Context context) {
        try {
            LoaderTask.getThreadPoolExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    init(context, UdeskUtil.getDirectoryPath(context, UdeskConst.FileEmotion));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyStickerToStickerPath(String assetsFolderPath) {
        AssetManager assetManager = mContext.getResources().getAssets();
        List<String> srcFile = new ArrayList<>();
        try {
            String[] stickers = assetManager.list(assetsFolderPath);
            for (String fileName : stickers) {
                if (!new File(LQREmotionKit.getEmotionPath(), fileName).exists()) {
                    srcFile.add(fileName);
                }
            }
            if (srcFile.size() > 0) {
                copyToStickerPath(assetsFolderPath, srcFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void copyToStickerPath(final String assetsFolderPath, final List<String> srcFile) {
        LoaderTask.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                AssetManager assetManager = mContext.getResources().getAssets();
                for (String fileName : srcFile) {

                    if (fileName.contains(".")) {//文件
                        InputStream is = null;
                        FileOutputStream fos = null;
                        try {
                            is = assetManager.open(assetsFolderPath + File.separator + fileName);
                            File destinationFile;
                            if (assetsFolderPath.startsWith(EMOTION_NAME_IN_ASSETS + File.separator)) {//递归回来的时候assetsFolderPath可能变为"sticker/tsj"
                                destinationFile = new File(getEmotionPath(), assetsFolderPath.substring(assetsFolderPath.indexOf(File.separator) + 1) + File.separator + fileName);
                            } else {
                                destinationFile = new File(getEmotionPath(), fileName);
                            }
                            fos = new FileOutputStream(destinationFile);
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (is != null) {
                                try {
                                    is.close();
                                    is = null;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    is = null;
                                }
                            }
                            if (fos != null) {
                                try {
                                    fos.close();
                                    fos = null;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    fos = null;
                                }
                            }
                        }
                    } else {//文件夹

                        File dir = new File(getEmotionPath(), fileName);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }

                        copyStickerToStickerPath(assetsFolderPath + File.separator + fileName);
                    }
                }
            }
        });
    }

    public static Context getContext() {
        return mContext;
    }

    public static String getEmotionPath() {
        return EMOTION_PATH;
    }


    public static int dip2px(float dipValue) {
        return (int) (dipValue * density + 0.5f);
    }


}
