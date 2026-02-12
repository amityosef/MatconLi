package com.colman.matconli.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.colman.matconli.dao.AppLocalDB
import com.colman.matconli.data.models.FirebaseModel
import com.colman.matconli.model.Recipe
import java.util.concurrent.Executors

object RecipeRepository {

    private val firebaseModel = FirebaseModel()
    private val recipeDao by lazy { AppLocalDB.db.recipeDao() }
    private val executor = Executors.newSingleThreadExecutor()

    val recipes: LiveData<MutableList<Recipe>> by lazy { recipeDao.getAll() }

    fun refreshAllRecipes(): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        firebaseModel.getAllRecipes(Recipe.Companion.lastUpdated) { recipesList ->
            executor.execute {
                var latestUpdate = 0L

                for (recipe in recipesList) {
                    recipe.lastUpdated?.let {
                        if (it > latestUpdate) latestUpdate = it
                    }
                }

                if (recipesList.isNotEmpty()) {
                    recipeDao.insertAll(recipesList)
                    if (latestUpdate > Recipe.Companion.lastUpdated) {
                        Recipe.Companion.lastUpdated = latestUpdate
                    }
                }
                result.postValue(true)
            }
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
        firebaseModel.saveRecipe(recipe) {
            executor.execute {
                recipeDao.insert(recipe.copy(lastUpdated = System.currentTimeMillis()))
                callback(true)
            }
        }
    }

    fun updateRecipe(recipe: Recipe, callback: (Boolean) -> Unit) {
        firebaseModel.updateRecipe(recipe) {
            executor.execute {
                recipeDao.update(recipe.copy(lastUpdated = System.currentTimeMillis()))
                callback(true)
            }
        }
    }

    fun deleteRecipe(recipe: Recipe, callback: (Boolean) -> Unit) {
        firebaseModel.deleteRecipe(recipe) {
            executor.execute {
                recipeDao.delete(recipe)
                callback(true)
            }
        }
    }
}