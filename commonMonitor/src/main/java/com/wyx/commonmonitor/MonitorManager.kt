package com.wyx.commonmonitor

import android.content.Context
import com.bonree.sdk.agent.Bonree
import java.util.Objects

object MonitorManager {

    fun init(context: Context, appKey : String, address : String, appVersion : String, channel : String) {
        Bonree.withAppID(appKey)
            .withConfigAddress(address)
            .withAppVersion(appVersion)
            .withChannelID(channel)
            .start(context)
    }

    fun setUserFlag(userFlag : String) {
        Bonree.setUserID(userFlag)
    }

    fun trendsData(eventId : String, eventName : String, label : String, param : String, info : Map<String, Object>) {
        Bonree.setCustomEventWithLabel(eventId, eventName, label, param, info)
    }

}