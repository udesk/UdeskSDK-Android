package cn.udesk.imageloader;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import cn.udesk.UdeskUtil;

public class UdeskImage {
    private static UdeskImageLoader udeskImageLoader;

    private UdeskImage() {
    }

    private static final UdeskImageLoader getImageLoader() {
        if (udeskImageLoader == null) {
            synchronized (UdeskImage.class) {
                if (udeskImageLoader == null) {
                    if (UdeskUtil.isClassExists("com.bumptech.glide.Glide")) {
                        udeskImageLoader = new UdeskGlideImageLoaderV4();
                    }
                }
            }
        }
        return udeskImageLoader;
    }

    public static void loadDontAnimateAndResizeImage(Context context, ImageView imageView, Uri uri, int loadingResId, int failResId, int width, int height, UdeskImageLoader.UdeskDisplayImageListener displayImageListener) {
        try {
            getImageLoader().loadDontAnimateImage(context, imageView, uri, loadingResId, failResId, width, height, displayImageListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void loadImage(Context context, ImageView imageView, Uri uri, int loadingResId, int failResId, int width, int height, UdeskImageLoader.UdeskDisplayImageListener displayImageListener) {
        try {
            getImageLoader().loadImage(context, imageView, uri, loadingResId, failResId, width, height, displayImageListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static void loadImageFile(Context context, final Uri uri, final UdeskImageLoader.UdeskDownloadImageListener udeskDownloadImageListener) {
        try {
            getImageLoader().loadImageFile(context, uri, udeskDownloadImageListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
