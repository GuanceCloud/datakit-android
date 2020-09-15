# Dataflux SDK Android

**Demo**

地址：[https://github.com/CloudCare/dataflux-sdk-android-demo](https://github.com/CloudCare/dataflux-sdk-android-demo)

**Agent**

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmvnrepo.jiagouyun.com%2Frepository%2Fmaven-releases%2Fcom%2Fcloudcare%2Fft%2Fmobile%2Fsdk%2Ftracker%2Fagent%2Fft-sdk%2Fmaven-metadata.xml)](https://mvnrepo.jiagouyun.com/repository/maven-releases/com/cloudcare/ft/mobile/sdk/tracker/agent/ft-sdk/maven-metadata.xml)

**Plugin**

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmvnrepo.jiagouyun.com%2Frepository%2Fmaven-releases%2Fcom%2Fcloudcare%2Fft%2Fmobile%2Fsdk%2Ftracker%2Fplugin%2Fft-plugin%2Fmaven-metadata.xml)](https://mvnrepo.jiagouyun.com/repository/maven-releases/com/cloudcare/ft/mobile/sdk/tracker/plugin/ft-plugin/maven-metadata.xml)

## 安装
### 1. DataFlux SDK 支持 Android 的最低版本为 23（即 Android 6.0）
### 2. 在项目的根目录的 build.gradle 文件中添加 DataFlux SDK 的远程仓库地址
``` groovy
buildscript {
    //...省略部分代码
    repositories {
        //...省略部分代码
        //添加 DataFlux SDK 的远程仓库地址
        maven {
            url 'https://mvnrepo.jiagouyun.com/repository/maven-releases'
        }
    }
    dependencies {
        //...省略部分代码
        //添加 DataFlux Plugin 的插件依赖
        classpath 'com.cloudcare.ft.mobile.sdk.tracker.plugin:ft-plugin:[ latest version ]'
    }
}
allprojects {
    repositories {
        //...省略部分代码
        //添加 DataFlux SDK 的远程仓库地址
        maven {
            url 'https://mvnrepo.jiagouyun.com/repository/maven-releases'
        }
    }
}
```
### 3. 在项目主模块( app 模块)的 build.gradle 文件中添加 DataFlux SDK 的依赖及 DataFlux Plugin 的使用 和 Java 8 的支持

``` groovy
dependencies {
    //添加 DataFlux SDK 的依赖
    implementation 'com.cloudcare.ft.mobile.sdk.tracker.agent:ft-sdk:$last_version'
}
//应用插件
apply plugin: 'ft-plugin'
//配置插件使用参数
FTExt {
    //是否显示日志
    showLog = true
}
android{
	//...省略部分代码
    compileOptions {
        sourceCompatibility = 1.8
        targetCompatibility = 1.8
    }
}
```

>最新的版本请看上方的 Agent 和 Plugin 的版本名

### 4. 添加混淆

如果你的项目开启了混淆，那么在你的 proguard-rules.pro 文件中添加如下配置

```
-keep class * extends com.ft.sdk.garble.http.ResponseData{ *;}
-keep class com.ft.sdk.FTAutoTrack{*;}
-keep enum com.ft.sdk.FTAutoTrackType{*;}
-keep enum com.ft.sdk.FTSdk{*;}
-keep class com.ft.sdk.garble.utils.TrackLog{*;}
```
> 注意：如果你的项目中开启了全埋点和流程图，那么需要将你的 Fragment 和 Activity 保持不被混淆，这样流程图中
> 就会显示页面的真实名称，而不是混淆后的名称

## 配置

### 一、 DataFlux SDK 功能配置项说明

#### 1. DataFlux SDK 包含的功能说明

|           方法名           |           含义            | 是否必须 |                                           注意                                           |
|:-----------------------:|:-----------------------:|:----:|:--------------------------------------------------------------------------------------:|
|       setUseOAID        | 是否使用OAID作为设备唯一识别号的替代字段  |  否   |                 默认不使用,开启后全埋点数据里将会添加一个 oaid 字段<br>[了解 OAID](#一关于-oaid)                  |
|     setXDataKitUUID     |       设置数据采集端的名称        |  否   |                                  不设置该值系统会生成一个默认的 uuid                                  |
|        setDebug         |        是否开启调试模式         |  否   |                                 默认不开启，开启后方可打印 SDK 运行日志                                 |
|     setMonitorType      |          设置监控项          |  否   | 默认不开启任何监控项,<br>[关于监控项说明](#四监控配置项类-monitortype),<br>[关于监控项参数获取问题](#二关于监控项中有些参数获取不到问题说明) |
|     setNeedBindUser     |       是否开启绑定用户数据        |  否   |                  默认不开启,<br>开启后必须要绑定用户数据[如何绑定用户数据](#一初始化类-ftsdk-提供的方法)                  |
|     enableAutoTrack     |        是否使用自动埋点         |  否   |                                    不开启将不会上报流程图和埋点事件                                    |
| setEnableAutoTrackType  |         设置事件白名单         |  否   |                          开启自动埋点后，不设置该值表示接受所有事件类型。埋点事件类型见表下说明                           |
| setDisableAutoTrackType |         设置事件黑名单         |  否   |                                开启自动埋点后，不设置该值表示不设置事件黑名单                                 |
| setWhiteActivityClasses |          页面白名单          |  否   |                                  包括 Activity、Fragment                                  |
|   setWhiteViewClasses   |          控件白名单          |  否   |                                         包括基本控件                                         |
| setBlackActivityClasses |          页面黑名单          |  否   |                                  包括 Activity、Fragment                                  |
|   setBlackViewClasses   |          控件黑名单          |  否   |                                         包括基本控件                                         |
|       metricsUrl        | FT-GateWay metrics 写入地址 |  是   |                                      必须配置，配置后才能上报                                      |
|setDataWayToken|上传数据需要的身份认证|否|SaaS 版本需要传入该参数，PaaS 版本不需要传入|
|  enableRequestSigning   |      配置是否需要进行请求签名       |  否   |                                         默认不开启                                          |
|          akId           |      access key ID      |  否   |                           enableRequestSigning 为 true 时，必须要填                           |
|        akSecret         |    access key Secret    |  否   |                           enableRequestSigning 为 true 时，必须要填                           |
|        setGeoKey        |   设置是否使用高德作为地址解析器和key   |  否   |      如何申请高德的 key？[点我快速了解](https://lbs.amap.com/api/webservice/guide/api/georegeo)      |
|       trackNetRequestTime       |     设置是否开启网络请求时长的监控      |  否   |                           [点我快速了解如何监控网络请求时长](#三如何监控网络请求的相关时长)                            |
|  setPageVtpDescEnabled  |    设置页面和视图树是否使用描述显示     |  否   |                                       默认使用类名和视图树                                       |
|       addPageDesc       |        设置页面描述配置         |  否   |             Map 数据集，开启本地的描述日志显示，获取页面类名作为 Key，然后添加描述性文字作为 value 去创建 Map 数据集             |
|       addVtpDesc        |        设置视图树描述配置        |  否   |             Map 数据集，开启本地的描述日志显示，获取视图树作为 Key，然后添加描述性文字作为 value 去创建 Map 数据集              |
|       setDescLog        |    是否开启本地视图树和类名日志显示     |  否   |                           不开启将不会显示视图树和类名日志，该方法独立于  setDebug                            |
| setEnableTrackAppCrash | 是否开启 App 崩溃日志上报功能 | 否 | 默认不开启，开启后将上报当前应用的崩溃日志。上报成功后，可以在后台的日志模块查看对应的日志。<br /> [关于崩溃日志中混淆内容转换的问题](#五关于崩溃日志中混淆内容转换的问题)|
| setTraceServiceName | 设置崩溃日志的名称 | 否 | 默认为 dataflux sdk。你可以将你的应用名称设置给该字段，用来区分不同的日志 |
| setEnv | 设置崩溃日志中显示的应用的环境 | 否 | 默认情况下会获取应用当前的环境。如：debug、release |
| setCollectRate | 设置采集率 | 否 | 采集率的值范围为>=0、<=1，默认值为1。<br />说明：SDK 初始化是会随机生成一个0-1之间的随机数，当这个随机数小于你设置的采集率时，那么会上报当前设备的行为相关的埋点数据，否则就不会上报当前设备的行为埋点数据<br /> |
| setTraceConsoleLog | 是否开启本地打印日志上报功能 | 否 | 当开启后会将应用中打印的日志上报到后台，日志等级对应关系<br />Log.v->ok;Log.i、Log.d->info;Log.e->error;Log.w->warning |
| setNetworkTrace | 是否开启网络追踪功能| 否 | 开启后，可以在 web 中“日志”查看到对应日志的同时也可以在“链路追踪”中查找到对应的链路信息 |
| setTraceType | 设置链路追踪所使用的类型。 | 否 |目前支持 Zipkin 和 Jaeger 两种，默认为 Zipkin |
| setEventFlowLog | 设置是否开启页面事件的日志 | 否 | 可以在 web 版本日志中，查看到对应上报的日志，事件支持启动应用，进入页面，离开页面，事件点击等等 |
| setOnlySupportMainProcess|设置是否只支持在主进程中初始化|否|默认是 true ，默认情况下 SDK 只能在主进程中运行。如果应用中存在多个进程，那么其他进程中将不会执行。如果需要在其他进程中执行需要将该字段设置为 true

> FTAutoTrackType 自动埋点事件说明，事件总类目前支持3种：
    FTAutoTrackType.APP_START：页面的开始事件，Activity 依赖的是其 onResume 方法，Fragment 依赖的是其 onResume 方法；
    FTAutoTrackType.APP_END：页面的结束事件，Activity 依赖的是其 onPause 方法，Fragment 依赖的是其 onPause 方法；
    FTAutoTrackType.APP_CLICK：控件的点击事件。


#### 2. 通过 FTSdk 安装配置项和绑定用户信息

方法说明表

|      方法名       |        含义         | 是否必须 |                  注意                   |
|:--------------:|:-----------------:|:----:|:-------------------------------------:|
|    install     |     安装初始化配置项      |  是   |          在项目 application 中运行          |
|      get       | 获得安装后创建的 FTSdk 对象 |  否   |           应该在 install 运行后调用           |
| unbindUserData |      解绑用户信息       |  否   |  必须在 setNeedBindUser 方法设为 true 后才有效果  |
|  bindUserData  |      绑定用户信息       |  否   |  必须在 setNeedBindUser 方法设为 true 后才有效果  |
|    shutDown    |   关闭SDK中正在执行的操作   |  否   |                                       |
| setGpuRenderer | 设置 GPU 信息获取依赖的视图  |  否   | 当监控项 setMonitorType 设置监控 GPU 后一定调用该方法 |
|startLocation|开启定位获取，并异步回调定位结果|否|该方法为静态方法，可以在SDK初始化前调用|

#### 3. 通过 FTMonitor 开启独立监控数据周期上报

方法说明表

|方法名|含义|是否必须|注意|
|:--:|:--:|:--:|:--:|
|setMonitorType|设置监控类型|否|设置具体值后才会同步相关数据，[详情参考](#四监控配置项类-monitortype)|
|setPeriod|设置周期|否|单位秒，默认为10秒|
|setUseGeoKey|设置是否使用高德作为地址解析器|否|设置高德的 Key 后，如果不开启就不会使用|
|setGeoKey|设置高德解析器的 key|否|如何申请高德的 key？[点我快速了解](https://lbs.amap.com/api/webservice/guide/api/georegeo)|
|start|开启监控同步|是|只有调用该方法后才会执行同步监控|

#### 4. 通过 FTTrack 类实现主动上报埋点、日志、事件、对象数据

具体方法及说明参考 [三、手动埋点类 FTTrack](#三手动埋点类-fttrack)

#### 5. 示例代码

``` kotlin
class DemoAplication : Application() {
    private val accesskey_id = "key_id"
    private val accessKey_secret = "key_secret"
    private val serverUrl = "serverUrl"
    override fun onCreate() {
        super.onCreate()
        val ftSDKConfig = FTSDKConfig.builder(
            serverUrl,//服务器地址
            true,//是否需要签名
            accesskey_id,//access key ID
            accessKey_secret//access key Secret
        ).setUseOAID(true)//是否使用OAID
            .setDebug(true)//是否开启Debug模式（开启后能查看调试数据）
            .setDescLog(true)//开启显示页面和描述日志的显示
            .setXDataKitUUID("ft-dataKit-uuid-001")
            .setNeedBindUser(true)//是否绑定用户信息
            .setMonitorType(MonitorType.ALL)//设置监控项
            .enableAutoTrack(true)//是否开启自动埋点
            .addPageDesc(pageAliasMap())
            .addVtpDesc(eventAliasMap())
            .setPageVtpDescEnabled(true)
            .trackNetRequestTime(true)
            .setEnableTrackAppCrash(true)
            .setEnv("dev")
            .setTraceType(TraceType.ZIPKIN)
            .setCollectRate(0.5f)
            .setEnableAutoTrackType(FTAutoTrackType.APP_START.type or
                    FTAutoTrackType.APP_END.type or
                    FTAutoTrackType.APP_CLICK.type)//自动埋点事件白名单
            .setWhiteActivityClasses(listOf(MainActivity::class.java))//自动埋点页面白名单
            .setWhiteViewClasses(listOf(Button::class.java));//自动埋点控件白名单
        FTSdk.install(ftSDKConfig)
        
        //独立执行周期监控数据上报
        FTMonitor.get()
         .setMonitorType(MonitorType.ALL)
         .setPeriod(10)
         .start()
    }
}

```

``` kotlin
//可以在用户登录成功后调用此方法用来绑定用户信息
bind_user.setOnClickListener {
   val exts = JSONObject()
   exts.put("sex","male")
   FTSdk.get().bindUserData("jack","001",exts)
}

//可以在用户退出登录后调用此方法来解绑用户信息
unbind_user.setOnClickListener {
   FTSdk.get().unbindUserData()
}
```

>注意
1、当 setNeedBindUser 设置为 true 时，一定要绑定用户信息才能将埋点数据
上传到服务器，否者会一直等待用户绑定。
2、当监控项配置了 CAMERA 时，需要申请相机权限
3、当监控项配置了 LOCATION 时，需要申请定位权限

### 二、关于权限的配置
DataFlux SDK 用到了系统的四个权限，分别为 READ_PHONE_STATE、WRITE_EXTERNAL_STORAGE、CAMERA、ACCESS_FINE_LOCATION
权限使用说明

|名称|使用原因|
|:--:|:--:|
|READ_PHONE_STATE|用于获取手机的设备信息，便于精准分析数据信息|
|WRITE_EXTERNAL_STORAGE|用户存储缓存数据|
|CAMERA|用户获取相机的配置参数|
|ACCESS_FINE_LOCATION|获取当前位置所属城市|

关于如何申请动态权限，具体详情参考[Android Developer](https://developer.android.google.cn/training/permissions/requesting?hl=en)

## 方法

### 一、初始化类 FTSDK 提供的方法

``` java
class FTSDK{
    /**
     * SDK 配置项入口
     * @param ftSDKConfig 配置项参数
     * @return
     */
    public static synchronized FTSdk install(FTSDKConfig ftSDKConfig);

    /**
     * SDK 初始化后，获得 SDK 对象
     * @return
     */
    public static synchronized FTSdk get();

    /**
     * 注销用户信息
     */
    public void unbindUserData();

    /**
     * 绑定用户信息
     * @param name 用户名
     * @param id 用户唯一标识 ID
     * @param exts 其他参数
     */
    public void bindUserData(@NonNull String name,@NonNull String id, JSONObject exts);

    /**
     * 创建获取 GPU 信息的GLSurfaceView
     * @param root
     */
    public void setGpuRenderer(ViewGroup root);

    /**
     * 关闭 SDK 正在做的操作
     */
    public void shutDown();

    /**
     * 开启定，并且获取定位结果
     */
    public static void startLocation(String geoKey, SyncCallback syncCallback);
    
    /**
     * 设置开启网络请求追踪
     * @param networkTrace
     */
    public void setNetworkTrace(boolean networkTrace);
}
```

### 二、配置类 FTSDKConfig 提供的方法

```java
class FTSDKConfig{
    /**
     * 构建 SDK 必要的配置参数（当不需要签名时可以用此方法）
     * @param metricsUrl 服务器地址
     * @return
     */
    public static FTSDKConfig builder(String metricsUrl);
    /**
     * 构建 SDK 必要的配置参数
     * @param metricsUrl 服务器地址
     * @param enableRequestSigning 是否需要对请求进行签名
     * @param akId 签名 id，当 enableRequestSigning 为 true 时必须设置
     * @param akSecret 签名 Secret，当 enableRequestSigning 为 true 时必须设置
     * @return
    */
    public static FTSDKConfig builder(String metricsUrl, boolean enableRequestSigning, String akId, String akSecret);

    /**
     * 设置自动埋点的事件类别
     * @param type
     * @return
     */
    public FTSDKConfig setEnableAutoTrackType(int type);

    /**
     * 是否使用 UseOAID 作为设备唯一识别号的替代字段
     * @param useOAID
     * @return
     */
    public FTSDKConfig setUseOAID(boolean useOAID);

    /**
     * 是否开启Debug，开启后将显示 SDK 运行日志
     * @param debug
     * @return
     */
    public FTSDKConfig setDebug(boolean debug);

    /**
     * 是否需要绑定用户信息
     * @param needBindUserVar
     * @return
     */
    public FTSDKConfig setNeedBindUser(boolean needBindUserVar);

    /**
     * 设置监控类别
     * @param monitorType 支持一项或者几项取或值
     * 例如：MonitorType.BATTERY or MonitorType.MEMORY
     * @return
     */
    public FTSDKConfig setMonitorType(int monitorType);

    /**
     * 设置白名单（Activity，Fragment）
     *
     * @param classes
     * @return
     */
    public FTSDKConfig setWhiteActivityClasses(List<Class<?>> classes);


    /**
     * 设置控件白名单
     *
     * @param classes
     * @return
     */
    public FTSDKConfig setWhiteViewClasses(List<Class<?>> classes);

    /**
     * 设置关闭的自动埋点事件类别
     *
     * @param type
     * @return
     */
    public FTSDKConfig setDisableAutoTrackType(int type);

    /**
     * 设置黑名单（Acitivty，Fragment）
     *
     * @param classes
     * @return
     */
    public FTSDKConfig setBlackActivityClasses(List<Class<?>> classes);

    /**
     * 设置控件黑名单
     *
     * @param classes
     * @return
     */
    public FTSDKConfig setBlackViewClasses(List<Class<?>> classes);
    
    
    /**
     * 页面别名对应 map
     * @param pageDescMap
     * @return
     */
    public FTSDKConfig addPageDesc(Map<String, String> pageDescMap) {
        this.pageDescMap = pageDescMap;
        return this;
    }

    /**
     * 事件别名对应 map
     * @param vtpDescMap
     * @return
     */
    public FTSDKConfig addVtpDesc(Map<String, String> vtpDescMap) {
        this.vtpDescMap = vtpDescMap;
        return this;
    }

    /**
     * 设置页面和视图树是否使用别名
     * @param pageVtpDescEnabled
     * @return
     */
    public FTSDKConfig setPageVtpDescEnabled(boolean pageVtpDescEnabled) {
        this.pageVtpDescEnabled = pageVtpDescEnabled;
        return this;
    }

}
```

### 三、手动埋点类 FTTrack

``` java
class FTTrack{

    /*** 主动埋点
     * @param event 埋点事件名称
     * @param tags 埋点数据
     * @param values 埋点数据
     */
    public void trackBackground(String event, JSONObject tags, JSONObject values);

    /**
     * 主动埋点，异步上传用户埋点数据并返回上传结果
     *
     * @param event  埋点事件名称
     * @param tags   埋点数据
     * @param values 埋点数据
     * @param callback 上传结果回调
     */
    public void trackImmediate(String event, JSONObject tags, JSONObject values,SyncCallback callback);

    /**
     * 主动埋点多条数据，异步上传用户埋点数据并返回上传结果
     *
     * @param trackBeans  多条埋点数据
     * @param callback 上传结果回调
     */
    public void trackImmediate(List<TrackBean> trackBeans, SyncCallback callback);

  /**
     * 将单条日志数据存入本地同步
     *
     * @param content
     * @param status
     */
    public void logBackground(String content, Status status);
  
  /**
     * 将多条日志数据存入本地同步
     *
     * @param logDataList
     */
    public void logBackground(List<LogData> logDataList);
}
```

#### 关于主动埋点 trackImmediate 的结果回调 SyncCallback 的说明

回调方法 onResponse(int code,String response) 中 code 表示网络请求返回的返回码，response 为服务端返回的信息。
code 的值除了 HTTP 协议中规定的返回码，FT SDK 中额外规定了 5 种类型的错误码，他们是 10001，10002，10003，10004，10005 他们分别
代表的意思是网络问题、参数问题、IO异常、未知错误、token 错误

### 四、监控配置项类 MonitorType

``` java
public class MonitorType {
    //所有配置
    public static int ALL = 1;

    //电池（电池总量、电池使用量）
    public static int BATTERY = 1<<1;

    //内存（内存总量、内存使用率）
    public static int MEMORY = 1<<2;

    //CPU（CPU 型号、CPU 占用率、CPU 总频率、CPU 温度）
    public static int CPU = 1<<3;

    //GPU（GPU 型号、GPU 使用率、GPU 总频率）
    public static int GPU = 1<<4;

    //网络（信号强度、网络速度、网络类型、代理）
    public static int NETWORK = 1<<5;

    //相机（前后置相机像素）
    public static int CAMERA = 1<<6;

    //位置（所在城市）
    public static int LOCATION = 1<<7;
    
    //系统相关
    public static int SYSTEM = 1<<8;
    
    //传感器
    public static int SENSOR = 1<<9;
    
    //蓝牙
    public static int BLUETOOTH = 1<<10;
    
    //光线传感器
    public static int SENSOR_BRIGHTNESS = 1 << 11;
    
    //步数传感器
    public static int SENSOR_STEP = 1 << 12;
    
    //距离传感器
    public static int SENSOR_PROXIMITY = 1 << 13;
    
    //陀螺仪三轴旋转角速度
    public static int SENSOR_ROTATION = 1 << 14;
    
    //三轴线性加速度
    public static int SENSOR_ACCELERATION = 1 << 15;
    
    //三轴地磁强度
    public static int SENSOR_MAGNETIC = 1 << 16;
    
    //光线传感器
    public static int SENSOR_LIGHT = 1 << 17;
    
    //闪光灯
    public static int SENSOR_TORCH = 1 << 18;
    
    //屏幕帧率
    public static int FPS = 1 << 19;
}
```

### 五、添加网络追踪
目前只支持 OKHttp 网络请求追踪,其中支持的 OkHttp 最低版本为 4.0.0-RC1

```java
FTSDKConfig ftSDKConfig = FTSDKConfig.builder(AccountUtils.getProperty(this, [server url]),
                true,
                [ACCESS_KEY]
                [ACCESS_SECRET])
//...
                .setNetworkTrace(true)
                .setTraceType(TraceType.JAEGER);//默认为 ZIPKIN
        FTSdk.install(ftSDKConfig);

//建议使用全局 OkHttpClient,支持多个 OkHttpClient 且线程安全
OkHttpClient client = new OkHttpClient.Builder()
                        .addInterceptor(new FTNetWorkTracerInterceptor())
                        .build();

//可以通过方法动态改变                      
FTSdk.get().setNetworkTrace(true)


```

## 常见问题
### 一、关于 OAID
#### 1. 介绍

 在 Android 10 版本中，非系统应用将不能获取到系统的 IMEI、MAC 等信息。面对该问题移动安全联盟联合国内的手机厂商推出了
补充设备标准体系方案，选择用 OAID 字段作为IMEI等系统信息的替代字段。OAID 字段是由中国信通院联合华为、小米、OPPO、
VIVO 等厂商共同推出的设备识别字段，具有一定的权威性。目前 DataFlux SDK 使用的是 oaid_sdk_1.0.22.aar
关于 OAID 可移步参考[移动安全联盟](http://www.msa-alliance.cn/col.jsp?id=120)

#### 2. 使用

 使用方式和资源下载可参考[移动安全联盟的集成文档](http://www.msa-alliance.cn/col.jsp?id=120)

#### 3. 示例

##### 1. 下载好资源文件后，将 oaid_sdk_1.0.22.aar 拷贝到项目的 libs 目录下，并设置依赖
[获取最新版本](http://www.msa-alliance.cn/col.jsp?id=120)

![Alt](http://zhuyun-static-files-production.oss-cn-hangzhou.aliyuncs.com/helps/markdown-screentshot/ft-sdk-android/use_learn_1.png#pic_center)

##### 2. 将下载的资源中的 supplierconfig.json 文件拷贝到主项目的 assets 目录下，并修改里面对应的内容，特别是需要设置 appid 的部分。需要设置 appid 的部分需要去对应厂商的应用商店里注册自己的 app。

![Alt](http://zhuyun-static-files-production.oss-cn-hangzhou.aliyuncs.com/helps/markdown-screentshot/ft-sdk-android/use_learn_2.png#pic_center)

 ![Alt](http://zhuyun-static-files-production.oss-cn-hangzhou.aliyuncs.com/helps/markdown-screentshot/ft-sdk-android/use_learn_3.png#pic_center)

##### 3. 设置依赖

``` groovy
implementation files('libs/oaid_sdk_1.0.22.arr')
```

##### 4. 混淆设置

```
 -keep class com.bun.miitmdid.core.**{*;}
```

##### 5. 设置 gradle 编译选项，这块可以根据自己的对平台的选择进行合理的配置

``` groovy
ndk {
    abiFilters 'armeabi-v7a','x86','arm64-v8a','x86_64','armeabi'
}
packagingOptions {
    doNotStrip "*/armeabi-v7a/*.so"
    doNotStrip "*/x86/*.so"
    doNotStrip "*/arm64-v8a/*.so"
    doNotStrip "*/x86_64/*.so"
    doNotStrip "armeabi.so"
}
```

##### 6. 以上步骤配置完成后，在配置 FT SDK 时调用 FTSDKConfig 的 setUseOAID(true) 方法即可

### 二、关于监控项中有些参数获取不到问题说明

#### GPU

GPU 中的频率和使用率的值通过读取设备中配置文件获取，有些设备可能获取不到或只能在 root 下获取

#### CPU

CPU 温度有些设备可能获取不到（每种手机可能 CPU 温度文件存储位置不同），如果你有这样的问题欢迎在 Issue
中提出这问题，并把你的机型贴出来，以便我们完善 CPU 温度文件配置。

### 三、如何监控网络请求的相关时长
DataFlux SDK 中对于网络请求的全路径时长统计，是基于 OkHttp 网络请求引擎来实现的。如果你想要只想要监控 DataFlux SDK 中的相关网络
请求的时长，你只需要在配置 SDK 时调用 openNetTime(true) 方法即可。如果你需要监控当前应用的所有网络请求，你需要按以下步骤来实现。

> 步骤1:开启 SDK 中的网络时长监控开关

```kotlin

class DemoAplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val ftSDKConfig = FTSDKConfig.builder(
            "serverUrl",//服务器地址
            true,//是否需要签名
            "accesskey_id",//access key ID
            "accessKey_secret"//access key Secret
        ).openNetTime(true)
            
        FTSdk.install(ftSDKConfig)
    }
}

```

> 步骤2:在 OkHttpClient 中注册网络请求监听器

```kotlin
class CustomOkHttp {
    fun initOKHttp(){
        OkHttpClient.Builder()
                .eventListener(OKHttpEventListener())
                .build()
    }
}
```

### 四、关于页面和视图树及流程图的描述使用

> 重要提示：对于需要埋点的点击事件，你需要对点击的控件设置 ID，如果不设置 ID 那么视图树结尾处的控件 ID 将显示为null，这样不利于
> 区分视图树，因此需要对每个可以点击的控件设置一个 ID

#### 1、首先在配置 SDK 的时候打开描述日志的开关 setDescLog(true)

```kotlin
class MyApplication{
    override fun onCreate() {
        super.onCreate()
        val ftSDKConfig = FTSDKConfig.builder(
            "serverUrl",//服务器地址
            true,//是否需要签名
            "accesskey_id",//access key ID
            "accessKey_secret"//access key Secret
        ).setDebug(false)//是否开启Debug模式（开启后能查看调试数据）
            .setDescLog(true)//开启显示页面和描述日志的显示
            .setXDataKitUUID("ft-dataKit-uuid-001")
            .enableAutoTrack(true)//是否开启自动埋点
            .setPageVtpDescEnabled(true)
            .setEnableAutoTrackType(FTAutoTrackType.APP_START.type or
                    FTAutoTrackType.APP_END.type or
                    FTAutoTrackType.APP_CLICK.type)//自动埋点事件白名单
        FTSdk.install(ftSDKConfig)
       }
}
```

#### 2、运行程序，打开你需要设置描述的页面，点击你需要设置描述的按钮等控件，然后找到控制台输出的对应日志

例如：

```
2020-05-20 18:25:28.235 12123-12123/com.ft D/[FT-SDK]:: 当前页面的 name 值为:MainActivity
2020-05-20 18:25:28.236 12123-12123/com.ft D/[FT-SDK]:: 当前页面的 name 值为:MainActivity.Tab1Fragment
2020-05-20 18:26:36.029 12123-12123/com.ft D/[FT-SDK]:: 当前点击事件的 vtp 值为:MainActivity/ViewRootImpl/DecorView/LinearLayout/FrameLayout/ActionBarOverlayLayout/ContentFrameLayout/ScrollView/LinearLayout/AppCompatCheckBox/#checkbox
```

#### 3、建立两个 Map 数据集，将步骤2中输出的日志放入 Map 中,并且将数据集加入到 SDK 的配置中

例如：

```kotlin
class MyApplication{
    private fun pageDescMap(): Map<String, String>? {
        return mutableMapOf(
                Pair("MainActivity", "首页面"),
                Pair("MainActivity.Tab1Fragment", "首页面中的片段1"))
    }
    
    private fun vtpDescMap(): Map<String, String>? {
        return mutableMapOf(
                Pair("MainActivity/ViewRootImpl/DecorView/LinearLayout/FrameLayout/ActionBarOverlayLayout/ContentFrameLayout/ScrollView/LinearLayout/AppCompatCheckBox/#checkbox", "点击选中复选框"))
    }
    
    override fun onCreate() {
        super.onCreate()
        val ftSDKConfig = FTSDKConfig.builder(
            "serverUrl",//服务器地址
            true,//是否需要签名
            "accesskey_id",//access key ID
            "accessKey_secret"//access key Secret
        ).setDebug(true)//是否开启Debug模式（开启后能查看调试数据）
            .setDescLog(true)//开启显示页面和描述日志的显示
            .setXDataKitUUID("ft-dataKit-uuid-001")
            .enableAutoTrack(true)//是否开启自动埋点
            .addPageDesc(pageDescMap())
            .addVtpDesc(vtpDescMap())
            .setPageVtpDescEnabled(true)
            .setEnableAutoTrackType(FTAutoTrackType.APP_START.type or
                    FTAutoTrackType.APP_END.type or
                    FTAutoTrackType.APP_CLICK.type)//自动埋点事件白名单
        FTSdk.install(ftSDKConfig)
       }
}
```

#### 4、除了上面第3步添加两个 Map 方式外,也可以通过添加名为 ft_page_vtp_desc 的配置文件来实现

具体方法如下：
在 raw 文件夹下面建立一个名为 ft_page_vtp_desc 的 xml 文件，严格按照示例中的标签名来组织文件，文件格式如下

```xml
<?xml version="1.0" encoding="utf-8"?>
<root>
    <pagedesc>
        <page
            name="页面类名1"
            desc="页面别名1" />
        <page
            name="页面类名2"
            desc="页面别名2" />
    </pagedesc>
    <vtpdesc>
        <vtp
            desc="事件别名1"
            path="事件视图树1" />
        <vtp
            desc="事件别名2"
            path="事件视图树2" />
    </vtpdesc>
</root>
```

### 五、关于崩溃日志中混淆内容转换的问题

#### 1、问题描述

当你的应用发生崩溃且你已经接入 DataFlux SDK 同时你也开启了崩溃日志上报的开关时，你可以到 DataFlux 后台你的工作空间下的日志模块找到相应
的崩溃日志。如果你的应用开启了混淆，此时你会发现崩溃日志的堆栈信息也被混淆，无法直接定位具体的崩溃位置，因此你需要按以下方式来解决该问题。

#### 2、解决方式

* 找到 mapping 文件。如果你开启了混淆，那么在打包的时候会在该目录下（module-name -> build -> outputs -> mapping）生成一个混淆文件映射表（mapping.txt）,该文件就是源代码与混淆后的类、方法和属性名称之间的映射。因此每次发包后应该根据应用的版本号保存好对应 mapping.txt 文件，以备根据后台日志 tag 中的版本名字段（app_version_name）来找到对应 mapping.txt 文件。

* 下载崩溃日志文件。到 DataFlux 的后台中把崩溃日志下载到本地，这里假设下载到本地的文件名为 crash_log.txt

* 运行 retrace 命令转换崩溃日志。Android SDK 中自带 retrace 工具（该工具在目录 sdk-root/tools/proguard 下，windows 版本是 retrace.bat,Mac/Linux 版本是 retrace.sh），通过该工具可以恢复崩溃日志的堆栈信息。命令示例

  ```
  retrace.bat -verbose mapping.txt crash_log.txt
  ```

* 上一步是通过 retrace 命令行来执行，当然也可以通过 GUI 工具。在 <sdk-root>/tools/proguard/bin 目录下有个 proguardgui.bat 或 proguardgui.sh GUI 工具。运行 proguardgui.bat 或者 ./proguardgui.sh -> 从左侧的菜单中选择“ReTrace” -> 在上面的 Mapping file 中选择你的 mapping 文件，在下面输入框输入要还原的代码 ->点击右下方的“ReTrace!”

