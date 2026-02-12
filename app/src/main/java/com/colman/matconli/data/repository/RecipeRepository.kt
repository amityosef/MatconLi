package com.colman.matconli.data.repository

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.colman.matconli.dao.AppLocalDB
import com.colman.matconli.dao.AppLocalDbRepository
import com.colman.matconli.data.models.FirebaseModel
import com.colman.matconli.model.Recipe
import java.util.concurrent.Executors

class RecipeRepository private constructor() {

    private val firebaseModel = FirebaseModel()
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler.createAsync(Looper.getMainLooper())
    private val database: AppLocalDbRepository = AppLocalDB.db

    val recipes: LiveData<MutableList<Recipe>> by lazy {
        database.recipeDao().getAll()
    }

    companion object Companion {
        val shared = RecipeRepository()
    }

    fun getAllRecipes(): LiveData<MutableList<Recipe>> {
        return recipes
    }

    fun refreshAllRecipes(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        val lastUpdated = Recipe.Companion.lastUpdated

        firebaseModel.getAllRecipes(lastUpdated) { recipesList ->
            executor.execute {
                var time = lastUpdated

                for (recipe in recipesList) {
                    database.recipeDao().insert(recipe)
                    recipe.lastUpdated?.let { recipeLastUpdated ->
                        if (time < recipeLastUpdated) {
                            time = recipeLastUpdated
                        }
                    }
                }

                if (recipesList.isNotEmpty()) {
                    Recipe.Companion.lastUpdated = time
                }
                result.postValue(true)
            }
        }

        return result
    }

    fun getRecipeById(id: String, callback: (Recipe?) -> Unit) {
        executor.execute {
            val recipe = database.recipeDao().getById(id)
            mainHandler.post {
                callback(recipe)
            }
        }
    }

    fun addRecipe(recipe: Recipe, completion: (Boolean) -> Unit) {
        firebaseModel.saveRecipe(recipe) {
            executor.execute {
                database.recipeDao().insert(recipe.copy(lastUpdated = System.currentTimeMillis()))
                mainHandler.post {
                    completion(true)
                }
            }
        }
    }

    fun updateRecipe(recipe: Recipe, completion: (Boolean) -> Unit) {
        firebaseModel.updateRecipe(recipe) {
            executor.execute {
                database.recipeDao().update(recipe.copy(lastUpdated = System.currentTimeMillis()))
                mainHandler.post {
                    completion(true)
                }
            }
        }
    }

    fun deleteRecipe(recipe: Recipe, completion: (Boolean) -> Unit) {
        firebaseModel.deleteRecipe(recipe) {
            executor.execute {
                database.recipeDao().delete(recipe)
                mainHandler.post {
                    completion(true)
                }
            }
        }
    }
}