基于 apollo-kotlin 的轻量级封装库，旨在简化 Android 端 GraphQL 的开发流程。它内置了二级缓存（内存+SQLite）和智能离线降级策略，让你专注于业务逻辑而非底层网络配置。
1. 环境配置 (Gradle)在使用此库的主 App 模块（app/build.gradle.kts）中，需要配置 Apollo 插件和相关依赖。1.1 应用插件plugins {
id("com.android.application")
id("org.jetbrains.kotlin.android")
// Apollo 插件，用于根据 .graphql 文件生成 Java/Kotlin 代码
id("com.apollographql.apollo3") version "3.8.2"
}
1.2 添加依赖dependencies {
// 引入 commonConfigApollo 库 (假设已导入模块或 aar)
implementation(project(":commonConfigApollo"))

    // Apollo 运行时 (必须与插件版本一致)
    implementation("com.apollographql.apollo3:apollo-runtime:3.8.2")
    
    // SQLite 缓存支持 (commonConfigApollo 内部依赖此库，建议显式添加以防冲突)
    implementation("com.apollographql.apollo3:apollo-normalized-cache-sqlite:3.8.2")
}
1.3 配置 Apollo 包名apollo {
// 生成的 Query 类所在的包名
packageName.set("com.example.yourapp.graphql")

    // (可选) 指定 schema 文件位置，默认为 src/main/graphql
    // service("service") {
    //     srcDir("src/main/graphql")
    // }
}
2. 准备 GraphQL 文件在 src/main/graphql/ 目录下放置你的 schema.json (或 schema.graphqls) 和查询文件。示例文件结构：src/main/graphql/
   ├── schema.json               // 从服务器下载的 Schema
   └── UserQuery.graphql         // 你的查询语句
   示例 UserQuery.graphql:query UserProfile($userId: ID!) {
   user(id: $userId) {
   id
   name
   avatar
   email
   }
   }
   注意：添加或修改 .graphql 文件后，请执行 Build -> Rebuild Project 以生成对应的 Kotlin 类（例如 UserProfileQuery）。3. 初始化库建议在自定义的 Application 类中进行全局初始化。class MyApplication : Application() {
   override fun onCreate() {
   super.onCreate()

        // 配置 commonConfigApollo
        val config = ApolloConfig(
            serverUrl = "[https://api.example.com/graphql](https://api.example.com/graphql)",
            context = this,
            // 可选配置
            dbName = "app_cache.db",           // 数据库名
            memoryCacheSize = 5 * 1024 * 1024, // 内存缓存 5MB
            connectTimeoutSeconds = 15,        // 连接超时
            headers = mapOf(
                "Authorization" to "Bearer YOUR_TOKEN",
                "User-Agent" to "Android-App"
            )
        )

        ConfigApolloManager.initialize(config)
   }
   }
4. 发起查询 (ViewModel/Repository)commonConfigApollo 的核心方法是 query()，它是一个挂起函数 (suspend)，需要在协程中调用。它会自动处理网络请求和缓存回退。示例代码import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {

    fun loadUserData(userId: String) {
        viewModelScope.launch {
            // 1. 创建查询对象 (由 Apollo 插件生成)
            val query = UserProfileQuery(userId = userId)

            // 2. 调用 ConfigApolloManager
            val result = ConfigApolloManager.query(query)

            // 3. 处理结果
            when (result) {
                is ApolloResult.Success -> {
                    val data = result.data // 类型安全，自动推导为 UserProfileQuery.Data
                    val isCached = result.isFromCache
                    
                    if (isCached) {
                        println("当前无网络，显示缓存数据")
                    }
                    
                    // 更新 UI
                    updateUI(data.user)
                }

                is ApolloResult.Error -> {
                    // 处理错误 (网络错误且无缓存，或服务器返回 GraphQL Error)
                    val errorMsg = result.message
                    val exception = result.exception
                    println("加载失败: $errorMsg")
                }
            }
        }
    }
}
5. 高级功能5.1 数据预取 (Prefetch)如果你想在用户进入某个页面前提前加载数据（例如在列表页预加载详情页数据），可以使用 prefetch。它只下载并写入缓存，不返回数据。viewModelScope.launch {
   ConfigApolloManager.prefetch(UserProfileQuery(userId = "123"))
   }
   5.2 清理缓存当用户退出登录时，务必清理缓存，防止数据泄露。fun logout() {
   viewModelScope.launch {
   ConfigApolloManager.clearCache()
   // 执行其他登出逻辑...
   }
   }
   5.3 获取原始 Client如果库封装的功能无法满足特殊需求（例如 Mutation 上传文件、Subscription 订阅），你可以获取底层的 ApolloClient 自行操作。val rawClient = ConfigApolloManager.getClient()
   // rawClient.mutation(...)
   // rawClient.subscription(...)
6. 缓存策略原理当你调用 ConfigApolloManager.query(query) 时，内部逻辑如下：NetworkFirst: 尝试从网络请求数据。成功: 返回数据，并自动保存到内存和 SQLite 数据库。失败 (断网/超时): 捕获异常，进入第 2 步。CacheOnly: 尝试从本地缓存读取。命中: 返回缓存数据，标记 isFromCache = true。未命中: 返回 ApolloResult.Error，提示网络错误且无缓存。这种策略确保了用户在有网时看到最新内容，断网时看到最后一次成功加载的内容。