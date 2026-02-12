package com.colman.matconli.features.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.colman.matconli.base.MainActivity
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentEditProfileBinding
import com.colman.matconli.utilis.ImageUtils

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null

    private val viewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return _binding?.root ?: inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            setupObservers()
            setupClickListeners()
            setupImageUrlPreview()
            viewModel.loadCurrentUser()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupObservers() {
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            _binding?.let { binding ->
                user?.let {
                    binding.fragmentEditProfileEditTextName.setText(it.name)
                    binding.fragmentEditProfileEditTextAvatarUrl.setText(it.avatarUrl ?: "")
                    binding.fragmentEditProfileTextViewEmail.text = "Email: ${it.email}"
                    ImageUtils.loadImage(binding.fragmentEditProfileImageView, it.avatarUrl, R.drawable.ic_profile_placeholder)
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            _binding?.let { binding ->
                binding.fragmentEditProfileProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                binding.fragmentEditProfileButtonSave.isEnabled = !isLoading
                binding.fragmentEditProfileButtonCancel.isEnabled = !isLoading
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it) {
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.loadUserProfile()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
                viewModel.clearUpdateResult()
            }
        }
    }

    private fun setupClickListeners() {
        _binding?.let { binding ->
            binding.fragmentEditProfileButtonSave.setOnClickListener {
                val name = binding.fragmentEditProfileEditTextName.text.toString().trim()
                val avatarUrl = binding.fragmentEditProfileEditTextAvatarUrl.text.toString().trim().ifEmpty { null }

                if (name.isEmpty()) {
                    binding.fragmentEditProfileTextInputLayoutName.error = "Name is required"
                    return@setOnClickListener
                }

                binding.fragmentEditProfileTextInputLayoutName.error = null
                viewModel.updateProfile(name, avatarUrl)
            }

            binding.fragmentEditProfileButtonCancel.setOnClickListener {
                findNavController().popBackStack()
            }

            binding.fragmentEditProfileButtonChangeImage.setOnClickListener {
                binding.fragmentEditProfileEditTextAvatarUrl.requestFocus()
            }
        }
    }

    private fun setupImageUrlPreview() {
        _binding?.let { binding ->
            binding.fragmentEditProfileEditTextAvatarUrl.doAfterTextChanged { text ->
                val url = text.toString().trim()
                if (url.isNotEmpty()) {
                    ImageUtils.loadImage(binding.fragmentEditProfileImageView, url, R.drawable.ic_profile_placeholder)
                } else {
                    binding.fragmentEditProfileImageView.setImageResource(R.drawable.ic_profile_placeholder)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

