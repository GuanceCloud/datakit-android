# Dataflux SDK Android

**demo**

地址：[https://github.com/CloudCare/dataflux-sdk-android-demo](https://github.com/CloudCare/dataflux-sdk-android-demo)

**agent**

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmvnrepo.jiagouyun.com%2Frepository%2Fmaven-releases%2Fcom%2Fcloudcare%2Fft%2Fmobile%2Fsdk%2Ftraker%2Fagent%2Fft-sdk%2Fmaven-metadata.xml)](https://mvnrepo.jiagouyun.com/repository/maven-releases/com/cloudcare/ft/mobile/sdk/traker/agent/ft-sdk/maven-metadata.xml)

**plugin**

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fmvnrepo.jiagouyun.com%2Frepository%2Fmaven-releases%2Fcom%2Fcloudcare%2Fft%2Fmobile%2Fsdk%2Ftraker%2Fplugin%2Fft-plugin%2Fmaven-metadata.xml)](https://mvnrepo.jiagouyun.com/repository/maven-releases/com/cloudcare/ft/mobile/sdk/traker/plugin/ft-plugin/maven-metadata.xml)

## 安装
- 在项目的根目录的 build.gradle 文件中添加 FT SDK 的远程仓库地址

``` groovy

buildscript {
    //...
    repositories {
        //...
        //添加FT SDK的远程仓库地址
        maven {
            url 'https://mvnrepo.jiagouyun.com/repository/maven-releases'
        }
    }
    dependencies {
        //...
        //添加 FT Plugin 的插件依赖
        classpath 'com.cloudcare.ft.mobile.sdk.traker.plugin:ft-plugin:1.0.0-alpha5'
    }
}
allprojects {
    repositories {
        //...
        //添加FT SDK的远程仓库地址
        maven {
            url 'https://mvnrepo.jiagouyun.com/repository/maven-releases'
        }
    }
}
```

- 在项目主模块( app 模块)的 build.gradle 文件中添加 FT SDK 的依赖及 FT Plugin 的使用

``` groovy
dependencies {
    //添加 FT SDK 的依赖
    implementation 'com.cloudcare.ft.mobile.sdk.traker.agent:ft-sdk:1.0.0'
}
//应用插件
apply plugin: 'ft-plugin'
//配置插件使用参数
FTExt {
    //是否显示日志
    showLog = true
}
```

- 在项目主模块( app 模块)的 build.gradle 文件中添加 Java 8 的支持

``` groovy
android{
    compileOptions {
        sourceCompatibility = 1.8
        targetCompatibility = 1.8
    }
}
```

关于 FT SDK 最新的版本号，请参考[更新文档](https://mvnrepo.jiagouyun.com/repository/maven-public/com/cloudcare/ft/mobile/sdk/traker/agent/ft-sdk/maven-metadata.xml)

关于 FT Plugin 最新的版本号，请参考[更新文档](https://mvnrepo.jiagouyun.com/repository/maven-public/com/cloudcare/ft/mobile/sdk/traker/plugin/ft-plugin/maven-metadata.xml)
## 配置

### 一、添加混淆配置

如果你的项目开启了混淆，那请在您的 proguard-rules.pro 文件中添加如下配置

```
-keep class * extends com.ft.sdk.garble.http.ResponseData{
     *;
}

-keep class com.ft.sdk.FTAutoTrack{
     *;
}

-keep enum com.ft.sdk.FTAutoTrackType{
     *;
}
```

### 二、关于 FT SDK 初始化的参数、方法等配置项的说明

#### 1、通过 FTSDKConfig 构建 SDK 配置项的参数方法

方法说明表

方法名|含义|是否必须|注意
:--:|:--:|:--:|:--:
setUseOAID|是否使用OAID作为设备唯一识别号的替代字段 |否|默认不使用,<br>[了解 OAID](#1关于-oaid)
setXDataKitUUID|设置数据采集端的名称|否|不设置该值系统会生成一个默认的 uuid
setDebug|是否开启调试模式|否|默认不开启，开启后方可打印 SDK 运行日志
setMonitorType|设置监控项|否|默认不开启任何监控项,<br>[关于监控项说明](#四监控配置项类-monitortype),<br>[关于监控项参数获取问题]()
setNeedBindUser|是否开启绑定用户数据|否|默认不开启,<br>开启后必须要绑定用户数据[如何绑定用户数据](#一初始化类-ftsdk-提供的方法)
setOpenFlowChart|是否开启自动埋点流程图数据上报|否|开启且开启了自动埋点，将根据 Activity 的 onResume 和 onPause 来上报流程
setFlowProduct|设置流程的指标集|否|当开启了上报流程图一定要设置该值
enableAutoTrack|是否使用自动埋点|否|""
setEnableAutoTrackType|设置事件白名单|否|开启自动埋点后，不设置该值表示接受所有事件类型。埋点事件类型见表下说明
setDisableAutoTrackType|设置事件黑名单|否|开启自动埋点后，不设置该值表示不设置事件黑名单
setWhiteActivityClasses|页面白名单|否|包括 Activity、Fragment
setWhiteViewClasses|控件白名单|否|包括基本控件
setBlackActivityClasses|页面黑名单|否|""
setBlackViewClasses|控件黑名单|否|""
builder|构建配置项对象方法|是|关于其参数可见下方参数表

    FTAutoTrackType 自动埋点事件说明：
    事件总类目前支持3种
    FTAutoTrackType.APP_START：页面的开始事件，Activity 依赖的是其 onResume 方法，Fragment 依赖的是其 onCreate 方法；
    FTAutoTrackType.APP_END：页面的结束事件，Activity 依赖的是其 onPause 方法，Fragment 依赖的是其 onDestroy 方法；
    FTAutoTrackType.APP_CLICK：控件的点击事件。

FTSDKConfig.builder(...) 方法必要参数说明表

参数|类型|含义|是否必须
:--:|:--:|:--:|:--:
metricsUrl|String|FT-GateWay metrics 写入地址|是
enableRequestSigning|boolean|配置是否需要进行请求签名|否
akId|String|access key ID|enableRequestSigning 为 true 时，必须要填
akSecret|String|access key Secret|enableRequestSigning 为 true 时，必须要填

#### 2、通过 FTSdk 安装配置项和绑定用户信息

方法说明表

方法名|含义|是否必须|注意
:--:|:--:|:--:|:--:
install|安装初始化配置项|是|在项目 application 中运行
get|获得安装后创建的 FTSdk 对象|否|应该在 install 运行后调用
unbindUserData|解绑用户信息|否|必须在 setNeedBindUser 方法设为 true 后才有效果
bindUserData|绑定用户信息|否|必须在 setNeedBindUser 方法设为 true 后才有效果
setGpuRenderer|设置 GPU 信息获取依赖的视图|否|当监控项 setMonitorType 设置监控 GPU 后一定调用该方法

#### 3、示例代码

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
            .setOpenFlowChart(true)
            .setFlowProduct("demo12")
            .enableAutoTrack(true)//是否开启自动埋点
            .setEnableAutoTrackType(FTAutoTrackType.APP_START.type or
                    FTAutoTrackType.APP_END.type or
                    FTAutoTrackType.APP_CLICK.type)//自动埋点事件白名单
            .setWhiteActivityClasses(listOf(MainActivity::class.java))//自动埋点页面白名单
            .setWhiteViewClasses(listOf(Button::class.java))//自动埋点控件白名单
        FTSdk.install(ftSDKConfig)
    }
}

```

``` kotlin
//绑定用户
bind_user.setOnClickListener {
   val exts = JSONObject()
   exts.put("sex","male")
   FTSdk.get().bindUserData("jack","001",exts)
}

//解绑用户
unbind_user.setOnClickListener {
   FTSdk.get().unbindUserData()
}
```

***注意：<br>
1、当 setNeedBindUser 设置为 true 时，一定要绑定用户信息才能将埋点数据
上传到服务器，否者会一直等待用户绑定。
<br> 2、当监控项配置了 CAMERA 时，需要申请相机权限<br>
3、当监控项配置了 LOCATION 时，需要申请定位权限***

### 三、关于权限的配置
FT SDK 用到了系统的四个权限，分别为 READ_PHONE_STATE、WRITE_EXTERNAL_STORAGE、CAMERA、ACCESS_FINE_LOCATION
权限使用说明

名称|使用原因
:--:|:--:
READ_PHONE_STATE|用于获取手机的设备信息，便于精准分析数据信息
WRITE_EXTERNAL_STORAGE|用户存储缓存数据
CAMERA|用户获取相机的配置参数
ACCESS_FINE_LOCATION|获取当前位置所属城市

关于如何申请动态权限，具体详情参考[Android Devloper](https://developer.android.google.cn/training/permissions/requesting?hl=en)

## 方法

### 一、初始化类 FTSDK 提供的方法

方法 1

``` java
/**
 * SDK 配置项入口
 * @param ftSDKConfig 配置项参数
 * @return
 */
 public static synchronized FTSdk install(FTSDKConfig ftSDKConfig)
```

方法 2

``` java
/**
 * SDK 初始化后，获得 SDK 对象
 * @return
 */
 public static synchronized FTSdk get()
```

方法 3

``` java
/**
 * 注销用户信息
 */
 public void unbindUserData()
```

方法 4

``` java
/**
 * 绑定用户信息
 * @param name 用户名
 * @param id 用户唯一标识 ID
 * @param exts 其他参数
 */
 public void bindUserData(@NonNull String name,@NonNull String id, JSONObject exts)
```
方法 5

```java
/**
 * 创建获取 GPU 信息的GLSurfaceView
 * @param root
 */
public void setGpuRenderer(ViewGroup root)
```

### 二、配置类 FTSDKConfig 提供的方法

方法 1

```java
/**
 * 构建 SDK 必要的配置参数（当不需要签名时可以用此方法）
 * @param metricsUrl 服务器地址
 * @return
 */
public static FTSDKConfig builder(String metricsUrl)
```

方法 2

```java
/**
* 构建 SDK 必要的配置参数
* @param metricsUrl 服务器地址
* @param enableRequestSigning 是否需要对请求进行签名
* @param akId 签名 id，当 enableRequestSigning 为 true 时必须设置
* @param akSecret 签名 Secret，当 enableRequestSigning 为 true 时必须设置
* @return
*/
public static FTSDKConfig builder(String metricsUrl, boolean enableRequestSigning, String akId, String akSecret)
```

方法 3

```java
/**
 * 设置自动埋点的事件类别
 * @param type
 * @return
 */
public FTSDKConfig setEnableAutoTrackType(int type)
```
方法 4

``` java
/**
 * 是否使用 UseOAID 作为设备唯一识别号的替代字段
 * @param useOAID
 * @return
 */
 public FTSDKConfig setUseOAID(boolean useOAID)
```

方法 5

``` java
/**
 * 是否开启Debug，开启后将显示 SDK 运行日志
 * @param debug
 * @return
 */
 public FTSDKConfig setDebug(boolean debug)
```

方法 6

``` java
/**
 * 是否需要绑定用户信息
 * @param needBindUserVar
 * @return
 */
 public FTSDKConfig setNeedBindUser(boolean needBindUserVar)
```

方法 7

```java
/**
 * 设置是否开启流程图
 *
 * @param openFlowChart
 * @return
 */
public FTSDKConfig setOpenFlowChart(boolean openFlowChart)
```

方法 8

```java
/**
 * 图标类型代号
 *
 * @param flowProduct
 * @return
 */
public FTSDKConfig setFlowProduct(String flowProduct)
```

方法 9

``` java
/**
 * 设置监控类别
 * @param monitorType {@link com.ft.sdk.MonitorType} 支持一项或者几项取或值
 *                                                  例如：MonitorType.BATTERY or MonitorType.MEMORY
 * @return
 */
 public FTSDKConfig setMonitorType(int monitorType)
```

方法 10

```java
/**
 * 设置白名单（Activity，Fragment）
 *
 * @param classes
 * @return
 */
public FTSDKConfig setWhiteActivityClasses(List<Class<?>> classes)
```

方法 11

```java

/**
 * 设置控件白名单
 *
 * @param classes
 * @return
 */
public FTSDKConfig setWhiteViewClasses(List<Class<?>> classes)
```

方法 12

```java
/**
 * 设置关闭的自动埋点事件类别
 *
 * @param type
 * @return
 */
public FTSDKConfig setDisableAutoTrackType(int type)
```

方法 13

```java
/**
 * 设置黑名单（Acitivty，Fragment）
 *
 * @param classes
 * @return
 */
public FTSDKConfig setBlackActivityClasses(List<Class<?>> classes)
```

方法 14
```java
/**
 * 设置控件黑名单
 *
 * @param classes
 * @return
 */
public FTSDKConfig setBlackViewClasses(List<Class<?>> classes)
```

### 三、手动埋点类 FTTrack

方法 1

``` java
/*** 主动埋点
 * @param event 埋点事件名称
 * @param tags 埋点数据
 * @param values 埋点数据
 */
 public void trackBackground(String event, JSONObject tags, JSONObject values)
```

方法 2

```java
/**
     * 主动埋点，异步上传用户埋点数据并返回上传结果
     *
     * @param event  埋点事件名称
     * @param tags   埋点数据
     * @param values 埋点数据
     * @param callback 上传结果回调
     */
    public void trackImmediate(String event, JSONObject tags, JSONObject values,SyncCallback callback)
```

方法 3

``` java
/**
     * 主动埋点多条数据，异步上传用户埋点数据并返回上传结果
     *
     * @param trackBeans  多条埋点数据
     * @param callback 上传结果回调
     */
    public void trackImmediate(List<TrackBean> trackBeans, SyncCallback callback)
```

方法 4
``` java
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
 public void trackFlowChart(String product, String traceId, String name, String parent, long duration,JSONObject tags,JSONObject values)
```

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
}
```


## 常见问题
### 1.关于 OAID
- 介绍

 在 Android 10 版本中，非系统应用将不能获取到系统的 IMEI、MAC 等信息。面对该问题移动安全联盟联合国内的手机厂商推出了
补充设备标准体系方案，选择用 OAID 字段作为IMEI等系统信息的替代字段。OAID 字段是由中国信通院联合华为、小米、OPPO、
VIVO 等厂商共同推出的设备识别字段，具有一定的权威性。
关于 OAID 可移步参考[移动安全联盟](http://www.msa-alliance.cn/col.jsp?id=120)

- 使用

 使用方式和资源下载可参考[移动安全联盟的集成文档](http://www.msa-alliance.cn/col.jsp?id=120)

 示例：

1. 下载好资源文件后，将 miit_mdid_x.x.x.arr 拷贝到项目的 libs 目录下，并设置依赖，其中 x.x.x 代表版本号
[获取最新版本](http://www.msa-alliance.cn/col.jsp?id=120)

    ![Alt](http://zhuyun-static-files-production.oss-cn-hangzhou.aliyuncs.com/helps/markdown-screentshot/ft-sdk-android/use_learn_1.png#pic_center)

2. 将下载的资源中的 supplierconfig.json 文件拷贝到主项目的 assets 目录下，并修改里面对应的内容，特别是需要设置 appid 的部分。
需要设置 appid 的部分需要去对应厂商的应用商店里注册自己的 app。

    ![Alt](http://zhuyun-static-files-production.oss-cn-hangzhou.aliyuncs.com/helps/markdown-screentshot/ft-sdk-android/use_learn_2.png#pic_center)

    ![Alt](http://zhuyun-static-files-production.oss-cn-hangzhou.aliyuncs.com/helps/markdown-screentshot/ft-sdk-android/use_learn_3.png#pic_center)

3. 设置依赖

``` groovy
implementation files('libs/miit_mdid_x.x.x.arr')
```

4. 混淆设置

```
 -keep class com.bun.miitmdid.core.**{*;}
```

5. 设置 gradle 编译选项，这块可以根据自己的对平台的选择进行合理的配置

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

6. 以上步骤配置完成后，在配置 FT SDK 时调用 FTSDKConfig 的 setUseOAID(true) 方法即可

### 2.关于监控项中有些参数获取不到问题说明

- GPU

GPU 中的频率和使用率的值通过读取设备中配置文件获取，有些设备可能获取不到或只能在 root 下获取

- CPU

CPU 温度有些设备可能获取不到（每种手机可能 CPU 温度文件存储位置不同），如果你有这样的问题欢迎在 Issue
中提出这问题，并把你的机型贴出来，以便我们完善 CPU 温度文件配置。
