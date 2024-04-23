package com.example.ecommerceapp.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.ActivityCustomerLoginBinding
import com.google.firebase.auth.FirebaseAuth

class CustomerLoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomerLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cusLoginBtn.setOnClickListener {
            Constants.DATABASE_REFERENCE_USERS.child("Users").get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dataSnapshot = task.result
                    if (dataSnapshot.exists()) {
                        for (adminSnapshot in dataSnapshot.children) {

                            if (adminSnapshot != null){
                                val customer = adminSnapshot.getValue(RegisteredUser::class.java)!!

                                if (binding.cusEmailET.text.toString() == customer.userEmail && binding.cusPassET.text.toString() == customer.userPassword){
                                    FirebaseAuth.getInstance().signInWithEmailAndPassword(binding.cusEmailET.text.toString(),binding.cusPassET.text.toString()).addOnCompleteListener(this) { task ->
                                        if (task.isSuccessful){
                                            Toast.makeText(this,"Login Successful", Toast.LENGTH_SHORT).show()
                                            Globals.CURRENT_USER_ID = FirebaseAuth.getInstance().uid
                                        }
                                        else{
                                            Toast.makeText(this,"Incorrect Credentials ", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }

                        }
                    } else {
                        Log.d("AdminLoginLog", "No admin found in the 'Admins' child.")
                    }
                } else {
                    Log.e("AdminLoginLog", "Error getting admin data: ${task.exception}")
                }
            }
        }


    }

}