package com.broadcastdata.di

import com.broadcastdata.main.services.OnBroadcastRecieveRequestImpl
import com.broadcastdata.main.services.OnBroadcastRecieveService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnotherAppModule {

    @Binds
    @Singleton
    abstract fun bindOnBroadcastRecieveService(
        impl: OnBroadcastRecieveRequestImpl
    ): OnBroadcastRecieveService
}