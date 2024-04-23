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
import com.example.ecommerceapp.Models.UserType
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.FragmentAdminHomeBinding
import com.example.ecommerceapp.databinding.FragmentAdminPanelBinding
import com.google.firebase.auth.FirebaseAuth

class AdminPanelFragment : Fragment() {
    private lateinit var binding: FragmentAdminPanelBinding
    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = requireContext().getSharedPreferences("App_Cache", Context.MODE_PRIVATE)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdminPanelBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.mngCustomers.setOnClickListener {
            startActivity(Intent(requireContext(), ManageCustomersActivity::class.java))
        }

        binding.mngInventory.setOnClickListener {
            startActivity(Intent(requireContext(), ManageInventoryActivity::class.java))
        }

        binding.mngPromo.setOnClickListener {
            startActivity(Intent(requireContext(), ManagePromoCodesActivity::class.java))

        }

        binding.edtSlider.setOnClickListener {
            startActivity(Intent(requireContext(), EditSliderActivity::class.java))
        }
    }
}