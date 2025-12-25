package com.wyx.commonlog

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.OkHttpClient

class LogUploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val client = LogConfig.client
        val url = LogConfig.esUrl

        if (client == null || url == null) {
            return Result.failure()
        }

        val database = Room.databaseBuilder(applicationContext, LogDatabase::class.java, "common_log.db").build()
        val uploader = LogUploader(client, url) // 使用拿到的 client 和 url

        val dao = database.logDao()
        val logs = dao.getOldestLogs(50)

        if (logs.isNotEmpty()) {
            val success = uploader.upload(logs)
            if (success) {
                dao.delete(logs)
                return Result.success()
            } else {
                return Result.retry()
            }
        }
        return Result.success()
    }
}