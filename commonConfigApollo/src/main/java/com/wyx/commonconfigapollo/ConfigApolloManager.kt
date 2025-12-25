package com.wyx.commonconfigapollo

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.apolloStore
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.apollographql.apollo.cache.normalized.isFromCache
import com.apollographql.apollo.cache.normalized.normalizedCache
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.network.okHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * 通用结果包装类
 * 用于统一处理成功、失败和缓存命中的状态
 */
sealed class ApolloResult<out T> {
    data class Success<out T>(val data: T, val isFromCache: Boolean = false) : ApolloResult<T>()
    data class Error(val exception: Throwable, val message: String?) : ApolloResult<Nothing>()
}

/**
 * 配置类
 */
data class ApolloConfig(
    val serverUrl: String,
    val context: Context,
    val dbName: String = "apollo_cache.db",
    val memoryCacheSize: Int = 10 * 1024 * 1024, // 10MB
    val connectTimeoutSeconds: Long = 30,
    val readTimeoutSeconds: Long = 30,
    val headers: Map<String, String> = emptyMap()
)

/**
 * 核心管理器 (单例模式)
 */
object ConfigApolloManager {

    @Volatile
    private var apolloClient: ApolloClient? = null

    /**
     * 初始化库
     * 必须在 Application onCreate 或使用前调用
     */
    fun initialize(config: ApolloConfig) {
        if (apolloClient != null) return // 防止重复初始化

        // 1. 配置 OkHttp
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(config.readTimeoutSeconds, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                config.headers.forEach { (key, value) ->
                    builder.addHeader(key, value)
                }
                chain.proceed(builder.build())
            }
            .build()

        // 2. 配置缓存链 (内存 -> SQL)
        val memoryCacheFactory = MemoryCacheFactory(maxSizeBytes = config.memoryCacheSize)
        val sqlCacheFactory = SqlNormalizedCacheFactory(config.context, config.dbName)

        // 链接缓存：先查内存，再查数据库
        val cacheFactory = memoryCacheFactory.chain(sqlCacheFactory)

        // 3. 构建 ApolloClient
        apolloClient = ApolloClient.Builder()
            .serverUrl(config.serverUrl)
            .okHttpClient(okHttpClient)
            .normalizedCache(cacheFactory) // 启用规范化缓存
            .build()
    }

    /**
     * 获取原始 client 实例 (如果需要高级操作)
     */
    fun getClient(): ApolloClient {
        return apolloClient ?: throw IllegalStateException("Not initialized. Call initialize() first.")
    }

    /**
     * 执行查询 (核心方法)
     * * 策略：
     * 1. 尝试网络请求 (NetworkOnly/NetworkFirst)
     * 2. 如果发生网络异常，自动降级读取缓存 (CacheOnly)
     * 3. 规范化缓存机制会自动处理数据的一致性
     *
     * @param query Apollo生成的Query对象
     * @return ApolloResult 包含数据或错误信息
     */
    suspend fun <D : Query.Data> query(
        query: Query<D>
    ): ApolloResult<D> = withContext(Dispatchers.IO) {
        val client = getClient()

        try {
            // 第一步：尝试从网络获取最新数据，并写入缓存
            // 使用 NetworkFirst，如果成功则写入缓存，如果失败会抛出异常(取决于具体实现，这里我们手动捕获更稳健)
            val response = client.query(query)
                .fetchPolicy(FetchPolicy.NetworkFirst)
                .execute()

            if (response.hasErrors()) {
                // GraphQL 业务层面的错误
                return@withContext ApolloResult.Error(
                    Exception("GraphQL Errors"),
                    response.errors?.joinToString { it.message }
                )
            }

            val data = response.data
            if (data != null) {
                return@withContext ApolloResult.Success(data, isFromCache = response.isFromCache)
            } else {
                return@withContext ApolloResult.Error(Exception("Data is null"), "Server returned no data")
            }

        } catch (e: ApolloException) {
            // 网络请求失败 (无网、超时、服务器挂了)
            // 第二步：尝试读取缓存
            try {
                val cacheResponse = client.query(query)
                    .fetchPolicy(FetchPolicy.CacheOnly) // 强制只读缓存
                    .execute()

                val cachedData = cacheResponse.data
                if (cachedData != null) {
                    // 成功降级到缓存
                    return@withContext ApolloResult.Success(cachedData, isFromCache = true)
                } else {
                    // 缓存里也没有
                    return@withContext ApolloResult.Error(e, "Network failed and no cache available.")
                }
            } catch (cacheError: Exception) {
                // 读取缓存也失败了
                return@withContext ApolloResult.Error(e, "Network failed: ${e.message}")
            }
        } catch (e: Exception) {
            // 其他未知错误
            return@withContext ApolloResult.Error(e, e.message)
        }
    }

    /**
     * 简单的预取方法 (仅下载数据并存入缓存，不关心返回值)
     */
    suspend fun <D : Query.Data> prefetch(query: Query<D>) = withContext(Dispatchers.IO) {
        try {
            getClient().query(query)
                .fetchPolicy(FetchPolicy.NetworkOnly)
                .execute()
        } catch (_: Exception) {
            // 忽略预取错误
        }
    }

    /**
     * 清除所有缓存 (用户登出时使用)
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        getClient().apolloStore.clearAll()
    }
}