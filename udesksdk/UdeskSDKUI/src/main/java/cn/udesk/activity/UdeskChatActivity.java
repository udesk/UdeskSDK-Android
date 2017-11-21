package cn.udesk.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.udesk.PreferenceHelper;
import cn.udesk.R;
import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.activity.MessageAdatper.AudioViewHolder;
import cn.udesk.adapter.UDEmojiAdapter;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.model.SDKIMSetting;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.permission.RequestCode;
import cn.udesk.permission.XPermissionUtils;
import cn.udesk.presenter.ChatActivityPresenter;
import cn.udesk.presenter.IChatActivityView;
import cn.udesk.voice.RecordFilePlay;
import cn.udesk.voice.RecordPlay;
import cn.udesk.voice.RecordPlayCallback;
import cn.udesk.voice.RecordStateCallback;
import cn.udesk.voice.RecordTouchListener;
import cn.udesk.widget.HorVoiceView;
import cn.udesk.widget.UDPullGetMoreListView;
import cn.udesk.widget.UdeskConfirmPopWindow;
import cn.udesk.widget.UdeskConfirmPopWindow.OnPopConfirmClick;
import cn.udesk.widget.UdeskExpandableLayout;
import cn.udesk.widget.UdeskMultiMenuHorizontalWindow;
import cn.udesk.widget.UdeskMultiMenuHorizontalWindow.OnPopMultiMenuClick;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.UdeskCoreConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.event.InvokeEventContainer;
import udesk.core.model.AgentInfo;
import udesk.core.model.MessageInfo;
import udesk.core.utils.UdeskUtils;

public class UdeskChatActivity extends Activity implements IChatActivityView,
        OnClickListener, OnTouchListener, OnLongClickListener,
        OnItemClickListener, RecordStateCallback, HorVoiceView.UdeskTimeCallback {

    private Button sendBtn;
    private EditText mInputEditView;
    private View showEmjoImg;//表情选择图片，用户可根据自己的需求自行设置。
    private GridView emjoGridView;
    private ImageView audioPop;
    private HorVoiceView mHorVoiceView;
    private TextView udesk_audio_tips;
    private View emojisPannel;
    private View btnPhoto, btnCamera, btnsurvy, btnFile, btnLoaction;
    private View showVoiceImg;
    private View audioPanel;
    private View audioCancle;
    private View udeskImContainer;
    private UDEmojiAdapter mEmojiAdapter;
    private UDPullGetMoreListView mListView;
    private MessageAdatper mChatAdapter;
    private UdeskConfirmPopWindow popWindow = null;
    private UdeskExpandableLayout expandableLayout = null;  //动画显示上线离线提醒的控件
    private UdeskTitleBar mTitlebar;
    private RecordFilePlay mRecordFilePlay;
    private RecordPlayCallback mPlayCallback;
    private AgentInfo mAgentInfo;  // 保存客服信息的实例
    private MessageInfo redirectMsg;
    private Uri photoUri;
    private File cameraFile;

    private String groupId = "";
    private String agentId = "";
    private String isbolcked = "";
    private String bolckedNotice = "";

    // 标记当前是否有客服在线，客服不在线状态是不能发送消息的，
    private boolean currentStatusIsOnline = false;
    private boolean isNeedStartExpandabLyout = false;
    private boolean isNeedRelogin = false;
    private boolean hasSendCommodity = false;
    private boolean hasSendFirstMessage = false;
    private boolean hasAddCommodity = false;


    private int historyCount = 0; // 记录数据库中总的记录数
    private int offset = -1; // 标记偏移值
    private int initViewMode = 1;
    private int pullRefreshModel = 2;
    private int pullEVentModel = 3;
    private long preMsgSendTime = 0; //记录发送预支消息间隔时间
    private long QUEUE_RETEY_TIME = 25 * 1000; // 客服繁忙时  轮询的间隔时间

    private final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
    private final int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = 102;
    private final int SELECT_SURVY_OPTION_REQUEST_CODE = 103;
    private final int SELECT_FILE_OPTION_REQUEST_CODE = 104;
    private final int SELECT_LOCATION_OPTION_REQUEST_CODE = 105;

    private MyHandler mHandler;
    private ChatActivityPresenter mPresenter;
    private BroadcastReceiver mConnectivityChangedReceiver = null;
    private boolean isSurvyOperate = false;//如果是收到客服的满意度调查，则在onresume 处不在请求分配客服
    private boolean isInitComplete = false; //标识进入请求分配客服的流程是否结束
    private boolean isOverConversation = false;//标识会话是否客服已经关闭会话

    private boolean isLeavingmsg = false;
    private boolean isPermmitSurvy = true;
    private boolean isWait = false;
    private boolean isfirstWaitTips = true;
    private boolean isDestroyed = false;

    public static class MessageWhat {
        public static final int loadHistoryDBMsg = 1;
        public static final int NoAgent = 2;
        public static final int HasAgent = 3;
        public static final int WaitAgent = 4;
        public static final int refreshAdapter = 5;
        public static final int changeImState = 6;
        public static final int onNewMessage = 7;
        public static final int RECORD_ERROR = 8;
        public static final int RECORD_Too_Short = 10;
        public static final int UPDATE_VOCIE_STATUS = 12;
        public static final int recordllegal = 13;
        public static final int status_notify = 14;
        public static final int redirectSuccess = 15;
        public static final int surveyNotify = 16;
        public static final int IM_STATUS = 17;
        public static final int IM_BOLACKED = 18;
        public static final int Has_Survey = 19;
        public static final int Survey_error = 20;
        public static final int Add_UdeskEvent = 21;
        public static final int ChangeFielProgress = 22;
    }

    class ConnectivtyChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
                        .getAction()))
                    return;
                boolean bNetWorkAvailabl = UdeskUtils.isNetworkConnected(context);
                if (bNetWorkAvailabl) {
                    if (!currentStatusIsOnline && isNeedRelogin) {
                        if (isbolcked.equals("true")) {
                            return;
                        }
                        if (UdeskSDKManager.getInstance().getImSetting() != null && !UdeskSDKManager.getInstance().getImSetting().getIs_worktime()) {
                            return;
                        }
                        mPresenter.createIMCustomerInfo();
                    }
                } else {
                    isNeedRelogin = true;
                    UdeskUtils.showToast(
                            context,
                            context.getResources().getString(
                                    R.string.udesk_has_wrong_net));
                    setTitlebar(context.getResources().getString(
                            R.string.udesk_agent_connecting_error_net_uavailabl), "off");
                    currentStatusIsOnline = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class MyHandler extends Handler {
        WeakReference<UdeskChatActivity> mWeakActivity;

        public MyHandler(UdeskChatActivity activity) {
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                final UdeskChatActivity activity = mWeakActivity.get();
                if (activity == null) {
                    return;
                }
                switch (msg.what) {
                    case MessageWhat.loadHistoryDBMsg:
                        if (activity.mChatAdapter != null && activity.mListView != null) {
                            List<MessageInfo> msgs = (ArrayList<MessageInfo>) msg.obj;
                            if (UdeskBaseInfo.commodity != null && !activity.hasAddCommodity) {
                                msgs.add(UdeskBaseInfo.commodity);
                                activity.hasAddCommodity = true;
                            }
                            int selectIndex = msgs.size();
                            if (msg.arg1 == activity.pullEVentModel) {
                                activity.mChatAdapter.listAddEventItems(msgs);
                            } else {
                                activity.mChatAdapter.listAddItems(msgs);
                            }
                            activity.mListView.onRefreshComplete();
                            if (msg.arg1 == activity.initViewMode || msg.arg1 == activity.pullEVentModel) {
                                activity.mListView.setSelection(activity.mChatAdapter.getCount());
                            } else {
                                activity.mListView.setSelection(selectIndex);
                            }
                        }
                        break;
                    case MessageWhat.NoAgent:
                        activity.isWait = false;
                        if (activity.isleaveMessageTypeMsg()) {
                            activity.setUdeskImContainerVis(View.GONE);
                            activity.setTitlebar(activity.getString(R.string.udesk_ok), "off");
                        } else {
                            activity.mAgentInfo = (AgentInfo) msg.obj;
                            activity.setTitlebar(activity.mAgentInfo.getMessage(), "off");
                            activity.delayShowtips(this);
                        }
                        break;
                    case MessageWhat.HasAgent:
                        activity.isWait = false;
                        activity.setUdeskImContainerVis(View.VISIBLE);
                        activity.mAgentInfo = (AgentInfo) msg.obj;
                        activity.currentStatusIsOnline = true;
                        activity.showOnlieStatus(activity.mAgentInfo);
                        if (activity.mPresenter != null) {
                            activity.mPresenter.selfretrySendMsg();
                        }
                        break;
                    case MessageWhat.WaitAgent:
                        activity.isWait = true;
                        activity.mAgentInfo = (AgentInfo) msg.obj;
                        activity.setTitlebar(activity.mAgentInfo.getMessage(), "off");
                        this.postDelayed(activity.myRunnable, activity.QUEUE_RETEY_TIME);
                        if (activity.isfirstWaitTips) {
                            activity.isfirstWaitTips = false;
                            activity.delayShowtips(this);
                        }
                        break;
                    case MessageWhat.refreshAdapter:
                        if (activity.mChatAdapter != null) {
                            MessageInfo message = (MessageInfo) msg.obj;
                            activity.mChatAdapter.addItem(message);
                            activity.mListView.smoothScrollToPosition(activity.mChatAdapter.getCount());
                        }
                        break;
                    case MessageWhat.changeImState:
                        String msgId = (String) msg.obj;
                        int flag = msg.arg1;
                        activity.changeImState(msgId, flag);
                        break;
                    case MessageWhat.ChangeFielProgress:
                        String fileMsgId = (String) msg.obj;
                        int percent = msg.arg1;
                        long fileSize = 0;
                        boolean isSuccess = true;
                        if (msg.getData() != null) {
                            fileSize = msg.getData().getLong(UdeskConst.FileSize);
                            isSuccess = msg.getData().getBoolean(UdeskConst.FileDownIsSuccess, true);
                        }
                        activity.changeFileProgress(fileMsgId, percent, fileSize, isSuccess);
                        break;
                    case MessageWhat.onNewMessage:
                        MessageInfo msgInfo = (MessageInfo) msg.obj;
                        if (msgInfo.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_REDIRECT)) {
                            try {
                                if (activity.mPresenter != null) {
                                    activity.isInitComplete = false;
                                    activity.redirectMsg = msgInfo;
                                    JSONObject json = new JSONObject(msgInfo.getMsgContent());
                                    String agent_id = json.optString("agent_id");
                                    String group_id = json.optString("group_id");
                                    activity.mPresenter.getRedirectAgentInfo(agent_id, group_id);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (activity.mChatAdapter != null) {
                                if (activity.mAgentInfo != null) {
                                    msgInfo.setAgentUrl(activity.mAgentInfo.getHeadUrl());
                                    msgInfo.setNickName(activity.mAgentInfo.getAgentNick());
                                    if (!activity.mAgentInfo.getAgentJid().contains(msgInfo.getmAgentJid())) {
                                        activity.mAgentInfo.setAgentJid(msgInfo.getmAgentJid());
                                        activity.mPresenter.createIMCustomerInfo();
                                    }
                                }
                                activity.mChatAdapter.addItem(msgInfo);
                                activity.mListView.smoothScrollToPosition(activity.mChatAdapter.getCount());
                            }
                        }
                        break;
                    case MessageWhat.RECORD_ERROR:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_im_record_error));
                        break;
                    case MessageWhat.RECORD_Too_Short:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_label_hint_too_short));
                        break;
                    case MessageWhat.UPDATE_VOCIE_STATUS:
                        activity.updateRecordStatus(msg.arg1);
                        break;
                    case MessageWhat.recordllegal:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_im_record_error));
                        break;
                    case MessageWhat.status_notify:
                        int onlineflag = msg.arg1;
                        String jid = (String) msg.obj;
                        if (onlineflag == UdeskCoreConst.ONLINEFLAG) {
                            if (activity.isbolcked.equals("true")) {
                                return;
                            }
                            if (activity.mAgentInfo == null ||
                                    TextUtils.isEmpty(activity.mAgentInfo.getAgentJid())
                                    || !jid.contains(activity.mAgentInfo.getAgentJid())) {
                                return;
                            }
                            if (!activity.currentStatusIsOnline && activity.isNeedStartExpandabLyout) {
                                activity.expandableLayout.startAnimation(true);
                                activity.currentStatusIsOnline = true;
                                activity.isNeedStartExpandabLyout = false;
                            }
                            activity.showOnlieStatus(activity.mAgentInfo);
                            if (activity.popWindow != null) {
                                activity.popWindow.cancle();
                            }
                            if (!activity.hasSendCommodity) {
                                activity.hasSendCommodity = true;
                                activity.sendCommodityMsg(UdeskBaseInfo.commodity);
                            }
                            if (!activity.hasSendFirstMessage) {
                                activity.hasSendFirstMessage = true;
                                activity.sendDefualtMessage();
                            }
                        } else if (onlineflag == UdeskCoreConst.OFFLINEFLAG) {
                            if (activity.mPresenter != null) {
                                activity.mPresenter.getIMStatus(activity.mAgentInfo);
                            }
                        }
                        break;
                    case MessageWhat.IM_STATUS:
                        String imStatus = (String) msg.obj;
                        if (imStatus.equals("off")) {
                            activity.isWait = false;
                            activity.isInitComplete = true;
                            if (activity.isleaveMessageTypeMsg()) {
                                activity.setUdeskImContainerVis(View.GONE);
                                activity.currentStatusIsOnline = false;
                                activity.setTitlebar(activity.getString(R.string.udesk_ok), "off");
                            } else {
                                if (activity.mAgentInfo != null) {
                                    activity.setTitlebar(activity.mAgentInfo.getAgentNick(), "off");
                                } else {
                                    activity.setTitlebar(activity.getResources().getString(
                                            R.string.udesk_label_customer_offline), "off");
                                }
                                if (activity.currentStatusIsOnline) {
                                    activity.expandableLayout.startAnimation(false);
                                    activity.currentStatusIsOnline = false;
                                    activity.isNeedStartExpandabLyout = true;
                                }
                                activity.delayShowtips(this);
                            }
                        }
                        break;
                    case MessageWhat.IM_BOLACKED:
                        activity.isbolcked = "true";
                        activity.bolckedNotice = (String) msg.obj;
                        if (TextUtils.isEmpty(activity.bolckedNotice)) {
                            activity.bolckedNotice = activity
                                    .getString(R.string.add_bolcked_tips);

                        }
                        activity.setTitlebar(activity.bolckedNotice, "off");
                        this.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                activity.toBolckedView();
                            }
                        }, 1500);

                        break;
                    case MessageWhat.Has_Survey:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_has_survey));
                        break;
                    case MessageWhat.Survey_error:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_survey_error));
                        break;
                    case MessageWhat.redirectSuccess:
                        MessageInfo redirectSuccessmsg = (MessageInfo) msg.obj;
                        if (activity.mChatAdapter != null) {
                            activity.mChatAdapter.addItem(redirectSuccessmsg);
                            activity.mListView.smoothScrollToPosition(activity.mChatAdapter.getCount());
                        }
                        activity.currentStatusIsOnline = true;
                        activity.showOnlieStatus(activity.mAgentInfo);

                        break;
                    case MessageWhat.surveyNotify:
                        SurveyOptionsModel surveyOptions = (SurveyOptionsModel) msg.obj;
                        if (surveyOptions != null) {
                            activity.toLuanchSurveyActivity(surveyOptions);
                        }
                        break;
                    case MessageWhat.Add_UdeskEvent:
                        if (activity.mChatAdapter != null) {
                            MessageInfo eventMsg = (MessageInfo) msg.obj;
                            activity.mChatAdapter.addItem(eventMsg);
                        }
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class EditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) { //发送输入预支消息
            try {
                if (isbolcked.equals("true") || !currentStatusIsOnline || isOverConversation) {
                    return;
                }
                if (TextUtils.isEmpty(mInputEditView.getText().toString())) {
                    mPresenter.sendPreMessage();
                    return;
                }
                long currentTime = System.currentTimeMillis();
                if (currentTime - preMsgSendTime > 500) {
                    if (mPresenter != null) {
                        preMsgSendTime = currentTime;
                        mPresenter.sendPreMessage();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Fresco.hasBeenInitialized()) {
            UdeskSDKManager.getInstance().init(this);
        }
        try {
            setContentView(R.layout.udesk_activity_im);
            mHandler = new MyHandler(UdeskChatActivity.this);
            mPresenter = new ChatActivityPresenter(this);
            UdeskBaseInfo.isNeedMsgNotice = false;
            InvokeEventContainer.getInstance().event_IsOver.bind(this, "isOverConversation");
            isOverConversation = false;
            initIntent();
            settingTitlebar();
            initView();
            //进入会话界面 关闭推送
            if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getRegisterId(UdeskChatActivity.this)) && UdeskConfig.isUserSDkPush) {
                UdeskSDKManager.getInstance().setSdkPushStatus(UdeskSDKManager.getInstance().getDomain(this),
                        UdeskSDKManager.getInstance().getAppkey(this),
                        UdeskBaseInfo.sdkToken, UdeskConfig.UdeskPushFlag.OFF,
                        UdeskSDKManager.getInstance().getRegisterId(UdeskChatActivity.this),
                        UdeskSDKManager.getInstance().getAppId(this));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }


    //在指定客服组ID  或者指定客服ID  会传入值  其它的方式进入不会传值
    private void initIntent() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                groupId = intent.getStringExtra(UdeskConst.UDESKGROUPID);
                agentId = intent.getStringExtra(UdeskConst.UDESKAGENTID);
            }
            PreferenceHelper.write(this, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                    UdeskConst.SharePreParams.Udesk_Group_Id, groupId);
            PreferenceHelper.write(this, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                    UdeskConst.SharePreParams.Udesk_App_Key, agentId);
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
                UdekConfigUtil.setUITextColor(UdeskConfig.udeskTitlebarTextLeftRightResId, mTitlebar.getLeftTextView(), mTitlebar.getRightTextView());
                UdekConfigUtil.setUIbgDrawable(UdeskConfig.udeskTitlebarBgResId, mTitlebar.getRootView());
                if (UdeskConfig.DEFAULT != UdeskConfig.udeskbackArrowIconResId) {
                    mTitlebar.getUdeskBackImg().setImageResource(UdeskConfig.udeskbackArrowIconResId);
                }
                mTitlebar
                        .setLeftTextSequence(getString(R.string.udesk_agent_connecting));
                mTitlebar.setLeftLinearVis(View.VISIBLE);
                mTitlebar.setLeftViewClick(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finishAcitivty();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        try {
            popWindow = new UdeskConfirmPopWindow(this);
            sendBtn = (Button) findViewById(R.id.udesk_bottom_send);
            sendBtn.setOnClickListener(this);
            mInputEditView = (EditText) findViewById(R.id.udesk_bottom_input);
            mInputEditView.addTextChangedListener(new EditChangedListener());
            mInputEditView.setOnTouchListener(this);
            emojisPannel = findViewById(R.id.udesk_bottom_emojis);
            showEmjoImg = findViewById(R.id.udesk_bottom_show_emoji);
            showEmjoImg.setOnClickListener(this);
            mEmojiAdapter = new UDEmojiAdapter(this);
            emjoGridView = (GridView) findViewById(R.id.udesk_bottom_emoji_pannel);
            emjoGridView.setAdapter(mEmojiAdapter);
            emjoGridView.setOnItemClickListener(this);
            btnCamera = findViewById(R.id.udesk_bottom_option_camera);
            btnCamera.setOnClickListener(this);
            if (UdeskConfig.isUsecamera) {
                btnCamera.setVisibility(View.VISIBLE);
            } else {
                btnCamera.setVisibility(View.GONE);
            }
            btnPhoto = findViewById(R.id.udesk_bottom_option_photo);
            btnPhoto.setOnClickListener(this);
            if (UdeskConfig.isUsephoto) {
                btnPhoto.setVisibility(View.VISIBLE);
            } else {
                btnPhoto.setVisibility(View.GONE);
            }
            btnFile = findViewById(R.id.udesk_bottom_option_file);
            btnFile.setOnClickListener(this);
            if (UdeskConfig.isUsefile) {
                btnFile.setVisibility(View.VISIBLE);
            } else {
                btnFile.setVisibility(View.GONE);
            }
            btnsurvy = findViewById(R.id.udesk_bottom_survy_rl);
            btnsurvy.setOnClickListener(this);
            if (UdeskSDKManager.getInstance().getImSetting() != null && !UdeskSDKManager.getInstance().getImSetting().getEnable_im_survey()) {
                btnsurvy.setVisibility(View.GONE);
            }
            btnLoaction = findViewById(R.id.udesk_bottom_location_rl);
            btnLoaction.setOnClickListener(this);
            if (UdeskConfig.isUseMap) {
                btnLoaction.setVisibility(View.VISIBLE);
            } else {
                btnLoaction.setVisibility(View.GONE);
            }
            mListView = (UDPullGetMoreListView) findViewById(R.id.udesk_conversation);
            expandableLayout = (UdeskExpandableLayout) findViewById(R.id.udesk_change_status_info);

            showVoiceImg = findViewById(R.id.udesk_bottom_voice_rl);
            showVoiceImg.setOnClickListener(this);
            if (UdeskConfig.isUseVoice) {
                showVoiceImg.setVisibility(View.VISIBLE);
            } else {
                showVoiceImg.setVisibility(View.GONE);
            }
            audioPanel = findViewById(R.id.udesk_bottom_audios);
            mHorVoiceView = (HorVoiceView) findViewById(R.id.udesk_horvoiceview);
            udesk_audio_tips = (TextView) findViewById(R.id.udesk_audio_tips);
            audioCancle = findViewById(R.id.udesk_audio_cancle_image);
            udeskImContainer = findViewById(R.id.udesk_im_container);
            audioPop = (ImageView) findViewById(R.id.udesk_audio_pop);
            setListView();
            initDatabase();
            mPresenter.createIMCustomerInfo();
            isNeedRelogin = !UdeskUtils.isNetworkConnected(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setListView() {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initDatabase() {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    historyCount = UdeskDBManager.getInstance().getMessageCount();
                    UdeskDBManager.getInstance().updateSendFlagToFail();
                    loadHistoryRecords(initViewMode);
                    UdeskDBManager.getInstance().updateAllMsgRead();

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (UdeskSDKManager.getInstance().getImSetting() != null && !UdeskSDKManager.getInstance().getImSetting().getIs_worktime()) {
                return;
            }
            if (mPresenter != null) {
                mPresenter.bindReqsurveyMsg();
            }
            if (isInitComplete && !currentStatusIsOnline && !isSurvyOperate) {
                mPresenter.createIMCustomerInfo();
            }
            registerNetWorkReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        try {
            if (v == mInputEditView && event.getAction() == MotionEvent.ACTION_DOWN) {
                setUdeskAudioPanelVis(View.GONE);
                setUdeskEmojisPannel(View.GONE);
                emjoGridView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void onClick(View v) {
        try {
            //检查是否处在可发消息的状态
            if (!isShowNotSendMsg()) {
                UdeskUtils.hideSoftKeyboard(this, mInputEditView);
                return;
            }
            if (R.id.udesk_bottom_send == v.getId()) { //发送文本消息
                if (TextUtils.isEmpty(mInputEditView.getText().toString())) {
                    UdeskUtils.showToast(UdeskChatActivity.this,
                            getString(R.string.udesk_send_message_empty));
                    return;
                }
                if (currentStatusIsOnline) {
                    mPresenter.sendTxtMessage();
                } else if (UdeskSDKManager.getInstance().getImSetting() != null &&
                        UdeskSDKManager.getInstance().getImSetting().getLeave_message_type().equals("msg")) {
                    if (!isLeavingmsg) {
                        mPresenter.addCustomerLeavMsg();
                        isLeavingmsg = true;
                    }
                    mPresenter.sendLeaveMessage();
                }
            } else if (R.id.udesk_bottom_show_emoji == v.getId()) { // 显示表情面板

                if (emojisPannel.getVisibility() == View.VISIBLE) {
                    bottomoPannelBegginStatus();
                } else {
                    bottomoPannelBegginStatus();
                    setUdeskEmojisPannel(View.VISIBLE);
                }
                setUdeskEditClickabled(mInputEditView);
                UdeskUtils.hideSoftKeyboard(this, mInputEditView);
            } else if (R.id.udesk_bottom_option_photo == v.getId()) {  //选择本地的图片
                if (Build.VERSION.SDK_INT < 23) {
                    selectPhoto();
                    bottomoPannelBegginStatus();
                } else {
                    XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.EXTERNAL,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            new XPermissionUtils.OnPermissionListener() {
                                @Override
                                public void onPermissionGranted() {
                                    selectPhoto();
                                    bottomoPannelBegginStatus();
                                }

                                @Override
                                public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                    Toast.makeText(UdeskChatActivity.this,
                                            getResources().getString(R.string.photo_denied),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            } else if (R.id.udesk_bottom_option_camera == v.getId()) { // 拍照发送图片
                if (Build.VERSION.SDK_INT < 23) {
                    takePhoto();
                    bottomoPannelBegginStatus();
                } else {
                    XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.CAMERA,
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            new XPermissionUtils.OnPermissionListener() {
                                @Override
                                public void onPermissionGranted() {
                                    takePhoto();
                                    bottomoPannelBegginStatus();
                                }

                                @Override
                                public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                    Toast.makeText(UdeskChatActivity.this,
                                            getResources().getString(R.string.camera_denied),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else if (R.id.udesk_bottom_voice_rl == v.getId()) {  //录音 发送语音
                if (Build.VERSION.SDK_INT < 23) {
                    if (audioPanel.getVisibility() == View.VISIBLE) {
                        bottomoPannelBegginStatus();
                    } else {
                        bottomoPannelBegginStatus();
                        initAduioPannel();
                    }
                } else {
                    XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.AUDIO,
                            new String[]{Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            new XPermissionUtils.OnPermissionListener() {
                                @Override
                                public void onPermissionGranted() {
                                    if (audioPanel.getVisibility() == View.VISIBLE) {
                                        bottomoPannelBegginStatus();
                                    } else {
                                        bottomoPannelBegginStatus();
                                        initAduioPannel();
                                    }
                                }

                                @Override
                                public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                    Toast.makeText(UdeskChatActivity.this,
                                            getResources().getString(R.string.aduido_denied),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else if (R.id.udesk_bottom_survy_rl == v.getId()) {
                if (mPresenter != null && isPermmitSurvy) {
                    setIsPermmitSurvy(false);
                    mPresenter.getHasSurvey(mAgentInfo.getAgent_id(), null);
                }
            } else if (R.id.udesk_bottom_option_file == v.getId()) {
                if (Build.VERSION.SDK_INT < 23) {
                    selectFile();
                    bottomoPannelBegginStatus();
                } else {
                    XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.EXTERNAL,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            new XPermissionUtils.OnPermissionListener() {
                                @Override
                                public void onPermissionGranted() {
                                    selectFile();
                                    bottomoPannelBegginStatus();
                                }

                                @Override
                                public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                    Toast.makeText(UdeskChatActivity.this,
                                            getResources().getString(R.string.file_denied),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            } else if (R.id.udesk_bottom_location_rl == v.getId()) {
                //地理位置信息打开的页面 由客户传入  并通过Activity方式 来传递数据
                if (UdeskSDKManager.getInstance().getCls() != null) {
                    Intent intent = new Intent(UdeskChatActivity.this, UdeskSDKManager.getInstance().getCls());
                    startActivityForResult(intent, SELECT_LOCATION_OPTION_REQUEST_CODE);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    //电话呼叫
    public void callphone(final String mobile) {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(mobile));
                UdeskChatActivity.this.startActivity(intent);
            } else {
                XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.CallPhone,
                        new String[]{Manifest.permission.CALL_PHONE},
                        new XPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(mobile));
                                if (ActivityCompat.checkSelfPermission(UdeskChatActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                UdeskChatActivity.this.startActivity(intent);
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                Toast.makeText(UdeskChatActivity.this,
                                        getResources().getString(R.string.call_denied),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 长按录音
    @Override
    public boolean onLongClick(View v) {
        try {
            if (v.getId() == R.id.udesk_audio_pop) {
                if (!UdeskUtils.checkSDcard()) {
                    Toast.makeText(this,
                            getResources().getString(R.string.udesk_label_no_sd),
                            Toast.LENGTH_LONG).show();
                    return false;
                }

                if (Build.VERSION.SDK_INT < 23) {
                    recordVoiceStart();
                } else {
                    XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.AUDIO,
                            new String[]{Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            new XPermissionUtils.OnPermissionListener() {
                                @Override
                                public void onPermissionGranted() {
                                    recordVoiceStart();
                                }

                                @Override
                                public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                    Toast.makeText(UdeskChatActivity.this,
                                            getResources().getString(R.string.aduido_denied),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        try {
            if (parent == emjoGridView) {
                if (mPresenter != null) {
                    mPresenter.clickEmoji(id, mEmojiAdapter.getCount(),
                            mEmojiAdapter.getItem((int) id));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) { //拍照后发生图片
                if (Activity.RESULT_OK == resultCode) {
                    if (data != null && data.hasExtra("data") && data.getParcelableExtra("data") != null && mPresenter != null) {
                        mPresenter.sendBitmapMessage((Bitmap) data.getParcelableExtra("data"));
                    }
                    if (mPresenter != null && photoUri != null && photoUri.getPath() != null) {
                        mPresenter.sendBitmapMessage(UdeskUtil.parseOwnUri(photoUri, UdeskChatActivity.this, cameraFile));
                    }

                }
            } else if (SELECT_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) { //选择图片后发送
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }
                Uri mImageCaptureUri = data.getData();
                if (mImageCaptureUri != null) {
                    try {
                        if (mImageCaptureUri != null) {
                            String path = UdeskUtil.getFilePath(this, mImageCaptureUri);
                            mPresenter.sendBitmapMessage(path);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError error) {
                        error.printStackTrace();
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

            } else if (SELECT_SURVY_OPTION_REQUEST_CODE == requestCode) {
                setIsPermmitSurvy(true);
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }
                Toast.makeText(getApplicationContext(), getString(R.string.udesk_thanks_survy), Toast.LENGTH_SHORT).show();
                String optionId = data.getStringExtra(UdeskConst.SurvyOptionIDKey);
                mPresenter.putIMSurveyResult(optionId);
            } else if (SELECT_FILE_OPTION_REQUEST_CODE == requestCode) {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }
                Uri mImageCaptureUri = data.getData();
                if (mImageCaptureUri != null) {
                    try {
                        if (mImageCaptureUri != null) {
                            String path = UdeskUtil.getFilePath(this, mImageCaptureUri);
                            double size = UdeskUtil.getFileSize(new File(path));
                            if (size >= 30 * 1024 * 1024) {
                                Toast.makeText(getApplicationContext(), getString(R.string.udesk_file_to_large), Toast.LENGTH_SHORT).show();
                                return;
                            }
                            if (UdeskUtil.isGpsNet(getApplicationContext())) {
                                toGpsNetView(true, null, path);
                                return;
                            }
                            sendFile(path);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (OutOfMemoryError error) {
                        error.printStackTrace();
                    }
                }
            } else if (SELECT_LOCATION_OPTION_REQUEST_CODE == requestCode) {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }

                String postionValue = data.getStringExtra(UdeskConfig.UdeskMapIntentName.Position);
                String bitmapDir = data.getStringExtra(UdeskConfig.UdeskMapIntentName.BitmapDIR);
                double latitude = data.getDoubleExtra(UdeskConfig.UdeskMapIntentName.Latitude, 0.0);
                double longitude = data.getDoubleExtra(UdeskConfig.UdeskMapIntentName.Longitude, 0.0);

                mPresenter.sendLocationMessage(latitude, longitude, postionValue, bitmapDir);


            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }


    }

    private void sendFile(String path) {
        try {
            if (path.contains(".mp4") && mPresenter != null) {
                mPresenter.sendFileMessage(path, UdeskConst.ChatMsgTypeString.TYPE_VIDEO);
            } else {
                mPresenter.sendFileMessage(path, UdeskConst.ChatMsgTypeString.TYPE_File);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * 读取数据库中的历史数据
     */
    private void loadHistoryRecords(int mode) {
        try {
            mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
            // 已经没有更早的数据了
            if (offset == 0) {
                UdeskUtils.showToast(this,
                        getString(R.string.udesk_no_more_history));
                mListView.onRefreshComplete();
                mListView.setSelection(0);
            } else {
                // 还有老数据
                int pageNum = UdeskConst.UDESK_HISTORY_COUNT;
                if (offset == -1) {
                    offset = historyCount - UdeskConst.UDESK_HISTORY_COUNT;
                } else {
                    if (offset - UdeskConst.UDESK_HISTORY_COUNT < 0) {
                        pageNum = offset;
                    }
                    offset = offset - UdeskConst.UDESK_HISTORY_COUNT;
                }
                offset = (offset < 0 ? 0 : offset);
                List<MessageInfo> list = UdeskDBManager.getInstance().getMessages(
                        offset, pageNum);
                if (list != null) {
                    Message msg = Message.obtain();
                    msg.what = MessageWhat.loadHistoryDBMsg;
                    msg.arg1 = mode;
                    msg.obj = list;
                    mHandler.sendMessage(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    //设置客服状态
    private void showOnlieStatus(AgentInfo mAgentInfo) {
        try {
            if (mAgentInfo == null || !currentStatusIsOnline) {
                return;
            }
            setTitlebar(mAgentInfo.getAgentNick(), "on");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //发送广告消息
    private void sendCommodityMsg(UdeskCommodityItem commodity) {
        try {
            if (commodity != null && mPresenter != null) {
                mPresenter.sendCommodityMessage(commodity);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    //客户设置的第一句默认消息
    private void sendDefualtMessage() {
        if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getFirstMessage()) && mPresenter != null) {
            mPresenter.sendTxtMessage(UdeskSDKManager.getInstance().getFirstMessage());
        }
    }


    //启动手机默认的选择照片
    private void selectPhoto() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, SELECT_IMAGE_ACTIVITY_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    // 启动手机相机拍照
    private void takePhoto() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraFile = UdeskUtil.cameaFile(UdeskChatActivity.this);
            photoUri = UdeskUtil.getOutputMediaFileUri(UdeskChatActivity.this, cameraFile);
            if (Build.VERSION.SDK_INT >= 24) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            if (photoUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            }
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    //启动手机默认的选择mp4文件
    private void selectFile() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, SELECT_FILE_OPTION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }


    //启动满意度调查
    private void toLuanchSurveyActivity(SurveyOptionsModel surveyOptions) {
        try {
            if (surveyOptions.getOptions() == null || surveyOptions.getOptions().isEmpty()) {
                UdeskUtils.showToast(this,
                        getString(R.string.udesk_no_set_survey));
                setIsPermmitSurvy(true);
                return;
            }
            Intent intent = new Intent();
            intent.setClass(UdeskChatActivity.this, UdeskSurvyDialogActivity.class);
            intent.putExtra(UdeskConst.SurvyDialogKey, surveyOptions);
            startActivityForResult(intent, SELECT_SURVY_OPTION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //显示黑名单提示框
    private void toBolckedView() {
        try {

            String positiveLabel = this.getString(R.string.udesk_sure);
            String negativeLabel = this.getString(R.string.udesk_cancel);
            String title = bolckedNotice;
            if (UdeskChatActivity.this.isFinishing()) {
                return;
            }
            if (!popWindow.isShowing()) {
                popWindow.show(this, this.getWindow().getDecorView(),
                        positiveLabel, negativeLabel, title,
                        new OnPopConfirmClick() {
                            public void onPositiveClick() {
                                finish();
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

    //延期弹出提示框
    private void delayShowtips(Handler handler) {
        try {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    confirmToForm();
                }
            }, 500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toGpsNetView(final boolean isupload, final MessageInfo info, final String path) {
        try {
            String positiveLabel = this.getString(R.string.udesk_sure);
            String negativeLabel = this.getString(R.string.udesk_cancel);
            String content = "";
            if (isupload) {
                content = getApplicationContext().getString(R.string.udesk_gps_tips);
            } else {
                content = getApplicationContext().getString(R.string.udesk_gps_downfile_tips);
            }

            if (UdeskChatActivity.this.isFinishing()) {
                return;
            }
            if (!popWindow.isShowing()) {
                popWindow.show(this, this.getWindow().getDecorView(),
                        positiveLabel, negativeLabel, content,
                        new OnPopConfirmClick() {
                            public void onPositiveClick() {
                                if (isupload && TextUtils.isEmpty(path)) {
                                    sendFile(path);
                                }
                                if (!isupload && info != null && mPresenter != null) {
                                    mPresenter.downFile(info);
                                }
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

    private Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPresenter != null) {
                mPresenter.getAgentInfo(true);
            }
        }
    };

    //语音的回收
    private void checkRecoredView(View view) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * 根据消息的ID 修改发送状态
     */
    private void changeImState(String msgId, int state) {
        try {
            if (!TextUtils.isEmpty(msgId) && mListView != null
                    && mChatAdapter != null) {
                //getChildAt(i) 只能获取可见区域View， 会有bug
                for (int i = mListView.getChildCount() - 1; i >= 0; i--) {
                    View child = mListView.getChildAt(i);
                    if (child != null) {
                        if (mChatAdapter.changeImState(child, msgId, state)) {
                            return;
                        }
                    }
                }
                //当不在可见区域则调用整个刷新
                mChatAdapter.updateStatus(msgId, state);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }


    /**
     * 根据消息的ID 修改发送状态
     */
    private void changeFileProgress(String msgId, int precent, long fileSize, boolean isSuccess) {
        try {
            if (!TextUtils.isEmpty(msgId) && mListView != null
                    && mChatAdapter != null) {
                //getChildAt(i) 只能获取可见区域View， 会有bug
                for (int i = mListView.getChildCount() - 1; i >= 0; i--) {
                    View child = mListView.getChildAt(i);
                    if (child != null) {
                        if (mChatAdapter.changeFileState(child, msgId, precent, fileSize, isSuccess)) {
                            return;
                        }
                    }
                }
                //当不在可见区域则调用整个刷新
                mChatAdapter.updateProgress(msgId, precent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }


    // 判断可发送消息
    private boolean isShowNotSendMsg() {
        try {
            if (isbolcked != null && isbolcked.equals("true")) {
                toBolckedView();
                return false;
            }
            if (!isInitComplete) {
                UdeskUtils.showToast(this,
                        getResources().getString(R.string.udesk_agent_inti));
                return false;
            }
            if (!UdeskUtils.isNetworkConnected(this)) {
                UdeskUtils.showToast(this,
                        getResources().getString(R.string.udesk_has_wrong_net));
                return false;
            }

            if (isOverConversation) {
                UdeskUtils.showToast(this,
                        getResources().getString(R.string.udesk_agent_inti));
                isInitComplete = false;
                isOverConversation = false;
                mPresenter.createIMCustomerInfo();
                return false;
            }

            if (isWait) {
                confirmToForm();
                return false;
            }

            if (!currentStatusIsOnline && !isleaveMessageTypeMsg()) {
                confirmToForm();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    //开始录音
    private void recordVoiceStart() {
        try {
            if (mRecordFilePlay != null) {
                showStartOrStopAnaimaition(
                        mRecordFilePlay.getPlayAduioMessage(), false);
                recycleVoiceRes();
            }
            setAudiotipsVis(View.GONE);
            setHorVoiceViewVis(View.VISIBLE);
            setAudioCancleViewVis(View.VISIBLE);

            if (audioCancle != null) {
                audioPop.setOnTouchListener(new RecordTouchListener(this,
                        UdeskChatActivity.this, audioCancle));
                if (mPresenter != null) {
                    // 开始录音
                    mPresenter.recordStart();
                    if (mHorVoiceView != null) {
                        mHorVoiceView.startRecording(this);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    //弹出表单留言的提示框
    private void confirmToForm() {
        try {
            boolean isOpen = true;
            SDKIMSetting imsetting = UdeskSDKManager.getInstance().getImSetting();
            //是否弹出表单： 如果获取到管理员的配置，使用管理员的配置。没获取到则使用配置中默认设置
            if (imsetting != null) {
                isOpen = imsetting.getEnable_web_im_feedback();
            } else {
                isOpen = UdeskConfig.isUserForm;
            }
            //如果客户设置了回调处理留言系统，一定设置true
            if (UdeskSDKManager.getInstance().getFormCallBak() != null) {
                isOpen = true;
            }
            String positiveLabel = this.getString(R.string.udesk_sure);
            if (isOpen) {
                positiveLabel = this.getString(R.string.udesk_ok);
            }
            String negativeLabel = this.getString(R.string.udesk_cancel);
            String title = "";
            if (isOpen) {
                //表单留言的文案,如果客户设置了，则使用客户的设置文案。否则用默认的文案
                if (!TextUtils.isEmpty(UdeskConfig.UdeskLeavingMsg)) {
                    title = UdeskConfig.UdeskLeavingMsg;
                } else {
                    title = this.getString(R.string.udesk_msg_offline_to_form);
                }
            } else {
                //关闭留言的文案,如果获取到后台的设置，则使用后台的设置，没获取的后台的，本地客户设置了，则使用客户的设置文案。否则用默认的文案
                if (imsetting != null && !TextUtils.isEmpty(imsetting.getNo_reply_hint())) {
                    title = imsetting.getNo_reply_hint();
                } else {
                    if (!TextUtils.isEmpty(UdeskConfig.UdeskLeavingMsg)) {
                        title = UdeskConfig.UdeskLeavingMsg;
                    } else {
                        title = this.getString(R.string.udesk_msg_busy_default_to_form);
                    }

                }
            }
            if (mAgentInfo != null && mAgentInfo.getAgentCode() == UdeskConst.AgentReponseCode.WaitAgent) {
                title = mAgentInfo.getMessage();
                if (TextUtils.isEmpty(title)) {
                    title = this.getString(R.string.udesk_msg_busyline_to_wait);
                }
            }
            if (UdeskChatActivity.this.isFinishing()) {
                return;
            }
            if (!popWindow.isShowing()) {
                final String finalPositiveLabel = positiveLabel;
                popWindow.show(this, this.getWindow().getDecorView(),
                        finalPositiveLabel, negativeLabel, title,
                        new OnPopConfirmClick() {
                            public void onPositiveClick() {
                                dismissFormWindow();
                                if (finalPositiveLabel.equals(UdeskChatActivity.this.getString(R.string.udesk_ok))) {

                                    if (isleaveMessageTypeMsg()) {

                                        //直接留言
                                        isWait = false;
                                        if (mHandler != null && myRunnable != null) {
                                            mHandler.removeCallbacks(myRunnable);
                                        }
                                        if (mPresenter != null) {
                                            mPresenter.quitQuenu();
                                        }
                                        setUdeskImContainerVis(View.GONE);
                                        setTitlebar(getString(R.string.udesk_ok), "off");
                                    } else {
                                        goToForm();

                                    }

                                }
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

    //启动留言界面
    protected void goToForm() {
        try {
            dismissFormWindow();
            if (UdeskSDKManager.getInstance().getFormCallBak() != null) {
                UdeskSDKManager.getInstance().getFormCallBak().toLuachForm(UdeskChatActivity.this);
                return;
            }
            if (UdeskSDKManager.getInstance().getImSetting() != null && !UdeskSDKManager.getInstance().getImSetting().getEnable_web_im_feedback()) {
                return;
            }
            UdeskSDKManager.getInstance().goToForm(UdeskChatActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void dismissFormWindow() {
        if (popWindow != null) {
            popWindow.dismiss();
        }
    }


    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public CharSequence getInputContent() {
        if (mInputEditView != null) {
            return mInputEditView.getText();
        }
        return "";
    }

    @Override
    public void clearInputContent() {
        if (mInputEditView != null) {
            mInputEditView.setText("");
        }
    }

    @Override
    public void showFailToast(final String failMsg) {

        try {
            this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    isInitComplete = true;
                    setTitlebar(getResources().getString(
                            R.string.udesk_api_error), "off");
                    if (!TextUtils.isEmpty(failMsg)) {
                        UdeskUtils.showToast(UdeskChatActivity.this, failMsg);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    /**
     * 处理请求获取到客服信息
     *
     * @param agentInfo
     */
    @Override
    public void dealAgentInfo(final AgentInfo agentInfo) {

        try {
            isInitComplete = true;
            if (agentInfo == null) {
                return;
            }
            switch (agentInfo.getAgentCode()) {
                case UdeskConst.AgentReponseCode.NoAgent:
                    Message msgNoAgent = mHandler.obtainMessage(MessageWhat.NoAgent);
                    msgNoAgent.obj = agentInfo;
                    mHandler.sendMessage(msgNoAgent);
                    break;
                case UdeskConst.AgentReponseCode.HasAgent:
                    // 有客服titlebar上显示
                    UdeskDBManager.getInstance().addAgentInfo(agentInfo);
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
                            Toast.makeText(UdeskChatActivity.this, getString(R.string.udesk_nonexistent_agent), Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case UdeskConst.AgentReponseCode.NonExistentGroupId:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UdeskChatActivity.this, getString(R.string.udesk_nonexistent_groupId), Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理请求转移客服获取到客服信息
     *
     * @param agentInfo
     */
    @Override
    public void dealRedirectAgentInfo(AgentInfo agentInfo) {
        try {
            isInitComplete = true;
            if (agentInfo == null) {
                return;
            }
            switch (agentInfo.getAgentCode()) {
                case UdeskConst.AgentReponseCode.NoAgent:
                    Message msgNoAgent = mHandler.obtainMessage(MessageWhat.NoAgent);
                    msgNoAgent.obj = agentInfo;
                    mHandler.sendMessage(msgNoAgent);
                    break;
                case UdeskConst.AgentReponseCode.HasAgent:
                    String redirectTip = getString(R.string.udesk_transfer_success) + agentInfo.getAgentNick() + getString(R.string.udesk_service);
                    if (redirectMsg != null) {
                        redirectMsg.setMsgContent(redirectTip);
                        UdeskDBManager.getInstance().addMessageInfo(redirectMsg);
                    }
                    mAgentInfo = agentInfo;
                    UdeskDBManager.getInstance().addAgentInfo(mAgentInfo);
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void addMessage(MessageInfo message) {
        try {
            Message msgWaitAgent = mHandler
                    .obtainMessage(MessageWhat.refreshAdapter);
            msgWaitAgent.obj = message;
            mHandler.sendMessage(msgWaitAgent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public AgentInfo getAgentInfo() {
        if (mAgentInfo != null) {
            return mAgentInfo;
        }
        return new AgentInfo();
    }

    @Override
    public void setAgentInfo(AgentInfo agentInfo) {
        mAgentInfo = agentInfo;
    }

    @Override
    public Handler getHandler() {
        return mHandler;
    }


    @Override
    public void refreshInputEmjio(String s) {
        try {
            if (UDEmojiAdapter.replaceEmoji(this, s,
                    (int) mInputEditView.getTextSize()) != null) {
                mInputEditView.setText(UDEmojiAdapter.replaceEmoji(this, s,
                        (int) mInputEditView.getTextSize()));
            } else {
                mInputEditView.setText(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public List<String> getEmotionStringList() {
        List<String> emotionList = new ArrayList<String>();
        try {
            int emojiSum = mEmojiAdapter.getCount();
            for (int i = 0; i < emojiSum; i++) {
                if (mEmojiAdapter.getItem(i) != null) {
                    emotionList.add(mEmojiAdapter.getItem(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emotionList;
    }

    @Override
    public void onTimeOver() {
        if (mPresenter != null) {
            mPresenter.doRecordStop(false);
        }
        initAduioPannel();
    }

    @Override
    public void readyToCancelRecord() {

        try {
            setHorVoiceViewVis(View.GONE);
            setAudiotipsVis(View.VISIBLE);
            setAudiotiptext(getString(R.string.udesk_voice_cancle));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doCancelRecord() {
        try {
            if (mPresenter != null) {
                mPresenter.doRecordStop(true);

            }
            if (mHorVoiceView != null) {
                mHorVoiceView.stopRecording();
            }
            initAduioPannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readyToContinue() {
        try {
            setHorVoiceViewVis(View.VISIBLE);
            setAudiotipsVis(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void endRecord() {
        try {
            if (mPresenter != null) {
                mPresenter.doRecordStop(false);
            }
            if (mHorVoiceView != null) {
                mHorVoiceView.stopRecording();
            }
            initAduioPannel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onRecordSuccess(String filePath, long duration) {
        if (mPresenter != null) {
            mPresenter.sendRecordAudioMsg(filePath, duration);
        }
    }

    @Override
    public String getAgentId() {
        if (TextUtils.isEmpty(agentId)) {
            return PreferenceHelper.readString(this, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                    UdeskConst.SharePreParams.Udesk_Agent_Id);
        }
        return agentId;
    }


    @Override
    public String getGroupId() {
        if (TextUtils.isEmpty(groupId)) {
            return PreferenceHelper.readString(this, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                    UdeskConst.SharePreParams.Udesk_Group_Id);
        }
        return groupId;
    }

    @Override
    public void changgeiSSurvyOperate() {
        isSurvyOperate = true;
    }

    @Override
    public void setIsPermmitSurvy(boolean isPermmit) {
        try {
            if (btnsurvy != null) {
                if (isPermmit) {
                    btnsurvy.setOnClickListener(this);
                } else {
                    btnsurvy.setOnClickListener(null);
                }
            }
            isPermmitSurvy = isPermmit;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerNetWorkReceiver() {
        try {
            if (mConnectivityChangedReceiver == null) {
                mConnectivityChangedReceiver = new ConnectivtyChangedReceiver();
                UdeskChatActivity.this.registerReceiver(
                        mConnectivityChangedReceiver, new IntentFilter(
                                ConnectivityManager.CONNECTIVITY_ACTION));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unRegister() {
        try {
            if (mConnectivityChangedReceiver != null) {
                UdeskChatActivity.this
                        .unregisterReceiver(mConnectivityChangedReceiver);
                mConnectivityChangedReceiver = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showStartOrStopAnaimaition(final MessageInfo info,
                                           final boolean isstart) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 点击播放录音及动画
    public void clickRecordFile(MessageInfo message) {

        try {
            if (mRecordFilePlay == null) {
                mRecordFilePlay = new RecordPlay(UdeskChatActivity.this);

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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //回收录音资源
    private void recycleVoiceRes() {
        try {
            if (mRecordFilePlay != null) {
                mRecordFilePlay.recycleRes();
                mRecordFilePlay.recycleCallback();
                mRecordFilePlay = null;
            }
            if (mPlayCallback != null) {
                mPlayCallback = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateRecordStatus(int status) {
        try {
            if (mHorVoiceView != null) {
                mHorVoiceView.addElement(status * 10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //复制文本消息
    public void handleText(final MessageInfo message, View targetView) {
        try {
            String[] menuLabel = new String[]{getResources().getString(
                    R.string.udesk_copy)};
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
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void doCopy(String content) {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                android.content.ClipboardManager c = (android.content.ClipboardManager) UdeskChatActivity.this
                        .getSystemService(Activity.CLIPBOARD_SERVICE);
                c.setPrimaryClip(ClipData.newPlainText(null, content));
            } else {
                android.text.ClipboardManager c = (android.text.ClipboardManager) UdeskChatActivity.this
                        .getSystemService(Activity.CLIPBOARD_SERVICE);
                c.setText(content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //发送广告的连接地址消息
    public void sentLink(String linkMsg) {
        if (mPresenter != null) {
            if (currentStatusIsOnline) {
                mPresenter.sendTxtMessage(linkMsg);
            } else if (UdeskSDKManager.getInstance().getImSetting() != null &&
                    UdeskSDKManager.getInstance().getImSetting().getLeave_message_type().equals("msg")) {
                if (!isLeavingmsg) {
                    mPresenter.addCustomerLeavMsg();
                    isLeavingmsg = true;
                }
                mPresenter.sendLeaveMessage(linkMsg);
            } else {
                confirmToForm();
            }

        }

    }

    //重试发送消息
    public void retrySendMsg(MessageInfo message) {
        try {
            if (mPresenter != null && message != null) {
                changeImState(message.getMsgId(), UdeskConst.SendFlag.RESULT_RETRY);
                mPresenter.startRetryMsg(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下载文件
    public void downLoadMsg(MessageInfo message) {
        try {
            if (mPresenter != null && message != null) {
                if (UdeskUtil.isGpsNet(getApplicationContext())) {
                    toGpsNetView(false, message, null);
                    return;
                }
                mPresenter.downFile(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bottomoPannelBegginStatus() {
        try {
            UdeskUtils.hideSoftKeyboard(this, mInputEditView);
            setUdeskAudioPanelVis(View.GONE);
            setUdeskEmojisPannel(View.GONE);
            emjoGridView.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initAduioPannel() {
        try {
            setUdeskAudioPanelVis(View.VISIBLE);
            setHorVoiceViewVis(View.GONE);
            setAudioCancleViewVis(View.GONE);
            setAudiotipsVis(View.VISIBLE);
            setAudiotiptext(getString(R.string.udesk_voice_init));
            audioPop.setOnLongClickListener(this);
            audioPop.setOnTouchListener(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param title
     * @param status on / off
     */
    private void setTitlebar(String title, String status) {
        try {
            if (mTitlebar != null) {
                mTitlebar.setLeftTextSequence(title);
                if (status.equals("on")) {
                    mTitlebar.getudeskStateImg().setImageResource(R.drawable.udesk_online_status);
                } else if (status.equals("off")) {
                    mTitlebar.getudeskStateImg().setImageResource(R.drawable.udesk_offline_status);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUdeskAudioPanelVis(int vis) {
        try {
            audioPanel.setVisibility(vis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setHorVoiceViewVis(int vis) {
        try {
            mHorVoiceView.setVisibility(vis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAudiotipsVis(int vis) {
        try {
            udesk_audio_tips.setVisibility(vis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAudioCancleViewVis(int vis) {
        try {
            audioCancle.setVisibility(vis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAudiotiptext(String s) {
        try {
            udesk_audio_tips.setText(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUdeskEmojisPannel(int vis) {
        try {
            emojisPannel.setVisibility(vis);
            emjoGridView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUdeskEditClickabled(EditText editText) {
        try {
            editText.setFocusable(true);
            editText.setFocusableInTouchMode(true);
            editText.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void isOverConversation(Boolean isOver) {
        try {
            isOverConversation = isOver;
            if (isOver) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTitlebar(getResources().getString(
                                R.string.udesk_close_chart), "off");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        finishAcitivty();
    }


    private void finishAcitivty() {
        //后台勾选开启后，对于同一个对话，用户多次进入，点击返回离开，若没有进行过满意度调查，
        // 则返回点击后均弹出满意度调查窗口，若已经有满意度调查结果，则返回不再发起调查
        try {
            SDKIMSetting imsetting = UdeskSDKManager.getInstance().getImSetting();
            if (imsetting == null || !imsetting.isInvestigation_when_leave() || !imsetting.getEnable_im_survey()) {
                finish();
            } else if (mPresenter != null && mAgentInfo != null && !TextUtils.isEmpty(mAgentInfo.getAgent_id())) {
                mPresenter.getHasSurvey(mAgentInfo.getAgent_id(), new ChatActivityPresenter.IUdeskHasSurvyCallBack() {

                    @Override
                    public void hasSurvy(boolean hasSurvy) {
                        finish();
                    }
                });
            } else {
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    //设置会话时的语音图片等按钮的显隐藏
    private void setUdeskImContainerVis(int vis) {
        if (udeskImContainer != null) {
            udeskImContainer.setVisibility(vis);
        }
    }

    private boolean isleaveMessageTypeMsg() {
        SDKIMSetting sdkimSetting = UdeskSDKManager.getInstance().getImSetting();
        if (sdkimSetting != null && sdkimSetting.getLeave_message_type().equals("msg")) {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        try {
            XPermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onPause() {
        try {
            if (mPresenter != null) {
                mPresenter.unbindReqsurveyMsg();
            }
            if (isFinishing()) {
                cleanSource();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }


    @Override
    protected void onStop() {
        recycleVoiceRes();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        cleanSource();
        super.onDestroy();

    }

    private void cleanSource() {
        if (isDestroyed) {
            return;
        }
        // 回收资源
        isDestroyed = true;
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    UdeskDBManager.getInstance().updateAllMsgRead();
                }
            }).start();
            recycleVoiceRes();
            if (mPresenter != null) {
                mPresenter.quitQuenu();
                mPresenter.unBind();
                mPresenter.removeCallBack();
                mPresenter = null;
            }
            if (mHandler != null && myRunnable != null) {
                mHandler.removeCallbacks(myRunnable);
            }
            //设置了开启推送标识，离开会话界面开启推送，
            if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getRegisterId(UdeskChatActivity.this)) && UdeskConfig.isUserSDkPush) {
                UdeskSDKManager.getInstance().setSdkPushStatus(UdeskSDKManager.getInstance().getDomain(this),
                        UdeskSDKManager.getInstance().getAppkey(this), UdeskBaseInfo.sdkToken, UdeskConfig.UdeskPushFlag.ON,
                        UdeskSDKManager.getInstance().getRegisterId(UdeskChatActivity.this), UdeskSDKManager.getInstance().getAppId(this));
            }
            UdeskBaseInfo.isNeedMsgNotice = true;
            unRegister();
            UdeskHttpFacade.getInstance().cancel();
            InvokeEventContainer.getInstance().event_IsOver.unBind(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
