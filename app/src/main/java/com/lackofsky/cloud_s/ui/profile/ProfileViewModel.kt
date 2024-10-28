package com.lackofsky.cloud_s.ui.profile

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isHeaderEdit = MutableLiveData<Boolean>(false)
    val isHeaderEdit: LiveData<Boolean> = _isHeaderEdit
    private val _isAboutEdit = MutableLiveData<Boolean>(false)
    val isAboutEdit: LiveData<Boolean> = _isAboutEdit
    private val _isInfoEdit = MutableLiveData<Boolean>(false)
    val isInfoEdit: LiveData<Boolean> = _isInfoEdit


    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user
    private val _editUser = MutableLiveData<User>()
     val editUser: LiveData<User> get() = _editUser

    private val _userInfo = MutableLiveData<UserInfo>()
    val userInfo: LiveData<UserInfo> get() = _userInfo
    private val _editUserInfo = MutableLiveData<UserInfo>()
    val editUserInfo: LiveData<UserInfo> get() = _editUserInfo

    init {
        viewModelScope.launch {
            //todo стартовая страница для ввода этих данных
//            if(userRepository.getUserOwner().isInitialized){
//                userRepository.insertUser(
//                    User(1,"John Doe", //TODO подхват с БД
//                    "@just_someone",
//                            "030303030")
//                )}
            }
        loadUserOwner()
    }

    private fun loadUserOwner() = viewModelScope.launch {
        userRepository.getUserOwner().observeForever { user ->
            _user.value = user
            _editUser.value = _user.value
        }
        userRepository.getUserInfoById(1).observeForever { userInfo ->
            _userInfo.value = userInfo
            _editUserInfo.value = _userInfo.value
        }

    }

    fun onUserNameChange(newName: String) {
        Log.d("GrimBerry vm", newName)
        _editUser.value = _editUser.value?.copy(fullName = newName)
    }
    fun onUserLoginChange(newLogin: String) {
        _editUser.value = _editUser.value?.copy(login = newLogin)
    }
    fun onUserAboutUpdate(newAbout: String) {
        _editUserInfo.value = _editUserInfo.value?.copy(about= newAbout)
    }
    fun onUserAdditionalInfoUpdate(newInfo: String) {
        _editUserInfo.value = _editUserInfo.value?.copy(info= newInfo)
    }
        fun setIsHeaderEdit(newValue: Boolean) {
            _isHeaderEdit.value = newValue
        }

        fun setIsAboutEdit(newValue: Boolean) {
            _isAboutEdit.value = newValue
        }

        fun setIsInfoEdit(newValue: Boolean) {
            _isInfoEdit.value = newValue
        }

        fun onConfirmUpdate() = viewModelScope.launch {
            _editUser.value?.let { userRepository.updateUser(it) }
        }
    fun onConfirmUpdateNameLogin() = viewModelScope.launch {
        _user.value!!.let{
            userRepository.updateUser(it.copy(
                login = editUser.value!!.login,
                fullName = editUser.value!!.fullName))
        }

    }
    fun onConfirmUpdateAboutUser() = viewModelScope.launch {
        _userInfo.value?.let{
            userRepository.updateUserInfo(it.copy(
                about = editUserInfo.value!!.about))
        }
    }
    fun onConfirmUpdateUserInfo() = viewModelScope.launch {
        _userInfo.value?.let{
            userRepository.updateUserInfo(it.copy(
                info = editUserInfo.value!!.info))
        }
    }

    fun onCancelUpdate() = viewModelScope.launch {
        _editUser.value = _user.value
        _editUserInfo.value = _userInfo.value
    }
    fun closeEdit(){
        _isHeaderEdit.value = false
        _isAboutEdit.value = false
        _isInfoEdit.value = false
    }

}