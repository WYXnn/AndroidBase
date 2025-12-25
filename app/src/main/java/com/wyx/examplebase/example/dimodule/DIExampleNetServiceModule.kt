package com.wyx.examplebase.example.dimodule

import com.wyx.examplebase.example.service.ExampleService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DIExampleNetServiceModule {

    @Provides
    @Singleton
    fun provideHomeApiService(retrofit: Retrofit): ExampleService {
        return retrofit.create(ExampleService::class.java)
    }
}