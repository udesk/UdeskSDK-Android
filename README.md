# UdeskSDK Android 3.2.1 开发者文档

## 集成UdeskSDK

###1下载UdeskSDK

解压后Udesk_Separate_UI做为独立的module import, 在你APP build.gradle文件中加入：
``` java
dependencies {
    compile project(':Udesk_Separate_UI')
}
```
###2初始化
``` java
UdeskSDKManager.getInstance().initApiKey(this, "You domain","You Secretkey");
```
如果您还没有注册域名和创建应用的Secretkey，请你到[Udesk 官网注册][1]，获得你独立的域名(domain)和生成你独立的密钥(secretkey)。
###3创建顾客信息
Udesk系统提供了一些默认的顾客信息，如果你们觉得不够，可以通过管理员账号登入Udesk系统，创建自定义字段。

####3.1使用Udesk系统默认的字段创建顾客信息：
``` java
String sdktoken = “用户唯一的标识”; 
Map<String, String> info = new HashMap<String, String>();
//sdktoken 信息一定要填写
info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, sdktoken);
//以下信息是可选
info.put(UdeskConst.UdeskUserInfo.NICK_NAME,"昵称");
//输入邮箱
info.put(UdeskConst.UdeskUserInfo.EMAIL,"0631@163.com");
//输入手机号
info.put(UdeskConst.UdeskUserInfo.CELLPHONE,"15651818750");
//输入微信账号
info.put(UdeskConst.UdeskUserInfo.WEIXIN_ID,"huahua");
//输入微博名字
info.put(UdeskConst.UdeskUserInfo.WEIBO_NAME,"weiboName")
//输入QQ号
info.put(UdeskConst.UdeskUserInfo.QQ,"506313505");
info.put(UdeskConst.UdeskUserInfo.DESCRIPTION,"描述信息")
UdeskSDKManager.getInstance().setUserInfo(this, sdktoken, info);
```
####3.2使用自定义字段创建顾客信息：

#####3.2.1添加用户自定义字段 
用管理员账号登录后台，在[管理中心-用户字段]中添加自定义字段。
![alt text](indeximg/用户自定义.png)
#####3.2.2获取自定义字段信息
``` java
UdeskHttpFacade.getInstance().getUserFields(UDESK_DOMAIN, UDESK_SECRETKEY, new UdeskCallBack(){

	@Override
	public void onSuccess(String message) {
		
	}

	@Override
	public void onFail(String message) {

	}
});
```
#####3.2.3给自定义字段赋值
用户自定义字段共有两类：文本型字段和选择型字段。 
文本型字段示例：
``` java
{
      "field_name": "TextField_684",
      "field_label": "地址",
      "content_type": "text",
      "comment": "字段描述",
      "options": null,
      "permission": 0,
      "requirment": false
}
取该json中字段“field_name”对应的value值作为自定义字段key值进行赋值。 示例如下：
textFieldMap.put("TextField_684","北京西城区");
```
选择型字段示例：
``` java
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
取该json中字段“field_name”对应的value值作为自定义字段key值进行赋值,取"options"中的某一项key值作为value，示例如下：
roplistMap.put("SelectField_457","1");
```
#####3.2.4创建顾客信息
假设都有值， 调用如下，
``` java
UdeskSDKManager.getInstance().setUserInfo(this, sdktoken, info,textFieldMap, roplistMap);
```

####3.3 用户创建及更新逻辑
``` java
1使用主键 sdk_token email cellphone 依次查找用户,找到转1.1

     1.1 设找到的用户为customer

     1.2 如果有 sdk_token 参数并且不与 customer中原有的sdk_token相同, go 1.2.1

     1.2.1 更新用户主键及附加信息

     1.3 更新 device

2创建用户 email 没有会默认生成

3创建用户 device

创建用户失败返回的常见错误
   wrong_subdomain: {code: "2001" , message: "子域名错误"},

    no_sdktoken: {code: "2003" , message: "用户token错误"},

    wrong_sign: {code: "3001" , message: "签名错"},

    agent: {code: "5050" , message: "客服不存在"},

    user_group: {code: "5060" , message: "客服组不存在"}
    
    validate_error: {:code=>"2004", :message=>"验证错", :exception=>"Validation failed: Email is invalid"}

    exception 中会带有具体验证错误信息 

```
**注意sdktoken** 是顾客的唯一标识，用来识别身份，**sdk_token: 传入的字符请使用 字母 / 数字 等常见字符集**  。就如同身份证一样，不允许出现一个身份证号对应多个人，或者一个人有多个身份证号;**其次**如果给顾客设置了邮箱和手机号码，也要保证不同顾客对应的手机号和邮箱不一样，如出现相同的，则不会创建新顾客。  **完成了以上操作，接下来就可以使用UdeskSDK的其它功能了，祝你好运！**

# UdeskSDK功能使用
###1咨询客服
直接进入人工客服会话界面。系统根据客服分配规则安排客服接待；如果没有在线客服，则提示用户留言。
``` java
 UdeskSDKManager.getInstance().toLanuchChatAcitvity(this);
```
###2咨询机器人客服
机器人客服需确保后台已开启机器人客服功能及开通“移动SDK”渠道。
``` java
  UdeskSDKManager.getInstance().showRobot(this);
```
**提醒通过后台配置机器人客服会话界面还提供入口转人工客服界面**

###3后台配置，灵活的选择咨询客服对象
如果后台开通了机器人客服，则进入机器人会话界面，如果没开通则进入人工客服会话界面
``` java
  UdeskSDKManager.getInstance().showRobotOrConversation(this);
```
### 4指引客户选择客服组
####4.1在后台添加自定义菜单
管理员在[管理中心-渠道管理-即时通讯-自定义链接-使用导航菜单]中添加自定义菜单，引导客户选择客服组。如果客户选择的客服组没有客服在线时，系统会提示用户留言。
![alt text](indeximg/指引客户选择客服组.png)
####4.2指引选择客服组进入会话界面
``` java
  UdeskSDKManager.getInstance().showConversationByImGroup(this);
```
###5指定客服
####5.1客服id获取方式
管理员在[管理中心-渠道管理-即时通讯-自定义链接-指定客服和客服组]中选择指定的客服，这样在上方链接中就会加载指定客服的id。如下图所示，客服“测试2”的agentId是“4418”。
![alt text ](indeximg/指定客服.png)
####5.2通过客服ID指定客服
``` java
String agentId = "4418";
UdeskSDKManager.getInstance().lanuchChatByAgentId(this,agentId);
```
###6指定客服组
####6.1客服组id获取方式
管理员在[管理中心-渠道管理-即时通讯-自定义链接-指定客服和客服组]中选择指定的客服组，这样在上方链接中就会加载指定客服组的id。如下图所示，客服组“客服部”的groupId是“9831”。
![alt text ](indeximg/指定客服组.png)
####6.2通过客服组ID指定客服组
``` java
String groupId = "9831";
UdeskSDKManager.getInstance().lanuchChatByGroupId(this,groupId);
```
###7帮助中心
Udek系统后台可以设置常见的问题解答，顾客通过帮助中心可查看所有的问题列表，并可查看设置问题的具体内容。
启动帮助中心界面：
``` java
UdeskSDKManager.getInstance().toLanuchHelperAcitivty(this);
```
###8更新顾客信息
####8.1更新Udesk系统默认的提供的信息
``` java
Map<String, String> info = new HashMap<String, String>();
info.put(UdeskConst.UdeskUserInfo.NICK_NAME,"更新后的昵称");
//更新后的邮箱
info.put(UdeskConst.UdeskUserInfo.EMAIL,"0631@163.com");
//更新后的手机号
info.put(UdeskConst.UdeskUserInfo.CELLPHONE,"15651818750");
//更新后的微信账号
info.put(UdeskConst.UdeskUserInfo.WEIXIN_ID,"huahua");
//更新后的微博名字
info.put(UdeskConst.UdeskUserInfo.WEIBO_NAME,"weiboName")
//更新后的QQ号
info.put(UdeskConst.UdeskUserInfo.QQ,"506313505");
info.put(UdeskConst.UdeskUserInfo.DESCRIPTION,"更新后的描述信息")

//传入需要更新的Udesk系统默认字段
UdeskSDKManager.getInstance().setUpdateUserinfo(info);
**更新邮箱或者手机号码，如果在后端有同样的手机号或邮箱，则会更新失败**
```
####8.2更新自定义字段
参考创建顾客信息时的添加自定义字段和获取自定义字段
文本型字段示例：
``` java
{
      "field_name": "TextField_684",
      "field_label": "地址",
      "content_type": "text",
      "comment": "字段描述",
      "options": null,
      "permission": 0,
      "requirment": false
}
取该json中字段“field_name”对应的value值作为自定义字段key值进行赋值。 示例如下：
updateTextFieldMap.put("TextField_684","北京西城区");

//传入需要更新的自定义文本字段
 UdeskSDKManager.getInstance().setUpdateTextField(updateTextFieldMap);
          
```
选择型字段示例：
``` java
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
取该json中字段“field_name”对应的value值作为自定义字段key值进行赋值,取"options"中的某一项key值作为value，示例如下：
updateRoplistMap.put("SelectField_457","1");

//传入需要更新的自定义下拉列表字段
UdeskSDKManager.getInstance().setUpdateRoplist(updateRoplistMap);
```
###9获取新消息的通知
在离开会话界面后，没有断开xmpp服务的连接，注册了获取通知消息的方法，则可获得通知上来的消息。注册事件说明 如demo中的注册事件：
``` java
注册了方法"OnNewMsgNotice"
UdeskMessageManager.getInstance().event_OnNewMsgNotice.bind(this, "OnNewMsgNotice");

OnNewMsgNotice方法的实现
public void OnNewMsgNotice(MsgNotice msgNotice) {
	if (msgNotice != null) {
		NotificationUtils.getInstance().notifyMsg(UdeskCaseActivity.this, msgNotice.getContent());
	}

}
注意：1 消息上报的对象是：MsgNotice ；2 注册的方法和实现的方法  字符串必须保证一致分大小写；
      3 实现的方法  必须public修饰。
```
###10获取当前会话未读消息数
sdk 3.2.0版本开始，可在退出会话界面后，在本地应用与xmpp服务器连接没断开期间，可获得这个会话的未读消息数。
``` java
UdeskSDKManager.getInstance().getCurrentConnectUnReadMsgCount();
```
###11删除顾客聊天数据
sdk接入成功后，可删除当前顾客的聊天记录信息
``` java
UdeskSDKManager.getInstance().deleteMsg();
```

###12断开xmpp连接
如果你需要主动断开xmpp的连接，可按如下调用:
``` java
UdeskSDKManager.getInstance().disConnectXmpp();
```
###13控制台日志开关
如果开发中，相在控制台xmpp交互报文，可通过如下接口设置
``` java
//true 表示开启控制台日志  false表示关闭控制台日志
UdeskSDKManager.getInstance().isShowLog(true);
```
###14Android M 权限处理
采用开源库rxpermissions 依赖如下库
compile 'com.tbruyelle.rxpermissions:rxpermissions:0.7.0@aar'
compile 'io.reactivex:rxjava:1.1.4'

选取rxpermissions 原因是github上目赞和使用最多，使用方便简单。
举例拍照的使用代码:
``` java
RxPermissions.getInstance(this)
	.request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
	.subscribe(new Action1<Boolean>() {
		@Override
		public void call(Boolean aBoolean) {
			if (aBoolean) {
				takePhoto();
				bottomoPannelBegginStatus();
			} else {
				Toast.makeText(UdeskChatActivity.this,
						getResources().getString(R.string.camera_denied),
						Toast.LENGTH_SHORT).show();
			}
		}
	});  
其它的动态权限可以看UdeskChatActivity代码。
```

###15发送商品连接
支持发送商品的信息，目前最多支持发送商品的4个属性(detail,image,title,url)，如下例子所示
``` java
//创建商品的实例
UdeskCommodityItem item = new UdeskCommodityItem();
// 商品主标题
item.setTitle("木林森男鞋新款2016夏季透气网鞋男士休闲鞋网面韩版懒人蹬潮鞋子");
//商品描述
item.setSubTitle("¥ 99.00");
//左侧图片
item.setThumbHttpUrl("https://img.alicdn.com/imgextra/i1/1728293990/TB2ngm0qFXXXXcOXXXXXXXXXXXX_!!1728293990.jpg_430x430q90.jpg");
// 商品网络链接
item.setCommodityUrl("https://detail.tmall.com/item.htm?spm=a1z10.3746-b.w4946-14396547293.1.4PUcgZ&id=529634221064&sku_properties=-1:-1");

UdeskSDKManager.getInstance().setCommodity(item);    

//发送商品信息 见ChatActivityPresenter类中的sendCommodityMessage方法
public void sendCommodityMessage(UdeskCommodityItem commodityItem) {
UdeskMessageManager.getInstance().sendComodityMessage(buildCommodityMessage(commodityItem),
        mChatView.getAgentInfo().getAgentJid());
}


//商品信息xmpp最后message报文
<message to='agent_5236_3055@im03.udesk.cn' id='Tozz0-59' type='chat'>
	<product xmlns="udesk:product">
	{&quot;data&quot;:{
			&quot;product_params&quot;:{&quot;detail&quot;:&quot;¥ 99.00&quot;},
			&quot;image&quot;:&quot;https:\/\/img.alicdn.com\/imgextra\/i1\/1728293990\/TB2ngm0qFXXXXcOXXXXXXXXXXXX_!!1728293990.jpg_430x430q90.jpg&quot;,
			&quot;title&quot;:&quot;木林森男鞋新款2016夏季透气网鞋男士休闲鞋网面韩版懒人蹬潮鞋子&quot;,
			&quot;url&quot;:&quot;https:\/\/detail.tmall.com\/item.htm?spm=a1z10.3746-b.w4946-14396547293.1.4PUcgZ&amp;id=529634221064&amp;sku_properties=-1:-1&quot;
			},
	&quot;type&quot;:&quot;product&quot;,
	&quot;platform&quot;:&quot;android&quot;
	}
	</product>
</message>
```

## UdeskSDK内部封装好支持的功能
1###支持的消息类型 
收发文本，语音，图片，富文本信息。在项目中定义的标识为：

``` java
    /**
     * 收到的文本消息标识
     */
    private static final int MSG_TXT_L = 0;
    /**
     * 发送的文本消息标识
     */
    private static final int MSG_TXT_R = 1;
    /**
     * 收到的语音消息标识
     */
    private static final int MSG_AUDIO_L = 2;
    /**
     * 发送的语音消息标识
     */
    private static final int MSG_AUDIO_R = 3;
    /**
     * 收到图片消息标识
     */
    private static final int MSG_IMG_L = 4;
    /**
     * 发送图片消息标识
     */
    private static final int MSG_IMG_R = 5;
    /**
     * 收到转移客服消息标识
     */
    private static final int MSG_REDIRECT = 6;
    /**
     * 收到富文本消息标识
     */
    private static final int RICH_TEXT = 7;
    /**
     * 发送商品链接本消息标识
     */
    private static final int COMMODITY = 8;
```
####1.1 发送文本消息
``` java
     //发送文本消息  见ChatActivityPresenter
    public void sendTxtMessage(String msgString) {
        MessageInfo msg = buildSendMessage(
                UdeskConst.ChatMsgTypeString.TYPE_TEXT,
                System.currentTimeMillis(), msgString, "");
        saveMessage(msg);
        mChatView.clearInputContent();
        mChatView.addMessage(msg);
        UdeskMessageManager.getInstance().sendMessage(msg.getMsgtype(),
                msg.getMsgContent(), msg.getMsgId(),
                mChatView.getAgentInfo().getAgentJid(), msg.getDuration());
        UdeskDBManager.getInstance().addSendingMsg(msg.getMsgId(),
                UdeskConst.SendFlag.RESULT_SEND, System.currentTimeMillis());
    }
    //xmpp最后message报文
    <message to='agent_5236_3055@im03.udesk.cn' id='6314149748198604802' type='chat'>
    <body>
	   {
	     &quot;data&quot;:{&quot;content&quot;:&quot;测试信息&quot;,&quot;duration&quot;:0},
	     &quot;type&quot;:&quot;message&quot;,
		 &quot;platform&quot;:&quot;android&quot;,
		 &quot;version&quot;:&quot;3.2.1&quot;
		}
	</body>
	<request xmlns='urn:xmpp:receipts'/>
</message>	
```
####1.2 发送预输入消息

``` java
//发送输入预支消息  见ChatActivityPresenter
public void sendPreMessage() {
     UdeskMessageManager.getInstance().sendPreMsg(UdeskConst.ChatMsgTypeString.TYPE_TEXT,
		mChatView.getInputContent().toString(), mChatView.getAgentInfo().getAgentJid());
}

//xmpp最后预输入消息message报文    premsg= "true" 标识预输入消息
<message to='agent_5236_3055@im03.udesk.cn' id=' ' type='chat'>
	<body>
		{
			&quot;data&quot;:{&quot;content&quot;:&quot;&quot;},
			&quot;type&quot;:&quot;message&quot;,
			&quot;platform&quot;:&quot;android&quot;,
			&quot;version&quot;:&quot;3.2.1&quot;
		}
	</body>
	<premsg xmlns="udesk:premsg" premsg= "true"></premsg>
</message>

```
####1.3 发送语音消息
``` java
//xmpp发语音消息message报文
<message to='agent_5236_3055@im03.udesk.cn' id='6314152806215319555' type='chat'>
	<body>
		{
			&quot;data&quot;:
				{   
				   &quot;content&quot;:&quot;http:\/\/qnudeskim.flyudesk.com\/audio_20160802_165740.aac&quot;,
				   &quot;duration&quot;:3
				 },
			&quot;type&quot;:&quot;audio&quot;,
			&quot;platform&quot;:&quot;android&quot;,
			&quot;version&quot;:&quot;3.2.2&quot;
		}
	</body>
	<request xmlns='urn:xmpp:receipts'/>
</message>

```
####1.4 发送图片消息
``` java
//xmpp发图片消息message报文
<message to='agent_5236_3055@im03.udesk.cn' id='6314154919339229186' type='chat'>
	<body>
		{
			&quot;data&quot;:{&quot;content&quot;:&quot;http:\/\/qnudeskim.flyudesk.com\/eaa5d186b72abd4a44b9ef211ec6bc83&quot;,&quot;duration&quot;:0},
			&quot;type&quot;:&quot;image&quot;,
			&quot;platform&quot;:&quot;android&quot;,
			&quot;version&quot;:&quot;3.2.1&quot;
		}
	</body>
	<request xmlns='urn:xmpp:receipts'/>
</message>
```

####1.5支持满意度调查
#####1.5.1客服在pc端叉掉会话，sdk会受到一条满意度调查信息。
``` java
//满意度调查xmpp报文消息  survey='true'  标识调查
<message from='agent_13988_3055@im03.udesk.cn/3201253852559793907391870209273268523016675556031893299406' 
	to='customer_8698750_3055@im03.udesk.cn' type='chat' nick='许一' user_id='13988'>
		isreqsurvey xmlns='survey' survey='true'/>
</message>
```
#####1.5.2处理满意度选项
移动端会在见ChatActivityPresenter中处理收到满意度调查消息，获取满意度选项，弹出满意度调查框，可提交满意度选项
``` java
//收到满意度调查消息
public void onReqsurveyMsg(Boolean isSurvey) {

     if (isSurvey) {
         getIMSurveyOptions();
     }
}
```
#####1.5.3启动满意度调查
``` java
private void toLuanchSurveyActivity(SurveyOptionsModel surveyOptions) {
	Intent intent = new Intent();
	intent.setClass(UdeskChatActivity.this, SurvyDialogActivity.class);
	intent.putExtra(UdeskConst.SurvyDialogKey, surveyOptions);
	startActivityForResult(intent, SELECT_SURVY_OPTION_REQUEST_CODE);
}
```
#####1.5.4提交满意度调查
``` java
    Toast.makeText(UdeskChatActivity.this, "感谢您的评价！", Toast.LENGTH_SHORT).show();
    String optionId = data.getStringExtra(UdeskConst.SurvyOptionIDKey);
    mPresenter.putIMSurveyResult(optionId);
```

####1.6支持客服不在线留言
启动留言表单界面
当前客服繁忙或者不在线	，输入内容发送消息，弹出留言对话框提示。如果客服点击则跳转到表单界面
 ``` java
protected void goToForm() {
      Intent intent = new Intent(UdeskChatActivity.this,UdeskFormActivity.class);
      startActivity(intent);
      dismissFormWindow();
}
```    

####1.7支持客服在pc端拉黑客户和移除拉黑

####1.8支持客服在pc转移客服

## 集成UdeskSDK中使用的接口说明
  接口已开发实现，不需要再开发。
``` java
    "/udesk_im/sdk/v3/im/customers.json"  //创建客户信息并提交设备信息
    "/api/v2/im.json"; // 创建客户信息成功后调用，获取当前顾客xmpp登录信息
    "/udesk_im/sdk/v3/im/agent.json"; // 请求获取客服进行会话
    "/api/v1/articles.json?sign="; //帮助中心获取文章列表的接口
     "/api/v1/articles/search.json";  //帮助中心搜索获取匹配的文章接口    
    "/api/v2/user_fields.json";	// 获取设置顾客自定义字段的接口
    "/udesk_im/sdk/v3/im/im_survey.json"; // 获取满意度评价内容的接口	
    "/udesk_im/sdk/v3/im/surveys.json";	//提交满意度评价的接口
    "/udesk_im/sdk/v3/im/im_group.json"; // 获取客服组列表接口
    "/udesk_im/sdk/v3/im/status.json"; //获取客服状态的接口
		
```

##主要的界面说明
 聊天界面UdeskChatActivity中的MessageAdatper
 聊天界面的的MessageAdatper： 里面进行展示语音，文本，图片等消息。对应的布局界面：
``` java
     udesk_chat_msg_item_txt_l,//文本消息左边的UI布局文件
	 udesk_chat_msg_item_txt_r,//文本消息右边的UI布局文件
	 udesk_chat_msg_item_audiot_l,//语音消息左边的UI布局文件
	 udesk_chat_msg_item_audiot_r,//语音消息右边的UI布局文件
	 udesk_chat_msg_item_imgt_l,//图片消息左边的UI布局文件
	 udesk_chat_msg_item_imgt_r,//图片消息右边的UI布局文件
	 udesk_chat_msg_item_redirect,//转移消息提示信息UI布局文件
	 udesk_chat_rich_item_txt,//富文本消息UI布局文件
	 udesk_im_commodity_item  //显示广告商品信息的UI布局文件
	 
	 对应的ViewHolder分别是：
	 TxtViewHolder  显示文本消息;
	 AudioViewHolder 显示语音消息；
	 ImgViewHolder    显示图片消息；
	 RichTextViewHolder 显示富文本消息；
	 CommodityViewHolder 显示广告商品信息；
	 RedirectViewHolder  显示转移提示语信息；
```
其它对应的UI
``` java
    UdeskHelperActivity  帮助中心界面；
    UdeskHelperArticleActivity   显示一篇文章的具体内容
    UdeskRobotActivity   机器人会话界面    
    SurvyDialogActivity  满意度对话框
    UdeskConfirmPopWindow 弹出离线表单的PopWindow 
    UdeskPopVoiceWindow  录音的PopWindow
    UdeskExpandableLayout 提示客服上下线的动画
    UdeskTitleBar 标题栏
```
##UdeskSDK Udesk_Separate_UImodule的依赖
``` java
    compile files('libs/qiniu-android-sdk-7.0.1.jar')
    compile files('libs/universal-image-loader-1.9.4.jar')
    compile files('libs/bugly_crash_release__2.1.jar')
    compile 'org.igniterealtime.smack:smack-android-extensions:4.1.0'
    compile 'org.igniterealtime.smack:smack-tcp:4.1.0'
    compile 'com.tbruyelle.rxpermissions:rxpermissions:0.7.0@aar'
    compile 'io.reactivex:rxjava:1.1.4'
    compile files('libs/android-async-http-1.4.6.jar')
    compile files('libs/udesk_sdk_3.2.1.jar')	
```

##UdeskSDK混淆忽略设置
``` java
-keep class udesk.core.** {*;} 
-keep class cn.udesk.**{*; } 
-keep class com.loopj.android.http.** {*; } 
-keep class com.tencent.bugly.** {*; } 
-keep class com.qiniu.android.** {*;} 
-keep class com.nostra13.universalimageloader.** {*;} 
-keep class org.jxmpp.** {*;} 
-keep class de.measite.** {*;} 
-keep class rx.** {*;} 
-keep class org.jivesoftware.** {*;} 
-keep class org.xmlpull.** {*;} 
-keep classcom.tbruyelle.rxpermissions.** {*;}
```
[1]:http://www.udesk.cn/
