# commonDns

DNS解析模块，基于阿里云DNS服务提供优化的域名解析功能。

## 功能概述

本模块提供优化的DNS解析服务，支持阿里云DNS服务，提高网络请求的稳定性和速度。支持DNS缓存和故障转移机制。

## 依赖说明

在app模块的build.gradle.kts中添加：

```kotlin
implementation(project(":commonDns"))

项目的gradle文件需要添加 maven { setUrl("https://maven.aliyun.com/repository/public/") }
```

## 使用方式

### 1. 基本DNS解析

```kotlin
// 直接DNS解析
val dnsResult = DNSHelper.resolve("www.example.com")
if (dnsResult.isSuccess) {
    val ipAddresses = dnsResult.ipAddresses
    ipAddresses.forEach { ip ->
        println("Resolved IP: $ip")
    }
} else {
    println("DNS resolve failed: ${dnsResult.error}")
}
```

### 2. 异步DNS解析

```kotlin
// 异步DNS解析
DNSHelper.resolveAsync("api.example.com") { result ->
    if (result.isSuccess) {
        // 使用解析结果
        val ip = result.ipAddresses.firstOrNull()
        // 进行网络请求
        makeNetworkRequest(ip)
    } else {
        // 处理解析失败
        Log.e("DNS", "Resolve failed: ${result.error}")
    }
}
```

### 3. 带缓存的DNS解析

```kotlin
// 启用本地缓存
DNSHelper.setCacheEnabled(true)
DNSHelper.setCacheTimeout(5 * 60 * 1000L) // 5分钟缓存

// 解析时会优先使用缓存
val result = DNSHelper.resolveWithCache("cdn.example.com")
```

### 4. 配置DNS服务器

```kotlin
// 配置阿里云DNS服务器
DNSHelper.configureDNSServers(
    listOf(
        "223.5.5.5",    // 阿里云DNS
        "223.6.6.6",    // 阿里云DNS备用
        "8.8.8.8"       // Google DNS备用
    )
)
```

### 5. 网络库集成

```kotlin
// 在OkHttp中使用DNS解析
val dns = object : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val result = DNSHelper.resolve(hostname)
        return if (result.isSuccess) {
            result.ipAddresses.map { InetAddress.getByName(it) }
        } else {
            // 降级到系统DNS
            Dns.SYSTEM.lookup(hostname)
        }
    }
}

val okHttpClient = OkHttpClient.Builder()
    .dns(dns)
    .build()
```

## 配置参数说明

### DNSHelper配置方法

```kotlin
// 设置缓存
DNSHelper.setCacheEnabled(true/false)
DNSHelper.setCacheTimeout(timeoutMs: Long)

// 设置DNS服务器
DNSHelper.configureDNSServers(servers: List<String>)

// 设置超时时间
DNSHelper.setResolveTimeout(timeoutMs: Long)

// 清除缓存
DNSHelper.clearCache()

// 设置重试次数
DNSHelper.setMaxRetryCount(count: Int)
```

## 数据模型

### DNS解析结果

```kotlin
data class DNSResult(
    val isSuccess: Boolean,
    val ipAddresses: List<String>,
    val error: String?,
    val resolveTime: Long, // 解析耗时（毫秒）
    val fromCache: Boolean // 是否来自缓存
)
```

## 高级功能

### 1. DNS预解析

```kotlin
// 应用启动时预解析常用域名
val commonDomains = listOf(
    "api.example.com",
    "cdn.example.com",
    "auth.example.com"
)

commonDomains.forEach { domain ->
    DNSHelper.resolveAsync(domain) { result ->
        if (result.isSuccess) {
            Log.d("DNS", "Pre-resolved $domain to ${result.ipAddresses}")
        }
    }
}
```

### 2. 故障转移

```kotlin
// 配置多个DNS服务器实现故障转移
DNSHelper.configureDNSServers(
    listOf(
        "223.5.5.5",    // 主DNS
        "114.114.114.114", // 备用DNS1
        "8.8.8.8"       // 备用DNS2
    )
)

// 设置故障转移策略
DNSHelper.setFailoverEnabled(true)
DNSHelper.setFailoverTimeout(2000L) // 2秒超时后切换
```

### 3. DNS安全

```kotlin
// 启用DNS over HTTPS (如果支持)
DNSHelper.enableDoH(true, "https://dns.alidns.com/dns-query")

// 启用DNSSEC验证
DNSHelper.enableDNSSEC(true)
```

## 性能优化

### 1. 并发解析

```kotlin
// 批量并行解析
val domains = listOf("domain1.com", "domain2.com", "domain3.com")
val results = DNSHelper.resolveBatch(domains)

results.forEach { (domain, result) ->
    if (result.isSuccess) {
        Log.d("DNS", "$domain resolved to ${result.ipAddresses}")
    }
}
```

### 2. 智能缓存

```kotlin
// 根据TTL自动缓存
DNSHelper.enableSmartCache(true)

// 设置缓存大小限制
DNSHelper.setCacheMaxSize(1000) // 最多缓存1000条记录

// 设置缓存清理策略
DNSHelper.setCacheEvictPolicy(CacheEvictPolicy.LRU)
```

## 注意事项

1. 首次DNS解析可能较慢，建议使用预解析
2. 缓存会占用内存，需要合理设置缓存大小
3. 网络异常时DNS解析可能失败，需要做好降级处理
4. 某些网络环境下可能限制自定义DNS服务器
5. 需要在主线程中调用异步解析方法

## 错误处理

### 常见错误类型

- 网络超时：检查网络连接和超时设置
- DNS服务器不可达：检查DNS服务器配置
- 域名不存在：验证域名是否正确
- 解析被阻止：检查网络防火墙设置

### 降级策略

```kotlin
fun resolveWithFallback(hostname: String): List<String> {
    // 1. 尝试自定义DNS
    val result = DNSHelper.resolve(hostname)
    if (result.isSuccess) {
        return result.ipAddresses
    }
    
    // 2. 降级到系统DNS
    try {
        return Dns.SYSTEM.lookup(hostname).map { it.hostAddress }
    } catch (e: Exception) {
        Log.e("DNS", "System DNS failed", e)
    }
    
    // 3. 返回硬编码的IP（如果可用）
    return getHardcodedIP(hostname)
}
```

## 监控和调试

### 1. DNS解析统计

```kotlin
// 获取DNS解析统计信息
val stats = DNSHelper.getStatistics()
Log.d("DNS", "Total requests: ${stats.totalRequests}")
Log.d("DNS", "Cache hits: ${stats.cacheHits}")
Log.d("DNS", "Average resolve time: ${stats.averageResolveTime}ms")
```

### 2. 调试模式

```kotlin
// 启用调试日志
DNSHelper.setDebugEnabled(true)

// 设置日志级别
DNSHelper.setLogLevel(LogLevel.VERBOSE)
```

## 版本要求

- Android API Level: 21+
- 网络权限
- 存储权限（用于缓存）

## 权限要求

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<!-- 可选：用于持久化缓存 -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```