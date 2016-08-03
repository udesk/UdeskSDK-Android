# UdeskSDK Android 3.2.1 开发者文档
## 一、SDK工作流程图
Udesk-SDK的工作流程如下图所示。

![udesk](http://7xr0de.com2.z0.glb.qiniucdn.com/ios3.png)
## 二、下载和集成SDK

####2.1[下载Udesk SDK](https://github.com/udesk/UdeskSDK-Android)

#### 2.2集成到AndroidStudio

2.2.1解压后文件介绍

| SDK 中的文件          | 说明                                       |
| ----------------- | ---------------------------------------- |
| UdeskUIDemo       | UdeskSdk集成demo,提供对Udesk_Separate_UI module使用的例子 |
| Udesk_Separate_UI | UdeskSDK开源moudle                         |

2.2.2导入集成

你所要做的是把Udesk_Separate_UI做为独立的module import, 并在你APP build.gradle文件中加入：

``` java
dependencies {
    compile project(':Udesk_Separate_UI')
}
```
## 三、快速集成SDK

###3.1初始化

获取密钥和公司域名（xxxx.udesk.cn）

![udesk](http://7xr0de.com1.z0.glb.clouddn.com/key.jpeg)



使用公司域名和密钥初始化SDK

``` java
UdeskSDKManager.getInstance().initApiKey(this, "You domain","You Secretkey");
```
###3.2初始化客户信息

注意：若要在SDK中使用 客户自定义字段 需先在管理员网页端设置添加用户自定义字字段。 

默认系统字段是Udesk已定义好的字段，开发者可以直接传入这些用户信息，供客服查看。

``` java
String sdktoken = “用户唯一的标识”; 
Map<String, String> info = new HashMap<String, String>();
//sdktoken 必填
info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, sdktoken);
//以下信息是可选
info.put(UdeskConst.UdeskUserInfo.NICK_NAME,"昵称");
info.put(UdeskConst.UdeskUserInfo.EMAIL,"0631@163.com");
info.put(UdeskConst.UdeskUserInfo.CELLPHONE,"15651818750");
info.put(UdeskConst.UdeskUserInfo.DESCRIPTION,"描述信息")
UdeskSDKManager.getInstance().setUserInfo(this, sdktoken, info);
```
默认客户字段说明

| key           | 是否必选   | 说明         |
| ------------- | ------ | ---------- |
| **sdk_token** | **必选** | **用户唯一标识** |
| cellphone     | 可选     | 用户手机号      |
| email         | 可选     | 邮箱账号       |
| description   | 可选     | 用户描述       |

#####3.2.1添加用户自定义字段 
用管理员账号登录后台，在[管理中心-用户字段]中添加自定义字段。
![udesk](http://7xr0de.com1.z0.glb.clouddn.com/custom.jpeg)
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
#####3.2.4初始化客户逻辑

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
**注意sdktoken** 是客户的唯一标识，用来识别身份，**sdk_token: 传入的字符请使用 字母 / 数字 等常见字符集**  。就如同身份证一样，不允许出现一个身份证号对应多个人，或者一个人有多个身份证号;**其次**如果给顾客设置了邮箱和手机号码，也要保证不同顾客对应的手机号和邮箱不一样，如出现相同的，则不会创建新顾客。  **完成了以上操作，接下来就可以使用UdeskSDK的其它功能了，祝你好运！**

#### 3.3启动人工客服对话界面

直接进入人工客服会话界面。系统根据客服分配规则安排客服接待；如果没有在线客服，则提示用户留言。

```
 UdeskSDKManager.getInstance().toLanuchChatAcitvity(this);
```

#### 3.4启动机器人对话界面

确保管理员后后【管理中心-即时通讯-IM机器人】开启机器人SDK IM渠道。可以设置是否允许转人员。使用此界面，则会根据后台配置显示机器人或人工客服对话界面

```
  UdeskSDKManager.getInstance().showRobotOrConversation(this);
```

#### 3.5启动帮助中心界面

Udek系统帮助中心后台可以创建帮助文档，客户通过帮助中心可查看相关文档。调用以下接口启动帮助中心界面

```
UdeskSDKManager.getInstance().toLanuchHelperAcitivty(this);
```



# 四、Udesk SDK API说明

#### 4.1更新客户信息

4.1.1更新系统默认客户字段，昵称、邮箱、电话、描述

```
Map<String, String> info = new HashMap<String, String>();
info.put(UdeskConst.UdeskUserInfo.NICK_NAME,"更新后的昵称");
//更新后的邮箱
info.put(UdeskConst.UdeskUserInfo.EMAIL,"0631@163.com");
//更新后的手机号
info.put(UdeskConst.UdeskUserInfo.CELLPHONE,"15651818750");
info.put(UdeskConst.UdeskUserInfo.DESCRIPTION,"更新后的描述信息")

//传入需要更新的Udesk系统默认字段
UdeskSDKManager.getInstance().setUpdateUserinfo(info);
注意更新邮箱或者手机号码，如果在后端有同样的手机号或邮箱，则会更新失败        
```

4.1.2更新自定义字段

文本型字段示例：

```
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

选择型字段示例

```
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

#### 4.2发送咨询对象

在客户与客服对话时，经常需要将如咨询商品或订单发送给客服以便客服查看。

咨询对象目前最多支持发送4个属性(detail,image,title,url)，如下以商品举例说明

```
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
```

#### 4.3指定分配客服或客服组

在创建客户之后，调用此接口可主动指定分配客服或客服组并自动打开人工客服界面。

指定客服或客服组接口一次只能用一个

```
String agentId = "4418";UdeskSDKManager.getInstance().lanuchChatByAgentId(this,agentId);

```

```
String groupId = "9831";UdeskSDKManager.getInstance().lanuchChatByGroupId(this,groupId);

```

**客服和客服组ID获取方式**

管理员在【管理中心-渠道管理-即时通讯-自定义链接-指定客服和客服组】中选择指定的客服组，这样在上方链接中就会加载指定客服组的id。如下图所示，客服组“客服部”的groupId是“9831”。![udesk](http://7xr0de.com1.z0.glb.clouddn.com/%E8%8E%B7%E5%8F%96%E5%AE%A2%E6%9C%8Did.jpg)



####4.4使用导航菜单选择客服组

管理员在【管理中心-渠道管理-即时通讯-自定义链接-使用导航菜单】中添加自定义菜单，引导客户选择客服组。调用此接口打开导航菜单选择客服组界面，选中客服组后进入自动进入对话界面。

```
  UdeskSDKManager.getInstance().showConversationByImGroup(this);
```

管理员在【管理中心-即时通讯-自定义链接-使用导航菜单】设置导航菜单
![udesk](http://7xr0de.com1.z0.glb.clouddn.com/%E6%8C%87%E5%BC%95%E5%AE%A2%E6%88%B7%E9%80%89%E6%8B%A9%E5%AE%A2%E6%9C%8D%E7%BB%84.png)

#### 4.5断开与Udesk服务器连接

  App运行时如果需要客服离线或不再接收客服消息，调此接口可以主动断开与Udesk服务器的的连接。

```
UdeskSDKManager.getInstance().disConnectXmpp();
```

#### 4.6获取未读消息

在退出对话界面后，没有断开与Udesk服务器的连接，注册获取未读消息事件方法，之后在该方法中可以收到未读消息。

注册方法"OnNewMsgNotice"

``` java
UdeskMessageManager.getInstance().event_OnNewMsgNotice.bind(this, "OnNewMsgNotice");
```

OnNewMsgNotice方法的实现

    public void OnNewMsgNotice(MsgNotice msgNotice) {
         if (msgNotice != null) {
             NotificationUtils.getInstance().notifyMsg(UdeskCaseActivity.this, msgNotice.getContent());
         }
    }

注意：1 消息上报的对象是：MsgNotice ；2 注册的方法和实现的方法  字符串必须保证一致分大小写；

3 实现的方法  必须public修饰。

#### 4.7获取未读消息数

sdk 3.2.0版本开始，可在退出对话界面后，没有断开与Udesk服务器的连接，可获得这个会话的未读消息数，打开对话界面后未读消息数会清空。

```
UdeskSDKManager.getInstance().getCurrentConnectUnReadMsgCount();
```

#### 4.8删除客户聊天数据

sdk初始化成功，创建客户后，调用此接口可删除当前客户的聊天记录信息

```
UdeskSDKManager.getInstance().deleteMsg();
```

#### 4.9控制台日志开关

如果开发中，想在控制台看当前客户与Udesk服务器连接（xmpp)的交互报文，调用如下接口可实现

```
//true 表示开启控制台日志  false表示关闭控制台日志
UdeskSDKManager.getInstance().isShowLog(true);
```

#### 4.10Android M 权限处理

Udesk SDK已经兼容Android M不需开发，兼容方法如下。

采用开源库rxpermissions 依赖如下库compile 'com.tbruyelle.rxpermissions:rxpermissions:0.7.0@aar'compile 'io.reactivex:rxjava:1.1.4'

选取rxpermissions 原因是github上赞和使用最多，使用方便简单。举例拍照的运行代码:

```

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

# 五、SDK内部封装API

#### 5.1支持的消息类型 

收发文本、图片、语音，接收富文本信息。在项目中定义的标识为：

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
####5.2 发送文本消息
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
####5.2 输入预知

通过以下方法将用户正在输入的内容，实时显示在客服对话窗口，每500毫秒发送一次消息。注掉以下实现方法可以取消输入预知功能。

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
####5.3 发送语音消息

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
####5.4 发送图片消息
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

####5.5支持满意度调查
5.5.1客服在pc端叉掉会话，sdk会受到一条满意度调查信息。

``` java
//满意度调查xmpp报文消息  survey='true'  标识调查
<message from='agent_13988_3055@im03.udesk.cn/3201253852559793907391870209273268523016675556031893299406' 
	to='customer_8698750_3055@im03.udesk.cn' type='chat' nick='许一' user_id='13988'>
		isreqsurvey xmlns='survey' survey='true'/>
</message>
```
5.5.2处理满意度选项

移动端会在见ChatActivityPresenter中处理收到满意度调查消息，获取满意度选项，弹出满意度调查框，可提交满意度选项
``` java
//收到满意度调查消息
public void onReqsurveyMsg(Boolean isSurvey) {

     if (isSurvey) {
         getIMSurveyOptions();
     }
}
```
5.5.3启动满意度调查

``` java
private void toLuanchSurveyActivity(SurveyOptionsModel surveyOptions) {
	Intent intent = new Intent();
	intent.setClass(UdeskChatActivity.this, SurvyDialogActivity.class);
	intent.putExtra(UdeskConst.SurvyDialogKey, surveyOptions);
	startActivityForResult(intent, SELECT_SURVY_OPTION_REQUEST_CODE);
}
```
5.5.4提交满意度调查

``` java
    Toast.makeText(UdeskChatActivity.this, "感谢您的评价！", Toast.LENGTH_SHORT).show();
    String optionId = data.getStringExtra(UdeskConst.SurvyOptionIDKey);
    mPresenter.putIMSurveyResult(optionId);
```

####5.6支持客服不在线留言
当前客服繁忙或者不在线，输入内容发送消息，弹出留言提示，如果客户点击则跳转到表单界面。
``` java
protected void goToForm() {
      Intent intent = new Intent(UdeskChatActivity.this,UdeskFormActivity.class);
      startActivity(intent);
      dismissFormWindow();
}
```

# 六、自定义UI

可以通过以下文件名称快速定位SDK资源，修改相应的资源可以实现UI自定义

 聊天界面UdeskChatActivity中的MessageAdatper，展示语音，文本，图片等消息。

```
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
ImgViewHolder    显示图片消息；
RichTextViewHolder 显示富文本消息；
CommodityViewHolder 显示广告商品信息；

RedirectViewHolder  显示转移提示语信息；

```

其它对应的UI

```
    UdeskHelperActivity  帮助中心界面；
    UdeskHelperArticleActivity   显示一篇文章的具体内容
    UdeskRobotActivity   机器人会话界面    
    SurvyDialogActivity  满意度对话框
    UdeskConfirmPopWindow 弹出离线表单的PopWindow 
    UdeskPopVoiceWindow  录音的PopWindow
    UdeskExpandableLayout 提示客服上下线的动画
    UdeskTitleBar 标题栏
```

# 七、集成UdeskSDK中使用的接口说明

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

#八、SDK 第三方库依赖

以下依赖文件已封装在SDK内，无需自行添加

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

#九、代码混淆

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
-keep class com.tbruyelle.rxpermissions.** {*;}
-keep class de.hdodenhof.circleimageview.** {*;}
```
