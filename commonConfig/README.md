# commonConfig

远程配置管理模块，支持动态获取和管理应用配置。

## 功能概述

本模块提供远程配置的获取和管理功能，支持单次获取和循环获取模式，支持内存和磁盘缓存，支持自定义参数和回调。

## 依赖说明

在app模块的build.gradle.kts中添加：

```kotlin
implementation(project(":commonConfig"))
```

## 使用方式

### 1. 基本初始化

```kotlin
@Inject
lateinit var configManager: ConfigManager

// 在Application或需要的地方初始化
configManager
    .init(
        nameSpace = "app_config",
        onFail = { e ->
            Log.e("Config", "Failed to get config", e)
        },
        onSuccess = { result ->
            Log.d("Config", "Config received: $result")
            // 处理配置结果
        }
    )
    .setUrl("https://api.example.com/config")
    .setCommonParam(yourRequestObject)
    .getConfig()
```

### 2. 循环获取模式

```kotlin
configManager
    .init(
        nameSpace = "app_config",
        onFail = { e ->
            Log.e("Config", "Failed to get config", e)
        },
        onSuccess = { result ->
            Log.d("Config", "Config received: $result")
        },
        needLoop = true,
        loopMillis = 30 * 1000L // 30秒循环一次
    )
    .setUrl("https://api.example.com/config")
    .setCommonParam(yourRequestObject)
    .getConfig()
```

### 3. 带缓存的配置获取

```kotlin
configManager
    .init(
        nameSpace = "app_config",
        onFail = { e ->
            Log.e("Config", "Failed to get config", e)
        },
        onSuccess = { result ->
            Log.d("Config", "Config received: $result")
        },
        needLoop = false,
        useDiskMemory = true, // 启用磁盘缓存
        useRuntimeMemory = true // 启用内存缓存
    )
    .setUrl("https://api.example.com/config")
    .setCommonParam(yourRequestObject)
    .getConfig()
```

### 4. 停止循环获取

```kotlin
configManager.stopLoop()
```

## 配置参数说明

### init方法参数

- nameSpace: 配置命名空间
- onFail: 失败回调
- onSuccess: 成功回调
- needLoop: 是否循环获取，默认false
- loopMillis: 循环间隔时间，默认10秒
- useDiskMemory: 是否使用磁盘缓存，默认false
- useRuntimeMemory: 是否使用内存缓存，默认false

### 链式配置方法

- setUrl(url: String): 设置请求URL
- setCommonParam(param: IConfigRequest): 设置通用请求参数
- getConfig(): 开始获取配置

## 数据模型

### IConfigRequest接口

需要实现该接口来定义请求参数结构。

### 返回结果

返回JSONObject类型的配置数据，包含所有远程配置信息。

## 生命周期管理

- ConfigManager继承自BaseViewModel，支持协程
- 自动管理请求的生命周期
- 支持取消正在进行的请求

## 注意事项

1. 必须先调用init方法进行初始化
2. 使用循环模式时记得在适当时机调用stopLoop
3. 缓存功能可以减少网络请求，提高性能
4. 请求在IO线程中执行，回调在主线程

## 线程安全

- 配置获取操作在Dispatchers.IO中执行
- 回调操作在主线程中执行
- 支持并发安全的配置管理

## 依赖模块

- commonNet: 提供网络请求能力
- 需要依赖注入支持