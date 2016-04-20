## TalkingData App Analytics Hybrid 混合模式 SDK 集成文档
### 综述

1. 适用范围  
	TalkingData App Analytics 为移动应用提供数据统计分析服务，通过在应用中加入数据SDK，在 [https://www.talkingdata.com](https://www.talkingdata.com) 网站中查阅应用的相关数据。  
	目前，TalkingData App Analytics Hybird 混合模式 SDK 支持开发运行于 Android 和 iOS 两个平台 Native + WebView 混合模式的游戏/应用。

2. 统计标准  
	数据系统中的基本数据单元依据以下标准定义：
	- 新增用户  
		TalkingData 数据系统中的“用户”指用户的一台唯一设备。
	- 用户的一次使用  
		用户从打开应用的界面至离开界面的完整过程。如用户在离开界面后30秒内重新回到应用中，将被认为是上次使用被打扰后的延续，记为一次完整使用。
	- 自定义事件  
		指用户在应用中进行了特定的操作或达成了特定的条件。例如：用户点击了广告栏、用户进行付费等。自定义事件用于收集任意您期望跟踪的数据。

### 接入数据系统

1. 申请 APP ID  
	访问 [https://www.talkingdata.com](https://www.talkingdata.com) 网站并注册免费用户，创建一款应用，您将获得一串32位的16进制App ID，用于唯一标识您的一款应用。TalkingData 支持跨平台的应用，跨平台应用只需获得一个App ID 即可。

2. iOS 平台向工程中导入追踪SDK  
	- 添加插件  
		a). 将 TalkingData 文件夹中的 `TalkingDataHTML.h`、`TalkingDataHTML.m`、`TalkingData.h` 和 `libTalkingData.a` 添加到 iOS 工程中。  
		b). 将 TalkingData 文件夹中的 `TalkingData.js` 复制到混合模式的项目中。

	- 配置插件  
	在 iOS 工程使用 `UIWebView` 控件的 `xxxxViewController.m` 文件中引入 `TalkingDataHTML.h` 头文件，在`UIWebViewDelegate` 的方法中加入以下代码：

			#import "TalkingDataHTML.h"
			- (BOOL)webView:(UIWebView *)webView
			shouldStartLoadWithRequest:(NSURLRequest *)request
			navigationType:(UIWebViewNavigationType)navigationType {
			NSURL * url = [request URL];
			if ([TalkingDataHTML execute:url webView:webView]) {
				return NO;
			}
				return YES;
			}

		如果 js 触发的是 TalkingData 的事件，`execute` 方法会返回 `YES`，否则返回 `NO`。

	- 工程配置  
	在工程中添加 TalkingData App Analytics SDK 使用到的库  
	`AdSupport.framework` 获取 advertisingIdentifier  
	`CFNetwork.framework` 使用网络连接  
	`CoreTelephony.framework` 框架获取运营商标识  
	`CoreMotion.framework` 支持摇一摇功能  
	`Security.framework` 来辅助存储设备标识  
	`SystemConfiguraton.framework` 检测网络状况  
	`libz.tbd`(xcode7以下：`libz.dylib`） 进行数据压缩


3. Android 平台向工程中导入追踪SDK
	- 将 `TalkingDataHTML.java` 文件添加到项目中，修改其包名为项目包名。
	- 添加自定义的 WebViewClient 类型，实现参考下面的代码：

			class MyWebviewClient extends WebViewClient{
			    @Override 
			    public void onPageFinished(WebView view, String url)
			    { 
			        view.loadUrl("javascript:setWebViewFlag()"); 
			        if(url != null && url.endsWith("/index.html")){
			            TCAgent.onPageStart(MainActivity.this, "index.html");
			        }
			    } 
			
			    @Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url) {
			        try {
			            TalkingDataHTML.GetInstance().execute(MainActivity.this, url, view);
			        } catch (Exception e) {
			            e.printStackTrace();
			        }
			        return true;
			    }
			}

		主要是在 `WebView` 跳转前，对 URL 进行处理，只有 `talkingdata` 开始的字符串会被处理，其他的正常 URL 需要按照正常流程处理。
	- 使用 WebView 的方法 `setWebViewClient` 为 `WebView` 实例设置 `WebClient`：

			webView.setWebViewClient(new MyWebviewClient());

	- 设置 `setWebViewFlag` 这个参数的存在相当于我们 SDK 的 javascript 和 native 代码之间通信的 bridge 的开关。因为这个 bridge 需要一个自定义的 `WebVeiwClient`，并且 Override 其中的 `shouldOverrideUrlLoading` 方法来处理。如果 H5 端集成了 `TalkingData.js`, 但是 native 端还没有来得及完成集成或者集成不正确，没有这个开关就可能导致 `WebView` 在页面跳转的时候发生异常。
	- 由于我们从 javascript 向 native 传递数据时我们构造的 url 里面有可能含有中文字符的 json 格式字符串，会被进行 urlencode，请提醒客户在调用下面的方法前进行 urldecode：

			TalkingDataHTML.GetInstance().execute(MainActivity.this, url, view);

	- 集成测试完毕后，进行 release 打包的时候，请在 proguard 配置文件中加入下面的规则：

			-keep public class com.tendcloud.tenddata.** { public protected *;}
			-keep public class com.tendcloud.hybriddemo.TalkingDataHTML { *;}
			-keepclassmembers class ** {
			    @com.tendcloud.tenddata.OttoProduce public *;
			    @com.tendcloud.tenddata.OttoSubscribe public *;
			}
		__注意__：上面的 `com.tendcloud.hybriddemo` 需要替换为项目中实际存放 `TalkingDataHTML` 类文件的包名。

4. 初始化集成（重要）
	- 跟踪游戏/应用的启动和关闭：用于准确跟踪玩家的游戏/应用的启动次数、使用时长等信息  
	在游戏/应用启动后，调用 `TalkingData.sessionStarted`，该接口完成统计模块的初始化和统计 Session 的创建，所以越早调用越好。也可以在 `Objective-C` 层的 `AppDelegate` 中对 SDK 初始化。
	- 添加渠道信息：可以选择性的在 `channelId` 中填入推广渠道的名称，数据报表中则可单独查询到他们的数据。每台设备仅记录首次安装激活的渠道，更替渠道包安装不会重复计量。不同的渠道ID包请重新编译打包。  
	- appKey: string 类型，填写创建游戏/应用时获得的 AppID，用于唯一识别您的游戏/应用。
	- channelId： string 类型，用来标注游戏/应用推广渠道，区分玩家的来源来查看统计。格式：32 个字符内，支持中文、英文、数字、空格、英文点和下划线，请勿使用其他符号。

	iOS示例代码

			#import "TalkingData.h"
			- (BOOL)application:(UIApplication *)application
			didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
			{
			[TalkingData sessionStarted:@"A1B2C3E4D5F7C0"
			withChannelId:@"BAIDU"];
			......
			}

### 接口说明：（Javascript 文件中的 TalkingData 对象）

在 `index.html` 中引入 `TalkingData.js` 文件。下面以 iOS 平台为例说明接口调用方法。Android 平台 HTML 部分的集成和 iOS 相同，Java 端集成方式请参考： [TalkingData App Analytics SDK on Android 在线集成文档](https://www.talkingdata.com/app/document_web/index.jsp?statistics) 

1. 页面统计  
	统计页面的点击次数和停留时间，需要在页面打开和关闭的时候添加对应的API 调用，如下所示：

		//在进入页面时调用
		TalkingData.onPageBegin("page_name");
		//在离开页面时调用
		TalkingData.onPageEnd("page_name");

	注：page_name 是自定义的页面名称，注意不要加空格或其他的转义字符。

2. 使用 DEVICE ID  
	如要使用 TalkingData 提供的 `DeviceId`，请在调用 `getDeviceId` 接口时传入回调函数。在获取到 `DeviceId` 时会调用该函数，并将 `DeviceId` 以参数的形式传给该函数。

		function callBack (deviceId) {
		// 在此处可以使用deviceId
		}
		TalkingData.getDeviceId(callBack);

3. 位置信息统计  
	对于iOS 平台，TalkingData 默认不统计用户的位置信息，只提供了记录接口，请根据苹果公司的审核原则合理使用用户的位置信息，如有需要，可以将已获取的位置信息提交到TalkingData 统计服务器，服务器只保存最近一次提交的位置信息。如下所示：

		TalkingData.setLocation(纬度, 经度);

4. 使用自定义事件  
	自定义事件用于统计任何您期望去跟踪的数据，如：点击某功能按钮、填写某个输入框、触发了某个广告等；同时，自定义事件还支持添加一些描述性的属性参数，使用多对Key-Value 的方式来进行发送（非必须使用），用来对事件发生时的状况做详尽分析。
	- Event ID 无需提前在数据平台中定义，可自行定义名称，直接加入到应用中需要跟踪的位置即可生效。
	- 格式：32 个字符以内的中文、英文、数字、下划线，注意eventId 中不要加空格或其他的转义字符。
	- TalkingData 最多支持100 个不同的Event ID。

	如果您要跟踪更多的事件，我们提供了Label 参数的用法，可以给多个要跟踪的同类型或类似的事件使用相同的 Event ID，通过给他们分配不同 Label 来达到区分跟踪多个事件的目的。这可理解为 Event ID 成为了多个事件的目录，EventID+Label 形成了一个具体事件。请对事件做好分类，这也对您管理和查阅事件数据有利。  
	调用方法:

	- 在应用程序要跟踪的事件处加入下面格式的代码，也就成功的添加了一个简单事件到您的应用程序中了：

			TalkingData.onEvent("Event_ID");

	- 跟踪多个同类型事件，无需定义多个 Event ID，可以使用 Event ID 做为目录名，而使用Label 标签来区分这些事件，可按照下面格式添加代码：

			TalkingData.onEventWithLabel("Event_ID", "Event_Label");

	- 为事件添加详尽的描述信息，可以更有效的对事件触发的条件和场景做分析，可按照下面格式添加代码：

			TalkingData.onEventWithParameters("Event_ID","Event_Label",EventParameters);

		注: 此 `EventParameters` 的 Value 仅支持字符串（string）和数字（number）类型，每一次事件数据支持 `10` 对不同参数传入。在 value 使用 string 格式时，报表中将给出事件发生时每种 value 出现的频次；value 为 number 时，报表将帮助计算value 值的总计值和平均数。
	- 示例1：  
		使用自定义事件跟踪玩家的死亡情况，并记录死亡时的等级、场景、关卡、原因等信息，可在玩家死亡时调用：

			// 可定义eventId=dead
			var dic = {
			level:"50-60", //级别区间，注意是字符串哟！
			map:"沼泽地阿卡村", //地图场景
			mission:"屠龙副本", //关卡
			reason:"PK 致死", //死亡原因
			coin:"10000～20000" //携带金币数量
			}

			TalkingData.onEventWithParameters("dead", "", dic);

		注：在某key 的value 取值较离散情况下，不要直接填充具体数值，而应划分区间后传入，否则value 不同取值很可能超过平台最大数目限制，而影响最终展示数据的效果，比如：示例中金币数可能很离散，请先划分合适的区间。
	- 示例2：  
		使用自定义事件跟踪玩家在注册过程中每个步骤的失败情况：

			var dic = {
			step:"1" // 在注册环节的每一步完成时，以步骤名作为value 传送数据
			}
			TalkingData.onEventWithParameters("reg", "", dic);

