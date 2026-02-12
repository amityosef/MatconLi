package com.colman.matconli.features.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.colman.matconli.base.MainActivity
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentProfileBinding
import com.colman.matconli.utilis.ImageUtils
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val viewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return _binding?.root ?: inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupClickListeners()
        viewModel.loadCurrentUser()
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            _binding?.let { binding ->
                if (user != null) {
                    binding.tvName.text = user.name
                    binding.tvEmail.text = user.email
                    ImageUtils.loadImage(binding.ivProfileImage, user.avatarUrl, R.drawable.ic_profile_placeholder)
                } else {
                    binding.tvName.text = ""
                    binding.tvEmail.text = ""
                    binding.ivProfileImage.setImageResource(R.drawable.ic_profile_placeholder)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            _binding?.let { binding ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        _binding?.let { binding ->
            binding.btnEditProfile.setOnClickListener {
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
                )
            }

            binding.btnLogout.setOnClickListener {
                (activity as? MainActivity)?.clearUserProfile()
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(
                    ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

