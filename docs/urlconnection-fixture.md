# URLConnection 三种来源的一次性测试

仅保留 `ft-urlconnection-fixture` 一个 Gradle 测试 App，同时测试工程源码（Java/Kotlin）、固定 AAR 和固定 JAR 中的网络调用。

连接设备或启动模拟器后，从仓库根目录运行：

```sh
./gradlew -PftUrlConnectionFixture :ft-urlconnection-fixture:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ft.sdk.URLConnectionDeviceTest#javaKotlinJarAarProxyEntrypointsAndOptionalDependencies
```

同一次测试覆盖三种来源的 `openConnection()` 和 `openConnection(Proxy)`，断言包含来源名称；同时检查排除规则及可选依赖隔离。去掉 class 参数可运行完整设备测试。增加 `-PftDisableURLConnection` 可验证关闭插桩后的行为。

`ft-urlconnection-fixture/libs/` 中的 AAR/JAR 是需要版本管理的固定测试输入，未经插桩，每次构建由当前配置的插件处理。不再编译两个额外 Gradle 库模块。需要验证本地插件修改时，先构建 `:ft-plugin:jar`，再以 `FT_USE_LOCAL_PLUGIN=true` 运行上述命令。

样例源码位于 `ft-urlconnection-fixture/fixtures/`，不参与 App 源码编译。更新样例时，使用 JDK 11+ 和 Python 3 执行：

```sh
python3 ft-urlconnection-fixture/fixtures/regenerate.py
```

生成脚本固定 ZIP 时间戳，输出 Java 8 字节码；源码、脚本和两个产物一起更新。不要提交 APK、build 目录或临时测试证书。
