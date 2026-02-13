package com.colman.matconli.features.external_search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colman.matconli.data.repository.RemoteRecipeRepository
import com.colman.matconli.model.ExternalRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExternalSearchViewModel : ViewModel() {

    private val _recipes = MutableLiveData<List<ExternalRecipe>>()
    val recipes: LiveData<List<ExternalRecipe>> = _recipes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun searchRecipes(query: String) {
        if (query.isBlank()) {
            _recipes.value = emptyList()
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RemoteRecipeRepository.shared.searchRecipes(query)
                }

                _recipes.value = result.meals ?: emptyList()
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to search recipes: ${e.message}"
                _recipes.value = emptyList()
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
