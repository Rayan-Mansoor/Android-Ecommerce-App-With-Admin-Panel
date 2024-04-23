package com.example.ecommerceapp.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.ecommerceapp.Models.Gender
import com.example.ecommerceapp.Models.GuestUser
import com.example.ecommerceapp.Models.PaymentMethod
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.Models.User
import com.example.ecommerceapp.Models.UserType
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.ActivitySignUpBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class UserSignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    private lateinit var guestID: String
    private var gender : Gender = Gender.MALE
    private var isInfoValid : Boolean = false
    private var userToUpdate : User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userToUpdate = intent.getSerializableExtra("current_user_to_edit") as User?

        val currentUser = userToUpdate
        if (currentUser != null && currentUser is RegisteredUser && currentUser.userAge != null){
            binding.uemailET.setText(currentUser.userEmail)
            binding.unameET.setText(currentUser.userName)
            binding.upassET.setText(currentUser.userPassword)
            binding.uphoneET.setText(currentUser.userPhoneNo)
            binding.uageET.setText(currentUser.userAge.toString())

            when (currentUser.userGender!!) {
                Gender.MALE -> {
                    binding.maleRadioBtn.isChecked = true
                    gender = Gender.MALE
                }
                Gender.FEMALE -> {
                    binding.femaleRadioBtn.isChecked = true
                    gender = Gender.FEMALE
                }
            }

            binding.uemailET.isEnabled = false
            binding.uemailET.alpha = 0.5f

            binding.signupBtn.text = "Update User"

        }

        guestID = Globals.getAndroidId(this)

        binding.genderRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.male_radioBtn -> {
                    gender = Gender.MALE
                }
                R.id.female_radioBtn -> {
                    gender = Gender.FEMALE
                }
            }
        }

//        binding.uemailET.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                binding.uemailET.hint = null
//                if (binding.uemailET.text.isNullOrEmpty()){
//                    binding.uemailET.hint = "Email Address"
//                }
//            }
//
//            override fun afterTextChanged(p0: Editable?) {}
//        })
//
//        binding.unameET.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                binding.unameET.hint = null
//                if (binding.unameET.text.isNullOrEmpty()){
//                    binding.unameET.hint = "User Name"
//                }
//            }
//
//            override fun afterTextChanged(p0: Editable?) {}
//
//        })
//
//        binding.upassET.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                binding.upassET.hint = null
//                if (binding.upassET.text.isNullOrEmpty()){
//                    binding.upassET.hint = "Password"
//                }
//            }
//
//            override fun afterTextChanged(p0: Editable?) {}
//
//        })
//
//        binding.uphoneET.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                binding.uphoneET.hint = null
//                if (binding.uphoneET.text.isNullOrEmpty()){
//                    binding.uphoneET.hint = "Phone No."
//                }
//            }
//
//            override fun afterTextChanged(p0: Editable?) {}
//
//        })
//
//        binding.uageET.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                binding.uageET.hint = null
//                if (binding.uageET.text.isNullOrEmpty()){
//                    binding.uageET.hint = "Age"
//                }
//            }
//
//            override fun afterTextChanged(p0: Editable?) {}
//
//        })

        binding.uemailET.setOnFocusChangeListener { view, hasFocus ->
            Log.d("CreateAccount","Focused change listener of email called")
            if (hasFocus){
                binding.textInputLayout2.error = null
            }
            else if(!hasFocus){
                if (!Patterns.EMAIL_ADDRESS.matcher(binding.uemailET.text.toString()).matches()){
                    isInfoValid = false
                    binding.textInputLayout2.error = "Invalid Email"
                }
                else{
                    isInfoValid = true
                }
            }
        }

        binding.uphoneET.setOnFocusChangeListener { view, hasFocus ->
            Log.d("CreateAccount","Focused change listener of email called")
            val phoneText = binding.uphoneET.text.toString()


            if (hasFocus){
                binding.textInputLayout4.error = null
            }
            else if(!hasFocus){
                if (phoneText.length != 11){
                    isInfoValid = false
                    binding.textInputLayout4.error = "Invalid Phone No."
                }
                else{
                    isInfoValid = true
                }
            }
        }

        binding.uageET.setOnFocusChangeListener { view, hasFocus ->
            Log.d("CreateAccount", "Focused change listener of email called")
            val ageText = binding.uageET.text.toString()

            if (hasFocus) {
                binding.textInputLayout5.error = null
            } else if (!hasFocus) {
                if (ageText.isNotEmpty()) {
                    val age = ageText.toIntOrNull()

                    if (age != null && age in 1..99) {
                        // Valid age
                        isInfoValid = true
                    } else {
                        // Invalid age
                        isInfoValid = false
                        binding.textInputLayout5.error = "Invalid Age"
                    }
                } else {
                    // Empty input
                    isInfoValid = false
                    binding.textInputLayout5.error = "Age is required"
                }
            }
        }



        binding.upassET.setOnFocusChangeListener { view, hasFocus ->
            Log.d("CreateAccount","Focused change listener of password called")
            if (hasFocus){
                binding.textInputLayout3.error = null
            }
            else if (!hasFocus){
                if (binding.upassET.text.toString().length<6){
                    binding.textInputLayout3.error = "Password Length should be at least 6"
                    isInfoValid = false
                }
                else{
                    isInfoValid = true
                }
            }
        }

        binding.signupBtn.setOnClickListener {
            binding.unameET.clearFocus()
            binding.uemailET.clearFocus()
            binding.upassET.clearFocus()
            binding.uphoneET.clearFocus()
            binding.uageET.clearFocus()
            createAcc()
        }
    }

    private fun createAcc(){
            setProgressBar(true)
            val firebaseAuth = FirebaseAuth.getInstance()

            val currentUser = userToUpdate
            if (currentUser != null && currentUser is RegisteredUser){
                Log.d("CreateAccount", "currentUser != null && currentUser is RegisteredUser")

                Constants.DATABASE_REFERENCE_USERS.child("Registered").orderByChild("userID").equalTo(Globals.CURRENT_USER_ID).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            Log.d("CreateAccount", "RegisteredUser exists")
                            for (snap in snapshot.children){
                                Constants.DATABASE_REFERENCE_USERS.child("Registered").child(snap.key!!).child("userName").setValue(binding.unameET.text.toString())
                                Constants.DATABASE_REFERENCE_USERS.child("Registered").child(snap.key!!).child("userPassword").setValue(binding.upassET.text.toString())
                                Constants.DATABASE_REFERENCE_USERS.child("Registered").child(snap.key!!).child("userPhoneNo").setValue(binding.uphoneET.text.toString())
                                Constants.DATABASE_REFERENCE_USERS.child("Registered").child(snap.key!!).child("userAge").setValue(binding.uageET.text.toString().toInt())
                                Constants.DATABASE_REFERENCE_USERS.child("Registered").child(snap.key!!).child("userGender").setValue(gender)
                            }

                            val createdUser = RegisteredUser(FirebaseAuth.getInstance().uid!!, binding.unameET.text.toString(), binding.uemailET.text.toString().lowercase(), binding.upassET.text.toString(), binding.uphoneET.text.toString(), binding.uageET.text.toString().toInt(), gender)
                            Globals.CURRENT_USER = createdUser
                            setProgressBar(false)
                            finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}

                })

                if (currentUser.userPassword != binding.upassET.text.toString()){
                    Log.d("CreateAccount", "currentUser.userPassword != binding.upassET.text.toString()")

                    firebaseAuth.currentUser!!.updatePassword(binding.upassET.text.toString()).addOnCompleteListener {task ->
                        if (task.isSuccessful){
                            firebaseAuth.signInWithEmailAndPassword(binding.uemailET.text.toString().lowercase(), binding.upassET.text.toString()).addOnCompleteListener(this) { authTask ->
                                if (authTask.isSuccessful) {
                                    setProgressBar(false)
                                    finish()
                                }
                            }
                        }
                    }
                }
            }

            else {
                if (isInfoValid){
                    Log.d("CreateAccount", "createUserWithEmailAndPassword")

                    firebaseAuth.createUserWithEmailAndPassword(binding.uemailET.text.toString().lowercase(), binding.upassET.text.toString())
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful){
                                setProgressBar(false)

                                Toast.makeText(applicationContext,"Account Created Successfully", Toast.LENGTH_SHORT).show()

                                val createdUser = RegisteredUser(FirebaseAuth.getInstance().uid!!, binding.unameET.text.toString(), binding.uemailET.text.toString().lowercase(), binding.upassET.text.toString(), binding.uphoneET.text.toString(), binding.uageET.text.toString().toInt(), gender)

                                Constants.DATABASE_REFERENCE_USERS.child("Registered").push().setValue(createdUser)


                                updateCartReference(FirebaseAuth.getInstance().uid!!)
                                deleteGuestAcc()

                                firebaseAuth.signOut()
                                startActivity(Intent(this, UserLoginActivity::class.java))
                            }
                            else{
                                Toast.makeText(this,"Account Creation Failed. Try Again",Toast.LENGTH_SHORT).show()
                                setProgressBar(false)

                            }

                        }
                }else{
                    Toast.makeText(applicationContext,"Invalid Data Entered",Toast.LENGTH_SHORT).show()
                }

            }


    }

    private fun deleteGuestAcc() {
        Constants.DATABASE_REFERENCE_USERS.child("Guests").orderByChild("guestID").equalTo(guestID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.ref.removeValue()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }


    private fun updateCartReference(newUserId: String) {
        Constants.DATABASE_REFERENCE_CARTS.child(guestID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Constants.DATABASE_REFERENCE_CARTS.child(newUserId).setValue(dataSnapshot.value)
                dataSnapshot.ref.removeValue()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

    private fun setProgressBar(progress : Boolean){
        if (progress){
            binding.progressBar.visibility = View.VISIBLE
        }
        else
            binding.progressBar.visibility = View.INVISIBLE
    }


}