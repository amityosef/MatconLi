package com.colman.matconli.features.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colman.matconli.data.repository.UserRepository
import com.colman.matconli.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MediatorLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _updateResult = MutableLiveData<Boolean?>()
    val updateResult: LiveData<Boolean?> = _updateResult

    private var userSource: LiveData<User?>? = null
    private var isActive = true

    override fun onCleared() {
        super.onCleared()
        isActive = false
        userSource?.let { _currentUser.removeSource(it) }
        userSource = null
    }

    fun loadCurrentUser() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _isLoading.postValue(false)
            _currentUser.postValue(null)
            return
        }

        _isLoading.postValue(true)

        UserRepository.shared.ensureUserExists(userId) { success ->
            if (isActive) {
                if (success) {
                    userSource?.let { _currentUser.removeSource(it) }

                    val source = UserRepository.shared.getUserByIdLiveData(userId)
                    userSource = source

                    _currentUser.addSource(source) { user ->
                        if (isActive) {
                            _currentUser.postValue(user)
                            _isLoading.postValue(false)
                        }
                    }
                } else {
                    _isLoading.postValue(false)
                    _currentUser.postValue(null)
                }
            }
        }
    }

    fun updateProfile(name: String, avatarUrl: String?) {
        val userId = auth.currentUser?.uid ?: return
        val email = auth.currentUser?.email ?: return

        _isLoading.value = true
        _updateResult.value = null

        val updatedUser = User(
            id = userId,
            name = name,
            email = email,
            avatarUrl = avatarUrl,
            lastUpdated = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            UserRepository.shared.updateUser(
                storageAPI = com.colman.matconli.data.models.StorageModel.StorageAPI.CLOUDINARY,
                image = null,
                user = updatedUser
            ) { success ->
                viewModelScope.launch {
                    withContext(Dispatchers.Main) {
                        _isLoading.value = false
                        _updateResult.value = success
                        if (success) {
                            _currentUser.value = updatedUser
                        }
                    }
                }
            }
        }
    }

    fun clearUpdateResult() {
        _updateResult.value = null
    }
}

