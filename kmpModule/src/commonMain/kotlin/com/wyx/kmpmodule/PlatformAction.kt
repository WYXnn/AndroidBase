package com.wyx.kmpmodule

sealed class PlatformAction {
    data class ShowToast(val message: String) : PlatformAction()
    data class NavigateToNative(val route: String) : PlatformAction()
    object TriggerHapticFeedback : PlatformAction()
    data class needCallback(val content: String, val callback : (msg : String) -> Unit) : PlatformAction()
}