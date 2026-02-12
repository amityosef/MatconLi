package com.colman.matconli.data.repository

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.colman.matconli.dao.AppLocalDB
import com.colman.matconli.dao.AppLocalDbRepository
import com.colman.matconli.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executors

typealias UserCompletion = (User?) -> Unit

class UserRepository private constructor() {

    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler.createAsync(Looper.getMainLooper())
    private val database: AppLocalDbRepository = AppLocalDB.db

    companion object Companion {
        val shared = UserRepository()
    }

    fun getUserById(userId: String): LiveData<User?> {
        refreshUser(userId)
        return database.userDao().getById(userId)
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
                            database.userDao().insert(user)
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

    fun updateUser(user: User, completion: (Boolean) -> Unit) {
        usersCollection.document(user.id)
            .set(user.toJson)
            .addOnSuccessListener {
                executor.execute {
                    try {
                        database.userDao().insert(user.copy(lastUpdated = System.currentTimeMillis()))
                        mainHandler.post {
                            completion(true)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mainHandler.post {
                            completion(false)
                        }
                    }
                }
            }
            .addOnFailureListener {
                mainHandler.post {
                    completion(false)
                }
            }
    }

    fun createUser(user: User, completion: (Boolean) -> Unit) {
        usersCollection.document(user.id)
            .set(user.toJson)
            .addOnSuccessListener {
                executor.execute {
                    try {
                        database.userDao().insert(user.copy(lastUpdated = System.currentTimeMillis()))
                        mainHandler.post {
                            completion(true)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        mainHandler.post {
                            completion(false)
                        }
                    }
                }
            }
            .addOnFailureListener {
                mainHandler.post {
                    completion(false)
                }
            }
    }

    fun ensureUserExists(userId: String, completion: (Boolean) -> Unit) {
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
                            createUser(newUser, completion)
                        } else {
                            mainHandler.post {
                                completion(false)
                            }
                        }
                    } else {
                        mainHandler.post {
                            completion(true)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mainHandler.post {
                        completion(false)
                    }
                }
            }
            .addOnFailureListener {
                mainHandler.post {
                    completion(false)
                }
            }
    }
}