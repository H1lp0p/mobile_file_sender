package com.broadcastdata.di

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.broadcastdata.main.actions.DirectoryWalker
import com.broadcastdata.main.actions.SimplePushNotification
import com.broadcastdata.main.services.OnBroadcastRecieveRequestImpl
import com.broadcastdata.main.services.OnBroadcastRecieveService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager{
        return WorkManager.getInstance(context)
    }

    @Provides
    fun provideDirWalker(@ApplicationContext context: Context): DirectoryWalker{
        return DirectoryWalker(context)
    }

    @Provides
    @Singleton
    fun provideSimplePushNotification(@ApplicationContext context: Context): SimplePushNotification {
        return SimplePushNotification(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}