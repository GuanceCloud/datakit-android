# URLConnection 测试夹具

`ft-urlconnection-fixture` 是独立的 Gradle 测试 App，用于验证工程源码（Java/Kotlin）、固定 AAR 和固定 JAR 中的 URLConnection 插桩。通过 `-PftUrlConnectionFixture` 启用该模块。

## 运行设备测试

连接设备或启动模拟器后，从仓库根目录运行：

```sh
./gradlew -PftUrlConnectionFixture :ft-urlconnection-fixture:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ft.sdk.URLConnectionDeviceTest#javaKotlinJarAarProxyEntrypointsAndOptionalDependencies
```

该用例覆盖三种来源的 `openConnection()` 和 `openConnection(Proxy)`，断言包含来源名称；同时检查排除规则，并确认测试 App 未引入 OkHttp 和 Session Replay 可选依赖。

- 去掉 `-Pandroid.testInstrumentationRunnerArguments.class=...` 参数可运行完整设备测试。
- 增加 `-PftDisableURLConnection` 可验证关闭插桩后的入口行为。

## 验证本地插件

先使用已发布的插件配置构建当前插件 JAR，再使用本地 JAR 运行测试：

```sh
./gradlew :ft-plugin:jar
FT_USE_LOCAL_PLUGIN=true ./gradlew -PftUrlConnectionFixture :ft-urlconnection-fixture:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.ft.sdk.URLConnectionDeviceTest#javaKotlinJarAarProxyEntrypointsAndOptionalDependencies
```

## 更新固定测试输入

`ft-urlconnection-fixture/libs/urlconnection-fixture.aar` 和 `ft-urlconnection-fixture/libs/urlconnection-fixture.jar` 是纳入版本管理的固定测试输入，未经插桩，每次构建由当前配置的插件处理。

样例源码位于 `ft-urlconnection-fixture/fixtures/`，不参与 App 源码编译。更新样例时，使用 JDK 11+ 和 Python 3 执行：

```sh
python3 ft-urlconnection-fixture/fixtures/regenerate.py
```

生成脚本固定 ZIP 时间戳，输出 Java 8 字节码。修改样例后重新生成产物，将源码、生成脚本（如有修改）和两个产物一并提交。不要提交 APK、`build/` 目录或临时测试证书。
