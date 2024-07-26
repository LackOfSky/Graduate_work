package com.lackofsky.cloud_s.ui.profile

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    //private val dataRepository: DataRepository
) : ViewModel() {

    private val _isHeaderEdit = MutableLiveData<Boolean>(false)
    val isHeaderEdit: LiveData<Boolean> = _isHeaderEdit
    private val _isAboutEdit = MutableLiveData<Boolean>(false)
    val isAboutEdit: LiveData<Boolean> = _isAboutEdit
    private val _isInfoEdit = MutableLiveData<Boolean>(false)
    val isInfoEdit: LiveData<Boolean> = _isInfoEdit

    val _currentUser = MutableStateFlow<User>(
        User("John Doe", //TODO подхват с БД
        "@just_someone",
            "     Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
                    "sed do eiusmod tempor incididunt " +
                    "ut labore et dolore magna aliqua incididunt ut labore et dolore magna aliqua. \n",

            "     Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do " +
                    "eiusmod tempor incididunt ut labore et dolore magna aliqua incididunt ut " +
                    "labore et dolore magna aliqua. \n",
            )
    )
    val currentUser : StateFlow<User> = _currentUser

    val drawerState = mutableStateOf(DrawerState(DrawerValue.Closed))
//
    fun setIsHeaderEdit(newValue: Boolean) {
        _isHeaderEdit.value = newValue
    }
    fun setIsAboutEdit(newValue: Boolean) {
        _isAboutEdit.value = newValue
    }
    fun setIsInfoEdit(newValue: Boolean) {
        _isInfoEdit.value = newValue
    }

    fun updateName(name: String){
        _currentUser.value = _currentUser.value.copy(fullName = name)
    }
    fun updateLogin(login: String){
        _currentUser.value = _currentUser.value.copy(login = login)
    }
    fun updateAbout(about: String){
        _currentUser.value = _currentUser.value.copy(about = about)
    }
    fun updateInfo(info: String){
        _currentUser.value = _currentUser.value.copy(info = info)
    }

    fun openDrawer() {
        viewModelScope.launch {
            drawerState.value.open()
        }
    }

    fun closeDrawer() {
        viewModelScope.launch {
            drawerState.value.close()
        }
    }
}

