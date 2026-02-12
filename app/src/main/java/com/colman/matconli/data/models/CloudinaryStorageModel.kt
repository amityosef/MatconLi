package com.colman.matconli.data.models

import android.graphics.Bitmap
import com.colman.matconli.model.Recipe

/**
 * CloudinaryStorageModel - Stub implementation
 * To enable Cloudinary upload, add the Cloudinary Android SDK dependency:
 * implementation("com.cloudinary:cloudinary-android:3.0.2")
 * and implement the actual upload logic.
 */
class CloudinaryStorageModel {
    
    fun uploadRecipeImage(image: Bitmap, recipe: Recipe, completion: (String?) -> Unit) {
        // Cloudinary SDK is not available in this project.
        // To implement, add cloudinary-android dependency and uncomment the implementation.
        completion(null)
    }
}