package com.wyx.commonconfig.service

import com.wyx.commonconfig.entity.IConfigRequest
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface ConfigService {

    @POST
    suspend fun postConfig(@Url url: String, @Body body: IConfigRequest?): JSONObject

}