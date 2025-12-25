package com.wyx.commonnet.network

import okhttp3.Interceptor

interface INetworkConfig {

    fun getBaseUrl() : String
    fun getConnectTimeout() : Long
    fun getReadTimeout() : Long
    fun getInterceptors() : List<Interceptor>

}