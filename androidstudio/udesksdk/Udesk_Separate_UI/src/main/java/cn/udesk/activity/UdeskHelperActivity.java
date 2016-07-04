package cn.udesk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import cn.udesk.R;
import cn.udesk.JsonUtils;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.adapter.UDHelperAdapter;
import cn.udesk.widget.UdeskLoadingView;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskHttpFacade;
import udesk.core.model.UDHelperItem;

public class UdeskHelperActivity extends Activity implements OnClickListener,AdapterView.OnItemClickListener {

	private UdeskTitleBar  mTitlebar;
	private View naviToIm;
	private View mNoDataView;
	private View mcontainListview;
	private ListView mListView;
	private LinearLayout mSearchView;
	private View btnSearch;
	private UDHelperAdapter mHelperAdapter = null;
	private EditText mSearchEdit;
	private UdeskLoadingView mLoadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udesk_activity_base);
		UdeskUtil.initCrashReport(this);
		initView();
	}

	private void initView(){
		settingTitlebar();
		mNoDataView = findViewById(R.id.udesk_navi_may_search_fail);
		naviToIm = findViewById(R.id.udesk_navi_to_im);
		naviToIm.setOnClickListener(this);
		mSearchView = (LinearLayout) findViewById(R.id.udesk_helper_search);
		btnSearch = findViewById(R.id.udesk_helper_search_button);
		mSearchEdit = (EditText) findViewById(R.id.udesk_helper_search_input);
		btnSearch.setOnClickListener(this);
		mcontainListview = findViewById(R.id.udesk_listviewcontain_view);
		mListView = (ListView) findViewById(R.id.udesk_helper_list);
		mListView.setOnItemClickListener(this);
		mHelperAdapter = new UDHelperAdapter(this);
		mListView.setAdapter(mHelperAdapter);
		mLoadingView = (UdeskLoadingView) findViewById(R.id.udesk_loading);

		mSearchEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				if (editable.toString().length() == 0) {
					startGetHelperList();
				}
			}
		});

		startGetHelperList();
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

	/**
	 * 获取帮助列表
	 */
	private void startGetHelperList() {
		showLoading();
		getListArticles();
	}

	private void startGetSerchHelper(String query) {
		showLoading();
		getArticlesSearch(query);
	}

	/**
	 * 显示loading
	 */
	private void showLoading() {
		mSearchView.setVisibility(View.GONE);
		mcontainListview.setVisibility(View.GONE);
		mLoadingView.setVisibility(View.VISIBLE);
	}



	private void hideLoading() {
		mLoadingView.setVisibility(View.GONE);
		mSearchView.setVisibility(View.VISIBLE);
		showNoDataVis(View.VISIBLE);
	}

	/**
	 * 显示问题列表
	 */
	private void showListView() {
		if(mcontainListview!= null){
			mcontainListview.setVisibility(View.VISIBLE);
		}

		showNoDataVis(View.GONE);
	}

	private void showNoDataVis(int vis) {
		if(mNoDataView != null){
			mNoDataView.setVisibility(vis);
		}

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.udesk_helper_search_button) {
			String search = mSearchEdit.getText().toString();
			if (!TextUtils.isEmpty(search)) {
				startGetSerchHelper(search);
			}
		} else if (v.getId() == R.id.udesk_navi_to_im) {
			UdeskSDKManager.getInstance().showConversationByImGroup(UdeskHelperActivity.this);
		}
	}

	private void getListArticles() {

		UdeskHttpFacade.getInstance().getListArticlesJsonAPi(
				UdeskSDKManager.getInstance().getDomain(UdeskHelperActivity.this),
				UdeskSDKManager.getInstance().getSecretKey(UdeskHelperActivity.this),
				new UdeskCallBack() {
					@Override
					public void onSuccess(String message) {
						List<UDHelperItem> helpItems = JsonUtils.parseListArticlesResult(message);
						hideLoading();
						if (helpItems != null && helpItems.size() > 0) {
							mHelperAdapter.setList(helpItems);
							showListView();
						} else {
							showNoDataVis(View.VISIBLE);
						}
					}

					@Override
					public void onFail(String message) {
						hideLoading();
						Toast.makeText(UdeskHelperActivity.this,
								message, Toast.LENGTH_LONG).show();
					}
				});

	}

	private void getArticlesSearch(String query) {
		UdeskHttpFacade.getInstance().getArticlesSearchJsonAPi(
				UdeskSDKManager.getInstance().getDomain(UdeskHelperActivity.this),
				UdeskSDKManager.getInstance().getSecretKey(UdeskHelperActivity.this),
				query,
				new UdeskCallBack() {

					@Override
					public void onSuccess(String message) {
						List<UDHelperItem> searchitems = JsonUtils
								.parseListArticlesResult(message);
						hideLoading();
						if (searchitems != null && searchitems.size() > 0) {
							mHelperAdapter.setList(searchitems);
							showListView();
						} else {
							showNoDataVis(View.VISIBLE);
						}
					}

					@Override
					public void onFail(String message) {
						hideLoading();
						showNoDataVis(View.VISIBLE);
					}
				});

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {

		UDHelperItem item = mHelperAdapter.getItem(position);
		if(item != null){
			Intent intent= new Intent();
			intent.putExtra(UdeskConst.UDESKARTICLEID, item.id);
			intent.setClass(UdeskHelperActivity.this, UdeskHelperArticleActivity.class);
			UdeskHelperActivity.this.startActivity(intent);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UdeskUtil.closeCrashReport();
	}
}
