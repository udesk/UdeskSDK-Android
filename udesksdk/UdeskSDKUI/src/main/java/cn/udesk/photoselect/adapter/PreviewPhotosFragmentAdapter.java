package cn.udesk.photoselect.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.photoselect.SelectResult;

/**
 * 预览所有选中图片集合的适配器
 * Created by user on 2018/3/9.
 */

public class PreviewPhotosFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private OnClickListener listener;
    private int checkedPosition = -1;

    public PreviewPhotosFragmentAdapter(Context context, OnClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.udesk_privew_selected_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final ViewHolder contentHolder = (ViewHolder) holder;
        try {
            String path = SelectResult.getPhotoPath(position);
            String type = SelectResult.getPhotoType(position);
            final int mediaMimeType = UdeskUtil.isPictureType(type);
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.udesk_video_icon);
            UdeskUtil.modifyTextViewDrawable(contentHolder.tv_duration, drawable, 0);
            contentHolder.tv_duration.setVisibility(mediaMimeType == UdeskUtil.TYPE_SHORT_VIDEO ? View.VISIBLE : View.GONE);
            UdeskUtil.loadViewBySize(context, contentHolder.iv_picture, path, UdeskUtil.dip2px(context, 60), UdeskUtil.dip2px(context, 60));
            if (checkedPosition == position) {
                contentHolder.v_selector.setVisibility(View.VISIBLE);
            } else {
                contentHolder.v_selector.setVisibility(View.GONE);
            }

            contentHolder.iv_picture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onPhotoClick(position);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return SelectResult.count();
    }

    public void setChecked(int position) {
        if (checkedPosition == position) {
            return;
        }
        checkedPosition = position;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_picture;
        TextView tv_duration;
        View contentView, v_selector;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            iv_picture =  itemView.findViewById(R.id.udesk_iv_picture);
            tv_duration = (TextView) itemView.findViewById(R.id.udesk_duration);
            v_selector = itemView.findViewById(R.id.udesk_v_selector);
        }
    }

    public interface OnClickListener {
        void onPhotoClick(int position);
    }

}
