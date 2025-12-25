package com.wyx.examplebase.example.step

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue

class AuthOrchestratorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState = _uiState.asStateFlow()

    private val stepQueue: Queue<AuthStep> = LinkedList()

    init {
        fetchAuthFlow()
    }

    private fun fetchAuthFlow() {
        viewModelScope.launch {
            _uiState.update { it.copy(currentStep = AuthStep.Idle) }

            val mockBackendResponse = listOf(
                AuthStep.SmsVerify("138****8888", 60),
                AuthStep.FaceVerify("face_sdk_token_123"),
                AuthStep.SetPaymentPassword("请设置6位数字密码")
            )

            stepQueue.addAll(mockBackendResponse)
            proceedToNextStep()
        }
    }


    fun onStepResult(result: StepResult) {
        when (result) {
            is StepResult.Success -> {
                _uiState.update {
                    it.copy(collectedData = it.collectedData + result.resultData)
                }
                proceedToNextStep()
            }
            is StepResult.Failure -> {
                _uiState.update { it.copy(error = result.error) }
            }
            is StepResult.Cancelled -> {

            }
        }
    }

    private fun proceedToNextStep() {
        val nextStep = stepQueue.poll()
        if (nextStep == null) {
            handleFinalCommit()
        } else {
            _uiState.update { it.copy(currentStep = nextStep, error = null) }
        }
    }


    private fun handleFinalCommit() {
        val allData = _uiState.value.collectedData
        _uiState.update { it.copy(currentStep = AuthStep.Complete) }
    }
}