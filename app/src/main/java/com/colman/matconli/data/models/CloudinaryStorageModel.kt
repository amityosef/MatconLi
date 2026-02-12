package com.colman.matconli.data.models

import android.graphics.Bitmap
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.colman.matconli.base.MyApplication
import com.colman.matconli.model.Recipe
import java.io.File
import java.io.FileOutputStream

class CloudinaryStorageModel {

    fun uploadRecipeImage(image: Bitmap, recipe: Recipe, completion: (String?) -> Unit) {
        val fileName = "recipe_${recipe.id}_${System.currentTimeMillis()}"
        uploadImage(image, fileName, "recipes", completion)
    }

    fun uploadUserImage(image: Bitmap, userId: String, completion: (String?) -> Unit) {
        val fileName = "user_${userId}_${System.currentTimeMillis()}"
        uploadImage(image, fileName, "users", completion)
    }

    private fun uploadImage(image: Bitmap, fileName: String, folder: String, completion: (String?) -> Unit) {
        val context = MyApplication.Globals.appContext ?: return completion(null)
        val file = bitmapToFile(image, context.cacheDir, fileName)

        MediaManager.get().upload(file.path)
            .option("folder", folder)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("CloudinaryStorageModel", "Upload started: $requestId")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    val progress = bytes.toDouble() / totalBytes * 100
                    Log.d("CloudinaryStorageModel", "Upload progress: $progress%")
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url") as? String
                    Log.d("CloudinaryStorageModel", "Upload success: $url")
                    completion(url)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("CloudinaryStorageModel", "Upload error: ${error?.description}")
                    completion(null)
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.d("CloudinaryStorageModel", "Upload rescheduled: ${error?.description}")
                }
            })
            .dispatch()
    }

    private fun bitmapToFile(bitmap: Bitmap, cacheDir: File, fileName: String): File {
        val file = File(cacheDir, "$fileName.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        }
        return file
    }
}