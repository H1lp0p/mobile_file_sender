package com.broadcastdata.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.broadcastdata.main.screens.MainScreen
import com.broadcastdata.main.screens.PassScreen
import com.broadcastdata.main.screens.viewmodels.MainViewMode
import com.broadcastdata.main.ui.theme.BroadcastdataTheme
import java.util.Objects

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainComposable()
        }
    }
}

sealed class Navigation(
    val route: String
){
    object PasswordRoute: Navigation("password")
    object MainScreen: Navigation("MainScreen")
}

@Composable
fun MainComposable(
    modifier: Modifier = Modifier
){
    val navcontroller = rememberNavController()

    BroadcastdataTheme{
        Scaffold { innerPadding ->
            NavHost(
                navController = navcontroller,
                startDestination = Navigation.PasswordRoute.route,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ){
                composable(Navigation.PasswordRoute.route){
                    PassScreen(
                        onSuccess = {
                            navcontroller.navigate(Navigation.MainScreen.route)
                        }
                    )
                }
                composable(Navigation.MainScreen.route){
                    MainScreen()
                }
            }
        }
    }
}