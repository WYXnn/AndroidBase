package com.wyx.commonnet.network

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DINetworkModule {


    /**
     * [OkHttpClient]依赖提供方法
     *
     * @return OkHttpClient
     */
    @Singleton
    @Provides
    fun provideOkHttpClient(config : INetworkConfig?): OkHttpClient {
        val logInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val builder : OkHttpClient.Builder = OkHttpClient.Builder()
            .connectTimeout(config?.getConnectTimeout() ?: 15000, TimeUnit.MILLISECONDS)
            .readTimeout(config?.getReadTimeout() ?: 15000, TimeUnit.MILLISECONDS)
        builder.addInterceptor(logInterceptor)
        if (config != null) {
            for (interceptor in config.getInterceptors()) {
                builder.addInterceptor(interceptor)
            }
        }
        builder.retryOnConnectionFailure(true)
        return builder.build()
    }

    /**
     * 项目主要服务器地址的[Retrofit]依赖提供方法
     *
     * @param okHttpClient OkHttpClient OkHttp客户端
     * @return Retrofit
     */
    @Singleton
    @Provides
    fun provideMainRetrofit(okHttpClient: OkHttpClient, config : INetworkConfig?): Retrofit {
        return Retrofit.Builder()
            .baseUrl(config?.getBaseUrl() ?: "")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }
}