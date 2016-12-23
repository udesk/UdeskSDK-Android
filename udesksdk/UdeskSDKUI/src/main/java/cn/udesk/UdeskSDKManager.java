package cn.udesk;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.udesk.activity.OptionsAgentGroupActivity;
import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.activity.UdeskFormActivity;
import cn.udesk.activity.UdeskHelperActivity;
import cn.udesk.activity.UdeskRobotActivity;
import cn.udesk.config.UdeskConfig;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.messagemanager.UdeskMessageManager;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.widget.UdeskDialog;
import rx.Subscriber;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskCoreConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.model.MessageInfo;
import udesk.core.utils.UdeskUtils;


public class UdeskSDKManager {


    private static String userId = null;
    private static String transfer = null;
    private static String h5Url = null;
    private UdeskCommodityItem commodity = null;
    private UdeskDialog dialog;
    //    private static UdeskSDKManager instance;
    private static UdeskSDKManager instance = new UdeskSDKManager();
    /**
     * 保存客户的头像地址，由用户app传递
     */
    private static String customerUrl = null;
    private boolean isNeedMsgNotice = true;

    //表示上次会话是否还存在  默认是不存在的
    public static boolean isSessioning = false;

    //文本消息中的链接消息的点击事件的拦截回调。 包含表情的不会拦截回调。
    private ITxtMessageWebonCliclk txtMessageClick;

    private UdeskSDKManager() {
    }


    public static UdeskSDKManager getInstance() {
        return instance;
    }


//    public static UdeskSDKManager getInstance() {
//        if (instance == null) {
//            synchronized (UdeskSDKManager.class) {
//                if (instance == null) {
//                    instance = new UdeskSDKManager();
//                }
//            }
//        }
//        return instance;
//    }


    /**
     * 老的初始模式，key值是单点登录的共享密钥
     *
     * @param context
     * @param domain    注册的二级域名
     * @param secretKey 单点登录的key值
     */
    public void initApiKey(Context context, String domain, String secretKey) {
        UdeskConfig.domain = domain;
        UdeskConfig.secretKey = secretKey;
        UdeskUtil.initImageLoaderConfig(context);
        initCrashReport(context);
    }


    public void initCrashReport(Context context) {
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setAppVersion(UdeskCoreConst.sdkversion);
        CrashReport.initCrashReport(context, UdeskCoreConst.buglyAppid, false, strategy);
    }

    /**
     * 创建应用生成的key值和appid
     *
     * @param context
     * @param domain    注册的二级域名
     * @param secretKey 支持多应用和推送的appkey
     * @param appid     支持多应用和推送的appid
     */
    public void initApiKey(Context context, String domain, String secretKey, String appid) {
        UdeskConfig.domain = domain;
        UdeskConfig.secretKey = secretKey;
        UdeskConfig.appid = appid;
        UdeskUtil.initImageLoaderConfig(context);
    }

    /**
     * @param context
     * @param sdkToken 用户唯一的标识，最好填写数字和英文字母，最好不要包含特殊字符
     * @param info     包含默认提供的用户信息
     */
    public void setUserInfo(Context context, String sdkToken, Map<String, String> info) {
        this.setUserInfo(context, sdkToken, info, null);
    }

    /**
     * @param context
     * @param sdkToken  用户唯一的标识，最好填写数字和英文字母，最好不要包含特殊字符
     * @param info      包含默认提供的用户信息
     * @param textField 包含自定义的文本信息
     */
    public void setUserInfo(Context context, String sdkToken, Map<String, String> info, Map<String, String> textField) {
        this.setUserInfo(context, sdkToken, info, textField, null);
    }

    /**
     * @param context
     * @param token     用户唯一的标识，最好填写数字和英文字母，最好不要包含特殊字符
     * @param info      包含默认提供的用户信息
     * @param textField 包含自定义的文本信息
     * @param roplist   包含自定义的列表信息
     */
    public void setUserInfo(final Context context, String token, Map<String, String> info, Map<String, String> textField, Map<String, String> roplist) {

        String cacheToken = getSdkToken(context);
        if ((cacheToken == null) || (cacheToken != null && !cacheToken.equals(token))) {
            if (!TextUtils.isEmpty(UdeskConfig.registerId) && UdeskConfig.isUserSDkPush) {
                setSdkPushStatus(UdeskConfig.domain, UdeskConfig.secretKey, UdeskConfig.sdkToken, "off", UdeskConfig.registerId, UdeskConfig.appid);
            }
            clean(context);
            disConnectXmpp();
        }

        isShowLog(true);
        UdeskConfig.sdkToken = token;
        initDB(context, UdeskConfig.sdkToken);
        PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                UdeskConst.SharePreParams.Udesk_SdkToken, UdeskConfig.sdkToken);
        if (info == null) {
            info = new HashMap<String, String>();
        }
        UdeskConfig.userinfo = info;
        UdeskConfig.userinfo.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, UdeskConfig.sdkToken);
        UdeskConfig.textField = textField;
        UdeskConfig.roplist = roplist;
    }

    /**
     * 直接进入人工客服聊天页面
     *
     * @param context
     */
    public void toLanuchChatAcitvity(Context context) {
        Intent intent = new Intent(context, UdeskChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 直接进入帮助中心页面
     *
     * @param context
     */
    public void toLanuchHelperAcitivty(Context context) {
        Intent intent = new Intent(context, UdeskHelperActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 进入机器人聊天会话页面
     *
     * @param context
     */
    public void showRobot(final Context context) {

        showLoading(context);
        UdeskHttpFacade.getInstance().setUserInfo(context, getDomain(context),
                getSecretKey(context), getSdkToken(context),
                getUserinfo(), getTextField(),
                UdeskSDKManager.getInstance().getRoplist(), UdeskConfig.appid, new UdeskCallBack() {

                    @Override
                    public void onSuccess(String string) {
                        JsonUtils.parserCustomersJson(context, string);
                        dismiss();
                        if (!TextUtils.isEmpty(getH5Url(context))) {
                            toLanuchRobotAcitivty(context, getH5Url(context), getTransfer(context), true);
                        } else {
                            UdeskUtils.showToast(context, context.getString(R.string.udesk_has_not_open_robot));
                        }
                    }

                    @Override
                    public void onFail(String string) {
                        UdeskUtils.showToast(context, string);
                    }
                });
    }


    /**
     * 上次的会话没有关闭，则直接进入人工会话；
     * 如果没有上次会话记录，则判断有没机器人权限，
     * 有则进入机器人会话，没有则进入人工会话
     *
     * @param context
     */
    public void showRobotOrConversation(final Context context) {
        if (isSessioning && UdeskConfig.isDirectAccessToSession) {
            toLanuchChatAcitvity(context);
            return;
        }
        showLoading(context);
        UdeskHttpFacade.getInstance().setUserInfo(context, getDomain(context),
                getSecretKey(context), getSdkToken(context),
                getUserinfo(), getTextField(),
                UdeskSDKManager.getInstance().getRoplist(), UdeskConfig.appid, new UdeskCallBack() {

                    @Override
                    public void onSuccess(String string) {
                        String robotUrl = JsonUtils.parserCustomersJson(context, string);
                        dismiss();
                        if (!TextUtils.isEmpty(robotUrl)) {
                            toLanuchRobotAcitivty(context, robotUrl, getTransfer(context), false);
                        } else {
                            toLanuchChatAcitvity(context);
                        }
                    }

                    @Override
                    public void onFail(String string) {
                        dismiss();
                        toLanuchChatAcitvity(context);
                    }
                });
    }

    /**
     * 上次的会话没有关闭，则直接进入人工会话；
     * 如果没有上次会话记录，则判断有没机器人权限，
     * 有则进入机器人会话，没有则通过导航页进入人工会话
     *
     * @param context
     */
    public void showRobotOrConversationByImGroup(final Context context) {
        if (isSessioning) {
            showConversationByImGroup(context);
            return;
        }
        showLoading(context);
        UdeskHttpFacade.getInstance().setUserInfo(context, getDomain(context),
                getSecretKey(context), getSdkToken(context),
                getUserinfo(), getTextField(),
                UdeskSDKManager.getInstance().getRoplist(), UdeskConfig.appid, new UdeskCallBack() {

                    @Override
                    public void onSuccess(String string) {
                        String robotUrl = JsonUtils.parserCustomersJson(context, string);
                        dismiss();
                        if (!TextUtils.isEmpty(robotUrl)) {
                            toLanuchRobotAcitivty(context, robotUrl, getTransfer(context), true);
                        } else {
                            showConversationByImGroup(context);
                        }
                    }

                    @Override
                    public void onFail(String string) {
                        dismiss();
                        showConversationByImGroup(context);
                    }
                });
    }

    /**
     * 指引客户选择客服组，如果记录了上次会话记录还存在，则直接进入人工会话，否则进入相应的指引客户选择的页面
     *
     * @param context
     */
    public void showConversationByImGroup(Context context) {
        if (isSessioning && UdeskConfig.isDirectAccessToSession) {
            toLanuchChatAcitvity(context);
            return;
        }
        Intent intent = new Intent(context, OptionsAgentGroupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 指定客服 id 进行分配 进行会话
     *
     * @param context
     * @param agentId 客服ID
     */
    public void lanuchChatByAgentId(Context context, String agentId) {
        lanuchChatByConfirmId(context, "", agentId);
    }

    /**
     * @param context
     * @param groupId 指定客服组 id 进行分配
     */
    public void lanuchChatByGroupId(Context context, String groupId) {
        lanuchChatByConfirmId(context, groupId, "");
    }

    /**
     * 控制控制台日志的开关
     *
     * @param isShow
     */
    public void isShowLog(boolean isShow) {
        UdeskCoreConst.xmppDebug = isShow;
        UdeskCoreConst.isDebug = isShow;
    }

    /**
     * 断开xmpp连接
     */
    public void disConnectXmpp() {
        UdeskMessageManager.getInstance().cancelXmppConnect().subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Boolean aBoolean) {

            }
        });
    }

    /**
     * 获取当前会话未读消息的记录  setUserInfo调用后才有效
     */
    public int getCurrentConnectUnReadMsgCount() {
        return UdeskDBManager.getInstance().getUnReadMessageCount();
    }

    public List<MessageInfo> getUnReadMessages() {
        return UdeskDBManager.getInstance().getUnReadMessages();
    }

    /**
     * 删除聊天数据
     */
    public void deleteMsg() {
        UdeskDBManager.getInstance().deleteAllMsg();
    }

    public Map<String, String> getUserinfo() {
        return UdeskConfig.userinfo;
    }

    public Map<String, String> getTextField() {
        return UdeskConfig.textField;
    }

    public Map<String, String> getRoplist() {
        return UdeskConfig.roplist;
    }

    public String getSdkToken(Context context) {
        if (!TextUtils.isEmpty(UdeskConfig.sdkToken)) {
            return UdeskConfig.sdkToken;
        }
        return PreferenceHelper.readString(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_SdkToken);
    }

    public String getDomain(Context context) {
        return UdeskConfig.domain;
    }

    public String getSecretKey(Context context) {
        return UdeskConfig.secretKey;
    }

    public String getUserId(Context context) {
        if (!TextUtils.isEmpty(userId)) {
            return userId;
        }
        return "";
    }


    public String getTransfer(Context context) {

        if (!TextUtils.isEmpty(transfer)) {
            return transfer;
        }
        return "";
    }

    public String getH5Url(Context context) {
        if (!TextUtils.isEmpty(h5Url)) {
            return h5Url;
        }
        return "";
    }

    public void setH5Url(String h5Url) {
        UdeskSDKManager.h5Url = h5Url;
    }

    public void setUserId(String userId) {
        UdeskSDKManager.userId = userId;
    }

    public void setTransfer(String transfer) {
        UdeskSDKManager.transfer = transfer;
    }

    public UdeskCommodityItem getCommodity() {
        return commodity;
    }

    public void setCommodity(UdeskCommodityItem commodity) {
        this.commodity = commodity;
    }

    private void showLoading(Context context) {
        try {
            dialog = new UdeskDialog(context, R.style.udesk_dialog);
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void toLanuchRobotAcitivty(Context context, String url, String tranfer, boolean isTransferByImGroup) {
        Intent intent = new Intent(context, UdeskRobotActivity.class);
        intent.putExtra(UdeskConst.UDESKTRANSFER, tranfer);
        intent.putExtra(UdeskConst.UDESKHTMLURL, url);
        intent.putExtra(UdeskConst.UDESKISTRANFERSESSION, isTransferByImGroup);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void dismiss() {
        try {
            if (dialog != null) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void lanuchChatByConfirmId(Context context, String groupId, String agentId) {
        Intent intent = new Intent(context, UdeskChatActivity.class);
        intent.putExtra(UdeskConst.UDESKGROUPID, groupId);
        intent.putExtra(UdeskConst.UDESKAGENTID, agentId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    //启动留言界面
    public void goToForm(Context context) {
        if (formCallBak != null){
            formCallBak.toLuachForm(context);
            return;
        }
        Intent intent = new Intent(context,
                UdeskFormActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 初始话DB
     *
     * @param context
     * @param sdkToken
     */
    public void initDB(Context context, String sdkToken) {
        releaseDB();
        UdeskDBManager.getInstance().init(context, sdkToken);
    }

    /**
     * 销毁DB
     */
    public void releaseDB() {
        UdeskDBManager.getInstance().release();
    }

    private void clean(Context context) {
        userId = null;
        transfer = null;
        h5Url = null;
        UdeskConfig.sdkToken = null;
        UdeskConfig.userinfo = null;
        UdeskConfig.textField = null;
        UdeskConfig.updateUserinfo = null;
        UdeskConfig.updateTextField = null;
        UdeskConfig.roplist = null;
        customerUrl = null;
        isSessioning = false;
    }

    public boolean isNeedMsgNotice() {
        return isNeedMsgNotice;
    }

    public void setIsNeedMsgNotice(boolean isNeedMsgNotice) {
        this.isNeedMsgNotice = isNeedMsgNotice;
    }

    public Map<String, String> getUpdateUserinfo() {
        return UdeskConfig.updateUserinfo;
    }

    public void setUpdateUserinfo(Map<String, String> updateUserinfo) {
        UdeskConfig.updateUserinfo = updateUserinfo;
    }

    public Map<String, String> getUpdateTextField() {
        return UdeskConfig.updateTextField;
    }

    public void setUpdateTextField(Map<String, String> updateTextField) {
        UdeskConfig.updateTextField = updateTextField;
    }

    public Map<String, String> getUpdateRoplist() {
        return UdeskConfig.updateRoplist;
    }

    public void setUpdateRoplist(Map<String, String> updateRoplist) {
        UdeskConfig.updateRoplist = updateRoplist;
    }

    public void logoutUdesk() {
        releaseDB();
        disConnectXmpp();
    }

    public void setCustomerUrl(String url) {
        customerUrl = url;
    }

    public String getCustomerUrl() {
        return customerUrl;
    }

    public String getAppid() {
        return UdeskConfig.appid;
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
        UdeskHttpFacade.getInstance().sdkPushStatus(domain, key, sdkToken, status, registrationID, appid, callBack);
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
        setSdkPushStatus(domain, key, sdkToken, status, registrationID, appid, null);
    }

    //获取注册推送的设备ID
    public String getRegisterId(Context context) {
        if (TextUtils.isEmpty(UdeskConfig.registerId)) {
            return PreferenceHelper.readString(context, UdeskConst.SharePreParams.RegisterIdName, UdeskConst.SharePreParams.Udesk_Push_RegisterId);
        }
        return UdeskConfig.registerId;
    }

    //保存注册推送的的设备ID
    public void setRegisterId(Context context, String registerId) {
        UdeskConfig.registerId = registerId;
        PreferenceHelper.write(context, UdeskConst.SharePreParams.RegisterIdName,
                UdeskConst.SharePreParams.Udesk_Push_RegisterId, registerId);
    }


    /**
     * 设置表单留言的页面地址
     *
     * @param webUrl
     */
    public void setFormUrl(String webUrl) {
        UdeskConfig.udeskFormUrl = webUrl;
    }


    public ITxtMessageWebonCliclk getTxtMessageClick() {
        return txtMessageClick;
    }

    /**
     * 设置文本中的链接地址的点击事件的拦截回调
     *
     * @param txtMessageClick
     */
    public void setTxtMessageClick(ITxtMessageWebonCliclk txtMessageClick) {
        this.txtMessageClick = txtMessageClick;
    }

    private IUdeskFormCallBak  formCallBak;
    /**
     * 设置留言界面的回调
     */
    public interface IUdeskFormCallBak {
        void toLuachForm(Context context);
    }

    public IUdeskFormCallBak getFormCallBak() {
        return formCallBak;
    }

    public void setFormCallBak(IUdeskFormCallBak formCallBak) {
        this.formCallBak = formCallBak;
    }
}
