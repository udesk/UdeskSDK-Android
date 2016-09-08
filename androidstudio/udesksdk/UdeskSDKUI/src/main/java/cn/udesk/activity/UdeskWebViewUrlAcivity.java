package cn.udesk.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tencent.bugly.crashreport.CrashReport;

import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.widget.UdeskTitleBar;


/**
 * Created by k on 2016/4/6.
 */
public class UdeskWebViewUrlAcivity extends Activity{

    UdeskTitleBar welcome_titlebar;
    WebView mwebView;
    String url="";
    String urlTile="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_webview);
        initView();
        getintent();
        settingWebView(url);
        WebChromeClient wcc = new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
//                welcome_titlebar.setTitleTextVis(View.VISIBLE);
                welcome_titlebar.setLeftTextSequence(title);
            }
        };
        mwebView.setWebChromeClient(wcc);
//        setUrlTile();
    }

    public void initView(){
        welcome_titlebar = (UdeskTitleBar) findViewById(R.id.welcome_udesktitlebar);
        UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId,welcome_titlebar.getLeftTextView(),welcome_titlebar.getRightTextView());
//        UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextcenterResId,welcome_titlebar.getTitleTextView(),welcome_titlebar.getStateTextView());
        UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId ,welcome_titlebar.getRootView());
        if (UdeskConfig.DEFAULT != UdeskConfig.udeskbackArrowIconResId) {
            welcome_titlebar.getUdeskBackImg().setImageResource(UdeskConfig.udeskbackArrowIconResId);
        }
        mwebView = (WebView) findViewById(R.id.welcome_webview);
        welcome_titlebar.setLeftTextSequence(UdeskWebViewUrlAcivity.this.getString(R.string.udesk_titlebar_back));
        welcome_titlebar.setLeftLinearVis(View.VISIBLE);
        welcome_titlebar.setLeftViewClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public void getintent(){
        Intent intent = getIntent();
        url = intent.getStringExtra(UdeskConst.WELCOME_URL);
        urlTile = intent.getStringExtra(UdeskConst.WELCOME_URL_TITLE);
    }
    public void setUrlTile(){
//        welcome_titlebar.setTitleTextVis(View.VISIBLE);
        welcome_titlebar.setLeftTextSequence(urlTile);
    }
    private void settingWebView(String url) {
        WebSettings settings = mwebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mwebView.setInitialScale(1);
        mwebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mwebView.setScrollbarFadingEnabled(false);
        settings.setDefaultTextEncodingName("UTF-8");
        // 关于是否缩放
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            settings.setDisplayZoomControls(false);
        }
        settings.setSupportZoom(false); // 支持缩放
        settings.setBuiltInZoomControls(false);
        // 关于自适应屏幕
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDomStorageEnabled(true);
        mwebView.getSettings()
                .setCacheMode(WebSettings.LOAD_NO_CACHE);
        mwebView.setWebChromeClient(new WebChromeClient());
        mwebView.setWebViewClient(new WebViewClient());
        mwebView.loadUrl(url);
        mwebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
