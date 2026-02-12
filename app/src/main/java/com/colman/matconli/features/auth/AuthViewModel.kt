package com.colman.matconli.features.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.matconli.data.models.FirebaseAuthModel
import com.colman.matconli.data.repository.UserRepository
import com.colman.matconli.model.User
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {

    private val authModel = FirebaseAuthModel()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    init {
        _currentUser.value = authModel.currentUser
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        _authState.value = AuthState.Loading
        authModel.signIn(
            email = email,
            password = password,
            onSuccess = { user ->
                val userId = user?.uid
                if (userId != null) {
                    UserRepository.ensureUserExists(userId) { success ->
                        _currentUser.value = authModel.currentUser
                        _authState.value = AuthState.Success
                    }
                } else {
                    _currentUser.value = authModel.currentUser
                    _authState.value = AuthState.Success
                }
            },
            onFailure = { errorMessage ->
                _authState.value = AuthState.Error(errorMessage)
            }
        )
    }

    fun register(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        if (password != confirmPassword) {
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }

        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }

        _authState.value = AuthState.Loading
        authModel.signUp(
            email = email,
            password = password,
            onSuccess = { user ->
                val userId = user?.uid ?: return@signUp
                val userEmail = user.email ?: email

                val newUser = User(
                    id = userId,
                    name = name,
                    email = userEmail,
                    avatarUrl = null,
                    lastUpdated = null
                )

                UserRepository.createUser(newUser) { success ->
                    if (success) {
                        _currentUser.value = authModel.currentUser
                        _authState.value = AuthState.Success
                    } else {
                        _authState.value = AuthState.Error("Failed to create user profile")
                    }
                }
            },
            onFailure = { errorMessage ->
                _authState.value = AuthState.Error(errorMessage)
            }
        )
    }

    fun logout() {
        authModel.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun isLoggedIn(): Boolean = authModel.isLoggedIn()

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    sealed class AuthState {
        data object Idle : AuthState()
        data object Loading : AuthState()
        data object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
}

