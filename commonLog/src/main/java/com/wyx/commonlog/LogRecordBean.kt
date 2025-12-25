package com.wyx.commonlog

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "log_record")
data class LogRecordBean(@PrimaryKey(autoGenerate = true) val id: Long = 0,
                         @SerializedName("@timestamp") val timestamp: String,
                         val level: String,
                         val tag: String?,
                         val message: String)
