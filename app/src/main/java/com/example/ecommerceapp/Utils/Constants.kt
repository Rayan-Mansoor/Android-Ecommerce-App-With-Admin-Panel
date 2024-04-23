package com.example.ecommerceapp.Utils

import androidx.room.Room
import com.example.ecommerceapp.Database.AppDB
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

object Constants {
    private val DATABASE_REFERENCE_ROOT = FirebaseDatabase.getInstance("https://ecommercestore-7733c-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val DATABASE_REFERENCE_USERS = DATABASE_REFERENCE_ROOT.getReference("Users")
    val DATABASE_REFERENCE_PRODUCTS = DATABASE_REFERENCE_ROOT.getReference("Products")
    val DATABASE_REFERENCE_CARTS = DATABASE_REFERENCE_ROOT.getReference("Carts")
    val DATABASE_REFERENCE_IMAGES = DATABASE_REFERENCE_ROOT.getReference("Images")
    val DATABASE_REFERENCE_PROMO_CODE = DATABASE_REFERENCE_ROOT.getReference("Promo Codes")
    val DATABASE_REFERENCE_Reviews = DATABASE_REFERENCE_ROOT.getReference("Reviews")
    val DATABASE_REFERENCE_Orders = DATABASE_REFERENCE_ROOT.getReference("Orders")
    val DATABASE_REFERENCE_CATEGORIES = DATABASE_REFERENCE_ROOT.getReference("Categories")
    val DATABASE_REFERENCE_NOTIFICATIONS = DATABASE_REFERENCE_ROOT.getReference("Notifications")
    val DATABASE_REFERENCE_WISHLIST = DATABASE_REFERENCE_ROOT.getReference("Wishlist")

    val CLOUD_STORAGE_REFERENCE = FirebaseStorage.getInstance("gs://ecommercestore-7733c.appspot.com").reference

}
