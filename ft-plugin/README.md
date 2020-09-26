# 关于 plugin 的说明

plugin 在编译期间实现对 java 字节码的动态修改，从而实现了函数插桩的功能。
其中关于插桩的实现是基于该库 https://github.com/Leaking/Hunter，关于该库
不做解释，这里主要说明相关业务逻辑的实现部分及未来功能如何扩展。

## 项目结构

```
garble
├── asm （该包下的类，一般不需要修改）
│      ├── BaseTransform （负责遍历所有 class 文件，同时进行转换）
│      ├── BaseWeaver （负责过滤需要转换的 class，然后对单个文件进行转换）
│      ├── ClassLoaderHelper （获得 URLClassLoader 用来读取类字节流）
│      ├── ExtendClassWriter （生成 java class 的类）
│      └── IWeaver （修改类的接口，提供是否需要修改类方法，及修改类的方法）
├── bytecode
│      ├── FTClassAdapter （访问类结构的类，用来修改类结构）
│      ├── FTMethodAdapter （访问类方法，用类修改类方法）
│      └── FTWeaver IWeaver （实现类，判断哪些类需要修改）
├── ClassNameAnalytics （定义 SDK 中类名称判断方法）
├── Constants （一些常量字段）
├── FTExtension （插件中的开关定义类，可以在 build.gradle 中控制插件的功能）
├── FTHookConfig （包含系统中插桩数据的封装）
├── FTMethodCell （插桩数据结构定义类）
├── FTSubMethodCell （插桩数据子类结构定义类）
├── FTTransform （FT 定义的类结构转换类）
├── FTTransformHelper
├── FTUtil （定义一些判断类类型的方法）
├── Logger （日志类）
├── VersionUtils （比较版本好的类）
│   
```

## 业务逻辑说明

### 一、 业务逻辑的实现主要在 FTClassAdapter、FTMethodAdapter、FTHookConfig 这三个类中。
* 目前 FTClassAdapter 中主要实现了访问类 com.ft.sdk.FTSdk.java 中版本号字段，来判断系统 plugin
和 agent 的版本号是否兼容，然后有添加了访问类方法的类。
* FTMethodAdapter 中实现了对系统 Log 类的替换、替换 OkHttpClient.Builder.build() 方法、
  替换 HttpClientBuilder.build 方法、在 FTSdk 类中插入 plugin 的版本号、对系统事件进行插桩
* FTHookConfig 中定义了插桩数据的结构。关于 class 的字节码信息可以通过 ASM ByteCode Viewer 插件或者通过
  AS 的 Tools -> Kotlin -> show kotlin bytecode 来查看

## 扩展

后期的功能扩展也可在上述说的这三个类中进行添加修改

## 关于错误的修改
* 1、如果是修改字节码的框架出现问题可以在 https://github.com/Leaking/Hunter 中寻在答案
* 2、如果是修改了类的业务逻辑出错可以在编译的输出日志中搜索 "修改类异常-文件名" 的日志来定位
    出错的文件及出错信息。该条输出日志在 BaseTransform.transformDir() 方法中
