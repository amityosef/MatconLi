package com.colman.matconli.features.feed

import androidx.recyclerview.widget.RecyclerView
import com.colman.matconli.R
import com.colman.matconli.databinding.ItemRecipeBinding
import com.colman.matconli.model.Recipe
import com.colman.matconli.utilis.ImageUtils

class RecipeViewHolder(
    private val binding: ItemRecipeBinding
) : RecyclerView.ViewHolder(binding.root) {

    var listener: RecipeAdapter.OnItemClickListener? = null

    fun bind(recipe: Recipe?) {
        recipe?.let {
            binding.itemRecipeTextViewTitle.text = it.title
            ImageUtils.loadImage(binding.itemRecipeImageView, it.imageUrl, R.drawable.ic_recipe_placeholder)

            binding.root.setOnClickListener { _ ->
                listener?.onItemClick(it)
            }
        }
    }
}
