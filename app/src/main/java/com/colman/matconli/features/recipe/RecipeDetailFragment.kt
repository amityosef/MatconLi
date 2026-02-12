package com.colman.matconli.features.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentRecipeDetailBinding
import com.colman.matconli.model.Recipe
import com.colman.matconli.data.repository.RecipeRepository
import com.colman.matconli.utilis.ImageUtils
import com.google.firebase.auth.FirebaseAuth

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!

    private val args: RecipeDetailFragmentArgs by navArgs()
    private var recipe: Recipe? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadRecipe()
        setupClickListeners()
    }

    private fun loadRecipe() {
        binding.progressBar.visibility = View.VISIBLE
        RecipeRepository.getRecipeById(args.recipeId) { loadedRecipe ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                binding.progressBar.visibility = View.GONE
                loadedRecipe?.let {
                    recipe = it
                    displayRecipe(it)
                }
            }
        }
    }

    private fun displayRecipe(recipe: Recipe) {
        binding.tvTitle.text = recipe.title
        binding.tvDescription.text = recipe.description
        ImageUtils.loadImage(binding.ivRecipeImage, recipe.imageUrl, R.drawable.ic_recipe_placeholder)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == recipe.ownerId) {
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnDelete.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            recipe?.let {
                findNavController().navigate(
                    RecipeDetailFragmentDirections.actionRecipeDetailFragmentToAddRecipeFragment(it.id)
                )
            }
        }

        binding.btnDelete.setOnClickListener {
            recipe?.let { r ->
                binding.progressBar.visibility = View.VISIBLE
                RecipeRepository.deleteRecipe(r) { success ->
                    activity?.runOnUiThread {
                        if (_binding == null) return@runOnUiThread
                        binding.progressBar.visibility = View.GONE
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
        _binding = null
    }
}

