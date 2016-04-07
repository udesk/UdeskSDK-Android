package cn.udesk.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.UdeskHttpFacade;

public class UdeskRobotActivity extends Activity {
	private WebView mwebView;
	private UdeskTitleBar mTitlebar;
	private String h5Url = null;
	private String tranfer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udesk_robot_view);
		UdeskUtil.initCrashReport(this);
		initData();
		initView();
	}

	private void initData() {
		Intent intent = getIntent();
		if(intent != null){
			h5Url = intent.getStringExtra(UdeskConst.UDESKHTMLURL);
			tranfer = intent.getStringExtra(UdeskConst.UDESKTRANSFER);
		}
		
	}

	private void initView() {
		mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
		settingTitlebar(tranfer);
		mwebView = (WebView) findViewById(R.id.udesk_robot_webview);
		if (!TextUtils.isEmpty(h5Url)) {
			String url = UdeskHttpFacade.getInstance().buildRobotUrlWithH5(
					this, UdeskSDKManager.getInstance().getSecretKey(this),
					h5Url,
					UdeskSDKManager.getInstance().getSdkToken(this));
			settingWebView(url);
		} else {
			finish();
		}

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
				.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
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

	/**
	 * titlebar 的设置
	 */
	private void settingTitlebar(String tranfer) {
		mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
		if (mTitlebar != null) {
			mTitlebar
					.setTitleTextSequence(getString(R.string.udesk_robot_title));
			mTitlebar.setLeftTextVis(View.VISIBLE);
			mTitlebar.setLeftViewClick(new OnClickListener() {

				@Override
				public void onClick(View v) {
					finish();

				}
			});

			settingTitleBarRight(tranfer);

		}
	}

	private void settingTitleBarRight(String tranfer) {
		if (tranfer != null && tranfer.trim().equals("true")) {
			mTitlebar.setRightTextVis(View.VISIBLE);
			mTitlebar
					.setRightTextSequence(getString(R.string.udesk_transfer_persion));
			mTitlebar.setRightViewClick(new OnClickListener() {

				@Override
				public void onClick(View v) {
					UdeskSDKManager.getInstance().showConversationByImGroup(UdeskRobotActivity.this);
				}
			});
		} else {
			mTitlebar.setRightTextVis(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UdeskUtil.closeCrashReport();
	}
}