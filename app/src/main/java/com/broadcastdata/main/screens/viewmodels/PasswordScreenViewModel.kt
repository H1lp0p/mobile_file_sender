package com.broadcastdata.main.screens.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PasswordScreenViewModel @Inject constructor(

) : ViewModel() {

    private val _passwordFieldState = MutableStateFlow("")
    val passwordFieldState = _passwordFieldState.asStateFlow()

    fun check(): Boolean{
        return _passwordFieldState.value == PASSWORD
    }

    fun onPasswordFieldChange(newValue: String){
        _passwordFieldState.value = newValue
    }

    //TODO move password to build settings..?
    companion object{
        const val PASSWORD = "pass"
    }
}