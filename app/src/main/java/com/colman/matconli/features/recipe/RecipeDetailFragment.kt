package com.colman.matconli.features.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentRecipeDetailBinding
import com.colman.matconli.model.Recipe
import com.colman.matconli.data.repository.RecipeRepository
import com.colman.matconli.utilis.hide
import com.colman.matconli.utilis.show
import com.colman.matconli.base.BaseFragment
import com.squareup.picasso.Picasso

class RecipeDetailFragment : BaseFragment() {

    var binding: FragmentRecipeDetailBinding? = null

    private val args: RecipeDetailFragmentArgs by navArgs()
    private var recipe: Recipe? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadRecipe()
        setupClickListeners()
    }

    private fun loadRecipe() {
        binding?.fragmentRecipeDetailProgressBar?.show()
        RecipeRepository.shared.getRecipeById(args.recipeId) { recipe ->
            activity?.runOnUiThread {
                if (binding == null) return@runOnUiThread
                binding?.fragmentRecipeDetailProgressBar?.hide()
                recipe?.let {
                    this.recipe = it
                    displayRecipe(it)
                }
            }
        }
    }

    private fun displayRecipe(recipe: Recipe) {
        binding?.fragmentRecipeDetailTextViewTitle?.text = recipe.title
        binding?.fragmentRecipeDetailTextViewDescription?.text = recipe.description
        binding?.fragmentRecipeDetailImageView?.let { imageView ->
            if (!recipe.imageUrl.isNullOrBlank()) {
                Picasso.get()
                    .load(recipe.imageUrl)
                    .placeholder(R.drawable.ic_recipe_placeholder)
                    .error(R.drawable.ic_recipe_placeholder)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_recipe_placeholder)
            }
        }

        val currentUserId = getCurrentUserId()
        if (currentUserId == recipe.ownerId) {
            binding?.fragmentRecipeDetailButtonEdit?.visibility = View.VISIBLE
            binding?.fragmentRecipeDetailButtonDelete?.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        binding?.fragmentRecipeDetailButtonEdit?.setOnClickListener {
            recipe?.let {
                findNavController().navigate(
                    RecipeDetailFragmentDirections.actionRecipeDetailFragmentToAddRecipeFragment(it.id)
                )
            }
        }

        binding?.fragmentRecipeDetailButtonDelete?.setOnClickListener {
            recipe?.let { r ->
                binding?.fragmentRecipeDetailProgressBar?.show()
                RecipeRepository.shared.deleteRecipe(r) { success ->
                    activity?.runOnUiThread {
                        if (binding == null) return@runOnUiThread
                        binding?.fragmentRecipeDetailProgressBar?.hide()
                        if (success) {
                            Toast.makeText(requireContext(), "Recipe deleted", Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

