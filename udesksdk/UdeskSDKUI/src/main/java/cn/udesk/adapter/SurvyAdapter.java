package cn.udesk.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.model.OptionsModel;
import udesk.core.UdeskConst;
import udesk.core.utils.UdeskUtils;

/**
 * Created by user on 2018/3/28.
 */

public class SurvyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private Context mContext;
    private String fiveStar = "五星";
    List<OptionsModel> optionsModels = new ArrayList<OptionsModel>();
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    private int viewType;
    int checkId;

    int width;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int option, int type, OptionsModel data);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public SurvyAdapter(Context context, List<OptionsModel> datas, int type, int defualtId) {
        try {
            this.mContext = context;
            this.viewType = type;
            if (viewType == UdeskConst.UdeskSurvyShowType.STAR) {
                width = UdeskUtils.getScreenWidth(context) - UdeskUtil.dip2px(mContext, 155);
                width = width / 5;
                if (datas.get(0).getDesc().equals(fiveStar)) {
                    Collections.reverse(datas);
                }
            } else if (viewType == UdeskConst.UdeskSurvyShowType.EXPRESSION) {
                width = UdeskUtils.getScreenWidth(context) - UdeskUtil.dip2px(mContext, 108);
                width = width / 3;
            }
            this.optionsModels = datas;
            this.checkId = defualtId;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        try {
            if (mOnItemClickListener != null) {
                //注意这里使用getTag方法获取数据
                int postion = (int) view.getTag();
                mOnItemClickListener.onItemClick(view, postion, viewType, optionsModels.get(postion));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position) {

        return viewType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == UdeskConst.UdeskSurvyShowType.STAR) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.udesk_survy_type_star_item, parent, false);
            SurvyStarViewHolder starViewHolder = new SurvyStarViewHolder(view);
            view.setOnClickListener(this);
            return starViewHolder;
        } else if (viewType == UdeskConst.UdeskSurvyShowType.EXPRESSION) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.udesk_survy_type_express_item, parent, false);
            view.setOnClickListener(this);
            SurvyExpressionViewHolder expressionViewHolder = new SurvyExpressionViewHolder(view);
            return expressionViewHolder;
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.udesk_survy_type_text_item, parent, false);
            view.setOnClickListener(this);
            SurvyTextViewHolder survyTextViewHolder = new SurvyTextViewHolder(view);
            return survyTextViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        OptionsModel optionsModel = optionsModels.get(position);
        try {
            if (optionsModel != null) {
                holder.itemView.setTag(position);
                if (holder instanceof SurvyTextViewHolder) {
                    SurvyTextViewHolder textViewHolder = (SurvyTextViewHolder) holder;
                    textViewHolder.content.setText(optionsModel.getText());
                    if (checkId > 0 && checkId == optionsModel.getId()) {
                        textViewHolder.mCheckBox.setChecked(true);
                    } else {
                        textViewHolder.mCheckBox.setChecked(false);
                    }
                } else if (holder instanceof SurvyExpressionViewHolder) {
                    SurvyExpressionViewHolder expressionViewHolder = (SurvyExpressionViewHolder) holder;
                    ViewGroup.LayoutParams layoutParams = expressionViewHolder.root.getLayoutParams();
                    layoutParams.width = width;
                    expressionViewHolder.root.setLayoutParams(layoutParams);
                    if (position == 0) {
                        expressionViewHolder.express.setImageResource(R.drawable.udesk_survy_statify);
                        expressionViewHolder.name.setText(mContext.getString(R.string.udesk_statify));
                    } else if (position == 1) {
                        expressionViewHolder.express.setImageResource(R.drawable.udesk_survy_common);
                        expressionViewHolder.name.setText(mContext.getString(R.string.udesk_common));
                    } else if (position == 2) {
                        expressionViewHolder.express.setImageResource(R.drawable.udesk_survy_unstatify);
                        expressionViewHolder.name.setText(mContext.getString(R.string.udesk_unstatify));
                    }
                    if (checkId == optionsModel.getId()) {
                        expressionViewHolder.name.setTextColor(mContext.getResources().getColor(R.color.udesk_color_333333));
                        expressionViewHolder.root.setBackgroundResource(R.drawable.udesk_express_pressed_bg);
                    } else {
                        expressionViewHolder.name.setTextColor(mContext.getResources().getColor(R.color.udesk_color_999999));
                        expressionViewHolder.root.setBackgroundResource(R.drawable.udesk_express_noraml_bg);
                    }

                } else if (holder instanceof SurvyStarViewHolder) {
                    SurvyStarViewHolder starViewHolder = (SurvyStarViewHolder) holder;
                    ViewGroup.LayoutParams layoutParams = starViewHolder.starRoot.getLayoutParams();
                    layoutParams.width = width;
                    starViewHolder.starRoot.setLayoutParams(layoutParams);
                    if (checkId > 0 && optionsModel.getId() >= checkId) {
                        starViewHolder.star.setImageResource(R.drawable.udesk_star_on);
                    } else {
                        starViewHolder.star.setImageResource(R.drawable.udesk_star_off);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public int getItemCount() {
        return optionsModels.size();
    }

    public int getCheckId() {
        return checkId;
    }

    public void setCheckId(int checkId) {
        this.checkId = checkId;
    }


    public static class SurvyTextViewHolder extends RecyclerView.ViewHolder {

        private TextView content;
        private CheckBox mCheckBox;

        public SurvyTextViewHolder(View itemView) {
            super(itemView);
            content = (TextView) itemView.findViewById(R.id.text_context);
            mCheckBox = (CheckBox) itemView.findViewById(R.id.udesk_check_box);
        }

    }

    public static class SurvyExpressionViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private ImageView express;
        private View root;

        public SurvyExpressionViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.udesk_survy_desc);
            express = (ImageView) itemView.findViewById(R.id.udesk_express_img);
            root = itemView.findViewById(R.id.udesk_express_root);
        }

    }

    public static class SurvyStarViewHolder extends RecyclerView.ViewHolder {

        private ImageView star;
        private View starRoot;

        public SurvyStarViewHolder(View itemView) {
            super(itemView);
            star = (ImageView) itemView.findViewById(R.id.udesk_star_img);
            starRoot = itemView.findViewById(R.id.star_root);
        }

    }
}
