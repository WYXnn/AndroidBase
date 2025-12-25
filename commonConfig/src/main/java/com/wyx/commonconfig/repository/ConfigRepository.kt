package com.wyx.commonconfig.repository

import com.wyx.commonconfig.entity.IConfigRequest
import com.wyx.commonconfig.service.ConfigService
import com.wyx.commonnet.repository.BaseRepository
import kotlinx.coroutines.delay
import org.json.JSONObject
import javax.inject.Inject

class ConfigRepository @Inject constructor() : BaseRepository() {

    @Inject
    lateinit var mApi: ConfigService

    suspend fun postConfig(url : String, body : IConfigRequest?) = request<JSONObject> {
        mApi.postConfig(url, body).run {
            it.emit(this)
        }
    }

}