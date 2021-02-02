package cn.udesk.config;


import java.util.List;
import java.util.Map;

import cn.udesk.R;
import cn.udesk.callback.IFunctionItemClickCallBack;
import cn.udesk.callback.IImgTxtMessageWebonClick;
import cn.udesk.callback.ILinkMessageWebonClick;
import cn.udesk.callback.ILocationMessageClickCallBack;
import cn.udesk.callback.INavigationItemClickCallBack;
import cn.udesk.callback.IProductMessageWebonClick;
import cn.udesk.callback.IReplyProductMessageWebonClick;
import cn.udesk.callback.IRichMessageWebonClick;
import cn.udesk.callback.IStructMessageWebonClick;
import cn.udesk.callback.ITxtMessageWebonClick;
import cn.udesk.callback.IUdeskFormCallBack;
import cn.udesk.callback.IUdeskStructMessageCallBack;
import cn.udesk.model.FunctionMode;
import cn.udesk.model.NavigationMode;
import cn.udesk.model.UdeskCommodityItem;
import udesk.core.model.Product;

/**
 * Created by user on 2016/8/12.
 */
public class UdeskConfig {

    public static final int DEFAULT = -1;
    //配置是否把domain 和 appid 和 appkey 和 sdktoken 存在sharePrefence中， true保存，false 不存
    public static boolean isUseShare = true;

    // 标题栏TitleBar的背景色  通过颜色设置
    public int udeskTitlebarBgResId = DEFAULT;
    // 标题栏TitleBar，中部上下文字的颜色
    public int udeskTitlebarMiddleTextResId = DEFAULT;
    // 标题栏TitleBar，右侧文字的颜色
    public int udeskTitlebarRightTextResId = DEFAULT;
    //    //IM界面，左侧文字的字体颜色
    public int udeskIMLeftTextColorResId = DEFAULT;
    //IM界面，右侧文字的字体颜色
    public int udeskIMRightTextColorResId = DEFAULT;
    //IM界面，左侧客服昵称文字的字体颜色
    public int udeskIMAgentNickNameColorResId = DEFAULT;
    //IM界面，右侧用户昵称文字的字体颜色
    public int udeskIMCustomerNickNameColorResId = DEFAULT;
    //IM界面，时间文字的字体颜色
    public int udeskIMTimeTextColorResId = DEFAULT;
    // IM界面，提示语文字的字体颜色，比如客服转移
    public int udeskIMTipTextColorResId = DEFAULT;
    // 返回箭头图标资源id
    public int udeskbackArrowIconResId = DEFAULT;
    // 商品咨询item的背景颜色
    public int udeskCommityBgResId = DEFAULT;
    //    商品咨询介绍Title的字样颜色
    public int udeskCommityTitleColorResId = DEFAULT;
    //  商品咨询页面中，商品介绍子Title的字样颜色
    public int udeskCommitysubtitleColorResId = DEFAULT;
    //    商品咨询页面中，发送链接的字样颜色
    public int udeskCommityLinkColorResId = DEFAULT;
    // 商品消息背景左侧
    public int udeskProductLeftBgResId = DEFAULT;
    // 商品消息背景右侧
    public int udeskProductRightBgResId = DEFAULT;
    //商品消息的 带有链接时的  商品名字显示的颜色 右侧
    public int udeskProductRightNameLinkColorResId = DEFAULT;
    //商品消息的 带有链接时的  商品名字显示的颜色 左侧
    public int udeskProductLeftNameLinkColorResId = DEFAULT;
    //商品消息名称最大显示行数
    public int udeskProductMaxLines = 0;
    //配置 是否使用推送服务  true 表示使用  false表示不使用
    public boolean isUserSDkPush = true;
    //配置放弃排队的策略
    public String UdeskQuenuMode = UdeskQueueFlag.Mark;
    //是否使用录音功能  true表示使用 false表示不使用
    public boolean isUseVoice = true;
    //是否使用发送图片的功能  true表示使用 false表示不使用
    public boolean isUsephoto = true;
    //是否使用拍照的功能  true表示使用 false表示不使用
    public boolean isUsecamera = true;
    //是否使用上传文件功能  true表示使用 false表示不使用
    public boolean isUsefile = true;
    //是否使用发送位置功能  true表示使用 false表示不使用
    public boolean isUseMap = false;
    //是否使用表情 true表示使用 false表示不使用
    public boolean isUseEmotion = true;
    //是否使用更多展示出的列表选项 true表示使用 false表示不使用
    public boolean isUseMore = true;
    //设置是否使用导航UI rue表示使用 false表示不使用
    public boolean isUseNavigationRootView = false;
    //设置是否使用导航UI rue表示使用 false表示不使用
    public boolean isUseRobotNavigationRootView = false;
    //设置是否使用导航UI中的满意度评价UI rue表示使用 false表示不使用
    public boolean isUseNavigationSurvy = true;
    //设置是否需要小视频的功能 rue表示使用 false表示不使用
    public boolean isUseSmallVideo = true;
    //上传图片是否使用原图 还是缩率图
    public boolean isScaleImg = true;
    //缩放图 设置宽高最大值，如果超出则压缩，否则不压缩
    public int ScaleMax = 1024;
    //设置使用那种地图  //百度地图   腾讯地图  //高德底图
    public String useMapType = UdeskMapType.Other;
    //设置默认屏幕显示习惯
    public String Orientation = OrientationValue.portrait;
    //在没有请求到管理员在后端对sdk使用配置下，在默认的情况下，是否需要表单留言，true需要， false 不需要
    public boolean isUserForm = false;
    //创建 更新用户的基本信息
    public Map<String, String> defaultUserInfo;
    //创建 更新自定义的文本信息
    public Map<String, String> definedUserTextField;
    //创建自定义的列表信息
    public Map<String, String> definedUserRoplist;
    //设置带入一条消息  会话分配就发送给客服
    public String firstMessage;
    //udesk 机器人常见问题 对应的Id值
    public String robot_modelKey;
    //udesk 用于机器人页面收集客户信息
    public String concatRobotUrlWithCustomerInfo;
    //设置客户的头像地址
    public String customerUrl;
    // //配置发送商品链接的mode
    public UdeskCommodityItem commodity;
    //文本消息中的链接消息的点击事件的拦截回调。 包含表情的不会拦截回调。
    public ITxtMessageWebonClick txtMessageClick;
    //商品消息中的链接点击回调
    public IProductMessageWebonClick productMessageClick;
    //链接消息点击回调
    public ILinkMessageWebonClick linkMessageWebonClick;
    //商品回复消息点击回调
    public IReplyProductMessageWebonClick replyProductMessageWebonClick;
    //富文本链接消息点击回调
    public IRichMessageWebonClick richMessageWebonClick;
    //图文消息点击回调
    public IImgTxtMessageWebonClick imgTxtMessageWebonClick;
    //结构化消息按钮链接回调
    public IStructMessageWebonClick structMessageWebonClick;
    //离线留言表单的回调接口：  如果不用udesk系统提供的留言功能，可以设置该接口  回调使用自己的处理流程
    public IUdeskFormCallBack formCallBack;
    //设置结构化消息的回调接口
    public IUdeskStructMessageCallBack structMessageCallBack;
    //支持自定义功能按钮后 点击事件回调 和直接发送文本,图片,视频,文件,地理位置,商品信息  必须组合设置额外的自定义按钮
    public IFunctionItemClickCallBack functionItemClickCallBack;
    //设置额外的自定义功能按钮
    public List<FunctionMode> extreFunctions;
    //支持客户在导航处添加自定义按钮的点击回调事件   必须组合设置额外的自定义按钮
    public INavigationItemClickCallBack navigationItemClickCallBack;
    //支持客户在机器人导航处添加自定义按钮的点击回调事件
    public INavigationItemClickCallBack robotNavigationItemClickCallBack;
    //约定传递的自定义按钮集合
    public List<NavigationMode> navigationModes;
    //约定传递的自定义按钮集合
    public List<NavigationMode> robotnavigationModes;
    //点击地理位置信息的回调接口
    public ILocationMessageClickCallBack locationMessageClickCallBack;
    //传入打开的地图的activity
    public Class<?> cls;
    //设置的指定组，每次进入都必须重新指定
    public String groupId;
    //指定是否只通过设定groupId 进入会话的分配, 不再要机器人, 设置导航等判断
    public boolean isOnlyByGroupId = false;
    //设置的指客客服，每次进入都必须重新指定
    public String agentId;
    //指定是否只通过设定agentId进入会话的分配, 不再要机器人, 设置导航等判断
    public boolean isOnlyByAgentId = false;
    //配置是否只使用机器人功能 只使用机器人功能,只使用机器人功能;  其它功能不使用。
    public boolean isOnlyUseRobot = false;
    //商品消息
    public Product mProduct;
    //SDK支持自定义渠道
    public String channel;
    //是否显示客户昵称
    public boolean isShowCustomerNickname;
    //是否显示客户头像
    public boolean isShowCustomerHead = true;
    //设置带入一条消息  进入机器人界面自动发送
    public String preSendRobotMessages;
    //设置智能提示的最大高度按比例
    public float maxHeightViewRatio =0.0f;
    //设置智能提示的最大高度
    public float maxHeightViewDimen =0.0f;

    UdeskConfig(Builder builder) {

        this.udeskTitlebarBgResId = builder.udeskTitlebarBgResId;
        this.udeskTitlebarMiddleTextResId = builder.udeskTitlebarMiddleTextResId;
        this.udeskTitlebarRightTextResId=builder.udeskTitlebarRightTextResId;
        this.udeskIMLeftTextColorResId = builder.udeskIMLeftTextColorResId;
        this.udeskIMRightTextColorResId = builder.udeskIMRightTextColorResId;
        this.udeskIMAgentNickNameColorResId = builder.udeskIMAgentNickNameColorResId;
        this.udeskIMCustomerNickNameColorResId = builder.udeskIMCustomerNickNameColorResId;
        this.udeskIMTimeTextColorResId = builder.udeskIMTimeTextColorResId;
        this.udeskIMTipTextColorResId = builder.udeskIMTipTextColorResId;
        this.udeskbackArrowIconResId = builder.udeskbackArrowIconResId;
        this.udeskCommityBgResId = builder.udeskCommityBgResId;
        this.udeskCommityTitleColorResId = builder.udeskCommityTitleColorResId;
        this.udeskCommitysubtitleColorResId = builder.udeskCommitysubtitleColorResId;
        this.udeskCommityLinkColorResId = builder.udeskCommityLinkColorResId;
        this.udeskProductLeftBgResId = builder.udeskProductLeftBgResId;
        this.udeskProductRightBgResId = builder.udeskProductRightBgResId;
        this.udeskProductRightNameLinkColorResId = builder.udeskProductRightNameLinkColorResId;
        this.udeskProductLeftNameLinkColorResId = builder.udeskProductLeftNameLinkColorResId;
        this.udeskProductMaxLines = builder.udeskProductMaxLines;
        this.isUserSDkPush = builder.isUserSDkPush;
        this.UdeskQuenuMode = builder.UdeskQuenuMode;
        this.isUseVoice = builder.isUseVoice;
        this.isUsephoto = builder.isUsephoto;
        this.isUsecamera = builder.isUsecamera;
        this.isUsefile = builder.isUsefile;
        this.isUseMap = builder.isUseMap;
        this.isUseEmotion = builder.isUseEmotion;
        this.isUseMore = builder.isUseMore;
        this.isUseNavigationRootView = builder.isUseNavigationRootView;
        this.isUseRobotNavigationRootView = builder.isUseRobotNavigationRootView;
        this.isUseNavigationSurvy = builder.isUseNavigationSurvy;
        this.isUseSmallVideo = builder.isUseSmallVideo;
        this.isScaleImg = builder.isScaleImg;
        this.ScaleMax = builder.ScaleMax;
        this.useMapType = builder.useMapType;
        this.Orientation = builder.Orientation;
        this.isUserForm = builder.isUserForm;
        this.defaultUserInfo = builder.defaultUserInfo;
        this.definedUserTextField = builder.definedUserTextField;
        this.definedUserRoplist = builder.definedUserRoplist;
        this.firstMessage = builder.firstMessage;
        this.robot_modelKey = builder.robot_modelKey;
        this.concatRobotUrlWithCustomerInfo = builder.concatRobotUrlWithCustomerInfo;
        this.customerUrl = builder.customerUrl;
        this.commodity = builder.commodity;
        this.txtMessageClick = builder.txtMessageClick;
        this.productMessageClick = builder.productMessageClick;
        this.linkMessageWebonClick = builder.linkMessageWebonClick;
        this.replyProductMessageWebonClick = builder.replyProductMessageWebonClick;
        this.richMessageWebonClick = builder.richMessageWebonClick;
        this.imgTxtMessageWebonClick = builder.imgTxtMessageWebonClick;
        this.structMessageWebonClick = builder.structMessageWebonClick;
        this.formCallBack = builder.formCallBack;
        this.structMessageCallBack = builder.structMessageCallBack;
        this.functionItemClickCallBack = builder.functionItemClickCallBack;
        this.extreFunctions = builder.extreFunctions;
        this.navigationItemClickCallBack = builder.navigationItemClickCallBack;
        this.robotNavigationItemClickCallBack = builder.robotNavigationItemClickCallBack;
        this.navigationModes = builder.navigationModes;
        this.robotnavigationModes = builder.robotnavigationModes;
        this.locationMessageClickCallBack = builder.locationMessageClickCallBack;
        this.cls = builder.cls;
        this.groupId = builder.groupId;
        this.isOnlyByGroupId = builder.isOnlyByGroupId;
        this.agentId = builder.agentId;
        this.isOnlyByAgentId = builder.isOnlyByAgentId;
        this.isOnlyUseRobot = builder.isOnlyUseRobot;
        this.mProduct = builder.mProduct;
        this.channel = builder.channel;
        this.isShowCustomerNickname = builder.isShowCustomerNickname;
        this.isShowCustomerHead = builder.isShowCustomerHead;
        this.preSendRobotMessages= builder.preSendRobotMessages;
        this.maxHeightViewRatio = builder.maxHeightViewRatio;
        this.maxHeightViewDimen = builder.maxHeightViewDimen;
    }

    public static UdeskConfig createDefualt() {
        return new Builder().build();
    }


    public static class Builder {
        // 标题栏TitleBar的背景色  通过颜色设置
        private int udeskTitlebarBgResId = DEFAULT;
        // 标题栏TitleBar，中部上下文字的颜色
        public int udeskTitlebarMiddleTextResId = DEFAULT;
        // 标题栏TitleBar，右侧文字的颜色
        public int udeskTitlebarRightTextResId = DEFAULT;
        //IM界面，左侧文字的字体颜色
        private int udeskIMLeftTextColorResId = DEFAULT;
        //IM界面，右侧文字的字体颜色
        private int udeskIMRightTextColorResId = DEFAULT;
        //IM界面，左侧客服昵称文字的字体颜色
        private int udeskIMAgentNickNameColorResId = DEFAULT;
        //IM界面，右侧用户昵称文字的字体颜色
        private int udeskIMCustomerNickNameColorResId = DEFAULT;
        //IM界面，时间文字的字体颜色
        private int udeskIMTimeTextColorResId = DEFAULT;
        // IM界面，提示语文字的字体颜色，比如客服转移
        private int udeskIMTipTextColorResId = DEFAULT;
        // 返回箭头图标资源id
        private int udeskbackArrowIconResId = DEFAULT;
        // 咨询商品item的背景颜色
        private int udeskCommityBgResId = DEFAULT;
        //    商品介绍Title的字样颜色
        private int udeskCommityTitleColorResId = DEFAULT;
        //  商品咨询页面中，商品介绍子Title的字样颜色
        private int udeskCommitysubtitleColorResId = DEFAULT;
        //    商品咨询页面中，发送链接的字样颜色
        private int udeskCommityLinkColorResId = DEFAULT;
        // 商品消息背景左侧
        public int udeskProductLeftBgResId = DEFAULT;
         // 商品消息背景右侧
        public int udeskProductRightBgResId = DEFAULT;
        // 商品消息的 带有链接时的  商品名字显示的颜色 右侧
        private int udeskProductRightNameLinkColorResId = R.color.color_1850cc;
        // 商品消息的 带有链接时的  商品名字显示的颜色 左侧
        private int udeskProductLeftNameLinkColorResId = R.color.color_1850cc;
        //商品消息名称最大显示行数
        public int udeskProductMaxLines = 0;
        //配置 是否使用推送服务  true 表示使用  false表示不使用
        private boolean isUserSDkPush = true;
        //配置放弃排队的策略
        private String UdeskQuenuMode = UdeskQueueFlag.Mark;
        //是否使用录音功能  true表示使用 false表示不使用
        private boolean isUseVoice = true;
        //是否使用发送图片的功能  true表示使用 false表示不使用
        private boolean isUsephoto = true;
        //是否使用拍照的功能  true表示使用 false表示不使用
        private boolean isUsecamera = true;
        //是否使用上传文件功能  true表示使用 false表示不使用
        private boolean isUsefile = true;
        //是否使用发送位置功能  true表示使用 false表示不使用
        private boolean isUseMap = false;
        //是否使用表情 true表示使用 false表示不使用
        private boolean isUseEmotion = true;
        //是否使用更多控件 展示出更多功能选项 true表示使用 false表示不使用
        private boolean isUseMore = true;
        //设置是否使用导航UI rue表示使用 false表示不使用
        private boolean isUseNavigationRootView = false;
        //设置是否使用导航UI rue表示使用 false表示不使用
        private boolean isUseRobotNavigationRootView = false;
        //设置是否使用导航UI中的满意度评价UI rue表示使用 false表示不使用
        private boolean isUseNavigationSurvy = true;
        //设置是否需要小视频的功能 rue表示使用 false表示不使用
        private boolean isUseSmallVideo = true;
        //上传图片是否使用原图 还是缩率图
        private boolean isScaleImg = true;
        //缩放图 设置宽高最大值，如果超出则压缩，否则不压缩
        private int ScaleMax = 1024;
        //设置使用那种地图  //百度地图   腾讯地图  //高德底图
        private String useMapType = UdeskMapType.Other;
        //设置默认屏幕显示习惯
        private String Orientation = OrientationValue.portrait;
        //在没有请求到管理员在后端对sdk使用配置下，在默认的情况下，是否需要表单留言，true需要， false 不需要
        private boolean isUserForm = false;
        //创建用户的基本信息
        private Map<String, String> defaultUserInfo;
        //创建自定义的文本信息
        private Map<String, String> definedUserTextField;
        //创建自定义的列表信息
        private Map<String, String> definedUserRoplist;
        //设置带入一条消息  会话分配就发送给客服
        private String firstMessage;
        //udesk 机器人常见问题 对应的Id值
        private String robot_modelKey;
        private String concatRobotUrlWithCustomerInfo;
        //设置客户的头像地址
        private String customerUrl;
        //配置发送商品链接的mode
        private UdeskCommodityItem commodity;
        //文本消息中的链接消息的点击事件的拦截回调。 包含表情的不会拦截回调。
        public ITxtMessageWebonClick txtMessageClick;
        //商品消息中的链接点击回调
        public IProductMessageWebonClick productMessageClick;
        //链接消息点击回调
        public ILinkMessageWebonClick linkMessageWebonClick;
        //商品回复消息点击回调
        public IReplyProductMessageWebonClick replyProductMessageWebonClick;
        //富文本链接消息点击回调
        public IRichMessageWebonClick richMessageWebonClick;
        //图文消息点击回调
        public IImgTxtMessageWebonClick imgTxtMessageWebonClick;
        //结构化消息点击回调
        public IStructMessageWebonClick structMessageWebonClick;
        //离线留言表单的回调接口：  如果不用udesk系统提供的留言功能，可以设置该接口  回调使用自己的处理流程
        private IUdeskFormCallBack formCallBack;
        //设置结构化消息的回调接口
        private IUdeskStructMessageCallBack structMessageCallBack;
        //支持自定义功能按钮后 点击事件回调 和直接发送文本,图片,视频,文件,地理位置,商品信息
        private IFunctionItemClickCallBack functionItemClickCallBack;
        //设置额外的功能按钮
        private List<FunctionMode> extreFunctions;
        //支持客户在导航处添加自定义按钮的点击回调事件
        private INavigationItemClickCallBack navigationItemClickCallBack;
        //支持客户在机器人导航处添加自定义按钮的点击回调事件
        private INavigationItemClickCallBack robotNavigationItemClickCallBack;
        //约定传递的自定义按钮集合
        private List<NavigationMode> navigationModes;
        //约定传递的自定义按钮集合
        private List<NavigationMode> robotnavigationModes;

        //点击地理位置信息的回调接口
        private ILocationMessageClickCallBack locationMessageClickCallBack;
        //传入打开的地图的activity
        private Class<?> cls;
        //设置的指定组，每次进入都必须重新指定
        private String groupId;
        //指定是否只通过设定groupId 进入会话的分配, 不再要机器人, 设置导航等判断
        private boolean isOnlyByGroupId = false;
        //设置的指客客服，每次进入都必须重新指定
        private String agentId;
        //指定是否只通过设定agentId进入会话的分配, 不再要机器人, 设置导航等判断
        private boolean isOnlyByAgentId = false;
        //配置是否只使用机器人功能 只使用机器人功能,只使用机器人功能;  其它功能不使用。
        private boolean isOnlyUseRobot = false;
        private Product mProduct;
        //SDK支持自定义渠道
        private String channel;
        //是否显示客户昵称
        public boolean isShowCustomerNickname=false;
        //是否显示客户头像
        public boolean isShowCustomerHead = true;
        //设置带入一条消息  进入机器人界面自动发送
        public String preSendRobotMessages ="";
        //设置智能提示的最大高度按比例
        public float maxHeightViewRatio =0.0f;
        //设置智能提示的最大高度
        public float maxHeightViewDimen =0.0f;
        public Builder() {

        }

        /**
         * @param udeskTitlebarBgResId 标题栏TitleBar的背景色  通过颜色设置
         * @return
         */
        public Builder setUdeskTitlebarBgResId(int udeskTitlebarBgResId) {
            this.udeskTitlebarBgResId = udeskTitlebarBgResId;
            return this;
        }
        /**
         * @param udeskTitlebarMiddleTextResId 标题栏TitleBar，中间上下文字的颜色
         * @return
         */
        public Builder setUdeskTitlebarMiddleTextResId(int udeskTitlebarMiddleTextResId) {
            this.udeskTitlebarMiddleTextResId = udeskTitlebarMiddleTextResId;
            return this;
        }
        /**
         * @param udeskTitlebarRightTextResId 标题栏TitleBar，右侧文字的颜色
         * @return
         */
        public Builder setUdeskTitlebarRightTextResId(int udeskTitlebarRightTextResId) {
            this.udeskTitlebarRightTextResId = udeskTitlebarRightTextResId;
            return this;
        }

        /**
         * @param udeskIMLeftTextColorResId IM界面，左侧文字的字体颜色
         * @return
         */
        public Builder setUdeskIMLeftTextColorResId(int udeskIMLeftTextColorResId) {
            this.udeskIMLeftTextColorResId = udeskIMLeftTextColorResId;
            return this;
        }

        /**
         * @param udeskIMRightTextColorResId IM界面，右侧文字的字体颜色
         * @return
         */
        public Builder setUdeskIMRightTextColorResId(int udeskIMRightTextColorResId) {
            this.udeskIMRightTextColorResId = udeskIMRightTextColorResId;
            return this;
        }

        /**
         * IM界面，左侧客服昵称文字的字体颜色
         *
         * @param udeskIMAgentNickNameColorResId
         * @return
         */
        public Builder setUdeskIMAgentNickNameColorResId(int udeskIMAgentNickNameColorResId) {
            this.udeskIMAgentNickNameColorResId = udeskIMAgentNickNameColorResId;
            return this;
        }
       /**
         * IM界面，右侧用户昵称文字的字体颜色
         *
         * @param udeskIMCustomerNickNameColorResId
         * @return
         */
        public Builder setUdeskIMCustomerNickNameColorResId(int udeskIMCustomerNickNameColorResId) {
            this.udeskIMCustomerNickNameColorResId = udeskIMCustomerNickNameColorResId;
            return this;
        }

        /**
         * @param udeskIMTimeTextColorResId IM界面，时间文字的字体颜色
         * @return
         */
        public Builder setUdeskIMTimeTextColorResId(int udeskIMTimeTextColorResId) {
            this.udeskIMTimeTextColorResId = udeskIMTimeTextColorResId;
            return this;
        }

        /**
         * @param udeskIMTipTextColorResId IM界面，提示语文字的字体颜色，比如客服转移
         * @return
         */
        public Builder setUdeskIMTipTextColorResId(int udeskIMTipTextColorResId) {
            this.udeskIMTipTextColorResId = udeskIMTipTextColorResId;
            return this;
        }

        /**
         * @param udeskbackArrowIconResId 返回箭头图标资源id
         * @return
         */
        public Builder setUdeskbackArrowIconResId(int udeskbackArrowIconResId) {
            this.udeskbackArrowIconResId = udeskbackArrowIconResId;
            return this;
        }

        /**
         * @param udeskCommityBgResId 咨询商品item的背景颜色
         * @return
         */
        public Builder setUdeskCommityBgResId(int udeskCommityBgResId) {
            this.udeskCommityBgResId = udeskCommityBgResId;
            return this;
        }

        /**
         * @param udeskCommityTitleColorResId 商品介绍Title的字样颜色
         * @return
         */
        public Builder setUdeskCommityTitleColorResId(int udeskCommityTitleColorResId) {
            this.udeskCommityTitleColorResId = udeskCommityTitleColorResId;
            return this;
        }

        /**
         * @param udeskCommitysubtitleColorResId 商品咨询页面中，商品介绍子Title的字样颜色
         * @return
         */
        public Builder setUdeskCommitysubtitleColorResId(int udeskCommitysubtitleColorResId) {
            this.udeskCommitysubtitleColorResId = udeskCommitysubtitleColorResId;
            return this;
        }

        /**
         * @param udeskCommityLinkColorResId 商品咨询页面中，发送链接的字样颜色
         * @return
         */
        public Builder setUdeskCommityLinkColorResId(int udeskCommityLinkColorResId) {
            this.udeskCommityLinkColorResId = udeskCommityLinkColorResId;
            return this;
        }

        /**
         * 设置商品信息 带链接时显示的颜色
         *
         * @param udeskProductRightNameLinkColorResId
         * @return
         */
        public Builder setUdeskProductRightNameLinkColorResId(int udeskProductRightNameLinkColorResId) {
            this.udeskProductRightNameLinkColorResId = udeskProductRightNameLinkColorResId;
            return this;
        }
         /**
         * 设置商品信息 带链接时显示的颜色 左侧
         *
         * @param udeskProductLeftNameLinkColorResId
         * @return
         */
        public Builder setUdeskProductLeftNameLinkColorResId(int udeskProductLeftNameLinkColorResId) {
            this.udeskProductLeftNameLinkColorResId = udeskProductLeftNameLinkColorResId;
            return this;
        }

        /**
         * 设置商品消息左侧背景布局
         * @param udeskProductBgResId
         * @return
         */
        public Builder setUdeskProductLeftBgResId(int udeskProductBgResId) {
            this.udeskProductLeftBgResId = udeskProductBgResId;
            return this;
        }

        /**
         * 设置商品消息右侧背景布局
         *
         * @param udeskProductBgResId
         * @return
         */
        public Builder setUdeskProductRightBgResId(int udeskProductBgResId) {
            this.udeskProductRightBgResId = udeskProductBgResId;
            return this;
        }

        /**
         * 商品消息名称最大显示行数
         * @param maxLines
         * @return
         */
        public Builder setUdeskProductMaxLines(int maxLines) {
            this.udeskProductMaxLines = maxLines;
            return this;
        }
        /**
         * @param userSDkPush 配置 是否使用推送服务  true 表示使用  false表示不使用
         * @return
         */
        public Builder setUserSDkPush(boolean userSDkPush) {
            isUserSDkPush = userSDkPush;
            return this;
        }

        /**
         * 配置放弃排队的策略
         *
         * @param udeskQuenuMode
         * @return
         */
        public Builder setUdeskQuenuMode(String udeskQuenuMode) {
            UdeskQuenuMode = udeskQuenuMode;
            return this;
        }

        /**
         * @param useVoice 是否使用录音功能  true表示使用 false表示不使用
         * @return
         */
        public Builder setUseVoice(boolean useVoice) {
            isUseVoice = useVoice;
            return this;
        }

        /**
         * @param usephoto 是否使用发送图片的功能  true表示使用 false表示不使用
         * @return
         */
        public Builder setUsephoto(boolean usephoto) {
            isUsephoto = usephoto;
            return this;
        }

        /**
         * @param usecamera 是否使用拍照的功能  true表示使用 false表示不使用
         * @return
         */
        public Builder setUsecamera(boolean usecamera) {
            isUsecamera = usecamera;
            return this;
        }

        /**
         * @param usefile 是否使用上传文件功能  true表示使用 false表示不使用
         * @return
         */
        public Builder setUsefile(boolean usefile) {
            isUsefile = usefile;
            return this;
        }

        /**
         * @param useMap 是否使用发送位置功能  true表示使用 false表示不使用
         * @return
         */
        public Builder setUseMap(boolean useMap) {
            isUseMap = useMap;
            return this;
        }

        /**
         * @param useEmotion 是否使用表情 true表示使用 false表示不使用
         * @return
         */
        public Builder setUseEmotion(boolean useEmotion) {
            isUseEmotion = useEmotion;
            return this;
        }

        /**
         * @param useMore 是否使用更多控件 展示出更多功能选项 true表示使用 false表示不使用
         * @return
         */
        public Builder setUseMore(boolean useMore) {
            isUseMore = useMore;
            return this;
        }

        /**
         * @param useSmallVideo 设置是否需要小视频的功能 rue表示使用 false表示不使用
         * @return
         */
        public Builder setUseSmallVideo(boolean useSmallVideo) {
            isUseSmallVideo = useSmallVideo;
            return this;
        }

        /**
         * @param scaleImg 上传图片是否使用原图 还是缩率图
         * @return
         */
        public Builder setScaleImg(boolean scaleImg) {
            isScaleImg = scaleImg;
            return this;
        }

        /**
         * @param scaleMax 缩放图 设置宽高最大值，如果超出则压缩，否则不压缩
         * @return
         */
        public Builder setScaleMax(int scaleMax) {
            ScaleMax = scaleMax;
            return this;
        }

        /**
         * @param orientation 设置默认屏幕显示习惯
         * @return
         */
        public Builder setOrientation(String orientation) {
            Orientation = orientation;
            return this;
        }

        /**
         * @param userForm 在没有请求到管理员在后端对sdk使用配置下，在默认的情况下，是否需要表单留言，true需要， false 不需要
         * @return
         */
        public Builder setUserForm(boolean userForm) {
            isUserForm = userForm;
            return this;
        }

        /**
         * @param defaultUserInfo 创建用户的基本信息
         */
        public Builder setDefaultUserInfo(Map<String, String> defaultUserInfo) {
            this.defaultUserInfo = defaultUserInfo;
            return this;
        }

        /**
         * @param definedUserTextField 创建自定义的文本信息
         */
        public Builder setDefinedUserTextField(Map<String, String> definedUserTextField) {
            this.definedUserTextField = definedUserTextField;
            return this;
        }

        /**
         * @param definedUserRoplist 创建自定义的列表信息
         */
        public Builder setDefinedUserRoplist(Map<String, String> definedUserRoplist) {
            this.definedUserRoplist = definedUserRoplist;
            return this;
        }

        /**
         * @param firstMessage 设置带入一条消息  会话分配就发送给客服
         */
        public Builder setFirstMessage(String firstMessage) {
            this.firstMessage = firstMessage;
            return this;
        }

        /**
         * @param robot_modelKey udesk 机器人常见问题 对应的Id值
         */
        public Builder setRobot_modelKey(String robot_modelKey) {
            this.robot_modelKey = robot_modelKey;
            return this;
        }

        /**
         * udesk 用于机器人页面收集客户信息
         *
         * @param concatRobotUrlWithCustomerInfo
         * @return
         */
        public Builder setConcatRobotUrlWithCustomerInfo(String concatRobotUrlWithCustomerInfo) {
            this.concatRobotUrlWithCustomerInfo = concatRobotUrlWithCustomerInfo;
            return this;
        }

        /**
         * @param customerUrl 设置客户的头像地址
         */
        public Builder setCustomerUrl(String customerUrl) {
            this.customerUrl = customerUrl;
            return this;
        }

        /**
         * @param commodity 配置发送商品链接的mode
         */
        public Builder setCommodity(UdeskCommodityItem commodity) {
            this.commodity = commodity;
            return this;
        }

        /**
         * @param txtMessageClick 文本消息中的链接消息的点击事件的拦截回调。 包含表情的不会拦截回调。
         */
        public Builder setTxtMessageClick(ITxtMessageWebonClick txtMessageClick) {
            this.txtMessageClick = txtMessageClick;
            return this;
        }

        /**
         * 商品消息点击回调
         * @param productMessageClick
         * @return
         */
        public Builder setProductMessageClick(IProductMessageWebonClick productMessageClick) {
            this.productMessageClick = productMessageClick;
            return this;
        }

        /**
         * 链接消息点击回调
         * @param linkMessageWebonClick
         * @return
         */
        public Builder setLinkMessageWebonClick(ILinkMessageWebonClick linkMessageWebonClick) {
            this.linkMessageWebonClick = linkMessageWebonClick;
            return this;
        }

        /**
         * 商品回复消息点击回调
         * @param replyProductMessageWebonClick
         * @return
         */
        public Builder setReplyProductMessageWebonClick(IReplyProductMessageWebonClick replyProductMessageWebonClick) {
            this.replyProductMessageWebonClick = replyProductMessageWebonClick;
            return this;
        }

        /**
         * 富文本链接消息点击回调
         * @param richMessageWebonClick
         * @return
         */
        public Builder setRichMessageWebonClick(IRichMessageWebonClick richMessageWebonClick) {
            this.richMessageWebonClick = richMessageWebonClick;
            return this;
        }

        /**
         * 图文消息点击回调
         * @param imgTxtMessageWebonClick
         * @return
         */
        public Builder setImgTxtMessageWebonClick(IImgTxtMessageWebonClick imgTxtMessageWebonClick) {
            this.imgTxtMessageWebonClick = imgTxtMessageWebonClick;
            return this;
        }

        /**
         * 结构化消息按钮链接回调
         * @param structMessageWebonClick
         * @return
         */
        public Builder setStructMessageWebonClick(IStructMessageWebonClick structMessageWebonClick) {
            this.structMessageWebonClick = structMessageWebonClick;
            return this;
        }

        /**
         * @param formCallBack 离线留言表单的回调接口：  如果不用udesk系统提供的留言功能，可以设置该接口  回调使用自己的处理流程
         */
        public Builder setFormCallBack(IUdeskFormCallBack formCallBack) {
            this.formCallBack = formCallBack;
            return this;
        }

        /**
         * @param structMessageCallBack 设置结构化消息的点击事件回调接口.
         * @return
         */
        public Builder setStructMessageCallBack(IUdeskStructMessageCallBack structMessageCallBack) {
            this.structMessageCallBack = structMessageCallBack;
            return this;
        }

        /**
         * @param extreFunctions            设置额外的功能按钮
         * @param functionItemClickCallBack 支持自定义功能按钮后 点击事件回调 直接发送文本,图片,视频,文件,地理位置,商品信息
         */
        public Builder setExtreFunctions(List<FunctionMode> extreFunctions, IFunctionItemClickCallBack functionItemClickCallBack) {
            this.extreFunctions = extreFunctions;
            this.functionItemClickCallBack = functionItemClickCallBack;
            return this;
        }

        /**
         * @param useNavigationRootView       设置是否使用导航UI rue表示使用 false表示不使用
         * @param navigationModes             约定传递的自定义按钮集合
         * @param navigationItemClickCallBack 支持客户在导航处添加自定义按钮的点击回调事件
         */
        public Builder setNavigations(boolean useNavigationRootView, List<NavigationMode> navigationModes,
                                      INavigationItemClickCallBack navigationItemClickCallBack) {
            this.isUseNavigationRootView = useNavigationRootView;
            this.navigationModes = navigationModes;
            this.navigationItemClickCallBack = navigationItemClickCallBack;
            return this;
        }

 /**
         * @param useRobotNavigationRootView       设置是否使用导航UI rue表示使用 false表示不使用
         * @param robotNavigationModes             约定传递的自定义按钮集合
         * @param navigationItemClickCallBack 支持客户在导航处添加自定义按钮的点击回调事件
         */
        public Builder setRobotNavigations(boolean useRobotNavigationRootView, List<NavigationMode> robotNavigationModes,
                                      INavigationItemClickCallBack navigationItemClickCallBack) {
            this.isUseRobotNavigationRootView = useRobotNavigationRootView;
            this.robotnavigationModes = robotNavigationModes;
            this.robotNavigationItemClickCallBack = navigationItemClickCallBack;
            return this;
        }


        /**
         * @param useNavigationSurvy 设置是否使用导航UI中的满意度评价UI rue表示使用 false表示不使用
         * @return
         */
        public Builder setUseNavigationSurvy(boolean useNavigationSurvy) {
            isUseNavigationSurvy = useNavigationSurvy;
            return this;
        }

        /**
         * @param useMapType                   设置使用那种地图  //百度地图   腾讯地图  //高德底图
         * @param locationMessageClickCallBack 点击地理位置信息的回调接口
         * @param cls                          传入打开地图消息显示的详请activity
         * @return
         */
        public Builder setUseMapSetting(String useMapType, Class<?> cls, ILocationMessageClickCallBack locationMessageClickCallBack) {
            this.useMapType = useMapType;
            this.locationMessageClickCallBack = locationMessageClickCallBack;
            this.cls = cls;
            return this;
        }

        /**
         * @param groupId         设置的指定组，每次进入都必须重新指定
         * @param isOnlyByGroupId
         */
        public Builder setGroupId(String groupId, boolean isOnlyByGroupId) {
            this.groupId = groupId;
            this.isOnlyByGroupId = isOnlyByGroupId;
            return this;
        }

        /**
         * @param agentId         设置指订客服，每次进入都必须重新指定
         * @param isOnlyByAgentId 指定是否只通过设定agentId进入会话的分配, 不再要机器人, 设置导航等判断
         */
        public Builder setAgentId(String agentId, boolean isOnlyByAgentId) {
            this.agentId = agentId;
            this.isOnlyByAgentId = isOnlyByAgentId;
            return this;
        }

        /**
         * @param onlyUseRobot 设置是否只使用机器人 不用其它功能
         * @return
         */
        public Builder setOnlyUseRobot(boolean onlyUseRobot) {
            isOnlyUseRobot = onlyUseRobot;
            return this;
        }

        /**
         * 设置商品消息
         *
         * @param mProduct
         * @return
         */
        public Builder setProduct(Product mProduct) {
            this.mProduct = mProduct;
            return this;
        }

        /**
         * 设置SDK自定义渠道
         * @param channel
         * @return
         */
        public Builder setChannel(String channel) {
            this.channel = channel;
            return this;
        }

        /**
         * 是否显示客户昵称
         * @param isShowCustomerNickname
         * @return
         */
        public Builder isShowCustomerNickname(boolean isShowCustomerNickname) {
            this.isShowCustomerNickname = isShowCustomerNickname;
            return this;
        }

        /**
         * 是否显示客户头像
         * @param isShowCustomerHead
         * @return
         */
        public Builder isShowCustomerHead(boolean isShowCustomerHead) {
            this.isShowCustomerHead = isShowCustomerHead;
            return this;
        }

        /**
         * 设置带入一条消息  进入机器人界面自动发送
         * @param preSendRobotMessages
         * @return
         */
        public Builder setPreSendRobotMessages(String preSendRobotMessages){
            this.preSendRobotMessages = preSendRobotMessages;
            return this;
        }

        /**
         * 设置智能提示的高度按比例
         * @param maxHeightViewRatio
         * @return
         */
        public Builder setMaxHeightViewRatio(float maxHeightViewRatio) {
            this.maxHeightViewRatio = maxHeightViewRatio;
            return this;
        }

        /**
         * 设置智能提示的高度
         * @param maxHeightViewDimen
         * @return
         */
        public Builder setMaxHeightViewDimen(float maxHeightViewDimen) {
            this.maxHeightViewDimen = maxHeightViewDimen;
            return this;
        }

        public UdeskConfig build() {
            return new UdeskConfig(this);
        }
    }


    public static class OrientationValue {
        public static final String landscape = "landscape"; //横屏
        public static final String portrait = "portrait";//竖屏
        public static final String user = "user";//用户当前的首选方向
    }


    //mode: mark (默认,标记放弃)/ cannel_mark(取消标记) / force_quit(强制立即放弃)
    public static class UdeskQueueFlag {
        public static final String Mark = "mark";
        public static final String FORCE_QUIT = "force_quit";
        public static final String CANNEL_MARK = "cannel_mark";
    }

    public static class UdeskPushFlag {
        public static final String ON = "on";
        public static final String OFF = "off";
    }

    public static class UdeskMapType {
        //百度地图
        public static final String BaiDu = "baidu";
        //腾讯地图
        public static final String Tencent = "tencent ";

        //高德底图
        public static final String GaoDe = "amap ";

        //其它地图
        public static final String Other = "other ";

    }

    public static class UdeskMapIntentName {
        //选中的位置
        public static final String Position = "udesk_position";

        //选中位置周边位置的截图存储的本地路径
        public static final String BitmapDIR = "udesk_bitmap_dir";

        //选中位置的纬度
        public static final String Latitude = "udesk_latitude";

        //选中位置的经度
        public static final String Longitude = "udesk_longitude";
    }

}
