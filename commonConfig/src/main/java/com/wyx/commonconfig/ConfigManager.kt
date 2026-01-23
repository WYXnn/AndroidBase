package com.wyx.commonconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wyx.commonconfig.entity.IConfigRequest
import com.wyx.commonconfig.repository.ConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigManager @Inject constructor(private val mRepo: ConfigRepository) : ViewModel() {

    private var mNameSpace : String = ""
    private var mNeedLoop = false
    private var mLoopMillis = 10 * 1000L

    private var mUrl = ""

    private var mParam : IConfigRequest? = null

    private var mJob: Job? = null

    private var mUseDiskMemory = false
    private var mUseRuntimeMemory = false
    private val mLastJson = JSONObject()
    private var mOnFail: (e: Throwable) -> Unit = { }
    private var mOnSuccess: (result : JSONObject) -> Unit = { }

    fun init(nameSpace : String?, onFail: (e: Throwable) -> Unit, onSuccess: (result : JSONObject) -> Unit, needLoop : Boolean = false, loopMillis : Long = 10*1000, useDiskMemory: Boolean = false, useRuntimeMemory: Boolean = false) : ConfigManager {
        mNameSpace = nameSpace ?: ""
        mNeedLoop = needLoop
        mLoopMillis = loopMillis
        mUseDiskMemory = useDiskMemory
        mUseRuntimeMemory = useRuntimeMemory
        mOnFail = onFail
        mOnSuccess = onSuccess
        return this
    }

    fun setUrl(url : String) : ConfigManager {
        mUrl = url
        return this
    }

    fun setCommonParam(param : IConfigRequest) : ConfigManager {
        mParam = param
        return this
    }

    fun stopLoop() {
        mNeedLoop = false
        mJob?.cancel()
    }

    fun getConfig() {
        if (mNeedLoop) {
            loopConfig()
        } else {
            singleConfig()
        }
    }

    private fun loopConfig() {
        mJob?.cancel()
        mJob = viewModelScope.launch(Dispatchers.IO) {
            while (mNeedLoop) {
                fetchData()
                    .catch {
                        mOnFail(it)
                    }
                    .collect {
                        mOnSuccess(it)
                    }
                delay(mLoopMillis)
            }
        }
    }

    private fun singleConfig() {
        mJob?.cancel()
        mJob = viewModelScope.launch ( Dispatchers.IO ) {
            fetchData()
                .catch {
                    mOnFail(it)
                }
                .collect {
                    mOnSuccess(it)
                }
        }
    }

    private suspend fun fetchData() : Flow<JSONObject> {
        return mRepo.postConfig(mUrl, mParam)
    }

}