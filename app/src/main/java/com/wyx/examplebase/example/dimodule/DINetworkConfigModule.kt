package com.wyx.examplebase.example.dimodule

import com.wyx.examplebase.example.repository.NetworkConfigImpl
import com.wyx.commonnet.network.INetworkConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//@Module
//@InstallIn(SingletonComponent::class)
//abstract class DINetworkConfigModule {
//
//    @Binds
//    abstract fun bindNetworkConfig(config : NetworkConfigImpl) : INetworkConfig
//
//}

@Module
@InstallIn(SingletonComponent::class)
class DINetworkConfigModule {
    @Singleton
    @Provides
    fun provideDiImpl(config: NetworkConfigImpl): INetworkConfig {
        return config
    }
}