package cn.udesk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import cn.udesk.JsonUtils;
import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskHttpFacade;
import udesk.core.model.UDHelperArticleContentItem;

public class UdeskHelperArticleActivity extends Activity {

	   private UdeskTitleBar  mTitlebar;
	   private View udeskLoading;
	   private TextView udeskSubject;
	   private WebView  udeskWebView;
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.udesk_articleactivity_view);
			settingTitlebar();
			udeskLoading = findViewById(R.id.udesk_loading);
			udeskSubject = (TextView) findViewById(R.id.udesk_subject);
			udeskWebView = (WebView) findViewById(R.id.udesk_help_content_webview);
			Intent intent = getIntent();
			int id = intent.getIntExtra(UdeskConst.UDESKARTICLEID, -1);
			if(id != -1){
				getArticlesContentJsonApiById(id);
			}
			
		}

		/**
		 * titlebar 的设置
		 */
		private void settingTitlebar(){
			mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
			if(mTitlebar != null){
				mTitlebar.setTitleTextSequence(getString(R.string.udesk_navi_helper_title_main));
				mTitlebar.setLeftTextVis(View.VISIBLE);
				mTitlebar.setLeftViewClick(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						finish();
					}
				});
			}
		}
		
		
		private void getArticlesContentJsonApiById(int id) {
			udeskLoading.setVisibility(View.VISIBLE);
			UdeskHttpFacade.getInstance().getArticlesContentJsonApiById(
					UdeskSDKManager.getInstance().getDomain(this),
					UdeskSDKManager.getInstance().getSecretKey(this),
					id, new UdeskCallBack() {
				
				@Override
				public void onSuccess(String message) {
					udeskLoading.setVisibility(View.GONE);
					try{
						UDHelperArticleContentItem item = JsonUtils.parseArticleContentItem(message);
						udeskSubject.setText(item.subject);
						String htmlData = item.content;
						htmlData = htmlData.replaceAll("&amp;", "");
						htmlData = htmlData.replaceAll("quot;", "\"");
						htmlData = htmlData.replaceAll("lt;", "<");
						htmlData = htmlData.replaceAll("gt;", ">");
						udeskWebView.loadDataWithBaseURL(null, htmlData, "text/html", "utf-8", null);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
				
				@Override
				public void onFail(String message) {
					udeskLoading.setVisibility(View.GONE);
					Toast.makeText(UdeskHelperArticleActivity.this, message, Toast.LENGTH_SHORT).show();
				}
			});

		}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
