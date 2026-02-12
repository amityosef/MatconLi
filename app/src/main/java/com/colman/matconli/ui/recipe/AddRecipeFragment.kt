package com.colman.matconli.ui.recipe

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.colman.matconli.R
import com.colman.matconli.databinding.FragmentAddRecipeBinding
import com.colman.matconli.model.Recipe
import com.colman.matconli.model.RecipeRepository
import com.colman.matconli.util.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.UUID

class AddRecipeFragment : Fragment() {

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!

    private val args: AddRecipeFragmentArgs by navArgs()
    private var existingRecipe: Recipe? = null
    private var selectedImageUri: Uri? = null
    private var photoUri: Uri? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoUri?.let { uri ->
                selectedImageUri = uri
                loadImagePreview(uri.toString())
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                loadImagePreview(uri.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddRecipeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()

        args.recipeId?.let { id ->
            loadExistingRecipe(id)
        }

        setupClickListeners()
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_add_recipe, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_main_feed -> {
                        findNavController().popBackStack()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun loadExistingRecipe(id: String) {
        binding.progressBar.visibility = View.VISIBLE
        RecipeRepository.getRecipeById(id) { recipe ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                binding.progressBar.visibility = View.GONE
                recipe?.let {
                    existingRecipe = it
                    binding.etTitle.setText(it.title)
                    binding.etDescription.setText(it.description)
                    loadImagePreview(it.imageUrl)
                }
            }
        }
    }

    private fun loadImagePreview(url: String?) {
        ImageUtils.loadImage(binding.ivRecipeImage, url, R.drawable.ic_recipe_placeholder)
    }

    private fun openCamera() {
        val photoFile = File.createTempFile(
            "recipe_${System.currentTimeMillis()}",
            ".jpg",
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun setupClickListeners() {
        binding.btnSave.setOnClickListener {
            saveRecipe()
        }

        binding.btnSelectImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnGallery.setOnClickListener {
            openGallery()
        }
    }

    private fun saveRecipe() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isBlank() || description.isBlank()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSave.isEnabled = false

        if (selectedImageUri != null) {
            try {
                val imageDataUri = convertImageToDataUri(selectedImageUri!!)
                createAndSaveRecipe(title, description, imageDataUri, currentUser.uid)
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show()
            }
        } else {
            createAndSaveRecipe(title, description, existingRecipe?.imageUrl, currentUser.uid)
        }
    }

    private fun convertImageToDataUri(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val maxSize = 800
        val scaledBitmap = if (bitmap.width > maxSize || bitmap.height > maxSize) {
            val scale = maxSize.toFloat() / Math.max(bitmap.width, bitmap.height)
            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        bitmap.recycle()

        return "data:image/jpeg;base64,$base64String"
    }

    private fun createAndSaveRecipe(title: String, description: String, imageUrl: String?, ownerId: String) {
        val recipe = Recipe(
            id = existingRecipe?.id ?: UUID.randomUUID().toString(),
            title = title,
            description = description,
            imageUrl = imageUrl,
            ownerId = ownerId,
            lastUpdated = System.currentTimeMillis()
        )

        val callback: (Boolean) -> Unit = { success ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                binding.progressBar.visibility = View.GONE
                binding.btnSave.isEnabled = true
                if (success) {
                    Toast.makeText(requireContext(), "Recipe saved!", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Failed to save recipe", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (existingRecipe != null) {
            RecipeRepository.updateRecipe(recipe, callback)
        } else {
            RecipeRepository.addRecipe(recipe, callback)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

