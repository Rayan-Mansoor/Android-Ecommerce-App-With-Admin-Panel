package com.example.ecommerceapp.Helpers

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

class ImageStorageManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("ImageStorage", Context.MODE_PRIVATE)

    fun storeImageUri(path: String, uri: Uri) {
        val editor = sharedPreferences.edit()
        editor.putString(path, uri.toString())
        editor.apply()
    }

    fun getStoredImageUri(path: String): Uri? {
        val uriString = sharedPreferences.getString(path, null)
        return uriString?.let { Uri.parse(it) }
    }
}