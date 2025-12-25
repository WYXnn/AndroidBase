package com.wyx.commonlog

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase

@Dao
interface LogDao {
    @Insert
    suspend fun insert(log: LogRecordBean)

    // 取出最早的 X 条日志
    @Query("SELECT * FROM log_record ORDER BY id ASC LIMIT :limit")
    suspend fun getOldestLogs(limit: Int): List<LogRecordBean>

    @Delete
    suspend fun delete(logs: List<LogRecordBean>)
}

@Database(entities = [LogRecordBean::class], version = 1)
abstract class LogDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
}