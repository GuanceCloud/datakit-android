# plugin 1.3.0
1. 支持 datakit source map 自动上传，支持 native symbol 的上传
2. 支持捕获 Application 冷热启动，Activity 页面跳转，View、ListView、Dialog、Tab 点击事件。
3. 支持 Webview Js 监听方法的写入
4. 支持 Okhttp Trace 和 Resource 数据自动写入
5. 支持 Gradle 8.0,AGP 8.0 
6. 支持 IgnoreAOP 忽略标记
7. 支持兼容阿里云热修复框架

---
# plugin 1.3.0-beta01
1. plugin 1.2.2-alpha02

---
# plugin 1.2.2-alpha02
1. 适配 datakit 新 sourcemap 规则适配

---
# plugin 1.2.2-beta01
1. 同 plugin 1.2.2-alpha01

---
# plugin 1.2.2-alpha01
1. 修正 flavor 设置后，无法上传 native symbol 的问题
2. 兼容 AGP 8.0 不生成 /intermediates/cmake/debug/obj 路径的问题

---
# plugin 1.2.1-beta01
1. 修正 sourceMap 上传发生错误的问题

---
# plugin 1.2.1-alpha01
1. 适配阿里云 Sophix 热修复集成后无法采集 View 数据的问题

---
# plugin 1.2.0-beta03
1. 兼容 Java 11 版本

---
# plugin 1.2.0-beta02
1. 修正 application uuid，plugin 版本不生成问题
2. 优化性能

---

# plugin 1.2.0-beta01
1. plugin 1.2.0-alpha01

---
# plugin 1.2.0-alpha01
1. Gradle 8.0,AGP 8.0 适配

---
# plugin 1.1.4-alpha01
1. 支持 Annotation IgnoreAOP

---
# plugin 1.1.3-beta01
1. 优化 com.google.android.material:material 控件调用

---
# plugin 1.1.2-beta01
1. plugin 1.1.2-alpha01，plugin 1.1.1-alpha04 合并

---
# plugin 1.1.2-alpha01
1. 修正 WebView 衍生类注入后，发生循环调用的问题

---
# plugin 1.1.1-alpha04
1. 修正 cmake path 主项目错误的问题
2. 添加 proguard 支持

---
# plugin 1.1.1-alpha03
1. 优化FtExt 条件覆盖规则

---
# plugin 1.1.1-alpha02
1. 支持 productFlavor 配置

---
# plugin 1.1.1-alpha01
1. 添加 native symbol 文件 mapping 文件打包功能

---
# plugin 1.1.0-alpha02
1. 修正 MultiDex 库不兼容的问题

---
# plugin 1.1.0-alpha01
1. application 启动事件捕捉
2. 兼容版本规则修改

---
# plugin 1.0.1-beta01
1. 修复版本比较问题

---
# plugin 1.0.1-alpha12
1. 添加 trace 功能

