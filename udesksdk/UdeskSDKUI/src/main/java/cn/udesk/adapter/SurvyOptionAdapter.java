package cn.udesk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.model.OptionsModel;
import cn.udesk.model.SurveyOptionsModel;

public class SurvyOptionAdapter extends BaseAdapter {

    private Context mContext;
    private List<OptionsModel> list = new ArrayList<OptionsModel>();

    public SurvyOptionAdapter(Context context , SurveyOptionsModel model) {
        try {
            mContext = context;
            if(model != null  && model.getOptions() != null ){
                list = model.getOptions();
                if(!list.isEmpty() && list.get(0)!= null){
                    list.get(0).setCheck(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }
    
    public void updateCheckOptions(int position){

        try {
            for(int i=0; i<getCount();i++){
                if(i == position){
                    getItem(i).setCheck(true);
                }else{
                    getItem(i).setCheck(false);
                }
            }
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

  
    @Override
    public OptionsModel getItem(int position) {
        try {
            if(position < 0 || position >= list.size()) {
                return null;
            }
            return list.get(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodler viewHodler = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.udesk_dlg_select_singlechoice, null);
            viewHodler = new ViewHodler();
            viewHodler.content = (TextView) convertView.findViewById(R.id.text_context);
            viewHodler.mCheckBox = (CheckBox) convertView.findViewById(R.id.udesk_check_box);
            convertView.setTag(viewHodler);
        } else {
            viewHodler = (ViewHodler) convertView.getTag();
        }

        try {
            OptionsModel  model = getItem(position);
            if(model != null){
                viewHodler.content.setText(model.getText());
                viewHodler.mCheckBox.setChecked(model.isCheck());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    private class ViewHodler {
        private TextView content;
        private CheckBox mCheckBox;
    }

}
