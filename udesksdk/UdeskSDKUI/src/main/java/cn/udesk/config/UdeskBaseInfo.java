package cn.udesk.config;

import java.util.Map;

import cn.udesk.model.UdeskCommodityItem;

/**
 * Created by user on 2017/1/4.
 */

public class UdeskBaseInfo {
    /**
     * 注册udesk系统生成的二级域名
     */
    public static String domain = "";
    /**
     * udesk系统创建应用生成的App Id
     */
    public static String App_Id = "";

    /**
     * udesk系统创建应用生成的App Key
     */
    public static  String App_Key = "";
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
     * 用户需要更新自定义列表字段信息
     */
    public static  Map<String, String> updateRoplist = null;

    //相关推送平台注册生成的ID
    public static String registerId = "";

    /**
     *  创建用户成功时  生成的id值
     */
    public static String customerId = null;

    /**
     * 保存客户的头像地址，由用户app传递
     */
    public static String customerUrl = null;

    /**
     * 控制在线时的消息的通知  在聊天界面的时候 关闭，不在聊天的界面的开启
     */
    public static boolean isNeedMsgNotice = true;

    //发送商品链接的mode
    public static UdeskCommodityItem commodity = null;

    public static String sendMsgTo = "";

}
