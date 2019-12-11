# FT Mobile SDK Android

## 安装
在项目的app模块(主模块)的build.gradle文件中添加FT SDK的依赖
```
dependencies {
    implementation 'com.cloudcare.ft.mobile.sdk.traker.agent:ft-sdk:1.0.0'
}
```
关于最新的版本号，请参考[更新文档](https://gitlab.jiagouyun.com/cma/ft-sdk-android/blob/master/README.md)
    
## 配置
在程序的入口Application中添加关于FT SDK的初始化配置安装代码。
关于配置项的说明
参数|类型|含义|是否必须
:--:|:--:|:--:|:--:
metricsUrl|String|FT-GateWay metrics 写入地址|是
enableRequestSigning|boolean|配置是否需要进行请求签名|是
akId|String|access key ID|enableRequestSigning 为 true 时，必须要填
akSecret|String|access key Secret|enableRequestSigning 为 true 时，必须要填
useOAID|boolean|是否使用oaid字段[了解OAID](#about_oaid)|否

示例代码
```
public class DemoApplication extends Application {
    private String accesskey_id = "xxxx";
    private String accessKey_secret = "xxxxx";

    public DemoApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FTSDKConfig ftSDKConfig = new FTSDKConfig("http://xxxxx",
                true,
                accesskey_id,
                accessKey_secret);
        ftSDKConfig.setUseOAID(true);
        FTSdk.install(ftSDKConfig);
    }
}
```

## 方法
1、FT SDK公开了3个埋点方法，用户通过这三个方法可以主动在需要的地方实现埋点，然后将数据上传到服务端。

- 方法一：

```
/*** 主动埋点
 * @param event 埋点事件名称
 * @param tags 埋点数据
 * @param values 埋点数据
 */
 public void track(String event, JSONObject tags, JSONObject values)
```

- 方法二：

```
/**
 * 主动埋点
 * @param event 埋点事件名称
 * @param tags 埋点数据
 */
 public void trackTags(String event, JSONObject tags)
```

- 方法三：

```
/**
 * 主动埋点
 * @param event 埋点事件名称
 * @param values 埋点数据
 */
 public void trackValues(String event, JSONObject values)
```

2、方法使用示例
```
public void clickText(View view) {
    try {
        JSONObject tags = new JSONObject();
        JSONObject values = new JSONObject();
        tags.put("view","text");
        values.put("userName","admin");
        FTTrack.getInstance().track("ClickView",tags,values);
    } catch (JSONException e) {
        e.printStackTrace();
    }
}
```


## 常见问题
#### 1.<span id="about_oaid">关于OAID</span>
- 介绍
在Android 10版本中，非系统应用将不能获取到系统的IMEI、MAC等信息。面对该问题移动安全联盟联合国内的手机厂商推出了
补充设备标准体系方案，选择用OAID字段作为IMEI等系统信息的替代字段。OAID 字段是由中国信通院联合华为、小米、OPPO、
VIVO 等厂商共同推出的设备识别字段，具有一定的权威性。
关于OAID可移步参考[移动安全联盟](http://www.msa-alliance.cn/col.jsp?id=120)
- 使用
使用方式和资源下载可参考[移动安全联盟的集成文档](http://www.msa-alliance.cn/col.jsp?id=120)
集成了OAID SDK后便可以在初始化FT SDK时设置配置参数useOAID=true