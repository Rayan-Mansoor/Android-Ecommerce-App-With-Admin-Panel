package com.example.ecommerceapp

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.room.Room
import com.example.ecommerceapp.Activities.AdminActivity
import com.example.ecommerceapp.Activities.UserActivity
import com.example.ecommerceapp.Database.AppDB
import com.example.ecommerceapp.Models.AdminUser
import com.example.ecommerceapp.Models.Categories
import com.example.ecommerceapp.Models.GuestUser
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.Models.UserType
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.stripe.android.PaymentConfiguration


class App : Application() {
    private lateinit var sharedPreferences: SharedPreferences
    private var firebaseUser : FirebaseUser? = null
    private lateinit var db : AppDB

    override fun onCreate() {
        super.onCreate()
        Log.d("Activity Stack", "App class onCreate()")

        sharedPreferences = getSharedPreferences("App_Cache", Context.MODE_PRIVATE)

//        setUpLocalDatabase()

        firebaseUser = FirebaseAuth.getInstance().currentUser

        Log.d("AppLogLOG", firebaseUser?.uid.toString())

        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51OPInkDkA3rml0kyjnxWaTMgz0upX158ixBKbPPWaYNnNGzDSzc2uJc5AIt4TUanIeWjiMN14vLqxfQbTTv2d4jU00rL21APEf"
        )


        Globals.DEVICE_ANDROID_ID = Globals.getAndroidId(this)

        checkNetworkConnectivity()

        if (isFirstTimeLaunched()){
            firstTimeSetup()
        }

//        addCategories()

        Globals.retrieveCategories { _ -> }

    }

    private fun setUpLocalDatabase(){
        db = Room.databaseBuilder(this,AppDB::class.java,"ecommerce-database").build()
        Globals.APP_DAO = db.AppDAO()
    }

    private fun checkNetworkConnectivity(){
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            Globals.IS_ONLINE = networkCapabilities != null &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            Globals.IS_ONLINE = networkInfo != null && networkInfo.isConnected
        }
    }

    private fun firstTimeSetup() {
        Constants.DATABASE_REFERENCE_USERS.child("Guests").orderByChild("guestID").equalTo(Globals.getAndroidId(this)).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val guestExists = snapshot.exists()
                if (guestExists) {
                    Log.d("GuestCheck", "GuestID is already present")
                } else {
                    Log.d("GuestCheck", "GuestID is not present")
                    Constants.DATABASE_REFERENCE_USERS.child("Guests").push().setValue(GuestUser(Globals.getAndroidId(this@App)))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GuestCheck", "Error checking guestID: ${error.message}")
            }
        })

        sharedPreferences.edit().putBoolean("is_first_time", false).apply()
//        sharedPreferences.edit().putBoolean("is_admin", false).apply()
        sharedPreferences.edit().putString("current_user", UserType.GUEST.name).apply()
    }

    private fun isFirstTimeLaunched(): Boolean {
        return sharedPreferences.getBoolean("is_first_time", true)
    }

    private fun addCategories() {

        val categoriesList: ArrayList<String> = ArrayList(Categories.values().map { it.name })

        for (category in categoriesList) {
            Constants.DATABASE_REFERENCE_CATEGORIES.push().setValue(category)

            val resourceId = resources.getIdentifier(category.lowercase(), "drawable", packageName)

            val image = Uri.parse("android.resource://" + packageName + "/" + resourceId)


            val imagePath = "images/categories/${category.lowercase()}/${
                Globals.calculateHash(
                    Globals.getFileFromUri(
                        this,
                        image
                    )!!
                )
            }.jpg"

            Constants.CLOUD_STORAGE_REFERENCE.child(imagePath).putFile(image)

            Constants.DATABASE_REFERENCE_IMAGES.child("Categories").child(category).setValue(imagePath)

        }

    }

}