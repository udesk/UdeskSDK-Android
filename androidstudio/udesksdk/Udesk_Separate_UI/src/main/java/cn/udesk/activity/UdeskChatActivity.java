package cn.udesk.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskUtil;
import cn.udesk.activity.MessageAdatper.AudioViewHolder;
import cn.udesk.adapter.UDEmojiAdapter;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.presenter.ChatActivityPresenter;
import cn.udesk.presenter.IChatActivityView;
import cn.udesk.voice.RecordFilePlay;
import cn.udesk.voice.RecordPlay;
import cn.udesk.voice.RecordPlayCallback;
import cn.udesk.voice.RecordStateCallback;
import cn.udesk.voice.RecordTouchListener;
import cn.udesk.widget.UDPullGetMoreListView;
import cn.udesk.widget.UdeskConfirmPopWindow;
import cn.udesk.widget.UdeskConfirmPopWindow.OnPopConfirmClick;
import cn.udesk.widget.UdeskExpandableLayout;
import cn.udesk.widget.UdeskMultiMenuHorizontalWindow;
import cn.udesk.widget.UdeskMultiMenuHorizontalWindow.OnPopMultiMenuClick;
import cn.udesk.widget.UdeskPopVoiceWindow;
import cn.udesk.widget.UdeskPopVoiceWindow.UdeskTimeEndCallback;
import cn.udesk.widget.UdeskTitleBar;
import cn.udesk.xmpp.UdeskMessageManager;
import udesk.core.UdeskCoreConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.UdeskLogUtil;
import udesk.core.model.AgentInfo;
import udesk.core.model.MessageInfo;
import udesk.core.utils.UdeskUtils;

public class UdeskChatActivity extends Activity implements IChatActivityView,
		OnClickListener, OnTouchListener, OnLongClickListener,
		OnItemClickListener, RecordStateCallback, UdeskTimeEndCallback {

	private UdeskTitleBar mTitlebar;
	private ImageView recordView;//录音图片，用户可根据自己的需求自行设置。
	private ImageView keyboardView;
	private EditText mInputEditView;
	private TextView tvSend;
	private TextView recordBack;

	private ImageView showEmjoImg;//表情选择图片，用户可根据自己的需求自行设置。
	private GridView emjoGridView;
	private View emojisPannel;
	private UDEmojiAdapter mEmojiAdapter;

	private ImageView showOptionMore;//加号更多选择器图片，用户可根据自己的需求自行设置。
	private View showImgPannel;
	private View btnPhoto, btnCamera;//拍照和图片选择图标，用户可根据自己的需求自行设置。

	private UDPullGetMoreListView mListView;
	private MessageAdatper mChatAdapter;
	private UdeskPopVoiceWindow mVoicePopWindow;
	private RecordFilePlay mRecordFilePlay;
	private RecordPlayCallback mPlayCallback;
	// 标记当前是否有客服在线，客服不在线状态是不能发送消息的，
	private boolean currentStatusIsOnline = false;
	private int historyCount = 0; // 记录数据库中总的记录数
	private int offset = -1; // 标记偏移值
	private Uri photoUri;
	private AgentInfo mAgentInfo;
	private final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
	private final int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = 102;
	private final int SELECT_SURVY_OPTION_REQUEST_CODE = 103;
	private final long QUEUE_RETEY_TIME = 10 * 1000;
	private final int initViewMode = 1;
	private final int pullRefreshModel = 2;
	private UdeskExpandableLayout expandableLayout = null;
	private boolean isNeedStartExpandabLyout = false;
	private UdeskConfirmPopWindow formWindow = null;
	private int agentFlag = UdeskConst.AgentFlag.NoAgent;
	private MessageInfo redirectMsg;
	private String groupId = "";
	private String agentId = "";
	private boolean isNeedRelogin = false;

	private ChatActivityPresenter mPresenter = new ChatActivityPresenter(this);

	public static class MessageWhat {
		public static final int loadHistoryDBMsg = 1;
		public static final int NoAgent = 2;
		public static final int HasAgent = 3;
		public static final int WaitAgent = 4;
		public static final int refreshAdapter = 5;
		public static final int changeImState = 6;
		public static final int onNewMessage = 7;
		public static final int RECORD_ERROR = 8;
		public static final int RECORD_Success = 9;
		public static final int RECORD_Too_Short = 10;
		public static final int RECORD_CANCEL = 11;
		public static final int UPDATE_VOCIE_STATUS = 12;
		public static final int recordllegal = 13;
		public static final int status_notify = 14;
		public static final int redirectSuccess = 15;
		public static final int surveyNotify = 16;
	}

	private BroadcastReceiver mConnectivityChangedReceiver = null;

	class ConnectivtyChangedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
					.getAction()))
				return;
			boolean bNetWorkAvailabl = UdeskUtils.isNetworkConnected(context);
			if (bNetWorkAvailabl) {
				if (!currentStatusIsOnline && isNeedRelogin) {
					mPresenter.getCustomerId();
				}
			} else {
				isNeedRelogin = true;
				UdeskUtils.showToast(
						context,
						context.getResources().getString(
								R.string.udesk_has_wrong_net));
				setAgentStatus("", View.GONE);
				currentStatusIsOnline = false;
			}
		}
	}

	private void registerNetWorkReceiver() {
		if (mConnectivityChangedReceiver == null) {
			mConnectivityChangedReceiver = new ConnectivtyChangedReceiver();
			UdeskChatActivity.this.registerReceiver(
					mConnectivityChangedReceiver, new IntentFilter(
							ConnectivityManager.CONNECTIVITY_ACTION));

		}
	}

	private void unRegister() {
		if (mConnectivityChangedReceiver != null) {
			UdeskChatActivity.this
					.unregisterReceiver(mConnectivityChangedReceiver);
			mConnectivityChangedReceiver = null;
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if(UdeskChatActivity.this.isFinishing()){
				return;
			}
			switch (msg.what) {
			case MessageWhat.loadHistoryDBMsg:
				List<MessageInfo> msgs = (ArrayList<MessageInfo>) msg.obj;
				mChatAdapter.listAddItems(msgs);
				mListView.onRefreshComplete();
				if (msg.arg1 == initViewMode) {
					mListView.setSelection(msgs.size());
				} else {
					mListView.setSelection(0);
				}

				break;
			case MessageWhat.NoAgent:
				mAgentInfo = (AgentInfo) msg.obj;
				setAgentStatus(mAgentInfo.message, View.VISIBLE);
				agentFlag = UdeskConst.AgentFlag.NoAgent;
				confirmToForm();
				break;
			case MessageWhat.HasAgent:
				mAgentInfo = (AgentInfo) msg.obj;
				showOnlieStatus(mAgentInfo);
				currentStatusIsOnline = true;
				mPresenter.SelfretrySendMsg();
				break;
			case MessageWhat.WaitAgent:
				mAgentInfo = (AgentInfo) msg.obj;
				setAgentStatus(mAgentInfo.message, View.VISIBLE);
				agentFlag = UdeskConst.AgentFlag.WaitAgent;
				this.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						mPresenter.getAgentInfo();
					}
				}, QUEUE_RETEY_TIME);
				break;
			case MessageWhat.refreshAdapter:
				if (mChatAdapter != null) {
					notifyRefresh();
				}
				break;
			case MessageWhat.changeImState:
				String msgId = (String) msg.obj;
				int flag = msg.arg1;
				changeImState(msgId, flag);
				break;
			case MessageWhat.onNewMessage:
				MessageInfo msgInfo = (MessageInfo) msg.obj;
				if(msgInfo.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_REDIRECT)){
					try {
						redirectMsg = msgInfo;
						JSONObject json = new JSONObject(msgInfo.getMsgContent());
						String agent_id = json.optString("agent_id");
						String group_id = json.optString("group_id");
						mPresenter.getRedirectAgentInfo(agent_id,group_id);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}else{
					if ( mChatAdapter != null) {
						mChatAdapter.addItem(msgInfo);
						notifyRefresh();
					}
				}
				break;
			case MessageWhat.RECORD_ERROR:
				disMissPopWindow();
				UdeskUtils.showToast(UdeskChatActivity.this, getResources()
						.getString(R.string.udesk_im_record_error));
				break;
			case MessageWhat.RECORD_Success:
				disMissPopWindow();
				break;
			case MessageWhat.RECORD_Too_Short:
				if (mVoicePopWindow != null) {
					mVoicePopWindow.showTooShortHint();
					mVoicePopWindow = null;
				}
				break;
			case MessageWhat.RECORD_CANCEL:
				disMissPopWindow();
				break;
			case MessageWhat.UPDATE_VOCIE_STATUS:
				updateRecordStatus(msg.arg1);
				break;
			case MessageWhat.recordllegal:
				disMissPopWindow();
				UdeskUtils.showToast(UdeskChatActivity.this, getResources()
						.getString(R.string.udesk_recordllegal));
				break;
			case MessageWhat.status_notify:
				int onlineflag = msg.arg1;
				String jid = (String) msg.obj;
				if (onlineflag == UdeskCoreConst.ONLINEFLAG) {
					if (mAgentInfo == null
							|| TextUtils.isEmpty(mAgentInfo.agentJid)) {
						mPresenter.getCustomerId();
						return;
					}
					if( !jid.contains(mAgentInfo.agentJid)){
						return;
					}
					showOnlieStatus(mAgentInfo);
					if (!currentStatusIsOnline && isNeedStartExpandabLyout) {
						expandableLayout.startAnimation(true);
						currentStatusIsOnline = true;
						isNeedStartExpandabLyout = false;
					}
					if (formWindow != null) {
						formWindow.cancle();
						formWindow = null;
					}
				} else if (onlineflag == UdeskCoreConst.OFFLINEFLAG) {
					setAgentStatus(
							UdeskChatActivity.this
									.getString(R.string.udesk_label_customer_offline),
							View.VISIBLE);
					if (currentStatusIsOnline) {
						expandableLayout.startAnimation(false);
						currentStatusIsOnline = false;
						isNeedStartExpandabLyout = true;
					}
				}
				break;
			case MessageWhat.redirectSuccess:
				MessageInfo redirectSuccessmsg = (MessageInfo) msg.obj;
				if ( mChatAdapter != null) {
					mChatAdapter.addItem(redirectSuccessmsg);
					notifyRefresh();
				}
				showOnlieStatus(mAgentInfo);
				currentStatusIsOnline = true;
				break;
			case MessageWhat.surveyNotify:
				
				SurveyOptionsModel surveyOptions = (SurveyOptionsModel) msg.obj;
				if(surveyOptions != null){
					toLuanchSurveyActivity(surveyOptions);
				}
				break;

			}
		}
	};
	
	private void toLuanchSurveyActivity(SurveyOptionsModel surveyOptions){
		Intent intent = new Intent();
		intent.setClass(UdeskChatActivity.this, SurvyDialogActivity.class);
		intent.putExtra(UdeskConst.SurvyDialogKey, surveyOptions);
		startActivityForResult(intent, SELECT_SURVY_OPTION_REQUEST_CODE);
	}
	
	private void showOnlieStatus(AgentInfo mAgentInfo){
		if(mAgentInfo == null){
			return;
		}
		String name = getString(
				R.string.udesk_label_customer_online,
				TextUtils.isEmpty(mAgentInfo.agentNick) ? "" : " "
						+ mAgentInfo.agentNick + " ");
		setAgentStatus(new String(name), View.VISIBLE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.udesk_activity_im);
		initIntent();
		initView();
		settingTitlebar();
	}

	private void initIntent(){
		Intent intent = getIntent();
		if(intent != null){
			groupId = intent.getStringExtra(UdeskConst.UDESKGROUPID);
			agentId = intent.getStringExtra(UdeskConst.UDESKAGENTID);
			if(UdeskLogUtil.DEBUG){
				Log.i("xxx","groupid = " + groupId + ";agentId = " + agentId);
			}
		}
	}

	private void initView() {
		formWindow = new UdeskConfirmPopWindow(this);
		recordView = (ImageView) findViewById(R.id.udesk_bottom_record);
		keyboardView = (ImageView) findViewById(R.id.udesk_bottom_keyboard);
		mInputEditView = (EditText) findViewById(R.id.udesk_bottom_input);
		setInputEditView();
		tvSend = (TextView) findViewById(R.id.udesk_bottom_send);
		tvSend.setOnClickListener(this);
		recordView.setOnClickListener(this);
		keyboardView.setOnClickListener(this);
		recordBack = (TextView) findViewById(R.id.udesk_im_long_voice_view);
		recordBack.setOnLongClickListener(this);
		mListView = (UDPullGetMoreListView) findViewById(R.id.udesk_conversation);

		emojisPannel = findViewById(R.id.udesk_bottom_emojis);
		showEmjoImg = (ImageView) findViewById(R.id.udesk_bottom_show_emoji);
		showEmjoImg.setOnClickListener(this);
		mEmojiAdapter = new UDEmojiAdapter(this);
		emjoGridView = (GridView) findViewById(R.id.udesk_bottom_emoji_pannel);
		emjoGridView.setAdapter(mEmojiAdapter);
		emjoGridView.setOnItemClickListener(this);

		showImgPannel = findViewById(R.id.udesk_bottom_options);
		showOptionMore = (ImageView) findViewById(R.id.udesk_bottom_show_option);
		showOptionMore.setOnClickListener(this);
		btnCamera = findViewById(R.id.udesk_bottom_option_camera);
		btnCamera.setOnClickListener(this);
		btnPhoto = findViewById(R.id.udesk_bottom_option_photo);
		btnPhoto.setOnClickListener(this);
		expandableLayout = (UdeskExpandableLayout) findViewById(R.id.udesk_change_status_info);
		setListView();

	}

	@Override
	protected void onResume() {
		super.onResume();
		initDatabase();
		mPresenter.getIMCustomerInfo();

		if(UdeskUtils.isNetworkConnected(this)){
			isNeedRelogin = false ;
		}else{
			isNeedRelogin = true;
		}
		registerNetWorkReceiver();
	}

	/**
	 * titlebar 的设置
	 */
	private void settingTitlebar() {
		mTitlebar = (UdeskTitleBar) findViewById(R.id.udesktitlebar);
		if (mTitlebar != null) {
			mTitlebar
					.setTitleTextSequence(getString(R.string.udesk_navi_im_title_online));
			mTitlebar.setLeftTextVis(View.VISIBLE);
			mTitlebar.setLeftViewClick(new OnClickListener() {

				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}
	}

	private void setAgentStatus(String message, int vis) {
		if (mTitlebar != null) {
			mTitlebar.setStateTextSequence(message);
			mTitlebar.setStateTextVis(vis);

		}
	}

	private void setInputEditView() {
		mInputEditView.setOnTouchListener(this);
		mInputEditView.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable arg0) {
			}

			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				int charSequencelength = mInputEditView.getText().toString()
						.length();
				if (charSequencelength > 0) {
					showSendTxtView();
				} else {
					showRecordView();
				}

			}

		});
	}

	private void setUdeskRecordBackVis(int vis) {
		recordBack.setVisibility(vis);
	}

	private void setUdeskKeyboardView(int vis) {
		keyboardView.setVisibility(vis);
	}

	private void setUdeskRecordView(int vis) {
		recordView.setVisibility(vis);
	}

	private void setUdeskInputEditViewVis(int vis) {
		mInputEditView.setVisibility(vis);
	}

	private void setUdeskSendViewVis(int vis) {
		tvSend.setVisibility(vis);
	}

	private void setUdeskShowOptionMoreVis(int vis) {
		showOptionMore.setVisibility(vis);
	}

	private void setUdeskShowImgPannelVis(int vis) {
		showImgPannel.setVisibility(vis);
	}

	private void setUdeskEmojisPannel(int vis) {
		emojisPannel.setVisibility(vis);
		emjoGridView.setVisibility(View.VISIBLE);
	}

	private void setUdeskEditClickabled(EditText editText) {
		editText.setFocusable(true);
		editText.setFocusableInTouchMode(true);
		editText.requestFocus();
	}

	private void showSendTxtView() {
		setUdeskShowOptionMoreVis(View.GONE);
		setUdeskSendViewVis(View.VISIBLE);

	}

	protected void showRecordView() {
		setUdeskSendViewVis(View.GONE);
		setUdeskShowOptionMoreVis(View.VISIBLE);

	}

	private void setListView() {
		mChatAdapter = new MessageAdatper(this);
		mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		mListView.setAdapter(mChatAdapter);
		mListView
				.setOnRefreshListener(new UDPullGetMoreListView.OnRefreshListener() {
					@Override
					public void onRefresh() {
						loadHistoryRecords(pullRefreshModel);
					}
				});

		mListView.setRecyclerListener(new AbsListView.RecyclerListener() {
			public void onMovedToScrapHeap(View view) {
				if (mRecordFilePlay != null) {
					checkRecoredView(view);
				}
			}
		});
	}

	private void initDatabase() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				UdeskDBManager.getInstance().setContext(UdeskChatActivity.this);
				historyCount = UdeskDBManager.getInstance().getMessageCount();
				UdeskDBManager.getInstance().updateSendFlagToFail();
				loadHistoryRecords(initViewMode);
			}
		});
	}

	/**
	 * 读取数据库中的历史数据
	 */
	private void loadHistoryRecords(int mode) {
		mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		// 已经没有更早的数据了
		if (offset == 0) {
			UdeskUtils.showToast(this,
					getString(R.string.udesk_no_more_history));
			mListView.onRefreshComplete();
			mListView.setSelection(0);
		} else {
			// 还有老数据
			if (offset == -1) {
				offset = historyCount - UdeskConst.UDESK_HISTORY_COUNT;
			} else {
				offset = offset - UdeskConst.UDESK_HISTORY_COUNT;
			}
			offset = (offset < 0 ? 0 : offset);
			List<MessageInfo> list = UdeskDBManager.getInstance().getMessages(
					offset, UdeskConst.UDESK_HISTORY_COUNT);
			Message msg = Message.obtain();
			msg.what = MessageWhat.loadHistoryDBMsg;
			msg.arg1 = mode;
			msg.obj = list;
			mHandler.sendMessage(msg);
		}

	}

	private void notifyRefresh() {
		mChatAdapter.notifyDataSetChanged();
		mListView.smoothScrollToPosition(mChatAdapter.getCount());
	}

	private void checkRecoredView(View view) {
		Object tag = view.getTag();
		if (tag == null || !(tag instanceof AudioViewHolder)) {
			return;
		}

		AudioViewHolder holder = (AudioViewHolder) tag;
		final RecordFilePlay recordFilePlay = mRecordFilePlay;
		if (recordFilePlay != null) {
			String path = recordFilePlay.getMediaPath();
			if (path != null
					&& (path.equalsIgnoreCase(holder.message.getLocalPath()) || path
							.equalsIgnoreCase(holder.message.getMsgContent()))) {
				recordFilePlay.recycleCallback();
			}
		}
	}

	private void changeImState(String msgId, int state) {
		if (!TextUtils.isEmpty(msgId) && mListView != null
				&& mChatAdapter != null) {
			for (int i = mListView.getChildCount() - 1; i >= 0; i--) {
				View child = mListView.getChildAt(i);
				if (child != null) {
					if (mChatAdapter.changeImState(child, msgId, state)) {
						return;
					}
				}
			}
		}
	}

	@Override
	public Context getContext() {
		return this;
	}

	@Override
	public void showFailToast(final String failMsg) {

		this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				UdeskUtils.showToast(UdeskChatActivity.this, failMsg);
			}
		});
	}

	@Override
	public void dealAgentInfo(AgentInfo agentInfo) {

		switch (agentInfo.agentCode) {
		case UdeskConst.AgentReponseCode.NoAgent:
			Message msgNoAgent = mHandler.obtainMessage(MessageWhat.NoAgent);
			msgNoAgent.obj = agentInfo;
			mHandler.sendMessage(msgNoAgent);
			break;
		case UdeskConst.AgentReponseCode.HasAgent:
			// 有客服连接xmpp, titlebar上显示
			UdeskMessageManager.getInstance().loginXmpp();
			Message msgHasAgent = mHandler.obtainMessage(MessageWhat.HasAgent);
			msgHasAgent.obj = agentInfo;
			mHandler.sendMessage(msgHasAgent);
			break;
		case UdeskConst.AgentReponseCode.WaitAgent:
			Message msgWaitAgent = mHandler
					.obtainMessage(MessageWhat.WaitAgent);
			msgWaitAgent.obj = agentInfo;
			mHandler.sendMessage(msgWaitAgent);
			break;
		case UdeskConst.AgentReponseCode.NonExistentAgent:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(UdeskChatActivity.this,"客服不存在，请核对输入的客服ID是否正确",Toast.LENGTH_SHORT).show();
					}
				});
				break;
		case UdeskConst.AgentReponseCode.NonExistentGroupId:
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(UdeskChatActivity.this,"客服组不存在，请核对输入的客服组ID是否正确",Toast.LENGTH_SHORT).show();
					}
				});
				break;

		default:
			break;
		}
	}
	
	@Override
	public void dealRedirectAgentInfo(AgentInfo agentInfo) {
		switch (agentInfo.agentCode) {
		case UdeskConst.AgentReponseCode.NoAgent:
			Message msgNoAgent = mHandler.obtainMessage(MessageWhat.NoAgent);
			msgNoAgent.obj = agentInfo;
			mHandler.sendMessage(msgNoAgent);
			break;
		case UdeskConst.AgentReponseCode.HasAgent:
			// 有客服连接xmpp, titlebar上显示
			UdeskMessageManager.getInstance().loginXmpp();
			String redirectTip = "客服转接成功，"+ agentInfo.agentNick +"为您服务";
			if(redirectMsg != null){
				redirectMsg.setMsgContent(redirectTip);
				UdeskDBManager.getInstance().addMessageInfo(redirectMsg);
			}
			mAgentInfo = agentInfo;
			Message msgHasRedirect = mHandler.obtainMessage(MessageWhat.redirectSuccess);
			msgHasRedirect.obj = redirectMsg;
			mHandler.sendMessage(msgHasRedirect);
			break;
		case UdeskConst.AgentReponseCode.WaitAgent:
			Message msgWaitAgent = mHandler
					.obtainMessage(MessageWhat.WaitAgent);
			msgWaitAgent.obj = agentInfo;
			mHandler.sendMessage(msgWaitAgent);
			break;
		default:
			break;
		}
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == mInputEditView && event.getAction() == MotionEvent.ACTION_DOWN) {
			bottomoPannelBegginStatus();
		}
		return false;
	}

	@Override
	public CharSequence getInputContent() {
		return mInputEditView.getText();
	}

	@Override
	public void clearInputContent() {
		mInputEditView.setText("");
	}

	@Override
	public void onClick(View v) {
		if (!isShowNotSendMsg()) {
			UdeskUtils.hideSoftKeyboard(this, mInputEditView);
			return;
		}

		if (R.id.udesk_bottom_send == v.getId()) {
			if (TextUtils.isEmpty(mInputEditView.getText().toString())) {
				UdeskUtils.showToast(UdeskChatActivity.this,
						getString(R.string.udesk_send_message_empty));
				return;
			}
			mPresenter.sendTxtMessage();
		} else if (R.id.udesk_bottom_show_emoji == v.getId()) {

			if (emojisPannel.getVisibility() == View.VISIBLE) {
				bottomoPannelBegginStatus();
			} else {
				bottomoPannelBegginStatus();
				setUdeskEmojisPannel(View.VISIBLE);
			}
			setUdeskEditClickabled(mInputEditView);
			UdeskUtils.hideSoftKeyboard(this, mInputEditView);
		} else if (R.id.udesk_bottom_show_option == v.getId()) {
			if (showImgPannel.getVisibility() == View.VISIBLE) {
				bottomoPannelBegginStatus();
			} else {
				bottomoPannelBegginStatus();
				setUdeskShowImgPannelVis(View.VISIBLE);
			}
			UdeskUtils.hideSoftKeyboard(this, mInputEditView);
		} else if (R.id.udesk_bottom_option_photo == v.getId()) {
			selectPhoto();
			bottomoPannelBegginStatus();
		} else if (R.id.udesk_bottom_option_camera == v.getId()) {
			takePhoto();
			bottomoPannelBegginStatus();
		} else if (R.id.udesk_bottom_record == v.getId()) {
			recordAudioStatus();
		} else if (R.id.udesk_bottom_keyboard == v.getId()) {
			inputMsgStatus();
		}

	}

	private void inputMsgStatus() {
		setUdeskKeyboardView(View.GONE);
		setUdeskRecordView(View.VISIBLE);
		setUdeskEmojisPannel(View.GONE);
		setUdeskShowImgPannelVis(View.GONE);
		setUdeskRecordBackVis(View.GONE);
		setUdeskInputEditViewVis(View.VISIBLE);
		setUdeskEditClickabled(mInputEditView);
		UdeskUtils.hideSoftKeyboard(this, mInputEditView);
	}

	private void recordAudioStatus() {
		UdeskUtils.hideSoftKeyboard(this, mInputEditView);
		setUdeskShowImgPannelVis(View.GONE);
		setUdeskInputEditViewVis(View.GONE);
		setUdeskRecordBackVis(View.VISIBLE);
		setUdeskEmojisPannel(View.GONE);
		setUdeskKeyboardView(View.VISIBLE);
		setUdeskRecordView(View.GONE);
	}

	private void bottomoPannelBegginStatus() {
		setUdeskShowImgPannelVis(View.GONE);
		setUdeskRecordBackVis(View.GONE);
		setUdeskInputEditViewVis(View.VISIBLE);
		setUdeskEmojisPannel(View.GONE);
		setUdeskKeyboardView(View.GONE);
		setUdeskRecordView(View.VISIBLE);
		emjoGridView.setVisibility(View.GONE);
	}

	private boolean isShowNotSendMsg() {
		if (!UdeskUtils.isNetworkConnected(this)) {
			UdeskUtils.showToast(this,
					getResources().getString(R.string.udesk_has_wrong_net));
			return false;
		}

		if (!currentStatusIsOnline) {
			confirmToForm();
			return false;
		}

		return true;
	}

	@Override
	public boolean onLongClick(View v) {
		if (v.getId() == R.id.udesk_im_long_voice_view) {
			if (!UdeskUtils.checkSDcard()) {
				Toast.makeText(this,
						getResources().getString(R.string.udesk_label_no_sd),
						Toast.LENGTH_LONG).show();
				return false;
			}

			if (mRecordFilePlay != null) {
				showStartOrStopAnaimaition(
						mRecordFilePlay.getPlayAduioMessage(), false);
				recycleVoiceRes();
			}
			recordBack.setOnTouchListener(new RecordTouchListener(this,
					UdeskChatActivity.this));
			if (mPresenter != null) {
				// 开始录音
				mPresenter.recordStart();
			}

			return true;
		}
		return false;
	}

	private void confirmToForm() {
		try {

			String positiveLabel = this.getString(R.string.udesk_ok);

			String negativeLabel = this.getString(R.string.udesk_cancel);
			String  title = this.getString(R.string.udesk_msg_offline_to_form);
			if(agentFlag == UdeskConst.AgentFlag.WaitAgent){
				title = this.getString(R.string.udesk_msg_busyline_to_form);
			}
			if(UdeskChatActivity.this.isFinishing()){
				return;
			}
			if(!formWindow.isShowing()){
			formWindow.show(this, this.getWindow().getDecorView(),
					positiveLabel, negativeLabel, title,
					new OnPopConfirmClick() {
						public void onPositiveClick() {
							goToForm();
						}

						@Override
						public void onNegativeClick() {
						}

					});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void goToForm() {
		Intent intent = new Intent(UdeskChatActivity.this,
				UdeskFormActivity.class);
		startActivity(intent);
		dismissFormWindow();
	}

	private void dismissFormWindow() {
		if (formWindow != null) {
			formWindow.dismiss();
		}
	}

	@Override
	public void addMessage(MessageInfo message) {
		mChatAdapter.addItem(message);
		Message msgWaitAgent = mHandler
				.obtainMessage(MessageWhat.refreshAdapter);
		mHandler.sendMessage(msgWaitAgent);
	}

	@Override
	public AgentInfo getAgentInfo() {
		if (mAgentInfo != null) {
			return mAgentInfo;
		}
		return new AgentInfo();
	}

	@Override
	public Handler getHandler() {
		return mHandler;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (parent == emjoGridView) {
			if (mPresenter != null) {
				mPresenter.clickEmoji(id, mEmojiAdapter.getCount(),
						mEmojiAdapter.getItem((int) id));
			}
		}

	}

	@Override
	public void refreshInputEmjio(String s) {
		mInputEditView.setText(UDEmojiAdapter.replaceEmoji(this, s,
				(int) mInputEditView.getTextSize()));

	}

	@Override
	public List<String> getEmotionStringList() {
		List<String> emotionList = new ArrayList<String>();
		int emojiSum = mEmojiAdapter.getCount();
		for (int i = 0; i < emojiSum; i++) {
			if (mEmojiAdapter.getItem(i) != null) {
				emotionList.add(mEmojiAdapter.getItem(i));
			}
		}
		return emotionList;
	}

	private void selectPhoto() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("image/*");
		startActivityForResult(intent, SELECT_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	private void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		photoUri = UdeskUtil.getOutputMediaFileUri();
		intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) {
			if (Activity.RESULT_OK == resultCode) {
				if (data != null) {
					if (data.hasExtra("data")) {
						Bitmap thumbnail = data.getParcelableExtra("data");
						if (thumbnail != null) {
							if (mPresenter != null) {
								mPresenter.sendBitmapMessage(thumbnail);
							}
						}
					}
				} else {
					if (mPresenter != null) {
						mPresenter.sendBitmapMessage(photoUri.getPath());
					}
				}
			}
		} else if (SELECT_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) {

			if (resultCode != Activity.RESULT_OK || data == null) {
				return;
			}

			Uri mImageCaptureUri = data.getData();
			if (mImageCaptureUri != null) {
				ContentResolver cr = UdeskChatActivity.this
						.getContentResolver();
				try {
					Bitmap bitmap = BitmapFactory.decodeStream(cr
							.openInputStream(mImageCaptureUri));
					if (mPresenter != null) {
						mPresenter.sendBitmapMessage(bitmap);
					}

				} catch (FileNotFoundException e) {

				}
			} else {
				Bundle extras = data.getExtras();
				if (extras != null) {
					Bitmap bitmap = extras.getParcelable("data");
					if (bitmap != null) {
						if (mPresenter != null) {
							mPresenter.sendBitmapMessage(bitmap);
						}

					}
				}
			}

		}else if(SELECT_SURVY_OPTION_REQUEST_CODE == requestCode){
			if (resultCode != Activity.RESULT_OK || data == null) {
				return;
			}
			Toast.makeText(UdeskChatActivity.this,"感谢您的评价！",Toast.LENGTH_SHORT).show();
			String optionId =  data.getStringExtra(UdeskConst.SurvyOptionIDKey);
			mPresenter.putIMSurveyResult(optionId);
		}

	}

	public void disMissPopWindow() {
		if (mVoicePopWindow != null) {
			mVoicePopWindow.dismiss();
			mVoicePopWindow = null;
			recordBack.setText(UdeskChatActivity.this.getResources().getString(
					R.string.udesk_label_im_long_voice));
		}
	}

	@Override
	public void onTimeOver() {
		if (mPresenter != null) {
			mPresenter.doRecordStop(false);
		}
	}

	@Override
	public void readyToCancelRecord() {
		if (mVoicePopWindow != null) {
			mVoicePopWindow.readyToCancelRecord();
		}
	}

	@Override
	public void doCancelRecord() {
		if (mPresenter != null) {
			mPresenter.doRecordStop(true);
		}
	}

	@Override
	public void readyToContinue() {
		if (mVoicePopWindow != null) {
			mVoicePopWindow.readyToContinue();
		}
	}

	@Override
	public void endRecord() {
		if (mPresenter != null) {
			mPresenter.doRecordStop(false);
		}
	}

	@Override
	public void showmVoicePopWindow() {
		mVoicePopWindow = new UdeskPopVoiceWindow(this, this);
		mVoicePopWindow.show(this, mListView);
		recordBack.setText(UdeskChatActivity.this.getResources().getString(
				R.string.udesk_cancle_im_long_voice));
	}

	// @Override
	public void showStartOrStopAnaimaition(final MessageInfo info,
			final boolean isstart) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (info == null) {
					return;
				}
				for (int i = 0, count = mListView.getChildCount(); i < count; i++) {
					View child = mListView.getChildAt(i);
					if (child == null || child.getTag() == null
							|| !(child.getTag() instanceof AudioViewHolder)) {
						continue;
					}
					AudioViewHolder holder = (AudioViewHolder) child.getTag();
					MessageInfo msgTemp = holder.message;
					holder.endAnimationDrawable();
					if (msgTemp != info) {
						msgTemp = info;
						continue;
					} else {
						if (isstart) {
							holder.startAnimationDrawable();
						} else {
							holder.endAnimationDrawable();
						}

					}

				}

			}
		});

	}

	// 点击播放录音及动画

	public void clickRecordFile(MessageInfo message) {

		if (mRecordFilePlay == null) {
			mRecordFilePlay = new RecordPlay();

		}
		if (mPlayCallback == null) {
			mPlayCallback = new RecordPlayCallback() {
				public void onPlayComplete(MessageInfo message) {
					showStartOrStopAnaimaition(message, false);
					recycleVoiceRes();
				}

				public void onPlayStart(MessageInfo message) {
					showStartOrStopAnaimaition(message, true);
				}

				public void onPlayPause(MessageInfo message) {
					showStartOrStopAnaimaition(message, false);
					recycleVoiceRes();
				}

				public void onPlayEnd(MessageInfo message) {
					showStartOrStopAnaimaition(message, false);
					recycleVoiceRes();// 新添加
				}

				@Override
				public void endAnimation() {
					if (mChatAdapter != null) {
						List<MessageInfo> list = mChatAdapter.getList();
						int size = list.size();
						for (int i = 0; i < size; i++) {
							MessageInfo message = list.get(i);
							if (message.isPlaying) {
								showStartOrStopAnaimaition(message, false);
							}
						}
					}

				}

			};

		}
		mRecordFilePlay.click(message, mPlayCallback);

	}

	private void recycleVoiceRes() {
		if (mRecordFilePlay != null) {
			mRecordFilePlay.recycleRes();
			mRecordFilePlay.recycleCallback();
			mRecordFilePlay = null;
		}

		mPlayCallback = null;
	}

	@Override
	public void onRecordSuccess(String filePath, long duration) {
		mHandler.sendEmptyMessage(MessageWhat.RECORD_Success);
		if (mPresenter != null) {
			mPresenter.sendRecordAudioMsg(filePath, duration);
		}
	}

	@Override
	public void setRecordBackgroundNullTouchListener() {
		recordBack.setOnTouchListener(null);
	}

	@Override
	protected void onStop() {
		super.onStop();
		recycleVoiceRes();

	}

	@Override
	protected void onDestroy() {
		unRegister();
		UdeskHttpFacade.getInstance().cancel();
		UdeskMessageManager.getInstance().cancelXmppConnect();
		mPresenter.unBind();
		super.onDestroy();
		
	}

	private void updateRecordStatus(int status) {
		if (mVoicePopWindow != null) {
			mVoicePopWindow.updateRecordStatus(status);
		}
	}

	public void handleText(final MessageInfo message, View targetView) {
		String[] menuLabel = new String[] { getResources().getString(
				R.string.udesk_copy) };
		UdeskMultiMenuHorizontalWindow menuWindow = new UdeskMultiMenuHorizontalWindow(
				UdeskChatActivity.this);
		menuWindow.show(UdeskChatActivity.this, targetView, menuLabel,
				new OnPopMultiMenuClick() {
					public void onMultiClick(int MenuIndex) {
						if (MenuIndex == 0) {
							doCopy(message.getMsgContent());
						}
					}
				});
	}

	@SuppressLint("NewApi")
	private void doCopy(String content) {
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB) {
			android.content.ClipboardManager c = (android.content.ClipboardManager) UdeskChatActivity.this
					.getSystemService(Activity.CLIPBOARD_SERVICE);
			c.setPrimaryClip(ClipData.newPlainText(null, content));
		} else {
			android.text.ClipboardManager c = (android.text.ClipboardManager) UdeskChatActivity.this
					.getSystemService(Activity.CLIPBOARD_SERVICE);
			c.setText(content);
		}
	}

	public void retrySendMsg(MessageInfo message) {
		if (mPresenter != null && message != null) {
			changeImState(message.getMsgId(), UdeskConst.SendFlag.RESULT_RETRY);
			mPresenter.startRetryMsg(message);
		}
	}

	public void previewPhoto(MessageInfo message) {
		String sourceImagePath = "";
		if (!TextUtils.isEmpty(message.getLocalPath())) {
			sourceImagePath = message.getLocalPath();
		} else {
			sourceImagePath = ImageLoader.getInstance().getDiscCache()
					.get(message.getMsgContent()).getPath();
		}
		File sourceFile = new File(sourceImagePath);
		if (sourceFile.exists()) {
			Intent intent = new Intent(UdeskChatActivity.this,
					UdeskZoomImageActivty.class);
			Bundle data = new Bundle();
			data.putParcelable("image_path", Uri.fromFile(sourceFile));
			intent.putExtras(data);
			startActivity(intent);
		}
	}


	@Override
	public String getAgentId() {
		return agentId;
	}

	@Override
	public String getGroupId() {
		return groupId;
	}
}
