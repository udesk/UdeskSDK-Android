package cn.udesk.config;

import java.util.Map;

/**
 * Created by user on 2016/8/12.
 */
public class UdeskConfig {

    /**
     * 用户唯一的标识
     */
    public static String appid = "";
    public static String domain = "";
    public static  String secretKey = "";
    /**
     * 用户唯一的标识
     */
    public static String sdkToken = null;
    /**
     * 用户的基本信息
     */
    public static Map<String, String> userinfo = null;

    /**
     * 用户自定义字段文本信息
     */
    public static Map<String, String> textField = null;


    /**
     * 用户自定义字段的列表信息
     */
    public static Map<String, String> roplist = null;

    /**
     * 用户需要更新的基本信息
     */
    public static Map<String, String> updateUserinfo = null;

    /**
     * 用户需要更新自定义字段文本信息
     */
    public static Map<String, String> updateTextField = null;

    /**
     * 用户需要更新自定义字段的列表信息
     */
    public static  Map<String, String> updateRoplist = null;

    //相关推送平台注册生成的ID
    public static String registerId = "";


    public static final int DEFAULT = -1;

    // 标题栏TitleBar的背景色  通过颜色设置
    public static int udeskTitlebarBgResId = DEFAULT;

    // 标题栏TitleBar，左右两侧文字的颜色
    public static int udeskTitlebarTextLeftRightResId = DEFAULT;

    //IM界面，左侧文字的字体颜色
    public static int udeskIMLeftTextColorResId = DEFAULT;

    //IM界面，右侧文字的字体颜色
    public static int udeskIMRightTextColorResId = DEFAULT;

    //IM界面，左侧客服昵称文字的字体颜色
    public static int udeskIMAgentNickNameColorResId = DEFAULT;

    //IM界面，时间文字的字体颜色
    public static int udeskIMTimeTextColorResId = DEFAULT;

    // IM界面，提示语文字的字体颜色，比如客服转移
    public static int udeskIMTipTextColorResId = DEFAULT;

    // 返回箭头图标资源id
    public static int udeskbackArrowIconResId = DEFAULT;

    // 咨询商品item的背景颜色
    public static int udeskCommityBgResId = DEFAULT;

    //    商品介绍Title的字样颜色
    public static int udeskCommityTitleColorResId = DEFAULT;

    //  商品咨询页面中，商品介绍子Title的字样颜色
    public static int udeskCommitysubtitleColorResId = DEFAULT;

    //    商品咨询页面中，发送链接的字样颜色
    public static int udeskCommityLinkColorResId = DEFAULT;

}
