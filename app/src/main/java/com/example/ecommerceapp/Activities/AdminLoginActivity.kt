package com.example.ecommerceapp.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.ecommerceapp.Models.AdminUser
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.Models.UserType
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.ActivityAdminLoginBinding
import com.example.ecommerceapp.databinding.ActivityUserLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AdminLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserLoginBinding
    private lateinit var sharedPreferences : SharedPreferences
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isInfoValid : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("App_Cache", Context.MODE_PRIVATE)

//        createAdmin()

        binding.LoginPageTitle.setText("Admin Login Page")

        binding.userEmailET.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus){
                binding.textInputLayout5.error = null
            }
            else if (!hasFocus){
                if (!Patterns.EMAIL_ADDRESS.matcher(binding.userEmailET.text.toString()).matches()){
                    isInfoValid = false
                    binding.textInputLayout5.error = "Invalid Email"
                }
                else{
                    isInfoValid = true
                }
            }
        }

        binding.gotoSignUpBtn.visibility = View.INVISIBLE
        binding.askTV.visibility = View.INVISIBLE

        binding.userLoginBtn.setOnClickListener {
            loginAcc()
            binding.userEmailET.clearFocus()

        }

        binding.googleSignInButton.visibility = View.GONE

    }

    private fun loginAcc() {
        if (isInfoValid){
            setProgressBar(true)

            val userEmail = binding.userEmailET.text.toString().lowercase()
            val userPassword = binding.userPassET.text.toString()

            Constants.DATABASE_REFERENCE_USERS.child("Admins")
                .orderByChild("adminEmail").equalTo(userEmail)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (childSnap in dataSnapshot.children){
                                val adminUser = childSnap.getValue(AdminUser::class.java)!!
                                Globals.CURRENT_USER = adminUser
                            }
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(userEmail, userPassword)
                                .addOnCompleteListener(this@AdminLoginActivity) { authTask ->
                                    if (authTask.isSuccessful) {
                                        Toast.makeText(this@AdminLoginActivity,"Login Successful", Toast.LENGTH_SHORT).show()
                                        Globals.CURRENT_USER_ID = FirebaseAuth.getInstance().uid
//                                        sharedPreferences.edit().putBoolean("is_admin", true).apply()
                                        sharedPreferences.edit().putString("current_user", UserType.ADMIN.name).apply()

                                        startActivity(Intent(this@AdminLoginActivity, AdminActivity::class.java))
                                        finishAffinity()

                                    } else {
                                        Toast.makeText(this@AdminLoginActivity, "Incorrect Credentials", Toast.LENGTH_SHORT).show()
                                        setProgressBar(false)

                                    }
                                }
                        } else {
                            Toast.makeText(this@AdminLoginActivity, "No Admin By This Email Exists", Toast.LENGTH_SHORT).show()
                            setProgressBar(false)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                        Toast.makeText(this@AdminLoginActivity, "Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
        else{
            Toast.makeText(this,"Invalid Email Entered", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setProgressBar(progress : Boolean){
        if (progress){
            binding.progressBar.visibility = View.VISIBLE
        }
        else
            binding.progressBar.visibility = View.INVISIBLE
    }

}
