package cn.udesk.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskUtil;
import cn.udesk.widget.UdeskRecycleView;
import udesk.core.UdeskConst;
import udesk.core.event.InvokeEventContainer;
import udesk.core.model.InfoListBean;
import udesk.core.model.ProductListBean;
import udesk.core.model.StrucTableBean;
import udesk.core.utils.UdeskUtils;

public class StrucTableAdapter<T> extends UdeskRecycleView.Adapter {
    private Context mContext;
    private List<T> list = new ArrayList<>();
    private int type;

    public StrucTableAdapter(Context mContext, List<T> list, int type) {
        this.mContext = mContext;
        this.list = list;
        this.type = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (type == UdeskConst.ChatMsgTypeInt.TYPE_SELECTIVE_TABLE) {
            return new TableViewHolder(LayoutInflater.from(mContext).inflate(R.layout.udesk_view_struc_table, null));
        }
        if (type == UdeskConst.ChatMsgTypeInt.TYPE_SELECTIVE_LIST) {
            return new ListViewHolder(LayoutInflater.from(mContext).inflate(R.layout.udesk_view_struc_list, viewGroup, false));
        }
        if (type == UdeskConst.ChatMsgTypeInt.TYPE_SHOW_PRODUCT) {
            return new ShowProductViewHolder(LayoutInflater.from(mContext).inflate(R.layout.udesk_item_reply_product, viewGroup, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        try {
            if (type == UdeskConst.ChatMsgTypeInt.TYPE_SELECTIVE_TABLE) {
                final StrucTableBean.OptionListBean optionListBean = (StrucTableBean.OptionListBean) list.get(i);
                TableViewHolder tableViewHolder = (TableViewHolder) viewHolder;
                tableViewHolder.brand.setText(optionListBean.getValue());
                tableViewHolder.brand.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InvokeEventContainer.getInstance().event_OnTableClick.invoke(optionListBean.getValue());
                    }
                });
            }
            if (type == UdeskConst.ChatMsgTypeInt.TYPE_SELECTIVE_LIST) {
                final StrucTableBean.OptionListBean optionListBean = (StrucTableBean.OptionListBean) list.get(i);
                ListViewHolder listViewHolder = (ListViewHolder) viewHolder;
                listViewHolder.brand.setText(optionListBean.getValue());
                listViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InvokeEventContainer.getInstance().event_OnTableClick.invoke(optionListBean.getValue());
                    }
                });
            }
            if (type == UdeskConst.ChatMsgTypeInt.TYPE_SHOW_PRODUCT) {
                final ProductListBean productListBean = (ProductListBean) list.get(i);
                ShowProductViewHolder showProductViewHolder = (ShowProductViewHolder) viewHolder;
                showProductViewHolder.title.setText(productListBean.getName());
                if (!TextUtils.isEmpty(productListBean.getImage())) {
                    UdeskUtil.loadImage(mContext, showProductViewHolder.image, productListBean.getImage());

                }
                if (productListBean.getInfoList() != null && productListBean.getInfoList().size() > 0) {
                    List<InfoListBean> infoList = productListBean.getInfoList();
                    for (int j = 0; j < infoList.size(); j++) {
                        SpannableString spannableString = UdeskUtil.setSpan(infoList.get(j).getInfo(), UdeskUtils.objectToString(infoList.get(j).getColor()), infoList.get(j).getBoldFlag());
                        if (j == 0) {
                            showProductViewHolder.mid.setVisibility(View.VISIBLE);
                            showProductViewHolder.infoOne.setText(spannableString);
                        } else if (j == 1) {
                            showProductViewHolder.infoTwo.setText(spannableString);
                        } else if (j == 2) {
                            showProductViewHolder.infoThree.setVisibility(View.VISIBLE);
                            showProductViewHolder.infoThree.setText(spannableString);
                        }
                    }
                } else {
                    showProductViewHolder.mid.setVisibility(View.GONE);
                    showProductViewHolder.infoThree.setVisibility(View.GONE);
                }

                showProductViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InvokeEventContainer.getInstance().event_OnShowProductClick.invoke(productListBean);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class TableViewHolder extends RecyclerView.ViewHolder {
        public final TextView brand;

        public TableViewHolder(@NonNull View itemView) {
            super(itemView);
            brand = itemView.findViewById(R.id.udesk_view_struc_table_brand);
        }
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        public final TextView brand;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            brand = itemView.findViewById(R.id.udesk_view_struc_list_brand);
        }
    }

    class ShowProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView image;
        private final TextView title;
        private final RelativeLayout mid;
        private final TextView infoOne;
        private final TextView infoTwo;
        private final TextView infoThree;

        public ShowProductViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.udesk_product_img);
            title = itemView.findViewById(R.id.udesg_product_title);
            mid = itemView.findViewById(R.id.udesk_product_mid);
            infoOne = itemView.findViewById(R.id.udesk_info_one);
            infoTwo = itemView.findViewById(R.id.udesk_info_two);
            infoThree = itemView.findViewById(R.id.udesk_info_three);
        }

    }
}
