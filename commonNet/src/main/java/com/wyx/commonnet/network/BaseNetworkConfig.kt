package com.wyx.commonnet.network

import okhttp3.Interceptor

open class BaseNetworkConfig constructor() {

    open var BASE_URL = ""
    open var READ_TIMEOUT = 10000L
    open var CONNECT_TIMEOUT = 10000L
    open var INTERCEPTORS = ArrayList<Interceptor>()

}