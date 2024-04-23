package com.example.ecommerceapp.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ecommerceapp.Models.AdminUser
import com.example.ecommerceapp.Models.GuestUser
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.Models.UserType
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.FragmentUserSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class UserSettingsFragment : Fragment() {
    private lateinit var binding: FragmentUserSettingsBinding
    private lateinit var sharedPreferences : SharedPreferences
    private var currentUname: String by Delegates.observable("") { _, _, newValue ->
        binding.currentUserName.text = newValue
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("UserActivityLog", "onCreate UserSettingsFragment")

        sharedPreferences = requireContext().getSharedPreferences("App_Cache", Context.MODE_PRIVATE)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = Globals.CURRENT_USER
        if (currentUser is GuestUser){
            currentUname = "Guest# ${currentUser.guestID}"
            binding.userLogoutBtn.visibility = View.GONE
            binding.editUserProfile.visibility = View.GONE
            binding.userLoginBtn.visibility = View.VISIBLE
        }
        else if (currentUser is RegisteredUser){
            currentUname = currentUser.userName
            Log.d("USF", currentUser.toString())
            if (currentUser.userAge == null) {
                binding.editUserProfile.visibility = View.GONE
                binding.googleLogo.visibility = View.VISIBLE

            }
        }
        else if (currentUser is AdminUser){
            currentUname = currentUser.adminEmail
        }

        binding.editUserProfile.setOnClickListener {
            val intent = Intent(requireContext(), UserSignUpActivity::class.java)
            intent.putExtra("current_user_to_edit", Globals.CURRENT_USER!!)
            startActivity(intent)
        }


        binding.userLoginBtn.setOnClickListener {
            startActivity(Intent(requireContext(), UserLoginActivity::class.java))
        }

        binding.userLogoutBtn.setOnClickListener {

            sharedPreferences.edit().putString("current_user", UserType.GUEST.name).apply()

            GlobalScope.launch {
                FirebaseAuth.getInstance().signOut()
            }

            Globals.CURRENT_USER = GuestUser(Globals.DEVICE_ANDROID_ID!!)
            Globals.CURRENT_USER_ID = Globals.DEVICE_ANDROID_ID!!

            startActivity(Intent(requireContext(), UserActivity::class.java))
        }

        binding.gotoAdminLogin.setOnClickListener {
            startActivity(Intent(requireContext(), AdminLoginActivity::class.java))
        }

        binding.myOrdersBtn.setOnClickListener {
            startActivity(Intent(requireContext(), MyOrdersActivity::class.java))
        }

        binding.myWishlistBtn.setOnClickListener {
            startActivity(Intent(requireContext(), WishlistActivity::class.java))
        }
    }
}