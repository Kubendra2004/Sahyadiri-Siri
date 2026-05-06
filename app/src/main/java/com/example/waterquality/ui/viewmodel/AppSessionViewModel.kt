package com.example.waterquality.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.waterquality.data.remote.AuthenticationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class AppSessionViewModel @Inject constructor(
    authenticationManager: AuthenticationManager
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = authenticationManager.isLoggedIn
}
