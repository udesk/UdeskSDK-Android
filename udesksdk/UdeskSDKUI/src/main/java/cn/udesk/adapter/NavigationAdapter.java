package cn.udesk.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.model.NavigationMode;
import udesk.core.UdeskConst;

/**
 * Created by user on 2018/3/28.
 */

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.NavigationViewHolder> implements View.OnClickListener {

    private Context mContext;
    List<NavigationMode> navigationModes = new ArrayList<NavigationMode>();
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, NavigationMode data);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public NavigationAdapter(Context context,String currentView) {
        this.mContext = context;
        if (TextUtils.equals(currentView,UdeskConst.CurrentFragment.agent)){
            if (UdeskSDKManager.getInstance().getUdeskConfig().navigationModes != null){
                navigationModes = UdeskSDKManager.getInstance().getUdeskConfig().navigationModes;
            }
        }else {
            if (UdeskSDKManager.getInstance().getUdeskConfig().robotnavigationModes != null){
                navigationModes = UdeskSDKManager.getInstance().getUdeskConfig().robotnavigationModes;
            }
        }


    }

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(view, (NavigationMode) view.getTag());
        }
    }

    @Override
    public NavigationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.udesk_text_view, parent, false);
        view.setOnClickListener(this);
        return new NavigationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NavigationViewHolder holder, int position) {
        NavigationMode navigationMode = navigationModes.get(position);
        if (navigationMode != null) {
            holder.itemView.setTag(navigationMode);
            holder.name.setText(navigationMode.getName());
        }
    }

    @Override
    public int getItemCount() {
        return navigationModes.size();
    }

    public static class NavigationViewHolder extends RecyclerView.ViewHolder {

        private TextView name;

        public NavigationViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
        }

    }
}
