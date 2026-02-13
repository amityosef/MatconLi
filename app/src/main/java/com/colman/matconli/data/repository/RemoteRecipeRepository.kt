package com.colman.matconli.data.repository

import android.util.Log
import com.colman.matconli.data.networking.NetworkClient
import com.colman.matconli.model.ExternalRecipeResponse

class RemoteRecipeRepository : ExternalRecipeRepository {

    companion object {
        val shared = RemoteRecipeRepository()
    }

    override fun searchRecipes(query: String): ExternalRecipeResponse {
        val request = NetworkClient.mealDbClient.searchMeals(query)
        Log.i("TAG", "searchRecipes: request: $request")

        val response = request.execute()

        val recipes = when (response.isSuccessful) {
            true -> {
                response.body() ?: run {
                    ExternalRecipeResponse(
                        meals = emptyList()
                    )
                }
            }
            false -> {
                Log.i("TAG", "searchRecipes: failed ${response.message()}, code: ${response.code()}, errorBody: ${response.errorBody()?.string()}")
                ExternalRecipeResponse(
                    meals = emptyList()
                )
            }
        }

        return recipes
    }
}
