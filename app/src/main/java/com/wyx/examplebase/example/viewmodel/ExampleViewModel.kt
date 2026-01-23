package com.wyx.examplebase.example.viewmodel

import com.wyx.examplebase.example.repository.ExampleRepository
import com.wyx.examplebase.ktx.launchIO
import com.wyx.examplebase.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

@HiltViewModel
class ExampleViewModel @Inject constructor(private val mRepo: ExampleRepository) : BaseViewModel() {

    private val mCount = MutableStateFlow<Int>(0)
    val count: StateFlow<Int> = mCount.asStateFlow()

     fun addCount() {
         launchIO {
             mRepo.addCount(mCount.value)
                 .catch {

                 }
                 .collect {
                     mCount.value = it
                 }
         }
    }

}