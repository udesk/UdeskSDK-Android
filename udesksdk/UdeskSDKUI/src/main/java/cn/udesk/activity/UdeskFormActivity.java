package cn.udesk.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.widget.UdeskTitleBar;

public class UdeskFormActivity extends UdeskBaseWebViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingView();
    }

    private void loadingView() {
        settingTitlebar();
        String url = "";
        if (TextUtils.isEmpty(UdeskConfig.udeskFormUrl)) {
            url = "http://" + UdeskSDKManager.getInstance().getDomain(this)
                    + "/im_client/feedback.html"
                    + UdeskUtil.getFormUrlPara(this);
        } else {
            url = UdeskConfig.udeskFormUrl;
        }
        mwebView.loadUrl(url);
    }

    /**
     * titlebar 的设置
     */
    private void settingTitlebar() {
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
    }


}
