package cn.udesk.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import cn.udesk.R;
import cn.udesk.widget.UdeskTitleBar;


public class UdeskBaseWebViewActivity extends UdeskBaseActivity {

    protected WebView mwebView;
    protected LinearLayout linearLayout;
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
            udeskWebChromeClient = new UdeskWebChromeClient(this, new UdeskWebChromeClient.ICloseWindow() {
                @Override
                public void closeActivty() {
                    finish();
                }
            });
            linearLayout = (LinearLayout) findViewById(R.id.udesk_webview_root);
            mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
            mwebView = new WebView(this);
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.
                    LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
            mwebView.setLayoutParams(param);
            linearLayout.addView(mwebView);
            settingWebView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setH5TitleListener(UdeskWebChromeClient.GetH5Title h5TitleListener) {
        try {
            udeskWebChromeClient.setH5TitleListener(h5TitleListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint({"NewApi", "SetJavaScriptEnabled"})
    private void settingWebView() {

        try {
            //支持获取手势焦点，输入用户名、密码或其他
            mwebView.requestFocusFromTouch();
            mwebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            mwebView.setScrollbarFadingEnabled(false);

            final WebSettings settings = mwebView.getSettings();
            settings.setJavaScriptEnabled(true);  //支持js
            mwebView.removeJavascriptInterface("searchBoxJavaBridge_");
            mwebView.removeJavascriptInterface("accessibilityTraversal");
            mwebView.removeJavascriptInterface("accessibility");
            //  设置自适应屏幕，两者合用
            settings.setUseWideViewPort(true); //将图片调整到适合webview的大小
            settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
            settings.supportMultipleWindows();  //多窗口
            settings.setAllowFileAccess(true);  //设置可以访问文件
            settings.setNeedInitialFocus(true); //当webview调用requestFocus时为webview设置节点

            settings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
            settings.setSavePassword(false);
            //设置编码格式
            settings.setDefaultTextEncodingName("UTF-8");
            // 关于是否缩放
            settings.setDisplayZoomControls(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

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

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                    handler.proceed();
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    Uri uri = Uri.parse(url);
                    if (url.contains("tel:")) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                        startActivity(intent);
                    } else if (url.contains("show_transfer")) {
                        showTransfer();
                        return true;
                    } else if (url.contains("go_chat")) {
                        goChat();
                    } else if (url.contains("auto_transfer")){
                        autoTransfer();
                    }else if (!(url.startsWith("http://")||url.startsWith("https://"))){
                        return super.shouldOverrideUrlLoading(view,url);
                    }else {
                        view.loadUrl(url);
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void showTransfer() {

    }

    protected void goChat() {

    }

    protected void autoTransfer() {

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
            if (mwebView != null) {
                ViewParent parent = mwebView.getParent();
                if (parent != null) {
                    ((ViewGroup) parent).removeView(mwebView);
                }
                mwebView.removeAllViews();
                mwebView.destroy();
                mwebView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
