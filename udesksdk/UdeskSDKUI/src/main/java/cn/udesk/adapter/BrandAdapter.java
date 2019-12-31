package cn.udesk.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import udesk.core.model.MessageInfo;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.BrandViewHold>{
    private final Context mContext;
    private List<MessageInfo> list = new ArrayList<>();
    public BrandAdapter(Context context, List<MessageInfo> list) {
        mContext=context;
        this.list=list;
    }



    public void addItem(MessageInfo messageInfo){
        this.list.add(messageInfo);
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public BrandViewHold onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return  new BrandViewHold(LayoutInflater.from(mContext).inflate(R.layout.udesk_robot_view_struc_brand,null));
    }

    @Override
    public void onBindViewHolder(@NonNull BrandViewHold brandViewHold, int i) {


    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class BrandViewHold extends RecyclerView.ViewHolder {
        private LinearLayout viewBrand;
        private LinearLayout llBrand;
        private LinearLayout llBrandImg;
        private LinearLayout llBrandLook;
        private ImageView ivBrand;
        private TextView txtBrand;
        public BrandViewHold(@NonNull View itemView) {
            super(itemView);
            viewBrand=itemView.findViewById(R.id.udesk_robot_view_struc_brand);
            llBrand=itemView.findViewById(R.id.udesk_robot_ll_struc_brand);
            llBrandImg=itemView.findViewById(R.id.udesk_robot_ll_struc_brand_img);
            llBrandLook=itemView.findViewById(R.id.udesk_robot_ll_struc_brand_look);
            ivBrand=itemView.findViewById(R.id.udesk_robot_img_struc_brand);
            txtBrand=itemView.findViewById(R.id.udesk_robot_txt_struc_brand);
        }
    }
}
