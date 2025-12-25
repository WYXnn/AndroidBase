package com.wyx.commonconfig.dimodule

import com.wyx.commonconfig.service.ConfigService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DIConfigServiceModule {

    @Provides
    @Singleton
    fun provideConfigApiService(retrofit: Retrofit): ConfigService {
        return retrofit.create(ConfigService::class.java)
    }

}