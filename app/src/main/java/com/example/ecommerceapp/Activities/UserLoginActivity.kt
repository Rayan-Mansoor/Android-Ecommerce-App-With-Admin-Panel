package com.example.ecommerceapp.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.ecommerceapp.Models.Gender
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.Models.UserType
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.ActivityUserLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class UserLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences : SharedPreferences
    private var isInfoValid : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("App_Cache", Context.MODE_PRIVATE)

        binding = ActivityUserLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        binding.userEmailET.hint = "Email Address"
//        binding.userPassET.hint = "Password"

        binding.LoginPageTitle.setText("Customer Login Page")


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

//        binding.userEmailET.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                binding.userEmailET.hint = null
//                if (binding.userEmailET.text.isNullOrEmpty()){
//                    binding.userEmailET.hint = "Email Address"
//                }
//            }
//
//            override fun afterTextChanged(p0: Editable?) {}
//        })
//
//        binding.userPassET.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                binding.userPassET.hint = null
//                if (binding.userPassET.text.isNullOrEmpty()){
//                    binding.userPassET.hint = "Password"
//                }
//            }
//
//            override fun afterTextChanged(p0: Editable?) {}
//
//        })

        binding.gotoSignUpBtn.setOnClickListener {
            startActivity(Intent(this, UserSignUpActivity::class.java))
            finish()
        }

        binding.userLoginBtn.setOnClickListener {
            loginAcc()
            binding.userEmailET.clearFocus()

        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.firebase_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.googleSignInButton.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, 20)
        }

    }

    private fun loginAcc() {
        if (isInfoValid){
            setProgressBar(true)

            val userEmail = binding.userEmailET.text.toString()
            val userPassword = binding.userPassET.text.toString()

            Constants.DATABASE_REFERENCE_USERS.child("Registered")
                .orderByChild("userEmail").equalTo(userEmail.lowercase())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (child in dataSnapshot.children){
                                Globals.CURRENT_USER = child.getValue(RegisteredUser::class.java)
                            }
                            FirebaseAuth.getInstance().signInWithEmailAndPassword(userEmail, userPassword)
                                .addOnCompleteListener(this@UserLoginActivity) { authTask ->
                                    if (authTask.isSuccessful) {
                                        setProgressBar(false)
                                        Toast.makeText(this@UserLoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                                        Globals.CURRENT_USER_ID = FirebaseAuth.getInstance().uid
//                                        sharedPreferences.edit().putBoolean("is_admin", false).apply()
                                        sharedPreferences.edit().putString("current_user", UserType.REGISTERED.name).apply()

                                        startActivity(Intent(this@UserLoginActivity, UserActivity::class.java))
                                        finishAffinity()
                                    } else {
                                        setProgressBar(false)
                                        binding.userPassET.text?.clear()
                                        Toast.makeText(this@UserLoginActivity, "Incorrect Credentials", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this@UserLoginActivity, "No User By This Email Exists", Toast.LENGTH_SHORT).show()
                            setProgressBar(false)

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                        Toast.makeText(this@UserLoginActivity, "Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                        setProgressBar(false)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 20){
            setProgressBar(true)

            Log.d("MainActivityLog", "Request code matched")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                googleSignInClient.revokeAccess()

                val account = task.result
                Log.d("MainActivityLog", account.toString())

                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                Log.d("MainActivityLog", credential.toString())


                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        setProgressBar(false)
                        Toast.makeText(this@UserLoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()

                        Globals.CURRENT_USER_ID = FirebaseAuth.getInstance().uid
                        sharedPreferences.edit().putString("current_user", UserType.REGISTERED.name).apply()

                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            user.displayName?.let { Log.d("MainActivityLogh", it) }
                            user.email?.let { Log.d("MainActivityLogh", it) }
                            user.phoneNumber?.let { Log.d("MainActivityLogh", it) }
                        }
                        else{
                            Log.d("MainActivityLogh", "its null")
                        }

                        val createdUser = RegisteredUser(FirebaseAuth.getInstance().uid!!, FirebaseAuth.getInstance().currentUser!!.displayName!!, FirebaseAuth.getInstance().currentUser!!.email!!, "encrypted by google", null, null, null)
                        Globals.CURRENT_USER = createdUser

                        Constants.DATABASE_REFERENCE_USERS.child("Registered").orderByChild("userID").equalTo(Globals.CURRENT_USER_ID).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (!snapshot.exists()){
                                    Constants.DATABASE_REFERENCE_USERS.child("Registered").push().setValue(createdUser)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}

                        })

                        startActivity(Intent(this@UserLoginActivity, UserActivity::class.java))
                        finishAffinity()
                    } else {
                        setProgressBar(false)
                        binding.userPassET.text?.clear()
                        Toast.makeText(this@UserLoginActivity, "Incorrect Credentials", Toast.LENGTH_SHORT).show()
                    }

                }
            }catch (e : Exception){
                setProgressBar(false)
                Log.d("MainActivityLog", e.message.toString())
            }
        }
    }
}