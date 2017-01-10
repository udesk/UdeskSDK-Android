package cn.udesk;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.udesk.activity.UdeskOptionsAgentGroupActivity;
import cn.udesk.activity.UdeskChatActivity;
import cn.udesk.activity.UdeskFormActivity;
import cn.udesk.activity.UdeskHelperActivity;
import cn.udesk.activity.UdeskRobotActivity;
import cn.udesk.config.UdeskBaseInfo;
import cn.udesk.config.UdeskConfig;
import cn.udesk.db.UdeskDBManager;
import cn.udesk.messagemanager.UdeskMessageManager;
import cn.udesk.model.SDKIMSetting;
import cn.udesk.model.UdeskCommodityItem;
import cn.udesk.widget.UdeskDialog;
import rx.Subscriber;
import udesk.core.UdeskCallBack;
import udesk.core.UdeskCoreConst;
import udesk.core.UdeskHttpFacade;
import udesk.core.model.MessageInfo;


public class UdeskSDKManager {

    private static UdeskSDKManager instance = new UdeskSDKManager();

    private UdeskDialog dialog;

    //文本消息中的链接消息的点击事件的拦截回调。 包含表情的不会拦截回调。
    private ITxtMessageWebonCliclk txtMessageClick;

    //离线留言表单的回调接口：  如果不用udesk系统提供的留言功能，可以设置该接口  回调使用自己的处理流程
    private IUdeskFormCallBak formCallBak;

    //多应用 配置选项mode
    private SDKIMSetting imSetting;

    private UdeskSDKManager() {
    }

    public static UdeskSDKManager getInstance() {
        return instance;
    }

    /**
     * 文本中的链接地址的点击事件的拦截回调接口
     */
    public interface ITxtMessageWebonCliclk {

        void txtMsgOnclick(String stirngUrl);
    }

    /**
     * 留言界面的回调接口
     */
    public interface IUdeskFormCallBak {
        void toLuachForm(Context context);
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

    public IUdeskFormCallBak getFormCallBak() {
        return formCallBak;
    }

    /**
     * 设置留言界面的回调接口
     *
     * @param formCallBak
     */
    public void setFormCallBak(IUdeskFormCallBak formCallBak) {
        this.formCallBak = formCallBak;
    }

    /**
     * 创建应用生成的key值和appid
     *
     * @param context
     * @param domain  注册udesk系统生成的二级域名
     * @param appkey  udesk系统创建应用生成的App Key
     * @param appid   udesk系统创建应用生成的App Key
     */
    public void initApiKey(Context context, String domain, String appkey, String appid) {
        UdeskBaseInfo.domain = domain;
        UdeskBaseInfo.App_Key = appkey;
        UdeskBaseInfo.App_Id = appid;
        initCrashReport(context);
        UdeskUtil.initImageLoaderConfig(context);
    }

    /**
     * @param context
     * @param sdkToken 用户唯一的标识，最好填写数字和英文字母，不要包含特殊字符
     * @param info     包含默认提供的用户信息
     */
    public void setUserInfo(Context context, String sdkToken, Map<String, String> info) {
        this.setUserInfo(context, sdkToken, info, null);
    }

    /**
     * @param context
     * @param sdkToken  用户唯一的标识，最好填写数字和英文字母，不要包含特殊字符
     * @param info      包含默认提供的用户信息
     * @param textField 包含自定义的文本信息
     */
    public void setUserInfo(Context context, String sdkToken, Map<String, String> info, Map<String, String> textField) {
        this.setUserInfo(context, sdkToken, info, textField, null);
    }

    /**
     * @param context
     * @param token     用户唯一的标识，最好填写数字和英文字母，不要包含特殊字符
     * @param info      包含默认提供的用户信息
     * @param textField 包含自定义的文本信息
     * @param roplist   包含自定义的列表信息
     */
    public void setUserInfo(final Context context, String token, Map<String, String> info, Map<String, String> textField, Map<String, String> roplist) {

        String cacheToken = getSdkToken(context);
        if ((cacheToken == null)) {
            clean(context);
            disConnectXmpp();
        } else if ((cacheToken != null && !cacheToken.equals(token))) {
            clean(context);
            disConnectXmpp();
            // 一个应用内切换用户，的关闭上个用户的推送
            if (!TextUtils.isEmpty(UdeskBaseInfo.registerId) && UdeskConfig.isUserSDkPush) {
                setSdkPushStatus(UdeskBaseInfo.domain, UdeskBaseInfo.App_Key, UdeskBaseInfo.sdkToken, UdeskConfig.UdeskPushFlag.OFF, UdeskBaseInfo.registerId, UdeskBaseInfo.App_Id);
            }
        }
        UdeskBaseInfo.sdkToken = token;
        initDB(context, UdeskBaseInfo.sdkToken);
        PreferenceHelper.write(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name,
                UdeskConst.SharePreParams.Udesk_SdkToken, UdeskBaseInfo.sdkToken);
        if (info == null) {
            info = new HashMap<String, String>();
        }
        UdeskBaseInfo.userinfo = info;
        UdeskBaseInfo.userinfo.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, UdeskBaseInfo.sdkToken);
        UdeskBaseInfo.textField = textField;
        UdeskBaseInfo.roplist = roplist;
    }

    //进入会话入口,支持配置,根据配置进入会话
    public void entryChat(Context context) {
        if (TextUtils.isEmpty(UdeskBaseInfo.App_Id)) {
            showConversationByImGroup(context);
            return;
        }
        getSDKImSetting(context);
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
     * 通过配置的导航页的客服组进入,分配进入相应的客服
     *
     * @param context
     */
    public void showConversationByImGroup(Context context) {
        if (imSetting == null) {
            Intent intent = new Intent(context, UdeskOptionsAgentGroupActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            if (imSetting.getEnable_im_group()) {
                Intent intent = new Intent(context, UdeskOptionsAgentGroupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                toLanuchChatAcitvity(context);
            }
        }

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
     * 直接进入帮助中心页面
     *
     * @param context
     */
    public void toLanuchHelperAcitivty(Context context) {
        Intent intent = new Intent(context, UdeskHelperActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    //启动留言界面
    public void goToForm(Context context) {
        if (formCallBak != null) {
            formCallBak.toLuachForm(context);
            return;
        }
        Intent intent = new Intent(context,
                UdeskFormActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 进入机器人页面
     *
     * @param context
     * @param url                 机器人的连接地址
     * @param tranfer             是否可以转人工
     * @param isTransferByImGroup 转人工是否通过导航页进入  true表示是 false 表示不是
     */
    public void toLanuchRobotAcitivty(Context context, String url, String tranfer, boolean isTransferByImGroup) {
        Intent intent = new Intent(context, UdeskRobotActivity.class);
        intent.putExtra(UdeskConst.UDESKTRANSFER, tranfer);
        intent.putExtra(UdeskConst.UDESKHTMLURL, url);
        intent.putExtra(UdeskConst.UDESKISTRANFERSESSION, isTransferByImGroup);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    //获取注册推送的唯一ID
    public String getRegisterId(Context context) {
        if (TextUtils.isEmpty(UdeskBaseInfo.registerId)) {
            return PreferenceHelper.readString(context, UdeskConst.SharePreParams.RegisterIdName, UdeskConst.SharePreParams.Udesk_Push_RegisterId);
        }
        return UdeskBaseInfo.registerId;
    }

    //保存注册推送的的唯一ID
    public void setRegisterId(Context context, String registerId) {
        UdeskBaseInfo.registerId = registerId;
        PreferenceHelper.write(context, UdeskConst.SharePreParams.RegisterIdName,
                UdeskConst.SharePreParams.Udesk_Push_RegisterId, registerId);
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

    /**
     * 退出：1，本地数据库的释放; 2 于xmpp服务器的断开
     */
    public void logoutUdesk() {
        releaseDB();
        disConnectXmpp();
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

    /**
     * 获取当前会话未读消息的记录  setUserInfo调用后才有效
     */
    public int getCurrentConnectUnReadMsgCount() {
        return UdeskDBManager.getInstance().getUnReadMessageCount();
    }

    /**
     * 获取未读消息的结合 setUserInfo调用后才有效
     *
     * @return
     */
    public List<MessageInfo> getUnReadMessages() {
        return UdeskDBManager.getInstance().getUnReadMessages();
    }

    /**
     * 删除当前用户的聊天数据
     */
    public void deleteMsg() {
        UdeskDBManager.getInstance().deleteAllMsg();
    }

    /**
     * 给商品链接的mode赋值
     *
     * @param commodity
     */
    public void setCommodity(UdeskCommodityItem commodity) {
        UdeskBaseInfo.commodity = commodity;
    }

    /**
     * 保存客户的头像地址，由用户app传递
     *
     * @param url 头像地址
     */
    public void setCustomerUrl(String url) {
        UdeskBaseInfo.customerUrl = url;
    }

    /**
     * 用户需要更新的基本信息
     *
     * @param updateUserinfo 存储更新用户的信息
     */
    public void setUpdateUserinfo(Map<String, String> updateUserinfo) {
        UdeskBaseInfo.updateUserinfo = updateUserinfo;
    }

    /**
     * 用户需要更新自定义字段文本信息
     *
     * @param updateTextField 存储更新用户的自定义文本字段信息
     */
    public void setUpdateTextField(Map<String, String> updateTextField) {
        UdeskBaseInfo.updateTextField = updateTextField;
    }

    /**
     * 用户需要更新自定义列表字段信息
     *
     * @param updateRoplist 存储更新用户的自定义列表字段信息
     */
    public void setUpdateRoplist(Map<String, String> updateRoplist) {
        UdeskBaseInfo.updateRoplist = updateRoplist;
    }

   //配置开启留言时的    留言表单的留言提示语
    public void setLeavingMsg(String leavingMsg){
        UdeskConfig.UdeskLeavingMsg = leavingMsg;
    }

    /**
     * 设置退出排队的模式
     * @param quitQuenuMode
     */
    public void setQuitQuenuMode(String quitQuenuMode){
        UdeskConfig.UdeskQuenuMode = quitQuenuMode;
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
     * 控制控制台日志的开关
     *
     * @param isShow true 开启 false 关闭
     */
    public void isShowLog(boolean isShow) {
        UdeskCoreConst.xmppDebug = isShow;
        UdeskCoreConst.isDebug = isShow;
    }

    public String getSdkToken(Context context) {
        if (!TextUtils.isEmpty(UdeskBaseInfo.sdkToken)) {
            return UdeskBaseInfo.sdkToken;
        }
        return PreferenceHelper.readString(context, UdeskConst.SharePreParams.Udesk_Sharepre_Name, UdeskConst.SharePreParams.Udesk_SdkToken);
    }

    public SDKIMSetting getImSetting() {
        return imSetting;
    }


    /**
     * 获取应用的设置项配置值
     *
     * @param context
     */
    private void getSDKImSetting(final Context context) {
        try {
            showLoading(context);
            UdeskHttpFacade.getInstance().getIMSettings(UdeskBaseInfo.domain, UdeskBaseInfo.App_Key, UdeskBaseInfo.sdkToken, UdeskBaseInfo.App_Id, new UdeskCallBack() {
                @Override
                public void onSuccess(String message) {
                    imSetting = JsonUtils.parserIMSettingJson(message);
                    switchBySetting(context, imSetting);
                }

                @Override
                public void onFail(String message) {
                    switchBySetting(context, null);
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
        if (imSetting != null) {
            if (imSetting.getIn_session()) {
                dismiss();
                toLanuchChatAcitvity(context);
                return;
            }
            if (imSetting.getEnable_robot()) {
                showRobotByConfigSetting(context, imSetting);
                return;
            }
            dismiss();
            showConversationByImGroup(context);
            return;

        } else {
            dismiss();
            toLanuchChatAcitvity(context);
        }

    }

    /**
     * 进入机器人聊天会话页面
     *
     * @param context
     */
    private void showRobotByConfigSetting(final Context context, final SDKIMSetting imSetting) {

        try {
            UdeskHttpFacade.getInstance().setUserInfo(context, UdeskBaseInfo.domain,
                    UdeskBaseInfo.App_Key, getSdkToken(context),
                    UdeskBaseInfo.userinfo, UdeskBaseInfo.textField,
                    UdeskBaseInfo.roplist, UdeskBaseInfo.App_Id, new UdeskCallBack() {

                        @Override
                        public void onSuccess(String string) {
                            dismiss();
                            if (!TextUtils.isEmpty(imSetting.getRobot())) {
                                toLanuchRobotAcitivty(context, imSetting.getRobot(), imSetting.getEnable_agent(), imSetting.getEnable_im_group());
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
        } catch (Exception e) {
            showConversationByImGroup(context);
        }
    }

    private void initCrashReport(Context context) {
        try {
            CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
            strategy.setAppVersion(UdeskCoreConst.sdkversion + UdeskUtil.getAppName(context));
            CrashReport.initCrashReport(context, UdeskCoreConst.buglyAppid, false, strategy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoading(Context context) {
        try {
            dialog = new UdeskDialog(context, R.style.udesk_dialog);
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private void clean(Context context) {
        UdeskBaseInfo.customerId = null;
        UdeskBaseInfo.customerUrl = null;
        UdeskBaseInfo.sdkToken = null;
        UdeskBaseInfo.userinfo = null;
        UdeskBaseInfo.textField = null;
        UdeskBaseInfo.updateUserinfo = null;
        UdeskBaseInfo.updateTextField = null;
        UdeskBaseInfo.roplist = null;
        imSetting = null;
    }


}
