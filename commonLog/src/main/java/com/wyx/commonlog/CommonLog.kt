package com.wyx.commonlog

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import timber.log.Timber

object CommonLog {

    private var isInit = false

    fun init(context: Context, isDebug : Boolean, usePersistence: Boolean,
             esUrl: String,
             client: OkHttpClient ) {
        isInit = true
        Timber.plant(if (isDebug) Timber.DebugTree() else UploadTree(context,usePersistence,esUrl,client))
    }

    fun d(msg : String) {
        if (!isInit) {
            throw RuntimeException("CommonLog error, must call init")
        }
        Timber.d(msg)
    }

    fun e(msg : String) {
        if (!isInit) {
            throw RuntimeException("CommonLog error, must call init")
        }
        Timber.e(msg)
    }

    fun i(msg : String) {
        if (!isInit) {
            throw RuntimeException("CommonLog error, must call init")
        }
        Timber.i(msg)
    }

    fun v(msg : String) {
        if (!isInit) {
            throw RuntimeException("CommonLog error, must call init")
        }
        Timber.v(msg)
    }

    fun w(msg : String) {
        if (!isInit) {
            throw RuntimeException("CommonLog error, must call init")
        }
        Timber.w(msg)
    }

//    companion object {
//        private var commonLog : CommonLog? = null
//        private var mTag : String = ""
//
//
//        var isWriteToDb = false
//
//        fun getLog(tag : String) : CommonLog {
//            mTag = tag
//            if (commonLog == null) {
//                commonLog = CommonLog()
//            }
//            return commonLog!!
//        }

//    }

//    private var mContext : Context? = null
//
//    fun init(context: Context) {
//        mContext = context
//    }


//    fun d(msg : String) {
//        Log.d(mTag, msg)
//        if (isWriteToDb) {
//            writeToDb(msg)
//        }
//    }
//
//    fun e(msg : String) {
//        Log.e(mTag, msg)
//        if (isWriteToDb) {
//            writeToDb(msg)
//        }
//    }
//
//    fun i(msg : String) {
//        Log.e(mTag, msg)
//        if (isWriteToDb) {
//            writeToDb(msg)
//        }
//    }
//
//    fun v(msg : String) {
//        Log.v(mTag, msg)
//        if (isWriteToDb) {
//            writeToDb(msg)
//        }
//    }
//
//    fun w(msg : String) {
//        Log.w(mTag, msg)
//        if (isWriteToDb) {
//            writeToDb(msg)
//        }
//    }
//
//    fun writeToDb(msg : String) {
//        if (mContext != null) {
//            val threadId = Thread.currentThread().id
//            val threadName = Thread.currentThread().name
//            val bean = LogRecordBean(threadName,threadId,mTag,msg, System.currentTimeMillis(), null)
//            LogDbProvider.getDatabase(mContext!!).logRecordDao().insert(bean)
//        }
//    }
//
//    suspend fun getFromDb(limit : Int, offset : Int) : List<LogRecordBean> {
//        if (mContext != null) {
//            return LogDbProvider.getDatabase(mContext!!).logRecordDao().doQueryByLimit(limit, offset) ?: ArrayList()
//        }
//        return ArrayList()
//    }

}