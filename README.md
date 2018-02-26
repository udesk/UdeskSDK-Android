# UdeskSDK Android 3.9.0+ 开发者文档

### 快速接入
注意导入包UdeskSDKUI后 在你apply plugin: 'com.android.application'的build.gradle文件里加入
 ``` java
repositories {
    flatDir {
        dirs project(':UdeskSDKUI').file('libs')
    }
}
 ```
 
------
   一.初始管理员后台创建应用是生成的对应app key 和 app id
   
   ``` java
      UdeskSDKManager.getInstance().initApiKey(context, "you domain","App Key","App Id");
      
      注意：域名不要带有http://部分，加入注册生成的域名是"http://udesksdk.udesk.cn/" ,只要传入"udesksdk.udesk.cn"
   ```
      
   二.设置客户的信息。
   
  ``` java   
      Map<String, String> info = new HashMap<String, String>();
      String sdkToken = "你们识别客户的唯一标识，和我们系统一一映射";
      info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, sdkToken);
      info.put(UdeskConst.UdeskUserInfo.NICK_NAME, "客户的姓名");
      UdeskSDKManager.getInstance().setUserInfo(context, sdkToken, info);
      注意sdktoken是客户的唯一标识，用来识别身份，sdk_token: 你们传入的字符请使用 只包含字母，数字的字符集。
      
  ```    
     
  三. 进入页面分配会话
  
``` java
    
      UdeskSDKManager.getInstance().entryChat(context);
	  注意：只有通过这个方法进入会话,管理员在后台配置的选项才会生效, 其它方式进入会话,配置不会生效。 
      
``` 

四. Proguard
  
``` java
//udesk
-keep class udesk.** {*;} 
-keep class cn.udesk.**{*; } 
//七牛
-keep class okhttp3.** {*;} 
-keep class okio.** {*;} 
-keep class com.qiniu.**{*;}
-keep class com.qiniu.**{public <init>();}
-ignorewarnings
//smack
-keep class org.jxmpp.** {*;} 
-keep class de.measite.** {*;} 
-keep class org.jivesoftware.** {*;} 
-keep class org.xmlpull.** {*;} 
-dontwarn org.xbill.**
-keep class org.xbill.** {*;} 

//eventbus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
 
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

//freso
-keep class com.facebook.** {*; }  
-keep class com.facebook.imagepipeline.** {*; } 
-keep class com.facebook.animated.gif.** {*; }  
-keep class com.facebook.drawee.** {*; }  
-keep class com.facebook.drawee.backends.pipeline.** {*; }  
-keep class com.facebook.imagepipeline.** {*; }  
-keep class bolts.** {*; }  
-keep class me.relex.photodraweeview.** {*; }  

-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}
# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-dontwarn okhttp3.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**
-dontwarn com.facebook.infer.**


 //bugly
-keep class com.tencent.bugly.** {*; } 

 //agora
-keep class io.agora.**{*;}

```
  五：  如果需要设置咨询对象，参照如下设置：
  ``` java
      UdeskCommodityItem item = new UdeskCommodityItem();
        item.setTitle("木林森男鞋新款2016夏季透气网鞋男士休闲鞋网面韩版懒人蹬潮鞋子");// 商品主标题
        item.setSubTitle("¥ 99.00");//商品副标题
        item.setThumbHttpUrl("https://img.alicdn.com/imgextra/i1/1728293990/TB2ngm0qFXXXXcOXXXXXXXXXXXX_!!1728293990.jpg_430x430q90.jpg");// 左侧图片
        item.setCommodityUrl("https://detail.tmall.com/item.htm?spm=a1z10.3746-b.w4946-14396547293.1.4PUcgZ&id=529634221064&sku_properties=-1:-1");// 商品网络链接
        UdeskSDKManager.getInstance().setCommodity(item);
       在进入会话界面前调用 。
  ```
  
  六： 如果需要设置客户头像，参照如下：
  ``` java
     UdeskSDKManager.getInstance().setCustomerUrl(url);
  ```
  
  更多功参考demo。
  
 
------


### SDK中较常见配置项说明
------

 一. IM中聊天功能选项的使用的配置开关说明:
 
     配置开关在UdeskConfig类中：
	 
    //配置 是否使用推送服务  true 表示使用  false表示不使用
    public static boolean isUserSDkPush = true;

    //配置放弃排队的策略
    public static String UdeskQuenuMode = UdeskQuenuFlag.Mark;

    //配置开启留言时的    留言表单留言提示语
    public static String UdeskLeavingMsg = "";

    //配置是否把domain 和 appid 和 appkey 和 sdktoken 存在sharePrefence中， true保存，false 不存
    public static boolean isUseShare = true;

    //是否使用录音功能  true表示使用 false表示不使用
    public static boolean isUseVoice = true;

    //是否使用发送图片的功能  true表示使用 false表示不使用
    public static boolean isUsephoto = true;

    //是否使用拍照的功能  true表示使用 false表示不使用
    public static boolean isUsecamera = true;

    //是否使用上传MP4视频文件功能  true表示使用 false表示不使用
    public static boolean isUsefile = true;

    //是否使用发送位置功能  true表示使用 false表示不使用
    public static boolean isUseMap = false;

    //配置接入使用的地图类型
    public static String useMapType = UdeskMapType.Other;
	
	//在没有请求到管理员在后端对sdk使用配置下，在默认的情况下，是否需要表单留言，true需要， false 不需要
    public static boolean isUserForm = false;
	 
 二. IM中聊天功能 如果场景需要进入会话界面,先配置一条消息发送给客服可以如下:
      每次进入会话界面前，调用如下方法传值：
      UdeskSDKManager.getInstance().setFirstMessage(String message);
	
 三. IM中聊天功能 集成发送地理位置的信息说明:
     
	 android 接入的第三方选择性比较多等原因，没有直接在SDK中内嵌地图SDK，由客户根据实际需要选择是否集成发送地理位置信息。
	 提供集成地图的demo例子有：百度地图（见baidumapdemo 这个module），高德地图（gaodemapdemo）腾讯地图（tenxunmapdemo）
	 
	 集成发送地理位置信息步骤：
	 1.初始配置 
	 UdeskConfig.isUseMap = true;
	 百度地图设置
     UdeskConfig.useMapType = UdeskConfig.UdeskMapType.BaiDu; 
	 高德地图设置
	 UdeskConfig.useMapType = UdeskConfig.UdeskMapType.GaoDe;
	 腾讯地图设置
	 UdeskConfig.useMapType = UdeskConfig.UdeskMapType.Tencent;
	 其它地图设置
	 UdeskConfig.useMapType = UdeskConfig.UdeskMapType.Other;
	 
	 2.设置地理位置点击事件的回调：
	 UdeskSDKManager.getInstance().setLocationMessageClickCallBack(new UdeskSDKManager.ILocationMessageClickCallBack() {
                        @Override
                        public void luanchMap(Context context, double latitude, double longitude, String selctLoactionValue) {
                        
                        }
                    });
	  说明：回调返回发送地理位置信息的经纬度和选择的位置信息， 由客户根据需要实现对应的跳转界面。
	  
	  3.需要设置选择地图位置的activity，供UdeskSDK，点击发送地理位置时调用
	  举例是：LocationActivity，以下用LocationActivity进行说明:
	  UdeskSDKManager.getInstance().setCls(LocationActivity.class);
	  
	  说明：UdeskChatActivity是通过startActivityForResult方式进入LocationActivity,在之后选择相应信息回传到UdeskChatActivity，是通过Intent方式。
	        对intent.putExtra中的name做了约定，的遵守才能有效显示地理位置信息。
			
		具体约定：	
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
	   
	   具体的例子如deom提供的代码片段
	      mMap.getScreenShot(new TencentMap.OnScreenShotListener() {
                    @Override
                    public void onMapScreenShot(Bitmap bitmap) {
                        saveBitmap(bitmap);
                        Intent intent = new Intent();
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Position, mPoiItem.title);
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Latitude, (double) mPoiItem.location.lat);
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Longitude, (double) mPoiItem.location.lng);
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.BitmapDIR, bitmapdir);
                        setResult(RESULT_OK, intent);
                        LocationActivity.this.finish();
                    }
                });

四. IM 使用视频功能:
    注意：需要使用视频功能，一定需要使用UdeskSDKUI IM中功能 ,但可以单独使用UdeskSDKUI功能  
	在你的app中 在依赖udeskvideo 模块。
    
------

### 常见问题
------
``` java
   1. 指定客服组或者客服分配出现与指定客服组客服不一致的情况？
   
     先要确认客服没有关闭会话。
     我们产品逻辑： 假设客户A   选了客服组B下的客服B1，进行会话。  之后客户A退出会话界面，进入另外界面，之后通过客服组C下的客服C 1分配会话：  这时      后台会判断，如果和B1会话还存在，则会直接分配给B1，而不会分配給客服C 1。  只有B1会话关闭了，才会分配給客服C1。 
     
   2.出现在不同客户分配的会话在一个会话中?
   
      出现这种情况，是客服传的sdktoken值一样。 sdktoken像身份证一样，是用户唯一的标识。让客户检查接入是传入的sdktoken值。
      如果设置了email 或者 cellphone  出现相同也会在一个客服的会话里。
   
   3.某个手机打不开机器人页面？
   
     这个问题的可能情况之一： 手机时间设置和当前时间不一致造成的。时间误差超过一小时，必然会出现链接不上机器人界面。
     
   4.集成sdk后出现找不到类的错误？
   
     检查是否加分包策略：
      由于Android的Gradle插件在Android Build Tool 21.1开始支持使用multidex，所以我们需要使用Android Build Tools 21.1及以上版本，修改app目录下       的build.gradle文件，有两点需要修改。
     （1）在defaultConfig中添加multiDexEnabled true这个配置项。 
     （2）在dependencies中添加multidex的依赖： compile ‘com.android.support:multidex:1.0.0’
     （3）继承Application，然后重写attachBaseContext方法，并在AndroidManifest.xml的application标签中进行注册。
    
          @Override
   	 protected void attachBaseContext(Context base) {
     	     super.attachBaseContext(base);
      	     MultiDex.install(this);
	  }
     
    5.h5接入参考例子
    https://github.com/udesk/udesk_android_sdk_h5
   
   有问题直接加QQ：1979305929
``` 
------
## 一、SDK工作流程图
Udesk-SDK的工作流程如下图所示。

![alt text](indeximg/android-new-liuchen.png)
## 二、下载和集成SDK

#### 2.1下载Udesk SDK

#### 2.2集成到AndroidStudio

2.2.1解压后文件介绍

| SDK 中的文件          | 说明                                       |
| ----------------- | ---------------------------------------- |
| udeskNewDemo      | UdeskSDKUI module使用的例子              |
| gaodemapdemo      | gaodemapdemo 使用高德地图的例子          | 
| tenxunmapdemo     | tenxunmapdemo 使用腾讯地图的例子         |
| baidumapdemo      | baidumapdemo 使用百度地图的例子          |
| UdeskSDKUI        | 核心IM功能                               |
| udeskvideo        | 视频功能的模块                           |

2.2.2导入集成

你所要做的是把UdeskSDKUI做为独立的module import, 并在你APP build.gradle文件中加入：

``` java
dependencies {
    compile project(':UdeskSDKUI')
}
```
### 注意[eclipse] [1]目录地址如下：
 https://github.com/udesk/udesk_sdk_android_eclipse      

 webview方式接入的参考demo目录地址：
 https://github.com/udesk/udesk_android_sdk_h5
## 三、快速集成SDK

### 3.1初始化

获取appid 和 密钥的方式，见如下图：

![udesk](http://7xr0de.com1.z0.glb.clouddn.com/initUdesk.png)
使用公司域名和密钥 和 appid 初始化SDK

``` java
UdeskSDKManager.getInstance().initApiKey(context, "You domain","You key","You appid") 

注意：域名不要带有http://部分，加入注册生成的域名是"http://udesksdk.udesk.cn/" ,只要传入"udesksdk.udesk.cn"
```

### 3.2初始化客户信息

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

##### 3.2.1添加用户自定义字段 
用管理员账号登录后台，在[管理中心-用户字段]中添加自定义字段。
![udesk](http://7xr0de.com1.z0.glb.clouddn.com/custom.jpeg)
#####3.2.2获取自定义字段信息
``` java
UdeskHttpFacade.getInstance().getUserFields(UDESK_DOMAIN, "you App key", "you App Id",new UdeskCallBack(){

	@Override
	public void onSuccess(String message) {
		
	}

	@Override
	public void onFail(String message) {

	}
});
```
##### 3.2.3给自定义字段赋值
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
##### 3.2.4初始化客户逻辑

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
**注意sdktoken** 是客户的唯一标识，用来识别身份，**sdk_token: 传入的字符请使用 字母 / 数字 字符集**  。就如同身份证一样，不允许出现一个身份证号对应多个人，或者一个人有多个身份证号;**其次**如果给顾客设置了邮箱和手机号码，也要保证不同顾客对应的手机号和邮箱不一样，如出现相同的，则不会创建新顾客。  **完成了以上操作，接下来就可以使用UdeskSDK的其它功能了，祝你好运！**

#### 3.3启动对话界面

逻辑：
   1 上次会话存在，直接进入会话；上次会话不存在，判断是否开启机器人，开启则进入机器人界面。没开启，则判断是否启动导航页，启动则通过导航页进入会话，没开启，则直接进入会话。
   2  机器人中 转人工，也会判断是否开启导航页，没开启，则判断是否启动导航页，启动则通过导航页进入会话，没开启，则直接进入会话。
   
```java
 UdeskSDKManager.getInstance().entryChat(this);
```


#### 3.4启动帮助中心界面

Udek系统帮助中心后台可以创建帮助文档，客户通过帮助中心可查看相关文档。调用以下接口启动帮助中心界面

```java
UdeskSDKManager.getInstance().toLanuchHelperAcitivty(this);
```

# 四、Udesk SDK API说明

#### 4.1更新客户信息

4.1.1更新系统默认客户字段，昵称、邮箱、电话、描述

```java
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

```java
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

```java
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

```java
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

```java
String agentId = "5236";
UdeskSDKManager.getInstance().lanuchChatByAgentId(this,agentId);

```

```java
String groupId = "14005";
UdeskSDKManager.getInstance().lanuchChatByGroupId(this,groupId);

```

**客服和客服组ID获取方式**

管理员在【管理中心-即时通讯-基本信息-专用链接】中选择指定的客服组或客服，可看到客服ID和客服组ID。
#### 4.4 支持设置客户的头像显示
在UdeskSDKManager中设置客户头像的url
``` java
UdeskSDKManager.getInstance().setCustomerUrl(url)

```
#### 4.5 配置开启留言时的留言表单的留言提示语
在UdeskSDKManager中设置
``` java
UdeskSDKManager.getInstance().setLeavingMsg(msg)

```
#### 4.6获取未读消息

在退出对话界面后，没有断开与Udesk服务器的连接，注册获取未读消息事件方法，之后在该方法中可以收到未读消息。

注册方法"OnNewMsgNotice"

``` java
UdeskMessageManager.getInstance().event_OnNewMsgNotice.bind(this, "OnNewMsgNotice");
```

OnNewMsgNotice方法的实现
``` java
    public void OnNewMsgNotice(MsgNotice msgNotice) {
         if (msgNotice != null) {
             NotificationUtils.getInstance().notifyMsg(UdeskCaseActivity.this, msgNotice.getContent());
         }
    }
```
注意：1 消息上报的对象是：MsgNotice ；2 注册的方法和实现的方法  字符串必须保证一致分大小写；

3 实现的方法  必须public修饰。

#### 4.7获取未读消息数

sdk 3.2.0版本开始，可在退出对话界面后，没有断开与Udesk服务器的连接，可获得这个会话的未读消息数，打开对话界面后未读消息数会清空。

```java
UdeskSDKManager.getInstance().getCurrentConnectUnReadMsgCount();
```

#### 4.8删除客户聊天数据

sdk初始化成功，创建客户后，调用此接口可删除当前客户的聊天记录信息

```java
UdeskSDKManager.getInstance().deleteMsg();
```

#### 4.9 设置留言界面的回调接口

不想使用udesk系统的留言模块，可以设置该接口，在接口回调的方法中处理你们的逻辑
```java
UdeskSDKManager.getInstance().setFormCallBak(formCallBak);

 public void setFormCallBak(IUdeskFormCallBak formCallBak) {
        this.formCallBak = formCallBak;
    }
```

#### 4.10 设置文本中的链接地址的点击事件的拦截回调
对于会话中的链接地址想自由处理，设置该接口
```java
UdeskSDKManager.getInstance().setTxtMessageClick(txtMessageClick);

 public void setTxtMessageClick(ITxtMessageWebonCliclk txtMessageClick) {
        this.txtMessageClick = txtMessageClick;
    }
```


#### 4.11 设置结构化消息type为sdk_callback的消息回调
设置结构化消息type为sdk_callback的消息回调接口
```java
  UdeskSDKManager.getInstance().setStructMessageCallBack(structMessageCallBack);

   /**
     * 设置结构化消息的回调接口
     *
     * @param structMessageCallBack
     */
    public void setStructMessageCallBack(IUdeskStructMessageCallBack structMessageCallBack) {
        this.structMessageCallBack = structMessageCallBack;
    }
    
```

#### 4.12控制台日志开关

如果开发中，想在控制台看当前客户与Udesk服务器连接（xmpp)的交互报文，调用如下接口可实现

```java
//true 表示开启控制台日志  false表示关闭控制台日志
UdeskSDKManager.getInstance().isShowLog(true);
```

#### 4.13 断开与Udesk服务器连接

  App运行时如果需要客服离线或不再接收客服消息，调此接口可以主动断开与Udesk服务器的的连接。

```java
UdeskSDKManager.getInstance().disConnectXmpp();
```
#### 4.14设置退出排队的模式

 quitmode: mark (默认,标记放弃)/ cannel_mark(取消标记) / force_quit(强制立即放弃)
```java
UdeskSDKManager.getInstance().setQuitQuenuMode(quitmode);
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
#### 5.2 发送文本消息
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
#### 5.3 输入预知

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
#### 5.4 发送语音消息

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
#### 5.5 发送图片消息
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

#### 5.6支持满意度调查
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

#### 5.7支持客服不在线留言
当前客服繁忙或者不在线，输入内容发送消息，弹出留言提示，如果客户点击则跳转到表单界面。
``` java
protected void goToForm() {
      Intent intent = new Intent(UdeskChatActivity.this,UdeskFormActivity.class);
      startActivity(intent);
      dismissFormWindow();
}
```

# 六、自定义UI和配置设置

UdeskConfig这个类提供了一些颜色的资源的配置
```java
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

    //配置 是否使用推送服务  true 表示使用  false表示不使用
    public  static  boolean isUserSDkPush = true;

    //配置放弃排队的策略
    public  static  String  UdeskQuenuMode = UdeskQuenuFlag.Mark;

    //配置开启留言时的    留言表单留言提示语
    public  static  String  UdeskLeavingMsg = "";

    //配置是否把domain 和 appid 和 appkey 和 sdktoken 存在sharePrefence中， ftrue保存，false 不存
    public  static  boolean  isUseShare = true;

    //是否使用录音功能  true表示使用 false表示不使用
    public  static  boolean  isUseVoice = true;
    
     //在没有请求到管理员在后端对sdk使用配置下，在默认的情况下，是否需要表单留言，true需要， false 不需要
     public  static  boolean  isUserForm= true;
```
参照udeskNewDemo 提供的例子进行配置
```java

 private void UIStyle1(){
        UdeskConfig.udeskTitlebarBgResId = R.color.udesk_titlebar_bg1;
        UdeskConfig.udeskTitlebarTextLeftRightResId = R.color.udesk_color_navi_text1;
        UdeskConfig.udeskIMRightTextColorResId = R.color.udesk_color_im_text_right1;
        UdeskConfig.udeskIMLeftTextColorResId = R.color.udesk_color_im_text_left1;
        UdeskConfig.udeskIMAgentNickNameColorResId = R.color.udesk_color_im_left_nickname1;
        UdeskConfig.udeskIMTimeTextColorResId = R.color.udesk_color_im_time_text1;
        UdeskConfig.udeskIMTipTextColorResId = R.color.udesk_color_im_tip_text1;
        UdeskConfig.udeskbackArrowIconResId = R.drawable.udesk_titlebar_back;
        UdeskConfig.udeskCommityBgResId = R.color.udesk_color_im_commondity_bg1;
        UdeskConfig.udeskCommityTitleColorResId = R.color.udesk_color_im_commondity_title1;
        UdeskConfig.udeskCommitysubtitleColorResId = R.color.udesk_color_im_commondity_subtitle1;
        UdeskConfig.udeskCommityLinkColorResId = R.color.udesk_color_im_commondity_title1;
    }
    
```

可以通过以下文件名称快速定位SDK资源，修改相应的资源可以实现UI自定义

 聊天界面UdeskChatActivity中的MessageAdatper，展示语音，文本，图片等消息。

```java
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

```java
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
    "/udesk_im/sdk/v3/im/sdk_push.json"; /控制推送状态开关的接口
		
```

# 八、离线消息推送
当前仅支持一种推送方案，即Udesk务端发送消息至开发者的服务端，开发者再推送消息到 App。
#### 8.1 设置接收推送的服务器地址
        推送消息将会发送至开发者的服务器。
	
	设置服务器地址，请使用Udesk管理员帐号登录 Udesk，在 设置 -> 移动SDK 中设置。
![udesk](http://7xr0de.com1.z0.glb.clouddn.com/5D761252-3D9D-467C-93C9-8189D0B22424.png)	
	
#### 8.2 使用Udesk 推送功能的配置
``` java
   //配置 是否使用推送服务  true 表示使用  false表示不使用
    public  static  boolean isUserSDkPush = false;

``` 
	
#### 8.3 设置用户的设备唯一标识
``` java
    UdeskSDKManager.getInstance().setRegisterId（context,"xxxxregisterId"）
     //保存注册推送的的设备ID
    public void setRegisterId(Context context, String registerId) {
        UdeskConfig.registerId = registerId;
        PreferenceHelper.write(context, UdeskConst.SharePreParams.RegisterIdName,
                UdeskConst.SharePreParams.Udesk_Push_RegisterId, registerId);
    }
		
``` 
   关闭和开启Udesk推送服务，Udesk推送给开发者服务端的消息数据格式中，会有 device_token 的字段。
   
#### 8.4	关闭开启Udek推送服务
``` java
  /**
     * @param domain    公司注册生成的域名
     * @param key        创建app时，生成的app key
     * @param sdkToken   用户唯一标识
     * @param status         sdk推送状态 ["on" | "off"]  on表示开启Udesk推送服务， off表示关闭udesk推送服务
     * @param registrationID 注册推送设备的ID
     * @param appid  创建app时，生成的app id 
     */

    public void setSdkPushStatus(String domain, String key, String sdkToken, String status, String registrationID, String appid)
		
```

#### 8.5 Udek推送给开发者服务端的接口说明
**基本要求**

- 推送接口只支持 http，不支持 https
- 请求已 POST 方法发送
- 请求 Body 数据为 JSON 格式，见示例
- 请求时使用的 content-type 为 application/x-www-form-urlencoded



**参数**

当有消息或事件发生时，将会向推送接口传送以下数据

| 参数名          | 类型       | 说明                                       |
| ------------ | -------- | ---------------------------------------- |
| message_id   | string   | 消息id                                     |
| platform     | string   | 平台，'ios' 或 'android'                     |
| device_token | string   | 设备标识                                     |
| app_id       | string   | SDK app id                               |
| content      | string   | 消息内容，仅 type 为 'message' 时有效              |
| sent_at      | datetime | 消息推送时间，格式 iso8601                        |
| from_id      | integer  | 发送者id(客服)                                |
| from_name    | string   | 发送者名称                                    |
| to_id        | integer  | 接收者id(客户)                                |
| to_token     | string   | 接收者 sdk_token(唯一标识)                      |
| type         | string   | 消息类型，'event' 为事件，'message'为消息            |
| event        | string   | 事件类型，'redirect' 客服转接，'close'对话关闭，'survey'发送满意度调查 |



**参数示例**

```json
{
    "message_id": "di121jdlasf82jfdasfklj39dfda",
    "platform": "ios",
    "device_token": "4312kjklfds2",
    "app_id": "dafjidalledaf",
    "content": "Hello world!",
    "sent_at": "2016-11-21T10:40:38+08:00",
    "from_id": 231,
    "from_name": "Tom",
    "to_id": 12,
    "to_token": "dae121dccepm1",
    "type": "message",
  	"event": "close"
}
```

