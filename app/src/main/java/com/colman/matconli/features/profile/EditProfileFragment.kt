package com.colman.matconli.features.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.colman.matconli.base.MainActivity
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentEditProfileBinding
import com.colman.matconli.data.repository.UserRepository
import com.colman.matconli.model.User
import com.colman.matconli.utilis.ImageUtils
import com.google.firebase.auth.FirebaseAuth

class EditProfileFragment : Fragment() {

    private var binding: FragmentEditProfileBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding?.root ?: inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            setupClickListeners()
            loadCurrentUser()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadCurrentUser() {
        val userId = auth.currentUser?.uid ?: return

        binding?.fragmentEditProfileProgressBar?.visibility = View.VISIBLE

        UserRepository.shared.getUserById(userId) { user ->
            activity?.runOnUiThread {
                binding?.fragmentEditProfileProgressBar?.visibility = View.GONE
                currentUser = user
                user?.let {
                    binding?.fragmentEditProfileEditTextName?.setText(it.name)
                    binding?.fragmentEditProfileTextViewEmail?.text = "Email: ${it.email}"
                    ImageUtils.loadImage(binding?.fragmentEditProfileImageView!!, it.avatarUrl, R.drawable.ic_profile_placeholder)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding?.let { binding ->
            binding.fragmentEditProfileButtonSave.setOnClickListener {
                saveProfile()
            }

            binding.fragmentEditProfileButtonCancel.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }

    private fun saveProfile() {
        val name = binding?.fragmentEditProfileEditTextName?.text.toString().trim()

        if (name.isEmpty()) {
            binding?.fragmentEditProfileTextInputLayoutName?.error = "Name is required"
            return
        }

        val user = currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        binding?.fragmentEditProfileTextInputLayoutName?.error = null
        binding?.fragmentEditProfileProgressBar?.visibility = View.VISIBLE
        binding?.fragmentEditProfileButtonSave?.isEnabled = false
        binding?.fragmentEditProfileButtonCancel?.isEnabled = false

        val updatedUser = user.copy(name = name)

        UserRepository.shared.updateUser(
            storageAPI = com.colman.matconli.data.models.StorageModel.StorageAPI.CLOUDINARY,
            image = null,
            user = updatedUser
        ) { success ->
            activity?.runOnUiThread {
                binding?.fragmentEditProfileProgressBar?.visibility = View.GONE
                binding?.fragmentEditProfileButtonSave?.isEnabled = true
                binding?.fragmentEditProfileButtonCancel?.isEnabled = true

                if (success) {
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                    (activity as? MainActivity)?.loadUserProfile()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

