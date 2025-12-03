package com.broadcastdata.main

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.broadcastdata.main.actions.DirectoryWalker
import com.broadcastdata.main.actions.SimplePushNotification
import com.broadcastdata.main.services.OnBroadcastRecieveService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MainApplication() : Application() {
    @Inject
    lateinit var onBroadcastService: OnBroadcastRecieveService

    @Inject
    lateinit var directoryWalker: DirectoryWalker
}