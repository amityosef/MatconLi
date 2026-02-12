package com.colman.matconli.data.models

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.colman.matconli.model.Recipe

class FirebaseModel {

    private val db = Firebase.firestore

    private companion object COLLECTIONS {
        const val RECIPES = "recipes"
    }

    fun getAllRecipes(since: Long, completion: (List<Recipe>) -> Unit) {
        db.collection(RECIPES)
            .whereGreaterThanOrEqualTo(Recipe.LAST_UPDATED_KEY, Timestamp(since / 1000, 0))
            .get()
            .addOnCompleteListener { result ->
                when (result.isSuccessful) {
                    true -> completion(result.result.map { Recipe.fromJson(it.data) })
                    false -> completion(emptyList())
                }
            }
    }

    fun addRecipe(recipe: Recipe, completion: () -> Unit) {
        db.collection(RECIPES)
            .document(recipe.id)
            .set(recipe.toJson)
            .addOnSuccessListener { documentReference ->
                completion()
            }
            .addOnFailureListener { e ->
                completion()
            }
    }

    fun deleteRecipe(recipe: Recipe, completion: () -> Unit) {
        db.collection(RECIPES)
            .document(recipe.id)
            .delete()
            .addOnSuccessListener {
                completion()
            }
            .addOnFailureListener {
                completion()
            }
    }

    fun getRecipeById(id: String, completion: (Recipe?) -> Unit) {
        db.collection(RECIPES)
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    completion(Recipe.fromJson(document.data ?: return@addOnSuccessListener))
                } else {
                    completion(null)
                }
            }
            .addOnFailureListener {
                completion(null)
            }
    }
}
