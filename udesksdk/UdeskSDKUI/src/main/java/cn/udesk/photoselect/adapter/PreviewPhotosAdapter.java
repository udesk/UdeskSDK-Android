package cn.udesk.photoselect.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.OnScaleChangedListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.photoselect.PictureVideoPlayActivity;
import cn.udesk.photoselect.entity.LocalMedia;
import udesk.core.UdeskConst;

/**
 * Created by user on 2018/3/8.
 */

public class PreviewPhotosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<LocalMedia> images = new ArrayList<LocalMedia>();
    private OnClickListener listener;
    int disPlayWidth;
    int disPlayHeghit;

    public interface OnClickListener {
        void onPhotoClick();

        void onPhotoScaleChanged();
    }

    public PreviewPhotosAdapter(Context context, List<LocalMedia> images, OnClickListener listener, int width,
                                int heghit) {
        this.context = context;
        this.images = images;
        this.listener = listener;
        this.disPlayHeghit = heghit;
        this.disPlayWidth = width;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.udesk_item_preview_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ViewHolder contentHolder = (ViewHolder) holder;
        try {
            final LocalMedia image = images.get(position);
            if (image != null) {
                final int mediaMimeType = UdeskUtil.isPictureType(image.getPictureType());
                UdeskUtil.loadImage(context, contentHolder.iv_picture, image.getPath());
                contentHolder.video_tip.setVisibility(mediaMimeType == UdeskUtil.TYPE_SHORT_VIDEO ? View.VISIBLE : View.GONE);
                contentHolder.iv_picture.setOnPhotoTapListener(new OnPhotoTapListener() {
                    @Override
                    public void onPhotoTap(ImageView view, float x, float y) {
                        listener.onPhotoClick();
                    }
                });
                contentHolder.iv_picture.setOnScaleChangeListener(new OnScaleChangedListener() {
                    @Override
                    public void onScaleChange(float scaleFactor, float focusX, float focusY) {
                        listener.onPhotoScaleChanged();
                    }
                });

                contentHolder.video_tip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setClass(context, PictureVideoPlayActivity.class);
                        Bundle data = new Bundle();
                        data.putString(UdeskConst.PREVIEW_Video_Path, image.getPath());
                        intent.putExtras(data);
                        context.startActivity(intent);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (images != null) {
            return images.size();
        }
        return 0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        PhotoView iv_picture;
        ImageView video_tip;

        View contentView;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            iv_picture = itemView.findViewById(R.id.udesk_iv_photo);
            video_tip = itemView.findViewById(R.id.video_tip);

        }
    }
}
