package com.wyx.kmpmodule

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


object SharedPlatformViewModel {

    private val mMsg = MutableStateFlow<PlatformAction>(PlatformAction.TriggerHapticFeedback)
    val msg: StateFlow<PlatformAction> = mMsg.asStateFlow()

    val channel = Channel<PlatformAction>()


    fun onToastButtonClicked(message: PlatformAction) {
        // 3. UI 触发事件，ViewModel 将其发送到 Channel
        mMsg.value = message
        GlobalScope.launch {
            channel.send(message)
        }
    }

     suspend fun pickImage(message: String): String? {
        // 3. 创建 CompletableDeferred 对象
//        val deferred = CompletableDeferred<String?>()
//
//        // 4. 将请求和 CompletableDeferred 一起发送到 Channel
//        _actions.send(ImagePickerRequest(deferred, message))
//
//        // 5. 暂停 Coroutine，等待原生层完成操作并调用 complete()
//        val result = deferred.await()
//        print("wyx111 from native callback:$result")
//        return result
         return ""
    }
}