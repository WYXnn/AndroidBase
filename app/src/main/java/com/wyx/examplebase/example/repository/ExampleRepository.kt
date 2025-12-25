package com.wyx.examplebase.example.repository

import com.wyx.examplebase.example.service.ExampleService
import com.wyx.commonnet.repository.BaseRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class ExampleRepository @Inject constructor() : BaseRepository() {

    @Inject
    lateinit var mApi: ExampleService

    fun addCount(count: Int) = request<Int> {
        delay(1000)
        it.emit(count + 1)
    }

    //emit将数据发射到catch或者collect上面
    suspend fun getData() = request<String> {
        val map = HashMap<String, String>()
        map.put("xxx", "xxx")
        mApi.doSomething(map).run {
            it.emit(this)
        }
    }

}