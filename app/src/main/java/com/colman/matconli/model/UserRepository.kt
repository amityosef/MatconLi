package com.colman.matconli.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executors

object UserRepository {

    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")
    private val userDao by lazy { AppLocalDB.db.userDao() }
    private val executor = Executors.newSingleThreadExecutor()

    fun getUserById(userId: String): LiveData<User?> {
        refreshUser(userId)
        return userDao.getById(userId)
    }

    fun refreshUser(userId: String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { document ->
                executor.execute {
                    if (document.exists()) {
                        val user = User.fromJson(document.data ?: return@execute)
                        userDao.insert(user)
                        user.lastUpdated?.let {
                            if (it > User.lastUpdated) {
                                User.lastUpdated = it
                            }
                        }
                        result.postValue(true)
                    } else {
                        result.postValue(false)
                    }
                }
            }
            .addOnFailureListener {
                result.postValue(false)
            }

        return result
    }

    fun updateUser(user: User, callback: (Boolean) -> Unit) {
        usersCollection.document(user.id)
            .set(user.toJson)
            .addOnSuccessListener {
                executor.execute {
                    userDao.insert(user.copy(lastUpdated = System.currentTimeMillis()))
                    callback(true)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun createUser(user: User, callback: (Boolean) -> Unit) {
        usersCollection.document(user.id)
            .set(user.toJson)
            .addOnSuccessListener {
                executor.execute {
                    userDao.insert(user.copy(lastUpdated = System.currentTimeMillis()))
                    callback(true)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun ensureUserExists(userId: String, callback: (Boolean) -> Unit) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    if (firebaseUser != null && firebaseUser.uid == userId) {
                        val newUser = User(
                            id = userId,
                            name = firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "User",
                            email = firebaseUser.email ?: "",
                            avatarUrl = firebaseUser.photoUrl?.toString(),
                            lastUpdated = null
                        )
                        createUser(newUser, callback)
                    } else {
                        callback(false)
                    }
                } else {
                    callback(true)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}

