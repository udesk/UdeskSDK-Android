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
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import cn.udesk.JsonUtils;
import cn.udesk.PreferenceHelper;
import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.aac.MergeMode;
import cn.udesk.aac.MergeModeManager;
import cn.udesk.aac.QuestionMergeMode;
import cn.udesk.aac.UdeskViewMode;
import cn.udesk.adapter.MessageAdatper;
import cn.udesk.adapter.TipAdapter;
import cn.udesk.callback.IUdeskHasSurvyCallBack;
import cn.udesk.camera.UdeskCameraActivity;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.config.UdeskConfigUtil;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.emotion.IEmotionSelectedListener;
import cn.udesk.emotion.LQREmotionKit;
import cn.udesk.fragment.UdeskAgentFragment;
import cn.udesk.fragment.UdeskRobotFragment;
import cn.udesk.fragment.UdeskbaseFragment;
import cn.udesk.itemview.BaseViewHolder;
import cn.udesk.messagemanager.UdeskXmppManager;
import cn.udesk.model.AgentGroupNode;
import cn.udesk.model.Customer;
import cn.udesk.model.ImSetting;
import cn.udesk.model.InitCustomerBean;
import cn.udesk.model.Robot;
import cn.udesk.model.SurveyOptionsModel;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.model.UdeskQueueItem;
import cn.udesk.permission.RequestCode;
import cn.udesk.permission.XPermissionUtils;
import cn.udesk.photoselect.PhotoSelectorActivity;
import cn.udesk.photoselect.entity.LocalMedia;
import cn.udesk.voice.RecordFilePlay;
import cn.udesk.voice.RecordPlay;
import cn.udesk.voice.RecordPlayCallback;
import cn.udesk.widget.RecycleViewDivider;
import cn.udesk.widget.UDPullGetMoreListView;
import cn.udesk.widget.UdeskConfirmPopWindow;
import cn.udesk.widget.UdeskConfirmPopWindow.OnPopConfirmClick;
import cn.udesk.widget.UdeskExpandableLayout;
import cn.udesk.widget.UdeskMultiMenuHorizontalWindow;
import cn.udesk.widget.UdeskMultiMenuHorizontalWindow.OnPopMultiMenuClick;
import cn.udesk.widget.UdeskSurvyPopwindow;
import cn.udesk.widget.UdeskTitleBar;
import udesk.core.JsonObjectUtils;
import udesk.core.UdeskConst;
import udesk.core.event.InvokeEventContainer;
import udesk.core.model.AgentInfo;
import udesk.core.model.AllMessageMode;
import udesk.core.model.Content;
import udesk.core.model.DataBean;
import udesk.core.model.LogBean;
import udesk.core.model.MessageInfo;
import udesk.core.model.Product;
import udesk.core.model.ProductListBean;
import udesk.core.model.RobotInit;
import udesk.core.model.RobotTipBean;
import udesk.core.utils.UdeskIdBuild;
import udesk.core.utils.UdeskUtils;


public class UdeskChatActivity extends UdeskBaseActivity implements IEmotionSelectedListener {
    public LinearLayout mContentLinearLayout;//消息内容区域
    public UDPullGetMoreListView mListView;
    private MessageAdatper mChatAdapter;
    private UdeskConfirmPopWindow popWindow = null;
    private UdeskExpandableLayout expandableLayout = null;  //动画显示上线离线提醒的控件
    private UdeskTitleBar mTitlebar;
    public RecordFilePlay mRecordFilePlay;
    private RecordPlayCallback mPlayCallback;
    public AgentInfo mAgentInfo;  // 保存客服信息的实例
    private MessageInfo redirectMsg;
    private Uri photoUri;
    private File cameraFile;

    private String groupId = "";
    private String agentId = "";
    private String menuId = "";
    public String isblocked = "";
    private String blockedNotice = "";

    // 标记当前是否有客服在线，客服不在线状态是不能发送消息的，
    public boolean currentStatusIsOnline = false;
    private boolean isNeedStartExpandabLyout = false;
    private boolean isNeedRelogin = false;
    private boolean hasSendCommodity = false;
    private boolean hasSendFirstMessage = false;


    private int historyCount = 0; // 记录数据库中总的记录数
    private int offset = -1; // 标记偏移值
    public static final int InitViewMode = 1; //初始化
    public static final int PullRefreshModel = 2; //下拉刷新
    public static final int PullEventModel = 3; //拉去工单回复
    private int currentMode = InitViewMode;

    private long QUEUE_RETEY_TIME = 20 * 1000; // 客服繁忙时  轮询的间隔时间
    private long IM_LEAVE_MSG_INTERVAL = 2 * 60 * 1000; // 对话留言时，请求客服的间隔时间
    private final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 101;
    private final int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = 102;
    private final int SELECT_FILE_OPTION_REQUEST_CODE = 104;
    private final int SELECT_LOCATION_OPTION_REQUEST_CODE = 105;
    private final int SELECT_UDESK_IMAGE_ACTIVITY_REQUEST_CODE = 106;
    private final int CAPTURE_IMAGE_SMALLVIDEO_ACTIVITY_REQUEST_CODE = 107;
    private final int IM_GROUP_REQUEST_CODE = 108;

    private MyHandler mHandler;
    private BroadcastReceiver mConnectivityChangedReceiver = null;
    private boolean isSurvyOperate = false;//如果是收到客服的满意度调查，则在onresume 处不在请求分配客服
//    public boolean isInitComplete = false; //标识进入请求分配客服的流程是否结束


    private boolean isFirstFinish = true;//第一返回,如果设置了弹出满意度评价则弹出,无论评价与否,第二次

    private boolean isPermitSurvy = true;
    private boolean isNeedOpenLocalCamera = false;
    private boolean isDestroyed = false; //表示是否执行ondestory

    public String pre_session_id = ""; //无消息对话状态下id

    //咨询对象的展示界面
    private View commodityView;
    public ImageView commodityThumbnail;
    public TextView commodityTitle;
    public TextView commoditySubTitle;
    public TextView commodityLink;

    //排队中的消息提醒
    private UdeskQueueItem queueItem;
    //    private boolean isInTheQueue = false;
    //最多允许发送20条消息
    public boolean isMoreThan20 = false;
    private UdeskbaseFragment fragment;
    private String leavMsgId = "";

    UdeskViewMode udeskViewMode;
    String customerId = "";
    String moreThanStirng;
    //输入联想功能
    private RecyclerView mRvAssociate;
    private LinearLayout mLlAssociate;
    private int robotSengMsgCount = 0;

    public InitCustomerBean initCustomer;
    public ImSetting imSetting;
    Robot robot;
    public String curentStatus = UdeskConst.Status.init;
    private TipAdapter tipAdapter;
    //机器人初始化返回数据
    private RobotInit robotInit;
    private String moreMarking = "";//拉取服务器消息的标识
    private boolean isShowNet = false;
    private Map<String, Boolean> usefulMap = new ConcurrentHashMap<>();//有用
    private Map<String, Boolean> transferMap = new ConcurrentHashMap<>();//转人工
    private List<ProductListBean> randomList = new ArrayList<>();
    private UdeskSurvyPopwindow udeskSurvyPopwindow;
    List<MessageInfo> imLeaveMsgCache = new ArrayList<>();
    private boolean isExit = true;
    private RelativeLayout commodityRoot;
    private MessageInfo robotTransferMsg;
    private boolean isInAgentFragment;
    private String preSendRobotMessages;
    private Object connectVideoWebSocket;

    public static class MyHandler extends Handler {
        WeakReference<UdeskChatActivity> mWeakActivity;

        public MyHandler(UdeskChatActivity activity) {
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                UdeskChatActivity udeskChatActivity = mWeakActivity.get();
                if (udeskChatActivity == null) {
                    return;
                }
                switch (msg.what) {
                    case UdeskConst.LiveDataType.UpLoadFileLiveData_progress:
                        MessageInfo progressMsg = (MessageInfo) msg.obj;
                        udeskChatActivity.changeFileProgress(progressMsg.getMsgId(), progressMsg.getPrecent(), 0, true);
                        break;

                    default:
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class ConnectivtyChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent
                        .getAction())) {
                    return;
                }
                boolean netWorkAvailable = UdeskUtils.isNetworkConnected(context);
                if (netWorkAvailable) {
                    if (isNeedRelogin) {
                        if (isblocked.equals("true")) {
                            return;
                        }
                        if (isShowNet) {
                            expandableLayout.stopAnimation();
                        }
                        udeskViewMode.getApiLiveData().initCustomer(getApplicationContext());
                    }
                } else {
                    isNeedRelogin = true;
                    UdeskUtils.showToast(getApplicationContext(), context.getResources().getString(R.string.udesk_has_wrong_net));
                    setTitlebar(context.getResources().getString(
                            R.string.udesk_agent_connecting_error_net_uavailabl), "off");
                    currentStatusIsOnline = false;
                    initfunctionItems();
                    expandableLayout.startNetAnimation();
                    isShowNet = true;
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
            mHandler = new MyHandler(this);
            initUdeskViewMode();
            UdeskUtils.resetTime();
            UdeskUtil.setOrientation(this);
            setContentView(R.layout.udesk_activity_im);
            initView();
            bind();
            initGroupIdAndAgentID();
            settingTitlebar();
            initCustomer = UdeskSDKManager.getInstance().getInitCustomerBean();
            if (initCustomer == null || initCustomer.getCode() != 1000) {
                udeskViewMode.getApiLiveData().initCustomer(getApplicationContext());
            } else {
                doWithInitCustomer();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    private void bind() {
        InvokeEventContainer.getInstance().event_OnVideoEventReceived.bind(this, "onVideoEvent");
        InvokeEventContainer.getInstance().event_OnMessageReceived.bind(this, "onMessageReceived");
        InvokeEventContainer.getInstance().event_OnNewPresence.bind(this, "onNewPresence");
        InvokeEventContainer.getInstance().event_OnReqsurveyMsg.bind(this, "onReqsurveyMsg");
        InvokeEventContainer.getInstance().event_OnActionMsg.bind(this, "onActionMsg");
        InvokeEventContainer.getInstance().eventui_OnNewMessage.bind(this, "onNewMessage");
    }

    //在指定客服组ID  或者指定客服ID  会传入值  其它的方式进入不会传值
    private void initGroupIdAndAgentID() {
        try {
            if (getIntent() != null) {
                menuId = getIntent().getStringExtra(UdeskConst.UDESKMENUID);
                if (!TextUtils.isEmpty(menuId)) {
                    PreferenceHelper.write(getApplicationContext(), UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                            UdeskConst.SharePreParams.Udesk_Menu_Id, menuId);
                }
            }
            groupId = UdeskSDKManager.getInstance().getUdeskConfig().groupId;
            agentId = UdeskSDKManager.getInstance().getUdeskConfig().agentId;
            PreferenceHelper.write(getApplicationContext(), UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                    UdeskConst.SharePreParams.Udesk_Group_Id, groupId);
            if (!TextUtils.isEmpty(agentId)) {
                PreferenceHelper.write(getApplicationContext(), UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                        UdeskConst.SharePreParams.Udesk_Agent_Id, agentId);
            }
            udeskViewMode.getApiLiveData().setSpecifyAgentID(getAgentId());
            udeskViewMode.getApiLiveData().setSpecifyGroupId(getGroupId());
            udeskViewMode.getApiLiveData().setMenu_id(getMenuId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String on = "on";
    private String off = "off";
    private String queue = "queue";

    private void doWithInitCustomer() {
        try {
            //获取历史记录
            if (initCustomer == null || initCustomer.getCode() != 1000) {
                //失败  结束流程， 走留言提示
                showFailToast("");
                curentStatus = UdeskConst.Status.failure;
                initFragment(UdeskConst.CurrentFragment.agent);
                setTitlebar(getString(R.string.udesk_label_customer_offline), off);
                return;
            }
            Customer customer = initCustomer.getCustomer();
            if (isBlocked(customer)){
                commodityView.setVisibility(View.GONE);
                mListView.setVisibility(View.GONE);
                return;
            }
            if (UdeskSDKManager.getInstance().getUdeskConfig().commodity != null) {
                showCommodity(UdeskSDKManager.getInstance().getUdeskConfig().commodity);
            }
            mListView.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(initCustomer.getUploadService().getReferer())) {
                UdeskConst.REFERER_VALUE = initCustomer.getUploadService().getReferer();
            }
            curentStatus = initCustomer.getStatus();
            if (!TextUtils.equals(curentStatus, UdeskConst.Status.chatting)) {
                udeskViewMode.getApiLiveData().messages("", UdeskConst.PullMsgFrom.init);
            }
            customerId = customer != null ? customer.getId() : "";
            if (!TextUtils.isEmpty(customerId)) {
                udeskViewMode.setCustomerId(customerId);
            }
            UdeskXmppManager.getInstance().connection();
            imSetting = initCustomer.getImSetting();
            if (curentStatus.equals(UdeskConst.Status.init)) {
                //机器人判断
                robot = imSetting != null ? imSetting.getRobot() : null;
                if (robot != null && robot.getEnable()) {
                    //进入机器人对话界面
                    udeskViewMode.setRobotUrl(UdeskUtils.objectToString(robot.getUrl()));
                    initFragment(UdeskConst.CurrentFragment.robot);
                    setTitlebar(robot.getRobot_name(), on);
                    showTranferAgent();
                    return;
                }
            }
            initFragment(UdeskConst.CurrentFragment.agent);
            if (curentStatus.equals(UdeskConst.Status.chatting)) {
                //会话中 直接请求分配客服
                udeskViewMode.getApiLiveData().getAgentInfo(null, null);
            } else if (curentStatus.equals(UdeskConst.Status.pre_session)) {
                initAgentInfo();
            } else {
                //如果客户被加入黑名单 则结束后续流程
                if (!isBlocked(customer)) {
                    initAgentInfo();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initAgentInfo() {
        try {
            pre_session_id = initCustomer.getPre_session().getPre_session_id();
            if (getPressionStatus()) {
                String preTitle = initCustomer.getPre_session().getPre_session_title();
                if (TextUtils.isEmpty(pre_session_id)) {
                    udeskViewMode.getApiLiveData().getPressionInfo();
                }
                curentStatus = UdeskConst.Status.pre_session;
                mAgentInfo = null;
                setTitlebar(preTitle, off);
            } else {
                udeskViewMode.getApiLiveData().getAgentInfo(pre_session_id, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isWorkTime() {
        boolean isWorkTime = imSetting != null ? imSetting.getIs_worktime() : true;
        return isWorkTime;
    }

    private void showTranferAgent() {
        try {
            if (mTitlebar != null) {
                if (robot != null && robot.getEnable_agent() && curentStatus.equals(UdeskConst.Status.robot) && !UdeskSDKManager.getInstance().getUdeskConfig().isOnlyUseRobot) {
                    if (robotSengMsgCount >= robot.getShow_robot_times()) {
                        mTitlebar.setRightViewVis(View.VISIBLE);
                    } else {
                        mTitlebar.setRightViewVis(View.GONE);
                    }
                } else {
                    mTitlebar.setRightViewVis(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initUdeskViewMode() {
        try {
            udeskViewMode = ViewModelProviders.of(this).get(UdeskViewMode.class);
            udeskViewMode.setBaseValue(
                    UdeskSDKManager.getInstance().getDomain(getApplicationContext()),
                    UdeskSDKManager.getInstance().getAppkey(getApplicationContext()),
                    UdeskSDKManager.getInstance().getSdkToken(getApplicationContext()),
                    UdeskSDKManager.getInstance().getAppId(getApplicationContext())
            );
            udeskViewMode.setHandler(mHandler);
            udeskViewMode.getLiveDataMerger().observeForever(new Observer<MergeMode>() {
                @Override
                public void onChanged(@Nullable MergeMode mergeMode) {
                    Log.i("xxxxx", " type = " + mergeMode.getType());
                    udeskViewMode.postNextMessage(mergeMode);
                    switch (mergeMode.getType()) {
                        case UdeskConst.LiveDataType.CustomerInitSuccess:
                            initCustomer = (InitCustomerBean) mergeMode.getData();
                            UdeskSDKManager.getInstance().setInitCustomerBean(initCustomer);
                            doWithInitCustomer();
                            break;
                        case UdeskConst.LiveDataType.CustomerInitFailure:
                            showFailToast("");
                            curentStatus = UdeskConst.Status.failure;
                            initFragment(UdeskConst.CurrentFragment.agent);
                            break;
                        case UdeskConst.LiveDataType.SetPreSessionStatus:
                            pre_session_id = UdeskUtils.objectToString(mergeMode.getData());
                            break;
                        //收到通过xmpp发送过来的消息
                        case UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmppMessage:
                            MessageInfo receiveMsg = (MessageInfo) mergeMode.getData();
                            dealReceiveMsg(receiveMsg);
                            break;
                        //收到发送给客服的消息 客服给的回执
                        case UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmppMessageReceived:
                            String successMsgId = UdeskUtils.objectToString(mergeMode.getData());
                            if (!TextUtils.isEmpty(successMsgId)) {
                                udeskViewMode.getSendMessageLiveData().removeSendMsgCace(successMsgId);
                                changeImState(successMsgId, UdeskConst.SendFlag.RESULT_SUCCESS);
                            }
                            break;
                        //收到客服状态的presence消息
                        case UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmmpPresence:
                            Map<String, Object> hashMap = (Map<String, Object>) mergeMode.getData();
                            dealReceivePresence(hashMap);
                            break;
                        //收到满意度调查的消息
                        //弹出评价选项
//                        case UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmmpSurvey:
//                            dealReceiveSurvey();
//                            break;
                        //收到客服在工单处回复SDK的消息
                        case UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmmpTicketReplay:
                            if (!TextUtils.isEmpty(customerId)) {
                                udeskViewMode.getApiLiveData().getTicketReplies(1, UdeskConst.UDESK_HISTORY_COUNT);
                            }
                            break;
                        //收到结束会话通知
                        case UdeskConst.LiveDataType.XmppReceiveLivaData_ReceiveXmmpOver:
                            curentStatus = UdeskConst.Status.over;
                            setTitlebar(getResources().getString(R.string.udesk_close_chart), off);
                            isMoreThan20 = false;
                            UdeskSDKManager.getInstance().disConnectXmpp();
                            currentStatusIsOnline = false;
                            initfunctionItems();
                            break;
                        // 发送消息增加到页面上，不break，共用收到消息刷新?
                        case UdeskConst.LiveDataType.AddMessage:
                            MessageInfo sendMessage = (MessageInfo) mergeMode.getData();
                            if (sendMessage == null) {
                                return;
                            }
                            if (curentStatus.equals(UdeskConst.Status.robot)) {
                                sendMessage.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
                                udeskViewMode.getRobotApiData().robotMessage(sendMessage);
                                mChatAdapter.addItem(sendMessage);
                                mListView.smoothScrollToPosition(mChatAdapter.getCount());
                                udeskViewMode.getDbLiveData().saveMessageDB(sendMessage);
                                robotSengMsgCount++;
                                showTranferAgent();
                                return;
                            }
                            if (TextUtils.equals(sendMessage.getMsgtype(), UdeskConst.ChatMsgTypeString.TYPE_IMAGE)
                                    || TextUtils.equals(sendMessage.getMsgtype(), UdeskConst.ChatMsgTypeString.TYPE_File)
                                    || TextUtils.equals(sendMessage.getMsgtype(), UdeskConst.ChatMsgTypeString.TYPE_VIDEO)
                                    || TextUtils.equals(sendMessage.getMsgtype(), UdeskConst.ChatMsgTypeString.TYPE_SHORT_VIDEO)) {
                                changeFileProgress(sendMessage.getMsgId(), 100, 0, true);
                            }
                            if (mAgentInfo != null && mAgentInfo.getAgentCode() == 2000) {
                                sendMessage.setmAgentJid(mAgentInfo.getAgentJid());
                                sendMessage.setSubsessionid(mAgentInfo.getIm_sub_session_id());
                                sendMessage.setSeqNum(UdeskDBManager.getInstance().getSubSessionId(mAgentInfo.getIm_sub_session_id()));
                            }
                            if (!TextUtils.isEmpty(customerId)) {
                                sendMessage.setCustomerId(customerId);
                            }
                            if (getPressionStatus()) {
                                JSONObject preMessage = JsonObjectUtils.buildPreSessionInfo(sendMessage.getMsgtype(),
                                        sendMessage.getMsgContent(), sendMessage.getMsgId(),
                                        sendMessage.getDuration(), pre_session_id, sendMessage.getFilename(),
                                        sendMessage.getFilesize());
                                udeskViewMode.getApiLiveData().getAgentInfo(pre_session_id, preMessage);
                                udeskViewMode.addPressionMsg(sendMessage);
                                return;
                            }
                            mChatAdapter.addItem(sendMessage);
                            mListView.smoothScrollToPosition(mChatAdapter.getCount());
                            udeskViewMode.getDbLiveData().saveMessageDB(sendMessage);
                            if (isNeedQueueMessageSave()) {
                                udeskViewMode.getSendMessageLiveData().sendQueueMessage(sendMessage);
                                return;
                            }
                            udeskViewMode.getSendMessageLiveData().sendMessage(sendMessage);
                            break;
                        //增加直接离线留言消息
                        case UdeskConst.LiveDataType.AddLeaveMsg:
                            MessageInfo leaveMsg = (MessageInfo) mergeMode.getData();
                            if (leaveMsg == null) {
                                return;
                            }
                            mChatAdapter.addItem(leaveMsg);
                            mListView.smoothScrollToPosition(mChatAdapter.getCount());
                            udeskViewMode.getDbLiveData().saveMessageDB(leaveMsg);
                            udeskViewMode.putLeavesMsg(leaveMsg);
                            break;
                        //增加对话留言消息
                        case UdeskConst.LiveDataType.AddIMLeaveMsg:
                            MessageInfo imLeaveMsg = (MessageInfo) mergeMode.getData();
                            if (imLeaveMsg == null) {
                                return;
                            }
                            imLeaveMsgCache.add(imLeaveMsg);
                            sendIMLeaveMsg(imLeaveMsgCache);
                            break;

                        //检查本地收到客服消息是否有跳序
                        case UdeskConst.LiveDataType.Check_Agent_Seq_Num:
                            int agent_seq_num = (int) mergeMode.getData();
                            if (getAgentSeqNum() != 0 && agent_seq_num > getAgentSeqNum()) {
                                udeskViewMode.getApiLiveData().messages("", UdeskConst.PullMsgFrom.jump);
                            }
                            break;
                        //消息发送失败
                        case UdeskConst.LiveDataType.Send_Message_Failure:
                        case UdeskConst.LiveDataType.RobotHitFailure:
                        case UdeskConst.LiveDataType.RobotMessageFailure:
                            String failureMsgId = UdeskUtils.objectToString(mergeMode.getData());
                            changeImState(failureMsgId, UdeskConst.SendFlag.RESULT_FAIL);
                            break;
                        //messagesave 返回code 8002 需要重新请求流程
                        case UdeskConst.LiveDataType.RECREATE_CUSTOMER_INFO:
                            reCreateIMCustomerInfo();
                            break;
                        //排队中发送消息的错误提示
                        case UdeskConst.LiveDataType.QueueMessageSaveError:
                            moreThanStirng = UdeskUtils.objectToString(mergeMode.getData());
                            isMoreThan20 = true;
                            UdeskUtils.showToast(getApplicationContext(), getMoreThanSendTip());
                            break;
                        //给出评价成功与否的提示
                        case UdeskConst.LiveDataType.Survey_Result:
                            boolean isSuccess = (boolean) mergeMode.getData();
                            setIsPermitSurvy(true);
                            if (isSuccess) {
                                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_thanks_survy));
                            } else {
                                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_survey_error));
                            }
                            break;
                        //给出评价成功与否的提示
                        case UdeskConst.LiveDataType.ROBOT_SURVEY_RESULT:
                            if ((boolean) mergeMode.getData()) {
                                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_thanks_survy));
                            } else {
                                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_survey_error));
                            }
                            break;

                        case UdeskConst.LiveDataType.click_Survey:
                            if (initCustomer != null && initCustomer.getIm_survey() != null) {
                                SurveyOptionsModel surveyOptions = initCustomer.getIm_survey();
                                setIsPermitSurvy(true);
                                if (surveyOptions != null) {
                                    toLaunchSurveyView(surveyOptions);
                                }
                            } else {
                                udeskViewMode.getApiLiveData().getIMSurveyOptions(null);
                            }
                            break;
                        case UdeskConst.LiveDataType.finsh_Survey:
                            if (initCustomer != null && initCustomer.getIm_survey() != null) {
                                SurveyOptionsModel surveyOptions = initCustomer.getIm_survey();
                                setIsPermitSurvy(true);
                                if (surveyOptions != null) {
                                    toLaunchSurveyView(surveyOptions);
                                }
                            } else {
                                finish();
                            }
                            break;

                        case UdeskConst.LiveDataType.toLaunchSurveyView:
                            SurveyOptionsModel surveyOptions = (SurveyOptionsModel) mergeMode.getData();
                            setIsPermitSurvy(true);
                            if (surveyOptions != null) {
                                toLaunchSurveyView(surveyOptions);
                            }
                        case UdeskConst.LiveDataType.RobotSessionHasSurvey:
                            if ((boolean) mergeMode.getData()) {
                                UdeskUtils.showToast(getApplicationContext(), getResources()
                                        .getString(R.string.udesk_has_survey));
                            } else {
                                SurveyOptionsModel surveyOptionsModel1 = UdeskUtil.buildSurveyOptionsModel(getApplicationContext());
                                toLaunchSurveyView(surveyOptionsModel1);
                            }
                            break;
                        //给出已经评价的提示
                        case UdeskConst.LiveDataType.HasSurvey:
                            setIsPermitSurvy(true);
                            UdeskUtils.showToast(getApplicationContext(), getResources()
                                    .getString(R.string.udesk_has_survey));
                            break;

                        //请求流程未完成的 失败提示
                        case UdeskConst.LiveDataType.FailEnd:
                            String failMsg = UdeskUtils.objectToString(mergeMode.getData());
                            showFailToast(failMsg);
                            curentStatus = UdeskConst.Status.failure;
                            break;
                        //处理正常请求客服的返回流程
                        case UdeskConst.LiveDataType.DealAgentInfo:
                            AgentInfo agentInfo = (AgentInfo) mergeMode.getData();
                            dealAgentInfo(agentInfo, false);
                            break;
                        //更新客服的是否在线状态
                        case UdeskConst.LiveDataType.IMSTATUS:
                            String imStatus = UdeskUtils.objectToString(mergeMode.getFrom());
                            AgentInfo agent = (AgentInfo) mergeMode.getData();
                            doAgentStatus(imStatus, agent);
                            break;
                        //处理收到转移客服请求的流程
                        case UdeskConst.LiveDataType.RedirectAgentInfo:
                            AgentInfo redirectAgentInfo = (AgentInfo) mergeMode.getData();
                            dealAgentInfo(redirectAgentInfo, true);
                            break;
                        //拉取留言消息接口后刷新listviiew
                        case UdeskConst.LiveDataType.PullEventModel:
                            AllMessageMode replieMode = (AllMessageMode) mergeMode.getData();
                            if (replieMode != null && replieMode.getMessages() != null && replieMode.getMessages().size() > 0) {
                                List<MessageInfo> messageInfoList = new ArrayList<>();
                                for (int i = 0; i < replieMode.getMessages().size(); i++) {
                                    LogBean allMessage = (LogBean) replieMode.getMessages().get(i);
                                    MessageInfo info = UdeskUtil.buildAllMessage(allMessage);
                                    if (info != null) {
                                        info.setSwitchStaffType(0);
                                        messageInfoList.add(info);
                                    }
                                }
                                currentMode = PullEventModel;
                                udeskViewMode.getDbLiveData().addAllMessageInfo(messageInfoList);
                                reFreshMessages(messageInfoList);
                            }
                            break;
                        //增加一条文件类型的消息(包含语言 图片 等)
                        case UdeskConst.LiveDataType.AddFileMessage:
                            MessageInfo fileMessage = (MessageInfo) mergeMode.getData();
                            if (fileMessage == null) {
                                return;
                            }
                            if (mAgentInfo != null && mAgentInfo.getAgentCode() == 2000) {
                                fileMessage.setmAgentJid(mAgentInfo.getAgentJid());
                                fileMessage.setSubsessionid(mAgentInfo.getIm_sub_session_id());
                                fileMessage.setSeqNum(UdeskDBManager.getInstance().getSubSessionId(mAgentInfo.getIm_sub_session_id()));
                            }
                            if (!TextUtils.isEmpty(customerId)) {
                                fileMessage.setCustomerId(customerId);
                            }
                            if (getPressionStatus()) {
                                JSONObject preMessage = JsonObjectUtils.buildPreSessionInfo(fileMessage.getMsgtype(),
                                        fileMessage.getMsgContent(), fileMessage.getMsgId(),
                                        fileMessage.getDuration(), pre_session_id, fileMessage.getFilename(),
                                        fileMessage.getFilesize());
                                udeskViewMode.getApiLiveData().getAgentInfo(pre_session_id, preMessage);
                                udeskViewMode.addPressionMsg(fileMessage);
                                return;
                            }
                            mChatAdapter.addItem(fileMessage);
                            mListView.smoothScrollToPosition(mChatAdapter.getCount());
                            udeskViewMode.getDbLiveData().saveMessageDB(fileMessage);
                            udeskViewMode.getFileLiveData().upLoadFile(UdeskChatActivity.this.getApplicationContext(), fileMessage);
                            break;
                        //更新文件上传进度
                        case UdeskConst.LiveDataType.UpLoadFileLiveData_progress:
                            MessageInfo progressMsg = (MessageInfo) mergeMode.getData();
                            changeFileProgress(progressMsg.getMsgId(), progressMsg.getPrecent(), 0, true);
                            break;
                        //文件下载失败
                        case UdeskConst.LiveDataType.DownFileError:
                            String msgId = UdeskUtils.objectToString(mergeMode.getData());
                            changeFileProgress(msgId, 0, 0, false);
                            break;
                        //获取到拉取远程数据处理完的通知
//                        case UdeskConst.LiveDataType.PullMessages:
//                            initLoadData();
//                            break;
                        //加载本地数据库
                        case UdeskConst.LiveDataType.LoadHistoryDBMsg:
                            List<MessageInfo> msgs = (List<MessageInfo>) mergeMode.getData();
//                            for (MessageInfo messageInfo : msgs) {
//                                messageInfo.setFirstLoad(true);
//                            }
                            reFreshMessages(msgs);
                            break;
                        //刷新小视频第一帧图
                        case UdeskConst.LiveDataType.ChangeVideoThumbnail:
                            String videoMsgId = UdeskUtils.objectToString(mergeMode.getData());
                            changeVideoThumbnail(videoMsgId);
                            break;
                        //机器人初始化成功
                        case UdeskConst.LiveDataType.RobotInitSuccess:
                            String robotInitMsg = UdeskUtils.objectToString(mergeMode.getData());
                            robotInit = JsonUtils.parseRobotInit(robotInitMsg);
                            if (robotInit != null && robotInit.getWebConfig() != null) {
                                List<MessageInfo> infoList = new ArrayList<>();
                                MessageInfo welcomInfo = UdeskUtil.buildWelcomeRelpy(robotInit);
                                if ((robotInit.getTopAsk() != null && robotInit.getTopAsk().size() > 0)
                                        || !TextUtils.isEmpty(robotInit.getWebConfig().getLeadingWord())) {
                                    MessageInfo questionInfo = UdeskUtil.buildRobotInitRelpy(robotInit);
                                    if (UdeskUtils.objectToInt(robotInit.getSwitchStaffType()) == 2) {
                                        welcomInfo.setSwitchStaffType(0);
                                        questionInfo.setSwitchStaffType(0);
                                        infoList.add(welcomInfo);
                                        infoList.add(questionInfo);
                                        mChatAdapter.listAddEventItems(infoList);
                                        mListView.smoothScrollToPosition(mChatAdapter.getCount());
                                        udeskViewMode.setSessionId(robotInit.getSessionId());
                                        autoTransfer();
                                    } else {
                                        infoList.add(welcomInfo);
                                        infoList.add(questionInfo);
                                        mChatAdapter.listAddEventItems(infoList);
                                        mListView.smoothScrollToPosition(mChatAdapter.getCount());
                                        udeskViewMode.setSessionId(robotInit.getSessionId());
                                    }
                                } else {
                                    if (UdeskUtils.objectToInt(robotInit.getSwitchStaffType()) == 2) {
                                        welcomInfo.setSwitchStaffType(0);
                                        infoList.add(welcomInfo);
                                        mChatAdapter.listAddEventItems(infoList);
                                        mListView.smoothScrollToPosition(mChatAdapter.getCount());
                                        udeskViewMode.setSessionId(robotInit.getSessionId());
                                        autoTransfer();
                                    } else {
                                        infoList.add(welcomInfo);
                                        mChatAdapter.listAddEventItems(infoList);
                                        mListView.smoothScrollToPosition(mChatAdapter.getCount());
                                        udeskViewMode.setSessionId(robotInit.getSessionId());
                                    }
                                }
                                if (robotInit != null && !TextUtils.isEmpty(preSendRobotMessages.trim())) {
                                    udeskViewMode.setSessionId(robotInit.getSessionId());
                                    udeskViewMode.getRobotApiData().sendTxtMsg(preSendRobotMessages);
                                    preSendRobotMessages = "";
                                }
                            }
                            break;
                        //机器人初始化失败
                        case UdeskConst.LiveDataType.RobotInitFailure:
                            break;
                        //机器人发送文本请求返回成功
                        case UdeskConst.LiveDataType.RobotMessageSuccess:
                            //机器人流程请求返回成功
                        case UdeskConst.LiveDataType.RobotFlowSuccess:
                            //机器人点击请求返回成功 机器人返回消息
                        case UdeskConst.LiveDataType.RobotHitSuccess:
                            String sendMsgId = mergeMode.getFrom();
                            changeImState(sendMsgId, UdeskConst.SendFlag.RESULT_SUCCESS);
                            String hitMessage = UdeskUtils.objectToString(mergeMode.getData());
                            LogBean logBean = JsonUtils.parseLogBean(hitMessage);
                            if (logBean != null) {
                                if (logBean.getContent() != null) {
                                    Content content = logBean.getContent();
                                    if (content.getData() != null) {
                                        DataBean data = content.getData();
                                        if (data.getSwitchStaffType() == 1 && !TextUtils.isEmpty(data.getSwitchStaffTips()) && !UdeskSDKManager.getInstance().getUdeskConfig().isOnlyUseRobot) {
                                            transferMap.put(UdeskUtils.objectToString(logBean.getMessage_id()), true);
                                            usefulMap.put(UdeskUtils.objectToString(logBean.getMessage_id()), true);
                                        }
                                        if (data.getTopAsk() != null && data.getTopAsk().size() > 0 && !TextUtils.isEmpty(data.getContent())) {
                                            usefulMap.put(UdeskUtils.objectToString(logBean.getMessage_id()), true);
                                        }
                                        if (content.getType().equals(UdeskConst.ChatMsgTypeString.TYPE_WECHAT_IMAGE)) {
                                            usefulMap.put(UdeskUtils.objectToString(logBean.getMessage_id()), true);
                                        }
                                    }
                                }
                                MessageInfo info = UdeskUtil.buildAllMessage(logBean);
                                if (info != null) {
                                    udeskViewMode.getDbLiveData().saveMessageDB(info);
                                    mChatAdapter.addItem(info);
                                    mListView.smoothScrollToPosition(mChatAdapter.getCount());
                                    if (info.getSwitchStaffType() == 2) {
                                        autoTransfer();
                                    }
                                }
                            }
                            break;
                        //智能提示返回成功
                        case UdeskConst.LiveDataType.RobotTipsSuccess:
                            String tipMsg = UdeskUtils.objectToString(mergeMode.getData());
                            String inputTxt = ((QuestionMergeMode) mergeMode).getQuestion();
                            RobotTipBean tipBean = JsonUtils.parseRobotTip(tipMsg);
                            if (tipBean != null && tipBean.getList() != null && tipBean.getList().size() > 0) {
                                tipAdapter.setListAndContent(tipBean.getList(), inputTxt);
                                if (TextUtils.isEmpty(fragment.getInputContent())) {
                                    isShowAssociate(false);
                                } else {
                                    isShowAssociate(true);
                                }
                            } else {
                                isShowAssociate(false);
                            }
                            break;
                        //机器人智能提示点击
                        case UdeskConst.LiveDataType.RobotTipHit:
                            RobotTipBean.ListBean listBean = (RobotTipBean.ListBean) mergeMode.getData();
                            isShowAssociate(false);
                            fragment.clearInputContent();
                            if (listBean != null) {
                                MessageInfo sendTipMsg = UdeskUtil.buildSendChildMsg((String) listBean.getQuestion());
                                mChatAdapter.addItem(sendTipMsg);
                                udeskViewMode.getDbLiveData().saveMessageDB(sendTipMsg);
                                mListView.smoothScrollToPosition(mChatAdapter.getCount());
                                robotSengMsgCount++;
                                showTranferAgent();
                                udeskViewMode.getRobotApiData().robotHit(sendTipMsg.getMsgId(), 0, UdeskUtils.objectToString(listBean.getQuestion()), UdeskUtils.objectToInt(listBean.getQuestionId()), ((QuestionMergeMode) mergeMode).getQueryType());
                            }
                            break;
                        //机器人流程点击
                        case UdeskConst.LiveDataType.ROBOT_FLOW_HIT:
                            MessageInfo flowInfo = (MessageInfo) mergeMode.getData();
                            isShowAssociate(false);
                            fragment.clearInputContent();
                            if (flowInfo != null) {
                                MessageInfo sendTipMsg = UdeskUtil.buildSendChildMsg(((QuestionMergeMode) mergeMode).getQuestion());
                                mChatAdapter.addItem(sendTipMsg);
                                udeskViewMode.getDbLiveData().saveMessageDB(sendTipMsg);
                                mListView.smoothScrollToPosition(mChatAdapter.getCount());
                                robotSengMsgCount++;
                                showTranferAgent();
                                udeskViewMode.getRobotApiData().robotFlow(sendTipMsg.getMsgId(), flowInfo.getLogId(), ((QuestionMergeMode) mergeMode).getQuestionId(), ((QuestionMergeMode) mergeMode).getQuestion());
                            }
                            break;

                        //智能提示返回失败
                        case UdeskConst.LiveDataType.RobotTipsFailure:
                            mLlAssociate.setVisibility(View.GONE);
                            break;
                        //问题待推荐点击子条目
                        case UdeskConst.LiveDataType.RobotChildHit:
                            MessageInfo sendChildQueMsg = UdeskUtil.buildSendChildMsg(((QuestionMergeMode) mergeMode).getQuestion());
                            mChatAdapter.addItem(sendChildQueMsg);
                            udeskViewMode.getDbLiveData().saveMessageDB(sendChildQueMsg);
                            mListView.smoothScrollToPosition(mChatAdapter.getCount());
                            robotSengMsgCount++;
                            showTranferAgent();
                            udeskViewMode.getRobotApiData().robotHit(sendChildQueMsg.getMsgId(), ((QuestionMergeMode) mergeMode).getLogId(), ((QuestionMergeMode) mergeMode).getQuestion(), ((QuestionMergeMode) mergeMode).getQuestionId(), ((QuestionMergeMode) mergeMode).getQueryType());
                            break;
                        //机器人发送文本消息
                        case UdeskConst.LiveDataType.ROBOT_SEND_TXT_MSG:
                            MessageInfo txtMsg = UdeskUtil.buildSendChildMsg((UdeskUtils.objectToString(mergeMode.getData())));
                            mChatAdapter.addItem(txtMsg);
                            mListView.smoothScrollToPosition(mChatAdapter.getCount());
                            udeskViewMode.getDbLiveData().saveMessageDB(txtMsg);
                            udeskViewMode.getRobotApiData().robotMessage(txtMsg);
                            robotSengMsgCount++;
                            showTranferAgent();
                            break;

                        case UdeskConst.LiveDataType.ROBOT_TABLE_CLICK:
                            MessageInfo tableMsg = UdeskUtil.buildSendChildMsg((UdeskUtils.objectToString(mergeMode.getData())));
                            mChatAdapter.addItem(tableMsg);
                            mListView.smoothScrollToPosition(mChatAdapter.getCount());
                            udeskViewMode.getDbLiveData().saveMessageDB(tableMsg);
                            udeskViewMode.getRobotApiData().robotMessage(tableMsg);
                            robotSengMsgCount++;
                            showTranferAgent();
                            break;
                        case UdeskConst.LiveDataType.ROBOT_TRANSFER_CLICK:
                            autoTransfer();
                            break;
                        case UdeskConst.LiveDataType.ROBOT_SHOW_PRODUCT_CLICK:
                            ProductListBean productListBean = (ProductListBean) mergeMode.getData();
                            MessageInfo replyProductMsg = UdeskUtil.buildReplyProductMsg(productListBean);
                            mChatAdapter.addItem(replyProductMsg);
                            mListView.smoothScrollToPosition(mChatAdapter.getCount());
                            udeskViewMode.getDbLiveData().saveMessageDB(replyProductMsg);
                            udeskViewMode.getRobotApiData().robotMessage(replyProductMsg);
                            robotSengMsgCount++;
                            showTranferAgent();
                            break;

                        case UdeskConst.LiveDataType.V4PullMessagesSuccess:
                            String loadMsg = UdeskUtils.objectToString(mergeMode.getData());
                            String from = mergeMode.getFrom();
                            AllMessageMode allMessageMode = JsonUtils.parseMessage(loadMsg);
                            if (allMessageMode != null) {
                                List<MessageInfo> messageInfoList = new ArrayList<>();
                                if (allMessageMode.getMessages() != null && allMessageMode.getMessages().size() > 0) {
                                    for (int i = 0; i < allMessageMode.getMessages().size(); i++) {
                                        LogBean allMessage = (LogBean) allMessageMode.getMessages().get(i);
                                        MessageInfo info = UdeskUtil.buildAllMessage(allMessage);
                                        if (info != null) {
                                            messageInfoList.add(info);
                                        }
                                    }
                                    if (TextUtils.equals(from, UdeskConst.PullMsgFrom.init)) {
                                        currentMode = PullEventModel;
                                        udeskViewMode.getDbLiveData().addAllMessageInfo(messageInfoList);
                                        if (robotTransferMsg != null) {
                                            mChatAdapter.removeQueueMessage(robotTransferMsg);
                                        }
                                        reFreshMessages(messageInfoList);
                                        if (TextUtils.isEmpty(moreMarking) && isExit) {
                                            isExit = false;
                                            moreMarking = allMessageMode.getMore_marking();
                                        }
                                    } else if (TextUtils.equals(from, UdeskConst.PullMsgFrom.refresh)) {
                                        reFreshMessages(messageInfoList);
                                        moreMarking = allMessageMode.getMore_marking();
                                    } else if (TextUtils.equals(from, UdeskConst.PullMsgFrom.jump)) {
                                        currentMode = PullEventModel;
                                        udeskViewMode.getDbLiveData().addAllMessageInfo(messageInfoList);
                                        reFreshMessages(messageInfoList);
                                    } else if (TextUtils.equals(from, UdeskConst.PullMsgFrom.hasAgent)) {
                                        udeskViewMode.getDbLiveData().deleteAllMsg();
                                        udeskViewMode.getDbLiveData().addAllMessageInfo(messageInfoList);
                                        if (TextUtils.isEmpty(moreMarking) && isExit) {
                                            isExit = false;
                                            moreMarking = allMessageMode.getMore_marking();
                                        }

                                    }

                                }
                            }
                            break;
                        case UdeskConst.LiveDataType.ROBOT_TRANSFER:
                            robotTransferMsg = UdeskUtil.buildRobotTransferMsg(getResources().getString(R.string.udesk_transfer_person));
                            mChatAdapter.addItem(robotTransferMsg);
                            mListView.smoothScrollToPosition(mChatAdapter.getCount());
//                            udeskViewMode.getDbLiveData().saveMessageDB(robotTransferMsg);
                            break;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendIMLeaveMsg(List<MessageInfo> imLeaveMsgCache) {
        if (imLeaveMsgCache != null && imLeaveMsgCache.size() > 0) {
            for (MessageInfo imLeaveMsg : imLeaveMsgCache) {
                if (imLeaveMsg != null) {
                    mChatAdapter.addItem(imLeaveMsg);
                    mListView.smoothScrollToPosition(mChatAdapter.getCount());
                    udeskViewMode.getDbLiveData().saveMessageDB(imLeaveMsg);
                    udeskViewMode.putIMLeavesMsg(imLeaveMsg, getAgentId(), getGroupId(), getMenuId());
                    fragment.clearInputContent();
                }
            }
            imLeaveMsgCache.clear();
        }
    }

    private void dealReceiveSurvey() {
        if (TextUtils.equals(curentStatus, UdeskConst.Status.robot)) {
            return;
        }
        isSurvyOperate = true;
        if (initCustomer != null && initCustomer.getIm_survey() != null) {
            SurveyOptionsModel surveyOptions = initCustomer.getIm_survey();
            setIsPermitSurvy(true);
            if (surveyOptions != null) {
                toLaunchSurveyView(surveyOptions);
            }
        } else {
            udeskViewMode.getApiLiveData().getIMSurveyOptions(null);
        }
    }

    private void dealReceivePresence(Map<String, Object> hashMap) {
        if (hashMap != null && !TextUtils.equals(curentStatus, UdeskConst.Status.robot)) {
            int onlineflag = UdeskUtils.objectToInt(hashMap.get("onlineflag"));
            String jid = UdeskUtils.objectToString(hashMap.get("jid"));
            if (isblocked.equals("true")) {
                return;
            }
            if (mAgentInfo == null
                    || TextUtils.isEmpty(mAgentInfo.getAgentJid())
                    || !jid.contains(mAgentInfo.getAgentJid())) {
                if (onlineflag == UdeskConst.ONLINEFLAG && imSetting.getLeave_message_type().equals(UdeskConst.LeaveMsgType.imMsg)
                        && isleaveMessageTypeMsg() && (!TextUtils.equals(curentStatus, UdeskConst.Status.chatting))) {
                    udeskViewMode.getApiLiveData().getAgentInfo(null, null);
                }
                return;
            }
            if (onlineflag == UdeskConst.ONLINEFLAG) {
                if (!currentStatusIsOnline) {
                    currentStatusIsOnline = true;
                    if (isNeedStartExpandabLyout) {
                        expandableLayout.startAnimation(true);
                        isNeedStartExpandabLyout = false;
                    }
                }
                showOnlineStatus(mAgentInfo);
                setUdeskImContainerVis(View.VISIBLE);
                initfunctionItems();
                if (popWindow != null) {
                    popWindow.cancle();
                }
            } else if (onlineflag == UdeskConst.OFFLINEFLAG) {
//                udeskViewMode.getApiLiveData().getIMStatus(mAgentInfo);
                udeskViewMode.getApiLiveData().getAgentInfo(null, null);

            }
        }
    }

    private void dealReceiveMsg(MessageInfo receiveMsg) {
        if (receiveMsg == null || ((TextUtils.equals(curentStatus, UdeskConst.Status.robot) && !isInAgentFragment))) {
            return;
        }
        if (receiveMsg.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_REDIRECT)) {
            try {
                redirectMsg = receiveMsg;
                JSONObject json = new JSONObject(receiveMsg.getMsgContent());
                String agent_id = json.optString("agent_id");
                String group_id = json.optString("group_id");
                curentStatus = UdeskConst.Status.init;
                udeskViewMode.getApiLiveData().getRedirectAgentInfo(agent_id, group_id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //多人会话发过来的消息
            if (receiveMsg.getInviterAgentInfo() != null && !TextUtils.isEmpty(receiveMsg.getInviterAgentInfo().getJid())) {
                receiveMsg.setUser_avatar(receiveMsg.getInviterAgentInfo().getAvatar());
                receiveMsg.setReplyUser(receiveMsg.getInviterAgentInfo().getNick_name());
            } else {
                if (mAgentInfo != null && mAgentInfo.getAgentCode() == UdeskConst.AgentResponseCode.HasAgent) {
                    if (!TextUtils.isEmpty(receiveMsg.getReplyUser())) {
                        mAgentInfo.setAgentNick(receiveMsg.getReplyUser());
                    } else {
                        receiveMsg.setReplyUser(mAgentInfo.getAgentNick());
                    }
                    if (!TextUtils.isEmpty(receiveMsg.getUser_avatar())) {
                        mAgentInfo.setHeadUrl(receiveMsg.getUser_avatar());
                    } else {
                        receiveMsg.setUser_avatar(mAgentInfo.getHeadUrl());
                    }

                    if (!mAgentInfo.getAgentJid().contains(receiveMsg.getmAgentJid())) {
                        udeskViewMode.getApiLiveData().getAgentInfo(null, null);
                        mAgentInfo.setAgentJid(receiveMsg.getmAgentJid());
                        String jid[] = receiveMsg.getmAgentJid().split("/");
                        String[] urlAndNick = UdeskDBManager.getInstance().getAgentUrlAndNick(jid[0]);
                        String agentName = "";
                        String headUrl = "";
                        if (urlAndNick != null) {
                            headUrl = urlAndNick[0];
                            agentName = urlAndNick[1];
                        }
                        if (TextUtils.isEmpty(headUrl)) {
                            mAgentInfo.setHeadUrl(headUrl);
                            receiveMsg.setUser_avatar(headUrl);
                        }
                        if (!TextUtils.isEmpty(receiveMsg.getReplyUser())) {
                            mAgentInfo.setAgentNick(receiveMsg.getReplyUser());
                        } else if (!TextUtils.isEmpty(agentName)) {
                            mAgentInfo.setAgentNick(agentName);
                            receiveMsg.setReplyUser(agentName);
                        } else {
                            receiveMsg.setReplyUser(mAgentInfo.getAgentNick());
                        }
                    }

                } else if (!TextUtils.isEmpty(receiveMsg.getmAgentJid()) && mAgentInfo != null && mAgentInfo.getAgentCode() == UdeskConst.AgentResponseCode.WaitAgent) {
                    if (myRunnable != null) {
                        mHandler.removeCallbacks(myRunnable);
                        mHandler.post(myRunnable);
                    }
                } else {
                    if (mAgentInfo != null && !TextUtils.isEmpty(receiveMsg.getmAgentJid())) {
                        udeskViewMode.getApiLiveData().getAgentInfo(pre_session_id, null);
                    }
                }
            }
            mChatAdapter.addItem(receiveMsg);
            UdeskDBManager.getInstance().addMessageDB(receiveMsg);
            mListView.smoothScrollToPosition(mChatAdapter.getCount());
        }
    }

    public void isShowAssociate(boolean b) {
        if (b) {
            mLlAssociate.setVisibility(View.VISIBLE);
        } else {
            mLlAssociate.setVisibility(View.GONE);
        }
    }

    /**
     * 机器人自动转人工
     */
    private void autoTransfer() {
        try {
            if (!UdeskSDKManager.getInstance().getUdeskConfig().isOnlyUseRobot) {
                //判断是否有导航设置，有导航设置进入导航页面
                boolean enableImgroup = imSetting != null ? imSetting.getEnable_im_group() : false;
                Customer customer = initCustomer.getCustomer();
                customerId = customer != null ? customer.getId() : "";
                if (!TextUtils.isEmpty(customerId)) {
                    udeskViewMode.setCustomerId(customerId);
                }
                List<AgentGroupNode> agentGroups = initCustomer.getIm_group();
                if (enableImgroup && agentGroups != null && agentGroups.size() > 0) {
                    UdeskOptionsAgentGroupActivity.start(UdeskChatActivity.this, IM_GROUP_REQUEST_CODE);
                    return;
                }
                initFragment(UdeskConst.CurrentFragment.agent);
                if (robot != null && robot.getEnable()) {
                    udeskViewMode.getRobotApiData().robotTransfer();
                }
                if (!isBlocked(customer)) {
                    initAgentInfo();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //是不是在黑名单
    private boolean isBlocked(Customer customer) {
        try {
            //如果客户被加入黑名单 则结束后续流程
            if (customer != null && customer.getIs_blocked()) {
                isblocked = "true";
                blockedNotice = initCustomer.getBlack_list_notice();
                if (TextUtils.isEmpty(blockedNotice)) {
                    blockedNotice = getString(R.string.add_bolcked_tips);
                }
                setTitlebar(blockedNotice, off);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toBlockedView();
                    }
                }, 1500);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void reFreshMessages(List<MessageInfo> msgs) {
        try {
            if (mChatAdapter != null && mListView != null) {
                int selectIndex = msgs.size();
                if (currentMode == UdeskChatActivity.PullEventModel) {
                    mChatAdapter.listAddEventItems(msgs);
                } else if (currentMode == UdeskChatActivity.PullRefreshModel) {
                    mChatAdapter.listAddItems(msgs, true);
                } else {
                    mChatAdapter.listAddItems(msgs, false);
                }
                mListView.onRefreshComplete();
                if (currentMode == UdeskChatActivity.InitViewMode || currentMode == UdeskChatActivity.PullEventModel) {
                    mListView.setSelection(mChatAdapter.getCount());
                } else {
                    mListView.setSelection(selectIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 根据来源创建底部fragment
     *
     * @param from
     */
    private void initFragment(String from) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (TextUtils.equals(UdeskConst.CurrentFragment.agent, from)) {
                fragment = new UdeskAgentFragment();
                mLlAssociate.setVisibility(View.GONE);
                mTitlebar.setRightViewVis(View.GONE);
                robotSengMsgCount = 0;
                usefulMap.clear();
                transferMap.clear();
                isInAgentFragment = true;
            } else if (TextUtils.equals(UdeskConst.CurrentFragment.robot, from)) {
                fragment = new UdeskRobotFragment();
                curentStatus = UdeskConst.Status.robot;
                isInAgentFragment = false;
            }
            transaction.replace(R.id.udesk_fragment_container, fragment).commitNowAllowingStateLoss();
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
                UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarMiddleTextResId, mTitlebar.getUdeskTopText(), mTitlebar.getUdeskBottomText());
                UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarRightTextResId, mTitlebar.getRightTextView());
                if (mTitlebar.getRootView() != null) {
                    UdeskConfigUtil.setUIbgDrawable(UdeskSDKManager.getInstance().getUdeskConfig().udeskTitlebarBgResId, mTitlebar.getRootView());
                }
                if (UdeskConfig.DEFAULT != UdeskSDKManager.getInstance().getUdeskConfig().udeskbackArrowIconResId) {
                    mTitlebar.getUdeskBackImg().setImageResource(UdeskSDKManager.getInstance().getUdeskConfig().udeskbackArrowIconResId);
                }
                mTitlebar.setTopTextSequence(getString(R.string.udesk_agent_connecting));
                mTitlebar.setLeftLinearVis(View.VISIBLE);
                mTitlebar.setLeftViewClick(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishAcitivty();
                    }
                });
                //机器转人工
                mTitlebar.setRightViewClick(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (UdeskUtils.isNetworkConnected(getApplicationContext())) {
                            autoTransfer();
                        } else {
                            UdeskUtils.showToast(getApplicationContext(),
                                    getResources().getString(R.string.udesk_has_wrong_net));
                        }
                    }
                });
                mTitlebar.setRightViewVis(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示顶部商品布局
     *
     * @param item
     */
    private void showCommodity(final UdeskCommodityItem item) {
        try {
            commodityView.setVisibility(View.VISIBLE);
            commodityTitle.setText(item.getTitle());
            commoditySubTitle.setText(item.getSubTitle());
            UdeskUtil.loadImage(getApplicationContext(), commodityThumbnail, item.getThumbHttpUrl());
            commodityLink.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (UdeskUtils.isNetworkConnected(getApplicationContext())) {
                        sentLink(item.getCommodityUrl());
                    } else {
                        UdeskUtils.showToast(getApplicationContext(),
                                getResources().getString(R.string.udesk_has_wrong_net));
                    }
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
            commodityThumbnail = findViewById(R.id.udesk_im_commondity_thumbnail);
            commodityRoot = (RelativeLayout) findViewById(R.id.udesk_commit_root);
            commodityTitle = (TextView) findViewById(R.id.udesk_im_commondity_title);
            commoditySubTitle = (TextView) findViewById(R.id.udesk_im_commondity_subtitle);
            commodityLink = (TextView) findViewById(R.id.udesk_im_commondity_link);
            UdeskConfigUtil.setUIbgDrawable(UdeskSDKManager.getInstance().getUdeskConfig().udeskCommityBgResId, commodityRoot);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskCommityTitleColorResId, commodityTitle);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskCommitysubtitleColorResId, commoditySubTitle);
            UdeskConfigUtil.setUITextColor(UdeskSDKManager.getInstance().getUdeskConfig().udeskCommityLinkColorResId, commodityLink);
            mListView = findViewById(R.id.udesk_conversation);
            mListView.setVisibility(View.GONE);
            mListView.addFooterView(LayoutInflater.from(this).inflate(R.layout.udesk_im_footview, null));
            expandableLayout = (UdeskExpandableLayout) findViewById(R.id.udesk_change_status_info);
            mContentLinearLayout = (LinearLayout) findViewById(R.id.udesk_content_ll);
            mRvAssociate = (RecyclerView) findViewById(R.id.udesk_robot_rv_associate);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRvAssociate.setLayoutManager(linearLayoutManager);
            mRvAssociate.addItemDecoration(new RecycleViewDivider(getApplicationContext(), LinearLayoutManager.HORIZONTAL, UdeskUtil.dip2px(getApplicationContext(), 1), getResources().getColor(R.color.udesk_color_d8d8d8), true));
            tipAdapter = new TipAdapter(getApplicationContext());
            mRvAssociate.setAdapter(tipAdapter);
            mLlAssociate = (LinearLayout) findViewById(R.id.udesk_robot_ll_associate);
            popWindow = new UdeskConfirmPopWindow(getApplicationContext());
            udeskViewMode.getDbLiveData().initDB(getApplicationContext());
            preSendRobotMessages = UdeskSDKManager.getInstance().getUdeskConfig().preSendRobotMessages;
            setListView();
            initLoadData();
            isNeedRelogin = !UdeskUtils.isNetworkConnected(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 是否打开video
     *
     * @return
     */
    private boolean isOpenVideo() {
        try {
            return imSetting != null
                    && imSetting.getVcall()
                    && imSetting.getSdk_vcall()
                    && UdeskUtil.isClassExists("udesk.udeskvideo.UdeskVideoActivity");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setListView() {
        try {
            mChatAdapter = new MessageAdatper(this);
            mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            mListView.setAdapter(mChatAdapter);
            mListView.setOnRefreshListener(new UDPullGetMoreListView.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // 已经没有更早的数据了
                    if (TextUtils.isEmpty(moreMarking)) {
                        UdeskUtils.showToast(getApplicationContext(), getString(R.string.udesk_no_more_history));
                        mListView.onRefreshComplete();
                        mListView.setSelection(0);
                    } else {
                        currentMode = PullRefreshModel;
                        udeskViewMode.getApiLiveData().messages(moreMarking, UdeskConst.PullMsgFrom.refresh);
                    }
                }
            });

            mListView.setRecyclerListener(new AbsListView.RecyclerListener() {
                @Override
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

    @Override
    protected void onResume() {
        super.onResume();
        try {
            UdeskConst.sdk_page_status = UdeskConst.SDK_PAGE_FOREGROUND;
            UdeskBaseInfo.isNeedMsgNotice = false;
            if (TextUtils.isEmpty(LQREmotionKit.getEmotionPath())) {
                LQREmotionKit.init(getApplicationContext());
            }
            if (!isWorkTime()) {
                return;
            }
            if (mAgentInfo != null && isOpenVideo()) {
                sendVideoMessage(imSetting, mAgentInfo, getApplicationContext());
            }
            registerNetWorkReceiver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void sendVideoMessage(ImSetting sdkimSetting,AgentInfo mAgentInfo, Context context ) {
        UdeskUtil.sendVideoMessage(sdkimSetting,mAgentInfo,context);
        if (connectVideoWebSocket != null){
            InvokeEventContainer.getInstance().event_OnConnectWebsocket.invoke(context);
        }else {
            connectVideoWebSocket = UdeskUtil.connectVideoWebSocket(context);
        }
    }

    //点击拍摄入口
    public void clickCamera() {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                takePhoto();
            } else {
                XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.CAMERA,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        new XPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                takePhoto();
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.camera_denied));
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //点击相册入口
    public void clickPhoto() {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                selectPhoto();
            } else {
                XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.EXTERNAL,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        new XPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                selectPhoto();
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.photo_denied));
                            }
                        });
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    //点击文件入口
    public void clickFile() {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                selectFile();
            } else {
                XPermissionUtils.requestPermissions(UdeskChatActivity.this, RequestCode.EXTERNAL,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        new XPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                selectFile();
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.file_denied));
                            }
                        });
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    //点击评价入口
    public void clickSurvy() {
        try {
            if (isPermitSurvy && mAgentInfo != null && !TextUtils.isEmpty(mAgentInfo.getAgent_id()) && !TextUtils.isEmpty(mAgentInfo.getIm_sub_session_id())) {
                setIsPermitSurvy(false);
                udeskViewMode.getApiLiveData().getHasSurvey(null);
            } else {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_survey_error));
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    //点击机器人评价入口
    public void clickRobotSurvy() {
        try {
            if (robotInit != null && 0 != robotInit.getSessionId()) {
                udeskViewMode.getRobotApiData().robotSessionHasSurvey();
            } else {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_survey_error));
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    //点击位置入口
    public void clickLocation() {
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
            if (!UdeskUtils.isNetworkConnected(getApplicationContext())) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_has_wrong_net));
                return;
            }
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
                                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.call_denied));
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
            if (curentStatus != UdeskConst.Status.robot) {
                udeskViewMode.sendFileMessage(this.getApplicationContext(), bitmapPath, UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
            } else {
                UdeskUtils.showToast(getApplicationContext(), "暂不支持");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (isMoreThan20 && isNeedQueueMessageSave()) {
                UdeskUtils.showToast(getApplicationContext(), getMoreThanSendTip());
                return;
            }
            if (CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) { //拍照后发生图片
                if (Activity.RESULT_OK == resultCode) {
                    if (photoUri != null && photoUri.getPath() != null) {
                        if (UdeskSDKManager.getInstance().getUdeskConfig().isScaleImg) {
                            udeskViewMode.scaleBitmap(getApplicationContext(),UdeskUtil.parseOwnUri(photoUri, UdeskChatActivity.this, cameraFile), 0);
                        } else {
                            udeskViewMode.sendFileMessage(this.getApplicationContext(), UdeskUtil.parseOwnUri(photoUri, this.getApplicationContext(), cameraFile), UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
                        }
                    } else if (data != null && data.hasExtra("data") && data.getParcelableExtra("data") != null && data.getParcelableExtra("data") instanceof Bitmap) {
                        udeskViewMode.sendBitmapMessage((Bitmap) data.getParcelableExtra("data"), getApplicationContext());
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
                        udeskViewMode.sendFileMessage(this.getApplicationContext(), path, UdeskConst.ChatMsgTypeString.TYPE_SHORT_VIDEO);
                    } else if (type.equals(UdeskConst.PICTURE)) {
                        String path = bundle.getString(UdeskConst.BitMapData);
                        udeskViewMode.sendFileMessage(this.getApplicationContext(), path, UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
                    }
                }

            } else if (SELECT_UDESK_IMAGE_ACTIVITY_REQUEST_CODE == requestCode) {
                if (resultCode != Activity.RESULT_OK || data == null) {
                    return;
                }
                Bundle bundle = data.getBundleExtra(UdeskConst.SEND_BUNDLE);
                if (bundle != null) {
                    ArrayList<LocalMedia> localMedias = bundle.getParcelableArrayList(UdeskConst.SEND_PHOTOS);
                    boolean isOrigin = bundle.getBoolean(UdeskConst.SEND_PHOTOS_IS_ORIGIN, false);

                    for (LocalMedia media : localMedias) {
                        final String pictureType = media.getPictureType();
                        final int mediaMimeType = UdeskUtil.isPictureType(pictureType);
                        if (mediaMimeType == UdeskUtil.TYPE_SHORT_VIDEO) {
                            long size = UdeskUtil.getFileSizeQ(this.getApplicationContext(), media.getPath());
                            if (size >= 30 * 1000 * 1000) {
                                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_file_to_large));
                                break;
                            }
                            udeskViewMode.sendFileMessage(this.getApplicationContext(), media.getPath(), UdeskConst.ChatMsgTypeString.TYPE_SHORT_VIDEO);
                        } else if (mediaMimeType == UdeskUtil.TYPE_IMAGE) {
                            if (isOrigin) {
                                udeskViewMode.sendFileMessage(this.getApplicationContext(), media.getPath(), UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
                            } else {
                                udeskViewMode.scaleBitmap(getApplicationContext(),media.getPath(), media.getOrientation());
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
                            String path = UdeskUtil.getFilePath(getApplicationContext(), mImageCaptureUri);
                            if (UdeskSDKManager.getInstance().getUdeskConfig().isScaleImg) {
                                udeskViewMode.scaleBitmap(getApplicationContext(),path, 0);
                            } else {
                                udeskViewMode.sendFileMessage(this.getApplicationContext(), path, UdeskConst.ChatMsgTypeString.TYPE_IMAGE);
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
                            udeskViewMode.sendBitmapMessage(bitmap, getApplicationContext());
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
                        String path = UdeskUtil.getFilePath(getApplicationContext(), mImageCaptureUri);
                        if (this.getWindow() != null && this.getWindow().getDecorView().getWindowToken() != null && UdeskUtil.isGpsNet(getApplicationContext())) {
                            toGpsNetView(true, null, path);
                            return;
                        }
                        sendFile(path);

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
                if (udeskViewMode != null) {
                    udeskViewMode.sendLocationMessage(latitude, longitude, postionValue, bitmapDir);
                }

            } else if (IM_GROUP_REQUEST_CODE == requestCode) {
                if (resultCode == Activity.RESULT_OK || data != null) {
                    initFragment(UdeskConst.CurrentFragment.agent);
                    if (robot != null && robot.getEnable()) {
                        udeskViewMode.getRobotApiData().robotTransfer();
                    }
                    menuId = data.getStringExtra(UdeskConst.UDESKMENUID);
                    if (!TextUtils.isEmpty(menuId)) {
                        PreferenceHelper.write(getApplicationContext(), UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                                UdeskConst.SharePreParams.Udesk_Menu_Id, menuId);
                    }
                    udeskViewMode.getApiLiveData().setMenu_id(getMenuId());
                    initAgentInfo();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }


    }

    private void sendFile(String path) {
        try {
            long size = UdeskUtil.getFileSizeQ(getApplicationContext(), path);
            if (size >= 30 * 1000 * 1000) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_file_to_large));
                return;
            } else if (size == 0) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_file_not_exist));
                return;
            }
            if (path.contains(".mp4")) {
                udeskViewMode.sendFileMessage(this.getApplicationContext(), path, UdeskConst.ChatMsgTypeString.TYPE_SHORT_VIDEO);
            } else {
                udeskViewMode.sendFileMessage(this.getApplicationContext(), path, UdeskConst.ChatMsgTypeString.TYPE_File);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }


    public void initLoadData() {
        try {
            offset = -1;
            historyCount = UdeskDBManager.getInstance().getMessageCount();
            currentMode = InitViewMode;
            loadHistoryRecords();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getAgentSeqNum() {
        try {
            if (mChatAdapter != null) {
                List<MessageInfo> listMessages = mChatAdapter.getList();
                for (int i = listMessages.size() - 1; i > 0; i--) {
                    MessageInfo messageUI = listMessages.get(i);
                    if (messageUI.getDirection() == UdeskConst.ChatMsgDirection.Recv && TextUtils.equals(UdeskConst.Sender.agent, messageUI.getSender())) {
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
    private void loadHistoryRecords() {
        try {
            mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
            // 已经没有更早的数据了
            if (offset == 0) {
                UdeskUtils.showToast(getApplicationContext(), getString(R.string.udesk_no_more_history));
                mListView.onRefreshComplete();
                mListView.setSelection(0);
            } else {
                // 还有老数据
//                int pageNum = UdeskConst.UDESK_HISTORY_COUNT;
//                if (offset == -1) {
//                    offset = historyCount - UdeskConst.UDESK_HISTORY_COUNT;
//                } else {
//                    if (offset - UdeskConst.UDESK_HISTORY_COUNT < 0) {
//                        pageNum = offset;
//                    }
//                    offset = offset - UdeskConst.UDESK_HISTORY_COUNT;
//                }
//                offset = (offset < 0 ? 0 : offset);
                udeskViewMode.getDbLiveData().getHistoryMessage(0, historyCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    //设置客服状态
    private void showOnlineStatus(AgentInfo mAgentInfo) {
        try {
            if (mAgentInfo == null) {
                return;
            }
            if (currentStatusIsOnline) {
                setTitlebar(mAgentInfo.getAgentNick(), on);
            } else {
                setTitlebar(mAgentInfo.getAgentNick(), off);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //发送广告消息
    private void sendCommodityMsg(UdeskCommodityItem commodity) {
        try {
            if (commodity != null && udeskViewMode != null) {
                udeskViewMode.sendCommodityMessage(commodity);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    //发送商品信息
    private void sendProduct(Product product) {
        try {
            if (product != null && udeskViewMode != null) {
                udeskViewMode.sendProductMessage(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    //客户设置的第一句默认消息
    private void sendDefaultMessage() {
        try {
            if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getUdeskConfig().firstMessage)) {
                udeskViewMode.sendTxtMessage(UdeskSDKManager.getInstance().getUdeskConfig().firstMessage);
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
                cameraFile = UdeskUtil.cameraFile(getApplicationContext());
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
    private void toLaunchSurveyView(SurveyOptionsModel surveyOptions) {
        try {
            if (surveyOptions.getOptions() == null || surveyOptions.getOptions().isEmpty()
                    || surveyOptions.getType().isEmpty()) {
                UdeskUtils.showToast(getApplicationContext(), getString(R.string.udesk_no_set_survey));
                return;
            }
            if (udeskSurvyPopwindow == null || !udeskSurvyPopwindow.isShowing()) {
                showSurveyPopWindow(surveyOptions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    WindowManager.LayoutParams params;

    /**
     * 显示满意度调查
     *
     * @param surveyOptions
     */
    private void showSurveyPopWindow(SurveyOptionsModel surveyOptions) {

        try {
            udeskSurvyPopwindow = new UdeskSurvyPopwindow(this, surveyOptions, new UdeskSurvyPopwindow.SumbitSurvyCallBack() {
                @Override
                public void sumbitSurvyCallBack(boolean isRobot, String optionId, String show_type, String survey_remark, String tags) {
                    if (isRobot) {
                        udeskViewMode.getRobotApiData().robotSessionSurvey(UdeskUtils.toInt(optionId), survey_remark);
                    } else {
                        if (TextUtils.isEmpty(customerId) || TextUtils.isEmpty(mAgentInfo.getAgent_id())) {
                            isPermitSurvy = false;
                            return;
                        }
                        udeskViewMode.getApiLiveData().putIMSurveyResult(optionId, show_type, survey_remark, tags);
                    }
                }

            });
            if (!udeskSurvyPopwindow.isShowing() &&
                    this.getWindow() != null &&
                    this.getWindow().getDecorView() != null &&
                    this.getWindow().getDecorView().getWindowToken() != null &&
                    findViewById(R.id.udesk_im_content).getWindowToken() != null) {
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
    public void toBlockedView() {
        try {

            String positiveLabel = this.getString(R.string.udesk_sure);
            String negativeLabel = this.getString(R.string.udesk_cancel);
            String title = blockedNotice;
            if (UdeskChatActivity.this.isFinishing()) {
                return;
            }
            if (!popWindow.isShowing() && this.getWindow() != null
                    && this.getWindow().getDecorView() != null && this.getWindow().getDecorView().getWindowToken() != null) {
                popWindow.show(this, this.getWindow().getDecorView(),
                        positiveLabel, negativeLabel, title,
                        new OnPopConfirmClick() {
                            @Override
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

    /**
     * 非wifi网络提示框
     *
     * @param isupload
     * @param info
     * @param path
     */
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
                            @Override
                            public void onPositiveClick() {
                                try {
                                    if (isupload && !TextUtils.isEmpty(path)) {
                                        sendFile(path);
                                    }
                                    if (!isupload && info != null) {
                                        udeskViewMode.getFileLiveData().downFile(info, getApplicationContext());
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
                udeskViewMode.getApiLiveData().getAgentInfo(pre_session_id, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    //语音的回收
    private void checkRecoredView(View view) {
        try {
            Object tag = view.getTag();
            if (tag != null) {
                BaseViewHolder holder = (BaseViewHolder) tag;
                if (holder.message != null &&
                        UdeskConst.parseTypeForMessage(holder.message.getMsgtype()) == UdeskConst.ChatMsgTypeInt.TYPE_AUDIO) {
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

    /**
     * 重新创建会话
     */
    public void reCreateIMCustomerInfo() {
        try {
            UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_agent_inti));
            curentStatus = UdeskConst.Status.init;
            udeskViewMode.getApiLiveData().initCustomer(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isOpenLeaveMsg() {
        boolean isOpen = false;
        try {
            if (imSetting != null) {
                isOpen = imSetting.getEnable_web_im_feedback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOpen;
    }

    //弹出表单留言的提示框
    public void confirmToForm() {
        try {
            boolean isOpen;
            //是否弹出表单： 如果获取到管理员的配置，使用管理员的配置。没获取到则使用配置中默认设置
            if (imSetting != null) {
                isOpen = imSetting.getEnable_web_im_feedback();
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
                if (imSetting != null && !TextUtils.isEmpty(imSetting.getLeave_message_guide())) {
                    title = imSetting.getLeave_message_guide();
                } else {
                    title = this.getString(R.string.udesk_msg_offline_to_form);
                }
            } else {
                //关闭留言的文案,如果获取到后台的设置，则使用后台的设置，没获取的后台的，本地客户设置了，则使用客户的设置文案。否则用默认的文案
                if (imSetting != null && !TextUtils.isEmpty(imSetting.getNo_reply_hint())) {
                    title = imSetting.getNo_reply_hint();
                } else {
                    title = this.getString(R.string.udesk_msg_busy_default_to_form);
                }
            }
            if (mAgentInfo != null && mAgentInfo.getAgentCode() == UdeskConst.AgentResponseCode.WaitAgent) {
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
                            @Override
                            public void onPositiveClick() {
                                try {
                                    dismissFromWindow();
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
            curentStatus = UdeskConst.Status.leaveMessage;
            if (UdeskSDKManager.getInstance().getUdeskConfig().formCallBack != null) {
                UdeskSDKManager.getInstance().getUdeskConfig().formCallBack.toLuachForm(UdeskChatActivity.this);
                return;
            }
            if (isleaveMessageTypeMsg()) {
                //直接留言或者会话留言
                if (mAgentInfo != null) {
                    mAgentInfo = null;
                }
                if (mHandler != null && myRunnable != null) {
                    mHandler.removeCallbacks(myRunnable);
                }
                udeskViewMode.getApiLiveData().quitQueue(UdeskConfig.UdeskQueueFlag.FORCE_QUIT);
                addWeclomeMessage();
                setUdeskImContainerVis(View.GONE);
                setTitlebar(getString(R.string.udesk_ok), off);

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
            dismissFromWindow();
            UdeskSDKManager.getInstance().goToForm(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissFromWindow() {
        if (popWindow != null) {
            popWindow.dismiss();
        }
    }

    public void showFailToast(final String failMsg) {
        try {
            setTitlebar(getResources().getString(R.string.udesk_api_error), off);
            if (!TextUtils.isEmpty(failMsg)) {
                UdeskUtils.showToast(getApplicationContext(), failMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    public boolean getPressionStatus() {
        try {
            if ((curentStatus.equals(UdeskConst.Status.pre_session) || curentStatus.equals(UdeskConst.Status.robot) || curentStatus.equals(UdeskConst.Status.init))
                    && initCustomer.getPre_session().getShow_pre_session() && initCustomer.getPre_session().getPre_session()
            ) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 处理请求获取到客服信息
     *
     * @param agentInfo
     */
    public void dealAgentInfo(final AgentInfo agentInfo, boolean isRedirect) {

        try {
            if (agentInfo == null) {
                return;
            }
            switch (agentInfo.getAgentCode()) {
                case UdeskConst.AgentResponseCode.NoAgent:
                    mAgentInfo = agentInfo;
                    curentStatus = UdeskConst.Status.noAgent;
                    dismissFromWindow();
                    udeskViewMode.sendPrefilterMsg(false);
                    if (isleaveMessageTypeMsg()) {
                        if (imSetting.getLeave_message_type().equals(UdeskConst.LeaveMsgType.imMsg) && imLeaveMsgCache.size() > 0) {
                            sendIMLeaveMsg(imLeaveMsgCache);
                        }
                        addWeclomeMessage();
                        setUdeskImContainerVis(View.GONE);
                        setTitlebar(getString(R.string.udesk_ok), off);
                    } else {
                        setTitlebar(getString(R.string.udesk_label_customer_offline), off);
                        setUdeskImContainerVis(View.GONE);
                        delayShowtips(mHandler);
                    }
                    if (queueItem != null && mChatAdapter != null) {
                        mChatAdapter.removeQueueMessage(queueItem);
                        queueItem = null;
                        mListView.smoothScrollToPosition(mChatAdapter.getCount());
                    }
                    break;
                case UdeskConst.AgentResponseCode.HasAgent:
                    mAgentInfo = agentInfo;
                    curentStatus = UdeskConst.Status.chatting;
                    // 有客服titlebar上显示
                    UdeskDBManager.getInstance().addAgentInfoDB(agentInfo);
                    udeskViewMode.setAgentInfo(mAgentInfo);
                    fragment.clearInputContent();
                    if (!isRedirect) {
                        dismissFromWindow();
                        setUdeskImContainerVis(View.VISIBLE);
                        setNavigationViewVis();
                        initfunctionItems();
                        if (queueItem != null && mChatAdapter != null) {
                            mChatAdapter.removeQueueMessage(queueItem);
                            queueItem = null;
                            mListView.smoothScrollToPosition(mChatAdapter.getCount());
                        }
                        udeskViewMode.sendPrefilterMsg(true);
                        if (!hasSendCommodity) {
                            hasSendCommodity = true;
                            sendCommodityMsg(UdeskSDKManager.getInstance().getUdeskConfig().commodity);
                            sendProduct(UdeskSDKManager.getInstance().getUdeskConfig().mProduct);
                        }
                        if (!hasSendFirstMessage) {
                            hasSendFirstMessage = true;
                            sendDefaultMessage();
                        }
                        if (imLeaveMsgCache.size() > 0) {
                            for (MessageInfo msg : imLeaveMsgCache) {
                                if (msg != null && !TextUtils.isEmpty(msg.getMsgContent())) {
                                    udeskViewMode.sendTxtMessage(msg.getMsgContent());
                                }
                            }
                            imLeaveMsgCache.clear();
                        }
                    } else {
                        String redirectTip = getString(R.string.udesk_transfer_success) + agentInfo.getAgentNick() + getString(R.string.udesk_service);
                        if (redirectMsg != null) {
                            redirectMsg.setMsgContent(redirectTip);
                            UdeskDBManager.getInstance().addMessageDB(redirectMsg);
                        }
                        if (mChatAdapter != null) {
                            mChatAdapter.addItem(redirectMsg);
                            mListView.smoothScrollToPosition(mChatAdapter.getCount());
                        }
                    }
                    showOnlineStatus(mAgentInfo);
                    if (mAgentInfo != null && isOpenVideo()) {
                        sendVideoMessage(imSetting, mAgentInfo, getApplicationContext());
                    }
                    if (currentStatusIsOnline) {
                        udeskViewMode.getApiLiveData().messages("", UdeskConst.PullMsgFrom.hasAgent);
                    }
                    break;
                case UdeskConst.AgentResponseCode.WaitAgent:
                    curentStatus = UdeskConst.Status.queuing;
                    mAgentInfo = agentInfo;
                    setTitlebar(getApplicationContext().getResources().getString(R.string.udesk_in_the_line), queue);
                    setUdeskImContainerVis(View.VISIBLE);
                    mHandler.postDelayed(myRunnable, QUEUE_RETEY_TIME);
                    udeskViewMode.sendPrefilterMsg(true);
                    if (!hasSendCommodity) {
                        hasSendCommodity = true;
                        sendCommodityMsg(UdeskSDKManager.getInstance().getUdeskConfig().commodity);
                        sendProduct(UdeskSDKManager.getInstance().getUdeskConfig().mProduct);
                    }
                    if (!hasSendFirstMessage) {
                        hasSendFirstMessage = true;
                        sendDefaultMessage();
                    }
                    if (queueItem == null) {
                        queueItem = new UdeskQueueItem(isOpenLeaveMsg(), mAgentInfo.getMessage());
                        if (mChatAdapter != null) {
                            mChatAdapter.addItem(queueItem);
                            mListView.smoothScrollToPosition(mChatAdapter.getCount());
                        }
                    } else {
                        queueItem.setQueueContent(mAgentInfo.getMessage());
                        mChatAdapter.notifyDataSetChanged();
                    }
                    if (imLeaveMsgCache.size() > 0) {
                        for (MessageInfo msg : imLeaveMsgCache) {
                            if (msg != null && !TextUtils.isEmpty(msg.getMsgContent())) {
                                udeskViewMode.sendTxtMessage(msg.getMsgContent());
                            }
                        }
                        imLeaveMsgCache.clear();
                    }
                    break;
                case UdeskConst.AgentResponseCode.NonExistentAgent:
                    UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_nonexistent_agent));
                    break;
                case UdeskConst.AgentResponseCode.NonExistentGroupId:
                    UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_nonexistent_groupId));
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAgentId() {
        try {
            if (TextUtils.isEmpty(agentId)) {
                return PreferenceHelper.readString(getApplicationContext(), UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                        UdeskConst.SharePreParams.Udesk_Agent_Id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return agentId;
    }

    public String getGroupId() {
        try {
            if (TextUtils.isEmpty(groupId)) {
                return PreferenceHelper.readString(getApplicationContext(), UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                        UdeskConst.SharePreParams.Udesk_Group_Id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return groupId;
    }

    public String getMenuId() {
        try {
            if (TextUtils.isEmpty(menuId) && TextUtils.isEmpty(groupId) && TextUtils.isEmpty(agentId)) {
                return PreferenceHelper.readString(getApplicationContext(), UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                        UdeskConst.SharePreParams.Udesk_Menu_Id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return menuId;
    }

    public void setIsPermitSurvy(boolean isPermmit) {
        try {
            isPermitSurvy = isPermmit;
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


    public void showStartOrStopAnimation(final MessageInfo info,
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
                        if (child == null || child.getTag() == null) {
                            continue;
                        }
                        BaseViewHolder holder = (BaseViewHolder) child.getTag();
                        MessageInfo msgTemp = holder.message;
                        if (msgTemp != null &&
                                UdeskConst.parseTypeForMessage(msgTemp.getMsgtype()) == UdeskConst.ChatMsgTypeInt.TYPE_AUDIO) {
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
                    @Override
                    public void onPlayComplete(MessageInfo message) {
                        showStartOrStopAnimation(message, false);
                        recycleVoiceRes();
                    }

                    @Override
                    public void onPlayStart(MessageInfo message) {
                        showStartOrStopAnimation(message, true);
                    }

                    @Override
                    public void onPlayPause(MessageInfo message) {
                        showStartOrStopAnimation(message, false);
                        recycleVoiceRes();
                    }

                    @Override
                    public void onPlayEnd(MessageInfo message) {
                        showStartOrStopAnimation(message, false);
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
                                    showStartOrStopAnimation(message, false);
                                }
                            }
                        }

                    }

                };

            }
            if ((TextUtils.isEmpty(message.getLocalPath()) || !UdeskUtil.isExitFileByPath(this.getApplicationContext(), message.getLocalPath()))
                    && !UdeskUtil.fileIsExitByUrl(UdeskChatActivity.this.getApplicationContext(), UdeskConst.FileAudio, message.getMsgContent())) {
                if (UdeskUtils.isNetworkConnected(getApplicationContext())) {
                    udeskViewMode.getFileLiveData().downAudio(message, getApplicationContext());
                }
            }
            mRecordFilePlay.click(message, mPlayCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //回收录音资源
    public void recycleVoiceRes() {
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
                        @Override
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
            if (!UdeskUtils.isNetworkConnected(getApplicationContext())) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_has_wrong_net));
                return;
            }
            if (curentStatus.equals(UdeskConst.Status.robot) || TextUtils.equals(curentStatus, UdeskConst.Status.chatting)) {
                udeskViewMode.sendTxtMessage(linkMsg);
            } else if (getPressionStatus()) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_agent_connecting));
                udeskViewMode.sendTxtMessage(linkMsg);
            } else if (isNeedQueueMessageSave()) {
                if (isMoreThan20) {
                    UdeskUtils.showToast(getApplicationContext(), getMoreThanSendTip());
                    return;
                }
                //排队中需要发送消息
                udeskViewMode.sendTxtMessage(linkMsg);
            } else if (isleaveMessageTypeMsg()) {
                if (!udeskViewMode.isLeavingMsg()) {
                    addCustomerLeavMsg();
                    udeskViewMode.setLeavingMsg(true);
                }
                if (imSetting.getLeave_message_type().equals(UdeskConst.LeaveMsgType.directMsg)) {
                    udeskViewMode.sendLeaveMessage(linkMsg);
                } else if (imSetting.getLeave_message_type().equals(UdeskConst.LeaveMsgType.imMsg)) {
                    udeskViewMode.sendIMLeaveMessage(linkMsg);
                }
            } else {
                confirmToForm();
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

    }

    //重试发送消息(无消息对话过滤状态，排队，在线)
    public void retrySendMsg(MessageInfo message) {
        try {
            if (!UdeskUtils.isNetworkConnected(getApplicationContext())) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_has_wrong_net));
                return;
            }
            if (isMoreThan20 && isNeedQueueMessageSave()) {
                UdeskUtils.showToast(getApplicationContext(), getMoreThanSendTip());
                return;
            }
            //不在无消息对话过滤状态，也不在排队，也不是在线情况下 提示客服
            if (!getPressionStatus() && !TextUtils.equals(curentStatus, UdeskConst.Status.chatting) && !curentStatus.equals(UdeskConst.Status.queuing) && !curentStatus.equals(UdeskConst.Status.robot)) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_label_customer_offline));
                return;
            }
            if (message != null) {
                changeImState(message.getMsgId(), UdeskConst.SendFlag.RESULT_RETRY);

                if (curentStatus.equals(UdeskConst.Status.robot)) {
                    udeskViewMode.getRobotApiData().robotMessage(message);
                } else {
                    if (TextUtils.equals(curentStatus, UdeskConst.Status.chatting)) {
                        udeskViewMode.startRetryMsg(message);
                    } else {
                        if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_LEAVEMSG)) {
                            if (!TextUtils.isEmpty(customerId)) {
                                udeskViewMode.putLeavesMsg(message);
                            }
                        } else if (message.getMsgtype().equals(UdeskConst.ChatMsgTypeString.TYPE_LEAVEMSG_IM)) {
                            if (!TextUtils.isEmpty(customerId)) {
                                udeskViewMode.putIMLeavesMsg(message, getAgentId(), getGroupId(), getMenuId());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //取消上传视频消息
    public void cancelSendVideoMsg(MessageInfo message) {
        try {
            if (message != null) {
                udeskViewMode.getFileLiveData().cancleUploadFile(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //下载文件
    public void downLoadMsg(MessageInfo message) {
        try {
            if (!UdeskUtils.isNetworkConnected(getApplicationContext())) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_has_wrong_net));
                return;
            }
            if (message != null) {
                if (UdeskUtil.isGpsNet(getApplicationContext())) {
                    toGpsNetView(false, message, null);
                    return;
                }
                udeskViewMode.getFileLiveData().downFile(message, getApplicationContext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void downLoadVideo(MessageInfo message) {
        try {
            if (!UdeskUtils.isNetworkConnected(getApplicationContext())) {
                UdeskUtils.showToast(getApplicationContext(), getResources().getString(R.string.udesk_has_wrong_net));
                return;
            }
            if (message != null) {
                udeskViewMode.getFileLiveData().downVideo(message, getApplicationContext());
            }
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
                mTitlebar.setTopTextSequence(title);
                if (TextUtils.equals(curentStatus, UdeskConst.Status.pre_session)) {
                    mTitlebar.setUdeskBottomTextVis(View.GONE);
                } else {
                    mTitlebar.setUdeskBottomTextVis(View.VISIBLE);
                    if (status.equals(on)) {
                        mTitlebar.setBottomTextSequence(getString(R.string.udesk_online));
                    } else if (status.equals(off)) {
                        mTitlebar.setBottomTextSequence(getString(R.string.udesk_offline));
                    } else if (status.equals(queue)) {
                        mTitlebar.setBottomTextSequence(getString(R.string.udesk_in_the_line));
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (fragment != null){
                fragment.onBackPressed();
            }else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void finishAcitivty() {
        //后台勾选开启后，对于同一个对话，用户多次进入，点击返回离开，若没有进行过满意度调查，
        // 则返回点击后均弹出满意度调查窗口，若已经有满意度调查结果，则返回不再发起调查都关闭.

        if (!isFirstFinish) {
            finish();
            return;
        }
        isFirstFinish = false;
        try {
            if (!currentStatusIsOnline || imSetting == null || !imSetting.getInvestigation_when_leave() || !imSetting.getEnable_im_survey()) {
                finish();
            } else if (initCustomer != null && initCustomer.getIm_survey() != null && mAgentInfo != null && !TextUtils.isEmpty(mAgentInfo.getAgent_id())) {
                udeskViewMode.getApiLiveData().getHasSurvey(new IUdeskHasSurvyCallBack() {
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

    private void setNavigationViewVis() {
        try {
            fragment.setNavigationViewVis();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initfunctionItems() {
        try {
            fragment.initfunctionItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置会话时的语音图片等按钮的显隐藏
    private void setUdeskImContainerVis(int vis) {
        fragment.setUdeskImContainerVis(vis);
    }

    public boolean isleaveMessageTypeMsg() {
        try {
            return imSetting != null && imSetting.getEnable_web_im_feedback()
                    && (imSetting.getLeave_message_type().equals(UdeskConst.LeaveMsgType.directMsg) || imSetting.getLeave_message_type().equals(UdeskConst.LeaveMsgType.imMsg));
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

    /**
     * video缩略图
     *
     * @param info
     */
    public void showVideoThumbnail(MessageInfo info) {
        try {
            udeskViewMode.getFileLiveData().getBitmap(getApplicationContext(), info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拉取跳序信息
     */
    public void pullByJumpOrder() {
        try {
            moreMarking = "";
            udeskViewMode.getApiLiveData().messages(moreMarking, UdeskConst.PullMsgFrom.jump);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        try {
            UdeskBaseInfo.isNeedMsgNotice = true;
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

    public boolean isNeedQueueMessageSave() {
        return curentStatus.equals(UdeskConst.Status.queuing) && UdeskSDKManager.getInstance().getEnableSendMessageWhenQueue();
    }

    public String getMoreThanSendTip() {
        return TextUtils.isEmpty(moreThanStirng) ? getResources().getString(R.string.udesk_in_the_line_max_send) : moreThanStirng;
    }

    private void doAgentStatus(String imStatus, AgentInfo agentInfo) {
        try {
            if (imStatus.equals(off)) {
                if (currentStatusIsOnline) {
                    expandableLayout.startAnimation(false);
                    currentStatusIsOnline = false;
                    isNeedStartExpandabLyout = true;
                }
            } else {
                if (!currentStatusIsOnline) {
                    currentStatusIsOnline = true;
                    if (isNeedStartExpandabLyout) {
                        expandableLayout.startAnimation(true);
                        isNeedStartExpandabLyout = false;
                    }
                }
            }
            if (agentInfo != null) {
                dealAgentInfo(agentInfo, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 清理资源
     */
    private void cleanSource() {
        try {
            if (isDestroyed) {
                return;
            }
            // 回收资源
            UdeskConst.sdk_page_status = UdeskConst.SDK_PAGE_FINISH;
            MergeModeManager.getmInstance().clear();
            isDestroyed = true;

            if (popWindow != null && popWindow.isShowing()) {
                popWindow.dismiss();
            }
            fragment.cleanSource();
            XPermissionUtils.destory();
            recycleVoiceRes();
            if (mHandler != null && myRunnable != null) {
                mHandler.removeCallbacks(myRunnable);
            }
            if (queueItem != null) {
                queueItem = null;
            }
            InvokeEventContainer.getInstance().event_OnDisConnectWebsocket.invoke(getApplicationContext());
            unRegister();
            unbind();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unbind() {
        InvokeEventContainer.getInstance().event_OnVideoEventReceived.unBind(this);
        InvokeEventContainer.getInstance().eventui_OnNewMessage.unBind(this);
        InvokeEventContainer.getInstance().event_OnMessageReceived.unBind(this);
        InvokeEventContainer.getInstance().event_OnNewPresence.unBind(this);
        InvokeEventContainer.getInstance().event_OnReqsurveyMsg.unBind(this);
        InvokeEventContainer.getInstance().event_OnActionMsg.unBind(this);
    }

    public void recordError(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UdeskUtils.showToast(UdeskChatActivity.this.getApplicationContext(), getResources()
                        .getString(R.string.udesk_im_record_error));
            }
        });
    }

    //增加一条客户留言事件
    public void addCustomerLeavMsg() {
        try {
            MessageInfo msg = UdeskUtil.buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_EVENT,
                    System.currentTimeMillis(), getString(R.string.udesk_customer_leavemsg));
            msg.setSendFlag(UdeskConst.SendFlag.RESULT_SUCCESS);
            msg.setDirection(UdeskConst.ChatMsgDirection.Recv);
            udeskViewMode.getDbLiveData().saveMessageDB(msg);
            mChatAdapter.addItem(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addWeclomeMessage() {
        try {
            if (TextUtils.isEmpty(leavMsgId) && imSetting != null && !TextUtils.isEmpty(imSetting.getLeave_message_guide())) {
                leavMsgId = UdeskIdBuild.buildMsgId();
                MessageInfo weclomeMsg = UdeskUtil.addLeavMsgWeclome(imSetting.getLeave_message_guide(), leavMsgId);
                mChatAdapter.addItem(weclomeMsg);
                mListView.smoothScrollToPosition(mChatAdapter.getCount());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onVideoEvent(String event, String id, String message, Boolean isInvite) {

        try {
            if (TextUtils.equals(curentStatus, UdeskConst.Status.robot)) {
                return;
            }
            if (mAgentInfo != null && mAgentInfo.getAgentCode() == 2000) {
                if (event.equals(UdeskConst.ReceiveType.StartMedio)) {
                    MessageInfo messageInfo = UdeskUtil.buildVideoEventMsg(id, isInvite, message, customerId,
                            mAgentInfo.getAgentJid(), mAgentInfo.getAgentNick(), mAgentInfo.getIm_sub_session_id());
                    udeskViewMode.getDbLiveData().saveMessageDB(messageInfo);
                } else if (event.equals(UdeskConst.ReceiveType.Cancle)) {
                    //取消
                    UdeskDBManager.getInstance().updateMsgContent(id,
                            message);
                    MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                    addMessage(eventMsg);

                } else if (event.equals(UdeskConst.ReceiveType.Busy)) {
                    message = mAgentInfo.getAgentNick() + message;
                    UdeskDBManager.getInstance().updateMsgContent(id, message);
                    MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                    addMessage(eventMsg);
                } else if (event.equals(UdeskConst.ReceiveType.Timeout)) {
                    message = mAgentInfo.getAgentNick() + message;
                    UdeskDBManager.getInstance().updateMsgContent(id,
                            message);
                    MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                    addMessage(eventMsg);
                } else if (event.equals(UdeskConst.ReceiveType.Reject)) {
                    UdeskDBManager.getInstance().updateMsgContent(id,
                            message);
                    MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                    addMessage(eventMsg);
                } else if (event.equals(UdeskConst.ReceiveType.Over)) {
                    UdeskDBManager.getInstance().updateMsgContent(id,
                            message);
                    MessageInfo eventMsg = UdeskDBManager.getInstance().getMessage(id);
                    addMessage(eventMsg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addMessage(final MessageInfo message) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mChatAdapter.addItem(message);
                    mListView.smoothScrollToPosition(mChatAdapter.getCount());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Boolean> getUsefulMap() {
        return usefulMap;
    }

    public Map<String, Boolean> getTransferMap() {
        return transferMap;
    }

    public List<ProductListBean> getRandomList() {
        return randomList;
    }

    /**
     * 收到发送消息的回执
     */
    public void onMessageReceived(final String msgId) {
        try {
            if (!TextUtils.isEmpty(msgId) && !TextUtils.equals(curentStatus, UdeskConst.Status.robot)) {
                udeskViewMode.getSendMessageLiveData().removeSendMsgCace(msgId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeImState(msgId, UdeskConst.SendFlag.RESULT_SUCCESS);
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 收到新消息
     */
    public void onNewMessage(final MessageInfo msgInfo) {
        try {
            if (msgInfo != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dealReceiveMsg(msgInfo);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 收到客服在线下线的通知
     */
    public void onNewPresence(final String jid, final Integer onlineflag) {
        try {
            if (!TextUtils.isEmpty(jid)) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Map<String, Object> hashMap = new HashMap<>();
                        hashMap.put("jid", jid);
                        hashMap.put("onlineflag", onlineflag);
                        dealReceivePresence(hashMap);
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 收到满意度调查消息
     *
     * @param isSurvey
     */
    public void onReqsurveyMsg(Boolean isSurvey) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dealReceiveSurvey();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //收到結束會話和工單回復的Action消息
    public void onActionMsg(String type, String actionText, String agentJId) {
        try {
            if (TextUtils.isEmpty(actionText) || TextUtils.equals(curentStatus, UdeskConst.Status.robot)) {
                return;
            }
            if (type.equals("ticket_reply")) {
                //调用获取工单离线消息
                if (!TextUtils.isEmpty(customerId)) {
                    udeskViewMode.getApiLiveData().getTicketReplies(1, UdeskConst.UDESK_HISTORY_COUNT);
                }
                return;
            }
            if (actionText.equals("overtest")) {
                UdeskXmppManager.getInstance().sendActionMessage(agentJId);
            } else if (actionText.equals("over")) {
                curentStatus = UdeskConst.Status.over;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTitlebar(getResources().getString(R.string.udesk_close_chart), off);
                        InvokeEventContainer.getInstance().eventui_OnHideLayout.invoke(true);
                        initfunctionItems();
                    }
                });
                isMoreThan20 = false;
                currentStatusIsOnline = false;
                try {
                    Thread.sleep(2000);
                    UdeskXmppManager.getInstance().cancleXmpp();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
