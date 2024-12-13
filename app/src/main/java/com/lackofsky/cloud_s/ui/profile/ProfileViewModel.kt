package com.lackofsky.cloud_s.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _isHeaderEdit = MutableLiveData<Boolean>(false)
    val isHeaderEdit: LiveData<Boolean> = _isHeaderEdit
    private val _isAboutEdit = MutableLiveData<Boolean>(false)
    val isAboutEdit: LiveData<Boolean> = _isAboutEdit
    private val _isInfoEdit = MutableLiveData<Boolean>(false)
    val isInfoEdit: LiveData<Boolean> = _isInfoEdit


    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user
    private val _editUser = MutableLiveData<UserDTO>()
     val editUser: LiveData<UserDTO> get() = _editUser

    private val _userInfo = MutableLiveData<UserInfo>()
    val userInfo: LiveData<UserInfo> get() = _userInfo
    private val _editUserInfo = MutableLiveData<UserInfo>()
    val editUserInfo: LiveData<UserInfo> get() = _editUserInfo

    private val maxImageSize = 1000000

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
            _editUser.value = UserDTO(_user.value!!.id,_user.value!!.fullName,_user.value!!.login,_user.value!!.ipAddr )

            userRepository.getUserInfoById(user.uniqueID).observeForever { userInfo ->
                _userInfo.value = userInfo
                _editUserInfo.value = _userInfo.value
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

        fun onConfirmUpdate() = viewModelScope.launch {
            _editUser.value?.let { userRepository.updateUser(_user.value!!.copy(login =it.login, fullName = it.fullName,)) }
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
        _editUser.value = UserDTO(_user.value!!.id,
            _user.value!!.fullName,
            _user.value!!.login,
            _user.value!!.ipAddr
        )
        _editUserInfo.value = _userInfo.value
    }
    fun closeEdit(){
        _isHeaderEdit.value = false
        _isAboutEdit.value = false
        _isInfoEdit.value = false
    }


    /*** settingNewImage*/
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    fun setImageUri(uri: Uri?,context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                try{
                    compressImageToByteArray(uri!!, context = context)?.let {


                        if (it.size < maxImageSize){
                            userRepository.updateUserInfo(
                                userInfo.value!!.copy(iconImg = it)
                            )
                        }
                    }
                }catch (e:Exception){
                    //TODO("предупреждение большого обьема файла")
                }

            }
        //}
    }
    fun setBannerUri(context: Context,uri: Uri?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                storageRepository.saveFileFromUri(context, uri!!, "profileBanner")
                userRepository.updateUserInfo(
                    userInfo.value!!.copy(bannerImgURI = uri.path!!)
                )
            }catch (e:Exception){
                //todo
            }

            }
    }
    fun getBannerBitmap(context: Context):Bitmap?{
        return storageRepository.loadBitmapFromFilesDir(context = context, fileName = "profileBanner")
    }
    private suspend fun compressImageToByteArray(uri: Uri,context: Context): ByteArray? {
        val imageLoader = ImageLoader(context)
        return withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(uri) // URI изображения
                .size(256, 256) // Размер для сжатия
                .build()

            val result = imageLoader.execute(request).drawable
            val bitmap = (result as? BitmapDrawable)?.bitmap ?: return@withContext null

            // Преобразование Bitmap в ByteArray
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Сжатие в JPEG с 80% качеством
            outputStream.toByteArray()
        }
    }
}