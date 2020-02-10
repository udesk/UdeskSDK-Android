package cn.udesk.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.udesk.R;
import cn.udesk.UdeskAssociate;
import cn.udesk.UdeskSDKManager;
import cn.udesk.UdeskUtil;
import cn.udesk.activity.NavigationFragment;
import cn.udesk.adapter.UdeskFunctionAdapter;
import cn.udesk.emotion.EmotionKeyboard;
import cn.udesk.emotion.EmotionLayout;
import cn.udesk.emotion.LQREmotionKit;
import cn.udesk.model.FunctionMode;
import cn.udesk.permission.RequestCode;
import cn.udesk.permission.XPermissionUtils;
import udesk.core.UdeskConst;
import udesk.core.event.InvokeEventContainer;
import udesk.core.utils.UdeskUtils;

public class UdeskRobotFragment extends UdeskbaseFragment implements View.OnClickListener {

    private LinearLayout navigationRootView, addNavigationFragmentView, navigation_survy;
    private ImageView mAudioImg; //语言和内容
    private EditText mInputEditView;
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
    private int count=0;//连续请求5次请求停止

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        try {
            navigationRootView = view.findViewById(R.id.navigation_root_view);
            addNavigationFragmentView = view.findViewById(R.id.fragment_view);
            navigation_survy = view.findViewById(R.id.navigation_survy);
            mAudioImg = (ImageView) view.findViewById(R.id.udesk_img_audio);
            mInputEditView = (EditText) view.findViewById(R.id.udesk_bottom_input);
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            InvokeEventContainer.getInstance().eventui_OnHideLayout.bind(this,"onHideBottomLayout");
            InvokeEventContainer.getInstance().event_OnAudioResult.bind(this,"onAudioResult");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        try {
            InvokeEventContainer.getInstance().eventui_OnHideLayout.unBind(this);
            InvokeEventContainer.getInstance().event_OnAudioResult.unBind(this);

        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * 语音识别返回数据处理
     * @param s
     */
    public void onAudioResult(String s) {
        try {
            if (!TextUtils.isEmpty(s)) {
                mInputEditView.setText(s);
                sendMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    /**
     * 隐藏表情 更多布局 图标恢复默认
     * @param isHide
     */
    public void onHideBottomLayout(Boolean isHide){
        try {
            if (isHide){
                mBottomFramlayout.setVisibility(View.GONE);
                hideEmotionLayout();
                hideMoreLayout();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            udeskViewMode.getRobotApiData().initRobot(udeskChatActivity);
            initFunctionAdapter();
            setNavigationViewVis();
            if (isOpenAudio()) {
                mAudioImg.setVisibility(View.VISIBLE);
            } else {
                mAudioImg.setVisibility(View.GONE);
            }
            showEmoji();
            initEmotionKeyboard();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    /**
     * 是否打开语音识别
     *
     * @return
     */
    private boolean isOpenAudio() {
        try {
            return  UdeskUtil.isClassExists("udesk.udeskasr.activity.UdeskASRActivity");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
                                    return true;
                                }
                            } else if (mEmotionlayout.isShown() && !mMoreLayout.isShown()) {
                                mEmojiImg.setImageResource(R.drawable.udesk_chat_emoj);
                                return false;
                            }
                            showEmotionLayout();
                            hideMoreLayout();
                        } else if (i == R.id.udesk_more_img) {
                            if (!mMoreLayout.isShown()) {
                                if (mEmotionlayout.isShown()) {
                                    showMoreLayout();
                                    hideEmotionLayout();
                                    return true;
                                }
                            }else if (mMoreLayout.isShown()&&!mEmotionlayout.isShown()){
                                mMoreImg.setImageResource(R.drawable.udesk_chat_add);
                                return false;
                            }
                            showMoreLayout();
                            hideEmotionLayout();
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
                            mMoreImg.setVisibility(View.GONE);
                        } else {
                            sendBtn.setVisibility(View.GONE);
                            udeskChatActivity.isShowAssociate(false);
                            mMoreImg.setVisibility(View.VISIBLE);
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
                public void afterTextChanged(final Editable s) {
                    try {
                        if (TextUtils.isEmpty(s.toString())) {
                            UdeskAssociate.getmInstance().cancel();
                            return;
                        }
                        if (UdeskAssociate.getmInstance().getFuture() != null){
                            return;
                        }
                        UdeskAssociate.getmInstance().scheduleWithFixedDelay(new Runnable() {
                            @Override
                            public void run() {
                                if (UdeskAssociate.getmInstance().compareText(s.toString())) {
                                    if (count >=2) {
                                        UdeskAssociate.getmInstance().cancel();
                                        udeskViewMode.getRobotApiData().robotTips(s.toString());
                                    }
                                    count++;

                                } else {
                                    count = 0;
                                }
                            }
                        }, 0, 300, TimeUnit.MILLISECONDS);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
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
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseRobotNavigationRootView) {
                navigationRootView.setVisibility(View.VISIBLE);
            } else {
                navigationRootView.setVisibility(View.GONE);
            }
            if (UdeskSDKManager.getInstance().getUdeskConfig().robotnavigationModes != null
                    && UdeskSDKManager.getInstance().getUdeskConfig().robotnavigationModes.size() > 0) {
                addNavigationFragment();
            }
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseNavigationSurvy ) {
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
            NavigationFragment navigationFragment=new NavigationFragment();
            navigationFragment.setCurrentView(UdeskConst.CurrentFragment.robot);
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
        functionItems.clear();
    }


    /**
     * 初始化功能界面
     */
    @Override
    public void initfunctionItems() {
        try {
            functionItems.clear();
            FunctionMode survyItem = new FunctionMode(getString(R.string.survy), UdeskConst.UdeskFunctionFlag.Udesk_Survy, R.drawable.udesk_survy_normal);
            functionItems.add(survyItem);
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
                    mEmojiImg.setVisibility(View.VISIBLE);
                }
            }
            showEmoji();
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseMore) {
                mMoreImg.setVisibility(vis);
                if (vis == View.GONE) {
                    hideMoreLayout();
                }else if (vis==View.VISIBLE){
                    sendBtn.setVisibility(View.GONE);
                }
            }
            if (UdeskSDKManager.getInstance().getUdeskConfig().isUseNavigationRootView) {
                navigationRootView.setVisibility(vis);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * emoji的显示隐藏
     */
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

    @Override
    protected int getLayoutId() {
        return R.layout.udesk_fragment_robot;
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
                        case UdeskConst.UdeskFunctionFlag.Udesk_Survy:
                            udeskChatActivity.clickRobotSurvy();
                            break;
                        default:
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
                        getString(R.string.udesk_in_the_line_max_send));
                return;
            }

            if (!isShowNotSendMsg()) {
                mEmotionKeyboard.hideSoftInput();
                return;
            }
            if (v.getId() == R.id.udesk_img_audio) {
                if (UdeskUtil.isClassExists("udesk.udeskasr.activity.UdeskASRActivity")){
                    if (Build.VERSION.SDK_INT < 23) {
                        goToASR();
                    } else {
                        XPermissionUtils.requestPermissions(udeskChatActivity, RequestCode.ASR,
                                new String[]{Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.INTERNET,
                                        Manifest.permission.READ_PHONE_STATE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                new XPermissionUtils.OnPermissionListener() {
                                    @Override
                                    public void onPermissionGranted() {
                                        goToASR();
                                    }

                                    @Override
                                    public void onPermissionDenied(String[] deniedPermissions, boolean alwaysDenied) {
                                        Toast.makeText(udeskChatActivity,
                                                getString(R.string.aduido_denied),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }else {
                    UdeskUtils.showToast(udeskChatActivity,getString(R.string.udesk_asr_close));
                }

            } else if (R.id.udesk_bottom_send == v.getId()) {
                sendMessage();
            } else if (R.id.navigation_survy == v.getId()) {
                udeskChatActivity.clickRobotSurvy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }

    }
    private void sendMessage() {
        try {
            if (TextUtils.isEmpty(mInputEditView.getText().toString())) {
                UdeskUtils.showToast(udeskChatActivity.getApplicationContext(),
                        getString(R.string.udesk_send_message_empty));
                return;
            }
            udeskChatActivity.isShowAssociate(false);
            udeskViewMode.getRobotApiData().sendTxtMsg(mInputEditView.getText().toString());
            mInputEditView.setText("");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 显示录音button
     */
    public void goToASR() {
        try {
            hideEmotionLayout();
            hideMoreLayout();
            if (mBottomFramlayout.isShown()) {
                if (mEmotionKeyboard != null) {
                    mEmotionKeyboard.interceptBackPress();
                }
            } else {
                if (mEmotionKeyboard != null) {
                    mEmotionKeyboard.hideSoftInput();
                }
            }
            Intent intent = new Intent();
            intent.setClass(udeskChatActivity.getApplicationContext(), Class.forName("udesk.udeskasr.activity.UdeskASRActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
            udeskChatActivity.overridePendingTransition(R.anim.udesk_pop_enter_anim,0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 判断可发送消息
    private boolean isShowNotSendMsg() {
        try {
            if (!UdeskUtils.isNetworkConnected(getActivity().getApplicationContext())) {
                UdeskUtils.showToast(getActivity().getApplicationContext(),
                        getString(R.string.udesk_has_wrong_net));
                return false;
            }
            if (udeskChatActivity.curentStatus.equals(UdeskConst.Status.init)) {
                UdeskUtils.showToast(getActivity().getApplicationContext(), getString(R.string.udesk_agent_inti));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
