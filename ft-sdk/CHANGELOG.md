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
1.webview 时间精度问题适配
2.skywalking propagation header service 参数调整 

---
# agent 1.4.1-alpha02
1.新增 ANR Error 事件和 ANR 引起崩溃的日志
2.修复 Okhttp 中使用加密算法，单纯依赖 ASM 写入，resource 不发送的问题
3.支持自定义 Resource 内容，与 FTTraceConfig.enableAutoTrace，FTRUMConfig.enableTraceUserResource 同时开启

---
# agent 1.4.1-alpha01
1.修复由于超长 session 导致的 view 展示重复数据的问题
2.修复未结束 resource 导致重复传 view 数据的问题
3.修改无 userid 绑定随机 uuid 算法
4.新增 view_update_time 自增数，解决数据并发写入次序问题 
5.优化数据同步

---
# agent 1.4.0-beta01
1.优化数据同步机制
2.优化内部日志输出
3.支持公网 Dataway 上传
4.支持自定义 error 数据中的 error_type 字段属性 

---
# agent 1.3.17-alpha05
1.优化日志输出
2.add Resource 空指针数据处理

---
# agent 1.3.17-alpha04
1.优化重试机制
2.新增 addResource header map 支持

---
# agent 1.3.17-alpha03
1.添加数据同步最大同步次数
2.添加内部日志接管对象

---
# agent 1.3.17-beta01
1.同 agent 1.3.17-alpha01，agent 1.3.17-alpha02
2.新增自定义 TraceHeader

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
1.SDK 延迟加载异常处理

---
# agent 1.3.16-beta01
1.弱化 service process SDK 初始化错误的提示

---
# agent 1.3.16-alpha02
1.tag，field 为空时，规则修改

---
# agent 1.3.16-alpha01
1.优化 AOP MenuItem 点击生成 ActionName 的描述 

---
# agent 1.3.15-beta01
1.同 1.3.15-alpha03

---
# agent 1.3.15-alpha03
1.修改 webview js 调用 service 赋值

---
# agent 1.3.15-alpha02
1.修改 webview js 回掉上传逻辑

---
# agent 1.3.15-alpha01
1.适配阿里云 Sophix 热修复集成后无法采集 View 数据的问题

---
# agent 1.3.14-beta01
1.同 1.3.13-alpha01
2.添加自定义日志打印到控制台的功能

---
# agent 1.3.13-alpha01
1.添加自定义 env

---
# agent 1.3.12-beta01
1.修正 3.12.0 okhttp 依赖 resource 某些情况下，不发送的问题

---
# agent 1.3.12-alpha01
1.优化 TraceHeader 逻辑

---
# agent 1.3.11-beta02
1.修正查看器 Fetch/XHR 数量统计不正确的问题

---
# agent 1.3.11-beta01
1.同 agent 1.3.11-alpha01，agent 1.3.11-alpha02

---
# agent 1.3.11-alpha02
1.修正编译器部分方法 index 报错，但是可以正常编译的问题

---
# agent 1.3.11-alpha01
1.优化测试日志输出
2.添加 AOP Ignore 方式
3.修正 Resource 数据丢失问题

---
# agent 1.3.10-beta01
1.调整日志 Tag

---
# agent 1.3.10-alpha01
1.AGP 3.2.0 以下适配，去掉 lambda 语法

---
# agent 1.3.9-beta02
1.优化 DeviceMetricsMonitorType，ErrorMonitorType Config 传参数

---
# agent 1.3.9-beta01
1.测试用例适配优化
2.补充文档注释

---
# agent 1.3.9-alpha02
1.优化对 com.google.android.material:material 的依赖

---
# agent 1.3.9-alpha01
1.添加 View，Action，Resource，Error，LongTask，Log 可以使用扩展参数

---
# agent 1.3.8-beta03
1.修改 DDtrace Header Propagation 规则

---
# agent 1.3.8-beta02
1.修正冷启动时间在跨平台 SDK 应用错误的问题

---
# agent 1.3.8-beta01
1.同 agent 1.3.8-alpha05

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
1.agent 1.3.6-beta05，agent 1.3.6-beta06 功能合并
2.修改电池最大值错误

---
# agent 1.3.7-alpha03
1.修改 CPU 指标规则

---
# agent 1.3.7-alpha02
1.修正冷启动统计时间规则

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
1.修正已close action 还会被篡改 duration 的问题

---
# agent 1.3.4-alpha01
1.启动事件计算规则修改
2.RUM 页面 viewReferrer 记录规则修改

---
# agent 1.3.3-alpha05
1.修正 ID 溢出问题

---
# agent 1.3.3-alpha04
1.修正 DDTrace ID 算法

---
# agent 1.3.3-alpha02
1.省去多余 Trace 配置

---
# agent 1.3.3-alpha01
1.支持 Skywalking 、W3c TraceParent、
2.Zipkin 添加 single header 支持

---
# agent 1.3.2-alpha02
1.修正 Trace 和 log tags底层相互污染的问题

---
# agent 1.3.2-alpha01
1.添加 Trace Log 和 全局 globalContext 的功能

---
# agent 1.3.1-alpha15
1.修正 trace 发挥 400 以上错误时，ResponseBody 无法  read 的问题

---
# agent 1.3.1-alpha14
1.修复单个 Trace 数据不触发同步的问题

---
# agent 1.3.1-alpha13
1.优化 Trace resource 调度逻辑，去掉次序调用要求

---
# agent 1.3.1-alpha12
1.resource,trace 自动抓取数据逻辑判定优化

---
# agent 1.3.1-alpha11
1.优化线程池嵌套问题

---
# agent 1.3.1-alpha10
1.修正线程池死锁问题

---
# agent 1.3.1-alpha09
1.修正线程池核心数

---
# agent 1.3.1-alpha08
1.对未初始化情况进行崩溃保护

---
# agent 1.3.1-alpha07
1.处理偶现崩溃问题

---
# agent 1.3.1-alpha06
1.更改 trace 自动配置设定

---
# agent 1.3.1-alpha05
1.调整方法可见修饰
2.添加方法注释

---
# agent 1.3.1-alpha04
1. 优化 AddError

---
# agent 1.3.1-alpha03
1. 用户行为追踪逻辑调整
2. RUM 开放 LongTask 接口

---
# agent 1.3.1-alpha02
1.恢复 url path encode 显示方式

---
# agent 1.3.1-alpha01
1.RUM、TRACE 接口开放
2.添加第三方库判断

---
# agent 1.2.4-alpha02
1.行协议添加 escape 规则

---
# agent 1.2.4-alpha01
1.调整 RUM resource 统计算法

---
# agent 1.2.3-alpha03
1.修改自定义标签方法

---
# agent 1.2.3-alpha02
1.修正 Trace 功能中 SpanId 和 TraceId 错误逻辑

---
# agent 1.2.3-alpha01
1.增加 RUM 添加自定义标签功能

---
# agent 1.2.2-alpha01
1.添加日志等级过滤功能
2.添加控制台日志前缀过滤功能
3.移除多余的权限声明

---
# agent 1.2.1-alpha01
1.添加日志等级过滤功能
2.添加控制台日志前缀过滤功能
3.移除多余的权限声明

---
# agent 1.2.0-alpha06
1.适配 okhttp3 3.12.0 版本

---
# agent 1.2.0-alpha05
1.修改 UIBlock 错误条件读取问题
2.优化 UIBlock 算法

---
# agent 1.2.0-alpha04
1.action viewName 错误为题修复
2.Tab 点击描述优化

---
# agent 1.2.0-alpha03
1.action id 判断问题修复
2.click 事件部分 action_name 错误的问题

---
# agent 1.2.0-alpha02
1.修改 action_name 描述
2.修正 action 时间统计错误的问题

---
# agent 1.2.0-alpha01
1.新增日志链路关联 RUM 功能
2.新增 DDtrace 链路类型
3.重新规划 RUM Trace Log 的配置

---
# agent 1.1.0-alpha11
1.补充 resource 缺失字段

---
# agent 1.1.0-alpha10
1.修正快速切换页面统计错误的问题
2.修正统计错位的问题

---
# agent 1.1.0-alpha09
1.修正统计偏差的问题

---
# agent 1.1.0-alpha08
1.修改 response size 统计

---
# agent 1.1.0-alpha07
1.优化 action duration 统计

---
# agent 1.1.0-alpha06
1.修正长任务计算问题
2.去掉 launch action viewid 属性

---
# agent 1.1.0-alpha04
1.resource count 统计优化
2.duration action 逻辑添加

---
# agent 1.1.0-alpha03
1.trace resource 属性添加
2.user action 配置添加
---
# agent 1.1.0-alpha02
1.优化 action 统计算法
2.修改长任务时间计算

---
# agent 1.1.0-alpha01
1.rum 添加 action，session，view 统计功能

---
# agent 1.0.4-alpha17
1.补充缺失字段

---
# agent 1.0.4-alpha16
1.进行字段替换

---
# agent 1.0.4-alpha15
1.修正设备机型空格转译的错误

---
# agent 1.0.4-alpha14
1.进一步优化页面时间统计的算法

---
# agent 1.0.4-alpha13
1.修正无网络产生死锁卡顿问题
2.优化应用启动的时间统计

---
# agent 1.0.4-alpha12
1.优化数据数据组装，缩减传输数据

---
# agent 1.0.4-alpha09
1.修正 Trace 地址和传输数据结构问题

---
# agent 1.0.4-alpha06
1.修正暴露的方法

---
# agent 1.0.4-alpha04
1.数据结构调整，优化性能
2.提供时间的精确度

---
# agent 1.0.4-alpha03
1.缩减功能
2.添加 RUM 数据输出

---
# agent 1.0.4-alpha02
1.修复 Object 输出数据问题
2.优化测试用例

---
# agent 1.0.4-alpha01
1.添加网络响应时间统计
2.添加崩溃，卡顿，ANR 统计

---
# agent 1.0.3-beta01
1.修正若干错误问题，发布稳定版本

---
# agent 1.0.3-alpha12
1.应用启动上报一条对象数据

---
# agent 1.0.3-alpha09
1.修正 log 日志格式问题

---
# agent 1.0.3-alpha01
1.添加 trace 功能
2.添加服务重试机制
3.添加日志上报功能
4.添加事件 Flow 日志功能

---
# agent 1.0.2-alpha14
1.添加页面别名和事件别名

---
# agent 1.0.2-alpha01
1.独立监控模块，提供周期上报功能

---
# agent 1.0.1-alpha10
1.上报流程图
2.修改监控项指标集

---
# agent 1.0.0
1.用户自定义埋点
2.FT Gateway 数据同步