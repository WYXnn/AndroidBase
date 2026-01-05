# commonConfigApollo

GraphQL配置管理模块，基于Apollo GraphQL提供现代化的远程配置获取和管理功能。

## 功能概述

本模块使用Apollo GraphQL客户端提供类型安全的配置管理，支持GraphQL查询、订阅和缓存机制，提供比传统REST API更高效的配置获取方式。

## 依赖说明

在app模块的build.gradle.kts中添加：

```kotlin
implementation(project(":commonConfigApollo"))

环境配置 (Gradle)在使用此库的主 App 模块（app/build.gradle.kts）中，需要配置 Apollo 插件和相关依赖。1.1 应用插件plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Apollo 插件，用于根据 .graphql 文件生成 Java/Kotlin 代码
    id("com.apollographql.apollo3") version "3.8.2"

    // Apollo 运行时 (必须与插件版本一致)
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.2")

    // SQLite 缓存支持 (commonConfigApollo 内部依赖此库，建议显式添加以防冲突)
    implementation("com.apollographql.apollo3:apollo-normalized-cache-sqlite:3.8.2")

    配置 Apollo 包名apollo {
    // 生成的 Query 类所在的包名 
    packageName.set("com.example.yourapp.graphql")

```

## 使用方式

### 1. 初始化Apollo客户端

```kotlin
class MyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        ConfigApolloManager.init(
            context = this,
            serverUrl = "https://api.example.com/graphql",
            apiKey = "your_api_key"
        )
    }
}
```

### 2. 基本配置查询

```kotlin
// 查询应用配置
val query = GetAppConfigQuery()
ConfigApolloManager.query(query) { result ->
    when (result) {
        is ApolloResponse.Success -> {
            val config = result.data?.appConfig
            config?.let {
                // 使用配置数据
                val appName = it.name
                val version = it.version
                val features = it.features
            }
        }
        is ApolloResponse.Failure -> {
            Log.e("ApolloConfig", "Query failed", result.exception)
        }
    }
}
```

### 3. 带参数的配置查询

```kotlin
// 根据用户类型查询配置
val query = GetFeatureFlagsQuery(
    userType = "premium",
    platform = "android"
)

ConfigApolloManager.query(query) { result ->
    when (result) {
        is ApolloResponse.Success -> {
            val features = result.data?.featureFlags
            features?.forEach { feature ->
                Log.d("Feature", "${feature.name}: ${feature.enabled}")
            }
        }
        is ApolloResponse.Failure -> {
            // 处理错误
        }
    }
}
```

### 4. 配置订阅（实时更新）

```kotlin
// 订阅配置变化
val subscription = ConfigUpdatedSubscription()

val job = ConfigApolloManager.subscribe(subscription) { result ->
    when (result) {
        is ApolloResponse.Success -> {
            val updatedConfig = result.data?.configUpdated
            updatedConfig?.let {
                // 处理配置更新
                updateLocalConfig(it)
            }
        }
        is ApolloResponse.Failure -> {
            Log.e("ApolloConfig", "Subscription failed", result.exception)
        }
    }
}

// 取消订阅
job.cancel()
```

### 5. 缓存配置管理

```kotlin
// 启用缓存
ConfigApolloManager.enableCache(true)

// 设置缓存过期时间
ConfigApolloManager.setCacheTimeout(5 * 60 * 1000L) // 5分钟

// 清除缓存
ConfigApolloManager.clearCache()

// 仅从缓存获取
ConfigApolloManager.queryFromCache(query) { result ->
    // 处理缓存结果
}
```

## GraphQL Schema定义

### 配置查询示例

```graphql
# 获取应用配置
query GetAppConfig {
  appConfig {
    name
    version
    features {
      name
      enabled
      parameters {
        key
        value
      }
    }
  }
}

# 获取功能开关
query GetFeatureFlags($userType: String!, $platform: String!) {
  featureFlags(userType: $userType, platform: $platform) {
    name
    enabled
    rolloutPercentage
  }
}

# 配置更新订阅
subscription ConfigUpdated {
  configUpdated {
    id
    name
    value
    updatedAt
  }
}
```

## 配置参数说明

### ApolloClient配置

```kotlin
ConfigApolloManager.configure(
    serverUrl: String,
    apiKey: String? = null,
    timeout: Long = 30_000L,
    enableCache: Boolean = true,
    cacheMaxSize: Long = 10 * 1024 * 1024, // 10MB
    enableLogging: Boolean = BuildConfig.DEBUG
)
```

### HTTP Headers配置

```kotlin
ConfigApolloManager.addHttpHeader("Authorization", "Bearer $token")
ConfigApolloManager.addHttpHeader("User-Agent", "MyApp/1.0")
```

## 数据模型

### Apollo生成的类型

Apollo会根据GraphQL schema自动生成对应的Kotlin类型：

```kotlin
// 自动生成的查询类型
class GetAppConfigQuery : GraphQLQuery() {
    override fun variables(): Map<String, Any?> = emptyMap()
}

// 自动生成的数据类型
data class AppConfig(
    val name: String?,
    val version: String?,
    val features: List<Feature?>?
)

data class Feature(
    val name: String?,
    val enabled: Boolean?,
    val parameters: List<Parameter?>?
)
```

## 高级功能

### 1. 乐观更新

```kotlin
// 乐观更新配置
val mutation = UpdateConfigMutation(
    input = ConfigInput(name = "new_feature", value = "enabled")
)

// 先更新本地UI，再发送到服务器
ConfigApolloManager.optimisticUpdate(
    mutation = mutation,
    optimisticResponse = UpdateConfigMutation.Data(
        updateConfig = Config(
            id = "temp_id",
            name = "new_feature",
            value = "enabled"
        )
    )
) { result ->
    // 处理最终结果
}
```

### 2. 重试机制

```kotlin
// 配置重试策略
ConfigApolloManager.setRetryPolicy(
    maxRetries = 3,
    backoffMultiplier = 2.0,
    initialDelay = 1000L
)
```

### 3. 网络状态监控

```kotlin
// 监控网络状态
ConfigApolloManager.setNetworkStatusListener { isConnected ->
    if (isConnected) {
        // 网络恢复，重新同步配置
        syncConfig()
    }
}
```

## 错误处理

### GraphQL错误处理

```kotlin
ConfigApolloManager.query(query) { result ->
    when (result) {
        is ApolloResponse.Success -> {
            // 处理成功响应
            handleSuccess(result.data)
        }
        is ApolloResponse.Failure -> {
            when (result.exception) {
                is ApolloNetworkException -> {
                    // 网络错误
                    handleNetworkError(result.exception)
                }
                is ApolloHttpException -> {
                    // HTTP错误
                    handleHttpError(result.exception)
                }
                is ApolloParseException -> {
                    // 解析错误
                    handleParseError(result.exception)
                }
            }
        }
    }
}
```

### GraphQL错误信息

```kotlin
// 访问GraphQL错误信息
if (result is ApolloResponse.Success) {
    val errors = result.errors
    errors?.forEach { error ->
        Log.e("GraphQL", "Error: ${error.message}")
        // 处理特定的GraphQL错误
        when (error.extensions?.get("code")) {
            "CONFIG_NOT_FOUND" -> handleConfigNotFound()
            "INVALID_PARAMETER" -> handleInvalidParameter()
        }
    }
}
```

## 性能优化

### 1. 查询优化

```kotlin
// 只查询需要的字段
val optimizedQuery = GetMinConfigQuery() {
    // 只查询必要的字段，减少网络传输
    name
    version
}

// 字段别名
val queryWithAlias = GetConfigWithAliasQuery {
    appName: name
    appVersion: version
}
```

### 2. 批量查询

```kotlin
// 一次请求获取多个配置
val batchQuery = GetBatchConfigQuery(
    configIds = listOf("feature1", "feature2", "feature3")
)
```

### 3. 本地缓存策略

```kotlin
// 设置不同的缓存策略
ConfigApolloManager.setCachePolicy(
    queryPolicy = CachePolicy.CacheFirst,      // 查询优先使用缓存
    mutationPolicy = CachePolicy.NetworkOnly, // 变更强制网络请求
    subscriptionPolicy = CachePolicy.NetworkOnly // 订阅强制网络请求
)
```

## 注意事项

1. 必须在使用前调用init方法初始化Apollo客户端
2. GraphQL schema变更后需要重新生成类型
3. 网络请求需要在后台线程执行
4. 缓存会占用内存，需要合理设置缓存大小
5. 订阅需要在适当时机取消以避免内存泄漏

## 构建配置

### Gradle配置

```kotlin
// 在build.gradle.kts中添加Apollo插件
plugins {
    id("com.apollographql.apollo3") version "3.8.2"
}

// Apollo配置
apollo {
    service("service") {
        packageName.set("com.example.app.apollo")
        schemaFile.set(file("src/main/graphql/schema.graphqls"))
    }
}
```

## 版本要求

- Android API Level: 21+
- Apollo Kotlin: 3.8.0+
- Kotlin Coroutines: 1.6.0+

## 权限要求

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```