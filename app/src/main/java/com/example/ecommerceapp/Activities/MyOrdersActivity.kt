package com.example.ecommerceapp.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.Adapters.OrdersAdapter
import com.example.ecommerceapp.Models.Order
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.FirebaseDatabaseManager
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.ActivityMyOrdersBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class MyOrdersActivity : AppCompatActivity(), FirebaseDatabaseManager.OrderDataChangeListener  {
    private lateinit var binding : ActivityMyOrdersBinding
    private lateinit var ordersAdapter: OrdersAdapter
    private  var orderList : ArrayList<Order> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ordersAdapter = OrdersAdapter(this, "MyOrdersActivity", orderList)

//        Constants.DATABASE_REFERENCE_Orders.child(Globals.CURRENT_USER_ID!!).addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(orderSnapshot: DataSnapshot) {
//                if (orderSnapshot.exists()) {
//                    val newOrderList: ArrayList<Order> = ArrayList()
//                        for (orderChildSnapshot in orderSnapshot.children) {
//                            val order = orderChildSnapshot.getValue(Order::class.java)
//                            if (order != null) {
//                                newOrderList.add(order)
//                            }
//                        }
//
//                    orderList.clear()
//                    orderList.addAll(newOrderList)
//                    ordersAdapter.notifyDataSetChanged()
//                } else {
//                    Log.d("AdminHomeFragmentLog", "Orders not found")
//                }
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("AdminHomeFragmentLog", "Error fetching Orders: ${error.message}")
//            }
//        })

        FirebaseDatabaseManager.addOrderDataChangeListener(this)

        binding.myOrdersRCV.adapter = ordersAdapter
        binding.myOrdersRCV.layoutManager = LinearLayoutManager(this)

    }

    override fun onOrderDataChanged(updatedData: Map<String, List<Order>>) {
        orderList.clear()
        updatedData[Globals.CURRENT_USER_ID!!]?.let { orderList.addAll(it) }
        ordersAdapter.notifyDataSetChanged()

    }

    override fun onDestroy() {
        super.onDestroy()

        FirebaseDatabaseManager.removeOrderDataChangeListener(this)
    }
}