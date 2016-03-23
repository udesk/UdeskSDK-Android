package udesk.udesksdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskHttpFacade;

/**
 * 
 * 在新创建个用户的时候，需要先设置相关的信息 1 调用 UdeskHttpFacade.getInstance().initApiKey
 * 保存信息，不涉及网络操作 2 UdeskHttpFacade.getInstance().setUserInfo 提交用户信息，
 * 返回创建分配终端用户账号ID的值,每个唯一的sdktoken值对应一个终端用户账号 ,
 */
public class UdeskInitActivity extends Activity implements OnClickListener {
	private EditText mSdktoken,  mNickname , mEmail;
//	private String UDESK_DOMAIN = "reocar.udeskmonkey.com";//
//	private String UDESK_SECRETKEY = "3a4dc5e0cd39995448018c553048fdd4";//
	private String UDESK_DOMAIN = "rd-dota.udesk.cn";
	private String UDESK_SECRETKEY = "cc36f043f1e3bf71a0f73a51f4ac3fb5";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udesk_init_activity_view);
		initView();
		UdeskSDKManager.getInstance().initApiKey(this, UDESK_DOMAIN, UDESK_SECRETKEY);
	}

	private void initView() {
		mSdktoken = (EditText) findViewById(R.id.udesk_sdktoken);
		mNickname = (EditText) findViewById(R.id.udesk_nickname);
		mEmail = (EditText) findViewById(R.id.udesk_email);
		findViewById(R.id.udesk_commituserinfo).setOnClickListener(this);
		findViewById(R.id.udesk_commit_selffield).setOnClickListener(this);
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
		UdeskSDKManager.getInstance().setUserInfo(
				this, mSdktoken.getText().toString(), getUserInfo());
		toUseCaseActivity();

	}

	private Map<String, String> getUserInfo() {

		Map<String, String> info = new HashMap<String, String>();
		info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, mSdktoken.getText().toString());
		//以下注释的字段都是可选的字段， 有邮箱建议填写
		info.put(UdeskConst.UdeskUserInfo.NICK_NAME, mNickname.getText().toString());
		info.put(UdeskConst.UdeskUserInfo.EMAIL, mEmail.getText().toString());
		info.put(UdeskConst.UdeskUserInfo.CELLPHONE, UUID.randomUUID().toString().substring(0,12));
		info.put(UdeskConst.UdeskUserInfo.WEIXIN_ID, "这填写微信的ID号");
		info.put(UdeskConst.UdeskUserInfo.WEIBO_NAME, "这是微博的账号");
		info.put(UdeskConst.UdeskUserInfo.DESCRIPTION, "这填写的是描述信息");
		return info;

	}

	private void commitSelffield() {
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
					UdeskSDKManager.getInstance().setUserInfo(UdeskInitActivity.this,
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
		Intent intent = new Intent(this, UdeskCaseActivity.class);
		startActivity(intent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

}
