package com.example.ecommerceapp.Database

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ecommerceapp.Models.AdminUser
import com.example.ecommerceapp.Models.GuestUser
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.Models.RegisteredUser


@Dao
interface AppDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuestUser(guest: GuestUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegisteredUser(registered: RegisteredUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminUser(admin: AdminUser)

    @Query("select * from guestuser")
    suspend fun getAllGuests() : List<GuestUser>

    @Query("select * from registereduser")
    suspend fun getAllRegistered() : List<RegisteredUser>

    @Query("select * from adminuser where adminEmail = :email and adminPassword = :pass")
    suspend fun getAdmin(email : String, pass : String) : List<AdminUser>

}