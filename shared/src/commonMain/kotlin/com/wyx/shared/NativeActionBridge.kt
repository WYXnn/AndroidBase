package com.wyx.shared

abstract class NativeActionBridge {
    abstract fun playSystemSound()
    abstract fun navigateToNativeScreen(message: String)
    abstract fun getAppVersion(): String

    fun callFromNative(msg : String) {
        println("callFromNative:$msg")
    }
}