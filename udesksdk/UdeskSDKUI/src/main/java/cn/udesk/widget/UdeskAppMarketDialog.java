package cn.udesk.widget;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import cn.udesk.R;

public class UdeskAppMarketDialog extends Dialog {

    private TextView okTxt, cancleTxt, titleTxt, contentTxt;
    private View udeskBottom;

    public UdeskAppMarketDialog(Context context) {
        super(context, R.style.udesk_app_market_dialog);
        getWindow().setGravity(Gravity.TOP);
        setCustomDialog();
    }

    private void setCustomDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.udesk_app_market_dialog_define_view, null);

        okTxt = (TextView) mView.findViewById(R.id.udesk_ok);
        cancleTxt = (TextView) mView.findViewById(R.id.udesk_cancle);
        titleTxt = (TextView) mView.findViewById(R.id.udesk_title);
        contentTxt = (TextView) mView.findViewById(R.id.udesk_content);
        udeskBottom = mView.findViewById(R.id.udesk_bottom_rl);
        contentTxt.setMovementMethod(ScrollingMovementMethod.getInstance());
        super.setContentView(mView);
    }

    public void setTitleViewVis(int vis) {
        titleTxt.setVisibility(vis);
    }

    public void setTitle(String s) {
        titleTxt.setText(s);
    }


    public void setCancleTextViewVis(int vis) {
        cancleTxt.setVisibility(vis);
    }

    public void setOkAndCancelTxt(String ok, String cancle) {
        if (okTxt != null && !TextUtils.isEmpty(ok)) {
            okTxt.setText(ok);
        }
        if (cancleTxt != null && !TextUtils.isEmpty(cancle)) {
            cancleTxt.setText(cancle);
        }
    }

    public void setContent(String s) {
        contentTxt.setText(s);
    }

    public void setContentTxtVis(int vis) {
        contentTxt.setVisibility(View.VISIBLE);
    }

    //设置确定监听事件
    public void setOkTextViewOnclick(View.OnClickListener listener) {
        okTxt.setOnClickListener(listener);
    }

    //设置取消监听事件
    public void setCancleTextViewOnclick(View.OnClickListener listener) {
        cancleTxt.setOnClickListener(listener);
    }

    public void setOkTxtTextViewVis(int vis) {
        okTxt.setVisibility(vis);
    }

    public void setudeskBottomViewVis(int vis) {
        udeskBottom.setVisibility(vis);
    }

}
