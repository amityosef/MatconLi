package com.colman.matconli.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.colman.matconli.model.User
import com.colman.matconli.model.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    init {
        _currentUser.value = auth.currentUser
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    UserRepository.ensureUserExists(userId) { success ->
                        _currentUser.value = auth.currentUser
                        _authState.value = AuthState.Success
                    }
                } else {
                    _currentUser.value = auth.currentUser
                    _authState.value = AuthState.Success
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
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
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener
                val userEmail = authResult.user?.email ?: email

                val newUser = User(
                    id = userId,
                    name = name,
                    email = userEmail,
                    avatarUrl = null,
                    lastUpdated = null
                )

                UserRepository.createUser(newUser) { success ->
                    if (success) {
                        _currentUser.value = auth.currentUser
                        _authState.value = AuthState.Success
                    } else {
                        _authState.value = AuthState.Error("Failed to create user profile")
                    }
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

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

