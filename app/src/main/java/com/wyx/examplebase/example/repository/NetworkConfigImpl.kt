package com.wyx.examplebase.example.repository

import com.wyx.commonnet.network.BaseNetworkConfig
import com.wyx.commonnet.network.INetworkConfig
import okhttp3.Interceptor
import javax.inject.Inject
class NetworkConfigImpl @Inject constructor() : INetworkConfig {
    override fun getBaseUrl(): String {
        return "https://www.baidu1.com"
    }

    override fun getConnectTimeout(): Long {
        return 10000L
    }

    override fun getReadTimeout(): Long {
        return 10000L
    }

    override fun getInterceptors(): List<Interceptor> {
        return ArrayList()
    }
}