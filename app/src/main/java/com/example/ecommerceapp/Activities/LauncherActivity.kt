package com.example.ecommerceapp.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ecommerceapp.Models.UserType
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Globals
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LauncherActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private var firebaseUser : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        sharedPreferences = getSharedPreferences("App_Cache", Context.MODE_PRIVATE)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        Globals.retrieveCurrentUser(sharedPreferences, firebaseUser)

        checkAuthentication()
    }

    private fun checkAuthentication(){

        if(firebaseUser == null){

            Globals.CURRENT_USER_ID = Globals.DEVICE_ANDROID_ID

            val intent = Intent(this, UserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            startActivity(intent)
            finish()
        }
        else{
            Globals.CURRENT_USER_ID  = firebaseUser!!.uid

            if(sharedPreferences.getString("current_user", UserType.GUEST.name) == UserType.ADMIN.name){
                val intent = Intent(this, AdminActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }

            else{
                val intent = Intent(this, UserActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                startActivity(intent)
                finish()
            }
        }
    }
}