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
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.widget.UdeskLoadingView;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskHttpFacade;
import udesk.core.model.UDHelperItem;

public class UdeskHelperActivity extends Activity implements OnClickListener, AdapterView.OnItemClickListener {

    private UdeskTitleBar mTitlebar;
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
        initView();
    }

    private void initView() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * titlebar 的设置
     */
    private void settingTitlebar() {
        try {
            mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
            if (mTitlebar != null) {
                mTitlebar.setLeftTextSequence(getString(R.string.udesk_navi_helper_title_main));
                mTitlebar.setLeftLinearVis(View.VISIBLE);
                mTitlebar.setLeftViewClick(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                if (UdeskConfig.DEFAULT != UdeskConfig.udeskbackArrowIconResId) {
                    mTitlebar.getUdeskBackImg().setImageResource(UdeskConfig.udeskbackArrowIconResId);
                }
               UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId,mTitlebar.getLeftTextView(),mTitlebar.getRightTextView());
               UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId ,mTitlebar.getRootView());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取帮助列表
     */
    private void startGetHelperList() {
        try {
            showLoading();
            getListArticles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startGetSerchHelper(String query) {
        try {
            showLoading();
            getArticlesSearch(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示loading
     */
    private void showLoading() {
        try {
            mSearchView.setVisibility(View.GONE);
            mcontainListview.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void hideLoading() {
        try {
            mLoadingView.setVisibility(View.GONE);
            mSearchView.setVisibility(View.VISIBLE);
            showNoDataVis(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示问题列表
     */
    private void showListView() {
        try {
            if (mcontainListview != null) {
                mcontainListview.setVisibility(View.VISIBLE);
            }

            showNoDataVis(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNoDataVis(int vis) {
        if (mNoDataView != null) {
            mNoDataView.setVisibility(vis);
        }

    }

    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.udesk_helper_search_button) {
                String search = mSearchEdit.getText().toString();
                if (!TextUtils.isEmpty(search)) {
                    startGetSerchHelper(search);
                }
            } else if (v.getId() == R.id.udesk_navi_to_im) {
                UdeskSDKManager.getInstance().showConversationByImGroup(UdeskHelperActivity.this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //请求获取文章列表
    private void getListArticles() {

        try {
            UdeskHttpFacade.getInstance().getListArticlesJsonAPi(
                    UdeskSDKManager.getInstance().getDomain(this),
                    UdeskSDKManager.getInstance().getAppkey(this),
                    UdeskSDKManager.getInstance().getAppId(this),
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //获取包含搜索内容的文章列表
    private void getArticlesSearch(String query) {
        try {
            UdeskHttpFacade.getInstance().getArticlesSearchJsonAPi(
                    UdeskSDKManager.getInstance().getDomain(this),
                    UdeskSDKManager.getInstance().getAppkey(this),
                    query,
                    UdeskSDKManager.getInstance().getAppId(this),
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

        try {
            UDHelperItem item = mHelperAdapter.getItem(position);
            if (item != null) {
                Intent intent = new Intent();
                intent.putExtra(UdeskConst.UDESKARTICLEID, item.id);
                intent.setClass(UdeskHelperActivity.this, UdeskHelperArticleActivity.class);
                UdeskHelperActivity.this.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
