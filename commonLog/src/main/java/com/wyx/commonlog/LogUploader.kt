package com.wyx.commonlog

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class LogUploader(private val client: OkHttpClient, private val esUrl: String) {
    private val gson = Gson()

    fun upload(logs: List<LogRecordBean>): Boolean {
        if (logs.isEmpty()) return true

        logs.forEach { log ->
            try {
                val json = gson.toJson(log)
                val body = json.toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(esUrl).post(body).build()
                client.newCall(request).execute().close()
            } catch (e: Exception) {
                e.printStackTrace()
                return false // 标记失败
            }
        }
        return true
    }
}