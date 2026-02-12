package com.colman.matconli.util

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.squareup.picasso.Picasso

object ImageUtils {

    fun loadImage(
        imageView: ImageView,
        imageUrl: String?,
        @DrawableRes placeholderRes: Int
    ) {
        if (imageUrl.isNullOrBlank()) {
            imageView.setImageResource(placeholderRes)
            return
        }

        if (imageUrl.startsWith("data:image")) {
            try {
                val base64String = imageUrl.substring(imageUrl.indexOf(",") + 1)
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                imageView.setImageBitmap(bitmap)
            } catch (_: Exception) {
                imageView.setImageResource(placeholderRes)
            }
        } else {
            Picasso.get()
                .load(imageUrl)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .into(imageView)
        }
    }
}

