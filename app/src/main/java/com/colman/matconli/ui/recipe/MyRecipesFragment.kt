package com.colman.matconli.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentMyRecipesBinding
import com.colman.matconli.model.Recipe
import com.colman.matconli.model.RecipeRepository
import com.google.firebase.auth.FirebaseAuth

class MyRecipesFragment : Fragment(), RecipeAdapter.OnItemClickListener {

    private var _binding: FragmentMyRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecipeAdapter
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupRecyclerView()
        setupClickListeners()
        observeRecipes()
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_my_recipes, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_main_feed -> {
                        findNavController().popBackStack()
                        true
                    }
                    R.id.action_logout -> {
                        FirebaseAuth.getInstance().signOut()
                        findNavController().navigate(
                            MyRecipesFragmentDirections.actionMyRecipesFragmentToLoginFragment()
                        )
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        adapter = RecipeAdapter(listener = this)
        binding.rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecipes.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.fabAddRecipe.setOnClickListener {
            findNavController().navigate(
                MyRecipesFragmentDirections.actionMyRecipesFragmentToAddRecipeFragment(null)
            )
        }

        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }

    private fun observeRecipes() {
        RecipeRepository.recipes.observe(viewLifecycleOwner) { recipes ->
            val myRecipes = recipes.filter { it.ownerId == currentUserId }
            adapter.updateRecipes(myRecipes)
            binding.tvEmpty.visibility = if (myRecipes.isEmpty()) View.VISIBLE else View.GONE
            binding.rvRecipes.visibility = if (myRecipes.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun refreshData() {
        binding.progressBar.visibility = View.VISIBLE
        RecipeRepository.refreshAllRecipes().observe(viewLifecycleOwner) {
            binding.progressBar.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onItemClick(recipe: Recipe) {
        findNavController().navigate(
            MyRecipesFragmentDirections.actionMyRecipesFragmentToRecipeDetailFragment(recipe.id)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

