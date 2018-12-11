package cn.udesk;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.activity.UdeskFormActivity;
import cn.udesk.activity.UdeskHelperActivity;
import cn.udesk.activity.UdeskOptionsAgentGroupActivity;
import cn.udesk.activity.UdeskRobotActivity;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.emotion.LQREmotionKit;
import cn.udesk.messagemanager.UdeskMessageManager;
import cn.udesk.model.SDKIMSetting;
import cn.udesk.presenter.MessageCache;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.model.MessageInfo;
import udesk.core.utils.UdeskUtils;


public class UdeskSDKManager {

    private int countSettingReq;

    /**
     * 注册udesk系统生成的二级域名
     */
    public static String domain = "";
    /**
     * udesk系统创建应用生成的App Id
     */
    public static String app_Id = "";

    /**
     * udesk系统创建应用生成的App Key
     */
    public static String app_Key = "";

    /**
     * 用户唯一的标识
     */
    private String sdkToken = null;

    private static UdeskSDKManager instance = new UdeskSDKManager();

    private UdeskConfig udeskConfig;

    //多应用 配置选项mode
    private SDKIMSetting imSetting;

    private UdeskSDKManager() {
    }

    public static UdeskSDKManager getInstance() {
        return instance;
    }

    /**
     * 创建应用生成的key值和appid
     *
     * @param context
     * @param domain  注册udesk系统生成的二级域名
     * @param appkey  udesk系统创建应用生成的App Key
     * @param appid   udesk系统创建应用生成的App ID
     */
    public void initApiKey(Context context, String domain, String appkey, String appid) {
        try {
            UdeskSDKManager.domain = domain;
            UdeskSDKManager.app_Key = appkey;
            UdeskSDKManager.app_Id = appid;
            if (UdeskConfig.isUseShare) {
                PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                        UdeskConst.SharePreParams.Udesk_Domain, domain);
                PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                        UdeskConst.SharePreParams.Udesk_App_Key, app_Key);
                PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                        UdeskConst.SharePreParams.Udesk_App_Id, app_Id);
            }
            LQREmotionKit.init(context.getApplicationContext());
            UdeskUtil.frescoInit(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //如果没有传入UdeskConfig  则使用Udesk提供的默认配置
    public UdeskConfig getUdeskConfig() {
        if (udeskConfig == null) {
            udeskConfig = UdeskConfig.createDefualt();
        }
        return udeskConfig;
    }

    /**
     * 直接进入帮助中心页面
     *
     * @param context
     */
    public void toLanuchHelperAcitivty(Context context, UdeskConfig udeskConfig) {
        try {
            if (this.udeskConfig == null) {
                this.udeskConfig = udeskConfig;
            }
            Intent intent = new Intent(context.getApplicationContext(), UdeskHelperActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //启动留言界面
    public void goToForm(Context context, UdeskConfig udeskConfig) {
        try {
            if (this.udeskConfig == null) {
                this.udeskConfig = udeskConfig;
            }
            if (udeskConfig.formCallBack != null) {
                udeskConfig.formCallBack.toLuachForm(context);
                return;
            }
            Intent intent = new Intent(context,
                    UdeskFormActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //进入会话唯一入口,必须配置,根据配置展示会话
    public void entryChat(Context context, UdeskConfig udeskConfig, String sdktoken) {
        try {
            if (udeskConfig == null) {
                Toast.makeText(context, "UdeskConfig is null", Toast.LENGTH_LONG).show();
                return;
            }
            if (TextUtils.isEmpty(getAppId(context))) {
                showConversationByImGroup(context);
                return;
            }
            if (TextUtils.isEmpty(sdktoken)) {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.udesk_no_sdktoken), Toast.LENGTH_SHORT).show();
                return;
            }
            this.udeskConfig = udeskConfig;
            String cacheToken = getSdkToken(context);
            sdkToken = UdeskUtil.stringFilter(sdktoken);
            if ((cacheToken == null)) {
                disConnectXmpp();
            } else if (!cacheToken.equals(sdkToken)) {
                // 一个应用内切换用户，的关闭上个用户的推送
                if (!TextUtils.isEmpty(UdeskBaseInfo.registerId) && getUdeskConfig().isUserSDkPush) {
                    setSdkPushStatus(getDomain(context), getAppkey(context), getSdkToken(context),
                            UdeskConfig.UdeskPushFlag.OFF, UdeskBaseInfo.registerId, getAppId(context));
                }
                disConnectXmpp();
            }
            if (udeskConfig.defualtUserInfo != null) {
                udeskConfig.defualtUserInfo.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, sdktoken);
            }
            initDB(context, sdkToken);
            PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                    UdeskConst.SharePreParams.Udesk_SdkToken, sdkToken);
            UdeskUtil.initCrashReport(context);
            getSDKImSetting(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取应用的设置项配置值
     *
     * @param context
     */
    private void getSDKImSetting(final Context context) {
        try {
            UdeskHttpFacade.getInstance().getIMSettings(getDomain(context), getAppkey(context), getSdkToken(context),
                    getAppId(context), new UdeskCallBack() {
                        @Override
                        public void onSuccess(String message) {
                            try {
                                imSetting = JsonUtils.parserIMSettingJson(message);
                                switchBySetting(context, imSetting);
                                countSettingReq = 0;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String message) {
                            try {
                                if (countSettingReq >= 2) {
                                    countSettingReq = 0;
                                    if (imSetting != null) {
                                        switchBySetting(context, imSetting);
                                    } else {
                                        if (!getUdeskConfig().isOnlyUseRobot) {
                                            showConversationByImGroup(context);
                                        } else {
                                            Toast.makeText(context.getApplicationContext(), context.getApplicationContext().getString(R.string.udesk_has_bad_net), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    countSettingReq++;
                                    getSDKImSetting(context);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 分配会话的逻辑
     *
     * @param context
     * @param imSetting
     */
    private void switchBySetting(Context context, SDKIMSetting imSetting) {
        try {
            if (imSetting != null) {
                if (getUdeskConfig().isOnlyUseRobot) {
                    if (imSetting.getEnable_robot()) {
                        showRobotByConfigSetting(context, imSetting);
                        return;
                    }
                } else {
                    if (imSetting.getIn_session() || getUdeskConfig().isOnlyByAgentId || getUdeskConfig().isOnlyByGroupId) {
                        toLanuchChatAcitvity(context);
                        return;
                    }
                    if (imSetting.getEnable_robot()) {
                        showRobotByConfigSetting(context, imSetting);
                        return;
                    }
                    showConversationByImGroup(context);
                }
            } else {
                Toast.makeText(context, context.getString(R.string.udesk_has_bad_net), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 进入机器人聊天会话页面
     *
     * @param context
     */
    private void showRobotByConfigSetting(final Context context, final SDKIMSetting imSetting) {

        try {
            UdeskHttpFacade.getInstance().setUserInfo(context, getDomain(context),
                    getAppkey(context), getSdkToken(context),
                    getUdeskConfig().defualtUserInfo, getUdeskConfig().definedUserTextField,
                    getUdeskConfig().definedUserRoplist, getAppId(context), getUdeskConfig().channel, new UdeskCallBack() {

                        @Override
                        public void onSuccess(String string) {
                            try {
                                if (getUdeskConfig().isOnlyUseRobot) {

                                    if (!TextUtils.isEmpty(imSetting.getRobot())) {
                                        toLanuchRobotAcitivty(context, imSetting.getRobot(), "false", false);
                                    }
                                } else {
                                    if (!TextUtils.isEmpty(imSetting.getRobot())) {
                                        toLanuchRobotAcitivty(context, imSetting.getRobot(), imSetting.getEnable_agent(), imSetting.getEnable_im_group());
                                    } else {
                                        showConversationByImGroup(context);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFail(String string) {
                            try {
                                if (!getUdeskConfig().isOnlyUseRobot) {
                                    showConversationByImGroup(context);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            showConversationByImGroup(context);
        }
    }

    /**
     * 进入机器人页面
     *
     * @param context
     * @param url                 机器人的连接地址
     * @param tranfer             是否可以转人工
     * @param isTransferByImGroup 转人工是否通过导航页进入  true表示是 false 表示不是
     */
    private void toLanuchRobotAcitivty(Context context, String url, String tranfer, boolean isTransferByImGroup) {
        try {
            Intent intent = new Intent(context, UdeskRobotActivity.class);
            intent.putExtra(UdeskConst.UDESKTRANSFER, tranfer);
            intent.putExtra(UdeskConst.UDESKHTMLURL, url);
            intent.putExtra(UdeskConst.UDESKISTRANFERSESSION, isTransferByImGroup);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过配置的导航页的客服组进入,分配进入相应的客服
     *
     * @param context
     */
    private void showConversationByImGroup(Context context) {
        try {
            if (getUdeskConfig().isOnlyByAgentId || getUdeskConfig().isOnlyByGroupId) {
                toLanuchChatAcitvity(context);
            } else if (imSetting != null && imSetting.getEnable_im_group()) {
                Intent intent = new Intent(context, UdeskOptionsAgentGroupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                //没有开启导航组进入，得清楚groupid,agentid
                PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                        UdeskConst.SharePreParams.Udesk_Group_Id, "");
                PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                        UdeskConst.SharePreParams.Udesk_Agent_Id, "");
                toLanuchChatAcitvity(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 直接进入人工客服聊天页面
     *
     * @param context
     */
    private void toLanuchChatAcitvity(Context context) {
        try {
            Intent intent = new Intent(context, UdeskChatActivity.class);
            if (!TextUtils.isEmpty(getUdeskConfig().groupId)) {
                intent.putExtra(UdeskConst.UDESKGROUPID, getUdeskConfig().groupId);
            }
            if (!TextUtils.isEmpty(getUdeskConfig().agentId)) {
                intent.putExtra(UdeskConst.UDESKAGENTID, getUdeskConfig().agentId);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前会话未读消息的记录
     */
    public int getCurrentConnectUnReadMsgCount(Context context, String sdkToken) {
        try {
            initDB(context.getApplicationContext(), sdkToken);
            return UdeskDBManager.getInstance().getUnReadMessageCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取未读消息的结合
     *
     * @return
     */
    public List<MessageInfo> getUnReadMessages(Context context, String sdkToken) {
        initDB(context.getApplicationContext(), sdkToken);
        return UdeskDBManager.getInstance().getUnReadMessages();
    }

    /**
     * 删除当前用户的聊天数据
     */
    public void deleteMsg(Context context, String token) {
        initDB(context.getApplicationContext(), sdkToken);
        UdeskDBManager.getInstance().deleteAllMsg();
    }

    /**
     * 初始话DB
     *
     * @param context
     * @param sdkToken
     */
    public void initDB(Context context, String sdkToken) {

        try {
            if (UdeskDBManager.getInstance().isNeedInit(sdkToken)) {
                releaseDB();
                UdeskDBManager.getInstance().init(context, sdkToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 退出后的 资源释放
     */
    public void logoutUdesk() {
        try {
            if (MessageCache.getInstance() != null){
                MessageCache.getInstance().clear();
            }
            releaseDB();
            disConnectXmpp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 销毁DB
     */
    public void releaseDB() {
        try {
            UdeskDBManager.getInstance().release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 断开xmpp连接
     */
    public void disConnectXmpp() {
        try {
            UdeskMessageManager.getInstance().cancleXmpp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //获取注册推送的唯一ID
    public String getRegisterId(Context context) {
        try {
            if (TextUtils.isEmpty(UdeskBaseInfo.registerId)) {
                return PreferenceHelper.readString(context, UdeskConst.SharePreParams.RegisterIdName, UdeskConst.SharePreParams.Udesk_Push_RegisterId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return UdeskBaseInfo.registerId;
    }

    //保存注册推送的的唯一ID
    public void setRegisterId(Context context, String registerId) {
        try {
            UdeskBaseInfo.registerId = registerId;
            PreferenceHelper.write(context, UdeskConst.SharePreParams.RegisterIdName,
                    UdeskConst.SharePreParams.Udesk_Push_RegisterId, registerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param domain
     * @param key
     * @param sdkToken
     * @param status         sdk推送状态 ["on" | "off"]
     * @param registrationID 注册推送设备的ID
     * @param appid
     * @param callBack
     */
    public void setSdkPushStatus(String domain, String key, String sdkToken, String status, String registrationID, String appid, UdeskCallBack callBack) {
        try {
            UdeskHttpFacade.getInstance().sdkPushStatus(domain, key, sdkToken, status, registrationID, appid, callBack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param domain         公司注册生成的域名
     * @param key            创建app时，生成的app key
     * @param sdkToken       用户唯一标识
     * @param status         sdk推送状态 ["on" | "off"]
     * @param registrationID 注册推送设备的ID
     * @param appid          创建app时，生成的app id
     */

    public void setSdkPushStatus(String domain, String key, String sdkToken, String status, String registrationID, String appid) {
        try {
            setSdkPushStatus(domain, key, sdkToken, status, registrationID, appid, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 控制控制台日志的开关
     *
     * @param isShow true 开启 false 关闭
     */
    public void isShowLog(boolean isShow) {
        UdeskConst.xmppDebug = isShow;
        UdeskConst.isDebug = isShow;
    }


    public String getSdkToken(Context context) {
        if (!TextUtils.isEmpty(sdkToken)) {
            return sdkToken;
        }
        return PreferenceHelper.readString(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_SdkToken);
    }

    public String getDomain(Context context) {
        try {
            if (!TextUtils.isEmpty(UdeskSDKManager.domain)) {
                return UdeskSDKManager.domain;
            }
            if (UdeskConfig.isUseShare) {
                UdeskSDKManager.domain = PreferenceHelper.readString(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_Domain);
                return UdeskSDKManager.domain;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getAppkey(Context context) {
        try {
            if (!TextUtils.isEmpty(UdeskSDKManager.app_Key)) {
                return UdeskSDKManager.app_Key;
            }
            if (UdeskConfig.isUseShare) {
                UdeskSDKManager.app_Key = PreferenceHelper.readString(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_App_Key);
                return UdeskSDKManager.app_Key;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getAppId() {
        try {
            if (!TextUtils.isEmpty(UdeskSDKManager.app_Id)) {
                return UdeskSDKManager.app_Id;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getAppId(Context context) {
        try {
            if (!TextUtils.isEmpty(UdeskSDKManager.app_Id)) {
                return UdeskSDKManager.app_Id;
            }
            if (UdeskConfig.isUseShare) {
                UdeskSDKManager.app_Id = PreferenceHelper.readString(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_App_Id);
                return UdeskSDKManager.app_Id;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //如果确定不要使用上次groupId的缓存，则需主动清理缓存
    //在某些场景下，会出现没法传入groupid，如果通过推送消息系统推送的消息取值，没有
    // 会话存在的时候，不会直接进入对话界面，会话界面后，客服关闭会话，重新发起会话
    public void cleanCacheGroupId(Context context) {
        try {
            PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                    UdeskConst.SharePreParams.Udesk_Group_Id, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public SDKIMSetting getImSetting() {
        return imSetting;
    }


    //先预留开关，当后期管理员可配置时，方便修改
    public boolean getEnableSendMessageWhenQueue() {
        return true;
    }


}
