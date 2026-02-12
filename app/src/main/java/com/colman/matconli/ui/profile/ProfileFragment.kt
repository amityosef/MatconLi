package com.colman.matconli.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentProfileBinding
import com.colman.matconli.util.ImageUtils
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
        viewModel.loadCurrentUser()
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvName.text = it.name
                binding.tvEmail.text = it.email
                ImageUtils.loadImage(binding.ivProfileImage, it.avatarUrl, R.drawable.ic_profile_placeholder)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(
                ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
            )
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(
                ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

