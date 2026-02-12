package com.colman.matconli.ui.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.colman.matconli.R
import com.colman.matconli.databinding.ItemRecipeBinding
import com.colman.matconli.model.Recipe
import com.colman.matconli.util.ImageUtils

class RecipeAdapter(
    private var recipes: MutableList<Recipe> = mutableListOf(),
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(recipe: Recipe)
    }

    inner class RecipeViewHolder(private val binding: ItemRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.tvTitle.text = recipe.title
            ImageUtils.loadImage(binding.ivRecipe, recipe.imageUrl, R.drawable.ic_recipe_placeholder)

            binding.root.setOnClickListener {
                listener.onItemClick(recipe)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipes[position])
    }

    override fun getItemCount(): Int = recipes.size

    fun updateRecipes(newRecipes: List<Recipe>) {
        val diffCallback = RecipeDiffCallback(recipes, newRecipes)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        recipes.clear()
        recipes.addAll(newRecipes)
        diffResult.dispatchUpdatesTo(this)
    }

    private class RecipeDiffCallback(
        private val oldList: List<Recipe>,
        private val newList: List<Recipe>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].id == newList[newPos].id
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}

