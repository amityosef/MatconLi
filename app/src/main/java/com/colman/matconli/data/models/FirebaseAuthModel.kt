package com.colman.matconli.data.models

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class FirebaseAuthModel {

    private var auth: FirebaseAuth = Firebase.auth

    fun signIn(email: String, password: String, completion: () -> Unit) {
        if (auth.currentUser != null) { completion(); return }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                completion()
            }
            .addOnFailureListener {
                Log.i("TAG", "signIn: failed ${it.message}")
            }
    }
}
