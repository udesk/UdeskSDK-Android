package cn.udesk.adapter;

import android.content.Context;
import android.content.res.Resources;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.model.Tag;

/**
 * Created by user on 2018/3/28.
 */

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> implements View.OnClickListener {

    private Context mContext;
    List<Tag> tags = new ArrayList<Tag>();
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, List<Tag> datas);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public TagAdapter(Context context) {
        this.mContext = context;
    }

    public void setDatas(List<Tag> datas) {
        if (datas != null) {
            this.tags = datas;
            notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View view) {
        try {
            if (mOnItemClickListener != null) {
                //注意这里使用getTag方法获取数据
                int index = (int) view.getTag();
                Tag tag = tags.get(index);
                tag.setCheck(!tag.isCheck());
                mOnItemClickListener.onItemClick(view, getCheckTags());
                notifyItemChanged(index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Tag> getCheckTags() {
        List<Tag> checkTags = new ArrayList<>();
        try {
            for (Tag tag : tags) {
                if (tag.isCheck()) {
                    checkTags.add(tag);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return checkTags;
    }

    @Override
    public TagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.udesk_tag_view, parent, false);
        view.setOnClickListener(this);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TagViewHolder holder, int position) {
        Tag tag = tags.get(position);
        try {
            if (tag != null) {
                holder.itemView.setTag(position);
                holder.name.setText(tag.getText());
                if (tag.isCheck()) {
                    holder.name.setBackgroundResource(R.drawable.udesk_remark_tag_checked_bg);
                    holder.name.setTextColor(mContext.getResources().getColor(R.color.udesk_color_2d93fa));
                } else {
                    holder.name.setBackgroundResource(R.drawable.udesk_remark_tag_uncheck_bg);
                    holder.name.setTextColor(mContext.getResources().getColor(R.color.udesk_color_999999));
                }
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public static class TagViewHolder extends RecyclerView.ViewHolder {

        private TextView name;

        public TagViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.udesk_tag_text);
        }

    }
}
