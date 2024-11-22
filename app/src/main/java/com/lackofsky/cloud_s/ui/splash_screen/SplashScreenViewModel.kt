package com.lackofsky.cloud_s.ui.splash_screen

import android.content.Context
import android.util.Log
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashScreenViewModel @Inject constructor(@ApplicationContext val context: Context,
                                                private val userRepository: UserRepository
) :ViewModel() {
    private val _isUserLoggedIn = MutableStateFlow<Boolean?>(null)
    val isUserLoggedIn: StateFlow<Boolean?> get() = _isUserLoggedIn


    fun checkIsUserLoggedIn() = viewModelScope.launch {
        val userExists = userRepository.isUserExists()
        _isUserLoggedIn.value = userExists
    }
}