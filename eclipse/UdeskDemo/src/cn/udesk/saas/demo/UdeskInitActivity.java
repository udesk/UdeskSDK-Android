package cn.udesk.saas.demo;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import udesk.core.UdeskCallBack;
import udesk.core.UdeskCoreConst;
import udesk.core.UdeskHttpFacade;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.udesk.UdeskSDKManager;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.widget.UdeskTitleBar;

/**
 * 
 * 在新创建个用户的时候，需要先设置相关的信息 1 调用 UdeskHttpFacade.getInstance().initApiKey
 * 保存信息，不涉及网络操作 2 UdeskHttpFacade.getInstance().setUserInfo 提交用户信息，
 * 返回创建分配终端用户账号ID的值,每个唯一的sdktoken值对应一个终端用户账号 ,
 */
public class UdeskInitActivity extends Activity implements OnClickListener {

	private UdeskTitleBar mTitlebar;

	private EditText mSdktoken, mEmail, mNickname, mCellphone, mWeixin, mWeibo,
			mDescriptionn;
	private Button mCommitInfoBtn;
	private Button mCommitSelfBtn;
	private String UDESK_DOMAIN = "reocar.udeskmonkey.com";//
	private String UDESK_SECRETKEY = "3a4dc5e0cd39995448018c553048fdd4";//
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udesk_init_activity_view);
		initView();
		settingTitlebar();
		UdeskSDKManager.getInstance(this).initApiKey(UDESK_DOMAIN, UDESK_SECRETKEY);
		UdeskSDKManager.getInstance(this).initImageLoaderConfig(this);
	}

	private void initView() {
		mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
		mSdktoken = (EditText) findViewById(R.id.udesk_sdktoken);
		mEmail = (EditText) findViewById(R.id.udesk_email);
		mNickname = (EditText) findViewById(R.id.udesk_nickname);
		mCellphone = (EditText) findViewById(R.id.udesk_cellphone);
		mWeixin = (EditText) findViewById(R.id.udesk_weixin);
		mWeibo = (EditText) findViewById(R.id.udesk_weibo_name);
		mDescriptionn = (EditText) findViewById(R.id.udesk_descriptionn);
		mCommitInfoBtn = (Button) findViewById(R.id.udesk_commituserinfo);
		mCommitInfoBtn.setOnClickListener(this);
		mCommitSelfBtn = (Button) findViewById(R.id.udesk_commit_selffield);
		mCommitSelfBtn.setOnClickListener(this);
	}

	/**
	 * titlebar 的设置
	 */
	private void settingTitlebar() {
		if (mTitlebar != null) {
			mTitlebar.setTitleTextSequence(getString(R.string.udesk_uidemo));
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.udesk_commituserinfo) {
			setUserInfo();
		} else if (v.getId() == R.id.udesk_commit_selffield) {
			commitSelffield();
		}

	}

	private void setUserInfo() {
		if (TextUtils.isEmpty(mSdktoken.getText().toString())) {
			Toast.makeText(this, getString(R.string.udesk_sdktoken_toast),
					Toast.LENGTH_SHORT).show();
			return;
		}
		UdeskDBManager.getInstance().release();
		UdeskDBManager.getInstance().init(this, mSdktoken.getText().toString());
		UdeskSDKManager.getInstance(this).setUserInfo(
				mSdktoken.getText().toString(), getUserInfo());
		toUseCaseActivity();

	}

	private Map<String, String> getUserInfo() {

		Map<String, String> info = new HashMap<String, String>();
		info.put(UdeskCoreConst.UdeskUserInfo.USER_SDK_TOKEN, mSdktoken.getText()
				.toString());
		if (!TextUtils.isEmpty(mEmail.getText().toString())) {
			info.put(UdeskCoreConst.UdeskUserInfo.EMAIL, mEmail.getText()
					.toString());
		}
		if (!TextUtils.isEmpty(mNickname.getText().toString())) {
			info.put(UdeskCoreConst.UdeskUserInfo.NICK_NAME, mNickname
					.getText().toString());
		}
		if (!TextUtils.isEmpty(mCellphone.getText().toString())) {
			info.put(UdeskCoreConst.UdeskUserInfo.CELLPHONE, mCellphone
					.getText().toString());
		}
		if (!TextUtils.isEmpty(mWeixin.getText().toString())) {
			info.put(UdeskCoreConst.UdeskUserInfo.WEIXIN_ID, mWeixin.getText()
					.toString());
		}
		if (!TextUtils.isEmpty(mWeibo.getText().toString())) {
			info.put(UdeskCoreConst.UdeskUserInfo.WEIBO_NAME, mWeibo.getText()
					.toString());
		}
		if (!TextUtils.isEmpty(mDescriptionn.getText().toString())) {
			info.put(UdeskCoreConst.UdeskUserInfo.DESCRIPTION, mDescriptionn
					.getText().toString());
		}
		return info;

	}

	private void commitSelffield() {
		UdeskDBManager.getInstance().release();
		UdeskDBManager.getInstance().init(this, mSdktoken.getText().toString());
		final HashMap<String, String> extraInfoTextField = new HashMap<String, String>();
		final HashMap<String, String> extraInfodRoplist = new HashMap<String, String>();
		UdeskHttpFacade.getInstance().getUserFields(UDESK_DOMAIN,UDESK_SECRETKEY, new UdeskCallBack() {

			@Override
			public void onSuccess(String result) {
				try {
					JSONObject jo = new JSONObject(result);
					int status = jo.optInt("status");
					if (status == 0) {
						JSONArray jsonArray = jo.getJSONArray("user_fields");
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject child = (JSONObject) jsonArray.get(i);
							if (child.has("content_type")) {
								String type = child.getString("content_type");
								if (type.equals("droplist")) {
									if (child.has("options")) {
										// 想设置下拉列表的第几个值，则传入第几个key值字符串， 如下所示
										extraInfodRoplist.put(
												child.getString("field_name"),
												"0");
									}
								} else if (type.equals("text")) {
									extraInfoTextField.put(
											child.getString("field_name"),
											child.getString("comment"));
								}
							}
						}
					}

					if (TextUtils.isEmpty(mSdktoken.getText().toString())) {
						Toast.makeText(UdeskInitActivity.this,
								getString(R.string.udesk_sdktoken_toast),
								Toast.LENGTH_SHORT).show();
						return;
					}
					UdeskDBManager.getInstance().release();
					UdeskDBManager.getInstance().init(UdeskInitActivity.this,
							mSdktoken.getText().toString());
					UdeskSDKManager.getInstance(UdeskInitActivity.this).setUserInfo(
							mSdktoken.getText().toString(), getUserInfo(),
							extraInfoTextField, extraInfodRoplist);
					toUseCaseActivity();
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onFail(String arg0) {

			}
		});

	}

	public void toUseCaseActivity() {
		Intent intent = new Intent(this, UdeskUseCaseActivity.class);
		startActivity(intent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		UdeskDBManager.getInstance().release();
	}

}
