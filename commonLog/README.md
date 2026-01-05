# commonLog

日志管理模块，基于Timber提供统一的日志输出和远程上报功能。

## 功能概述

本模块提供统一的日志管理功能，支持Debug模式的控制台输出和Release模式的远程上报。支持持久化存储和自定义上传到Elasticsearch。

## 依赖说明

在app模块的build.gradle.kts中添加：

```kotlin
implementation(project(":commonLog"))
```

## 使用方式

### 1. 初始化日志系统

在Application的onCreate方法中初始化：

```kotlin
class MyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 创建OkHttpClient用于日志上传
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        CommonLog.init(
            context = this,
            isDebug = BuildConfig.DEBUG,
            usePersistence = true, // 是否持久化存储
            esUrl = "https://your-elasticsearch.com/logs",
            client = client
        )
    }
}
```

### 2. 使用日志API

```kotlin
// Debug日志
CommonLog.d("This is a debug message")

// Info日志
CommonLog.i("This is an info message")

// Warning日志
CommonLog.w("This is a warning message")

// Error日志
CommonLog.e("This is an error message")

// Verbose日志
CommonLog.v("This is a verbose message")
```

### 3. 在不同模式下的表现

#### Debug模式
- 日志输出到Logcat控制台
- 使用Timber.DebugTree()

#### Release模式
- 日志上传到远程服务器
- 使用UploadTree进行处理
- 支持本地持久化存储

## 配置参数说明

### init方法参数

- context: 应用上下文
- isDebug: 是否为Debug模式
- usePersistence: 是否启用持久化存储
- esUrl: Elasticsearch服务器地址
- client: OkHttpClient实例，用于网络请求

## UploadTree功能

### 日志处理流程

1. 接收日志消息
2. 根据usePersistence决定是否本地存储
3. 批量上传到Elasticsearch服务器
4. 处理上传失败情况

### 网络上传

- 使用提供的OkHttpClient进行HTTP请求
- 支持自定义请求头和认证
- 处理网络异常和重试机制

### 本地存储

- 支持日志文件持久化存储
- 避免应用重启后日志丢失
- 支持存储大小和数量限制

## 注意事项

1. 必须在使用前调用init方法，否则会抛出RuntimeException
2. Release模式下需要配置正确的esUrl
3. 确保应用有网络权限才能进行远程上报
4. 持久化存储会占用设备存储空间
5. 建议在Application中进行初始化

## 最佳实践

### 1. 统一日志格式

```kotlin
class LogConstants {
    companion object {
        const val TAG_PREFIX = "MyApp_"
    }
}

// 使用统一的TAG
CommonLog.d("${LogConstants.TAG_PREFIX}User: User login success")
```

### 2. 敏感信息处理

```kotlin
// 避免记录敏感信息
fun logUserInfo(user: User) {
    CommonLog.d("User action: id=${user.id}, name=${user.name}")
    // 不要记录密码、token等敏感信息
}
```

### 3. 异常日志记录

```kotlin
try {
    // 业务逻辑
    doSomething()
} catch (e: Exception) {
    CommonLog.e("Operation failed: ${e.message}")
    // 可以考虑添加异常堆栈信息
}
```

## 性能考虑

1. Debug模式下的日志输出可能会影响性能
2. Release模式的网络上报需要注意频率控制
3. 持久化存储需要定期清理过期日志
4. 建议在高频调用的地方避免使用详细日志

## 依赖模块

- Timber: 日志框架
- OkHttp: 网络请求
- Elasticsearch: 日志存储（可选）

## 版本要求

- Android API Level: 21+
- Timber: 5.0.1+
- OkHttp: 4.10.0+