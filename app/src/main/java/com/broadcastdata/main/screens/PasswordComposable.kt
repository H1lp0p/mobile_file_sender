package com.broadcastdata.main.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.broadcastdata.main.screens.viewmodels.PasswordScreenViewModel


@Composable
fun PassScreen(
    modifier: Modifier = Modifier,
    onSuccess: () -> Unit
){
    val viewModel: PasswordScreenViewModel = hiltViewModel()
    val passwordField = viewModel.passwordFieldState.collectAsState()

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    )   {

        Text(
            "Password",
            modifier = Modifier
                .padding(0.dp, 16.dp)
        )

        OutlinedTextField(
            value = passwordField.value,
            onValueChange = { viewModel.onPasswordFieldChange(it) },
            modifier = Modifier
                .padding(0.dp, 0.dp, 0.dp, 32.dp),
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (viewModel.check()){
                        onSuccess()
                    }
                    else{
                        Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        )

        IconButton(
            onClick = {
                keyboardController?.hide()
                if (viewModel.check()){
                    onSuccess()
                }
                else{
                    Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Icon(
                Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = "Continue"
            )
        }
    }
}