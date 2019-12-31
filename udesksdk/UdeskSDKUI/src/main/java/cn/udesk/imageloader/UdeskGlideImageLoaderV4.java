package cn.udesk.imageloader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import cn.udesk.UdeskUtil;

public class UdeskGlideImageLoaderV4 extends UdeskImageLoader {

    private void displayImage(Context context, final ImageView imageView, final Uri uri, RequestOptions options, final UdeskDisplayImageListener displayImageListener) {
        try {
            Glide.with(context).load(uri).apply(options).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    if (displayImageListener != null) {
                        displayImageListener.onSuccess(imageView, uri, resource.getIntrinsicWidth(), resource.getIntrinsicHeight());
                    }
                    return false;
                }
            }).into(imageView);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadDontAnimateImage(Context context, ImageView imageView, Uri uri, int loadingResId, int failResId, int width, int height, UdeskDisplayImageListener displayImageListener) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(loadingResId)
                    .error(failResId)
                    .dontAnimate()
                    .override(width, height);
            displayImage(context, imageView, uri, options, displayImageListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadImage(Context context, ImageView imageView, Uri uri, int loadingResId, int failResId, int width, int height, UdeskDisplayImageListener displayImageListener) {
        try {
            RequestOptions options = new RequestOptions()
                    .placeholder(loadingResId)
                    .error(failResId)
                    .override(width, height);
            displayImage(context, imageView, uri, options, displayImageListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadImageFile(Context context, final Uri uri, final UdeskDownloadImageListener udeskDownloadImageListener) {
        try {
            Glide.with(context.getApplicationContext()).load(uri).into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    if (udeskDownloadImageListener != null) {
                        udeskDownloadImageListener.onSuccess(uri, UdeskUtil.drawableToBitmap(resource));
                    }
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    if (udeskDownloadImageListener != null) {
                        udeskDownloadImageListener.onFailed(uri);
                    }

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
