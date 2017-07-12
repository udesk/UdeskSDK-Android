package cn.udesk.activity;


import android.os.Bundle;
import android.view.View;



import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;


/**
 * Created by k on 2016/4/6.
 */
public class UdeskWebViewUrlAcivity extends UdeskBaseWebViewActivity {

    String url = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            if (getIntent() != null) {
                url = getIntent().getStringExtra(UdeskConst.WELCOME_URL);
            }
            settingTitlebar();
            mwebView.loadUrl(url);
            setH5TitleListener(new UdeskWebChromeClient.GetH5Title() {
                @Override
                public void h5Title(String title) {
                    mTitlebar.setLeftTextSequence(title);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void settingTitlebar() {

        try {
            UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId, mTitlebar.getLeftTextView(), mTitlebar.getRightTextView());
            UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId, mTitlebar.getRootView());
            if (UdeskConfig.DEFAULT != UdeskConfig.udeskbackArrowIconResId) {
                mTitlebar.getUdeskBackImg().setImageResource(UdeskConfig.udeskbackArrowIconResId);
            }
            mTitlebar.setLeftTextSequence(UdeskWebViewUrlAcivity.this.getString(R.string.udesk_titlebar_back));
            mTitlebar.setLeftLinearVis(View.VISIBLE);
            mTitlebar.setLeftViewClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
