# UdeskSDK-Android
UdeskSDK-Android


##Android新版说明
> Udesk为了让开发者更好的集成移动SDK,与企业业务结合更加紧密，我们开源了SDK的UI界面。用户可以根据自身业务以及APP的不同风格重写页面。当然开发者也可以用我们提供的默认的界面。

![alt text](indeximg/android-new-2.png)
##一、接口详细说明
> Udesk-SDK方法都封装在UdeskSDKManager类中,采用单例。通过UdeskSDKManager.getInstance().对应的方法调用。

> **特别注意：第一步必需是调用initApiKey,第二步是必需调用setUserInfo 。 其它的api的使用都必需在第一步和第二步之后,根据集成时的需求调用，否则会

出错误。 如果需要在同一个域名和共享密钥下切换用户，则可跳过第一步，直接调用第二步setUserInfo()。**

## 1)初始化SDK (必须调用)
###初始化SDK，传入Udesk专属域名和共享的密钥。




