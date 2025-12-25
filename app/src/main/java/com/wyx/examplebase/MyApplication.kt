package com.wyx.examplebase

import com.wyx.examplebase.app.BaseApplication
import com.wyx.commonlog.CommonLog
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : BaseApplication() {

    override fun onCreate() {
        super.onCreate()

//        CommonLog.init(true)
    }
}