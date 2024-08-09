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

    init {
        viewModelScope.launch {
            //todo стартовая страница для ввода этих данных
            if(userRepository.getUserOwner().isInitialized){
                userRepository.insert(
                    User(1,"John Doe", //TODO подхват с БД
                    "@just_someone",
                    "     Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
                            "sed do eiusmod tempor incididunt " +
                            "ut labore et dolore magna aliqua incididunt ut labore et dolore magna aliqua. \n",

                    "     Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do " +
                            "eiusmod tempor incididunt ut labore et dolore magna aliqua incididunt ut " +
                            "labore et dolore magna aliqua. \n",)
                )}
            }
        loadUserOwner()
    }

    private fun loadUserOwner() = viewModelScope.launch {
        userRepository.getUserOwner().observeForever { user ->
            _user.value = user
            _editUser.value = _user.value
        }
    }

    fun onUserNameChange(newName: String) {
        _editUser.value = _editUser.value?.copy(fullName = newName)
    }
    fun onUserLoginChange(newLogin: String) {
        _editUser.value = _editUser.value?.copy(login = newLogin)
    }
    fun onUserAboutUpdate(newAbout: String) {
        _editUser.value = _editUser.value?.copy(about= newAbout)
    }
    fun onUserAdditionalInfoUpdate(newInfo: String) {
        _editUser.value = _editUser.value?.copy(info= newInfo)
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
            _editUser.value?.let { userRepository.update(it) }
        }
    fun onConfirmUpdateNameLogin() = viewModelScope.launch {
        _user.value?.let{
            userRepository.update(it.copy(
                login = editUser.value!!.login,
                fullName = editUser.value!!.fullName))
        }

    }
    fun onConfirmUpdateAboutUser() = viewModelScope.launch {
        _user.value?.let{
            userRepository.update(it.copy(
                about = editUser.value!!.about))
        }
    }
    fun onConfirmUpdateUserInfo() = viewModelScope.launch {
        _user.value?.let{
            userRepository.update(it.copy(
                info = editUser.value!!.info))
        }
    }

    fun onCancelUpdate() = viewModelScope.launch {
        _editUser.value = _user.value
    }
    fun closeEdit(){
        _isHeaderEdit.value = false
        _isAboutEdit.value = false
        _isInfoEdit.value = false
    }

}