package com.wyx.examplebase.example.step

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class StepExampleUI : AppCompatActivity() {

    val mViewModel by viewModels<AuthOrchestratorViewModel>()

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        lifecycleScope.launch {
            mViewModel.uiState.collect {
                when (val step = it.currentStep) {
                    is AuthStep.Complete -> {
                        TODO()
                    }
                    is AuthStep.FaceVerify -> {

                    }
                    is AuthStep.Idle -> {

                    }
                    is AuthStep.Loading -> {

                    }
                    is AuthStep.SetPaymentPassword -> {

                    }
                    is AuthStep.SmsVerify -> {

                    }
                }
            }
        }
    }

}