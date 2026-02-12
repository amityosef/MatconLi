package com.colman.matconli.utilis

import android.view.View
import androidx.fragment.app.Fragment

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.toggleVisibility(show: Boolean) {
    visibility = if (show) View.VISIBLE else View.GONE
}

/**
 * Safely run UI operations on the main thread, checking if the fragment is still attached
 * Useful for callbacks that may execute after fragment is destroyed
 */
inline fun Fragment.runOnUiThreadSafe(crossinline block: () -> Unit) {
    activity?.runOnUiThread {
        if (isAdded && view != null) {
            block()
        }
    }
}
