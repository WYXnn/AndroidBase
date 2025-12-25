package com.wyx.examplebase.example.step


sealed class AuthStep {
    data class SmsVerify(val phoneMask: String, val timeout: Int) : AuthStep()
    data class FaceVerify(val sdkToken: String) : AuthStep()
    data class SetPaymentPassword(val hint: String) : AuthStep()

    object Complete : AuthStep()
    object Loading : AuthStep()
    object Idle : AuthStep()
}


sealed class StepResult {
    data class Success(val resultData: Map<String, Any>) : StepResult()
    data class Failure(val error: String) : StepResult()
    object Cancelled : StepResult()
}


data class AuthState(
    val currentStep: AuthStep = AuthStep.Idle,
    val collectedData: Map<String, Any> = emptyMap(),
    val error: String? = null
)