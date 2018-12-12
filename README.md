# UdeskSDK Android 4.1.0+ 开发者文档

# 特别提醒
```
   如果你的项目打包指定了so库的加载配置了，
     ndk{
            abiFilters "armeabi"
     }
     
    你需要特别注意fresco版本号 在fresco 1.9.0 以后的的 Application.mk APP_ABI := armeabi-v7a armeabi arm64-v8a x86 x86_64,
    不在包含armeabi 
    
    在UdeskSDKUI下的build.gradle文件下  修改dependencies fresco版本号 不能根据androidstudio提示更新到最新
    
```

### customer_token， sdk_token 仅支持字母、数字及下划线,禁用特殊字符

### 特别注意  注意 注意
    创建用户的基本信息(setDefualtUserInfo(Map<String, String> defualtUserInfo)) 
	和 更新用户的基本信息不能共用(setUpdateDefualtUserInfo(Map<String, String> updateDefualtUserInfo))
	
	不能共用同一个Map，  创建用户信息必须设置sdktoken，二更新用户信息不能设置sdktoken。并且更新用户信息如果设置了电话和邮箱，udesk系统存在，
	会导致更新失败， 建议自定义字段来采集用户的邮箱和电话信息。
	
	demo中的 NotificationUtils，状态通知栏没有做 Android O通知栏适配， 你在实现targetSdkVersion 大于26时， 必须做对应的通知栏适配，
	不可以用NotificationUtils  处理方式
	

### 4.1.4 修复内容 
    1.排队场景 发送图片 客服显示问题修复
	2.排队场景， 拉对话关闭对话场景下 无法发送语音和表情失败的修复
	3.开发者选项 打开保护活动 在使用流量情况下不能发送文件修复
	4 客服忙碌 排队，客服在切换离线，客户这边显示离线后， 客服在上线，拉入排队会话  客服发消息 客户收到 无法发送消息修复
	5 空指针隐患的保护
	6 其它优化

### 4.1.1 修复内容
1. 修复sdk排队中点击留言还在排队中；
2. 修复排队发送文本消息后，更多得按钮隐藏了；
3. 修复发送商品消息在客服端没显示；
4. 优化消息的发送到达；（离开会话界面，有未收到回执的消息，会放入单例中发送）


### 4.1.0 更新的内容
1. 支持排队时发送消息
2. 替换表情
3. 支持机器人  管理员配置名称
4. 支持自定义渠道 
5. 支持设置 全局客户唯一性customer_token
6. 支持离线消息显示实际发送时间
7. 修改录音文件格式wav，提高客服语音转文字的准确率
8. 优化无消息对话过滤消息保存

[下载地址](https://github.com/udesk/UdeskSDK-Android/tree/master)

## 快速接入
 
  ### 1.初始管理员后台创建应用是生成的对应app key 和 app id
   
   ``` java
      UdeskSDKManager.getInstance().initApiKey(context, "you domain","App Key","App Id");
      
      注意：域名不要带有http://部分，加入注册生成的域名是"http://udesksdk.udesk.cn/" ,只要传入"udesksdk.udesk.cn"
   ```
      
### 2.设置UdeskConfig配置信息。
   
   **说明：配置的功能根据你们实际的需要进行选择，都有默认行为。 最基本设置用户的基本信息 setDefualtUserInfo**
  ``` java 
	 
	  默认系统字段是Udesk已定义好的字段，开发者可以直接传入这些用户信息，供客服查看。
      String sdktoken = “用户唯一的标识”; 
      Map<String, String> info = new HashMap<String, String>();
      **//sdktoken 必填**
      info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, sdktoken);
      //以下信息是可选
      info.put(UdeskConst.UdeskUserInfo.NICK_NAME,"昵称");
      info.put(UdeskConst.UdeskUserInfo.EMAIL,"0631@163.com");
      info.put(UdeskConst.UdeskUserInfo.CELLPHONE,"15651818750");
      info.put(UdeskConst.UdeskUserInfo.DESCRIPTION,"描述信息")
   
      只设置用户基本信息的配置
      UdeskConfig.Builder builder = new UdeskConfig.Builder();
	  builder.setDefualtUserInfo(info)
	  UdeskSDKManager.getInstance().entryChat(getApplicationContext(), builder.build(), sdkToken);

 ```
 
**默认客户字段说明**

| key           | 是否必选   | 说明         |
| ------------- | ------ | ---------- |
| **sdk_token** | **必选** | **用户唯一标识** |
| cellphone     | 可选     | 用户手机号      |
| email         | 可选     | 邮箱账号       |
| description   | 可选     | 用户描述       |
  
  
  
  
**UdeskConfig内部类Builder的说明**


| 属性           | 设置方法   | 功能说明         |
| ------------- | ------ | ---------- |
| udeskTitlebarBgResId        | setUdeskTitlebarBgResId       | 标题栏TitleBar的背景色,通过颜色设置   |
| udeskTitlebarTextLeftRightResId        | setUdeskTitlebarTextLeftRightResId                         | 标题栏TitleBar，左右两侧文字的颜色                               |
| udeskIMLeftTextColorResId              | setUdeskIMLeftTextColorResId                               | IM界面，左侧文字的字体颜色                                       |
| udeskIMRightTextColorResId             | setUdeskIMRightTextColorResId                              | IM界面，右侧文字的字体颜色                                       |
| udeskIMAgentNickNameColorResId         | setUdeskIMAgentNickNameColorResId                          | IM界面，左侧客服昵称文字的字体颜色                               |
| udeskIMTimeTextColorResId              | setUdeskIMTimeTextColorResId                               | IM界面，时间文字的字体颜色                                       |
| udeskIMTipTextColorResId               | setUdeskIMTipTextColorResId                                | IM界面，提示语文字的字体颜色，比如客服转移                       |
| udeskbackArrowIconResId                | setUdeskbackArrowIconResId                                 | 返回箭头图标资源id                                               |
| udeskCommityBgResId                    | setUdeskCommityBgResId                                     | 咨询商品item的背景颜色                                           |
| udeskCommityTitleColorResId            | setUdeskCommityTitleColorResId                             | 商品介绍Title的字样颜色                                          |
| udeskCommitysubtitleColorResId         | setUdeskCommitysubtitleColorResId                          | 商品咨询页面中，商品介绍子Title的字样颜色                        |
| udeskCommityLinkColorResId             | setUdeskCommityLinkColorResId                              | 商品咨询页面中，发送链接的字样颜色                               |
| isUserSDkPush                          | setUserSDkPush                                             | 配置 是否使用推送服务  true 表示使用  false表示不使用            |
| UdeskQuenuMode                         | setUdeskQuenuMode                                          | 配置放弃排队的策略                                               |
| isUseVoice                             | setUseVoice                                                | 是否使用录音功能  true表示使用 false表示不使用                   | 
| isUsephoto                             | setUsephoto                                                | 是否使用发送图片的功能  true表示使用 false表示不使用             | 
| isUsecamera                            | setUsecamera                                               | 是否使用拍照的功能  true表示使用 false表示不使用                 |      
| isUsefile                              | setUsefile                                                 | 是否使用上传文件功能  true表示使用 false表示不使用               |  
| isUsefile                              | setUseMap                                                  | 是否使用发送位置功能  true表示使用 false表示不使用               |  
| isUseEmotion                           | setUseEmotion                                              | 是否使用表情 true表示使用 false表示不使用                        |  
| isUseMore                              | setUseMore                                                 | 否使用展示出更多功能选项 true表示使用 false表示不使用            |  
| isUseSmallVideo                        | setUseSmallVideo                                           | 设置是否需要小视频的功能 rue表示使用 false表示不使用             |  
| ScaleMax                               | setScaleImg                                                | 上传图片是否使用原图 还是缩率图                                  |  
| isScaleImg                             | setScaleMax                                                | 设置宽高最大值，如果超出则压缩，否则不压缩                       |  
| Orientation                            | setOrientation                                             | 设置默认屏幕显示习惯                                             |  
| isUserForm                             | setUserForm                                                | 本地配置是否需要表单留言，true需要， false 不需要                |  
| defualtUserInfo                        | setDefualtUserInfo                                         | 创建用户的基本信息                                               |  
| definedUserTextField                   | setDefinedUserTextField                                    | 创建自定义的文本信息                                             |
| definedUserRoplist                     | setDefinedUserRoplist                                      | 创建自定义的列表信息                                             |    
| updateDefualtUserInfo                  | setUpdateDefualtUserInfo                                   | 用户需要更新的基本信息                                           |  
| updatedefinedUserTextField             | setUpdatedefinedUserTextField                              | 用户需要更新自定义字段文本信息                                   |  
| updatedefinedUserRoplist               | setUpdatedefinedUserRoplist                                | 用户需要更新自定义列表字段信息                                   |  
| firstMessage                           | setFirstMessage                                            | 设置带入一条消息  会话分配就发送给客服                           |  
| robot_modelKey                         | setRobot_modelKey                                          |  udesk 机器人配置欢迎语 对应的Id值                               |  
| concatRobotUrlWithCustomerInfo         | setConcatRobotUrlWithCustomerInfo                          |  用于机器人页面收集客户信息                                      |  
| customerUrl                            | setCustomerUrl                                             |  设置客户的头像地址                                              |    
| commodity                              | setCommodity                                               |  配置发送商品链接的mode                                          |  
| txtMessageClick                        | setTxtMessageClick                                         | 文本消息中的链接消息的点击事件的拦截回调。 包含表情的不会拦截回调 |  
| formCallBack                           | setFormCallBack                                            | 离线留言表单的回调接口 ，回调使用自己的处理流程                   |  
| structMessageCallBack                  | setStructMessageCallBack                                   | 设置结构化消息的点击事件回调接口                                  |  
| extreFunctions                         | setExtreFunctions                                          | 设置额外的功能按钮                                               |  
| functionItemClickCallBack              | setExtreFunctions                                          | 点击事件回调 直接发送文本,图片,视频,文件,地理位置,商品信息        |  
| isUseNavigationRootView                | setNavigations                                             | 设置是否使用导航UI true表示使用 false表示不使用                   |  
| navigationModes                        | setNavigations                                             | 约定传递的自定义按钮集合                                          |  
| navigationItemClickCallBack            | setNavigations                                             | 支持客户在导航处添加自定义按钮的点击回调事件                      | 
| isUseNavigationSurvy                   | setUseNavigationSurvy                                      | 设置是否使用导航UI中的满意度评价UI rue表示使用 false表示不使用    | 
| useMapType                             | setUseMapSetting                                           | 设置使用那种地图                                                 | 
| locationMessageClickCallBack           | setUseMapSetting                                           | 点击地理位置信息的回调接口                                       | 
| cls                                    | setUseMapSetting                                           | 传入打开地图消息显示的详请activity                               | 
| groupId                                | setGroupId                                                 | 设置的指定组，每次进入都必须重新指定                             | 
| isOnlyByGroupId                        | setGroupId                                                 | 是否仅仅指定组进入                                               | 
| agentId                                | setAgentId                                                 | 设置指订客服id，每次进入都必须重新指定                           | 
| isOnlyByAgentId                        | setAgentId                                                 | 是否仅仅指定客服进入                                              | 
| isOnlyUseRobot                         | setOnlyUseRobot                                            | 设置是否只使用机器人 不用其它功能                                 | 
| mProduct                                | setProduct                                                   | 设置商品消息         
| channel                                | setChannel                                                   | SDK支持自定义渠道（只支持字符数字，不支持特殊支持）        


**一个完整的参考例子**
   ``` java 

   private UdeskConfig.Builder makeBuilder() {
        UdeskConfig.Builder builder = new UdeskConfig.Builder();
        builder.setUdeskTitlebarBgResId(R.color.udesk_titlebar_bg1) //设置标题栏TitleBar的背景色
                .setUdeskTitlebarTextLeftRightResId(R.color.udesk_color_navi_text1) //设置标题栏TitleBar，左右两侧文字的颜色
                .setUdeskIMLeftTextColorResId(R.color.udesk_color_im_text_left1) //设置IM界面，左侧文字的字体颜色
                .setUdeskIMRightTextColorResId(R.color.udesk_color_im_text_right1) // 设置IM界面，右侧文字的字体颜色
                .setUdeskIMAgentNickNameColorResId(R.color.udesk_color_im_left_nickname1) //设置IM界面，左侧客服昵称文字的字体颜色
                .setUdeskIMTimeTextColorResId(R.color.udesk_color_im_time_text1) // 设置IM界面，时间文字的字体颜色
                .setUdeskIMTipTextColorResId(R.color.udesk_color_im_tip_text1) //设置IM界面，提示语文字的字体颜色，比如客服转移
                .setUdeskbackArrowIconResId(R.drawable.udesk_titlebar_back) // 设置返回箭头图标资源id
                .setUdeskCommityBgResId(R.color.udesk_color_im_commondity_bg1) //咨询商品item的背景颜色
                .setUdeskCommityTitleColorResId(R.color.udesk_color_im_commondity_title1) // 商品介绍Title的字样颜色
                .setUdeskCommitysubtitleColorResId(R.color.udesk_color_im_commondity_subtitle1)// 商品咨询页面中，商品介绍子Title的字样颜色
                .setUdeskCommityLinkColorResId(R.color.udesk_color_im_commondity_link1) //商品咨询页面中，发送链接的字样颜色
                .setUserSDkPush(set_sdkpush.isChecked()) // 配置 是否使用推送服务  true 表示使用  false表示不使用
                .setOnlyUseRobot(set_use_onlyrobot.isChecked())//配置是否只使用机器人功能 只使用机器人功能,只使用机器人功能;  其它功能不使用。
                .setUdeskQuenuMode(force_quit.isChecked() ? UdeskConfig.UdeskQuenuFlag.FORCE_QUIT : UdeskConfig.UdeskQuenuFlag.Mark)  //  配置放弃排队的策略
                .setUseVoice(set_usevoice.isChecked()) // 是否使用录音功能  true表示使用 false表示不使用
                .setUsephoto(set_usephoto.isChecked()) //是否使用发送图片的功能  true表示使用 false表示不使用
                .setUsecamera(set_usecamera.isChecked()) //是否使用拍照的功能  true表示使用 false表示不使用
                .setUsefile(set_usefile.isChecked()) //是否使用上传文件功能  true表示使用 false表示不使用
                .setUseMap(set_usemap.isChecked()) //是否使用发送位置功能  true表示使用 false表示不使用
                .setUseMapSetting(UdeskConfig.UdeskMapType.GaoDe, LocationActivity.class, new ILocationMessageClickCallBack() {
                    @Override
                    public void luanchMap(Context context, double latitude, double longitude, String selctLoactionValue) {
                        Intent intent = new Intent();
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Position, selctLoactionValue);
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Latitude, latitude);
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Longitude, longitude);
                        intent.setClass(context, ShowSelectLocationActivity.class);
                        context.startActivity(intent);
                    }
                })
                .setUseEmotion(set_useemotion.isChecked()) //是否使用表情 true表示使用 false表示不使用
                .setUseMore(set_usemore.isChecked()) // 是否使用更多控件 展示出更多功能选项 true表示使用 false表示不使用
                .setUseNavigationSurvy(set_use_navigation_survy.isChecked()) //设置是否使用导航UI中的满意度评价UI rue表示使用 false表示不使用
                .setUseSmallVideo(set_use_smallvideo.isChecked())  //设置是否需要小视频的功能 rue表示使用 false表示不使用
                .setScaleImg(set_use_isscaleimg.isChecked()) //上传图片是否使用原图 还是缩率图
                .setScaleMax(1024) // 缩放图 设置最大值，如果超出则压缩，否则不压缩
                .setOrientation(landscape.isChecked() ? UdeskConfig.OrientationValue.landscape :
                        (user.isChecked() ? UdeskConfig.OrientationValue.user : UdeskConfig.OrientationValue.portrait)) //设置默认屏幕显示习惯
                .setUserForm(true) //在没有请求到管理员在后端对sdk使用配置下，在默认的情况下，是否需要表单留言，true需要， false 不需要
                .setDefualtUserInfo(getDefualtUserInfo()) // 创建用户基本信息
                .setDefinedUserTextField(getDefinedUserTextField()) //创建用户自定义的文本信息
                .setDefinedUserRoplist(getDefinedUserRoplist()) //创建用户自定义的列表信息
                .setUpdateDefualtUserInfo(getUpdateDefualtUserInfo()) // 设置更新用户的基本信息
                .setUpdatedefinedUserTextField(getUpdateDefinedTextField()) //设置用户更新自定义字段文本信息
                .setUpdatedefinedUserRoplist(getUpdateDefinedRoplist()) //设置用户更新自定义列表字段信息
                .setFirstMessage(firstMessage.getText().toString()) //设置带入一条消息  会话分配就发送给客服
                .setCustomerUrl(customerUrl.getText().toString()) //设置客户的头像地址
                .setRobot_modelKey(robot_modelKey.getText().toString()) // udesk 机器人配置插件 对应的Id值
                .setConcatRobotUrlWithCustomerInfo(robpt_customer_info.getText().toString())
                .setCommodity(set_use_commodity.isChecked() ? createCommodity() : null)//配置发送商品链接的mode
                .setExtreFunctions(getExtraFunctions(), new IFunctionItemClickCallBack() {
                    @Override
                    public void callBack(Context context, ChatActivityPresenter mPresenter, int id, String name) {

                        if (id == 21) {
                            UdeskSDKManager.getInstance().toLanuchHelperAcitivty(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());
                            mPresenter.sendTxtMessage("打开帮助中心");
                        } else if (id == 22) {
                            mPresenter.sendTxtMessage("打开表单留言");
                            UdeskSDKManager.getInstance().goToForm(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());
                        }
                    }
                })//在more 展开面板中设置额外的功能按钮
                .setNavigations(set_use_navigation_view.isChecked(), getNavigations(), new INavigationItemClickCallBack() {
                    @Override
                    public void callBack(Context context, ChatActivityPresenter mPresenter, NavigationMode navigationMode) {
                        if (navigationMode.getId() == 1) {
                            UdeskSDKManager.getInstance().toLanuchHelperAcitivty(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());
                        } else if (navigationMode.getId() == 2) {
                            mPresenter.sendTxtMessage(UUID.randomUUID().toString());
                            mPresenter.sendTxtMessage("www.baidu.com");
                        }
                    }
                })//设置是否使用导航UI rue表示使用 false表示不使用
                .setTxtMessageClick(new ITxtMessageWebonCliclk() {
                    @Override
                    public void txtMsgOnclick(String url) {
                        Toast.makeText(getApplicationContext(), "对文本消息中的链接消息处理设置回调", Toast.LENGTH_SHORT).show();
                    }
                })   //如果需要对文本消息中的链接消息处理可以设置该回调，点击事件的拦截回调。 包含表情的不会拦截回调。
                .setFormCallBack(new IUdeskFormCallBack() {
                    @Override
                    public void toLuachForm(Context context) {
                        Toast.makeText(getApplicationContext(), "不用udesk系统提供的留言功能", Toast.LENGTH_SHORT).show();
                    }
                })//离线留言表单的回调接口：  如果不用udesk系统提供的留言功能，可以设置该接口  回调使用自己的处理流程
                .setStructMessageCallBack(new IUdeskStructMessageCallBack() {

                    @Override
                    public void structMsgCallBack(Context context, String josnValue) {
                        Toast.makeText(getApplicationContext(), "结构化消息控件点击事件回调", Toast.LENGTH_SHORT).show();
                    }
                })//设置结构化消息控件点击事件回调接口.
        ;

        return builder;
    }

  ```    
     
### 3. 进入页面分配会话

``` java
    
  UdeskSDKManager.getInstance().entryChat(getApplicationContext(), makeBuilder().build(), sdkToken);
  注意：只有通过这个方法进入会话,管理员在后台配置的选项才会生效, 其它方式进入会话,配置不会生效。 
      
``` 

### 4. Proguard
  
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
### 5. 如果需要设置咨询对象，参照如下设置
  ``` java
      UdeskCommodityItem item = new UdeskCommodityItem();
        item.setTitle("木林森男鞋新款2016夏季透气网鞋男士休闲鞋网面韩版懒人蹬潮鞋子");// 商品主标题
        item.setSubTitle("¥ 99.00");//商品副标题
        item.setThumbHttpUrl("https://img.alicdn.com/imgextra/i1/1728293990/TB2ngm0qFXXXXcOXXXXXXXXXXXX_!!1728293990.jpg_430x430q90.jpg");// 左侧图片
        item.setCommodityUrl("https://detail.tmall.com/item.htm?spm=a1z10.3746-b.w4946-14396547293.1.4PUcgZ&id=529634221064&sku_properties=-1:-1");// 商品网络链接
        UdeskSDKManager.getInstance().setCommodity(item);
       在进入会话界面前调用 。
  ```
  
### 6. 多语言设置
  
  ``` java
  LocalManageUtil.saveSelectLanguage
  
  ```
  
  **更多功参考demo。**
  

### demo下载

[demo下载](https://s.beta.myapp.com/myapp/rdmexp/exp/file2/2018/11/26/udesksdkdemo_1.0_032e649f-e3ce-568d-8524-46fd7cb55fbe.apk)

### SDK中功能项说明

 ###  设置自定义表情的说明
     
	1，自定义表情必须在assets下建立udeskemotion目录，当程序启动时，会自动将assets的udeskemotion目录下所有的贴图复制到贴图的存放位置；
	2，udeskemotion目录下必须是 一个tab图标+一个贴图文件夹，两者必须同名 
	具体参考demo
   
### IM中聊天功能 集成发送地理位置的信息说明
     
	 android 接入的第三方选择性比较多等原因，没有直接在SDK中内嵌地图SDK，由客户根据实际需要选择是否集成发送地理位置信息。
	 提供集成地图的demo例子有：百度地图（见baidumapdemo 这个module），高德地图（gaodemapdemo）腾讯地图（tenxunmapdemo）
	 
	 
	 1.地图类型的说明
	 百度地图设置
     UdeskConfig.useMapType = UdeskConfig.UdeskMapType.BaiDu; 
	 高德地图设置
	 UdeskConfig.useMapType = UdeskConfig.UdeskMapType.GaoDe;
	 腾讯地图设置
	 UdeskConfig.useMapType = UdeskConfig.UdeskMapType.Tencent;
	 其它地图设置
	 UdeskConfig.useMapType = UdeskConfig.UdeskMapType.Other;
	   
	UdeskChatActivity是通过startActivityForResult方式进入LocationActivity,在之后选择相应信息回传到UdeskChatActivity，是通过Intent方式。
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
	    
		
	    .setUseMap(set_usemap.isChecked()) //是否使用发送位置功能  true表示使用 false表示不使用
                .setUseMapSetting(UdeskConfig.UdeskMapType.GaoDe, LocationActivity.class, new ILocationMessageClickCallBack() {
                    @Override
                    public void luanchMap(Context context, double latitude, double longitude, String selctLoactionValue) {
                        Intent intent = new Intent();
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Position, selctLoactionValue);
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Latitude, latitude);
                        intent.putExtra(UdeskConfig.UdeskMapIntentName.Longitude, longitude);
                        intent.setClass(context, ShowSelectLocationActivity.class);
                        context.startActivity(intent);
                    }
                })
       
### IM 使用视频功能

    注意：需要使用视频功能，一定需要使用UdeskSDKUI IM中功能 ,但可以单独使用UdeskSDKUI功能  
	在你的app中 在依赖udeskvideo 模块。

### 支持自定义设置 功能按钮  具体可参考demo

      demo中的代码片断
	  ``` java
      .setExtreFunctions(getExtraFunctions(), new IFunctionItemClickCallBack() {
                    @Override
                    public void callBack(Context context, ChatActivityPresenter mPresenter, int id, String name) {

                        if (id == 21) {
                            mPresenter.sendTxtMessage("发送一条消息");
                        } else if (id == 22) {
                            mPresenter.sendTxtMessage("打开表单留言");
                            UdeskSDKManager.getInstance().goToForm(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());
                        }
                    }
                })//在more 展开面板中设置额外的功能按钮
		
	   说明：一个功能按钮设置成一个FunctionMode， 包含属性
	        //显示内容
             private String name;
            //用来映射选择后对应的操作 id值 前20 是udesk 预留的,  客户自定义添加的，用于返回后根据id值建立映射关系
            private int id;
            //如 R.drawable.udesk_001
            //显示的图标
            private int mIconSrc ;
		

       提供的自定义后续功能操作 ：直接发送文本,图片,视频,文件,地理位置,商品信息		
	   根据接口回调返回的 参数进行调用方法操作 ChatActivityPresenter mPresenter
	   
	        1.发送文本消息
		   public void sendTxtMessage(String msgString)
		   mPresenter.sendTxtMessage( msgString)
			2.发送图片消息 
		   public void sendBitmapMessage(Bitmap bitmap)
		   mPresenter.sendBitmapMessage(bitmap)
			3.发送文件类的消息( 包含视频 文件 图片)
		  /**
			* @param filepath
			* @param msgType  图片:UdeskConst.ChatMsgTypeString.TYPE_IMAGE
			*                 文件:UdeskConst.ChatMsgTypeString.TYPE_File
			*                 MP4视频: UdeskConst.ChatMsgTypeString.TYPE_VIDEO
			*/
		  public void sendFileMessage(String filepath, String msgType)
			 
		  mPresenter.sendFileMessage(filepath,UdeskConst.ChatMsgTypeString.TYPE_IMAGE)
		  mPresenter.sendFileMessage(filepath,UdeskConst.ChatMsgTypeString.TYPE_File)
		  mPresenter.sendFileMessage(filepath,UdeskConst.ChatMsgTypeString.TYPE_VIDEO)
			 
			4.发送地理位置信息
		   public void sendLocationMessage(double lat, double longitude, String localvalue, String bitmapDir)
			  
		   mPresenter.sendLocationMessage(lat, longitude, localvalue, bitmapDir)
			5.发送录音信息
		   public void sendRecordAudioMsg(String audiopath, long duration)
		   mPresenter.sendRecordAudioMsg(audiopath,duration)
	 ``` 
###  支持自定义导航栏设置 具体可参考demo
		代码片断
		 ``` java
		.setNavigations(set_use_navigation_view.isChecked(), getNavigations(), new INavigationItemClickCallBack() {
                    @Override
                    public void callBack(Context context, ChatActivityPresenter mPresenter, NavigationMode navigationMode) {
                        if (navigationMode.getId() == 1) {
                            UdeskSDKManager.getInstance().toLanuchHelperAcitivty(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());
                        } else if (navigationMode.getId() == 2) {
                            mPresenter.sendTxtMessage(UUID.randomUUID().toString());
                            mPresenter.sendTxtMessage("www.baidu.com");
                        }
                    }
                })
		  ``` 
 说明：导航栏一个功能按钮设置成一个NavigationMode， 包含属性
        //文字的显示内容
      private String name;
      //用来映射选择后对应的操作
      private int id;
	  
	  支持自定义的功能 同 功能按钮
	  
### 发送商品消息 具体可参考demo  
	  
	  **Product字段属性说明**
	  
| key           | 是否必选   | 说明         |
| ------------- | ------ | ---------- |
| **name** | **必选** | **商品名称** |
| url     | 可选     | 商品跳转链接(新页显示)，如果值为空，则不能点击      |
| imgUrl         | 可选     | 商品显示图片的url       |
| params   | 可选     | 参数列表       |
|ParamsBean.text | 可选| 参数文本|
|ParamsBean.color | 可选| 参数颜色值，规定为十六进制值的颜色| 
|ParamsBean.fold | 可选| 是否粗体| 
|ParamsBean.breakX | 可选| 是否换行| 
|ParamsBean.size | 可选| 字体大小| 
	  
用法场景:  
1 可以每次进入会话, 通过UdeskConfig配置,设置一条商品消息,
2 可以通过导航栏 自定义功能按钮  发送商品消息 

  
	
## 常见问题

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
	
   
  
``` 


## 导入集成

你所要做的是把UdeskSDKUI做为独立的module import, 并在你APP build.gradle文件中加入：

``` java
dependencies {
    compile project(':UdeskSDKUI')
}

```
### 初始化客户逻辑

``` java
1使用主键 sdk_token customer_token email cellphone 依次查找用户,找到转1.1
     1.1 设找到的用户为customer
     1.2 如果有 sdk_token 参数并且不与 customer中原有的sdk_token相同, go 1.2.1
     1.2.1 更新用户主键及附加信息
     1.3 更新 device
2创建用户 email 没有会默认生成
3创建用户 device
4如果转入 customer_token 但以当前 customer.open_api_token 不同,也不与其它客户冲突,更新 customer_token 到客户 open_api_token 字段


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


### 启动帮助中心界面

Udek系统帮助中心后台可以创建帮助文档，客户通过帮助中心可查看相关文档。调用以下接口启动帮助中心界面

```java
UdeskSDKManager.getInstance().toLanuchHelperAcitivty(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());
```

## Udesk SDK API说明

 ### 1更新客户信息

  UdeskConfig.Builder builder = new UdeskConfig.Builder();

  更新系统默认客户字段，昵称、邮箱、电话、描述

```java
Map<String, String> info = new HashMap<String, String>();
info.put(UdeskConst.UdeskUserInfo.NICK_NAME,"更新后的昵称");
//更新后的邮箱
info.put(UdeskConst.UdeskUserInfo.EMAIL,"0631@163.com");
//更新后的手机号
info.put(UdeskConst.UdeskUserInfo.CELLPHONE,"15651818750");
info.put(UdeskConst.UdeskUserInfo.DESCRIPTION,"更新后的描述信息")
info.put(UdeskConst.UdeskUserInfo.CUSTOMER_TOKEN,"对应的custom_token 不要乱传")

//传入需要更新的Udesk系统默认字段
注意更新邮箱或者手机号码，如果在后端有同样的手机号或邮箱，则会更新失败     
builder.setUpdateDefualtUserInfo(info)   
```

### 2更新自定义字段

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
 builder.setUpdatedefinedUserTextField(updateTextFieldMap);
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
builder.setUpdatedefinedUserRoplist(updateRoplistMap);
```

### 3.发送咨询对象

在客户与客服对话时，经常需要将如咨询商品或订单发送给客服以便客服查看。

咨询对象目前最多支持发送4个属性(detail,image,title,url)，如下以商品举例说明

```java
 //创建咨询对象的实例
UdeskCommodityItem item = new UdeskCommodityItem();
// 咨询对象主标题
item.setTitle("木林森男鞋新款2016夏季透气网鞋男士休闲鞋网面韩版懒人蹬潮鞋子");
//咨询对象描述
item.setSubTitle("¥ 99.00");
//左侧图片
item.setThumbHttpUrl("https://img.alicdn.com/imgextra/i1/1728293990/TB2ngm0qFXXXXcOXXXXXXXXXXXX_!!1728293990.jpg_430x430q90.jpg");
// 咨询对象网络链接
item.setCommodityUrl("https://detail.tmall.com/item.htm?spm=a1z10.3746-b.w4946-14396547293.1.4PUcgZ&id=529634221064&sku_properties=-1:-1");

builder.setCommodity(item);    

//发送咨询对象信息 见ChatActivityPresenter类中的sendCommodityMessage方法
public void sendCommodityMessage(UdeskCommodityItem commodityItem) {
UdeskMessageManager.getInstance().sendComodityMessage(buildCommodityMessage(commodityItem),
        mChatView.getAgentInfo().getAgentJid());
}
```



### 4.获取未读消息

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

### 5获取未读消息数

sdk 3.2.0版本开始，可在退出对话界面后，没有断开与Udesk服务器的连接，可获得这个会话的未读消息数，打开对话界面后未读消息数会清空。

```java
UdeskSDKManager.getInstance().getCurrentConnectUnReadMsgCount();
```

### 6 删除客户聊天数据

sdk初始化成功，创建客户后，调用此接口可删除当前客户的聊天记录信息

```java
UdeskSDKManager.getInstance().deleteMsg();

```
### 7 控制台日志开关

如果开发中，想在控制台看当前客户与Udesk服务器连接（xmpp)的交互报文，调用如下接口可实现

```java
//true 表示开启控制台日志  false表示关闭控制台日志
UdeskSDKManager.getInstance().isShowLog(true);
```

### 8 断开与Udesk服务器连接

  App运行时如果需要客服离线或不再接收客服消息，调此接口可以主动断开与Udesk服务器的的连接。

```java
UdeskSDKManager.getInstance().disConnectXmpp();
```
### 9 设置退出排队的模式

 quitmode: mark (默认,标记放弃)/ cannel_mark(取消标记) / force_quit(强制立即放弃)
```java
build.setUdeskQuenuMode(quitmode);
```

### 10 可以通过以下文件名称快速定位SDK资源，修改相应的资源可以实现UI自定义

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
    "/udesk_im/sdk/v3/im/sdk_push.json"; /控制推送状态开关的接口
		
```

## 离线消息推送
当前仅支持一种推送方案，即Udesk务端发送消息至开发者的服务端，开发者再推送消息到 App。
### 1 设置接收推送的服务器地址
        推送消息将会发送至开发者的服务器。
	
	设置服务器地址，请使用Udesk管理员帐号登录 Udesk，在 设置 -> 移动SDK 中设置。
![udesk](http://7xr0de.com1.z0.glb.clouddn.com/5D761252-3D9D-467C-93C9-8189D0B22424.png)	

### 2 使用Udesk 推送功能的配置
``` java
   //配置 是否使用推送服务  true 表示使用  false表示不使用
    public  static  boolean isUserSDkPush = false;

``` 
	
### 3 设置用户的设备唯一标识
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
   
### 4	关闭开启Udek推送服务
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

### 5 Udek推送给开发者服务端的接口说明
**基本要求**

- 推送接口只支持 http，不支持 https
- 请求已 POST 方法发送
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


### 更新日志

### 4.1.0+
1. 支持排队时发送消息
2. 替换表情
3. 支持机器人  管理员配置名称
4. 支持自定义渠道 
5. 支持设置 全局客户唯一性customer_token
6. 支持离线消息显示实际发送时间
7. 修改录音文件格式wav，提高客服语音转文字的准确率
8. 优化无消息对话过滤消息保存

### 4.0.5+ 
1. 消息发送优化;
2. 支持机器人key;
3. 机器人SDK自动转人工事件

### 4.0.4+ 
1. 优化消息id的生成

### 4.0.3+
1. rtl的优化

### 4.0.1+
1. 支持商品消息

### 4.0.0+
1. SDK组片图件优化;  

2. SDK支持小视频;

3. 支持自定义表情包;

4. 支持无消息对话过滤;

5. 输入区优化;

6. 满意度优化;

7. SDK留言引导文案显示优化;

8. SDK横屏兼容性优化;

9. SDK支持机器人转人工按钮在x条消息后显示;

10. SDK支持给机器人传modelKey值;

11. IM图片查看支持点击返回;

12. SDK多语言支持优化;

13. 重构SDK中的各种设置;

14. 机器人传客户信息;

15. 8.0 设置方向和设置透明属性的适配;

16. SDK（安卓）部分问题优化;

17. SDK支持消息转人工按钮;

18. sdk 发送图片加上图片后缀;

### 3,9+ 
1. 消息到达率优化 
2. 支持视频聊天

### 3.8.0+
1. 支持发送地理位置信息

### 3.7.1+
1. 欢迎语显示优化

### 3.7.0+
1. 支持离线直接留言;
2. SDK支持返回满意度调查和支持开关设置;
3. SDK支持接收和发送GIF;
4. SDK支持接收和发送视频;
5. SDK支持客服消息撤回

### 3.6.0+
1. sdk增加显示结构化消息
2. 增加黑名单提示语

### 3.5.0 + 
1. sdk初始化的方法修改为 initApiKey(Context context, String domain, String appkey, String appid)
2. 接入会话的方式修改 统一调用entryChat(Context context)，内部处理了根据管理员在后台设置进行相应的业务流程跳转
3. 之前提供的一些接入会话方式接口删除，统一使用见entryChat(Context context) 

### 3.4.0 +
1. 支持推送，
2. 支持多应用

### 3.3.2+
1. http协议换成https

2. 增加sdk端 客户主动放弃满意度调查

### 3.3.1+
1. 适配android7.0

### 3.3+ 
1. 适配中英文，
2. 增加UI配置，
3. 修改连接会话的逻辑，
4. 增加头像的配置，
5. 界面UI的修改

### 3.2.1+
1. 支持黑名单的设置;
2. 客服在线状态优化;
3. 修复已离线的客户在手机端显示在线的；
4. 支持客服头像， 支持显示发送每条消息的客服昵称；
5. 支持更新用户的信息；

### 3.2.0 +
1. 增加发送商品链接，
2. 增加消息通知
3. 增加未读消息的接口
4. 增加android 6.0 运行权限

### 3.1+ 
1. 支持欢迎语设置链接
2. 支持后台设置自定义链接
3. 支持指定客服 id 进行分配
4. 指定客服组 id 进行分配

### 3.0+
1. 新增客服转移和邀请评价功能


### 部分功能截图说明

##### 机器人功能示意图

<img src="http://qn-im.udesk.cn/%E6%9C%BA%E5%99%A8%E4%BA%BA_1540869480_806.png"/>

##### 导航配置示意图

<img  src="http://qn-im.udesk.cn/%E5%AF%BC%E8%88%AA%E8%AE%BE%E7%BD%AE_1540870908_976.png"/>

##### 无消息对话过滤示意图


<img src="http://qn-im.udesk.cn/%E6%97%A0%E6%B6%88%E6%81%AF%E5%AF%B9%E8%AF%9D%E8%BF%87%E6%BB%A4_1540881672_329.png"/>


##### 消息对话示意图

<img src="http://qn-im.udesk.cn/%E5%8A%9F%E8%83%BD%E5%9B%BE%E7%89%87_1540881751_124.png"/>


<img  src="http://qn-im.udesk.cn/%E5%8A%9F%E8%83%BD2_1540870974_781.png"/>


<img src="http://qn-im.udesk.cn/%E5%8A%9F%E8%83%BD3_1540870997_368.png"/>

##### 自定义表情
<img src="http://qn-im.udesk.cn/%E8%87%AA%E5%AE%9A%E4%B9%89%E8%A1%A8%E6%83%85_1540871031_250.png"/>


##### 自定义按钮

<img src="http://qn-im.udesk.cn/%E6%94%AF%E6%8C%81%E8%87%AA%E5%AE%9A%E4%B9%89%E6%8C%89%E9%92%AE_1540881846_830.png"/>



##### 满意度评价示意图

<img  src="http://qn-im.udesk.cn/%E6%BB%A1%E6%84%8F%E5%BA%A6%E8%AF%84%E4%BB%B7_1540881195_147.png"/>



##### 留言示意图

<img  src="http://qn-im.udesk.cn/%E8%A1%A8%E5%8D%95%E7%95%99%E8%A8%80_1540871121_461.png"/>

<img  src="http://qn-im.udesk.cn/%E7%9B%B4%E6%8E%A5%E7%95%99%E8%A8%80_1540871126_218.png"/>







