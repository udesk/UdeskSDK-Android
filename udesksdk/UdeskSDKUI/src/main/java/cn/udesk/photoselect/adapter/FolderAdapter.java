package cn.udesk.photoselect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.photoselect.entity.LocalMediaFolder;

/**
 * Created by user on 2018/3/6.
 */

public class FolderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<LocalMediaFolder> floders = new ArrayList<LocalMediaFolder>();

    private OnFolderClickListener listener;

    public interface OnFolderClickListener {
        void onFolderItemClick(int position);
    }


    public FolderAdapter(Context context, OnFolderClickListener listener) {
        this.context = context;
        this.listener = listener;

    }

    public void bindFildersData(List<LocalMediaFolder> folders) {

        if (folders != null) {
            this.floders = folders;
            notifyDataSetChanged();
        }

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.udesk_folder_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final ViewHolder contentHolder = (ViewHolder) holder;
        final LocalMediaFolder folder = floders.get(position);
        try {
            if (folder != null) {
                UdeskUtil.loadDontAnimateAndResizeImage(context, contentHolder.iv_picture, folder.getFirstFilePath(),
                        UdeskUtil.dip2px(context, 80), UdeskUtil.dip2px(context, 80));
                contentHolder.name.setText(folder.getName());
                contentHolder.count.setText(String.valueOf(folder.getNum()));
                if (position == 1) {
                    contentHolder.video_tip.setVisibility(View.VISIBLE);
                } else {
                    contentHolder.video_tip.setVisibility(View.GONE);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.onFolderItemClick(position);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (floders != null) {
            return floders.size();
        }
        return 0;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_picture;
        ImageView video_tip;
        TextView name;
        TextView count;
        View contentView;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            iv_picture = itemView.findViewById(R.id.udesk_iv_picture);
            video_tip = (ImageView) itemView.findViewById(R.id.video_tip);
            name = (TextView) itemView.findViewById(R.id.udesk_name);
            count = (TextView) itemView.findViewById(R.id.udesk_size);
        }
    }


}
