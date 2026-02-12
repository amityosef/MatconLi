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
import com.colman.matconli.databinding.FragmentFeedBinding
import com.colman.matconli.model.Recipe
import com.colman.matconli.model.RecipeRepository
import com.google.firebase.auth.FirebaseAuth

class FeedFragment : Fragment(), RecipeAdapter.OnItemClickListener {

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: RecipeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
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
                menuInflater.inflate(R.menu.menu_feed, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_my_recipes -> {
                        findNavController().navigate(
                            FeedFragmentDirections.actionFeedFragmentToMyRecipesFragment()
                        )
                        true
                    }
                    R.id.action_profile -> {
                        findNavController().navigate(
                            FeedFragmentDirections.actionFeedFragmentToProfileFragment()
                        )
                        true
                    }
                    R.id.action_logout -> {
                        FirebaseAuth.getInstance().signOut()
                        findNavController().navigate(
                            FeedFragmentDirections.actionFeedFragmentToLoginFragment()
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
                FeedFragmentDirections.actionFeedFragmentToAddRecipeFragment(null)
            )
        }

        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }

    private var hasLoadedInitial = false

    private fun observeRecipes() {
        RecipeRepository.recipes.observe(viewLifecycleOwner) { recipes ->
            adapter.updateRecipes(recipes)
            binding.tvEmpty.visibility = if (recipes.isEmpty()) View.VISIBLE else View.GONE
            binding.rvRecipes.visibility = if (recipes.isEmpty()) View.GONE else View.VISIBLE

            if (!hasLoadedInitial && recipes.isEmpty()) {
                hasLoadedInitial = true
                refreshData()
            }
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
            FeedFragmentDirections.actionFeedFragmentToRecipeDetailFragment(recipe.id)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


