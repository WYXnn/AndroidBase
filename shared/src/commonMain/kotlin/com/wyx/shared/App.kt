package com.wyx.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun App(nativeBridge: NativeActionBridge) {
    var kotlinText by remember { mutableStateOf("Hello from Compose!") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = kotlinText)

        Spacer(modifier = Modifier.height(20.dp))

        // 1. 调用 iOS 方法
        Button(onClick = {
            nativeBridge.playSystemSound()
        }) {
            Text("Call iOS Sound")
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. 协程演示
        Button(onClick = {
            scope.launch {
                kotlinText = "Loading..."
                // 模拟耗时操作，协程在 iOS 上也能正常挂起恢复
                delay(2000)
                kotlinText = "Data loaded from Coroutine!\niOS Version: ${nativeBridge.getAppVersion()}"
            }
        }) {
            Text("Test Coroutine")
        }
    }
}