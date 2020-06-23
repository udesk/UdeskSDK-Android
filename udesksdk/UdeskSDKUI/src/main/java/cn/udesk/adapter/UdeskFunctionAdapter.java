package cn.udesk.adapter;

import android.content.Context;
import androidx.core.text.TextUtilsCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.model.FunctionMode;

/**
 * Created by user on 2018/3/19.
 */

public class UdeskFunctionAdapter extends BaseAdapter {

    private Context context;
    private List<FunctionMode> functionItems = new ArrayList<FunctionMode>();

    public UdeskFunctionAdapter(Context content) {

        this.context = content;

    }

    public void setFunctionItems(List<FunctionMode> datas) {
        if (datas != null) {
            functionItems.clear();
            functionItems.addAll(datas);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return functionItems.size();
    }

    @Override
    public Object getItem(int i) {
        return functionItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        try {
            if (convertView == null) {

                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(this.context).inflate(R.layout.udesk_picture_item, null);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.udesk_image);
                viewHolder.title = (TextView) convertView.findViewById(R.id.udesk_title);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            FunctionMode functionItem = functionItems.get(position);
            if (functionItem != null) {
                viewHolder.title.setText(functionItem.getName());
                viewHolder.image.setImageResource(functionItem.getmIconSrc());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    class ViewHolder {

        public ImageView image;
        public TextView title;
    }

}
