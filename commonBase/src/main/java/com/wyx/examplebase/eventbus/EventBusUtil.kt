package com.wyx.examplebase.eventbus

import org.greenrobot.eventbus.EventBus

object EventBusUtil {

    fun register(obj: Any) {
        EventBus.getDefault().register(obj)
    }

    fun unregister(obj: Any) {
        EventBus.getDefault().unregister(obj)
    }

    fun post(event: Any) {
        EventBus.getDefault().post(event)
    }

    fun postSticky(event: Any) {
        EventBus.getDefault().postSticky(event)
    }

    fun removeStickyEvent(event: Any) {
        EventBus.getDefault().removeStickyEvent(event)
    }

    fun removeAllStickyEvents() {
        EventBus.getDefault().removeAllStickyEvents()
    }

    fun removeStickyEvent(eventType: Class<*>) {
        EventBus.getDefault().removeStickyEvent(eventType)
    }

}