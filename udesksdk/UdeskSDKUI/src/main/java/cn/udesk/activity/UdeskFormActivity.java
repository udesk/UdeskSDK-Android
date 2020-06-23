package cn.udesk.activity;


import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.config.UdeskConfigUtil;
import cn.udesk.config.UdeskConfig;
import udesk.core.UdeskConst;


public class UdeskFormActivity extends UdeskBaseWebViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            UdeskUtil.setOrientation(this);
            loadingView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadingView() {
        try {
            settingTitlebar();
            String url = UdeskConst.HTTP +  UdeskSDKManager.getInstance().getDomain(this)
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
                UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarMiddleTextResId, mTitlebar.getUdeskTopText(), mTitlebar.getUdeskBottomText());
                UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarRightTextResId, mTitlebar.getRightTextView());
                if (mTitlebar.getRootView() != null){
                    UdeskConfigUtil.setUIbgDrawable(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarBgResId ,mTitlebar.getRootView());
                }
                if (UdeskConfig.DEFAULT != UdeskSDKManager.getInstance().getUdeskConfig().udeskbackArrowIconResId) {
                    mTitlebar.getUdeskBackImg().setImageResource(UdeskSDKManager.getInstance().getUdeskConfig().udeskbackArrowIconResId);
                }
                mTitlebar.setTopTextSequence(getString(R.string.udesk_ok));
                mTitlebar.setUdeskBottomTextVis(View.GONE);
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
