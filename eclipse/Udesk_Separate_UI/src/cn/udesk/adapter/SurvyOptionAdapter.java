package cn.udesk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.model.OptionsModel;
import cn.udesk.model.SurveyOptionsModel;

public class SurvyOptionAdapter extends BaseAdapter {

    private Context mContext;
    private List<OptionsModel> list = new ArrayList<OptionsModel>();

    public SurvyOptionAdapter(Context context , SurveyOptionsModel model) {
        mContext = context;
        if(model != null  && model.getOptions() != null){
        	list = model.getOptions();
        	if(list.get(0)!= null){
        		list.get(0).setCheck(true);
        	}
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }
    
    public void updateCheckOptions(int position){
    	
    	for(int i=0; i<getCount();i++){
    		if(i == position){
    			getItem(i).setCheck(true);
    		}else{
    			getItem(i).setCheck(false);
    		}
    	}
    	notifyDataSetChanged();
    }

  
    @Override
    public OptionsModel getItem(int position) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.udesk_dlg_select_singlechoice, null);
        }
        ((CheckedTextView)convertView).setText(list.get(position).getText());
        ((CheckedTextView)convertView).setChecked(list.get(position).isCheck());
        return convertView;
    }

}
