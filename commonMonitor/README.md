# commonMonitor

性能监控模块，基于Bonree提供应用性能监控和用户行为分析功能。

## 功能概述

本模块封装了Bonree性能监控SDK，提供应用启动时间、网络请求、崩溃率、用户行为等全方位的性能监控能力。

## 依赖说明

在app模块的build.gradle.kts中添加：

```kotlin
implementation(project(":commonMonitor"))

项目的gradle文件需要添加 maven { setUrl("https://gitlab.bonree.com/BonreeSDK_TAPM/Android/raw/master") }
以及 classpath ("com.bonree.agent.android:bonree:$brsdk_version")
主module的plugin中添加 id("bonree")
```

## 使用方式

### 1. 初始化监控SDK

在Application的onCreate方法中初始化：

```kotlin
class MyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        MonitorManager.init(
            context = this,
            appKey = "your_app_key",
            address = "https://your-bonree-server.com",
            appVersion = BuildConfig.VERSION_NAME,
            channel = BuildConfig.FLAVOR ?: "default"
        )
    }
}
```

### 2. 设置用户标识

```kotlin
// 用户登录后设置用户ID
MonitorManager.setUserFlag("user_12345")

// 用户登出时清除标识
MonitorManager.setUserFlag("")
```

### 3. 自定义事件埋点

```kotlin
// 简单的事件埋点
MonitorManager.trendsData(
    eventId = "button_click",
    eventName = "首页按钮点击",
    label = "重要操作",
    param = "homepage_button",
    info = mapOf(
        "page" to "home",
        "button_id" to "search_btn",
        "timestamp" to System.currentTimeMillis()
    )
)
```

### 4. 页面访问监控

```kotlin
class MainActivity : AppCompatActivity() {
    
    override fun onResume() {
        super.onResume()
        // 页面访问埋点
        MonitorManager.trendsData(
            eventId = "page_view",
            eventName = "页面访问",
            label = "页面监控",
            param = "main_activity",
            info = mapOf(
                "page_name" to "MainActivity",
                "enter_time" to System.currentTimeMillis()
            )
        )
    }
}
```

### 5. 业务流程监控

```kotlin
// 注册流程监控
fun monitorRegistration(success: Boolean, errorMessage: String? = null) {
    val info = mutableMapOf<String, Any>()
    info["step"] = "registration_complete"
    info["success"] = success
    if (errorMessage != null) {
        info["error_message"] = errorMessage
    }
    
    MonitorManager.trendsData(
        eventId = "registration",
        eventName = "用户注册",
        label = if (success) "成功" else "失败",
        param = "registration_flow",
        info = info
    )
}
```

## 配置参数说明

### init方法参数

- context: 应用上下文
- appKey: Bonree分配的应用Key
- address: Bonree服务器地址
- appVersion: 应用版本号
- channel: 应用渠道标识

### trendsData方法参数

- eventId: 事件唯一标识
- eventName: 事件名称
- label: 事件标签
- param: 事件参数
- info: 额外信息（Map格式）

## 监控功能

### 1. 应用性能监控

- 启动时间监控
- 页面加载时间
- 网络请求耗时
- 内存使用情况
- CPU使用率

### 2. 错误和崩溃监控

- Java崩溃捕获
- ANR监控
- JavaScript错误（WebView）
- 原生崩溃（NDK）

### 3. 网络监控

- HTTP请求成功率
- 响应时间分析
- 网络错误统计
- 域名解析时间

### 4. 用户行为分析

- 页面访问路径
- 用户留存分析
- 功能使用频率
- 用户转化率

## 注意事项

1. 必须在Application中初始化，否则监控可能不完整
2. appKey需要在Bonree控制台申请
3. address需要使用正确的Bonree服务器地址
4. 用户标识建议在用户登录后立即设置
5. 自定义事件埋点要注意频率，避免过多影响性能

## 最佳实践

### 1. 统一事件命名规范

```kotlin
object EventIds {
    const val PAGE_VIEW = "page_view"
    const val BUTTON_CLICK = "button_click"
    const val USER_LOGIN = "user_login"
    const val USER_REGISTER = "user_register"
}

object EventNames {
    const val PAGE_VIEW = "页面访问"
    const val BUTTON_CLICK = "按钮点击"
    const val USER_LOGIN = "用户登录"
    const val USER_REGISTER = "用户注册"
}
```

### 2. 埋点封装

```kotlin
object TrackingHelper {
    
    fun trackPage(pageName: String) {
        MonitorManager.trendsData(
            eventId = EventIds.PAGE_VIEW,
            eventName = EventNames.PAGE_VIEW,
            label = "页面访问",
            param = pageName,
            info = mapOf(
                "page_name" to pageName,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }
    
    fun trackClick(buttonId: String, pageName: String) {
        MonitorManager.trendsData(
            eventId = EventIds.BUTTON_CLICK,
            eventName = EventNames.BUTTON_CLICK,
            label = "按钮点击",
            param = buttonId,
            info = mapOf(
                "button_id" to buttonId,
                "page_name" to pageName,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }
}
```

### 3. 敏感信息处理

```kotlin
// 避免在埋点中包含敏感信息
fun trackLogin(success: Boolean, userId: String? = null) {
    val info = mutableMapOf<String, Any>()
    info["success"] = success
    // 不记录完整的userId，可以记录脱敏后的信息
    if (userId != null && userId.length >= 4) {
        info["user_id_suffix"] = userId.takeLast(4)
    }
    
    MonitorManager.trendsData(
        eventId = EventIds.USER_LOGIN,
        eventName = EventNames.USER_LOGIN,
        label = if (success) "成功" else "失败",
        param = "login_action",
        info = info
    )
}
```

## 版本要求

- Android API Level: 21+
- Bonree SDK: 具体版本请参考官方文档

## 权限要求

- 网络权限：用于上报监控数据
- 存储权限：用于本地缓存监控数据
- 设备信息权限：用于设备识别

## 数据安全

1. 监控数据使用HTTPS加密传输
2. 敏感信息需要在上传前进行脱敏处理
3. 符合用户隐私保护要求
4. 支持数据本地缓存和批量上传