package com.colman.matconli.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executors

object RecipeRepository {

    private val db = Firebase.firestore.apply {
        firestoreSettings = firestoreSettings {
            setLocalCacheSettings(
                com.google.firebase.firestore.memoryCacheSettings { }
            )
        }
    }

    private val recipesCollection = db.collection("recipes")
    private val recipeDao by lazy { AppLocalDB.db.recipeDao() }
    private val executor = Executors.newSingleThreadExecutor()

    val recipes: LiveData<MutableList<Recipe>> by lazy { recipeDao.getAll() }

    fun refreshAllRecipes(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        recipesCollection
            .get()
            .addOnSuccessListener { snapshot ->
                executor.execute {
                    var latestUpdate = 0L
                    val recipesList = mutableListOf<Recipe>()

                    for (doc in snapshot.documents) {
                        val recipe = Recipe.fromJson(doc.data ?: continue)
                        recipesList.add(recipe)
                        recipe.lastUpdated?.let {
                            if (it > latestUpdate) latestUpdate = it
                        }
                    }

                    if (recipesList.isNotEmpty()) {
                        recipeDao.insertAll(recipesList)
                        if (latestUpdate > Recipe.lastUpdated) {
                            Recipe.lastUpdated = latestUpdate
                        }
                    }
                    result.postValue(true)
                }
            }
            .addOnFailureListener {
                result.postValue(false)
            }

        return result
    }

    fun getRecipeById(id: String, callback: (Recipe?) -> Unit) {
        executor.execute {
            val recipe = recipeDao.getById(id)
            callback(recipe)
        }
    }

    fun addRecipe(recipe: Recipe, callback: (Boolean) -> Unit) {
        recipesCollection.document(recipe.id)
            .set(recipe.toJson)
            .addOnSuccessListener {
                executor.execute {
                    recipeDao.insert(recipe.copy(lastUpdated = System.currentTimeMillis()))
                    callback(true)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun updateRecipe(recipe: Recipe, callback: (Boolean) -> Unit) {
        recipesCollection.document(recipe.id)
            .set(recipe.toJson)
            .addOnSuccessListener {
                executor.execute {
                    recipeDao.update(recipe.copy(lastUpdated = System.currentTimeMillis()))
                    callback(true)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun deleteRecipe(recipe: Recipe, callback: (Boolean) -> Unit) {
        recipesCollection.document(recipe.id)
            .delete()
            .addOnSuccessListener {
                executor.execute {
                    recipeDao.delete(recipe)
                    callback(true)
                }
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}

