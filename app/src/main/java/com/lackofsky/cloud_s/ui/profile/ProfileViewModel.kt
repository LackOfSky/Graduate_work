package com.lackofsky.cloud_s.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import coil.ImageLoader
import coil.request.ImageRequest
import com.lackofsky.cloud_s.data.model.User
import com.lackofsky.cloud_s.data.model.UserDTO
import com.lackofsky.cloud_s.data.model.UserInfo
import com.lackofsky.cloud_s.data.database.repository.UserRepository
import com.lackofsky.cloud_s.data.storage.StorageRepository
import com.lackofsky.cloud_s.data.storage.UserInfoStorageFolder
import com.lackofsky.cloud_s.service.ClientPartP2P
import com.lackofsky.cloud_s.service.client.usecase.ChangesNotifierUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository,
    private val clientPartP2P: ClientPartP2P,
    private val changesNotifierUseCase: ChangesNotifierUseCase
) : ViewModel() {

    private val _isHeaderEdit = MutableStateFlow(false)
    val isHeaderEdit: StateFlow<Boolean> = _isHeaderEdit
    private val _isAboutEdit = MutableStateFlow(false)
    val isAboutEdit: StateFlow<Boolean> = _isAboutEdit
    private val _isInfoEdit = MutableStateFlow(false)
    val isInfoEdit: StateFlow<Boolean> = _isInfoEdit


    val user: StateFlow<User?> = userRepository.getUserOwner() .stateIn(scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null)//get() = _user
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo





    private val _editUser = MutableStateFlow<UserDTO?>(null)
    val editUser: StateFlow<UserDTO?> get() = _editUser
    private val _editUserInfo = MutableStateFlow<UserInfo?>(null)
    val editUserInfo: StateFlow<UserInfo?> get() = _editUserInfo


    init {
        viewModelScope.launch {
            userRepository.getUserOwner().collect { user ->
                _editUser.value =  UserDTO(
                    user.id, user.fullName, user.login, user.ipAddr
            )
                launch {
                    userRepository.getUserInfoById(user.uniqueID).collect { info ->
                        _userInfo.value = info
                        _editUserInfo.value = _userInfo.value
                        Log.d("GrimBerry","_editUserInfo.value = ${_editUserInfo.value.toString()} userInfo")
                    }
                }
            }
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

    fun onConfirmUpdateNameLogin() = viewModelScope.launch {
        val user = user.first()!!.copy(
            login = _editUser.value!!.login,
            fullName = _editUser.value!!.fullName)
//        val user = _user.value!!.copy(
//            login = _editUser.value!!.login,
//            fullName = _editUser.value!!.fullName)
        userRepository.updateUser(user)
        changesNotifierUseCase.userChangesNotifierRequest(
            sendTo = clientPartP2P.activeFriends.value.values.toList(),
            user = user)
        changesNotifierUseCase.userChangesNotifierRequest(
            sendTo = clientPartP2P.activeStrangers.value.values.toList(),
            user = user)
    }
    fun onConfirmUpdateAboutUser() = viewModelScope.launch {
        val info = userInfo.value!!.copy(about = _editUserInfo.value!!.about)
            userRepository.updateUserInfo(info)
            changesNotifierUseCase.userInfoChangesNotifierRequest(
                sendTo = clientPartP2P.activeFriends.value.values.toList(),
                userInfo = info
            )
    }
    fun onConfirmUpdateUserInfo() = viewModelScope.launch {
        val info = userInfo.value!!.copy(info = _editUserInfo.value!!.info)
            userRepository.updateUserInfo(info)
            changesNotifierUseCase.userInfoChangesNotifierRequest(
                sendTo = clientPartP2P.activeFriends.value.values.toList(),
                userInfo = info
            )
    }

    fun onCancelUpdate() = viewModelScope.launch {
        Log.d("GrimBerry ui", user.value.toString()+"\n "+_editUser.value.toString())
        _editUser.value = UserDTO(user.value!!.id,
            user.value!!.fullName,
            user.value!!.login,
            user.value!!.ipAddr
        )
        _editUserInfo.value = userInfo.value
    }
    fun closeEdit(){
        _isHeaderEdit.value = false
        _isAboutEdit.value = false
        _isInfoEdit.value = false
    }


    /*** settingNewImage*/
    private val _selectedIconUri = MutableStateFlow<Uri?>(null)
    val selectedIconUri: StateFlow<Uri?> = _selectedIconUri
    private val _selectedBannerUri = MutableStateFlow<Uri?>(null)
    val selectedBannerUri: StateFlow<Uri?> = _selectedBannerUri

    fun setImageUri(uri: Uri?,context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                uri?.let {
                    val uriToSave = storageRepository.saveFileFromUri(context, it, uri.lastPathSegment!!,
                        folder=UserInfoStorageFolder.USER_ICONS)
                    userRepository.updateUserInfo(
                        userInfo.value!!.copy(iconImgURI = uriToSave.toString())
                    )
                    _selectedIconUri.value = uri
                }

            }

    }
    fun setBannerUri(context: Context,uri: Uri?) {
        CoroutineScope(Dispatchers.IO).launch {
            uri?.let {
                val uriToSave = storageRepository.saveFileFromUri(context, it, uri.lastPathSegment!!,
                    folder=UserInfoStorageFolder.USER_BANNERS)
                userRepository.updateUserInfo(
                    userInfo.value!!.copy(bannerImgURI = uriToSave.toString())
                )
                _selectedBannerUri.value = uri
            }
            }
    }
}