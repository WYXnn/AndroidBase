package com.wyx.commonlog

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class UploadTree(private val context: Context,
                 private val usePersistence: Boolean,
                 private val esUrl: String,
                 private val client: OkHttpClient
) : Timber.Tree() {

    private val uploader = LogUploader(client, esUrl)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    private val logChannel = Channel<LogRecordBean>(Channel.UNLIMITED)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val database by lazy {
        Room.databaseBuilder(context, LogDatabase::class.java, "common_log.db").build()
    }

    init {
        LogConfig.client = client
        LogConfig.esUrl = esUrl
        if (usePersistence) {
            val uploadWork = PeriodicWorkRequestBuilder<LogUploadWorker>(
                1, TimeUnit.MINUTES
            )
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "UploadLogs", ExistingPeriodicWorkPolicy.KEEP, uploadWork
            )
        } else {
            startMemoryConsumer()
        }
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val logEvent = LogRecordBean(
            timestamp = dateFormat.format(Date()),
            level = getPriorityString(priority),
            tag = tag ?: "App",
            message = if (t != null) "$message\n${Log.getStackTraceString(t)}" else message
        )

        if (usePersistence) {
            scope.launch {
                database.logDao().insert(logEvent)
            }
        } else {
            logChannel.trySend(logEvent)
        }
    }

    private fun startMemoryConsumer() = scope.launch {
        val buffer = mutableListOf<LogRecordBean>()
        while (isActive) {
            withTimeoutOrNull(5000) {
                for (log in logChannel) {
                    buffer.add(log)
                    if (buffer.size >= 10) break
                }
            }
            if (buffer.isNotEmpty()) {
                uploader.upload(ArrayList(buffer))
                buffer.clear()
            }
        }
    }

    private fun getPriorityString(priority: Int) = when (priority) {
        Log.ERROR -> "ERROR"
        Log.WARN -> "WARN"
        Log.INFO -> "INFO"
        Log.DEBUG -> "DEBUG"
        else -> "VERBOSE"
    }
}