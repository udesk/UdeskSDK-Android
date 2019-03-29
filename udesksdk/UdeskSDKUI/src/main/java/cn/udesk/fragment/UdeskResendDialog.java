package cn.udesk.fragment;


import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.udesk.R;
import cn.udesk.activity.UdeskChatActivity;
import udesk.core.model.MessageInfo;

public class UdeskResendDialog extends UdeskBaseDialog {

    private TextView tvRetry;
    private TextView tvCancel;
    private MessageInfo messageInfo;
    private RetryListner retryListner;

    public static UdeskResendDialog newInstance(String s1, String s2, MessageInfo messageInfo) {
        try {
            UdeskResendDialog dialog = new UdeskResendDialog();
            Bundle args = new Bundle();
            args.putString("retry", s1);
            args.putString("cancel", s2);
            args.putSerializable("message", messageInfo);
            dialog.setArguments(args);
            return dialog;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void loadData(Bundle arguments) {
        try {
            if (arguments != null) {
                String retry = arguments.getString("retry");
                String cancel = arguments.getString("cancel");
                messageInfo = (MessageInfo) arguments.getSerializable("message");
                tvRetry.setText(retry);
                tvCancel.setText(cancel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void initView(View view) {
        try {
            tvRetry = view.findViewById(R.id.udesk_retry);
            tvCancel = view.findViewById(R.id.udesk_cancel);
            tvRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (retryListner!=null){
                        retryListner.onRetry();
                    }
//                    ((UdeskChatActivity) getActivity()).retrySendMsg(messageInfo);
                    dismiss();
                }
            });
            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public interface RetryListner{
        void onRetry();
    }
    public void setRetryListner(RetryListner listner){
        retryListner=listner;
    }
    @Override
    public void onResume() {
        super.onResume();
        try {
            setGravity(Gravity.BOTTOM);
            setDialogWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getLayoutId() {
        try {
            return R.layout.udesk_dialog_retry;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected int setWindowAnimationsStyle() {
        try {
            return R.style.udesk_survy_anim;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
