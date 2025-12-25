package com.wyx.commonnetktor.repository

import de.jensklingenberg.ktorfit.http.GET

interface ExampleApi {
    @GET("people/1/")
    suspend fun getPerson(): String
}