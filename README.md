# Dataflux SDK Android

**Demo**

地址：[https://github.com/CloudCare/dataflux-sdk-android-demo](https://github.com/CloudCare/dataflux-sdk-android-demo)

**Agent**

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmvnrepo.jiagouyun.com%2Frepository%2Fmaven-releases%2Fcom%2Fcloudcare%2Fft%2Fmobile%2Fsdk%2Ftraker%2Fagent%2Fft-sdk%2Fmaven-metadata.xml)](https://mvnrepo.jiagouyun.com/repository/maven-releases/com/cloudcare/ft/mobile/sdk/traker/agent/ft-sdk/maven-metadata.xml)

**Plugin**

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmvnrepo.jiagouyun.com%2Frepository%2Fmaven-releases%2Fcom%2Fcloudcare%2Fft%2Fmobile%2Fsdk%2Ftraker%2Fplugin%2Fft-plugin%2Fmaven-metadata.xml)](https://mvnrepo.jiagouyun.com/repository/maven-releases/com/cloudcare/ft/mobile/sdk/traker/plugin/ft-plugin/maven-metadata.xml)

## 安装
### 1. 在项目的根目录的 build.gradle 文件中添加 DataFlux SDK 的远程仓库地址
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
        classpath 'com.cloudcare.ft.mobile.sdk.traker.plugin:ft-plugin:1.0.0-alpha5'
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
### 2. 在项目主模块( app 模块)的 build.gradle 文件中添加 DataFlux SDK 的依赖及 DataFlux Plugin 的使用 和 Java 8 的支持

``` groovy
dependencies {
    //添加 DataFlux SDK 的依赖
    implementation 'com.cloudcare.ft.mobile.sdk.traker.agent:ft-sdk:$last_version'
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

### 3. 添加混淆

如果你的项目开启了混淆，那么在你的 proguard-rules.pro 文件中添加如下配置

```
-keep class * extends com.ft.sdk.garble.http.ResponseData{ *;}
-keep class com.ft.sdk.FTAutoTrack{*;}
-keep enum com.ft.sdk.FTAutoTrackType{*;}
-keep enum com.ft.sdk.FTSdk{*;}
```
> 注意：如果你的项目中开启了全埋点和流程图，那么需要将你的 Fragment 和 Activity 保持不被混淆，这样流程图中
> 就会显示页面的真实名称，而不是混淆后的名称

## 配置

### 一、 DataFlux SDK 功能配置项说明

#### 1. DataFlux SDK 包含的功能说明

|          方法名          |                含义                |       是否必须        |                                                          注意                                                           |
|:-----------------------:|:---------------------------------:|:-------------------:|:----------------------------------------------------------------------------------------------------------------------:|
|       setUseOAID        | 是否使用OAID作为设备唯一识别号的替代字段 |          否          |                         默认不使用,开启后全埋点数据里将会添加一个 oaid 字段<br>[了解 OAID](#一关于-oaid)                          |
|     setXDataKitUUID     |         设置数据采集端的名称          |         否          |                                              不设置该值系统会生成一个默认的 uuid                                              |
|        setDebug         |           是否开启调试模式           |          否          |                                            默认不开启，开启后方可打印 SDK 运行日志                                            |
|     setMonitorType      |             设置监控项              |         否          | 默认不开启任何监控项,<br>[关于监控项说明](#四监控配置项类-monitortype),<br>[关于监控项参数获取问题](#二关于监控项中有些参数获取不到问题说明) |
|     setNeedBindUser     |         是否开启绑定用户数据          |         否          |                        默认不开启,<br>开启后必须要绑定用户数据[如何绑定用户数据](#一初始化类-ftsdk-提供的方法)                        |
|    setOpenFlowChart     |     是否开启自动埋点流程图数据上报      |         否          |                                        [详细说明](#三关于自动埋点的页面路径流程图的说明)                                         |
|     setFlowProduct      |           设置流程的指标集           |          否          |                                               当开启了上报流程图一定要设置该值                                               |
|     enableAutoTrack     |           是否使用自动埋点           |          否          |                                               不开启将不会上报流程图和埋点事件                                               |
| setEnableAutoTrackType  |            设置事件白名单            |         否          |                                开启自动埋点后，不设置该值表示接受所有事件类型。埋点事件类型见表下说明                                 |
| setDisableAutoTrackType |            设置事件黑名单            |         否          |                                          开启自动埋点后，不设置该值表示不设置事件黑名单                                          |
| setWhiteActivityClasses |             页面白名单              |         否          |                                                 包括 Activity、Fragment                                                  |
|   setWhiteViewClasses   |             控件白名单              |         否          |                                                       包括基本控件                                                        |
| setBlackActivityClasses |             页面黑名单              |         否          |                                                 包括 Activity、Fragment                                                  |
|   setBlackViewClasses   |             控件黑名单              |         否          |                                                       包括基本控件                                                        |
|       metricsUrl        |    FT-GateWay metrics 写入地址     |          是          |                                                  必须配置，配置后才能上报                                                   |
|  enableRequestSigning   |       配置是否需要进行请求签名        |          否          |                                                      默认不开启                                                           |
|          akId           |           access key ID           |         否          |                                         enableRequestSigning 为 true 时，必须要填                                          |
|        akSecret         |         access key Secret         |         否          |                                         enableRequestSigning 为 true 时，必须要填                                          |
|        setGeoKey        |  设置是否使用高德作为地址解析器和key     |         否          |              如何申请高德的 key？[点我快速了解](https://lbs.amap.com/api/webservice/guide/api/georegeo)                      |
|        setINetEngineClass        |  设置网络请求框架的引擎实现类     |         否          |               [点我快速了解如何实现网络引擎](#四如何监控网络请求的相关时长)                                    |


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
|setTcpStartTime|设置网络请求握手开始时间|否|需要在实现的网络框架中调用。[点我查看如何使用](#四如何监控网络请求的相关时长)|
|setTcpEndTime|设置网络请求握手结束时间|否|需要在实现的网络框架中调用|
|setDnsStartTime|设置网络请求DNS解析开始时间|否|需要在实现的网络框架中调用|
|setDnsEndTime|设置网络请求DNS解析结束时间|否|需要在实现的网络框架中调用|
|setResponseStartTime|设置网络请求开始时间|否|需要在实现的网络框架中调用|
|setResponseEndTime|设置网络请求结束时间|否|需要在实现的网络框架中调用|
|setRequestCount|设置网路请求次数|否|需要在实现的网络框架中调用|
|setRequestErrCount|设置网路请求错误次数|否|需要在实现的网络框架中调用|
|setPeriod|设置周期|否|单位秒，默认为10秒|
|setUseGeoKey|设置是否使用高德作为地址解析器|否|设置高德的 Key 后，如果不开启就不会使用|
|setGeoKey|设置高德解析器的 key|否|如何申请高德的 key？[点我快速了解](https://lbs.amap.com/api/webservice/guide/api/georegeo)|
|start|开启监控同步|是|只有调用该方法后才会执行同步监控|

#### 4. 示例代码

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
            .setXDataKitUUID("ft-dataKit-uuid-001")
            .setNeedBindUser(true)//是否绑定用户信息
            .setMonitorType(MonitorType.ALL)//设置监控项
            .setOpenFlowChart(true)//开启流程图
            .setProduct("demo12")//流程图唯一识别号
            .enableAutoTrack(true)//是否开启自动埋点
            .setEnableAutoTrackType(FTAutoTrackType.APP_START.type or
                    FTAutoTrackType.APP_END.type or
                    FTAutoTrackType.APP_CLICK.type)//自动埋点事件白名单
            .setWhiteActivityClasses(listOf(MainActivity::class.java))//自动埋点页面白名单
            .setWhiteViewClasses(listOf(Button::class.java))//自动埋点控件白名单
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
     * 设置是否开启流程图
     *
     * @param openFlowChart
     * @return
     */
    public FTSDKConfig setOpenFlowChart(boolean openFlowChart);

    /**
     * 图标类型代号
     *
     * @param flowProduct
     * @return
     */
    public FTSDKConfig setFlowProduct(String flowProduct);

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
     * 流程图数据上报
     *
     * @param product 指标集，流程图以该值进行分类
     * @param traceId 标示一个流程图的全程唯一ID
     * @param name 流程节点名称
     * @param parent 流程图当前流程节点的上一个流程节点名称，如果是第一个节点，该值应填null
     * @param duration 流程图在该节点所耗费或持续时间，单位为毫秒
     * @param tags 其他标签值（该值中不能含 traceId，name，parent 字段）
     * @param values 其他指标（该值中不能含 duration 字段）
     */
    public void trackFlowChart(String product, String traceId, String name, String parent, long duration,JSONObject tags,JSONObject values);
}
```

#### 关于主动埋点 trackImmediate 的结果回调 SyncCallback 的说明

回调方法 onResponse(int code,String response) 中 code 表示网络请求返回的返回码，response 为服务端返回的信息。
code 的值除了 HTTP 协议中规定的返回码，FT SDK 中额外规定了 4 种类型的错误码，他们是 101，102，103，104，他们分别
代表的意思是网络问题、参数问题、IO异常和未知错误

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


## 常见问题
### 一、关于 OAID
#### 1. 介绍

 在 Android 10 版本中，非系统应用将不能获取到系统的 IMEI、MAC 等信息。面对该问题移动安全联盟联合国内的手机厂商推出了
补充设备标准体系方案，选择用 OAID 字段作为IMEI等系统信息的替代字段。OAID 字段是由中国信通院联合华为、小米、OPPO、
VIVO 等厂商共同推出的设备识别字段，具有一定的权威性。
关于 OAID 可移步参考[移动安全联盟](http://www.msa-alliance.cn/col.jsp?id=120)

#### 2. 使用

 使用方式和资源下载可参考[移动安全联盟的集成文档](http://www.msa-alliance.cn/col.jsp?id=120)

#### 3. 示例

##### 1. 下载好资源文件后，将 miit_mdid_x.x.x.arr 拷贝到项目的 libs 目录下，并设置依赖，其中 x.x.x 代表版本号
[获取最新版本](http://www.msa-alliance.cn/col.jsp?id=120)

![Alt](http://zhuyun-static-files-production.oss-cn-hangzhou.aliyuncs.com/helps/markdown-screentshot/ft-sdk-android/use_learn_1.png#pic_center)

##### 2. 将下载的资源中的 supplierconfig.json 文件拷贝到主项目的 assets 目录下，并修改里面对应的内容，特别是需要设置 appid 的部分。需要设置 appid 的部分需要去对应厂商的应用商店里注册自己的 app。

![Alt](http://zhuyun-static-files-production.oss-cn-hangzhou.aliyuncs.com/helps/markdown-screentshot/ft-sdk-android/use_learn_2.png#pic_center)

 ![Alt](http://zhuyun-static-files-production.oss-cn-hangzhou.aliyuncs.com/helps/markdown-screentshot/ft-sdk-android/use_learn_3.png#pic_center)

##### 3. 设置依赖

``` groovy
implementation files('libs/miit_mdid_x.x.x.arr')
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

### 三、关于自动埋点的页面路径流程图的说明
当集成者通过 setOpenFlowChart 方法开启了页面路径流程图统计并设置了流程图的指标集后，我们将会通过无埋点技术
将用户的使用页面路径上报并将其用流程图的形式显示。其中对于 Activity 我们监听的是 onResume 和 onPause 方法
，对于 Fragment 我们监听的是 onResume 和 onPause。其中需要注意的是 Fragment 显示的方式有多种，我们
统计如下。
1、通过 add 和 replace 方法添加和替换 Fragment
2、通过 hidden 和 show 方法显示和隐藏 Fragment
3、通过 ViewPager 来控制 Fragment。
由于这 3 种管理 Fragment 的方式，会使 Fragment 经历不同的生命周期，因此为了避免出现 Fragment 页面打开的路径
流程图有问题，我们建议集成者使用 1 和 3 两种方式来管理 Fragment

### 四、如何监控网络请求的相关时长
DataFlux SDK 中网络请求基于 HttpUrlConnection 实现，其无法显示对于标题中数据项的监控。研究发现若想监控上面的数据
需要使用 OKHttp 网络框架。OKHttp 中的 OkHttpClient 提供 eventListener(EventListener e) 来监听网络请求的
全路径。因此若需要监控上面数据需要自行切换网络引擎。DataFlux SDK 中提供 INetEngine 接口，接入方只需要实现接口，并且在
初始化 SDK 时，将实现的接口类通过 FTSDKConfig.setINetEngineClass(OkHttpEngine.class) 来使其生效。
下面提供一种实现引擎的方式以供参考
```java
public class OkHttpEngine implements INetEngine {
    private static OkHttpClient client;
    private Request request;

    @Override
    public void defaultConfig(HttpBuilder httpBuilder) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(httpBuilder.getSendOutTime(), TimeUnit.MILLISECONDS)
                    .readTimeout(httpBuilder.getReadOutTime(), TimeUnit.MILLISECONDS)
                    .eventListener(new EventListener() {
                        @Override
                        public void callEnd(@NotNull Call call) {
                            super.callEnd(call);
                            FTMonitor.get().setResponseEndTime();
                        }

                        @Override
                        public void callFailed(@NotNull Call call, @NotNull IOException ioe) {
                            super.callFailed(call, ioe);
                            FTMonitor.get().setRequestErrCount();
                        }

                        @Override
                        public void callStart(@NotNull Call call) {
                            super.callStart(call);
                            FTMonitor.get().setRequestCount();
                            FTMonitor.get().setResponseStartTime();
                        }

                        @Override
                        public void dnsEnd(@NotNull Call call, @NotNull String domainName, @NotNull List<InetAddress> inetAddressList) {
                            super.dnsEnd(call, domainName, inetAddressList);
                            FTMonitor.get().setDnsEndTime();
                        }

                        @Override
                        public void dnsStart(@NotNull Call call, @NotNull String domainName) {
                            super.dnsStart(call, domainName);
                            FTMonitor.get().setDnsStartTime();
                        }

                        @Override
                        public void secureConnectEnd(@NotNull Call call, @Nullable Handshake handshake) {
                            super.secureConnectEnd(call, handshake);
                            FTMonitor.get().setTcpEndTime();
                        }

                        @Override
                        public void secureConnectStart(@NotNull Call call) {
                            super.secureConnectStart(call);
                            FTMonitor.get().setTcpStartTime();
                        }
                    })
                    .build();
        }
    }

    @Override
    public void createRequest(HttpBuilder httpBuilder) {
        RequestBody requestBody = null;
        if (httpBuilder.getMethod() == RequestMethod.POST) {
            requestBody = RequestBody.create(null, httpBuilder.getBodyString());
        }
        Headers.Builder builder = new Headers.Builder();
        HashMap<String, String> hashMap = httpBuilder.getHeadParams();
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        request = new Request.Builder()
                .url(httpBuilder.getUrl())
                .method(httpBuilder.getMethod().name(), requestBody)
                .headers(builder.build())
                .build();
    }

    @Override
    public ResponseData execute() {
        try {
            Response response = client.newCall(request).execute();
            ResponseBody responseBody = response.body();
            String string = "";
            if (responseBody != null) {
                string = responseBody.string();
            }
            return new ResponseData(response.code(), string);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

```