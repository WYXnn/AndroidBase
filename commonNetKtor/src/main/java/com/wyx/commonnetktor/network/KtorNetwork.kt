package com.wyx.commonnetktor.network

import android.util.Log
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Exception(val e: Throwable) : NetworkResult<Nothing>()
}


object NetworkManager {

    private lateinit var ktorfit: Ktorfit
    private var isInitialized = false

    fun init(config: INetworkConfig) {
        if (isInitialized) return

        val ktorClient = HttpClient(OkHttp) {
            engine {
                config {
                    connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                    readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
                    config.getInterceptors().forEach { addInterceptor(it) }
                }
            }

            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = false
                })
            }

            install(Logging) {
                level = LogLevel.BODY
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorNetwork", message)
                    }
                }
            }

            install(HttpTimeout) {
                requestTimeoutMillis = config.getReadTimeout()
                connectTimeoutMillis = config.getConnectTimeout()
            }

//            defaultRequest {
//                // 默认 Header 设置
//                contentType(ContentType.Application.Json)
//            }
        }

        ktorfit = Ktorfit.Builder()
            .baseUrl(config.getBaseUrl())
            .httpClient(ktorClient)
            .build()

        isInitialized = true
    }

    // 提供创建接口的方法 (类似 Retrofit.create)
    fun create(): Ktorfit {
        check(isInitialized) { "NetworkManager must be initialized!" }
        return ktorfit
    }

}

suspend fun <T> apiCall(apiCall: suspend () -> T): NetworkResult<T> {
    return try {
        val response = apiCall()
        NetworkResult.Success(response)
    } catch (e: Exception) {
        NetworkResult.Exception(e)
    }
}
