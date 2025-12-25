package com.wyx.shared

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

// 暴露给 Swift 的工厂方法
fun MainViewController(bridge: NativeActionBridge): UIViewController {
    return ComposeUIViewController {
        App(nativeBridge = bridge)
    }
}