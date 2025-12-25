package com.wyx.commonlog

import okhttp3.OkHttpClient

object LogConfig {

    var client: OkHttpClient? = null
    var esUrl: String? = null

    fun isInitialized(): Boolean {
        return client != null && esUrl != null
    }

}