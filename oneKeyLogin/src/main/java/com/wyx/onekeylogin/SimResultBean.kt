package com.wyx.onekeylogin

data class SimResultBean(val resultCode : Long, val resultMsg : String, val seq : String, var resultData : OneKeyLoginResultBean) {
}