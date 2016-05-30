# UdeskSDK-Android
UdeskSDK-Android


##Android新版说明
> Udesk为了让开发者更好的集成移动SDK,与企业业务结合更加紧密，我们开源了SDK的UI界面。用户可以根据自身业务以及APP的不同风格重写页面。当然开发者也可以用我们提供的默认的界面。
##一、接口详细说明
> Udesk-SDK方法都封装在UdeskSDKManager类中,采用单例。通过UdeskSDKManager.getInstance().对应的方法调用。

> **特别注意：第一步必需是调用initApiKey,第二步是必需调用setUserInfo 。 其它的api的使用都必需在第一步和第二步之后,根据集成时的需求调用，否则会出错误。 如果需要在同一个域名和共享密钥下切换用户，则可跳过第一步，直接调用第二步setUserInfo()。**
##1、初始化SDK (必须调用)
####初始化SDK，传入Udesk专属域名和共享的密钥。
initApiKey(Context context,String domain, String secretKey);

> 参数说明
![alt text](indeximg/introduction.png)
###2、创建用户，上传用户的信息 （必须调用）
> setUserInfo(final Context context,String sdk_Token,Map<String, String> info,Map<String, String> textField,Map<String, String> roplist);
![alt text](indeximg/introduction2.png)
###Info存放用户基本信息示例：
private Map<String, String> getUserInfo() {

        Map<String, String> info = new HashMap<String, String>();
        info.put(UdeskConst.UdeskUserInfo.NICK_NAME,”这是个昵称”);
        info.put(UdeskConst.UdeskUserInfo.EMAIL, "test@163.com");
        info.put(UdeskConst.UdeskUserInfo.CELLPHONE, "13800000000");
        info.put(UdeskConst.UdeskUserInfo.WEIXIN_ID, "这填写微信的ID号");
        info.put(UdeskConst.UdeskUserInfo.WEIBO_NAME, "这是微博的账号");
        info.put(UdeskConst.UdeskUserInfo.QQ, "321321321");
        info.put(UdeskConst.UdeskUserInfo.DESCRIPTION, "这填写的是描述信息");

        return info;

    }
**特别注意：如果需要给用户自定义字段赋值，请先确保已在后台添加用户自定义字段，然后调用接口获取用户自定义字段，再进行赋值。**
####添加用户自定义字段 用管理员账号登录后台，在[管理中心-用户字段]中添加自定义字段。
![alt text](indeximg/用户自定义.png)
####调用接口获取用户自定义字段并赋值。
getCustomeUserfields();
####用户自定义字段共有两类：文本型字段和选择型字段。 文本型字段示例：
{
      "field_name": "TextField_684",
      "field_label": "地址",
      "content_type": "text",
      "comment": "字段描述",
      "options": null,
      "permission": 0,
      "requirment": false
    }
####当需要给某一个自定义字段赋值时，取该字段的“field_name”作为key进行赋值。 示例如下：

    extraInfoTextField.put(
            child.getString("field_name"),
            "北京西城区");
####选择型字段示例：
{
    "field_name": "SelectField_457", 
    "permission": 0, 
    "comment": "这是描述", 
    "requirment": true, 
    "content_type": "droplist", 
    "field_label": "性别", 
    "options": [
        {
            "0": "男"
        }, 
        {
            "1": "女"
        }
    ]
}   
####当开发者需要对选择型自定义字段赋值时，同样取"field_name"作为key值，取"options"中的key值作为value赋值。示例如下：
extraInfodRoplist.put(
    child.getString("field_name"),"0");
##3、进入人工客服
####直接进入人工客服界面。系统根据客服分配规则安排客服接待；如果没有在线客服，则提示用户留言。
toLanuchChatAcitvity(Context context)
##4、进入机器人智能客服
####智能选择客服，如果后台开通了机器人客服，则进入机器人会话界面，如果没开通则进入人工客服界面。
showRobotOrConversation( Context context);
##5、进入机器人客服
####进入机器人客服聊天界面。
showRobot(final Context context);

**特别注意：机器人客服需确保后台已开启机器人客服功能及开通“移动SDK”渠道。**
##6、指引客户选择客服组
####管理员在[管理中心-渠道管理-即时通讯-自定义链接-使用导航菜单]中添加自定义菜单，引导客户选择客服组。如果客户选择的客服组没有客服在线时，系统会提示用户留言。
![alt text](indeximg/指引客户选择客服组.png)
showConversationByImGroup( Context context);
##7、指定客服
####开发者可以调用接口指定客服接待用户。如果指定客服不在线，系统会提示用户留言。
lanuchChatByAgentId(Context context,String agentId);//agentId 客服Id
####客服id获取方式：管理员在[管理中心-渠道管理-即时通讯-自定义链接-指定客服和客服组]中选择指定的客服，这样在上方链接中就会加载指定客服的id。如下图所示，客服“测试2”的agentId是“4418”。
![alt text ](indeximg/指定客服.png)
##8、指定客服组
####开发者可以调用接口指定客服组接待用户，如果指定客服组没有在线客服，系统会提示用户留言。
lanuchChatByGroupId(Context context, String groupId);//groupId 客服组Id
####客服组id获取方式：管理员在[管理中心-渠道管理-即时通讯-自定义链接-指定客服和客服组]中选择指定的客服组，这样在上方链接中就会加载指定客服组的id。如下图所示，客服组“客服部”的groupId是“9831”。
![alt text ](indeximg/指定客服组.png)
##9、 进入帮助中心
####进入帮助中心界面
toLanuchHelperAcitivty(Context context);
##10、控制台日志开关
####控制台日志开关，设置true，可看日志，false关闭
isShowLog(boolean isShow);



