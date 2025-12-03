package com.broadcastdata.main.screens.viewmodels

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.broadcastdata.main.background.BackgroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewMode @Inject constructor(
    private val workManager: WorkManager,
    @ApplicationContext val context: Context,
) : ViewModel(){

    private val wifiCheckWorkName = "wifi_check_work"
    private val _workerState = MutableStateFlow(false)
    val workerState: StateFlow<Boolean> = _workerState.asStateFlow()

    private val _curNum = MutableStateFlow("")
    val currentCarNum = _curNum.asStateFlow()

    private val _selectedFolder = MutableStateFlow("none")
    val selectedFolder = _selectedFolder.asStateFlow()

    private val _carNum = MutableStateFlow<String?>(null)
    val carNumState: StateFlow<String?> = _carNum.asStateFlow()

    private val _carNumValidity = MutableStateFlow(false)
    val carNumValidity = _carNumValidity.asStateFlow()

    private fun checkMonitoringStatus() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(wifiCheckWorkName)
                .collect { workInfos ->
                    val isMonitoring = workInfos.any { it.state == WorkInfo.State.ENQUEUED }
                    _workerState.value = isMonitoring
                }
        }
    }

    fun startWifiMonitoring() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        //TODO switch to BroadcastReciever or network actions if android version allows
        val wifiCheckRequest = PeriodicWorkRequestBuilder<BackgroundService>(
            15, TimeUnit.MINUTES
        ).setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            wifiCheckWorkName,
            ExistingPeriodicWorkPolicy.KEEP,
            wifiCheckRequest
        )

        _workerState.value = true
    }

    fun stopWifiMonitoring() {
        workManager.cancelUniqueWork(wifiCheckWorkName)
    }

    fun saveFileUriForWorker(uri: Uri, originalName: String?) {
        val prefs = context.getSharedPreferences(WORKER_PREFS, Context.MODE_PRIVATE)
        prefs.edit {
            putString(PREFS_URI_KEY, uri.toString())
            putString(PREFS_NAME_KEY, originalName)
        }
        uri.lastPathSegment?.let {
            _selectedFolder.value = ".../$it"
        }
    }

    fun validateCarNum(newVal: String){

        _carNumValidity.value = newVal.isNotEmpty() && carNumRegex.matches(newVal)
        _carNum.value = newVal.ifEmpty { null }
    }

    fun saveCarNum(){
        if (_carNumValidity.value){
            context.getSharedPreferences(WORKER_PREFS, Context.MODE_PRIVATE).edit {
                putString(PREFS_CARNUM_KEY, _carNum.value)
            }
            _curNum.value = _carNum.value!!
        }
    }

    init {
        checkMonitoringStatus()

        context.getSharedPreferences(WORKER_PREFS, Context.MODE_PRIVATE)
            .getString(PREFS_CARNUM_KEY, null)?.let {
                _curNum.value = it
                validateCarNum(it)
            }

        context.getSharedPreferences(WORKER_PREFS, Context.MODE_PRIVATE)
            .getString(PREFS_URI_KEY, null)?.let{
                _selectedFolder.value = ".../${it.toUri().lastPathSegment}"
            }
    }

    companion object{
        const val WORKER_PREFS = "worker_presf"
        const val PREFS_URI_KEY = "file_uri"
        const val PREFS_NAME_KEY = "file_name"
        const val PREFS_CARNUM_KEY = "car_num"

        val carNumRegex = Regex("[а-я]\\d{3}[а-я]{2}\\d{2}")
    }
}