package cn.udesk.fragment;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.R;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.activity.NavigationFragment;
import cn.udesk.adapter.UdeskFunctionAdapter;
import cn.udesk.config.UdeskConfig;
import cn.udesk.emotion.EmotionKeyboard;
import cn.udesk.emotion.EmotionLayout;
import cn.udesk.emotion.LQREmotionKit;
import cn.udesk.model.FunctionMode;
import cn.udesk.permission.RequestCode;
import cn.udesk.permission.XPermissionUtils;
import cn.udesk.voice.AudioRecordButton;
import udesk.core.UdeskConst;
import udesk.core.event.InvokeEventContainer;
import udesk.core.utils.UdeskUtils;

public class UdeskAgentFragment extends UdeskbaseFragment implements View.OnClickListener {

    private LinearLayout navigationRootView, addNavigationFragmentView, navigation_survy;
    private ImageView mAudioImg; //语言和内容
    private EditText mInputEditView;
    private AudioRecordButton mBtnAudio;
    private ImageView mEmojiImg;
    private ImageView mMoreImg;
    private TextView sendBtn;
    private FrameLayout mBottomFramlayout;
    private EmotionLayout mEmotionlayout;
    private LinearLayout mMoreLayout;
    private GridView funGridView;
    private UdeskFunctionAdapter udeskFunctionAdapter;
    private List<FunctionMode> functionItems = new ArrayList<FunctionMode>();
    private EmotionKeyboard mEmotionKeyboard;
    private long preMsgSendTime = 0; //记录发送预支消息间隔时间

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        try {
            navigationRootView = view.findViewById(R.id.navigation_root_view);
            addNavigationFragmentView = view.findViewById(R.id.fragment_view);
            navigation_survy = view.findViewById(R.id.navigation_survy);
            mAudioImg = (ImageView) view.findViewById(R.id.udesk_img_audio);
            mInputEditView = (EditText) view.findViewById(R.id.udesk_bottom_input);
            mBtnAudio = (AudioRecordButton) view.findViewById(R.id.udesk_audio_btn);
            mEmojiImg = (ImageView) view.findViewById(R.id.udesk_emoji_img);
            mMoreImg = (ImageView) view.findViewById(R.id.udesk_more_img);
            sendBtn = (TextView) view.findViewById(R.id.udesk_bottom_send);
            mBottomFramlayout = (FrameLayout) view.findViewById(R.id.udesk_bottom_frame);
            mEmotionlayout = (EmotionLayout) view.findViewById(R.id.udesk_emotion_view);
            mMoreLayout = (LinearLayout) view.findViewById(R.id.udesk_more_layout);
            funGridView = (GridView) (view.findViewById(R.id.function_gridview));
            navigation_survy.setOnClickListener(this);
            sendBtn.setOnClickListener(this);
            mEmotionlayout.attachEditText(mInputEditView);
            //进入会话界面 关闭推送
            if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getRegisterId(getActivity().getApplicationContext()))) {
                UdeskSDKManager.getInstance().setSdkPushStatus(UdeskSDKManager.getInstance().getDomain(getActivity().getApplicationContext()),
                        UdeskSDKManager.getInstance().getAppkey(getActivity().getApplicationContext()),
                        UdeskSDKManager.getInstance().getSdkToken(getActivity().getApplicationContext()), UdeskConfig.UdeskPushFlag.OFF,
                        UdeskSDKManager.getInstance().getRegisterId(getActivity().getApplicationContext()),
                        UdeskSDKManager.getInstance().getAppId(getActivity().getApplicationContext()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            InvokeEventContainer.getInstance().eventui_OnHideLayout.bind(this, "onHideBottomLayout");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {
            InvokeEventContainer.getInstance().eventui_OnHideLayout.unBind(this);
            //设置了开启推送标识，离开会话界面开启推送，
            if (!TextUtils.isEmpty(UdeskSDKManager.getInstance().getRegisterId(getActivity().getApplicationContext())) && UdeskSDKManager.getInstance().getUdeskConfig().isUserSDkPush) {
                UdeskSDKManager.getInstance().setSdkPushStatus(UdeskSDKManager.getInstance().getDomain(getActivity().getApplicationContext()),
                        UdeskSDKManager.getInstance().getAppkey(getActivity().getApplicationContext()), UdeskSDKManager.getInstance().getSdkToken(getActivity().getApplicationContext()), UdeskConfig.UdeskPushFlag.ON,
                        UdeskSDKManager.getInstance().getRegisterId(getActivity().getApplicationContext()), UdeskSDKManager.getInstance().getAppId(getActivity().getApplicationContext()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * 隐藏表情 更多布局 图标恢复默认
     *
     * @param isHide
     */
    public void onHideBottomLayout(Boolean isHide) {
        try {
            if (isHide) {
                mBottomFramlayout.setVisibility(View.GONE);
                hideEmotionLayout();
                hideMoreLayout();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            initFunctionAdapter();
            setNavigationViewVis();
            mBtnAudio.init(UdeskUtil.getDirectoryPath(getActivity().getApplicationContext(), UdeskConst.FileAudio));
            mBtnAudio.setRecordingListener(new AudioRecordButton.OnRecordingListener() {
                @Override
                public void recordStart() {
                    if (udeskChatActivity.mRecordFilePlay != null) {
                        udeskChatActivity.showStartOrStopAnimation(
                                udeskChatActivity.mRecordFilePlay.getPlayAduioMessage(), false);
                        udeskChatActivity.recycleVoiceRes();
                    }
                }

                @Override
                public void recordFinish(String audioFilePath, long recordTime) {
                    udeskViewMode.sendRecordAudioMsg(udeskChatActivity.getApplicationContext(), audioFilePath, recordTime);
                }

                @Override
                public void recordError(String message) {
                    udeskChatActivity.recordError(message);
                }
            });
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseVoice) {
                mAudioImg.setVisibility(View.VISIBLE);
            } else {
                mAudioImg.setVisibility(View.GONE);
            }
            setEmojiVis(View.VISIBLE);
            setMoreVis(View.VISIBLE);
            initEmotionKeyboard();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * emoji的显示隐藏
     */
    private void setEmojiVis(int vis){
        if (View.VISIBLE == vis && UdeskSDKManager.getInstance().getUdeskConfig().isUseEmotion && LQREmotionKit.getEmotionPath() != null){
            mEmojiImg.setVisibility(vis);
        }else {
            mEmojiImg.setVisibility(View.GONE);
        }
    }

    /**
     * 更多控件的显示隐藏
     * @param vis
     */
    private void setMoreVis(int vis){
        if (View.VISIBLE == vis && UdeskSDKManager.getInstance().getUdeskConfig().isUseMore){
            mMoreImg.setVisibility(vis);
        }else {
            mMoreImg.setVisibility(View.GONE);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        mInputEditView.clearFocus();
    }

    /**
     * 初始化表情键盘
     */
    public void initEmotionKeyboard() {
        try {
            mEmotionKeyboard = EmotionKeyboard.with(udeskChatActivity);
            mEmotionKeyboard.bindToEditText(mInputEditView);
            mEmotionKeyboard.bindToContent(udeskChatActivity.mContentLinearLayout);
            mEmotionKeyboard.setEmotionLayout(mBottomFramlayout);
            mEmotionKeyboard.bindToEmotionButton(mEmojiImg, mMoreImg);
            mEmotionKeyboard.setOnEmotionButtonOnClickListener(new EmotionKeyboard.OnEmotionButtonOnClickListener() {
                @Override
                public boolean onEmotionButtonOnClickListener(View view) {
                    try {
                        if (udeskChatActivity.isblocked != null && udeskChatActivity.isblocked.equals("true")) {
                            udeskChatActivity.toBlockedView();
                            return true;
                        }
                        if (udeskChatActivity.isMoreThan20 && udeskChatActivity.isNeedQueueMessageSave()) {
                            UdeskUtils.showToast(udeskChatActivity.getApplicationContext(), udeskChatActivity.getMoreThanSendTip());
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
                                mEmojiImg.setImageResource(R.drawable.udesk_chat_emoj);
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
                            } else if (mMoreLayout.isShown() && !mEmotionlayout.isShown()) {
                                mMoreImg.setImageResource(R.drawable.udesk_chat_add);
                                return false;
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
            mEmotionlayout.setEmotionSelectedListener(udeskChatActivity);
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
                            setMoreVis(View.GONE);
                        } else {
                            sendBtn.setVisibility(View.GONE);
                            if (!(udeskChatActivity.imSetting != null && udeskChatActivity.imSetting.getEnable_web_im_feedback()) || TextUtils.equals(udeskChatActivity.curentStatus,UdeskConst.Status.chatting) || udeskChatActivity.isNeedQueueMessageSave()) {
                                setMoreVis(View.VISIBLE);
                            }
                        }

                        if (udeskChatActivity.isblocked.equals("true") ||
                                !udeskChatActivity.curentStatus.equals(UdeskConst.Status.chatting)) {
                            return;
                        }
                        if (TextUtils.isEmpty(mInputEditView.getText().toString())) {
                            if (udeskViewMode != null) {
                                udeskViewMode.getSendMessageLiveData().sendPreMessage("");
                            }
                            return;
                        }
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - preMsgSendTime > 500) {
                            preMsgSendTime = currentTime;
                            if (udeskViewMode != null) {
                                udeskViewMode.getSendMessageLiveData().sendPreMessage(mInputEditView.getText().toString());
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

    /**
     * 显示emoj布局
     */
    private void showEmotionLayout() {
        try {
            mEmotionlayout.setVisibility(View.VISIBLE);
            mEmojiImg.setImageResource(R.drawable.udesk_chat_emoj_keyboard);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏emoj布局
     */
    private void hideEmotionLayout() {
        try {
            mEmotionlayout.setVisibility(View.GONE);
            mEmojiImg.setImageResource(R.drawable.udesk_chat_emoj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏语音输入布局
     */
    private void hideAudioButton() {
        try {
            mBtnAudio.setVisibility(View.GONE);
            mInputEditView.setVisibility(View.VISIBLE);
            setEmojiVis(View.VISIBLE);
            mAudioImg.setImageResource(R.drawable.udesk_chat_voice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showMoreLayout() {
        try {
            mMoreLayout.setVisibility(View.VISIBLE);
            mMoreImg.setImageResource(R.drawable.udesk_chat_add_close);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏更多布局
     */
    @Override
    public void hideMoreLayout() {
        try {
            mMoreLayout.setVisibility(View.GONE);
            mMoreImg.setImageResource(R.drawable.udesk_chat_add);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置导航布局
     */
    @Override
    public void setNavigationViewVis() {
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
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseNavigationSurvy
                    && udeskChatActivity.mAgentInfo != null
                    && udeskChatActivity.imSetting != null
                    && udeskChatActivity.imSetting.getEnable_im_survey()
                    && udeskChatActivity.currentStatusIsOnline) {
                navigation_survy.setVisibility(View.VISIBLE);
            } else {
                navigation_survy.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addNavigationFragment() {
        try {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            NavigationFragment navigationFragment = new NavigationFragment();
            navigationFragment.setCurrentView(UdeskConst.CurrentFragment.agent);
            transaction.replace(R.id.fragment_view, navigationFragment);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取输入内容
     *
     * @return
     */
    @Override
    public CharSequence getInputContent() {
        try {
            if (mInputEditView != null) {
                return mInputEditView.getText();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 清空输入内容
     *
     * @return
     */
    @Override
    public void clearInputContent() {
        try {
            if (mInputEditView != null) {
                mInputEditView.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 回退键处理
     */
    @Override
    public void onBackPressed() {
        try {
            if (mEmotionlayout.isShown() || mMoreLayout.isShown()) {
                mEmotionKeyboard.interceptBackPress();
                hideEmotionLayout();
                hideMoreLayout();
            } else {
                udeskChatActivity.finishAcitivty();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清理资源
     */
    @Override
    public void cleanSource() {
        try {
            functionItems.clear();
            if (mBtnAudio != null) {
                mBtnAudio.setRecordingListener(null);
                mBtnAudio.destoryRelease();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 初始化功能界面
     */
    @Override
    public void initfunctionItems() {
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
            if (isOpenVideo() && udeskChatActivity.mAgentInfo != null) {
                FunctionMode videoItem = new FunctionMode(getString(R.string.video), UdeskConst.UdeskFunctionFlag.Udesk_Video, R.drawable.udesk_video_normal);
                functionItems.add(videoItem);
            }

            if (udeskChatActivity.mAgentInfo != null && udeskChatActivity.imSetting != null && udeskChatActivity.imSetting.getEnable_im_survey() && udeskChatActivity.currentStatusIsOnline) {
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

    /**
     * 底部框布局内部控件的显示隐藏
     *
     * @param vis
     */
    @Override
    public void setUdeskImContainerVis(int vis) {
        try {
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseVoice) {
                mAudioImg.setVisibility(vis);
                if (vis == View.GONE) {
                    mInputEditView.setVisibility(View.VISIBLE);
                    setEmojiVis(View.VISIBLE);
                    mBtnAudio.setVisibility(View.GONE);
                }
            }
            setEmojiVis(View.VISIBLE);
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseMore) {
                setMoreVis(vis);
                if (vis == View.GONE) {
                    hideMoreLayout();
                    mEmotionKeyboard.hideEmotionLayout(true);
                }else if (vis==View.VISIBLE){
                    if (mInputEditView.getText().toString().length()>0){
                        setMoreVis(View.GONE);
                        sendBtn.setVisibility(View.VISIBLE);
                    }else {
                        setMoreVis(View.VISIBLE);
                        sendBtn.setVisibility(View.GONE);
                    }
                }
            }
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseNavigationRootView) {
                if (vis == View.VISIBLE) {
                    setNavigationViewVis();
                } else {
                    navigationRootView.setVisibility(vis);
                }
            }
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
            return udeskChatActivity.imSetting != null
                    && udeskChatActivity.imSetting.getVcall()
                    && udeskChatActivity.imSetting.getSdk_vcall()
                    && UdeskUtil.isClassExists("udesk.udeskvideo.UdeskVideoActivity");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.udesk_fragment_agent;
    }

    /**
     * 初始化adapter
     */
    @Override
    public void initFunctionAdapter() {
        udeskFunctionAdapter = new UdeskFunctionAdapter(getActivity());
        funGridView.setAdapter(udeskFunctionAdapter);
        initfunctionItems();
        funGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    FunctionMode functionItem = (FunctionMode) adapterView.getItemAtPosition(i);
                    switch (functionItem.getId()) {
                        case UdeskConst.UdeskFunctionFlag.Udesk_Camera:
                            udeskChatActivity.clickCamera();
                            onHideBottomLayout(true);
                            break;
                        case UdeskConst.UdeskFunctionFlag.Udesk_Photo:
                            udeskChatActivity.clickPhoto();
                            onHideBottomLayout(true);
                            break;
                        case UdeskConst.UdeskFunctionFlag.Udesk_Udesk_File:
                            udeskChatActivity.clickFile();
                            onHideBottomLayout(true);
                            break;
                        case UdeskConst.UdeskFunctionFlag.Udesk_Survy:
                            if (udeskChatActivity.getPressionStatus()) {
                                UdeskUtils.showToast(getActivity().getApplicationContext(),
                                        getString(R.string.udesk_can_not_be_evaluated));
                                return;
                            }
                            udeskChatActivity.clickSurvy();
                            break;
                        case UdeskConst.UdeskFunctionFlag.Udesk_Location:
                            if (Build.VERSION.SDK_INT < 23) {
                                udeskChatActivity.clickLocation();
                                onHideBottomLayout(true);
                            } else {
                                XPermissionUtils.requestPermissions(getActivity(), RequestCode.LOCATION,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.READ_PHONE_STATE},
                                        new XPermissionUtils.OnPermissionListener() {
                                            @Override
                                            public void onPermissionGranted() {
                                                udeskChatActivity.clickLocation();
                                                onHideBottomLayout(true);
                                            }

                                            @Override
                                            public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                                Toast.makeText(getActivity(),
                                                        getResources().getString(R.string.location_denied),
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                            break;
                        case UdeskConst.UdeskFunctionFlag.Udesk_Video:
                            if (udeskChatActivity.getPressionStatus()) {
                                UdeskUtils.showToast(getActivity().getApplicationContext(), getString(R.string.udesk_can_not_be_video));
                                return;
                            }
                            udeskChatActivity.startVideo();
                            mBottomFramlayout.setVisibility(View.GONE);
                            onHideBottomLayout(true);
                            break;
                        default:
                            if (UdeskSDKManager.getInstance().getUdeskConfig().functionItemClickCallBack != null) {
                                UdeskSDKManager.getInstance().getUdeskConfig().functionItemClickCallBack
                                        .callBack(getActivity().getApplicationContext(), udeskViewMode, functionItem.getId(), functionItem.getName());
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        try {
            //检查是否处在可发消息的状态
            if (udeskChatActivity.isblocked != null && udeskChatActivity.isblocked.equals("true")) {
                udeskChatActivity.toBlockedView();
                return;
            }

            if (udeskChatActivity.isMoreThan20 && udeskChatActivity.isNeedQueueMessageSave()) {
                UdeskUtils.showToast(udeskChatActivity.getApplicationContext(),
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
                        XPermissionUtils.requestPermissions(udeskChatActivity, RequestCode.AUDIO,
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
                                        Toast.makeText(udeskChatActivity,
                                                getResources().getString(R.string.aduido_denied),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                }
            } else if (R.id.udesk_bottom_send == v.getId()) { //发送文本消息
                if (TextUtils.isEmpty(mInputEditView.getText().toString())) {
                    UdeskUtils.showToast(udeskChatActivity.getApplicationContext(),
                            getString(R.string.udesk_send_message_empty));
                    return;
                }
                if (TextUtils.equals(udeskChatActivity.curentStatus, UdeskConst.Status.chatting) || udeskChatActivity.getPressionStatus() || udeskChatActivity.isNeedQueueMessageSave()) {
                    udeskViewMode.sendTxtMessage(getInputContent().toString());
                    if (!udeskChatActivity.getPressionStatus()) {
                        clearInputContent();
                    }

                } else if (udeskChatActivity.imSetting != null && udeskChatActivity.imSetting.getEnable_web_im_feedback()) {
                    if (udeskChatActivity.isleaveMessageTypeMsg()) {
                        if (!udeskViewMode.isLeavingMsg()) {
                            udeskChatActivity.addCustomerLeavMsg();
                            udeskViewMode.setLeavingMsg(true);
                        }
                        if (udeskChatActivity.imSetting.getLeave_message_type().equals(UdeskConst.LeaveMsgType.directMsg)) {
                            udeskViewMode.sendLeaveMessage(mInputEditView.getText().toString());
                        } else if (udeskChatActivity.imSetting.getLeave_message_type().equals(UdeskConst.LeaveMsgType.imMsg)) {
                            udeskViewMode.sendIMLeaveMessage(mInputEditView.getText().toString());
                        }
                        clearInputContent();
                    } else {
                        udeskChatActivity.confirmToForm();
                    }
                }
            } else if (R.id.navigation_survy == v.getId()) {
                if (udeskChatActivity.getPressionStatus()) {
                    UdeskUtils.showToast(udeskChatActivity.getApplicationContext(),
                            getString(R.string.udesk_can_not_be_evaluated));
                    return;
                }
                udeskChatActivity.clickSurvy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }

    /**
     * 显示录音button
     */
    public void showAudioButton() {
        try {
            mBtnAudio.setVisibility(View.VISIBLE);
            mInputEditView.setVisibility(View.GONE);
            setEmojiVis(View.GONE);
            mAudioImg.setImageResource(R.drawable.udesk_chat_voice_keyboard);

            if (mBottomFramlayout.isShown()) {
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


    // 判断可发送消息
    private boolean isShowNotSendMsg() {
        try {

            if (!UdeskUtils.isNetworkConnected(getActivity().getApplicationContext())) {
                UdeskUtils.showToast(getActivity().getApplicationContext(),
                        getResources().getString(R.string.udesk_has_wrong_net));
                return false;
            }
            if (udeskChatActivity.curentStatus.equals(UdeskConst.Status.over) || udeskChatActivity.initCustomer == null) {
                udeskChatActivity.reCreateIMCustomerInfo();
                return false;
            }
            if (udeskChatActivity.isMoreThan20 && udeskChatActivity.isNeedQueueMessageSave()) {
                UdeskUtils.showToast(getActivity().getApplicationContext(),
                        getResources().getString(R.string.udesk_in_the_line_max_send));
                return false;
            }

            if (udeskChatActivity.curentStatus.equals(UdeskConst.Status.init)) {
                UdeskUtils.showToast(getActivity().getApplicationContext(), getResources().getString(R.string.udesk_agent_inti));
                return false;
            }

            if (!TextUtils.isEmpty(udeskChatActivity.pre_session_id)) {
                return true;
            }
            if (udeskChatActivity.isNeedQueueMessageSave()) {
                return true;
            }

            if (!TextUtils.equals(udeskChatActivity.curentStatus, UdeskConst.Status.chatting) && !udeskChatActivity.isleaveMessageTypeMsg()) {
                udeskChatActivity.confirmToForm();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
