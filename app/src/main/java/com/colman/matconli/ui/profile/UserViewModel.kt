package com.colman.matconli.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colman.matconli.model.User
import com.colman.matconli.model.UserRepository
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

    fun loadCurrentUser() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        UserRepository.ensureUserExists(userId) { success ->
            if (success) {
                userSource?.let { _currentUser.removeSource(it) }

                val source = UserRepository.getUserById(userId)
                userSource = source

                _currentUser.addSource(source) { user ->
                    _currentUser.value = user
                    _isLoading.value = false
                }
            } else {
                _isLoading.value = false
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
            UserRepository.updateUser(updatedUser) { success ->
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

