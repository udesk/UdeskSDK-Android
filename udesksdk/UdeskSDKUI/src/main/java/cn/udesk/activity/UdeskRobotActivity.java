package cn.udesk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;


import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.UdeskHttpFacade;

public class UdeskRobotActivity extends UdeskBaseWebViewActivity {
	private String h5Url = null;
	private String tranfer = null;
	private boolean isTranferByImGroup = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		loadingView();
	}

	private void initData() {
		try {
			Intent intent = getIntent();
			if(intent != null){
                h5Url = intent.getStringExtra(UdeskConst.UDESKHTMLURL);
                tranfer = intent.getStringExtra(UdeskConst.UDESKTRANSFER);
                isTranferByImGroup = intent.getBooleanExtra(UdeskConst.UDESKISTRANFERSESSION,true);
            }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void loadingView() {
		try {
			settingTitlebar(tranfer);
			if (!TextUtils.isEmpty(h5Url)) {
                String url = UdeskHttpFacade.getInstance().buildRobotUrlWithH5(
                        this, UdeskSDKManager.getInstance().getAppkey(this),
                        h5Url,
                        UdeskSDKManager.getInstance().getSdkToken(this));
                if (!UdeskUtil.isZh(this)){
                    url = url + "&language=en-us" ;
                }
                if(!TextUtils.isEmpty( UdeskSDKManager.getInstance().getAppId(this))){
                    url = url + "&app_id="+UdeskSDKManager.getInstance().getAppId(this) ;
                }
                mwebView.loadUrl(url);
            } else {
                finish();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * titlebar 的设置
	 */
	private void settingTitlebar(String tranfer) {
		try {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//根据传入的tranfer 控制右侧是否显示转人工
	private void settingTitleBarRight(String tranfer) {
		try {
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
                            Intent intent = new Intent(UdeskRobotActivity.this, UdeskChatActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            UdeskRobotActivity.this.startActivity(intent);
                        }

                    }
                });
            } else {
                mTitlebar.setRightTextVis(View.GONE);
                mTitlebar.setudeskTransferImgVis(View.GONE);
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