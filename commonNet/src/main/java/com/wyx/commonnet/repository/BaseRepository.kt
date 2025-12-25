package com.wyx.commonnet.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

open class BaseRepository {

    protected fun <T> request(
        requestBlock: suspend (FlowCollector<T>) -> Unit)
            : Flow<T> {
        return flow(requestBlock).flowOn(Dispatchers.IO)
    }

}