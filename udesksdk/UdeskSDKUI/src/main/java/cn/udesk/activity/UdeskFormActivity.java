package cn.udesk.activity;


import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;


public class UdeskFormActivity extends UdeskBaseWebViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingView();
    }

    private void loadingView() {
        try {
            settingTitlebar();
            String url = "https://" +  UdeskSDKManager.getInstance().getDomain(this)
                    + "/im_client/feedback.html"
                    + UdeskUtil.getFormUrlPara(this);

            mwebView.loadUrl(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * titlebar 的设置
     */
    private void settingTitlebar() {
        try {
            if (mTitlebar != null) {
                UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId, mTitlebar.getLeftTextView(), mTitlebar.getRightTextView());
                UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId, mTitlebar.getRootView());
                if (UdeskConfig.DEFAULT != UdeskConfig.udeskbackArrowIconResId) {
                    mTitlebar.getUdeskBackImg().setImageResource(UdeskConfig.udeskbackArrowIconResId);
                }
                mTitlebar
                        .setLeftTextSequence(getString(R.string.udesk_ok));
                mTitlebar.setLeftLinearVis(View.VISIBLE);
                mTitlebar.setLeftViewClick(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();

                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
