package com.example.ecommerceapp.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
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
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.FragmentAdminPanelBinding
import com.example.ecommerceapp.databinding.FragmentAdminSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import kotlin.properties.Delegates

class AdminSettingsFragment : Fragment() {
    private lateinit var binding : FragmentAdminSettingsBinding
    private lateinit var sharedPreferences : SharedPreferences
    private var currentUname: String by Delegates.observable("") { _, _, newValue ->
        binding.currentAdminName.text = newValue
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("App_Cache", Context.MODE_PRIVATE)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = Globals.CURRENT_USER
        if (currentUser is GuestUser){
            currentUname = currentUser.guestID
        }
        else if (currentUser is RegisteredUser){
            currentUname = currentUser.userName

        }
        else if (currentUser is AdminUser){
            currentUname = currentUser.adminEmail
            Log.d("AdminSettingsFragment", Globals.CURRENT_USER.toString())

        }

        binding.editProfile.visibility = View.GONE

        binding.adminLogoutBtn.setOnClickListener {
            sharedPreferences.edit().putString("current_user", UserType.GUEST.name).apply()

//            sharedPreferences.edit().putBoolean("is_admin", false).apply()
            FirebaseAuth.getInstance().signOut()
            Globals.CURRENT_USER_ID = Globals.DEVICE_ANDROID_ID
            Globals.CURRENT_USER = GuestUser(Globals.DEVICE_ANDROID_ID!!)

            Log.d("AdminPanelActivityLog", Globals.CURRENT_USER_ID!!)
            startActivity(Intent(requireContext(), UserActivity::class.java))
            requireActivity().finish()
        }
    }
}