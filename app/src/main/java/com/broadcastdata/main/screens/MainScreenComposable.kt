package com.broadcastdata.main.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.broadcastdata.main.screens.viewmodels.MainViewMode
import com.broadcastdata.main.ui.theme.BroadcastdataTheme


@Composable
fun MainScreen(){
    val viewModel : MainViewMode = hiltViewModel()
    val workerState = viewModel.workerState.collectAsState()

    val carNum = viewModel.carNumState.collectAsState()
    val carNumValidity = viewModel.carNumValidity.collectAsState()
    val curCarNum = viewModel.currentCarNum.collectAsState()

    val folderState = viewModel.selectedFolder.collectAsState()

    val context = LocalContext.current

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        emptyArray()
    } else {
        arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    val filePickerLaunch = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            viewModel.saveFileUriForWorker(uri, uri.lastPathSegment)
        }
        Toast.makeText(context, "Got uri $uri", Toast.LENGTH_SHORT).show()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ permissionMap ->
        val allGranted = permissionMap.values.all { it }
        if (allGranted || Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            filePickerLaunch.launch(null)
        }
        else{
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = "car number - ${curCarNum.value}",
            modifier = Modifier
                .padding(0.dp, 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            OutlinedTextField(
                value = carNum.value.orEmpty(),
                onValueChange = { it: String -> viewModel.validateCarNum(it)},
                isError = !carNumValidity.value,
                placeholder = {Text("a123aa70")}
            )
            Button(
                onClick = {
                    viewModel.saveCarNum()
                },
                enabled = carNumValidity.value && carNum.value !== curCarNum.value
            ) {
                Icon(Icons.Filled.Done, contentDescription = "save")
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {

            Text(
                "Folder: ${if (folderState.value.length > 10) "..." else ""}${folderState.value.takeLast(10)}",
                textAlign = TextAlign.Center
            )

            Button(
                onClick = {
                    permissionLauncher.launch(permissions)
                }
            ){
                Text(
                    text = "SELECT"
                )
            }
        }
        Button(
            onClick = {
                if (workerState.value) viewModel.stopWifiMonitoring() else viewModel.startWifiMonitoring()},
            modifier = Modifier
                .padding(8.dp)
        ) {
            Text(
                text = if (workerState.value) "STOP monitoring" else "START monitoring"
            )
        }
    }
}