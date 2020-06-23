package cn.udesk.photoselect.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.photoselect.SelectResult;
import cn.udesk.photoselect.entity.LocalMedia;
import udesk.core.UdeskConst;

/**
 * Created by user on 2018/3/6.
 */

public class PhotosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private OnPhotoSelectChangedListener imageSelectChangedListener;
    private List<LocalMedia> images = new ArrayList<LocalMedia>();
    int disPlayWidth;
    int disPlayHeghit;


    public PhotosAdapter(Context context, OnPhotoSelectChangedListener listener, int width,
                         int heghit) {
        this.context = context;
        this.imageSelectChangedListener = listener;
        this.disPlayHeghit = heghit;
        this.disPlayWidth = width;
    }

    public void bindImagesData(List<LocalMedia> images) {

        if (images != null) {
            this.images = images;
            notifyDataSetChanged();
        }

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.udesk_photo_select_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder contentHolder = (ViewHolder) holder;
        final LocalMedia image = images.get(position);
        try {
            if (image != null) {
                updateSelector(contentHolder.check, contentHolder.v_selector, SelectResult.isSelected(image), image);
                contentHolder.ll_check.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!image.isSelected() && SelectResult.count() >= UdeskConst.count) {
                            imageSelectChangedListener.onSelectorOutOfMax();
                            return;
                        }
                        image.setSelected(!image.isSelected());
                        if (image.isSelected()) {
                            SelectResult.addPhoto(image);
                        } else {
                            SelectResult.removePhoto(image);
                        }

                        notifyDataSetChanged();
                        imageSelectChangedListener.onSelectorChanged();

                    }
                });
                final String path = image.getPath();
                final String pictureType = image.getPictureType();
                final int mediaMimeType = UdeskUtil.isPictureType(pictureType);
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.udesk_video_icon);
                UdeskUtil.modifyTextViewDrawable(contentHolder.tv_duration, drawable, 0);
                contentHolder.tv_duration.setVisibility(mediaMimeType == UdeskUtil.TYPE_SHORT_VIDEO ? View.VISIBLE : View.GONE);
                long duration = image.getDuration();
                contentHolder.tv_duration.setText(UdeskUtil.timeParse(duration));
                if (disPlayWidth > 0) {
                    UdeskUtil.loadDontAnimateAndResizeImage(context, contentHolder.iv_picture, path,
                            disPlayWidth / 4, UdeskUtil.dip2px(context, 100));
                } else {
                    UdeskUtil.loadDontAnimateImage(context, contentHolder.iv_picture, path);
                }
                contentHolder.iv_picture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        imageSelectChangedListener.onPictureClick(image, position);
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


    private void updateSelector(TextView tvSelector, View v_selector, boolean selected, LocalMedia localMedia) {
        if (selected) {
            v_selector.setVisibility(View.VISIBLE);
            String number = SelectResult.getSelectorNumber(localMedia);
            tvSelector.setText(number);
            tvSelector.setBackgroundResource(R.drawable.udesk_checkphoto_bg_select_true);
        } else {
            tvSelector.setBackgroundResource(R.drawable.udesk_checkphoto_bg_select_false);
            tvSelector.setText("");
            v_selector.setVisibility(View.GONE);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_picture;
        TextView check;
        TextView tv_duration;
        View contentView, ll_check, v_selector;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            iv_picture = itemView.findViewById(R.id.udesk_iv_picture);
            tv_duration = (TextView) itemView.findViewById(R.id.udesk_duration);
            check = (TextView) itemView.findViewById(R.id.udesk_check);
            v_selector = itemView.findViewById(R.id.udesk_v_selector);
            ll_check = itemView.findViewById(R.id.ll_check);
        }
    }


    public interface OnPhotoSelectChangedListener {

        /**
         *
         */
        void onSelectorChanged();

        /**
         * 图片预览回调
         *
         * @param media
         * @param position
         */
        void onPictureClick(LocalMedia media, int position);

        /**
         * 达到最大选项
         */
        void onSelectorOutOfMax();
    }
}
