package cn.udesk.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.widget.UdeskTitleBar;

public class UdeskFormActivity extends Activity {
	private WebView mwebView;
	private UdeskTitleBar mTitlebar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udesk_form_view);
		initView();
	}

	private void initView() {
		mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
		settingTitlebar();
		mwebView = (WebView) findViewById(R.id.udesk_form_webview);
		String url = "";
		if (TextUtils.isEmpty(UdeskConfig.udeskFormUrl)){
			url = "http://" + UdeskSDKManager.getInstance().getDomain(this)
					+ "/im_client/feedback.html"
					+ UdeskUtil.getFormUrlPara(this);
		}else{
			url = UdeskConfig.udeskFormUrl;
		}
		//提交表单的url地址
		settingWebView(url);
	}



	@SuppressLint("NewApi")
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
		mwebView.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

				handler.proceed();
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				Toast.makeText(UdeskFormActivity.this,UdeskFormActivity.this.getString(R.string.udesk_has_wrong_net),Toast.LENGTH_SHORT).show();
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		mwebView.loadUrl(url);
	}

	/**
	 * titlebar 的设置
	 */
	private void settingTitlebar() {
		mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
		if (mTitlebar != null) {
			UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId,mTitlebar.getLeftTextView(),mTitlebar.getRightTextView());
//			UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextcenterResId,mTitlebar.getTitleTextView(),mTitlebar.getStateTextView());
			UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId ,mTitlebar.getRootView());
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
