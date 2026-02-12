package com.colman.matconli.base

import android.app.Application
import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.cloudinary.android.policy.UploadPolicy

class MyApplication : Application() {

    object Globals {
        var appContext: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        Globals.appContext = applicationContext
        initCloudinary()
    }

    private fun initCloudinary() {
        try {
            val config = mapOf(
                "cloud_name" to "duutna6lt",
                "api_key" to "646421153716863",
                "api_secret" to "zbfAoiCiwRSW0_Hj39GzHerfnbU"
            )
            MediaManager.init(this, config)
            MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.Builder()
                .maxConcurrentRequests(3)
                .networkPolicy(UploadPolicy.NetworkType.UNMETERED)
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

