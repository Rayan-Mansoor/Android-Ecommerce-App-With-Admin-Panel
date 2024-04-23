package com.example.ecommerceapp.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.Adapters.CustomerListAdapter
import com.example.ecommerceapp.Models.PromoCode
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.Utils.FirebaseDatabaseManager
import com.example.ecommerceapp.databinding.ActivitySendPromoCodeBinding

class SendPromoCodeActivity : AppCompatActivity(), FirebaseDatabaseManager.RegisteredUsersDataChangeListener {
    private lateinit var binding : ActivitySendPromoCodeBinding
    private lateinit var receivedCode : PromoCode
    private lateinit var customerListAdapter: CustomerListAdapter
    private  var customerList : ArrayList<RegisteredUser> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySendPromoCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receivedCode = intent.getSerializableExtra("Current_Promo_Code") as PromoCode

        customerListAdapter = CustomerListAdapter(this, "SendPromoCodeActivity", customerList, receivedCode)

        FirebaseDatabaseManager.addRegisteredUsersDataChangeListener(this)

        binding.regUsersRCV.adapter = customerListAdapter
        binding.regUsersRCV.layoutManager = LinearLayoutManager(this)
    }

    override fun onRegisteredUsersDataChanged(updatedData: List<RegisteredUser>) {
        customerList.clear()
        customerList.addAll(updatedData)
        customerListAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseDatabaseManager.removeRegisteredUsersDataChangeListener(this)

    }
}