package udesk.sdk.demo.activity;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import udesk.core.model.MessageInfo;
import udesk.sdk.demo.R;


public class UnRedMsgAdapter extends BaseAdapter {

    private Context mContext;
    private List<MessageInfo> list = new ArrayList<MessageInfo>();

    public UnRedMsgAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public void setList(List<MessageInfo> items) {
        list.clear();
        list = items;
        notifyDataSetChanged();
    }

    @Override
    public MessageInfo getItem(int position) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.udesk_unread_msg_item, parent,false);
        }
        ((TextView)convertView).setText((position+1) + "、 "+ list.get(position).getMsgContent());
        return convertView;
    }

}
