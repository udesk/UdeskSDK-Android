package cn.udesk.imageloader;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;


public class UdeskGlideImageLoaderV3 extends UdeskImageLoader{

    @Override
    public void loadDontAnimateImage(Context context, final ImageView imageView, final Uri uri, int loadingResId, int failResId, int width, int height, final UdeskDisplayImageListener displayImageListener) {
        try {
            DrawableRequestBuilder<Uri> builder = Glide.with(context).load(uri).placeholder(loadingResId).error(failResId).dontAnimate();
            if (width != 0 && height != 0){
                builder.override(width, height);
            }
            builder.listener(new RequestListener<Uri, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
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
    public void loadImage(Context context, final ImageView imageView, final Uri uri, int loadingResId, int failResId, int width, int height, final UdeskDisplayImageListener displayImageListener) {
        try {
            DrawableRequestBuilder<Uri> builder = Glide.with(context).load(uri).placeholder(loadingResId).error(failResId);
            if (width != 0 && height != 0){
                builder.override(width, height);
            }
            builder.listener(new RequestListener<Uri, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
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
    public void loadImageFile(Context context, final Uri uri, final UdeskDownloadImageListener udeskDownloadImageListener) {
        try {

            Glide.with(context).load(uri).asBitmap().into(new SimpleTarget<Bitmap>(){

                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    if (udeskDownloadImageListener !=null){
                        udeskDownloadImageListener.onSuccess(uri,resource);
                    }
                }

                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
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
