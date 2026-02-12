package com.colman.matconli.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.colman.matconli.dao.AppLocalDB
import com.colman.matconli.model.User
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
                    try {
                        if (document.exists()) {
                            val user = User.Companion.fromJson(document.data ?: return@execute)
                            userDao.insert(user)
                            user.lastUpdated?.let {
                                if (it > User.Companion.lastUpdated) {
                                    User.Companion.lastUpdated = it
                                }
                            }
                            result.postValue(true)
                        } else {
                            result.postValue(false)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
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
                    try {
                        userDao.insert(user.copy(lastUpdated = System.currentTimeMillis()))
                        callback(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(false)
                    }
                }
            }
            .addOnFailureListener {
                try {
                    callback(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    fun createUser(user: User, callback: (Boolean) -> Unit) {
        usersCollection.document(user.id)
            .set(user.toJson)
            .addOnSuccessListener {
                executor.execute {
                    try {
                        userDao.insert(user.copy(lastUpdated = System.currentTimeMillis()))
                        callback(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback(false)
                    }
                }
            }
            .addOnFailureListener {
                try {
                    callback(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    fun ensureUserExists(userId: String, callback: (Boolean) -> Unit) {
        usersCollection.document(userId).get()
            .addOnSuccessListener { document ->
                try {
                    if (!document.exists()) {
                        val firebaseUser = FirebaseAuth.getInstance().currentUser
                        if (firebaseUser != null && firebaseUser.uid == userId) {
                            val newUser = User(
                                id = userId,
                                name = firebaseUser.displayName
                                    ?: firebaseUser.email?.substringBefore("@") ?: "User",
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
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback(false)
                }
            }
            .addOnFailureListener {
                try {
                    callback(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }
}