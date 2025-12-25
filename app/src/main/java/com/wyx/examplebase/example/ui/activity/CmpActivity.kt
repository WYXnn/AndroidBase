package com.wyx.examplebase.example.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wyx.unicombase.R
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import com.wyx.kmpmodule.CmpView
import com.wyx.kmpmodule.PlatformAction
import com.wyx.kmpmodule.SharedPlatformViewModel
import kotlinx.coroutines.launch

class CmpActivity : ComponentActivity() {

    private val mSharedPlatformViewModel = SharedPlatformViewModel

    companion object {
        fun start(context : Context) {
            context.startActivity(Intent(context, CmpActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CmpView()
        }

        lifecycleScope.launch {
            for (action in mSharedPlatformViewModel.channel) {
                when (action) {
                    is PlatformAction.NavigateToNative -> {

                    }
                    is PlatformAction.ShowToast -> {

                    }
                    is PlatformAction.TriggerHapticFeedback -> {

                    }
                    is PlatformAction.needCallback -> {
                        Log.d("wyx111", action.content)
                        action.callback("native")
                    }
                }
            }
            mSharedPlatformViewModel.msg.collect {
                when (it) {
                    is PlatformAction.NavigateToNative -> {

                    }
                    is PlatformAction.ShowToast -> {

                    }
                    is PlatformAction.TriggerHapticFeedback -> {

                    }
                    is PlatformAction.needCallback -> {
                        Log.d("wyx111", it.content)
                        it.callback("native")
                    }
                }
            }
//            mSharedPlatformViewModel.actions.collect { action ->
//                when (action) {
//                    is PlatformAction.ShowToast -> {
//
//                    }
//                    is PlatformAction.NavigateToNative -> {
//                        // 执行原生导航
//                    }
//                    is PlatformAction.needCallback -> {
//                        Log.d("wyx111", action.content)
//                        action.callback("native")
//                    }
//                    else -> {}
//                }
////                Log.d("wyx111", action.message)
////                action.deferred.complete("native")
//            }
        }
    }
}