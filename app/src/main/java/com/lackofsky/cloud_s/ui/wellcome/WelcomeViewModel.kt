package com.lackofsky.cloud_s.ui.wellcome

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lackofsky.cloud_s.data.dao.UserDao
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.prefs.Preferences
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(private val userRepository: UserRepository) : ViewModel() {

    fun saveUser(user: User, userInfo: UserInfo) {
        viewModelScope.launch {
            userRepository.insertUser(user)
            userRepository.insertUserInfo(userInfo)
        }
    }

}