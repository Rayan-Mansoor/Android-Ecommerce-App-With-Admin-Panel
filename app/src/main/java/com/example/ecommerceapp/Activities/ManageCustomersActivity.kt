package com.example.ecommerceapp.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.Adapters.CustomerListAdapter
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.FirebaseDatabaseManager
import com.example.ecommerceapp.databinding.ActivityManageCustomersBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ManageCustomersActivity : AppCompatActivity(), FirebaseDatabaseManager.RegisteredUsersDataChangeListener {
    private lateinit var binding: ActivityManageCustomersBinding
    private lateinit var customerListAdapter: CustomerListAdapter
    private  var customerList : ArrayList<RegisteredUser> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageCustomersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        customerListAdapter = CustomerListAdapter(this, "ManageCustomersActivity", customerList, null)

        FirebaseDatabaseManager.addRegisteredUsersDataChangeListener(this)

        binding.rcvCustomerList.adapter = customerListAdapter
        binding.rcvCustomerList.layoutManager = LinearLayoutManager(this)
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