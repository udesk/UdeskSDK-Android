package udesk.udesksdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.model.MsgNotice;
import cn.udesk.xmpp.UdeskMessageManager;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskHttpFacade;

/**
 * 
 * 在新创建个用户的时候，需要先设置相关的信息 1 调用 UdeskHttpFacade.getInstance().initApiKey
 * 保存domian和key信息，不涉及网络操作 2 UdeskHttpFacade.getInstance().setUserInfo 提交用户信息，
 */
public class UdeskInitActivity extends Activity implements OnClickListener {


	//输入唯一标识的sdktoken的EditText
	private EditText mSdktoken;
	//输入昵称的EditText
	private EditText  mNickname ;
	//输入邮箱的EditText
	private EditText  mEmail;
	//输入电话的EditText
	private EditText  mPhone;
	//输入微博账号的EditText
	private EditText  mWeiBoId;
	//输入微信账号的EditText
	private EditText  mWeiXinId;
	//输入描述账号的EditText
	private EditText  mDescribe;

	//替换成你们注册的域名
	private   String UDESK_DOMAIN = "udesksdk.udesk.cn";
	//替换成你们在后台生成的密钥
	private   String UDESK_SECRETKEY = "6c37f775019907785d85c027e29dae4e";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udesk_init_activity_view);
		initView();
		//传入注册的域名和密钥
		UdeskSDKManager.getInstance().initApiKey(this, UDESK_DOMAIN, UDESK_SECRETKEY);
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		mSdktoken = (EditText) findViewById(R.id.udesk_sdktoken);
		mNickname = (EditText) findViewById(R.id.udesk_nickname);
		mEmail = (EditText) findViewById(R.id.udesk_email);
		mPhone = (EditText) findViewById(R.id.udesk_phone);
		mWeiBoId = (EditText)findViewById(R.id.udesk_weiboName);
		mWeiXinId = (EditText)findViewById(R.id.udesk_weixinId);
		mDescribe = (EditText)findViewById(R.id.udesk_describe);

		findViewById(R.id.udesk_commituserinfo).setOnClickListener(this);
		findViewById(R.id.udesk_commit_selffield).setOnClickListener(this);
		/**
		 * 注册接收消息提醒事件
		 */
		UdeskMessageManager.getInstance().event_OnNewMsgNotice.bind(this, "OnNewMsgNotice");

		Log.i("xxx","UdeskInitActivity 中bind OnNewMsgNotice");
	}

	//提供：仅仅传入用户的基本信息   和  传入用户基本信息和自定义信息的2种使用情况
	@Override
	public void onClick(View v) {


		if (v.getId() == R.id.udesk_commituserinfo) {  	//仅仅传入用户的基本信息
			setUserInfo();
		} else if (v.getId() == R.id.udesk_commit_selffield) {    // 传入用户基本信息和自定义信息的2中使用情况
			commitSelffield();
		}
	}

	/**
	 * 设置用户基本信息
	 */
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

	/**
	 * 获取各个EditText编辑框中输入的内如  存入到Map容器中
	 * @return
     */
	private Map<String, String> getUserInfo() {

		Map<String, String> info = new HashMap<String, String>();
		info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, mSdktoken.getText().toString());
		//以下注释的字段都是可选的字段， 有邮箱建议填写
		info.put(UdeskConst.UdeskUserInfo.NICK_NAME, mNickname.getText().toString());
		info.put(UdeskConst.UdeskUserInfo.EMAIL, mEmail.getText().toString());
		info.put(UdeskConst.UdeskUserInfo.CELLPHONE, mPhone.getText().toString());
		info.put(UdeskConst.UdeskUserInfo.WEIXIN_ID,mWeiXinId.getText().toString());
		info.put(UdeskConst.UdeskUserInfo.WEIBO_NAME, mWeiBoId.getText().toString());
		info.put(UdeskConst.UdeskUserInfo.DESCRIPTION, mDescribe.getText().toString());
		return info;

	}

	/**
	 * 提交用户基本信息和自定义信息
	 */
	private void commitSelffield() {
		final HashMap<String, String> extraInfoTextField = new HashMap<String, String>();
		final HashMap<String, String> extraInfodRoplist = new HashMap<String, String>();
		UdeskHttpFacade.getInstance().getUserFields(UDESK_DOMAIN, UDESK_SECRETKEY, new UdeskCallBack() {

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
									//传入显示下拉列表的值
									if (child.has("options")) {
										// 设置下拉列表的第几个值，则传入第几个key值字符串， 如下所示
										extraInfodRoplist.put(
												child.getString("field_name"),
												"0");
									}
								} else if (type.equals("text")) {
									//传入自定义的文本信息
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



	/**
	 * 处理不在会话界面 收到消息的通知事例  方法名OnNewMsgNotice  对应于绑定事件
	 * UdeskMessageManager.getInstance().event_OnNewMsgNotice.bind(this,"OnNewMsgNotice")中参数的字符串
	 *
	 * @param msgNotice
	 */
	public void OnNewMsgNotice(MsgNotice msgNotice) {
		if (msgNotice != null) {
			Log.i("xxx","UdeskInitActivity 中收到msgNotice");
			NotificationUtils.getInstance().notifyMsg(UdeskInitActivity.this, msgNotice.getContent());
		}

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();

	}



}
