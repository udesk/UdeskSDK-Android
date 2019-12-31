package cn.udesk.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

public abstract class UdeskImageLoader {

    public abstract void loadDontAnimateImage(Context context, ImageView imageView, Uri uri, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, UdeskDisplayImageListener displayImageListener);

    public abstract void loadImage(Context context, ImageView imageView, Uri uri, @DrawableRes int loadingResId, @DrawableRes int failResId, int width, int height, UdeskDisplayImageListener displayImageListener);

    public abstract void loadImageFile(Context context, Uri uri, UdeskDownloadImageListener udeskDownloadImageListener);

    public interface UdeskDisplayImageListener {
        void onSuccess(View view, Uri uri, int width, int height);
    }

    public interface UdeskDownloadImageListener {
        void onSuccess(Uri uri, Bitmap bitmap);

        void onFailed(Uri uri);
    }
}
