# agent 1.6.1-beta02
1. 修正 Log link RUM 动态 tag 缺失问题

---
# agent 1.6.1-beta01
1. 修改动态标签覆盖逻辑
2. 简化指标监控逻辑
3. 添加清理缓存数据逻辑

---
# agent 1.6.1-alpha04
1. 修复 RUM 单独调用自定义 startView，导致监控指标 FTMetricsMTR 线程未被回收的问题

---
# agent 1.6.1-alpha03
1. 优化动态 tags 赋值的时机
2. 添加全局、log、RUM globalContext 属性动态设置方式

---
# agent 1.6.0
1. 优化数据存储和同步性能
（旧版本升级至 1.6.0 需要配置 FTSDKConfig.setNeedTransformOldCache 进行旧数据兼容同步）
2. 处理在使用 ft-plugin 时，调用 Log.w(String,Throwable) 引发异常的问题

---
# agent 1.6.0-beta01
1. 同 1.6.0-alpha02
2. 优化 Inner Log 缓存清理规则
3. 优化行协议转化存储规则

---
# agent 1.6.0-alpha02
1. 修正日志丢弃策略
2. 优化全局 json 数据存储

---
# agent 1.6.0-alpha01
1. 优化数据序列化存储规则
2. 恢复数据库落盘
3. 1.5.2 功能合并

---
# agent 1.5.3-alpha01
1. 处理在使用 ft-plugin 时，调用 Log.w(String,Throwable) 引发异常的问题

---
# agent 1.5.2
1. Error network_error 添加本地网络错误类型的提示，用于补充说明 Resource 数据中 resource_status=0 场景
2. 修复 setEnableTrackAppCrash(false) 时 uncaughtException rethrow 传递问题

---
# agent 1.5.2-beta03
1. 同 agent 1.5.2-alpha03

---
# agent 1.5.2-alpha03
 1. 修复 setEnableTrackAppCrash(false) 时 uncaughtException rethrow 传递问题

---
# agent 1.5.2-beta02
1. 补充 resource 本地网络错误的简单描述

---
# agent 1.5.2-beta01
1. 同 agent 1.5.2-alpha02

---
# agent 1.5.2-alpha02
1. resource 添加本地网络错误类型的提示

---
# agent 1.5.1
1. Java Crash 及 ANR 补充其他线程代码堆栈
2. Java Crash，Native Crash，ANR 添加附加 logcat 配置功能
3. 修复长 session 且无 action 更新场景下，频繁更新 session_id 的问题

---
# agent 1.5.1-beta02
1. 添加 logcat Native Crash 和 ANR 配置功能
2. logcat 输出格式优化

---
# agent 1.5.1-beta01
1. 同 agent 1.5.1-alpha03

---
# agent 1.5.1-alpha03
1. ANR 及 Java Crash 补充其他线程代码堆栈
2. Java Crash 添加附加 logcat 功能
3. 处理 session_id 刷新偶现异常问题

---
# agent 1.5.1-alpha02
1. 优化长时间场景下 session_id 刷新机制

---
# agent 1.5.1-alpha01
1. 修复长 session 且无 action 更新场景下，频繁更新 session_id 的问题

---
# agent 1.5.0
1. RUM resource 网络请求添加 remote ip 地址解析功能
2. 修复开启 RUM SampleRate 后，高并发网路请求引发的数组线程安全问题
3. ConnectivityManager.registerDefaultNetworkCallback 方法容错优化
4. 添加行协议 Integer 数据兼容模式，处理 web 数据类型冲突问题
5. 自动采集 Action click 中控件资源名 id 获取优化
6. SDK config 配置读取异常问题容错优化

---
# agent 1.5.0-beta01
1. 同 agent 1.5.0-alpha04

---
# agent 1.5.0-alpha04
1. RUM Log Trace config 读取临界场景容错处理 

---
# agent 1.5.0-alpha03
1. RUM resource 网络请求添加 remote ip 地址解析功能
2. 添加行协议 Integer 数据兼容模式，处理 web 数据类型冲突问题

---
# agent 1.4.7-alpha01
1. 修复开启 RUM SampleRate 后，高并发网路请求引发的数组线程安全问题
2. ConnectivityManager.registerDefaultNetworkCallback 方法容错优化
3. 自动采集 Action click 中控件资源名 id 获取优化

---
# agent 1.4.6
1. SDK 初始化容错优化
2. 新增日志新增 Status.Debug 类型
3. 控制台抓取日志等级对应关系调整： Log.i -> info，Log.d -> debug
4. FTLogger 自定义日志支持自定义 status 字段

---
# agent 1.4.6-beta01
1. 同 agent 1.4.6-alpha02

---
# agent 1.4.6-alpha02
1. 新增日志新增 Status.Debug 类型
2. FTLogger 自定义日志支持自定义 status 字段

---
# agent 1.4.6-alpha01
1. SDK 初始化容错优化

---
# agent 1.4.5
1. 重复初始化兼容优化处理
2. 优化 c/c++ 崩溃采集数据同步逻辑，避免在某些场景下意外中断退出从而导致死锁
3. 优化 startAction Property 属性写入逻辑，避免发生线程安全访问问题

---
# agent 1.4.5-beta01
1. 重复初始化兼容优化处理
2. 优化 c/c++ 崩溃采集数据同步逻辑，避免在某些场景下意外中断退出从而导致死锁
3. 优化 startAction Property 属性写入逻辑，避免发生线程安全访问问题

---
# agent 1.4.4
1. 数据库链接容错保护
2. 修正 setOnlySupportMainProcess true 时，子进程配置部份不起效问题
3. 修正 RUM 不开启 View 采集, Crash 不会 rethrow 的问题

---
# agent 1.4.4-beta01
1. 修正 RUM 不开启 View 采集, Crash 不会 rethrow 的问题 

---
# agent 1.4.4-alpha02
1. 修正 setOnlySupportMainProcess true 时，子进程配置部份不起效问题

---
# agent 1.4.4-alpha01
1. 数据库链接容错保护

---
# agent 1.4.3
1. 支持 Dataway 与 Datakit 的地址上传
2. 支持发送 Action，View，Resource，LongTask，Error 类型的 RUM 数据。
   * View，Action 页面跳转，控件点击操作自动采集，需要使用 ft-plugin
   * Resource，自动采集，仅支持 Okhttp，并需要使用 ft-plugin
   * Error 中的 Native Crash 和 ANR 需要使用 ft-native
3. 支持发送 Log 数据，控制台自动写入，需要使用 ft-plugin
4. 链路 http header propagation，仅支持 Okhttp，并需要使用 ft-plugin
5. 支持数据同步参数配置，请求条目数据，同步间歇时间，以及日志缓存条目数。
6. 支持 SDK 内部日志转化为文件

---
# agent 1.4.3-beta04
1. 日志高速缓存策略判断条件优化

---
# agent 1.4.3-beta03
1. Action,View 数据生成方式优化
2. 优化 SDK 初始化

---
# agent 1.4.3-beta02
1. 崩溃时尝试 stopView 补充页面停留时间
2. 数据同步请求超时时间，响应时间延长
3. 内部日志输出优化
4. 修正日志丢弃旧数据策略，表名错误的问题

---
# agent 1.4.3-beta01
1. 内部日志写文件方法 try-with-resources 保护
2. agent 1.4.3-alpha01，agent 1.4.3-alpha02，agent 1.4.3-alpha03

---
# agent 1.4.3-alpha03
1. 传输性能优化
2. aar 添加 javadoc 和 source jar
3. 内部文件日志管理优化

---
# agent 1.4.3-alpha02
1. 修正 Native Library 版本号获取方式

---
# agent 1.4.3-alpha01
1. Native Crash 捕获时机优化，会尝试在崩溃当下捕获
2. Java Crash 在崩溃入库后，再进行 rethrow 

---
# agent 1.4.2-alpha04
1. 新增同步请求间歇时长设置

---
# agent 1.4.2-alpha03
1. 新增内部日志转文件方法

---
# agent 1.4.2-alpha02
1. 添加日志最大缓存条目数量限制

---
# agent 1.4.2-alpha01
1. 添加自动同步开关，支持手动上传数据
2. 添加 arch cpu abi 架构字段显示
3. 支持修改同步请求单次请求数据

---
# agent 1.4.1-beta01
1. agent 1.4.1-alpha01, agent 1.4.1-alpha02，agent 1.4.1-alpha03
2. 调整 longtask 发生时间点

---
# agent 1.4.1-alpha03
1. webview 时间精度问题适配
2. skywalking propagation header service 参数调整 

---
# agent 1.4.1-alpha02
1. 新增 ANR Error 事件和 ANR 引起崩溃的日志
2. 修复 Okhttp 中使用加密算法，单纯依赖 ASM 写入，resource 不发送的问题
3. 支持自定义 Resource 内容，与 FTTraceConfig.enableAutoTrace，FTRUMConfig.enableTraceUserResource 同时开启

---
# agent 1.4.1-alpha01
1. 修复由于超长 session 导致的 view 展示重复数据的问题
2. 修复未结束 resource 导致重复传 view 数据的问题
3. 修改无 userid 绑定随机 uuid 算法
4. 新增 view_update_time 自增数，解决数据并发写入次序问题 
5. 优化数据同步

---
# agent 1.4.0-beta01
1. 优化数据同步机制
2. 优化内部日志输出
3. 支持公网 Dataway 上传
4. 支持自定义 error 数据中的 error_type 字段属性 

---
# agent 1.3.17-alpha05
1. 优化日志输出
2. add Resource 空指针数据处理

---
# agent 1.3.17-alpha04
1. 优化重试机制
2. 新增 addResource header map 支持

---
# agent 1.3.17-alpha03
1. 添加数据同步最大同步次数
2. 添加内部日志接管对象

---
# agent 1.3.17-beta01
1. 同 agent 1.3.17-alpha01，agent 1.3.17-alpha02
2. 新增自定义 TraceHeader

---
# agent 1.3.17-alpha02
1. 支持 resource 自定义数据方式

---
# agent 1.3.17-alpha01
1. 优化 resource body size 计算逻辑

---
# agent 1.3.16-beta03
1. 调整 Open API 结构
2. Resource 本地错误逻辑添加

---
# agent 1.3.16-beta02
1. 优化 SDK 关闭
2. 同 agent 1.3.16-alpha05

---
# agent 1.3.16-alpha05
1. READ_PHONE_STATE 权限依赖关系弱化，优化无 READ_PHONE_STATE 错误提示

---
# agent 1.3.16-alpha04
1. SDK 延迟加载异常处理

---
# agent 1.3.16-beta01
1. 弱化 service process SDK 初始化错误的提示

---
# agent 1.3.16-alpha02
1. tag，field 为空时，规则修改

---
# agent 1.3.16-alpha01
1. 优化 AOP MenuItem 点击生成 ActionName 的描述 

---
# agent 1.3.15-beta01
1. 同 1.3.15-alpha03

---
# agent 1.3.15-alpha03
1. 修改 webview js 调用 service 赋值

---
# agent 1.3.15-alpha02
1. 修改 webview js 回掉上传逻辑

---
# agent 1.3.15-alpha01
1. 适配阿里云 Sophix 热修复集成后无法采集 View 数据的问题

---
# agent 1.3.14-beta01
1. 同 1.3.13-alpha01
2. 添加自定义日志打印到控制台的功能

---
# agent 1.3.13-alpha01
1. 添加自定义 env

---
# agent 1.3.12-beta01
1. 修正 3.12.0 okhttp 依赖 resource 某些情况下，不发送的问题

---
# agent 1.3.12-alpha01
1. 优化 TraceHeader 逻辑

---
# agent 1.3.11-beta02
1. 修正查看器 Fetch/XHR 数量统计不正确的问题

---
# agent 1.3.11-beta01
1. 同 agent 1.3.11-alpha01，agent 1.3.11-alpha02

---
# agent 1.3.11-alpha02
1. 修正编译器部分方法 index 报错，但是可以正常编译的问题

---
# agent 1.3.11-alpha01
1. 优化测试日志输出
2. 添加 AOP Ignore 方式
3. 修正 Resource 数据丢失问题

---
# agent 1.3.10-beta01
1. 调整日志 Tag

---
# agent 1.3.10-alpha01
1. AGP 3.2.0 以下适配，去掉 lambda 语法

---
# agent 1.3.9-beta02
1. 优化 DeviceMetricsMonitorType，ErrorMonitorType Config 传参数

---
# agent 1.3.9-beta01
1. 测试用例适配优化
2. 补充文档注释

---
# agent 1.3.9-alpha02
1. 优化对 com.google.android.material:material 的依赖

---
# agent 1.3.9-alpha01
1. 添加 View，Action，Resource，Error，LongTask，Log 可以使用扩展参数

---
# agent 1.3.8-beta03
1. 修改 DDtrace Header Propagation 规则

---
# agent 1.3.8-beta02
1. 修正冷启动时间在跨平台 SDK 应用错误的问题

---
# agent 1.3.8-beta01
1. 同 agent 1.3.8-alpha05

---
# agent 1.3.8-alpha05
1. 启动时间统计优化
2. Android ID 获取逻辑

---
# agent 1.3.8-alpha04
1. 调整 MonitorType 变量为 Enum 类型

---
# agent 1.3.8-alpha03
1. 调整 user ext 设置类型

---
# agent 1.3.8-alpha02
1. 修正观测云 js 与 webview 回调部分不正确的问题

---
# agent 1.3.8-alpha01
1. 扩充用户数据绑定字段
2. 放开 FTAutoTrack WebView 设置

---
# agent 1.3.7-beta01
1. agent 1.3.7-alpha04 相同

---
# agent 1.3.7-alpha04
1. agent 1.3.6-beta05，agent 1.3.6-beta06 功能合并
2. 修改电池最大值错误

---
# agent 1.3.7-alpha03
1. 修改 CPU 指标规则

---
# agent 1.3.7-alpha02
1. 修正冷启动统计时间规则

---
# agent 1.3.7-alpha01
1. 添加电池、内存、CPU、FPS 监控数据支持

---
# agent 1.3.6-beta06
1. 修改 session 时间统计规则

---
# agent 1.3.6-beta05
1. 优化冷启动规则

---
# agent 1.3.6-beta04
1. 优化 action 统计

---
# agent 1.3.6-beta03
1. 优化冷启动时间统计

---
# agent 1.3.6-beta02
1. 添加 resource 空值补充逻辑

---
# agent 1.3.6-bete01
1. agent 1.3.6-alpha01，1.3.6-alpha02 更新内容

---
# agent 1.3.6-alpha02
1. 优化 action 时间统计

---
# agent 1.3.6-alpha01
1. application code start 统计修正
2. tcp,dns 指标异常修复
3. long task 统计优化

---
# agent 1.3.5-beta02
1. 修复 view rum config 配置问题

---
# agent 1.3.5-beta01
1. 同 agent 1.3.5-alpha01

---
# agent 1.3.5-alpha01
1.修复 OkHttp 3.12 兼容崩溃问题

---
# agent 1.3.4-beta01
1. 删减冗余代码
2. 提升测试用例覆盖率

---
# agent 1.3.4-alpha04
1. 去掉 config action 与应用登录的关联

---
# agent 1.3.4-alpha03
1. 分支合并更新

---
# agent 1.3.4-alpha02
1. 修正已close action 还会被篡改 duration 的问题

---
# agent 1.3.4-alpha01
1. 启动事件计算规则修改
2. RUM 页面 viewReferrer 记录规则修改

---
# agent 1.3.3-alpha05
1. 修正 ID 溢出问题

---
# agent 1.3.3-alpha04
1. 修正 DDTrace ID 算法

---
# agent 1.3.3-alpha02
1. 省去多余 Trace 配置

---
# agent 1.3.3-alpha01
1. 支持 Skywalking 、W3c TraceParent、
2. Zipkin 添加 single header 支持

---
# agent 1.3.2-alpha02
1. 修正 Trace 和 log tags底层相互污染的问题

---
# agent 1.3.2-alpha01
1. 添加 Trace Log 和 全局 globalContext 的功能

---
# agent 1.3.1-alpha15
1. 修正 trace 发挥 400 以上错误时，ResponseBody 无法  read 的问题

---
# agent 1.3.1-alpha14
1. 修复单个 Trace 数据不触发同步的问题

---
# agent 1.3.1-alpha13
1. 优化 Trace resource 调度逻辑，去掉次序调用要求

---
# agent 1.3.1-alpha12
1. resource,trace 自动抓取数据逻辑判定优化

---
# agent 1.3.1-alpha11
1. 优化线程池嵌套问题

---
# agent 1.3.1-alpha10
1. 修正线程池死锁问题

---
# agent 1.3.1-alpha09
1. 修正线程池核心数

---
# agent 1.3.1-alpha08
1. 对未初始化情况进行崩溃保护

---
# agent 1.3.1-alpha07
1. 处理偶现崩溃问题

---
# agent 1.3.1-alpha06
1. 更改 trace 自动配置设定

---
# agent 1.3.1-alpha05
1. 调整方法可见修饰
2. 添加方法注释

---
# agent 1.3.1-alpha04
1. 优化 AddError

---
# agent 1.3.1-alpha03
1. 用户行为追踪逻辑调整
2. RUM 开放 LongTask 接口

---
# agent 1.3.1-alpha02
1. 恢复 url path encode 显示方式

---
# agent 1.3.1-alpha01
1. RUM、TRACE 接口开放
2. 添加第三方库判断

---
# agent 1.2.4-alpha02
1. 行协议添加 escape 规则

---
# agent 1.2.4-alpha01
1. 调整 RUM resource 统计算法

---
# agent 1.2.3-alpha03
1. 修改自定义标签方法

---
# agent 1.2.3-alpha02
1. 修正 Trace 功能中 SpanId 和 TraceId 错误逻辑

---
# agent 1.2.3-alpha01
1. 增加 RUM 添加自定义标签功能

---
# agent 1.2.2-alpha01
1. 添加日志等级过滤功能
2. 添加控制台日志前缀过滤功能
3. 移除多余的权限声明

---
# agent 1.2.1-alpha01
1. 添加日志等级过滤功能
2. 添加控制台日志前缀过滤功能
3. 移除多余的权限声明

---
# agent 1.2.0-alpha06
1. 适配 okhttp3 3.12.0 版本

---
# agent 1.2.0-alpha05
1. 修改 UIBlock 错误条件读取问题
2. 优化 UIBlock 算法

---
# agent 1.2.0-alpha04
1. action viewName 错误为题修复
2. Tab 点击描述优化

---
# agent 1.2.0-alpha03
1. action id 判断问题修复
2. click 事件部分 action_name 错误的问题

---
# agent 1.2.0-alpha02
1. 修改 action_name 描述
2. 修正 action 时间统计错误的问题

---
# agent 1.2.0-alpha01
1. 新增日志链路关联 RUM 功能
2. 新增 DDtrace 链路类型
3. 重新规划 RUM Trace Log 的配置

---
# agent 1.1.0-alpha11
1. 补充 resource 缺失字段

---
# agent 1.1.0-alpha10
1. 修正快速切换页面统计错误的问题
2. 修正统计错位的问题

---
# agent 1.1.0-alpha09
1. 修正统计偏差的问题

---
# agent 1.1.0-alpha08
1. 修改 response size 统计

---
# agent 1.1.0-alpha07
1. 优化 action duration 统计

---
# agent 1.1.0-alpha06
1. 修正长任务计算问题
2. 去掉 launch action viewid 属性

---
# agent 1.1.0-alpha04
1. resource count 统计优化
2. duration action 逻辑添加

---
# agent 1.1.0-alpha03
1. trace resource 属性添加
2. user action 配置添加
---
# agent 1.1.0-alpha02
1. 优化 action 统计算法
2. 修改长任务时间计算

---
# agent 1.1.0-alpha01
1. rum 添加 action，session，view 统计功能

---
# agent 1.0.4-alpha17
1. 补充缺失字段

---
# agent 1.0.4-alpha16
1. 进行字段替换

---
# agent 1.0.4-alpha15
1. 修正设备机型空格转译的错误

---
# agent 1.0.4-alpha14
1. 进一步优化页面时间统计的算法

---
# agent 1.0.4-alpha13
1. 修正无网络产生死锁卡顿问题
2. 优化应用启动的时间统计

---
# agent 1.0.4-alpha12
1. 优化数据数据组装，缩减传输数据

---
# agent 1.0.4-alpha09
1. 修正 Trace 地址和传输数据结构问题

---
# agent 1.0.4-alpha06
1. 修正暴露的方法

---
# agent 1.0.4-alpha04
1. 数据结构调整，优化性能
2. 提供时间的精确度

---
# agent 1.0.4-alpha03
1. 缩减功能
2. 添加 RUM 数据输出

---
# agent 1.0.4-alpha02
1. 修复 Object 输出数据问题
2. 优化测试用例

---
# agent 1.0.4-alpha01
1. 添加网络响应时间统计
2. 添加崩溃，卡顿，ANR 统计

---
# agent 1.0.3-beta01
1. 修正若干错误问题，发布稳定版本

---
# agent 1.0.3-alpha12
1. 应用启动上报一条对象数据

---
# agent 1.0.3-alpha09
1. 修正 log 日志格式问题

---
# agent 1.0.3-alpha01
1. 添加 trace 功能
2. 添加服务重试机制
3. 添加日志上报功能
4. 添加事件 Flow 日志功能

---
# agent 1.0.2-alpha14
1. 添加页面别名和事件别名

---
# agent 1.0.2-alpha01
1. 独立监控模块，提供周期上报功能

---
# agent 1.0.1-alpha10
1. 上报流程图
2. 修改监控项指标集

---
# agent 1.0.0
1. 用户自定义埋点
2. FT Gateway 数据同步