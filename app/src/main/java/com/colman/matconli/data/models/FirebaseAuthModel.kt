package com.colman.matconli.data.models

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class FirebaseAuthModel {

    private var auth: FirebaseAuth = Firebase.auth

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun signIn(email: String, password: String, onSuccess: (FirebaseUser?) -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Log.i("FirebaseAuthModel", "signIn: success")
                onSuccess(auth.currentUser)
            }
            .addOnFailureListener { e ->
                Log.i("FirebaseAuthModel", "signIn: failed ${e.message}")
                onFailure(e.message ?: "Login failed")
            }
    }

    fun signUp(email: String, password: String, onSuccess: (FirebaseUser?) -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                Log.i("FirebaseAuthModel", "signUp: success")
                onSuccess(authResult.user)
            }
            .addOnFailureListener { e ->
                Log.i("FirebaseAuthModel", "signUp: failed ${e.message}")
                onFailure(e.message ?: "Registration failed")
            }
    }

    fun signOut() {
        auth.signOut()
        Log.i("FirebaseAuthModel", "signOut: success")
    }
}
