# Dataflux SDK Android

**Demo**

地址：[https://github.com/CloudCare/dataflux-sdk-android-demo](https://github.com/CloudCare/dataflux-sdk-android-demo)

**Agent**

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=ft-sdk&metadataUrl=https%3A%2F%2Fmvnrepo.jiagouyun.com%2Frepository%2Fmaven-releases%2Fcom%2Fcloudcare%2Fft%2Fmobile%2Fsdk%2Ftracker%2Fagent%2Fft-sdk%2Fmaven-metadata.xml)](https://mvnrepo.jiagouyun.com/repository/maven-releases/com/cloudcare/ft/mobile/sdk/tracker/agent/ft-sdk/maven-metadata.xml)

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=ft-native&metadataUrl=https%3A%2F%2Fmvnrepo.jiagouyun.com%2Frepository%2Fmaven-releases%2Fcom%2Fcloudcare%2Fft%2Fmobile%2Fsdk%2Ftracker%2Fagent%2Fft-native%2Fmaven-metadata.xml)](https://mvnrepo.jiagouyun.com/repository/maven-releases/com/cloudcare/ft/mobile/sdk/tracker/agent/ft-native/maven-metadata.xml)

**Plugin**

[![Maven metadata URL](https://img.shields.io/maven-metadata/v?label=ft-plugin&metadataUrl=https%3A%2F%2Fmvnrepo.jiagouyun.com%2Frepository%2Fmaven-releases%2Fcom%2Fcloudcare%2Fft%2Fmobile%2Fsdk%2Ftracker%2Fplugin%2Fft-plugin%2Fmaven-metadata.xml)](https://mvnrepo.jiagouyun.com/repository/maven-releases/com/cloudcare/ft/mobile/sdk/tracker/plugin/ft-plugin/maven-metadata.xml)

## 安装
### DataFlux SDK 支持 Android 的最低版本为 23（即 Android 6.0）
### 在项目的根目录的 build.gradle 文件中添加 DataFlux SDK 的远程仓库地址
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
### 在项目主模块( app 模块)的 build.gradle 文件中添加 DataFlux SDK 的依赖及 DataFlux Plugin 的使用 和 Java 8 的支持

``` groovy
dependencies {
    //添加 DataFlux SDK 的依赖
    implementation 'com.cloudcare.ft.mobile.sdk.tracker.agent:ft-sdk:$last_version'
    //捕获 native 层崩溃信息的依赖，需要配合 ft-sdk 使用不能单独使用
    implementation 'com.cloudcare.ft.mobile.sdk.tracker.agent:ft-native:$last_version'
    //推荐使用这个版本，其他版本未做过充分兼容测试
    implementation 'com.google.code.gson:gson:2.8.5'

}
//应用插件
apply plugin: 'ft-plugin'
//配置插件使用参数
FTExt {
    //是否显示 Plugin 日志，默认为 false
    showLog = true
}
android{
	//...省略部分代码
	defaultConfig {
        //...省略部分代码
        ndk {
            //当使用 ft-native 捕获 native 层的崩溃信息时，应该根据应用适配的不同的平台
            //来选择支持的 abi 架构，目前 ft-native 中包含的 abi 架构有 'arm64-v8a',
            // 'armeabi-v7a', 'x86', 'x86_64'
            abiFilters 'armeabi-v7a'
        }
    }
    compileOptions {
        sourceCompatibility = 1.8
        targetCompatibility = 1.8
    }
}
```

>最新的版本请看上方的 Agent 和 Plugin 的版本名


## 配置

### DataFlux SDK 功能配置项说明

####  DataFlux SDK 包含的功能说明

|           方法名           |           含义            | 是否必须 |                                           注意                                           |
|:-----------------------:|:-----------------------:|:----:|:--------------------------------------------------------------------------------------:|
|       setUseOAID        | 是否使用OAID作为设备唯一识别号的替代字段  |  否   |                 默认不使用,开启后全埋点数据里将会添加一个 oaid 字段<br>[了解 OAID](#一关于-oaid)                  |
|     setXDataKitUUID     |       设置数据采集端的名称        |  否   |                                  不设置该值系统会生成一个默认的 uuid                                  |
|        setDebug         |        是否开启调试模式         |  否   |                                 默认不开启，开启后方可打印 SDK 运行日志                                 |
|     setMonitorType      |                    |  否   |  |
|       metricsUrl        |  metrics 写入地址 |  是   |                                      必须配置，配置后才能上报                                      |
| setEnableTrackAppCrash | 是否开启 App 崩溃日志上报功能 | 否 | 默认不开启，开启后将上报当前应用的崩溃日志。上报成功后，可以在后台的日志模块查看对应的日志。<br /> [关于崩溃日志中混淆内容转换的问题](#五关于崩溃日志中混淆内容转换的问题)|
| setEnableTrackAppANR | 是否开启 App ANR 检测 | 否 | 默认不开启，开启后上报 ANR 数据信息|
| setServiceName | 设置崩溃日志的名称 | 否 | 默认为 dataflux sdk。你可以将你的应用名称设置给该字段，用来区分不同的日志 |
| setEnv | 设置崩溃日志中显示的应用的环境 | 否 | 默认情况下会获取应用当前的环境。如：debug、release |
| setSampleRate | 设置采集率 | 否 | 采集率的值范围为>=0、<=1，默认值为1。<br />说明：SDK 初始化是会随机生成一个0-1之间的随机数，当这个随机数小于你设置的采集率时，那么会上报当前设备的行为相关的埋点数据，否则就不会上报当前设备的行为埋点数据<br /> |
| setTraceConsoleLog | 是否开启本地打印日志上报功能 | 否 | 当开启后会将应用中打印的日志上报到后台，日志等级对应关系<br />Log.v->ok;Log.i、Log.d->info;Log.e->error;Log.w->warning |
| setNetworkTrace | 是否开启网络追踪功能| 否 | 开启后，可以在 web 中“日志”查看到对应日志的同时也可以在“链路追踪”中查找到对应的链路信息 |
| setTraceType | 设置链路追踪所使用的类型。 | 否 |目前支持 Zipkin 和 Jaeger 两种，默认为 Zipkin |
| setEventFlowLog | 设置是否开启页面事件的日志 | 否 | 可以在 web 版本日志中，查看到对应上报的日志，事件支持启动应用，进入页面，离开页面，事件点击等等 |
| setOnlySupportMainProcess|设置是否只支持在主进程中初始化|否|默认是 true ，默认情况下 SDK 只能在主进程中运行。如果应用中存在多个进程，那么其他进程中将不会执行。如果需要在其他进程中执行需要将该字段设置为 true


####  通过 FTSdk 安装配置项和绑定用户信息

方法说明表

|      方法名       |        含义         | 是否必须 |                  注意                   |
|:--------------:|:-----------------:|:----:|:-------------------------------------:|
|    install     |     安装初始化配置项      |  是   |          在项目 application 中运行          |
|      get       | 获得安装后创建的 FTSdk 对象 |  否   |           应该在 install 运行后调用           |
| unbindUserData |      解绑用户信息       |  否   |    |
|  bindUserData  |      绑定用户信息       |  否   |    |
|    shutDown    |   关闭SDK中正在执行的操作   |  否   |                                       |

#### 通过 FTLogger 类实现主动上报日志

#### 示例代码

``` kotlin
class DemoAplication : Application() {
    private val serverUrl = "serverUrl"
    override fun onCreate() {
        super.onCreate()
        val ftSDKConfig = FTSDKConfig.builder(
            serverUrl//服务器地址
        ).setUseOAID(true)//是否使用OAID
            .setDebug(true)//是否开启Debug模式（开启后能查看调试数据）
            .setXDataKitUUID("ft-dataKit-uuid-001")
            .setEnableTrackAppCrash(true)
            .setEnv(EnvType.GRAY)
            .setTraceType(TraceType.ZIPKIN)
            .setTraceSamplingRate(0.5f);//自动埋点控件白名单
        FTSdk.install(ftSDKConfig)
        

    }
}

```

``` kotlin
//可以在用户登录成功后调用此方法用来绑定用户信息
bind_user.setOnClickListener {
   FTSdk.get().bindUserData("001")
}

//可以在用户退出登录后调用此方法来解绑用户信息
unbind_user.setOnClickListener {
   FTSdk.get().unbindUserData()
}
```


### 关于权限的配置
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

### 初始化类 FTSDK 提供的方法

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
    public void bindUserData(@NonNull String id);

    
    /**
     * 设置开启网络请求追踪
     * @param networkTrace
     */
    public void setNetworkTrace(boolean networkTrace);
}
```

### 配置类 FTSDKConfig 提供的方法

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

}
```

### 监控配置项类 MonitorType

``` java
public class MonitorType {
    //所有配置
    public static int ALL = 1;

    //电池（电池总量、电池使用量）
    public static int BATTERY = 1<<1;

    //内存（内存总量、内存使用率）
    public static int MEMORY = 1<<2;
	//CPU（CPU 占用率）
    public static int CPU = 1<<3;
    //GPS 是否开启
    public static int LOCATION = 1<<7;
    
    //蓝牙
    public static int BLUETOOTH = 1<<10;
   
    //屏幕帧率
    public static int FPS = 1 << 19;
}
```


## 常见问题
### 关于 OAID
####  介绍

 在 Android 10 版本中，非系统应用将不能获取到系统的 IMEI、MAC 等信息。面对该问题移动安全联盟联合国内的手机厂商推出了
补充设备标准体系方案，选择用 OAID 字段作为IMEI等系统信息的替代字段。OAID 字段是由中国信通院联合华为、小米、OPPO、
VIVO 等厂商共同推出的设备识别字段，具有一定的权威性。目前 DataFlux SDK 使用的是 oaid_sdk_1.0.22.aar
关于 OAID 可移步参考[移动安全联盟](http://www.msa-alliance.cn/col.jsp?id=120)

#### 使用

 使用方式和资源下载可参考[移动安全联盟的集成文档](http://www.msa-alliance.cn/col.jsp?id=120)

#### 示例

##### 下载好资源文件后，将 oaid_sdk_1.0.22.aar 拷贝到项目的 libs 目录下，并设置依赖
[获取最新版本](http://www.msa-alliance.cn/col.jsp?id=120)

![Alt](http://zhuyun-static-files-production.oss-cn-hangzhou.aliyuncs.com/helps/markdown-screentshot/ft-sdk-android/use_learn_1.png#pic_center)

##### 将下载的资源中的 supplierconfig.json 文件拷贝到主项目的 assets 目录下，并修改里面对应的内容，特别是需要设置 appid 的部分。需要设置 appid 的部分需要去对应厂商的应用商店里注册自己的 app。

![Alt](http://zhuyun-static-files-production.oss-cn-hangzhou.aliyuncs.com/helps/markdown-screentshot/ft-sdk-android/use_learn_2.png#pic_center)

 ![Alt](http://zhuyun-static-files-production.oss-cn-hangzhou.aliyuncs.com/helps/markdown-screentshot/ft-sdk-android/use_learn_3.png#pic_center)

##### 设置依赖

``` groovy
implementation files('libs/oaid_sdk_1.0.22.arr')
```

##### 混淆设置

```
 -keep class com.bun.miitmdid.core.**{*;}
```

##### 设置 gradle 编译选项，这块可以根据自己的对平台的选择进行合理的配置

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

##### 以上步骤配置完成后，在配置 FT SDK 时调用 FTSDKConfig 的 setUseOAID(true) 方法即可


### 关于崩溃日志中混淆内容转换的问题

#### 问题描述

当你的应用发生崩溃且你已经接入 DataFlux SDK 同时你也开启了崩溃日志上报的开关时，你可以到 DataFlux 后台你的工作空间下的日志模块找到相应
的崩溃日志。如果你的应用开启了混淆，此时你会发现崩溃日志的堆栈信息也被混淆，无法直接定位具体的崩溃位置，因此你需要按以下方式来解决该问题。

#### 解决方式

* 找到 mapping 文件。如果你开启了混淆，那么在打包的时候会在该目录下（module-name -> build -> outputs -> mapping）生成一个混淆文件映射表（mapping.txt）,该文件就是源代码与混淆后的类、方法和属性名称之间的映射。因此每次发包后应该根据应用的版本号保存好对应 mapping.txt 文件，以备根据后台日志 tag 中的版本名字段（app_version_name）来找到对应 mapping.txt 文件。

* 下载崩溃日志文件。到 DataFlux 的后台中把崩溃日志下载到本地，这里假设下载到本地的文件名为 crash_log.txt

* 运行 retrace 命令转换崩溃日志。Android SDK 中自带 retrace 工具（该工具在目录 sdk-root/tools/proguard 下，windows 版本是 retrace.bat,Mac/Linux 版本是 retrace.sh），通过该工具可以恢复崩溃日志的堆栈信息。命令示例

  ```
  retrace.bat -verbose mapping.txt crash_log.txt
  ```

* 上一步是通过 retrace 命令行来执行，当然也可以通过 GUI 工具。在 <sdk-root>/tools/proguard/bin 目录下有个 proguardgui.bat 或 proguardgui.sh GUI 工具。运行 proguardgui.bat 或者 ./proguardgui.sh -> 从左侧的菜单中选择“ReTrace” -> 在上面的 Mapping file 中选择你的 mapping 文件，在下面输入框输入要还原的代码 ->点击右下方的“ReTrace!”

