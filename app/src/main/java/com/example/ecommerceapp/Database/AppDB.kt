package com.example.ecommerceapp.Database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ecommerceapp.Helpers.DatabaseTypeConverters
import com.example.ecommerceapp.Models.AdminUser
import com.example.ecommerceapp.Models.GuestUser
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.Models.PromoCode
import com.example.ecommerceapp.Models.RegisteredUser

@Database(entities = [GuestUser::class, RegisteredUser::class, AdminUser::class, Product::class, PromoCode::class], version = 1, exportSchema = false)
@TypeConverters(DatabaseTypeConverters::class)
abstract class AppDB : RoomDatabase() {
    abstract fun AppDAO() : AppDAO
}