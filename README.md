# UdeskSDK-Android #
### 公告

SDK原生机器人功能在 5.x 分支下。
### SDK下载地址
[Udesk-Android 源码下载地址](https://github.com/udesk/UdeskSDK-Android)

[demo 下载地址](https://pro-cs-freq.kefutoutiao.com/doc/im/tid3055/udesksdk_5.1.4_androidQ_1577794274942_2t505.apk)

## 目录
- [一、特别声明](#1)
- [二、集成SDK](#2)
- [三、快速使用SDK](#3)
- [四、Udesk SDK 自定义配置](#4)
- [五、Udesk API说明](#5)
- [六、消息推送](#6)
- [七、常见问题](#7)
- [八、更新记录](#8)
- [九、功能截图](#9)
<h1 id="1">一、特别声明</h1>

### 5.1.0 设置商品消息背景、字体api发生调整 请注意更改 ###

### SDK 采用AAC框架 ###

### customer_token， sdk_token 仅支持字母、数字及下划线,禁用特殊字符

<h1 id="2">二、集成SDK</h1>

| Demo中的文件| 说明                            |
| ---------  | -----------------------         |
| UdeskSDKUI | Udesk在线咨询SDK                 |
| Udeskvideo | Udesk视频会话SDK（依赖在线咨询SDK）|
| Udeskasr	 | Udesk原生机器人语音识别功能（依赖在线咨询SDK）       |

### **注意：UdeskSDKUI并不依赖Udeskvideo和 udeskasr,如果不需要则不要导入该sdk。**

你所要做的是把UdeskSDKUI做为独立的module import, 并在你APP build.gradle文件中加入：

	dependencies {
	    api project(':UdeskSDKUI')
	}

在 root build.gradle 文件中加入：

	allprojects {
		repositories {
        	maven { url "https://jitpack.io" }
    	}
	}

### 用户创建及更新逻辑:

	1. 使用主键 [sdk_token customer_token email cellphone] (默认primary_key为sdk_token), 或 [customer_token sdk_token email cellphone] (primary_key 为 customer_taken时) 依次查找用户
	
		1. 设 primary_key默认, sdk_token 找到的用户为 customerA
			1.1 在不冲突情况下,更新用户主键 customer_token email cellphone,转1.4
			1.2 当存在冲突, 冲突的主键 customer_token email cellphone会被忽略
		2. 设 primary_key == 'customer_token' , customer_token 找到的用户为 customerA
			2.1 不存在另外客户 customerB.sdk_token 等于 sdk_token,更新sdk_token
			2.2 存在另外的客户 customerB.sdk_token 等于 sdk_token, customerB sdk_token 被更新为 原sdk_token + '_' + customerA.id + '_' + 时间戳, 昵称改为 新传入的 nick_name + ' ' + 'sdk',至此,用户 customerB将被视为更改主键
		3. email cellphone 主键处理
			3.1 不冲突时更新 email cellphone
			3.2 冲突时忽略 email cellphone
		4. 更新客户其它非主键信息 customer_field nick_name qq description lang
	
		5. 更新 device
	
		6. 更新用户 ip 及 省份信息
	
	2. 创建用户 device

**注意** 
	
	现在根据primary_key的值来作为客户的唯一标识，用来识别身份。
	
	如果customer_token的值不为空， primary_key 的值为customer_token 以customer_token的值作为客户的唯一标识，用来识别身份。
	
	如果customer_token的值为空， primary_key 的值为sdk_token 以sdk_token的值作为客户的唯一标识，用来识别身份。
	
	customer_token sdk_token: 传入的字符请使用 字母 / 数字 字符集**  。就如同身份证一样，不允许出现一个身份证号对应多个人，或者一个人有多个身份证号;
	
	其次,如果给顾客设置了邮箱和手机号码，也要保证不同顾客对应的手机号和邮箱不一样，如出现相同的，则不会创建新顾客。  

**完成了以上操作，接下来就可以使用UdeskSDK的其它功能了，祝你好运！**

### 启动帮助中心界面

Udek系统帮助中心后台可以创建帮助文档，客户通过帮助中心可查看相关文档。调用以下接口启动帮助中心界面

```java
UdeskSDKManager.getInstance().toLaunchHelperAcitivty(getApplicationContext(), UdeskSDKManager.getInstance().getUdeskConfig());
```


<h1 id="3">三、快速使用SDK</h1>

### 1.初始管理员后台创建应用是生成的对应app key 和 app id

      UdeskSDKManager.getInstance().initApiKey(context, "you domain","App Key","App Id");
      
      注意：域名不要带有http://部分，假如注册生成的域名是"http://udesksdk.udesk.cn/" ,只要传入"udesksdk.udesk.cn"

### 2.设置UdeskConfig配置信息。

**说明：配置的功能根据你们实际的需要进行选择，都有默认行为。**

### **2.1 设置用户的基本信息**
### **注意sdktoken必填**
	 
	  默认系统字段是Udesk已定义好的字段，开发者可以直接传入这些用户信息，供客服查看。
      String sdktoken = “用户唯一的标识”; 
      Map<String, String> info = new HashMap<String, String>();
      **//sdktoken 必填**
      info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, sdktoken);
      //以下信息是可选
      info.put(UdeskConst.UdeskUserInfo.NICK_NAME,"昵称");
      info.put(UdeskConst.UdeskUserInfo.EMAIL,"0631@163.com");
      info.put(UdeskConst.UdeskUserInfo.CELLPHONE,"15651818750");
      info.put(UdeskConst.UdeskUserInfo.DESCRIPTION,"描述信息");
	  info.put(UdeskConst.UdeskUserInfo.CUSTOMER_TOKEN,custom_token);
      只设置用户基本信息的配置
      UdeskConfig.Builder builder = new UdeskConfig.Builder();
	  builder.setDefualtUserInfo(info)
	  UdeskSDKManager.getInstance().entryChat(getApplicationContext(), builder.build(), sdkToken);

### **2.2 UdeskConfig内部类Builder的说明**
| 属性           | 设置方法   | 功能说明         |
| ------------- | ------ | ---------- |
| udeskTitlebarBgResId        			 | setUdeskTitlebarBgResId       							  | 标题栏TitleBar的背景色,通过颜色设置                             |
| udeskTitlebarMiddleTextResId           | setUdeskTitlebarMiddleTextResId                            | 标题栏TitleBar，中部上下文字的颜色                              |
| udeskTitlebarRightTextResId            | setUdeskTitlebarRightTextResId                             | 标题栏TitleBar，右侧文字的颜色                                  |
| udeskIMLeftTextColorResId              | setUdeskIMLeftTextColorResId                               | IM界面，左侧文字的字体颜色                                      |
| udeskIMRightTextColorResId             | setUdeskIMRightTextColorResId                              | IM界面，右侧文字的字体颜色                                      |
| udeskIMAgentNickNameColorResId         | setUdeskIMAgentNickNameColorResId                          | IM界面，左侧客服昵称文字的字体颜色                               |
| udeskIMCustomerNickNameColorResId      | setUdeskIMCustomerNickNameColorResId                       | IM界面，右侧客户昵称文字的字体颜色                               |
| udeskIMTimeTextColorResId              | setUdeskIMTimeTextColorResId                               | IM界面，时间文字的字体颜色                                      |
| udeskIMTipTextColorResId               | setUdeskIMTipTextColorResId                                | IM界面，提示语文字的字体颜色，比如客服转移                        |
| udeskbackArrowIconResId                | setUdeskbackArrowIconResId                                 | 返回箭头图标资源id                                             |
| udeskCommityBgResId                    | setUdeskCommityBgResId                                     | 咨询商品item的背景颜色                                         |
| udeskCommityTitleColorResId            | setUdeskCommityTitleColorResId                             | 商品介绍Title的字样颜色                                        |
| udeskCommitysubtitleColorResId         | setUdeskCommitysubtitleColorResId                          | 商品咨询页面中，商品介绍子Title的字样颜色                        |
| udeskCommityLinkColorResId             | setUdeskCommityLinkColorResId                              | 商品咨询页面中，发送链接的字样颜色                               |
| isUserSDkPush                          | setUserSDkPush                                             | 配置 是否使用推送服务  true 表示使用  false表示不使用            |
| UdeskQuenuMode                         | setUdeskQuenuMode                                          | 配置放弃排队的策略                                             |
| isUseVoice                             | setUseVoice                                                | 是否使用录音功能  true表示使用 false表示不使用                   | 
| isUsephoto                             | setUsephoto                                                | 是否使用发送图片的功能  true表示使用 false表示不使用             | 
| isUsecamera                            | setUsecamera                                               | 是否使用拍照的功能  true表示使用 false表示不使用                 |      
| isUsefile                              | setUsefile                                                 | 是否使用上传文件功能  true表示使用 false表示不使用               |  
| isUseMap                               | setUseMap                                                  | 是否使用发送位置功能  true表示使用 false表示不使用               |  
| isUseEmotion                           | setUseEmotion                                              | 是否使用表情 true表示使用 false表示不使用                       |  
| isUseMore                              | setUseMore                                                 | 否使用展示出更多功能选项 true表示使用 false表示不使用             |
| isUseNavigationRootView                | setNavigations                                             | 设置是否使用导航UI true表示使用 false表示不使用                  |  
| isUseRobotNavigationRootView           | setRobotNavigations                                        | 设置是否使用机器人导航UI rue表示使用 false表示不使用             |
| isUseNavigationSurvy                   | setUseNavigationSurvy                                      | 设置是否使用导航UI中的满意度评价UI rue表示使用 false表示不使用    |     
| isUseSmallVideo                        | setUseSmallVideo                                           | 设置是否需要小视频的功能 rue表示使用 false表示不使用             | 
| isScaleImg                             | setScaleMax                                                | 设置宽高最大值，如果超出则压缩，否则不压缩                       |   
| ScaleMax                               | setScaleImg                                                | 上传图片是否使用原图 还是缩率图                                |  
| useMapType                             | setUseMapSetting                                           | 设置使用那种地图                                             | 
| Orientation                            | setOrientation                                             | 设置默认屏幕显示习惯                                          |  
| isUserForm                             | setUserForm                                                | 本地配置是否需要表单留言，true需要， false 不需要               |  
| defaultUserInfo                        | setDefaultUserInfo                                         | 创建用户的基本信息                                           |  
| definedUserTextField                   | setDefinedUserTextField                                    | 创建自定义的文本信息                                         |
| definedUserRoplist                     | setDefinedUserRoplist                                      | 创建自定义的列表信息                                         |    
| firstMessage                           | setFirstMessage                                            | 设置带入一条消息  会话分配就发送给客服                         |  
| robot_modelKey                         | setRobot_modelKey                                          |  udesk 机器人常见问题 对应的Id值                            |  
| concatRobotUrlWithCustomerInfo         | setConcatRobotUrlWithCustomerInfo                          |  用于机器人页面收集客户信息                                   |  
| customerUrl                            | setCustomerUrl                                             |  设置客户的头像地址                                          |    
| commodity                              | setCommodity                                               |  配置发送商品链接的mode                                      |  
| txtMessageClick                        | setTxtMessageClick                                         | 文本消息中的链接消息的点击事件的拦截回调。 包含表情的不会拦截回调  |  
| formCallBack                           | setFormCallBack                                            | 离线留言表单的回调接口 ，回调使用自己的处理流程                 |  
| structMessageCallBack                  | setStructMessageCallBack                                   | 设置结构化消息的点击事件回调接口                               |  
| extreFunctions                         | setExtreFunctions                                          | 设置额外的功能按钮                                           |  
| functionItemClickCallBack              | setExtreFunctions                                          | 点击事件回调 直接发送文本,图片,视频,文件,地理位置,商品信息       |  
| navigationModes                        | setNavigations                                             | 约定传递的自定义按钮集合                                      |  
| robotnavigationModes                   | setRobotNavigations                                        | 约定传递的自定义按钮集合                                      |  
| navigationItemClickCallBack            | setNavigations                                             | 支持客户在导航处添加自定义按钮的点击回调事件                    | 
| locationMessageClickCallBack           | setUseMapSetting                                           | 点击地理位置信息的回调接口                                    | 
| cls                                    | setUseMapSetting                                           | 传入打开地图消息显示的详请activity                            | 
| groupId                                | setGroupId                                                 | 设置的指定组，每次进入都必须重新指定                           | 
| isOnlyByGroupId                        | setGroupId                                                 | 是否仅仅指定组进入                                           | 
| agentId                                | setAgentId                                                 | 设置指订客服id，每次进入都必须重新指定                         | 
| isOnlyByAgentId                        | setAgentId                                                 | 是否仅仅指定客服进入                                         | 
| isOnlyUseRobot                         | setOnlyUseRobot                                            | 设置是否只使用机器人 不用其它功能                              | 
| mProduct                               | setProduct                                                 | 设置商品消息             									   |
| channel                                | setChannel                                                 | SDK支持自定义渠道（只支持字符数字，不支持特殊支持）  			   |
| isShowCustomerNickname                 | isShowCustomerNickname                                     | 是否显示客户昵称  				                           |
| isShowCustomerHead                 	 | isShowCustomerHead                                         | 是否显示客户头像  				                           |
| udeskProductLeftBgResId                | setUdeskProductLeftBgResId                                 | 商品消息背景左侧                                             |
| udeskProductRightBgResId               | setUdeskProductRightBgResId                                | 商品消息背景右侧                                             |
| udeskProductRightNameLinkColorResId    | setUdeskProductRightNameLinkColorResId                     | 商品消息的 带有链接时的  商品名字显示的颜色 右侧                |
| udeskProductLeftNameLinkColorResId     | setUdeskProductLeftNameLinkColorResId                      | 商品消息的 带有链接时的  商品名字显示的颜色 左侧                 |
| udeskProductMaxLines                   | setUdeskProductMaxLines                                    | 商品消息名称最大显示行数                                      |


	    private UdeskConfig.Builder makeBuilder() {
        if (!TextUtils.isEmpty(edit_language.getText().toString())){
            LocalManageUtil.saveSelectLanguage(getApplicationContext(),new Locale(edit_language.getText().toString()));
        }

        UdeskConfig.Builder builder = new UdeskConfig.Builder();
        builder.setUdeskTitlebarBgResId(R.color.udesk_titlebar_bg1) //设置标题栏TitleBar的背景色
                .setUdeskTitlebarMiddleTextResId(R.color.udesk_color_middle_text) //设置标题栏TitleBar，左右两侧文字的颜色
                .setUdeskTitlebarRightTextResId(R.color.udesk_color_right_text) //设置标题栏TitleBar，右侧文字的颜色
                .setUdeskIMLeftTextColorResId(R.color.udesk_color_im_text_left1) //设置IM界面，左侧文字的字体颜色
                .setUdeskIMRightTextColorResId(R.color.udesk_color_im_text_right1) // 设置IM界面，右侧文字的字体颜色
                .setUdeskIMAgentNickNameColorResId(R.color.udesk_color_im_left_nickname1) //设置IM界面，左侧客服昵称文字的字体颜色
                .setUdeskIMCustomerNickNameColorResId(R.color.udesk_color_im_right_nickname1) //设置IM界面，右侧用户昵称文字的字体颜色
                .setUdeskIMTimeTextColorResId(R.color.udesk_color_im_time_text1) // 设置IM界面，时间文字的字体颜色
                .setUdeskIMTipTextColorResId(R.color.udesk_color_im_tip_text1) //设置IM界面，提示语文字的字体颜色，比如客服转移
                .setUdeskbackArrowIconResId(R.drawable.udesk_titlebar_back) // 设置返回箭头图标资源id
                .setUdeskCommityBgResId(R.color.udesk_color_im_commondity_bg1) //咨询商品item的背景颜色
                .setUdeskCommityTitleColorResId(R.color.udesk_color_im_commondity_title1) // 商品介绍Title的字样颜色
                .setUdeskCommitysubtitleColorResId(R.color.udesk_color_im_commondity_subtitle1)// 商品咨询页面中，商品介绍子Title的字样颜色
                .setUdeskCommityLinkColorResId(R.color.udesk_color_im_commondity_link1) //商品咨询页面中，发送链接的字样颜色
                .setUdeskProductLeftBgResId(R.drawable.udesk_im_txt_left_default) //商品消息背景
                .setUdeskProductRightBgResId(R.drawable.udesk_im_item_bg_right) //商品消息背景
                .setUdeskProductMaxLines(2) //商品消息名称最大显示行数
                .setUserSDkPush(set_sdkpush.isChecked()) // 配置 是否使用推送服务  true 表示使用  false表示不使用
                .setOnlyUseRobot(set_use_onlyrobot.isChecked())//配置是否只使用机器人功能 只使用机器人功能,只使用机器人功能;  其它功能不使用。
                .setUdeskQuenuMode(force_quit.isChecked() ? UdeskConfig.UdeskQueueFlag.FORCE_QUIT : UdeskConfig.UdeskQueueFlag.Mark)  //  配置放弃排队的策略
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
                .setDefaultUserInfo(getdefaultUserInfo()) // 创建用户基本信息
                .setDefinedUserTextField(getDefinedUserTextField()) //创建用户自定义的文本信息
                .setDefinedUserRoplist(getDefinedUserRoplist()) //创建用户自定义的列表信息
                .setFirstMessage(firstMessage.getText().toString()) //设置带入一条消息  会话分配就发送给客服
                .setCustomerUrl(customerUrl.getText().toString()) //设置客户的头像地址
                .setRobot_modelKey(robot_modelKey.getText().toString()) // udesk 机器人常见问题 对应的Id值
                .setConcatRobotUrlWithCustomerInfo(robpt_customer_info.getText().toString())
                .setCommodity(set_use_commodity.isChecked() ? createCommodity() : null)//配置发送商品链接的mode
                .setProduct(set_use_prouct.isChecked() ? createProduct() : null)//配置发送商品链接的mode
                .setExtreFunctions(getExtraFunctions(), new IFunctionItemClickCallBack() {
                    @Override
                    public void callBack(Context context, UdeskViewMode udeskViewMode, int id, String name) {
                        if (id == 22) {
                            udeskViewMode.sendCommodityMessage(createCommodity());
                        } else if (id == 23) {
                            UdeskSDKManager.getInstance().disConnectXmpp();
                        } else if (id == 24) {
                            udeskViewMode.sendProductMessage(createProduct());
                        }else if (id == 25) {
                            sendCustomerOrder();
                        }else if (id == 26) {
                            sendTrace();
                        }
                    }
                })//在more 展开面板中设置额外的功能按钮
                .setNavigations(set_use_navigation_view.isChecked(), getNavigations(), new INavigationItemClickCallBack() {
                    @Override
                    public void callBack(Context context, UdeskViewMode udeskViewMode, NavigationMode navigationMode,String currentView) {
                            if (navigationMode.getId() == 1) {
                                udeskViewMode.sendProductMessage(createProduct());
                            } else if (navigationMode.getId() == 2) {
                                udeskViewMode.sendTxtMessage("www.baidu.com");
                            }
                    }
                })//设置是否使用导航UI true表示使用 false表示不使用
                 .setRobotNavigations(set_use_navigation_view_robot.isChecked(), getRobotNavigations(), new INavigationItemClickCallBack() {
                    @Override
                    public void callBack(Context context, UdeskViewMode udeskViewMode, NavigationMode navigationMode,String currentView) {
                        if (TextUtils.equals(currentView,UdeskConst.CurrentFragment.robot)){
                            if (navigationMode.getId() == 1) {
                                udeskViewMode.sendTxtMessage("robot导航");
                            }else if (navigationMode.getId() == 2){
                                udeskViewMode.getRobotApiData().onShowProductClick(createReplyProduct());
                            }
                        }
                    }
                })//设置是否使用机器人导航UI true表示使用 false表示不使用

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
                .setChannel(channel.getText().toString())
                .isShowCustomerNickname(show_customer_nickname.isChecked())//设置是否显示昵称
                .isShowCustomerHead(show_customer_head.isChecked()); //设置是否显示头像

        return builder;
    }



### 3 进入页面分配会话
	UdeskSDKManager.getInstance().entryChat(getApplicationContext(), makeBuilder().build(), sdkToken);
  注意：只有通过这个方法进入会话,管理员在后台配置的选项才会生效, 其它方式进入会话,配置不会生效。 
      
### 4 Proguard
	
	//udesk
	-keep class udesk.** {*;} 
	-keep class cn.udesk.**{*; } 
	//百度语音(如果使用百度语音识别添加 不使用不用添加)
	-keep class com.baidu.speech.**{*;}
	//smack
	-keep class org.jxmpp.** {*;} 
	-keep class de.measite.** {*;} 
	-keep class org.jivesoftware.** {*;} 
	-keep class org.xmlpull.** {*;} 
	-dontwarn org.xbill.**
	-keep class org.xbill.** {*;} 
	//JSONobject
	-keep class org.json.** {*; }
	//okhttp
	-keep class okhttp3.** {*;} 
	-keep class okio.** {*;} 
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
	//glide
	-keep class com.bumptech.glide.Glide { *; }
	-keep public class * implements com.bumptech.glide.module.GlideModule
	-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  	**[] $VALUES;
 	 public *;
	}

	# for DexGuard only
	-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
	//agora
	-keep class io.agora.**{*;}

	# Keep native methods
	-keepclassmembers class * {
	    native <methods>;
	}
	
	-dontwarn okio.**
	-dontwarn com.squareup.okhttp.**
	-dontwarn okhttp3.**
	-dontwarn javax.annotation.**
	-dontwarn com.android.volley.toolbox.**
	-dontwarn com.bumptech.glide.**


<h1 id="4">四、Udesk SDK 自定义配置</h1>


### 1 如果需要设置咨询对象，参照如下设置

      UdeskCommodityItem item = new UdeskCommodityItem();
        item.setTitle("木林森男鞋新款2016夏季透气网鞋男士休闲鞋网面韩版懒人蹬潮鞋子");// 商品主标题
        item.setSubTitle("¥ 99.00");//商品副标题
        item.setThumbHttpUrl("https://img.alicdn.com/imgextra/i1/1728293990/TB2ngm0qFXXXXcOXXXXXXXXXXXX_!!1728293990.jpg_430x430q90.jpg");// 左侧图片
        item.setCommodityUrl("https://detail.tmall.com/item.htm?spm=a1z10.3746-b.w4946-14396547293.1.4PUcgZ&id=529634221064&sku_properties=-1:-1");// 商品网络链接

        UdeskConfig.Builder builder = new UdeskConfig.Builder();
		 .setCommodity(set_use_commodity.isChecked() ? createCommodity() : null)//配置发送商品链接的mode
       注意 在进入会话界面前调用
		
		发送咨询对象信息 在UdeskViewMode中调用
		udeskViewMode.sendCommodityMessage(createCommodity());

### 2 多语言设置

  	LocalManageUtil.saveSelectLanguage


 ### 3 设置自定义表情的说明
     
	1，自定义表情必须在assets下建立udeskemotion目录，当程序启动时，会自动将assets的udeskemotion目录下所有的贴图复制到贴图的存放位置；
	2，udeskemotion目录下必须是 一个tab图标+一个贴图文件夹，两者必须同名 
	具体参考demo

### 4 IM中聊天功能 集成发送地理位置的信息说明
     
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
### 5 IM 使用视频功能

    注意：需要使用视频功能，一定需要使用UdeskSDKUI IM中功能 ,但可以单独使用UdeskSDKUI功能  
	在你的app中 在依赖udeskvideo 模块。

### 6 支持自定义设置 功能按钮  具体可参考demo
	 .setExtreFunctions(getExtraFunctions(), new IFunctionItemClickCallBack() {
                    @Override
                    public void callBack(Context context, UdeskViewMode udeskViewMode, int id, String name) {
                        if (id == 22) {
                            udeskViewMode.sendCommodityMessage(createCommodity());
                        } else if (id == 23) {
                            UdeskSDKManager.getInstance().disConnectXmpp();
                        } else if (id == 24) {
                            udeskViewMode.sendProductMessage(createProduct());
                        }else if (id == 25) {
                            sendCustomerOrder();
                        }else if (id == 26) {
                            sendTrace();
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
	根据接口回调返回的参数  调用udeskViewMode 当中对应的方法进行处理	
	//发送消息
    //封装发送文本消息
    public void sendTxtMessage(String msgString) {
        try {
            MessageInfo msg = UdeskUtil.buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_TEXT,
                    System.currentTimeMillis(), msgString);

            postMessage(msg, UdeskConst.LiveDataType.AddMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	  //发送商品消息
    public void sendProductMessage(Product mProduct) {
        if (mProduct == null) {
            return;
        }
        try {
            MessageInfo msg = UdeskUtil.buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_PRODUCT,
                    System.currentTimeMillis(), JsonUtils.getProduceJson(mProduct).toString());
            postMessage(msg, UdeskConst.LiveDataType.AddMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


	/**
     * 发送地理位置信息
     *
     * @param lat
     * @param longitude
     * @param localvalue
     * @param bitmapDir
     */
    public void sendLocationMessage(double lat, double longitude, String localvalue, String bitmapDir) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(lat).append(";").append(longitude).append(";").append("16;").append(localvalue);
            MessageInfo msg = UdeskUtil.buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_Location,
                    System.currentTimeMillis(), builder.toString(), bitmapDir, "", "");
            postMessage(msg, UdeskConst.LiveDataType.AddMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	 //发送文件类的消息( 包含视频 文件 图片)

    /**
     * @param filepath
     * @param msgType  图片:UdeskConst.ChatMsgTypeString.TYPE_IMAGE
     *                 文件:UdeskConst.ChatMsgTypeString.TYPE_File
     *                 MP4视频: UdeskConst.ChatMsgTypeString.TYPE_SHORT_VIDEO
     */
    public synchronized void  sendFileMessage(String filepath, String msgType) {
        try {
            if (TextUtils.isEmpty(filepath)) {
                return;
            }
            String fileName = (UdeskUtils.getFileName(filepath, msgType));
            String fileSzie = UdeskUtils.getFileSizeByLoaclPath(filepath);
            MessageInfo msgInfo = UdeskUtil.buildSendMessage(msgType,
                    System.currentTimeMillis(), "", filepath, fileName, fileSzie);
            postMessage(msgInfo, UdeskConst.LiveDataType.AddFileMessage);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        }
    }

    // 发送录音信息
    public void sendRecordAudioMsg(String audiopath, long duration) {
        try {
            String fileName = (UdeskUtils.getFileName(audiopath, UdeskConst.FileAduio));
            MessageInfo msgInfo = UdeskUtil.buildSendMessage(
                    UdeskConst.ChatMsgTypeString.TYPE_AUDIO,
                    System.currentTimeMillis(), "", audiopath, fileName, "");
            duration = duration / 1000 + 1;
            msgInfo.setDuration(duration);
            postMessage(msgInfo, UdeskConst.LiveDataType.AddFileMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	//发送原图图片消息
    public void sendBitmapMessage(Bitmap bitmap, Context context) {
        try {
            if (bitmap == null) {
                return;
            }
            File scaleImageFile = UdeskUtil.getScaleFile(bitmap, context);
            if (scaleImageFile != null) {
                MessageInfo msgInfo = UdeskUtil.buildSendMessage(
                        UdeskConst.ChatMsgTypeString.TYPE_IMAGE,
                        System.currentTimeMillis(), "", scaleImageFile.getPath(), "", "");
                postMessage(msgInfo, UdeskConst.LiveDataType.AddFileMessage);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
### 7 支持自定义导航栏设置 具体可参考demo
 	
	 .setNavigations(set_use_navigation_view.isChecked(), getNavigations(), new INavigationItemClickCallBack() {
                    @Override
                    public void callBack(Context context, UdeskViewMode udeskViewMode, NavigationMode navigationMode,String currentView) {
                            if (navigationMode.getId() == 1) {
                                udeskViewMode.sendProductMessage(createProduct());
                            } else if (navigationMode.getId() == 2) {
                                udeskViewMode.sendTxtMessage("www.baidu.com");
                            }
                    }
                })//设置是否使用导航UI true表示使用 false表示不使用
                 .setRobotNavigations(set_use_navigation_view_robot.isChecked(), getRobotNavigations(), new INavigationItemClickCallBack() {
                    @Override
                    public void callBack(Context context, UdeskViewMode udeskViewMode, NavigationMode navigationMode,String currentView) {
                        if (TextUtils.equals(currentView,UdeskConst.CurrentFragment.robot)){
                            if (navigationMode.getId() == 1) {
                                udeskViewMode.sendTxtMessage("robot导航");
                            }else if (navigationMode.getId() == 2){
                                udeskViewMode.getRobotApiData().onShowProductClick(createReplyProduct());
                            }
                        }
                    }
                })//设置是否使用机器人导航UI true表示使用 false表示不使用


说明：导航栏一个功能按钮设置成一个NavigationMode， 包含属性

	//文字的显示内容
      private String name;
      //用来映射选择后对应的操作
      private int id;
	  
	  支持自定义的功能 同 功能按钮  
***注意*** 机器人聊天自定义导航只支持文本和商品回复类型，其他类型不支持。
### 8 发送商品回复消息 

	 private ProductListBean createReplyProduct(){
        ProductListBean productListBean=new ProductListBean();
        productListBean.setId(1);
        productListBean.setUrl("https://item.jd.com/7633415.html");
        productListBean.setImage("https://udeskzgh.oss-cn-beijing.aliyuncs.com/demo/sumsung.jpg");
        productListBean.setName("【SSD套装版】三星 Galaxy S 轻奢版（SM-G8750）4GB +64GB");
        List<InfoListBean> infoList = new ArrayList<>();

        InfoListBean bean0= new InfoListBean();
        bean0.setInfo("新品");
        bean0.setColor("#00ff00");
        bean0.setBoldFlag(0);

        InfoListBean bean1= new InfoListBean();
        bean1.setInfo("分期免息");
        bean1.setColor("#ff0000");
        bean1.setBoldFlag(0);

        infoList.add(bean0);
        infoList.add(bean1);
        productListBean.setInfoList(infoList);
        return productListBean;
    }

	udeskViewMode.getRobotApiData().onShowProductClick(createReplyProduct());

### 9 发送商品消息 具体可参考demo  
	  
	  **Product字段属性说明**
	  
| key           | 是否必选   | 说明         |
| ------------- | ------ | ---------- |
| **name** | **必选** | **商品名称** |
| url     | 可选     | 商品跳转链接(新页显示)，如果值为空，则不能点击      |
| imgUrl         | 可选     | 商品显示图片的url       |
| params   | 可选     | 参数列表       |
| customParameters   | 可选     | 参数列表  自定义添加内容      |
|ParamsBean.text | 可选| 参数文本|
|ParamsBean.color | 可选| 参数颜色值，规定为十六进制值的颜色| 
|ParamsBean.fold | 可选| 是否粗体| 
|ParamsBean.breakX | 可选| 是否换行| 
|ParamsBean.size | 可选| 字体大小| 
	  
用法场景:  
1 可以每次进入会话, 通过UdeskConfig配置,设置一条商品消息

2 可以通过导航栏 自定义功能按钮  发送商品消息 

### 10 自定义机器人满意度评价内容（人工客服满意度调查内容是在后台配置）
	  case UdeskConst.LiveDataType.RobotSessionHasSurvey:
                            if ((boolean) mergeMode.getData()) {
                                UdeskUtils.showToast(getApplicationContext(), getResources()
                                        .getString(R.string.udesk_has_survey));
                            } else {
                                SurveyOptionsModel surveyOptionsModel1 = UdeskUtil.buildSurveyOptionsModel(getApplicationContext());
                                toLaunchSurveyView(surveyOptionsModel1);
                            }
                            break;

	  public static SurveyOptionsModel buildSurveyOptionsModel(Context context){
        SurveyOptionsModel model=new SurveyOptionsModel();
        model.setEnabled(true);
        model.setName(context.getResources().getString(R.string.udesk_satisfy_evaluation));
        model.setTitle(context.getResources().getString(R.string.udesk_satisfy_evaluation_title));
        model.setRemark_enabled(true);
        model.setRemark(context.getResources().getString(R.string.udesk_satisfy_evaluation_remark));
        model.setType("text");
        model.setDefault_option_id(0);
        model.setRobot(true);
        List<OptionsModel> options=new ArrayList<>();
        int id=0;
        options.add(new OptionsModel(++id,true,context.getResources().getString(R.string.udesk_statify),context.getResources().getString(R.string.udesk_statify),UdeskConst.REMARK_OPTION_HIDE));
        options.add(new OptionsModel(++id,true,context.getResources().getString(R.string.udesk_common),context.getResources().getString(R.string.udesk_common),UdeskConst.REMARK_OPTION_OPTIONAL));
        options.add(new OptionsModel(++id,true,context.getResources().getString(R.string.udesk_unstatify),context.getResources().getString(R.string.udesk_unstatify),UdeskConst.REMARK_OPTION_REQUIRED));
        model.setOptions(options);
        return model;
    }

### 11 设置是否显示用户头像

	UdeskConfig.Builder builder = new UdeskConfig.Builder();
	builder.isShowCustomerHead(true|false);
	
### 12 设置是否显示用户昵称

	UdeskConfig.Builder builder = new UdeskConfig.Builder();
	builder.isShowCustomerNickname(true|false);


<h1 id="5">五、Udesk API说明</h1>

### 1.获取未读消息

在退出对话界面后，没有断开与Udesk服务器的连接，注册获取未读消息事件方法，之后在该方法中可以收到未读消息。

		 /**
         * 注册和处理接收未读消息提醒事件
         */
        UdeskSDKManager.getInstance().setNewMessage(new IUdeskNewMessage() {
            @Override
            public void onNewMessage(MsgNotice msgNotice) {
                if (msgNotice != null) {
                    Log.i("xxx","UdeskCaseActivity 中收到msgNotice");
                    NotificationUtils.getInstance().notifyMsg(getApplicationContext(), msgNotice.getContent());
                }
            }
        });

接收未读消息

	 if (UdeskBaseInfo.isNeedMsgNotice && UdeskSDKManager.getInstance().getNewMessage() != null) {
	   MsgNotice msgNotice = new MsgNotice(msgId, type, content);
	   UdeskSDKManager.getInstance().getNewMessage().onNewMessage(msgNotice);
	   }
获取未读消息

	//获取未读消息
    List<MessageInfo> unReadMsgs = UdeskSDKManager.getInstance().getUnReadMessages(getApplicationContext(), PreferenceHelper.readString(getApplicationContext(), "init_base_name", "sdktoken"));

### 2 获取未读消息数

在退出对话界面后，没有断开与Udesk服务器的连接，可获得这个会话的未读消息数，打开对话界面后未读消息数会清空。

	UdeskSDKManager.getInstance().getCurrentConnectUnReadMsgCount();

### 3 控制台日志开关

如果开发中，想在控制台看当前客户与Udesk服务器连接（xmpp)的交互报文，调用如下接口可实现

	//true 表示开启控制台日志  false表示关闭控制台日志
	UdeskSDKManager.getInstance().isShowLog(true);
### 4 断开与Udesk服务器连接

  App运行时如果需要客服离线或不再接收客服消息，调此接口可以主动断开与Udesk服务器的的连接。

	UdeskSDKManager.getInstance().disConnectXmpp();

### 5 设置退出排队的模式

 quitmode: mark (默认,标记放弃)/  force_quit(强制立即放弃)

	build.setUdeskQuenuMode(quitmode);

### 6 发送商品订单

		  **OrderBean字段属性说明**
	  
| key                | 是否必选   | 说明         |
| -------------      | ------    | ---------- |
| **name**           | **必选**   | 订单名称 |
| url                | 可选       | 订单跳转链接      |
| **order_no**       | **必选**   | 订单编号       |
| **price**          | **必选**   | 订单价格       |
| **order_at**       | **必选**   | 下单时间      |
| pay_at              | 可选       | 付款时间|
| **status**          | **必选**   | 订单状态(待付款: 'wait_pay'、已付款: 'paid'、已关闭: 'closed')| 
| remark              | 可选       | 备注| 

	 /**
     * 发送商品订单
     */
    private void sendCustomerOrder() {
        //发送订单信息
        OrderBean orderBean =new OrderBean();
        orderBean.setName("Apple iPhone X (A1903) 64GB");
        orderBean.setOrder_at(UdeskUtil.getCurrentDate());
        orderBean.setUrl("www.baidu.com");
        orderBean.setPrice(1200.33);
        orderBean.setOrder_no("123");
        orderBean.setPay_at(UdeskUtil.getCurrentDate());
        orderBean.setStatus(UdeskConst.OrderStatus.paid);
        orderBean.setRemark("我是测试的");
        String sdkToken = getSDKToken();
        UdeskSDKManager.getInstance().sendCustomerOrder(UdeskSDKManager.getInstance().getDomain(this),UdeskSDKManager.getInstance().getAppkey(this),
                sdkToken,UdeskSDKManager.getInstance().getAppId(this),JsonUtils.getOrderJson(orderBean));
    }

### 7 发送商品轨迹


		  **TraceBean字段属性说明**
	  
| key                | 是否必选   | 说明         |
| -------------      | ------    | ---------- |
| **type**           | **必选**   | 跟踪类型 |
| **data.name**      | **必选**   | 商品名称      |
| data.url           | 可选       | 商品跳转链接       |
| data.imgUrl        | 可选       | 图片url       |
| data.date          | 可选       | 访问时间      |
| data.params        | 可选       | 参数列表|
| data.params.text   | 可选       | 参数文本| 
| data.params.color  | 可选       | 参数颜色值| 
| data.params.fold   | 可选       | 是否粗体| 
| data.params.breakX | 可选       | 是否换行| 
| data.params.size   | 可选       | 字体大小| 
	
	    /**
     * 发送商品轨迹
     */
    private void sendTrace() {
        //发送商品轨迹
        TraceBean traceBean =new TraceBean();
        traceBean.setType("product");
        TraceBean.DataBean dataBean =new TraceBean.DataBean();
        dataBean.setName("traceBean");
        dataBean.setUrl("http://item.jd.com/6748052.html");
        dataBean.setDate(UdeskUtil.getCurrentDate());
        dataBean.setImgUrl("http://img12.360buyimg.com/n1/s450x450_jfs/t10675/253/1344769770/66891/92d54ca4/59df2e7fN86c99a27.jpg");
        List<TraceBean.DataBean.ParamsBean> paramsBeanList =new ArrayList<>();

        TraceBean.DataBean.ParamsBean paramsBean1 = new TraceBean.DataBean.ParamsBean();
        paramsBean1.setBreakX(false);
        paramsBean1.setColor("#ff0000");
        paramsBean1.setFold(false);
        paramsBean1.setSize("14");
        paramsBean1.setText("999999999.00");

        paramsBeanList.add(paramsBean1);
        dataBean.setParams(paramsBeanList);

        traceBean.setData(dataBean);
        String sdkToken = getSDKToken();
        UdeskSDKManager.getInstance().sendBehaviorTraces(UdeskSDKManager.getInstance().getDomain(this),UdeskSDKManager.getInstance().getAppkey(this),
                sdkToken,UdeskSDKManager.getInstance().getAppId(this),JsonUtils.getTraceJson(traceBean));
    }

### 8 资源 UI

	聊天界面UdeskChatActivity 
	
	机器人 UdeskRobotFragment
	
	人工客服 UdeskAgentFragment
	
	消息适配 MessageAdatper
	
	左侧布局 udesk_item_left.xml 里面包含多种消息类型的布局

	左侧viewHolder LeftViewHolder  处理左侧消息
	
	右侧布局 udesk_item_right.xml 里面包含多种数据类型的布局

	右侧viewHolder RightViewHolder  处理右侧消息

	xmpp消息处理 UdeskXmppManager
	
	数据处理 
		UdeskViewMode 
		APILiveData 人工api 处理 
		DBLiveData  数据库处理
		FileLiveData  文件上传下载处理
		ReceiveLiveData receive消息处理
		RobotApiData 机器人api处理
		SendMessageLiveData  发送消息处理
		
		MergeMode livedata 处理的消息
		questionMergeMode MergeMode 子类拓展 处理点击问题
		MergeModeManager  mergedata 管理类


### 9 清除缓存的agentId

	UdeskSDKManager.getInstance().cleanCacheAgentId(getApplicationContext());

### 10 清除缓存的groupId

	UdeskSDKManager.getInstance().cleanCacheGroupId(getApplicationContext());

### 11 清除缓存的menuId
	
	UdeskSDKManager.getInstance().cleanCacheMenuId(getApplicationContext());

<h1 id="6">六、消息推送</h1>

当前仅支持一种推送方案，即Udesk务端发送消息至开发者的服务端，开发者再推送消息到 App。
### 1 设置接收推送的服务器地址
    推送消息将会发送至开发者的服务器。
	
	设置服务器地址，请使用Udesk管理员帐号登录 Udesk，在 设置 -> 移动SDK 中设置。
![udesk](http://7xr0de.com1.z0.glb.clouddn.com/5D761252-3D9D-467C-93C9-8189D0B22424.png)	
### 2 使用Udesk 推送功能的配置

	配置 是否使用推送服务  true 表示使用  false表示不使用
    public  static  boolean isUserSDkPush = false;

### 3 设置用户的设备唯一标识
	UdeskSDKManager.getInstance().setRegisterId（context,"xxxxregisterId"）
     //保存注册推送的的设备ID
    public void setRegisterId(Context context, String registerId) {
        UdeskConfig.registerId = registerId;
        PreferenceHelper.write(context, UdeskConst.SharePreParams.RegisterIdName,
                UdeskConst.SharePreParams.Udesk_Push_RegisterId, registerId);
    }
关闭和开启Udesk推送服务，Udesk推送给开发者服务端的消息数据格式中，会有 device_token 的字段。
### 4	关闭开启Udek推送服务
	/**
     * @param domain    公司注册生成的域名
     * @param key        创建app时，生成的app key
     * @param sdkToken   用户唯一标识
	 * @param status         sdk推送状态 ["on" | "off"]  on表示开启Udesk推送服务， off表示关闭udesk推送服务
     * @param registrationID 注册推送设备的ID
     * @param appid  创建app时，生成的app id 
     */

    public void setSdkPushStatus(String domain, String key, String sdkToken, String status, String registrationID, String appid, UdeskCallBack callBack) {
        try {
            UdeskHttpFacade.getInstance().sdkPushStatus(domain, key, sdkToken, status, registrationID, appid, callBack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
### 5 Udek推送给开发者服务端的接口说明	
**基本要求**

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


<h1 id="7">七、常见问题</h1>

   1. 指定客服组或者客服分配出现与指定客服组客服不一致的情况？
   
     先要确认客服没有关闭会话。
     我们产品逻辑： 假设客户A   选了客服组B下的客服B1，进行会话。  之后客户A退出会话界面，进入另外界面，之后通过客服组C下的客服C 1分配会话：  这时      后台会判断，如果和B1会话还存在，则会直接分配给B1，而不会分配給客服C 1。  只有B1会话关闭了，才会分配給客服C1。 
     
   2.出现在不同客户分配的会话在一个会话中?
   
      出现这种情况，是客服传的sdktoken值一样。 sdktoken像身份证一样，是用户唯一的标识。让客户检查接入是传入的sdktoken值。
      如果设置了email 或者 cellphone  出现相同也会在一个客服的会话里。
   
   3.某个手机打不开机器人页面？
   
     这个问题的可能情况之一： 手机时间设置和当前时间不一致造成的。时间误差超过一小时，必然会出现链接不上机器人界面。

     5.x已采用原生机器人聊天 不存在这个问题
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
	
   6.有些消息显示不全

	由于5.1.0 数据库升级，添加一些新的字段，之前版本的数据库没有。覆盖安装时读取本地数据库历史消息，消息有些字段没有读取到，造成显示不全。 卸载重装一下就可以了。
	
<h1 id="8">八、更新记录</h1>

### 更新日志 ###

### 5.1.4 版本更新（5.x_android_Q 分支）：

1. 适配android Q
2. 修改接收满意度问题
3. 修改视频通话问题
4. 修改底部显示问题

### 5.1.3 版本更新： ###

1. 对话留言添加参数
2. 修改更多和表情显示隐藏问题
3. 修改取消文件上传问题
4. 修改不显示欢迎语问题
5. 修改html标签解析
6. 修改视频通话问题
7. 修改查询gif问题

### 5.1.2 版本更新： ###

1. 修改发送商品链接引发留言问题

### 5.1.1 版本更新： ###

1. 修改emoji文件夹名称
2. 修改webView上下文
3. 修改留言问题
4. 修改满意度评价问题
5. 修改键盘高度问题
6. 修改相册问题
7. 修改拍摄问题
8. 修改七牛下载问题

### 5.1.0 版本更新： ###

1. 支持对话留言
2. 支持模板消息
3. 支持发送商品订单和商品轨迹
4. 支持客服端添加商品消息类型
5. 修改数据存储逻辑和数据库
6. 修改机器人欢迎语逻辑
7. 修改阿里上传策略问题
8. webview适配非http/https 开头的链接
9. 修改人工导航点击问题
10. 修改无消息对话过滤状态发送消息问题
11. 添加和修改商品消息背景，字体颜色，行数设置api
12. 修改4.x版本覆盖安装5.x 请求客服问题
13. 修改文件上传进度显示策略

### 5.0.0版本更新: ###

1.支持原生机器人

2.支持三方会话

3.UI交互改版

4.采用AAC框架 

5.添加语音识别功能
### 4.1.1 修复内容
1. 修复sdk排队中点击留言还在排队中；
2. 修复排队发送文本消息后，更多得按钮隐藏了；
3. 修复发送商品消息在客服端没显示；
4. 优化消息的发送到达；（离开会话界面，有未收到回执的消息，会放入单例中发送）

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


<h1 id="9">九、部分功能截图</h1>

### 1.原生机器人 ###
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/1_1571025112_783.png?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
##### 后台配置 #####
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/2_1571025171_804.png?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
### 2.输入联想 ###
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/3_1571025223_508.jpg?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
### 3.语音识别 ###
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/4_1571025270_523.jpg?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
### 4.三方会话 ###
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/5_1571025306_362.png?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
### 5.商品链接 自定义导航及服务评价 自定义表情 ###
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/6_1571025346_475.png?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
### 6.自定义按钮 ###
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/7_1571025383_939.png?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
### 7.转人工导航设置 ###
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/8_1571025424_407.jpg?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
##### 后台配置 #####
![后台配置](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/9_1571025469_867.jpg?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
### 8.表单留言 ###
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/10_1571025521_212.jpg?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
### 9.直接留言 ###
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/11_1571025556_230.jpg?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
##### 后台配置 #####
![后台配置](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/12_1571031537_681.jpg?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
### 10.满意度评价 ###
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/13_1571031586_139.jpg?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
### 11.无消息对话过滤 ###
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/14_1571031624_463.jpg?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
##### 后台配置 #####
![后台配置](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/15_1571031682_360.jpg?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
### 12.消息类型展示 ###
##### 商品消息 地图 文件 小视频类型 #####
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/16_1571031722_733.png?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
##### 富文本 图文带推荐消息 #####
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/17_1571031770_240.png?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
##### 纯文本 流程消息 #####
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/18_1571031810_174.png?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
##### 商品选择 商品回复 #####
![udesk](http://qn-im.udesk.cn/%E5%9B%BE%E7%89%87_1554875805_806.png?imageMogr2/auto-orient/)
##### 问题带推荐 推荐带分类 #####
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/19_1571031855_373.png?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
##### 图片 链接 语音消息 转人工提示 答案评价 #####
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/20_1571031994_662.png?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
##### 转接 结构化消息 #####
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/21_1571032049_234.png?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
##### 转人工 留言事件 #####
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/22_1571032094_261.png?x-oss-process=image/auto-orient,1/resize,h_300,w_300)
##### 视频直播 #####
![udesk](http://pro-cs-freq.oss-cn-hangzhou.aliyuncs.com/doc/im/image_1554878719_504.png?x-oss-process=image/auto-orient,1/)