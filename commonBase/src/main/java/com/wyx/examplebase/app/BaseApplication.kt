package com.wyx.examplebase.app

import android.app.Application
import android.content.Context
import com.wyx.examplebase.utils.ForegroundBackgroundObserver
import com.wyx.examplebase.utils.ProcessUtils
import com.wyx.examplebase.utils.SpUtils

open class BaseApplication : Application(), ApplicationLifecycle, ForegroundBackgroundObserver {


    companion object {
        lateinit var sContext : BaseApplication
    }

    override fun onCreate() {
        super.onCreate()
        sContext = this

        registerActivityLifecycleCallbacks(ActivityLifecycleCallbacksImpl())

    }

    override fun onAttachBaseContext(context: Context) {
        sContext = this
    }

    override fun onCreate(application: Application) {
    }

    override fun onTerminate(application: Application) {
    }

    override fun initByFrontDesk(): MutableList<() -> String> {
        val list = mutableListOf<() -> String>()
        // 以下只需要在主进程当中初始化 按需要调整
        if (ProcessUtils.isMainProcess(sContext)) {
            list.add { initMMKV() }
        }
        return list
    }

    override fun initByBackstage() {
    }

    override fun foregroundBackgroundNotify(isForeground: Boolean) {
    }

    private fun initMMKV(): String {
        val result = SpUtils.initMMKV(sContext)
        return "MMKV -->> $result"
    }


}