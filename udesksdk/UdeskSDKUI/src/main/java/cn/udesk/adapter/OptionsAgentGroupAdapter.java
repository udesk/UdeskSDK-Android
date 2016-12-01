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
import cn.udesk.model.AgentGroupNode;

public class OptionsAgentGroupAdapter extends BaseAdapter {

    private Context mContext;
    private List<AgentGroupNode> list = new ArrayList<AgentGroupNode>();

    public OptionsAgentGroupAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public void setList(List<AgentGroupNode> items) {
        list.clear();
        list.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public AgentGroupNode getItem(int position) {
        if(position < 0 || position >= list.size()) {
            return null;
        }
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.udesk_layout_optionagentgroup_item, null);
        }
        ((TextView)convertView).setText(list.get(position).getItem_name());
        return convertView;
    }

}
