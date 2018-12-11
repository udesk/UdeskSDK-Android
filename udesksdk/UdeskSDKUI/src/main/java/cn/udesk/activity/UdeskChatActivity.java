package cn.udesk.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.udesk.model.UdeskQueueItem;
import cn.udesk.presenter.MessageCache;
import udesk.core.JsonObjectUtils;
import udesk.core.LocalManageUtil;
import cn.udesk.PreferenceHelper;
import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.activity.MessageAdatper.AudioViewHolder;
import cn.udesk.adapter.UdeskFunctionAdapter;
import cn.udesk.camera.UdeskCameraActivity;
import cn.udesk.config.UdekConfigUtil;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.emotion.EmotionKeyboard;
import cn.udesk.emotion.EmotionLayout;
import cn.udesk.emotion.IEmotionSelectedListener;
import cn.udesk.emotion.LQREmotionKit;
import cn.udesk.messagemanager.UdeskMessageManager;
import cn.udesk.model.FunctionMode;
import cn.udesk.model.SDKIMSetting;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.permission.RequestCode;
import cn.udesk.permission.XPermissionUtils;
import cn.udesk.photoselect.PhotoSelectorActivity;
import cn.udesk.photoselect.entity.LocalMedia;
import cn.udesk.presenter.ChatActivityPresenter;
import cn.udesk.presenter.IChatActivityView;
import cn.udesk.voice.AudioRecordButton;
import cn.udesk.voice.RecordFilePlay;
import cn.udesk.voice.RecordPlay;
import cn.udesk.voice.RecordPlayCallback;
import cn.udesk.widget.UDPullGetMoreListView;
import cn.udesk.widget.UdeskConfirmPopWindow;
import cn.udesk.widget.UdeskConfirmPopWindow.OnPopConfirmClick;
import cn.udesk.widget.UdeskExpandableLayout;
import cn.udesk.widget.UdeskMultiMenuHorizontalWindow;
import cn.udesk.widget.UdeskMultiMenuHorizontalWindow.OnPopMultiMenuClick;
import cn.udesk.widget.UdeskSurvyPopwindow;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.UdeskConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.event.InvokeEventContainer;
import udesk.core.model.AgentInfo;
import udesk.core.model.MessageInfo;
import udesk.core.model.Product;
import udesk.core.utils.UdeskUtils;
import udesk.core.xmpp.XmppInfo;


public class UdeskChatActivity extends UdeskBaseActivity implements IChatActivityView, IEmotionSelectedListener,
        OnClickListener {
    LinearLayout mContentLinearLayout;//消息内容区域
    ImageView mAudioImg; //语言和内容
    private TextView sendBtn;
    private AudioRecordButton mBtnAudio;
    ImageView mEmojiImg;
    ImageView mMoreImg;
    FrameLayout mBotomFramlayout;
    EmotionLayout mEmotionlayout;
    LinearLayout mMoreLayout;
    private EmotionKeyboard mEmotionKeyboard;

    private EditText mInputEditView;
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


    private int historyCount = 0; // 记录数据库中总的记录数
    private int offset = -1; // 标记偏移值
    public static final int InitViewMode = 1; //初始化
    public static final int PullRefreshModel = 2; //下拉刷新
    public static final int PullEventModel = 3; //拉去工单回复
    private long preMsgSendTime = 0; //记录发送预支消息间隔时间
    private long QUEUE_RETEY_TIME = 20 * 1000; // 客服繁忙时  轮询的间隔时间

    private final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
    private final int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = 102;
    private final int SELECT_FILE_OPTION_REQUEST_CODE = 104;
    private final int SELECT_LOCATION_OPTION_REQUEST_CODE = 105;
    private final int SELECT_UDESK_IMAGE_ACTIVITY_REQUEST_CODE = 106;
    private final int CAPTURE_IMAGE_SMALLVIDEO_ACTIVITY_REQUEST_CODE = 107;

    private MyHandler mHandler;
    private ChatActivityPresenter mPresenter;
    private BroadcastReceiver mConnectivityChangedReceiver = null;
    private boolean isSurvyOperate = false;//如果是收到客服的满意度调查，则在onresume 处不在请求分配客服
    private boolean isInitComplete = false; //标识进入请求分配客服的流程是否结束
    private boolean isOverConversation = false;//标识会话是否客服已经关闭会话

    private boolean isFirstFinish = true;//第一返回,如果设置了弹出满意度评价则弹出,无论评价与否,第二次
    private boolean isLeavingmsg = false;
    private boolean isPermmitSurvy = true;
    private boolean isNeedOpenLocalCamera = false;
    //    private boolean isfirstWaitTips = true;
    private boolean isDestroyed = false; //表示是否执行ondestory

    private boolean isPresessionStatus = false;//标记是否是无消息对话状态
    String pre_session_id = ""; //无消息对话状态下id

    //咨询对象的展示界面
    private View commodityView;
    public SimpleDraweeView commodityThumbnail;
    public TextView commodityTitle;
    public TextView commoditySubTitle;
    public TextView commodityLink;


    GridView funGridView;
    List<FunctionMode> functionItems = new ArrayList<FunctionMode>();
    UdeskFunctionAdapter udeskFunctionAdapter;

    private LinearLayout navigationRootView, addNavigationFragmentView, navigation_survy;

    //排队中的消息提醒
    private UdeskQueueItem queueItem;
    private boolean isInTheQueue = false;
    //最多允许发送20条消息
    boolean isMoreThan20 = false;

    public static class MessageWhat {
        public static final int loadHistoryDBMsg = 1;
        public static final int NoAgent = 2;
        public static final int HasAgent = 3;
        public static final int WaitAgent = 4;
        public static final int refreshAdapter = 5;
        public static final int changeImState = 6;
        public static final int onNewMessage = 7;
        public static final int pre_session_status = 8;
        public static final int RECORD_ERROR = 10;
        public static final int status_notify = 14;
        public static final int redirectSuccess = 15;
        public static final int surveyNotify = 16;
        public static final int IM_STATUS = 17;
        public static final int IM_BOLACKED = 18;
        public static final int Has_Survey = 19;
        public static final int Survey_error = 20;
        public static final int Add_UdeskEvent = 21;
        public static final int ChangeFielProgress = 22;
        public static final int ChangeVideoThumbnail = 23;
        public static final int Survey_Success = 24;
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
                            if (UdeskSDKManager.getInstance().getUdeskConfig().commodity != null) {
                                activity.showCommodity(UdeskSDKManager.getInstance().getUdeskConfig().commodity);
                            }
                            int selectIndex = msgs.size();
                            if (msg.arg1 == UdeskChatActivity.PullEventModel) {
                                activity.mChatAdapter.listAddEventItems(msgs);
                            } else if (msg.arg1 == UdeskChatActivity.PullRefreshModel) {
                                activity.mChatAdapter.listAddItems(msgs, true);
                            } else {
                                activity.mChatAdapter.listAddItems(msgs, false);
                            }
                            activity.mListView.onRefreshComplete();
                            if (msg.arg1 == UdeskChatActivity.InitViewMode || msg.arg1 == UdeskChatActivity.PullEventModel) {
                                activity.mListView.setSelection(activity.mChatAdapter.getCount());
                            } else {
                                activity.mListView.setSelection(selectIndex);
                            }
                        }
                        break;
                    case MessageWhat.NoAgent:
                        activity.isInTheQueue = false;
                        activity.dismissFormWindow();
                        if (activity.mPresenter != null) {
                            activity.mPresenter.sendPrefilterMsg(false);
                        }
                        if (activity.isleaveMessageTypeMsg()) {
                            if (activity.mPresenter != null) {
                                activity.mPresenter.addLeavMsgWeclome(UdeskSDKManager.getInstance().getImSetting().getLeave_message_guide());
                            }
                            activity.setUdeskImContainerVis(View.GONE);
                            activity.setTitlebar(activity.getString(R.string.udesk_ok), "off");
                        } else {
                            activity.mAgentInfo = (AgentInfo) msg.obj;
                            activity.setTitlebar(activity.getString(R.string.udesk_label_customer_offline), "off");
                            activity.delayShowtips(this);
                        }
                        if (activity.queueItem != null && activity.mChatAdapter != null) {
                            activity.mChatAdapter.removeQueueMessage(activity.queueItem);
                            activity.queueItem = null;
                            activity.mListView.smoothScrollToPosition(activity.mChatAdapter.getCount());
                        }
                        break;
                    case MessageWhat.HasAgent:
                        activity.isInTheQueue = false;
                        activity.dismissFormWindow();
                        activity.setUdeskImContainerVis(View.VISIBLE);
                        activity.mAgentInfo = (AgentInfo) msg.obj;
                        activity.currentStatusIsOnline = true;
                        activity.showOnlieStatus(activity.mAgentInfo);
                        activity.setNavigationViewVis();
                        activity.initfunctionItems();
                        if (activity.queueItem != null && activity.mChatAdapter != null) {
                            activity.mChatAdapter.removeQueueMessage(activity.queueItem);
                            activity.queueItem = null;
                            activity.mListView.smoothScrollToPosition(activity.mChatAdapter.getCount());
                        }
                        if (activity.mPresenter != null) {
                            activity.mPresenter.pullMessages(0, activity.mAgentInfo.getIm_sub_session_id());
                            activity.mPresenter.sendPrefilterMsg(true);
                            activity.mPresenter.selfretrySendMsg();
                        }
                        if (!activity.hasSendCommodity) {
                            activity.hasSendCommodity = true;
                            activity.sendCommodityMsg(UdeskSDKManager.getInstance().getUdeskConfig().commodity);
                            activity.sendProduct(UdeskSDKManager.getInstance().getUdeskConfig().mProduct);
                        }
                        if (!activity.hasSendFirstMessage) {
                            activity.hasSendFirstMessage = true;
                            activity.sendDefualtMessage();
                        }
                        activity.sendVideoMessage();
                        break;
                    case MessageWhat.WaitAgent:
                        activity.isInTheQueue = true;
                        activity.isPresessionStatus = false;
                        activity.isOverConversation = false;
                        activity.mAgentInfo = (AgentInfo) msg.obj;
                        activity.setTitlebar(activity.getResources().getString(R.string.udesk_in_the_line), "queue");
                        activity.setUdeskImContainerVis(View.VISIBLE);
                        this.postDelayed(activity.myRunnable, activity.QUEUE_RETEY_TIME);
                        if (activity.mPresenter != null) {
                            activity.mPresenter.sendPrefilterMsg(true);
                        }
                        if (!activity.hasSendCommodity) {
                            activity.hasSendCommodity = true;
                            activity.sendCommodityMsg(UdeskSDKManager.getInstance().getUdeskConfig().commodity);
                            activity.sendProduct(UdeskSDKManager.getInstance().getUdeskConfig().mProduct);
                        }
                        if (!activity.hasSendFirstMessage) {
                            activity.hasSendFirstMessage = true;
                            activity.sendDefualtMessage();
                        }
                        if (activity.queueItem == null) {
                            activity.queueItem = new UdeskQueueItem(activity.isOpenLeaveMsg(), activity.mAgentInfo.getMessage());
                            if (activity.mChatAdapter != null) {
                                activity.mChatAdapter.addItem(activity.queueItem);
                                activity.mListView.smoothScrollToPosition(activity.mChatAdapter.getCount());
                            }
                        } else {
                            activity.queueItem.setQueueContent(activity.mAgentInfo.getMessage());
                            activity.mChatAdapter.notifyDataSetChanged();
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
                    case MessageWhat.ChangeVideoThumbnail:
                        String videoMsgId = (String) msg.obj;
                        activity.changeVideoThumbnail(videoMsgId);
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
                                if (activity.mAgentInfo != null && activity.mAgentInfo.getAgentCode() == UdeskConst.AgentReponseCode.HasAgent) {
                                    msgInfo.setUser_avatar(activity.mAgentInfo.getHeadUrl());
                                    msgInfo.setReplyUser(activity.mAgentInfo.getAgentNick());
                                    if (!activity.mAgentInfo.getAgentJid().contains(msgInfo.getmAgentJid())) {
                                        activity.mAgentInfo.setAgentJid(msgInfo.getmAgentJid());
                                        activity.mPresenter.createIMCustomerInfo();
                                    }
                                } else if (activity.mAgentInfo != null && activity.mAgentInfo.getAgentCode() == UdeskConst.AgentReponseCode.WaitAgent) {
                                    if (activity.myRunnable != null) {
                                        this.removeCallbacks(activity.myRunnable);
                                        this.post(activity.myRunnable);
                                    }
                                }else{
                                    if (activity.mPresenter != null) {
                                        activity.mPresenter.getAgentInfo(activity.pre_session_id, null);
                                    }
                                }
                                activity.mChatAdapter.addItem(msgInfo);
                                activity.mListView.smoothScrollToPosition(activity.mChatAdapter.getCount());
                            }
                        }
                        break;
                    case MessageWhat.pre_session_status:
                        String title = (String) msg.obj;
                        activity.isPresessionStatus = true;
                        activity.mAgentInfo = null;
                        activity.setTitlebar(title, "on");
                        break;
                    case MessageWhat.RECORD_ERROR: {
                        activity.recordError();
                    }
                    case MessageWhat.status_notify:
                        int onlineflag = msg.arg1;
                        String jid = (String) msg.obj;
                        if (activity.mAgentInfo == null ||
                                activity.isbolcked.equals("true") ||
                                TextUtils.isEmpty(activity.mAgentInfo.getAgentJid())
                                || !jid.contains(activity.mAgentInfo.getAgentJid())) {
                            return;
                        }
                        if (onlineflag == UdeskConst.ONLINEFLAG) {
                            if (!activity.currentStatusIsOnline && activity.isNeedStartExpandabLyout) {
                                activity.expandableLayout.startAnimation(true);
                                activity.currentStatusIsOnline = true;
                                activity.isNeedStartExpandabLyout = false;
                            }
                            activity.showOnlieStatus(activity.mAgentInfo);
                            activity.setUdeskImContainerVis(View.VISIBLE);
                            if (activity.popWindow != null) {
                                activity.popWindow.cancle();
                            }
                        } else if (onlineflag == UdeskConst.OFFLINEFLAG) {
                            if (activity.mPresenter != null) {
                                activity.mPresenter.getIMStatus(activity.mAgentInfo);
                            }
                        }
                        break;
                    case MessageWhat.IM_STATUS:
                        String imStatus = (String) msg.obj;
                        if (imStatus.equals("off")) {
                            activity.isInTheQueue = false;
                            activity.isInitComplete = true;
                            if (activity.isleaveMessageTypeMsg()) {
                                activity.setUdeskImContainerVis(View.GONE);
                                if (activity.mPresenter != null) {
                                    activity.mPresenter.addLeavMsgWeclome(UdeskSDKManager.getInstance().getImSetting().getLeave_message_guide());
                                }
                                activity.setTitlebar(activity.getString(R.string.udesk_ok), "off");
                            } else {
                                if (activity.mAgentInfo != null) {
                                    activity.setTitlebar(activity.mAgentInfo.getAgentNick(), "off");
                                } else {
                                    activity.setTitlebar(activity.getResources().getString(
                                            R.string.udesk_label_customer_offline), "off");
                                }
                                activity.delayShowtips(this);
                            }
                            if (activity.currentStatusIsOnline) {
                                activity.expandableLayout.startAnimation(false);
                                activity.currentStatusIsOnline = false;
                                activity.isNeedStartExpandabLyout = true;
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
                    case MessageWhat.Survey_Success:
                        UdeskUtils.showToast(activity, activity.getResources()
                                .getString(R.string.udesk_thanks_survy));
                        break;
                    case MessageWhat.redirectSuccess:
                        MessageInfo redirectSuccessmsg = (MessageInfo) msg.obj;
                        if (activity.mChatAdapter != null) {
                            activity.mChatAdapter.addItem(redirectSuccessmsg);
                            activity.mListView.smoothScrollToPosition(activity.mChatAdapter.getCount());
                        }
                        activity.currentStatusIsOnline = true;
                        activity.showOnlieStatus(activity.mAgentInfo);
                        activity.sendVideoMessage();
                        break;
                    case MessageWhat.surveyNotify:
                        SurveyOptionsModel surveyOptions = (SurveyOptionsModel) msg.obj;
                        if (surveyOptions != null) {
                            activity.toLuanchSurveyView(surveyOptions);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            UdeskUtils.resetTime();
            LocalManageUtil.getSetLanguageLocale();
            UdeskUtil.setOrientation(this);
            if (!Fresco.hasBeenInitialized()) {
                UdeskUtil.frescoInit(this);
            }
            setContentView(R.layout.udesk_activity_im);
            mHandler = new MyHandler(UdeskChatActivity.this);
            mPresenter = new ChatActivityPresenter(this);

            InvokeEventContainer.getInstance().event_IsOver.bind(this, "isOverConversation");
            isOverConversation = false;
            initIntent();
            settingTitlebar();
            initView();
            //进入会话界面 关闭推送
            if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getRegisterId(UdeskChatActivity.this))) {
                UdeskSDKManager.getInstance().setSdkPushStatus(UdeskSDKManager.getInstance().getDomain(this),
                        UdeskSDKManager.getInstance().getAppkey(this),
                        UdeskSDKManager.getInstance().getSdkToken(getApplicationContext()), UdeskConfig.UdeskPushFlag.OFF,
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
            //重导航传入groupid的优先级 高于设置的groupid
            if (TextUtils.isEmpty(groupId) && !TextUtils.isEmpty(UdeskSDKManager.getInstance().getUdeskConfig().groupId)) {
                groupId = UdeskSDKManager.getInstance().getUdeskConfig().groupId;
            }
            if (TextUtils.isEmpty(agentId) && !TextUtils.isEmpty(UdeskSDKManager.getInstance().getUdeskConfig().agentId)) {
                agentId = UdeskSDKManager.getInstance().getUdeskConfig().agentId;
            }
            if (!TextUtils.isEmpty(groupId)) {
                PreferenceHelper.write(this, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                        UdeskConst.SharePreParams.Udesk_Group_Id, groupId);
            }
            PreferenceHelper.write(this, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                    UdeskConst.SharePreParams.Udesk_Agent_Id, agentId);
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
                UdekConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarTextLeftRightResId, mTitlebar.getLeftTextView(), mTitlebar.getRightTextView());
                if (mTitlebar.getRootView() != null) {
                    UdekConfigUtil.setUIbgDrawable(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarBgResId, mTitlebar.getRootView());
                }
                if (UdeskConfig.DEFAULT != UdeskSDKManager.getInstance().getUdeskConfig().udeskbackArrowIconResId) {
                    mTitlebar.getUdeskBackImg().setImageResource(UdeskSDKManager.getInstance().getUdeskConfig().udeskbackArrowIconResId);
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

    private void showCommodity(final UdeskCommodityItem item) {
        try {
            commodityView.setVisibility(View.VISIBLE);
            commodityTitle.setText(item.getTitle());
            commoditySubTitle.setText(item.getSubTitle());
            UdeskUtil.loadNoChangeView(getApplicationContext(), commodityThumbnail, Uri.parse(item.getThumbHttpUrl()));
            commodityLink.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sentLink(item.getCommodityUrl());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        try {
            commodityView = findViewById(R.id.commodity_rl);
            commodityView.setVisibility(View.GONE);
            commodityThumbnail = (SimpleDraweeView) findViewById(R.id.udesk_im_commondity_thumbnail);
            commodityTitle = (TextView) findViewById(R.id.udesk_im_commondity_title);
            commoditySubTitle = (TextView) findViewById(R.id.udesk_im_commondity_subtitle);
            commodityLink = (TextView) findViewById(R.id.udesk_im_commondity_link);

            popWindow = new UdeskConfirmPopWindow(this);
            sendBtn = (TextView) findViewById(R.id.udesk_bottom_send);
            sendBtn.setOnClickListener(this);
            mInputEditView = (EditText) findViewById(R.id.udesk_bottom_input);

            funGridView = (GridView) findViewById(R.id.function_gridview);
            udeskFunctionAdapter = new UdeskFunctionAdapter(this);
            funGridView.setAdapter(udeskFunctionAdapter);
            initfunctionItems();
            funGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    try {
                        FunctionMode functionItem = (FunctionMode) adapterView.getItemAtPosition(i);
                        switch (functionItem.getId()) {
                            case UdeskConst.UdeskFunctionFlag.Udesk_Camera:
                                clickCamera();
                                mBotomFramlayout.setVisibility(View.GONE);
                                break;
                            case UdeskConst.UdeskFunctionFlag.Udesk_Photo:
                                clickPhoto();
                                mBotomFramlayout.setVisibility(View.GONE);
                                break;
                            case UdeskConst.UdeskFunctionFlag.Udesk_Udesk_File:
                                clickFile();
                                mBotomFramlayout.setVisibility(View.GONE);
                                break;
                            case UdeskConst.UdeskFunctionFlag.Udesk_Survy:
                                if (isPresessionStatus) {
                                    UdeskUtils.showToast(getApplicationContext(),
                                            getString(R.string.udesk_can_not_be_evaluated));
                                    return;
                                }
                                clickSurvy();
                                break;
                            case UdeskConst.UdeskFunctionFlag.Udesk_Location:
                                if (Build.VERSION.SDK_INT < 23) {
                                    clickLocation();
                                    mBotomFramlayout.setVisibility(View.GONE);
                                } else {
                                    XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.LOCATION,
                                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                                    Manifest.permission.READ_PHONE_STATE},
                                            new XPermissionUtils.OnPermissionListener() {
                                                @Override
                                                public void onPermissionGranted() {
                                                    clickLocation();
                                                    mBotomFramlayout.setVisibility(View.GONE);
                                                }

                                                @Override
                                                public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                                    Toast.makeText(UdeskChatActivity.this,
                                                            getResources().getString(R.string.location_denied),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }

                                break;
                            case UdeskConst.UdeskFunctionFlag.Udesk_Video:
                                if (isPresessionStatus) {
                                    UdeskUtils.showToast(getApplicationContext(), getString(R.string.udesk_can_not_be_video));
                                    return;
                                }
                                startVideo();
                                mBotomFramlayout.setVisibility(View.GONE);
                                break;
                            default:
                                if (UdeskSDKManager.getInstance().getUdeskConfig().functionItemClickCallBack != null
                                        && mPresenter != null) {
                                    UdeskSDKManager.getInstance().getUdeskConfig().functionItemClickCallBack
                                            .callBack(getApplicationContext(), mPresenter, functionItem.getId(), functionItem.getName());
                                }
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mListView = (UDPullGetMoreListView) findViewById(R.id.udesk_conversation);
            expandableLayout = (UdeskExpandableLayout) findViewById(R.id.udesk_change_status_info);

            mContentLinearLayout = (LinearLayout) findViewById(R.id.udesk_content_ll);
            mAudioImg = (ImageView) findViewById(R.id.udesk_img_audio);
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseVoice) {
                mAudioImg.setVisibility(View.VISIBLE);
            } else {
                mAudioImg.setVisibility(View.GONE);
            }
            mBtnAudio = (AudioRecordButton) findViewById(R.id.udesk_audio_btn);
            mBtnAudio.init(UdeskUtils.getDirectoryPath(getApplicationContext(), UdeskConst.FileAduio));
            mBtnAudio.setRecordingListener(new AudioRecordButton.OnRecordingListener() {
                @Override
                public void recordStart() {
                    if (mRecordFilePlay != null) {
                        showStartOrStopAnaimaition(
                                mRecordFilePlay.getPlayAduioMessage(), false);
                        recycleVoiceRes();
                    }

                }

                @Override
                public void recordFinish(String audioFilePath, long recordTime) {
                    if (mPresenter != null) {
                        mPresenter.sendRecordAudioMsg(audioFilePath, recordTime);
                    }
                }

                @Override
                public void recordError(String message) {
                    Message msg = Message.obtain();
                    msg.what = MessageWhat.RECORD_ERROR;
                    mHandler.sendMessage(msg);

                }
            });

            mEmojiImg = (ImageView) findViewById(R.id.udesk_emoji_img);
            showEmoji();
            mMoreImg = (ImageView) findViewById(R.id.udesk_more_img);
            mBotomFramlayout = (FrameLayout) findViewById(R.id.udesk_bottom_frame);
            mEmotionlayout = (EmotionLayout) findViewById(R.id.udesk_emotion_view);
            mMoreLayout = (LinearLayout) findViewById(R.id.udesk_more_layout);
            mEmotionlayout.attachEditText(mInputEditView);
            initEmotionKeyboard();

            navigationRootView = findViewById(R.id.navigation_root_view);
            addNavigationFragmentView = findViewById(R.id.fragment_view);
            navigation_survy = findViewById(R.id.navigation_survy);
            navigation_survy.setOnClickListener(this);
            setNavigationViewVis();
            setListView();
            initDatabase();
            mPresenter.createIMCustomerInfo();
            isNeedRelogin = !UdeskUtils.isNetworkConnected(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showEmoji() {
        try {
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseEmotion && LQREmotionKit.getEmotionPath() != null) {
                mEmojiImg.setVisibility(View.VISIBLE);
            } else {
                mEmojiImg.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initfunctionItems() {
        try {
            functionItems.clear();
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUsecamera) {
                FunctionMode cameraItem = new FunctionMode(getString(R.string.funtion_camera), UdeskConst.UdeskFunctionFlag.Udesk_Camera, R.drawable.udesk_camer_normal1);
                functionItems.add(cameraItem);
            }
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUsephoto) {
                FunctionMode photoItem = new FunctionMode(getString(R.string.photo), UdeskConst.UdeskFunctionFlag.Udesk_Photo, R.drawable.udesk_image_normal1);
                functionItems.add(photoItem);
            }
            if (isOpenVideo() && mAgentInfo != null) {
                FunctionMode videoItem = new FunctionMode(getString(R.string.video), UdeskConst.UdeskFunctionFlag.Udesk_Video, R.drawable.udesk_video_normal);
                functionItems.add(videoItem);
            }

            if (mAgentInfo != null && UdeskSDKManager.getInstance().getImSetting() != null && UdeskSDKManager.getInstance().getImSetting().getEnable_im_survey()) {
                FunctionMode survyItem = new FunctionMode(getString(R.string.survy), UdeskConst.UdeskFunctionFlag.Udesk_Survy, R.drawable.udesk_survy_normal);
                functionItems.add(survyItem);
            }

            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseMap) {
                FunctionMode mapItem = new FunctionMode(getString(R.string.location), UdeskConst.UdeskFunctionFlag.Udesk_Location, R.drawable.udesk_location_normal);
                functionItems.add(mapItem);
            }

            if (UdeskSDKManager.getInstance().getUdeskConfig().isUsefile) {
                FunctionMode fileItem = new FunctionMode(getString(R.string.file), UdeskConst.UdeskFunctionFlag.Udesk_Udesk_File, R.drawable.udesk_file_icon);
                functionItems.add(fileItem);
            }

            if (UdeskSDKManager.getInstance().getUdeskConfig().extreFunctions != null
                    && UdeskSDKManager.getInstance().getUdeskConfig().extreFunctions.size() > 0) {
                functionItems.addAll(UdeskSDKManager.getInstance().getUdeskConfig().extreFunctions);
            }

            udeskFunctionAdapter.setFunctionItems(functionItems);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNavigationViewVis() {
        try {
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseNavigationRootView) {
                navigationRootView.setVisibility(View.VISIBLE);
            } else {
                navigationRootView.setVisibility(View.GONE);
            }
            if (UdeskSDKManager.getInstance().getUdeskConfig().navigationModes != null
                    && UdeskSDKManager.getInstance().getUdeskConfig().navigationModes.size() > 0) {
                addNavigationFragment();
            }
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseNavigationSurvy && mAgentInfo != null &&
                    UdeskSDKManager.getInstance().getImSetting() != null
                    && UdeskSDKManager.getInstance().getImSetting().getEnable_im_survey()) {
                navigation_survy.setVisibility(View.VISIBLE);
            } else {
                navigation_survy.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void recordError() {
        try {
            UdeskUtils.showToast(UdeskChatActivity.this.getApplicationContext(), getResources()
                    .getString(R.string.udesk_im_record_error));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean isOpenVideo() {
        try {
            return UdeskSDKManager.getInstance().getImSetting() != null
                    && UdeskSDKManager.getInstance().getImSetting().getVcall()
                    && UdeskSDKManager.getInstance().getImSetting().getSdk_vcall()
                    && UdeskUtil.isClassExists("udesk.udeskvideo.UdeskVideoActivity");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initEmotionKeyboard() {
        try {
            mEmotionKeyboard = EmotionKeyboard.with(this);
            mEmotionKeyboard.bindToEditText(mInputEditView);
            mEmotionKeyboard.bindToContent(mContentLinearLayout);
            mEmotionKeyboard.setEmotionLayout(mBotomFramlayout);
            mEmotionKeyboard.bindToEmotionButton(mEmojiImg, mMoreImg);
            mEmotionKeyboard.setOnEmotionButtonOnClickListener(new EmotionKeyboard.OnEmotionButtonOnClickListener() {
                @Override
                public boolean onEmotionButtonOnClickListener(View view) {
                    try {
                        if (isbolcked != null && isbolcked.equals("true")) {
                            toBolckedView();
                            return true;
                        }
                        if (isMoreTanSendCount() && isNeedQueueMessageSave()) {
                            UdeskUtils.showToast(getApplicationContext(), getMoreThanSendTip());
                            mEmotionKeyboard.hideSoftInput();
                            return true;
                        }
                        if (!isShowNotSendMsg()) {
                            mEmotionKeyboard.hideSoftInput();
                            return true;
                        }
                        int i = view.getId();
                        if (i == R.id.udesk_emoji_img) {
                            if (!mEmotionlayout.isShown()) {
                                if (mMoreLayout.isShown()) {
                                    showEmotionLayout();
                                    hideMoreLayout();
                                    hideAudioButton();
                                    return true;
                                }
                            } else if (mEmotionlayout.isShown() && !mMoreLayout.isShown()) {
                                mEmojiImg.setImageResource(R.drawable.udesk_ic_cheat_emo);
                                return false;
                            }
                            showEmotionLayout();
                            hideMoreLayout();
                            hideAudioButton();

                        } else if (i == R.id.udesk_more_img) {
                            if (!mMoreLayout.isShown()) {
                                if (mEmotionlayout.isShown()) {
                                    showMoreLayout();
                                    hideEmotionLayout();
                                    hideAudioButton();
                                    return true;
                                }
                            }
                            showMoreLayout();
                            hideEmotionLayout();
                            hideAudioButton();

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });

            initListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initListener() {
        try {
            mAudioImg.setOnClickListener(this);
            //触摸消息区域　隐藏键盘
//        mContentLinearLayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                bottomoPannelBegginStatus();
//                return false;
//            }
//        });

            mEmotionlayout.setEmotionSelectedListener(this);
            mEmotionlayout.setEmotionAddVisiable(true);
            mEmotionlayout.setEmotionSettingVisiable(true);


            mInputEditView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        if (mInputEditView.getText().toString().trim().length() > 0) {
                            sendBtn.setVisibility(View.VISIBLE);
                            mMoreImg.setVisibility(View.GONE);
                        } else {
                            sendBtn.setVisibility(View.GONE);
                            if (!isleaveMessageTypeMsg() || currentStatusIsOnline || isNeedQueueMessageSave()) {
                                mMoreImg.setVisibility(View.VISIBLE);
                            }
                        }

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

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void showAudioButton() {
        try {
            mBtnAudio.setVisibility(View.VISIBLE);
            mInputEditView.setVisibility(View.GONE);
            mAudioImg.setImageResource(R.drawable.udesk_ic_cheat_keyboard);

            if (mBotomFramlayout.isShown()) {
                if (mEmotionKeyboard != null) {
                    mEmotionKeyboard.interceptBackPress();
                }
            } else {
                if (mEmotionKeyboard != null) {
                    mEmotionKeyboard.hideSoftInput();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideAudioButton() {
        try {
            mBtnAudio.setVisibility(View.GONE);
            mInputEditView.setVisibility(View.VISIBLE);
            mAudioImg.setImageResource(R.drawable.udesk_ic_cheat_voice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showEmotionLayout() {
        try {
            mEmotionlayout.setVisibility(View.VISIBLE);
            mEmojiImg.setImageResource(R.drawable.udesk_ic_cheat_keyboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideEmotionLayout() {
        try {
            mEmotionlayout.setVisibility(View.GONE);
            mEmojiImg.setImageResource(R.drawable.udesk_ic_cheat_emo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMoreLayout() {
        try {
            mMoreLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideMoreLayout() {
        try {
            mMoreLayout.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void closeBottomAndKeyboard() {
//        mEmotionlayout.setVisibility(View.GONE);
//        mMoreLayout.setVisibility(View.GONE);
//        if (mEmotionKeyboard != null) {
//            mEmotionKeyboard.interceptBackPress();
//        }
//    }

    private void setListView() {
        try {
            mChatAdapter = new MessageAdatper(this);
            mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            mListView.setAdapter(mChatAdapter);
            mListView
                    .setOnRefreshListener(new UDPullGetMoreListView.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            loadHistoryRecords(UdeskChatActivity.PullRefreshModel);
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
                    try {
                        if (UdeskDBManager.getInstance().getSQLiteDatabase() == null) {
                            UdeskDBManager.getInstance().init(getApplicationContext(), UdeskSDKManager.getInstance().getSdkToken(getApplicationContext()));
                        }
                        historyCount = UdeskDBManager.getInstance().getMessageCount();
                        loadHistoryRecords(UdeskChatActivity.InitViewMode);
                        UdeskDBManager.getInstance().updateAllMsgRead();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

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
            UdeskConst.sdk_page_status = UdeskConst.SDK_PAGE_FOREGROUND;
            UdeskBaseInfo.isNeedMsgNotice = false;
            mInputEditView.clearFocus();
            if (TextUtils.isEmpty(LQREmotionKit.getEmotionPath())) {
                LQREmotionKit.init(getApplicationContext());
            }
            if (UdeskSDKManager.getInstance().getImSetting() != null && !UdeskSDKManager.getInstance().getImSetting().getIs_worktime()) {
                return;
            }
            if (mPresenter != null) {
                mPresenter.bindReqsurveyMsg();
            }
            if (isInitComplete && !isSurvyOperate && !UdeskMessageManager.getInstance().isConnection()) {
                mPresenter.createIMCustomerInfo();
            }
            sendVideoMessage();
            registerNetWorkReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        try {
            //检查是否处在可发消息的状态
            if (isbolcked != null && isbolcked.equals("true")) {
                toBolckedView();
                return;
            }

            if (isMoreTanSendCount() && isNeedQueueMessageSave()) {
                UdeskUtils.showToast(getApplicationContext(),
                        getResources().getString(R.string.udesk_in_the_line_max_send));
                return;
            }

            if (!isShowNotSendMsg()) {
                mEmotionKeyboard.hideSoftInput();
                return;
            }

            if (v.getId() == R.id.udesk_img_audio) {
                if (mBtnAudio.isShown()) {
                    hideAudioButton();
                    mInputEditView.requestFocus();
                    if (mEmotionKeyboard != null) {
                        mEmotionKeyboard.showSoftInput();
                    }
                } else {
                    if (Build.VERSION.SDK_INT < 23) {
                        showAudioButton();
                        hideEmotionLayout();
                        hideMoreLayout();
                    } else {
                        XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.AUDIO,
                                new String[]{Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                new XPermissionUtils.OnPermissionListener() {
                                    @Override
                                    public void onPermissionGranted() {
                                        showAudioButton();
                                        hideEmotionLayout();
                                        hideMoreLayout();
                                    }

                                    @Override
                                    public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                        Toast.makeText(UdeskChatActivity.this,
                                                getResources().getString(R.string.aduido_denied),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                }
            } else if (R.id.udesk_bottom_send == v.getId()) { //发送文本消息
                if (TextUtils.isEmpty(mInputEditView.getText().toString())) {
                    UdeskUtils.showToast(getApplicationContext(),
                            getString(R.string.udesk_send_message_empty));
                    return;
                }
                if (currentStatusIsOnline || isPresessionStatus || isNeedQueueMessageSave()) {
                    mPresenter.sendTxtMessage();
                } else if (UdeskSDKManager.getInstance().getImSetting() != null &&
                        UdeskSDKManager.getInstance().getImSetting().getLeave_message_type().equals("msg")) {
                    if (!isLeavingmsg) {
                        mPresenter.addCustomerLeavMsg();
                        isLeavingmsg = true;
                    }
                    mPresenter.sendLeaveMessage();
                }
            } else if (R.id.navigation_survy == v.getId()) {
                if (isPresessionStatus) {
                    UdeskUtils.showToast(getApplicationContext(),
                            getString(R.string.udesk_can_not_be_evaluated));
                    return;
                }
                clickSurvy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }


    //点击拍摄入口
    private void clickCamera() {

        if (Build.VERSION.SDK_INT < 23) {
            takePhoto();
            bottomoPannelBegginStatus();
        } else {
            XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.CAMERA,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
    }

    //点击相册入口
    private void clickPhoto() {
        try {
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
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    //点击文件入口
    private void clickFile() {
        try {
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
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    //点击评价入口
    private void clickSurvy() {
        try {
            if (mPresenter != null && isPermmitSurvy && mAgentInfo != null) {
                setIsPermmitSurvy(false);
                mPresenter.getHasSurvey(mAgentInfo.getAgent_id(), null);
            } else {
                Toast.makeText(UdeskChatActivity.this,
                        getResources().getString(R.string.udesk_survey_error),
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    //点击位置入口
    private void clickLocation() {
        try {
            //地理位置信息打开的页面 由客户传入  并通过Activity方式 来传递数据
            if (UdeskSDKManager.getInstance().getUdeskConfig().cls != null) {
                Intent intent = new Intent(UdeskChatActivity.this, UdeskSDKManager.getInstance().getUdeskConfig().cls);
                startActivityForResult(intent, SELECT_LOCATION_OPTION_REQUEST_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //点击视频的入口
    public void startVideo() {
        try {
            if (isOpenVideo()) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), Class.forName("udesk.udeskvideo.UdeskVideoActivity"));
                intent.putExtra(UdeskConst.IsInivte, true);
                intent.putExtra(UdeskConst.ChannelName, UUID.randomUUID().toString().replaceAll("-", ""));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //电话呼叫
    public void callphone(final String mobile) {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(mobile));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
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

    @Override
    public void onEmojiSelected(String key) {

    }

    @Override
    public void onStickerSelected(String categoryName, String stickerName, String bitmapPath) {
        try {
            mPresenter.sendFileMessage(bitmapPath, UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (isMoreTanSendCount() && isNeedQueueMessageSave()) {
                UdeskUtils.showToast(getApplicationContext(), getMoreThanSendTip());
                return;
            }
            if (CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) { //拍照后发生图片
                if (Activity.RESULT_OK == resultCode) {
                    if (mPresenter != null && photoUri != null && photoUri.getPath() != null) {
                        if (UdeskSDKManager.getInstance().getUdeskConfig().isScaleImg) {
                            mPresenter.scaleBitmap(UdeskUtil.parseOwnUri(photoUri, UdeskChatActivity.this, cameraFile));
                        } else {
                            mPresenter.sendFileMessage(UdeskUtil.parseOwnUri(photoUri, UdeskChatActivity.this, cameraFile), UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
                        }
                    } else if (data != null && data.hasExtra("data") && data.getParcelableExtra("data") != null && data.getParcelableExtra("data") instanceof Bitmap && mPresenter != null) {
                        mPresenter.sendBitmapMessage((Bitmap) data.getParcelableExtra("data"));
                    }

                }
            } else if (CAPTURE_IMAGE_SMALLVIDEO_ACTIVITY_REQUEST_CODE == requestCode) {

                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }
                Bundle bundle = data.getBundleExtra(UdeskConst.SEND_BUNDLE);
                if (bundle != null) {

                    isNeedOpenLocalCamera = bundle.getBoolean(UdeskConst.Camera_Error);

                    String type = bundle.getString(UdeskConst.SEND_SMALL_VIDEO);
                    if (type.equals(UdeskConst.SMALL_VIDEO)) {
                        String path = bundle.getString(UdeskConst.PREVIEW_Video_Path);
                        mPresenter.sendFileMessage(path, UdeskConst.ChatMsgTypeString.TYPE_VIDEO);
                    } else if (type.equals(UdeskConst.PICTURE)) {
                        String path = bundle.getString(UdeskConst.BitMapData);
                        mPresenter.sendFileMessage(path, UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
                    }
                }

            } else if (SELECT_UDESK_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }
                Bundle bundle = data.getBundleExtra(UdeskConst.SEND_BUNDLE);
                if (bundle != null) {
                    ArrayList<LocalMedia> localMedias = bundle.getParcelableArrayList(UdeskConst.SEND_PHOTOS);
                    boolean isOrgin = bundle.getBoolean(UdeskConst.SEND_PHOTOS_IS_ORIGIN, false);

                    for (LocalMedia media : localMedias) {
                        final String pictureType = media.getPictureType();
                        final int mediaMimeType = UdeskUtil.isPictureType(pictureType);
                        if (mediaMimeType == UdeskUtil.TYPE_VIDEO) {
                            double size = UdeskUtils.getFileSize(new File(media.getPath()));
                            if (size >= 30 * 1024 * 1024) {
                                Toast.makeText(getApplicationContext(), getString(R.string.udesk_file_to_large), Toast.LENGTH_SHORT).show();
                                break;
                            }
                            mPresenter.sendFileMessage(media.getPath(), UdeskConst.ChatMsgTypeString.TYPE_VIDEO);
                        } else if (mediaMimeType == UdeskUtil.TYPE_IMAGE) {
                            if (isOrgin) {
                                mPresenter.sendFileMessage(media.getPath(), UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
                            } else {
                                mPresenter.scaleBitmap(media.getPath());
                            }
                        }
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
                            if (UdeskSDKManager.getInstance().getUdeskConfig().isScaleImg) {
                                mPresenter.scaleBitmap(path);
                            } else {
                                mPresenter.sendFileMessage(path, UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
                            }
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

            } else if (SELECT_FILE_OPTION_REQUEST_CODE == requestCode) {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }
                Uri mImageCaptureUri = data.getData();
                if (mImageCaptureUri != null) {
                    try {
                        if (mImageCaptureUri != null) {
                            String path = UdeskUtil.getFilePath(this, mImageCaptureUri);
                            if (this.getWindow() != null && this.getWindow().getDecorView() != null &&
                                    this.getWindow().getDecorView().getWindowToken() != null && UdeskUtil.isGpsNet(getApplicationContext())) {
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
            double size = UdeskUtils.getFileSize(new File(path));
            if (size >= 30 * 1024 * 1024) {
                Toast.makeText(getApplicationContext(), getString(R.string.udesk_file_to_large), Toast.LENGTH_SHORT).show();
                return;
            }
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

    @Override
    public void initLoadData() {
        try {
            offset = -1;
            historyCount = UdeskDBManager.getInstance().getMessageCount();
            loadHistoryRecords(UdeskChatActivity.InitViewMode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getAgentSeqNum() {

        try {
            if (mChatAdapter != null) {
                List<MessageInfo> listMessages = mChatAdapter.getList();
                for (int i = listMessages.size() - 1; i > 0; i--) {
                    MessageInfo messageUI = listMessages.get(i);
                    if (messageUI.getDirection() == UdeskConst.ChatMsgDirection.Recv) {
                        return messageUI.getSeqNum();
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
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

    private void sendProduct(Product product) {
        try {
            if (product != null && mPresenter != null) {
                mPresenter.sendProductMessage(product);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    //客户设置的第一句默认消息
    private void sendDefualtMessage() {
        try {
            if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getUdeskConfig().firstMessage) && mPresenter != null) {
                mPresenter.sendTxtMessage(UdeskSDKManager.getInstance().getUdeskConfig().firstMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //启动手机默认的选择照片
    private void selectPhoto() {
        try {
            if (Build.VERSION.SDK_INT < 21) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, SELECT_IMAGE_ACTIVITY_REQUEST_CODE);
            } else {
                Intent intent = new Intent();
                intent.setClass(UdeskChatActivity.this, PhotoSelectorActivity.class);
                startActivityForResult(intent, SELECT_UDESK_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    // 启动手机相机拍照
    private void takePhoto() {
        try {
            if (isNeedOpenLocalCamera || Build.VERSION.SDK_INT < 21 || !UdeskSDKManager.getInstance().getUdeskConfig().isUseSmallVideo) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraFile = UdeskUtil.cameaFile(UdeskChatActivity.this);
                photoUri = UdeskUtil.getOutputMediaFileUri(UdeskChatActivity.this, cameraFile);
                if (Build.VERSION.SDK_INT >= 24) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                if (photoUri != null) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                } else {
                    return;
                }
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            } else {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), UdeskCameraActivity.class);
                startActivityForResult(intent, CAPTURE_IMAGE_SMALLVIDEO_ACTIVITY_REQUEST_CODE);
            }

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
            Intent wrapperIntent = Intent.createChooser(intent, null);
            startActivityForResult(wrapperIntent, SELECT_FILE_OPTION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }


    //启动满意度调查
    private void toLuanchSurveyView(SurveyOptionsModel surveyOptions) {
        try {
            setIsPermmitSurvy(true);
            if (surveyOptions.getOptions() == null || surveyOptions.getOptions().isEmpty()
                    || surveyOptions.getType().isEmpty()) {
                UdeskUtils.showToast(this,
                        getString(R.string.udesk_no_set_survey));
                return;
            }
            showSurveyPopWindow(surveyOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    WindowManager.LayoutParams params;

    private void showSurveyPopWindow(SurveyOptionsModel surveyOptions) {

        try {
            UdeskSurvyPopwindow udeskSurvyPopwindow = new UdeskSurvyPopwindow(this, surveyOptions, new UdeskSurvyPopwindow.SumbitSurvyCallBack() {

                @Override
                public void sumbitSurvyCallBack(String optionId, String show_type, String survey_remark, String tags) {
                    mPresenter.putIMSurveyResult(optionId, show_type, survey_remark, tags);
                }
            });
            if (findViewById(R.id.udesk_im_content).getWindowToken() != null) {
                udeskSurvyPopwindow.showAtLocation(findViewById(R.id.udesk_im_content), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                params = getWindow().getAttributes();
                params.alpha = 0.7f;
                getWindow().setAttributes(params);
                udeskSurvyPopwindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        params = getWindow().getAttributes();
                        params.alpha = 1f;
                        getWindow().setAttributes(params);
                    }
                });
            }
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
            if (!popWindow.isShowing() && this.getWindow() != null
                    && this.getWindow().getDecorView() != null && this.getWindow().getDecorView().getWindowToken() != null) {
                popWindow.show(this, this.getWindow().getDecorView(),
                        positiveLabel, negativeLabel, title,
                        new OnPopConfirmClick() {
                            public void onPositiveClick() {
                                finish();
                            }

                            @Override
                            public void onNegativeClick() {
                                popWindow.dismiss();
                            }

                            @Override
                            public void callTelPhone(String phone) {

                            }

                            @Override
                            public void toWebViewAcivity(String mUrl) {

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
            String content;
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
                                try {
                                    if (isupload && !TextUtils.isEmpty(path)) {
                                        sendFile(path);
                                    }
                                    if (!isupload && info != null && mPresenter != null) {
                                        mPresenter.downFile(info);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onNegativeClick() {
                                popWindow.dismiss();
                            }

                            @Override
                            public void callTelPhone(String phone) {

                            }

                            @Override
                            public void toWebViewAcivity(String mUrl) {

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
            try {
                if (mPresenter != null) {
                    mPresenter.getAgentInfo(pre_session_id, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
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

    private void changeVideoThumbnail(String msgId) {
        try {
            if (!TextUtils.isEmpty(msgId) && mListView != null
                    && mChatAdapter != null) {
                for (int i = mListView.getChildCount() - 1; i >= 0; i--) {
                    View child = mListView.getChildAt(i);
                    if (child != null) {
                        if (mChatAdapter.changeVideoThumbnail(child, msgId)) {
                            return;
                        }
                    }
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

            if (!UdeskUtils.isNetworkConnected(this)) {
                UdeskUtils.showToast(getApplicationContext(),
                        getResources().getString(R.string.udesk_has_wrong_net));
                return false;
            }

            if (isMoreTanSendCount() && isNeedQueueMessageSave()) {
                UdeskUtils.showToast(getApplicationContext(),
                        getResources().getString(R.string.udesk_in_the_line_max_send));
                return false;
            }

            if (!isInitComplete) {
                UdeskUtils.showToast(getApplicationContext(),
                        getResources().getString(R.string.udesk_agent_inti));
                return false;
            }

            if (!TextUtils.isEmpty(pre_session_id)) {
                return true;
            }
            if (isNeedQueueMessageSave()) {
                return true;
            }
            if (isOverConversation) {
                UdeskUtils.showToast(getApplicationContext(),
                        getResources().getString(R.string.udesk_agent_inti));
                isInitComplete = false;
                isOverConversation = false;
                mPresenter.createIMCustomerInfo();
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

    private boolean isOpenLeaveMsg() {
        boolean isOpen = false;
        try {
            SDKIMSetting imsetting = UdeskSDKManager.getInstance().getImSetting();
            if (imsetting != null) {
                isOpen = imsetting.getEnable_web_im_feedback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOpen;
    }

    //弹出表单留言的提示框
    private void confirmToForm() {
        try {
            boolean isOpen;
            SDKIMSetting imsetting = UdeskSDKManager.getInstance().getImSetting();
            //是否弹出表单： 如果获取到管理员的配置，使用管理员的配置。没获取到则使用配置中默认设置
            if (imsetting != null) {
                isOpen = imsetting.getEnable_web_im_feedback();
            } else {
                isOpen = UdeskSDKManager.getInstance().getUdeskConfig().isUserForm;
            }
            //如果客户设置了回调处理留言系统，一定设置true
            if (UdeskSDKManager.getInstance().getUdeskConfig().formCallBack != null) {
                isOpen = true;
            }
            String positiveLabel = this.getString(R.string.udesk_sure);
            if (isOpen) {
                positiveLabel = this.getString(R.string.udesk_ok);
            }
            String negativeLabel = this.getString(R.string.udesk_cancel);
            String title;
            if (isOpen) {
                if (imsetting != null && !TextUtils.isEmpty(imsetting.getLeave_message_guide())) {
                    title = imsetting.getLeave_message_guide();
                } else {
                    title = this.getString(R.string.udesk_msg_offline_to_form);
                }
            } else {
                //关闭留言的文案,如果获取到后台的设置，则使用后台的设置，没获取的后台的，本地客户设置了，则使用客户的设置文案。否则用默认的文案
                if (imsetting != null && !TextUtils.isEmpty(imsetting.getNo_reply_hint())) {
                    title = imsetting.getNo_reply_hint();
                } else {
                    title = this.getString(R.string.udesk_msg_busy_default_to_form);
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
            if (!popWindow.isShowing() && this.getWindow() != null && this.getWindow().getDecorView() != null
                    && this.getWindow().getDecorView().getWindowToken() != null) {
                final String finalPositiveLabel = positiveLabel;
                popWindow.show(this, this.getWindow().getDecorView(),
                        finalPositiveLabel, negativeLabel, title,
                        new OnPopConfirmClick() {
                            public void onPositiveClick() {
                                try {
                                    dismissFormWindow();
                                    if (UdeskSDKManager.getInstance().getUdeskConfig().formCallBack != null) {
                                        UdeskSDKManager.getInstance().getUdeskConfig().formCallBack.toLuachForm(UdeskChatActivity.this);
                                        return;
                                    }
                                    if (finalPositiveLabel.equals(UdeskChatActivity.this.getString(R.string.udesk_ok))) {
                                        leaveMessage();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onNegativeClick() {
                                popWindow.dismiss();
                            }

                            @Override
                            public void callTelPhone(String phone) {
                                try {
                                    popWindow.dismiss();
                                    callphone(phone);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void toWebViewAcivity(String mUrl) {
                                try {
                                    popWindow.dismiss();
                                    Intent intent = new Intent(UdeskChatActivity.this, UdeskWebViewUrlAcivity.class);
                                    intent.putExtra(UdeskConst.WELCOME_URL, mUrl);
                                    startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void leaveMessage() {
        try {
            if (UdeskSDKManager.getInstance().getUdeskConfig().formCallBack != null) {
                UdeskSDKManager.getInstance().getUdeskConfig().formCallBack.toLuachForm(UdeskChatActivity.this);
                return;
            }
            if (isleaveMessageTypeMsg()) {
                //直接留言
                isInTheQueue = false;
                if (mAgentInfo != null) {
                    mAgentInfo = null;
                }
                if (mHandler != null && myRunnable != null) {
                    mHandler.removeCallbacks(myRunnable);
                }
                if (mPresenter != null) {
                    mPresenter.quitQuenu(UdeskConfig.UdeskQuenuFlag.FORCE_QUIT);
                    mPresenter.addLeavMsgWeclome(UdeskSDKManager.getInstance().getImSetting().getLeave_message_guide());
                }
                setUdeskImContainerVis(View.GONE);
                setTitlebar(getString(R.string.udesk_ok), "off");

            } else {
                goToForm();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //启动留言界面
    protected void goToForm() {
        try {
            dismissFormWindow();
            UdeskSDKManager.getInstance().goToForm(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());
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
                    isInTheQueue = false;
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

    @Override
    public void updatePreSessionStatus(String pre_session_id) {
        isInitComplete = true;
        if (TextUtils.isEmpty(pre_session_id)) {
            this.pre_session_id = "";
            isPresessionStatus = false;
        } else {
            this.pre_session_id = pre_session_id;
            isPresessionStatus = true;
        }

    }


    @Override
    public boolean getPressionStatus(MessageInfo msg) {
        try {
            if (isPresessionStatus || isOverConversation) {
                JSONObject preMessage = JsonObjectUtils.buildPreSessionInfo(msg.getMsgtype(), msg.getMsgContent(), msg.getMsgId(),
                        msg.getDuration(), pre_session_id, msg.getFilename(), msg.getFilesize());
                mPresenter.getAgentInfo(pre_session_id, preMessage);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isPresessionStatus;
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
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
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
                        } else {
                            if (isstart) {
                                holder.startAnimationDrawable();
                            } else {
                                holder.endAnimationDrawable();
                            }

                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


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
            if ((TextUtils.isEmpty(message.getLocalPath()) || !UdeskUtils.isExitFileByPath(message.getLocalPath()))
                    && !UdeskUtils.fileIsExitByUrl(UdeskChatActivity.this.getApplicationContext(), UdeskConst.FileAduio, message.getMsgContent())) {
                mPresenter.downAudio(message);
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

            mPlayCallback = null;
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
        try {
            if (!UdeskUtils.isNetworkConnected(this)) {
                UdeskUtils.showToast(this,
                        getResources().getString(R.string.udesk_has_wrong_net));
                return;
            }
            if (mPresenter != null) {
                if (currentStatusIsOnline) {
                    mPresenter.sendTxtMessage(linkMsg);
                } else if (isPresessionStatus) {
                    Toast.makeText(UdeskChatActivity.this,
                            getResources().getString(R.string.udesk_agent_connecting),
                            Toast.LENGTH_SHORT).show();
                    mPresenter.sendTxtMessage(linkMsg);
                } else if (UdeskSDKManager.getInstance().getImSetting() != null &&
                        UdeskSDKManager.getInstance().getImSetting().getLeave_message_type().equals("msg")) {
                    if (!isLeavingmsg) {
                        mPresenter.addCustomerLeavMsg();
                        isLeavingmsg = true;
                    }
                    mPresenter.sendLeaveMessage(linkMsg);
                } else if (isNeedQueueMessageSave()) {
                    if (isMoreTanSendCount()) {
                        UdeskUtils.showToast(getApplicationContext(), getMoreThanSendTip());
                        return;
                    }
                    //排队中需要保存消息
                    mPresenter.sendTxtMessage(linkMsg);
                } else {
                    confirmToForm();
                }

            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    //重试发送消息(无消息对话过滤状态，排队，在线)
    public void retrySendMsg(MessageInfo message) {
        try {
            if (!UdeskUtils.isNetworkConnected(this)) {
                UdeskUtils.showToast(this,
                        getResources().getString(R.string.udesk_has_wrong_net));
                return;
            }
            if (isMoreTanSendCount() && isNeedQueueMessageSave()) {
                UdeskUtils.showToast(getApplicationContext(), getMoreThanSendTip());
                return;
            }
            //不在无消息对话过滤状态，也不在排队，也不是在线情况下 提示客服
            if (!isPresessionStatus && !currentStatusIsOnline && !isInTheQueue) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_label_customer_offline));
                return;
            }
            if (mPresenter != null && message != null) {
                changeImState(message.getMsgId(), UdeskConst.SendFlag.RESULT_RETRY);
                mPresenter.startRetryMsg(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //取消上传视频消息
    public void cancleSendVideoMsg(MessageInfo message) {
        try {
            if (mPresenter != null && message != null) {
                mPresenter.cancleUploadFile(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下载文件
    public void downLoadMsg(MessageInfo message) {
        try {
            if (!UdeskUtils.isNetworkConnected(this)) {
                UdeskUtils.showToast(this,
                        getResources().getString(R.string.udesk_has_wrong_net));
                return;
            }
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

    public synchronized void downLoadVideo(MessageInfo message) {
        try {
            if (!UdeskUtils.isNetworkConnected(this)) {
                UdeskUtils.showToast(this,
                        getResources().getString(R.string.udesk_has_wrong_net));
                return;
            }
            if (mPresenter != null && message != null) {
                mPresenter.downVideo(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void bottomoPannelBegginStatus() {
        try {
            mEmotionKeyboard.hideSoftInput();
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
                } else if (status.equals("queue")) {
                    mTitlebar.getudeskStateImg().setImageResource(R.drawable.udesk_queue_status);
                }

                mTitlebar.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (mBotomFramlayout.isShown()) {
                            if (mEmotionKeyboard != null) {
                                mEmotionKeyboard.interceptBackPress();
                            }
                        } else {
                            if (mEmotionKeyboard != null) {
                                mEmotionKeyboard.hideSoftInput();
                            }
                        }
                        return false;
                    }
                });

            }
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
                isMoreThan20 = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        try {
            if (mEmotionlayout.isShown() || mMoreLayout.isShown()) {
                mEmotionKeyboard.interceptBackPress();
            } else {
                finishAcitivty();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void finishAcitivty() {
        //后台勾选开启后，对于同一个对话，用户多次进入，点击返回离开，若没有进行过满意度调查，
        // 则返回点击后均弹出满意度调查窗口，若已经有满意度调查结果，则返回不再发起调查都关闭.

        if (!isFirstFinish) {
            finish();
            return;
        }
        isFirstFinish = false;
        try {
            SDKIMSetting imsetting = UdeskSDKManager.getInstance().getImSetting();
            if (!currentStatusIsOnline || imsetting == null || !imsetting.isInvestigation_when_leave() || !imsetting.getEnable_im_survey()) {
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
        try {
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseVoice) {
                mAudioImg.setVisibility(vis);
                if (vis == View.GONE) {
                    mInputEditView.setVisibility(View.VISIBLE);
                    mBtnAudio.setVisibility(View.GONE);
                }
            }
            showEmoji();
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseMore) {
                mMoreImg.setVisibility(vis);
                if (vis == View.GONE) {
                    hideMoreLayout();
                }
            }
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseNavigationRootView) {
                navigationRootView.setVisibility(vis);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isleaveMessageTypeMsg() {
        try {
            SDKIMSetting sdkimSetting = UdeskSDKManager.getInstance().getImSetting();
            return sdkimSetting != null && sdkimSetting.getEnable_web_im_feedback() && sdkimSetting.getLeave_message_type().equals("msg");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            XPermissionUtils.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showVideoThumbnail(final MessageInfo info) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap bitmap = UdeskUtil.getVideoThumbnail(info.getMsgContent());
                    if (bitmap != null) {
                        UdeskUtils.saveBitmap(UdeskChatActivity.this.getApplicationContext(), info.getMsgContent(), bitmap);
                        Message message = getHandler().obtainMessage(
                                MessageWhat.ChangeVideoThumbnail);
                        message.obj = info.getMsgId();
                        getHandler().sendMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    public void pullByJumpOrder(int seqNUm, String imSessionId) {
        if (mPresenter != null) {
            mPresenter.pullMessages(seqNUm, imSessionId);
        }
    }

    public void sendVideoMessage() {

        try {
            SDKIMSetting sdkimSetting = UdeskSDKManager.getInstance().getImSetting();
            if (sdkimSetting != null && mAgentInfo != null && isOpenVideo()) {
                //分配到客服后。建立websocket连接
                String domain = UdeskSDKManager.getInstance().getDomain(getApplicationContext());
                String[] domains = domain.split("\\.");
                if (domains.length > 0) {
                    domain = domains[0];
                }
                if (!TextUtils.isEmpty(mAgentInfo.getIm_sub_session_id())) {
                    UdeskConst.IMBusseniessId = mAgentInfo.getIm_sub_session_id();
                }
                if (!TextUtils.isEmpty(mAgentInfo.getAgentJid())) {
                    UdeskConst.IMAgentJid = mAgentInfo.getAgentJid();
                }
                if (!TextUtils.isEmpty(mAgentInfo.getAgentNick())) {
                    UdeskConst.IMAgentName = mAgentInfo.getAgentNick();
                }
                if (!TextUtils.isEmpty(XmppInfo.getInstance().getLoginName())) {
                    UdeskConst.IMCustomerJid = XmppInfo.getInstance().getLoginName();
                }
                if (!TextUtils.isEmpty(sdkimSetting.getVc_app_id())) {
                    UdeskConst.vc_app_id = sdkimSetting.getVc_app_id();
                }
                if (!TextUtils.isEmpty(sdkimSetting.getAgora_app_id())) {
                    UdeskConst.agora_app_id = sdkimSetting.getAgora_app_id();
                }
                if (!TextUtils.isEmpty(sdkimSetting.getServer_url())) {
                    UdeskConst.server_url = sdkimSetting.getServer_url();
                }

                if (!TextUtils.isEmpty(sdkimSetting.getVcall_token_url())) {
                    UdeskConst.signToenUrl = sdkimSetting.getVcall_token_url();
                }
                if (!TextUtils.isEmpty(domain)) {
                    UdeskConst.Subdomain = domain;
                }

                InvokeEventContainer.getInstance().event_OnConnectWebsocket.invoke(getApplicationContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ChatActivityPresenter getmPresenter() {
        return mPresenter;
    }

    private void addNavigationFragment() {
        try {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_view, new NavigationFragment());
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {

        try {
            UdeskBaseInfo.isNeedMsgNotice = true;
            if (mPresenter != null) {
                mPresenter.unbindReqsurveyMsg();
            }
            //失去焦点 就启动
            MessageCache.getInstance().putAll(mPresenter.getSendingMsgCache(), getApplicationContext());
            if (isFinishing()) {
                cleanSource();
            } else {
                UdeskConst.sdk_page_status = UdeskConst.SDK_PAGE_BACKGROUND;
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

    private static class MyUpdateMsgRead extends Thread {

        @Override
        public void run() {
            try {
                UdeskDBManager.getInstance().updateAllMsgRead();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                View v = getCurrentFocus();
                if (isShouldHideKeyboard(v, ev)) {
                    hideKeyboard(v.getWindowToken());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard(IBinder token) {
        try {
            if (token != null) {
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        try {
            if (v != null && (v instanceof EditText)) {
                int[] l = {0, 0};
                v.getLocationInWindow(l);
                int left = l[0],
                        top = l[1],
                        bottom = top + v.getHeight(),
                        right = left + v.getWidth();
                if (event.getY() > top) {
                    // 点击EditText的事件，忽略它。
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    @Override
    public boolean isNeedQueueMessageSave() {
        return isInTheQueue && UdeskSDKManager.getInstance().getEnableSendMessageWhenQueue();
    }

    String moreThanStirng;

    @Override
    public void isMoreThan(boolean isMore, String msg) {

        try {
            isMoreThan20 = isMore;
            moreThanStirng = msg;
            this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    UdeskUtils.showToast(UdeskChatActivity.this, getMoreThanSendTip());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isMoreTanSendCount() {
        return isMoreThan20;
    }

    public String getMoreThanSendTip() {

        return TextUtils.isEmpty(moreThanStirng) ? getResources().getString(R.string.udesk_in_the_line_max_send) : moreThanStirng;
    }

    private void cleanSource() {
        if (isDestroyed) {
            return;
        }
        // 回收资源
        UdeskConst.sdk_page_status = UdeskConst.SDK_PAGE_FINISH;
        isDestroyed = true;
        try {
            if (popWindow != null && popWindow.isShowing()) {
                popWindow.dismiss();
            }
            XPermissionUtils.destory();
            functionItems.clear();
            new MyUpdateMsgRead().start();
            recycleVoiceRes();
            if (mPresenter != null) {
                mPresenter.clearMsg();
                mPresenter.quitQuenu(UdeskSDKManager.getInstance().getUdeskConfig().UdeskQuenuMode);
                mPresenter.unBind();
                mPresenter.removeCallBack();
                mPresenter = null;
            }
            if (mHandler != null && myRunnable != null) {
                mHandler.removeCallbacks(myRunnable);
            }
            UdeskDBManager.getInstance().updateSendFlagToFail();
            //设置了开启推送标识，离开会话界面开启推送，
            if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getRegisterId(UdeskChatActivity.this)) && UdeskSDKManager.getInstance().getUdeskConfig().isUserSDkPush) {
                UdeskSDKManager.getInstance().setSdkPushStatus(UdeskSDKManager.getInstance().getDomain(this),
                        UdeskSDKManager.getInstance().getAppkey(this), UdeskSDKManager.getInstance().getSdkToken(getApplicationContext()), UdeskConfig.UdeskPushFlag.ON,
                        UdeskSDKManager.getInstance().getRegisterId(UdeskChatActivity.this), UdeskSDKManager.getInstance().getAppId(this));
            }
            if (mEmotionKeyboard != null) {
                mEmotionKeyboard.destory();
                mEmotionKeyboard = null;
            }
            if (queueItem != null) {
                queueItem = null;
            }
            unRegister();
            UdeskHttpFacade.getInstance().cancel();
            InvokeEventContainer.getInstance().event_IsOver.unBind(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
