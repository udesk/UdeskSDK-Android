package cn.udesk.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.udesk.R;
import cn.udesk.widget.UdeskTitleBar;

/**
 * Created by user on 2016/12/21.
 */

public class UdeskBaseWebViewActivity extends Activity {

    protected WebView mwebView;
    protected UdeskTitleBar mTitlebar;
    protected UdeskWebChromeClient udeskWebChromeClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udesk_base_webview);
        initView();
    }

    private void initView() {
        try {
            udeskWebChromeClient = new UdeskWebChromeClient(this, new ICloseWindow() {
                @Override
                public void closeActivty() {
                    finish();
                }
            });
            mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
            mwebView = (WebView) findViewById(R.id.udesk_webview);
            settingWebView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setH5TitleListener(UdeskWebChromeClient.GetH5Title h5TitleListener){
        udeskWebChromeClient.setH5TitleListener(h5TitleListener);
    }

    @SuppressLint("NewApi")
    private void settingWebView() {

        //支持获取手势焦点，输入用户名、密码或其他
        mwebView.requestFocusFromTouch();
        mwebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        mwebView.setScrollbarFadingEnabled(false);

        final WebSettings settings = mwebView.getSettings();
        settings.setJavaScriptEnabled(true);  //支持js
        //  设置自适应屏幕，两者合用
        settings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //若setSupportZoom是false，则该WebView不可缩放，这个不管设置什么都不能缩放。
        settings.setSupportZoom(true);  //支持缩放，默认为true。是setBuiltInZoomControls的前提。
        settings.setBuiltInZoomControls(true); //设置内置的缩放控件。
        settings.supportMultipleWindows();  //多窗口

        settings.setAllowFileAccess(true);  //设置可以访问文件
        settings.setNeedInitialFocus(true); //当webview调用requestFocus时为webview设置节点

        settings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        //设置编码格式
        settings.setDefaultTextEncodingName("UTF-8");
        // 关于是否缩放
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            settings.setDisplayZoomControls(false);
        }
        settings.setLoadsImagesAutomatically(true);  //支持自动加载图片

        settings.setDomStorageEnabled(true); //开启DOM Storage

        mwebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                // 监听下载功能，当用户点击下载链接的时候，直接调用系统的浏览器来下载
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                finish();
            }
        });

        mwebView.setWebChromeClient(udeskWebChromeClient);
        mwebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }

//            @Override
//            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                handler.proceed();
//            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                view.loadUrl(url);
                return true;
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            udeskWebChromeClient.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {

        try {
            mwebView.removeAllViews();
            mwebView.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
