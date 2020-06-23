package cn.udesk.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import udesk.core.model.UDHelperItem;

public class UDHelperAdapter extends BaseAdapter {

    private Context mContext;
    private List<UDHelperItem> list = new ArrayList<UDHelperItem>();

    public UDHelperAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public void setList(List<UDHelperItem> items) {
        try {
            list.clear();
            list = items;
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public UDHelperItem getItem(int position) {
        try {
            if(position < 0 || position >= list.size()) {
                return null;
            }
            return list.get(position);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            if(convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.udesk_layout_helper_item, null);
            }
            ((TextView)convertView).setText(list.get(position).subject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

}
