package com.wyx.examplebase.example.service

import retrofit2.http.GET
import retrofit2.http.QueryMap

interface ExampleService {

    @GET("/xxxx")
    suspend fun doSomething(@QueryMap params: Map<String, String>): String

    suspend fun addCount(count: Int) : Int

}