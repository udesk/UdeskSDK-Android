package udesk.sdk.demo.activity;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import udesk.sdk.demo.R;

/**
 * 自定义customer
 */
public class CustomDialog extends Dialog{

    private EditText editText;
    private TextView okTextView, cancleTextView;
    private TextView title,contentText;

    private ListView mListView;

    private View style1,style2;
    private CheckBox style1_checkbox,style2_checkbox;


    public CustomDialog(Context context) {
        super(context, R.style.add_dialog);
        setCustomDialog();
    }

    private void setCustomDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.udesk_dialog_normal_layout, null);
        title = (TextView) mView.findViewById(R.id.udeskdemo_customer_title);
        editText = (EditText) mView.findViewById(R.id.udeskdemo_custome_edit);
        okTextView = (TextView) mView.findViewById(R.id.udeskdemo_ok);
        cancleTextView = (TextView) mView.findViewById(R.id.udeskdemo_cancle);
        contentText = (TextView) mView.findViewById(R.id.udeskdemo_text);
        mListView = (ListView) mView.findViewById(R.id.udeskdemo_listview);
        style1 = mView.findViewById(R.id.udesk_ui_style_1);
        style1_checkbox = (CheckBox)mView.findViewById(R.id.udesk_check_ui_style_1);

        style2 = mView.findViewById(R.id.udesk_ui_style_2);
        style2_checkbox = (CheckBox) mView.findViewById(R.id.udesk_check_ui_style_2);
        super.setContentView(mView);
    }

    public  View getViewStyle1(){
        style1.setVisibility(View.VISIBLE);
        return style1;
    }

    public CheckBox getStyle1Checkbox(){
        return  style1_checkbox;
    }

    public  View getViewStyle2(){
        style2.setVisibility(View.VISIBLE);
        return style2;
    }

    public  CheckBox getStyle2Checkbox(){
        return  style2_checkbox;
    }

    public View getEditText(){
        editText.setVisibility(View.VISIBLE);
        return editText;
    }

    public View getcontentText(){
        contentText.setVisibility(View.VISIBLE);
        return contentText;
    }

    public ListView getListView(){
        mListView.setVisibility(View.VISIBLE);
        return mListView;
    }


    //设置确定监听事件
    public void setOkTextViewOnclick(View.OnClickListener listener){
        okTextView.setOnClickListener(listener);
    }

    //设置取消监听事件
    public void setCancleTextViewOnclick(View.OnClickListener listener){
        cancleTextView.setOnClickListener(listener);
    }

    public void setstyle1OnclickListener(View.OnClickListener listener){
        style1.setOnClickListener(listener);
    }

    public void setstyle2OnclickListener(View.OnClickListener listener){
        style2.setOnClickListener(listener);
    }

    public void setDialogTitle(String name){
        if(title != null){
            title.setText(name);
        }
    }



}
