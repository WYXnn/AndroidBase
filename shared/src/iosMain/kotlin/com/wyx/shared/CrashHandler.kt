package com.wyx.shared

import kotlin.experimental.ExperimentalNativeApi

object CrashHandler {

    @OptIn(ExperimentalNativeApi::class)
    fun setupKMPExceptionHook() {
        setUnhandledExceptionHook {
            val message = it.message ?: "Unknown Error"
            val stackTrace = it.getStackTrace().joinToString("\n")

            // 打印到控制台，这样你在 Xcode 的 Output 窗口就能看到了
            println("💥💥💥 KOTLIN UNCAUGHT EXCEPTION 💥💥💥")
            println("Message: $message")
            println("Stack Trace:\n$stackTrace")
            println("💥💥💥💥💥💥💥💥💥💥💥💥💥💥💥💥💥")

            // 可选：为了防止直接 Crash，这里可以不做任何操作，但这通常会导致状态不一致。
            // 最好是查看到日志后修复代码。
        }
    }
}