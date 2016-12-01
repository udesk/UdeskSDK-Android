package cn.udesk.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskConfig;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.UdeskHttpFacade;

public class UdeskRobotActivity extends Activity {
	private WebView mwebView;
	private UdeskTitleBar mTitlebar;
	private String h5Url = null;
	private String tranfer = null;
	private boolean isTranferByImGroup = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udesk_robot_view);
		initData();
		initView();
	}

	private void initData() {
		Intent intent = getIntent();
		if(intent != null){
			h5Url = intent.getStringExtra(UdeskConst.UDESKHTMLURL);
			tranfer = intent.getStringExtra(UdeskConst.UDESKTRANSFER);
			isTranferByImGroup = intent.getBooleanExtra(UdeskConst.UDESKISTRANFERSESSION,true);
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
			if (!UdeskUtil.isZh(this)){
				url = url + "&language=en-us" ;
			}
			if(!TextUtils.isEmpty(UdeskConfig.appid)){
				url = url + "&app_id="+UdeskConfig.appid ;
			}
			settingWebView(url);
		} else {
			finish();
		}

	}

	@SuppressLint("NewApi")
	private void settingWebView(String url) {
		final WebSettings settings = mwebView.getSettings();
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
		mwebView.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//				super.onReceivedSslError(view, handler, error);
				handler.proceed();
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//				super.onReceivedError(view, errorCode, description, failingUrl);
				Toast.makeText(UdeskRobotActivity.this,UdeskRobotActivity.this.getString(R.string.udesk_has_wrong_net),Toast.LENGTH_SHORT).show();
				UdeskRobotActivity.this.finish();
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
	private void settingTitlebar(String tranfer) {
		mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
		if (mTitlebar != null) {
			UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId,mTitlebar.getLeftTextView(),mTitlebar.getRightTextView());
			UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId ,mTitlebar.getRootView());
			if (UdeskConfig.DEFAULT != UdeskConfig.udeskbackArrowIconResId) {
				mTitlebar.getUdeskBackImg().setImageResource(UdeskConfig.udeskbackArrowIconResId);
			}
			mTitlebar
					.setLeftTextSequence(getString(R.string.udesk_robot_title));
			mTitlebar.setLeftLinearVis(View.VISIBLE);
			mTitlebar.setLeftViewClick(new OnClickListener() {

				@Override
				public void onClick(View v) {
					finish();

				}
			});

			settingTitleBarRight(tranfer);

		}
	}

	//根据传入的tranfer 控制右侧是否显示转人工
	private void settingTitleBarRight(String tranfer) {
		if (tranfer != null && tranfer.trim().equals("true")) {
			mTitlebar.setRightTextVis(View.VISIBLE);
			mTitlebar
					.setRightTextSequence(getString(R.string.udesk_transfer_persion));
			mTitlebar.setudeskTransferImgVis(View.VISIBLE);
			mTitlebar.setRightViewClick(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (isTranferByImGroup){
						UdeskSDKManager.getInstance().showConversationByImGroup(UdeskRobotActivity.this);
					}else{
						UdeskSDKManager.getInstance().toLanuchChatAcitvity(UdeskRobotActivity.this);
					}

				}
			});
		} else {
			mTitlebar.setRightTextVis(View.GONE);
			mTitlebar.setudeskTransferImgVis(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}