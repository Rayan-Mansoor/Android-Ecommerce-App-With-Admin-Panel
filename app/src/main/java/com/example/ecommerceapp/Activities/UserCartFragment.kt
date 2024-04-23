package com.example.ecommerceapp.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.Adapters.CartAdapter
import com.example.ecommerceapp.Models.Items
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.FirebaseDatabaseManager
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.FragmentUserCartBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.properties.Delegates

class UserCartFragment : Fragment() {
    private lateinit var binding: FragmentUserCartBinding
    private lateinit var cartAdapter: CartAdapter
    private  var cartProductList : ArrayList<Product> = ArrayList()
    private  var cartItemList : ArrayList<Items> = ArrayList()
    private var itemsTotal: Int by Delegates.observable(0) { _, _, newValue ->
        Log.d("UCF", "grandTotalTV: ${binding.grandTotalTV.text} called ")

        view?.postDelayed({
            binding.grandTotalTV.text = "${newValue}$"
        }, 100)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("UserActivityLog", "onCreate UserCartFragment")

        Log.d("UserCartFragmentLog", "user UID: ${Globals.CURRENT_USER_ID!!}")

        Constants.DATABASE_REFERENCE_CARTS.child(Globals.CURRENT_USER_ID!!).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val itemIDs = mutableListOf<String>()

                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(Items::class.java)
                        if (item != null) {
                            cartItemList.add(item)
                            itemIDs.add(item.itemID)
                        }
                    }

                    if (itemIDs.isNotEmpty()) {
                        Constants.DATABASE_REFERENCE_PRODUCTS.orderByChild("productID").addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(productSnapshot: DataSnapshot) {
                                if (productSnapshot.exists()) {
                                    for (productChildSnapshot in productSnapshot.children) {
                                        val product = productChildSnapshot.getValue(Product::class.java)
                                        if (product != null && itemIDs.contains(product.productID)) {
                                            Log.d("CartActivityLog", product.toString())
                                            cartProductList.add(product)
                                        }
                                    }
                                    cartAdapter.notifyDataSetChanged()
                                } else {
                                    Log.d("CartActivityLog", "Products not found")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                                Log.e("CartActivityLog", "Error fetching products: ${error.message}")
                            }
                        })
                    } else {
                        Log.d("CartActivityLog", "No itemIDs found")
                    }
                } else {
                    println("No data found at the specified location")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if any
                println("Error retrieving data: ${error.message}")
            }
        })


        cartAdapter = CartAdapter(requireContext(), cartProductList, cartItemList,  object : CartAdapter.TotalAmountListener {
            override fun onTotalAmountChanged(value: Int) {
                Log.d("UCF", "onTotalAmountChanged: $value")
                Log.d("UCF", "grandTotalTV: ${binding.grandTotalTV.text}")
                itemsTotal = value
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d("UserCartFragmentLog", "onCreate View")

        binding = FragmentUserCartBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.checkoutBtn.setOnClickListener{
            startActivity(Intent(requireContext(), CheckoutActivity::class.java).putExtra("ItemsTotal", itemsTotal))
        }


//        FirebaseDatabaseManager.addCartDataChangeListener(this, Globals.CURRENT_USER_ID!!)

        binding.cartRCV.adapter = cartAdapter
        binding.cartRCV.layoutManager = LinearLayoutManager(requireContext())

    }

//    override fun onCartDataChanged(updatedData: List<Items>) {
//        Log.d("FDM", "onCartDataChanged called in UserCartFragment")
//        cartItemList.clear()
//        cartProductList.clear()
//        cartItemList.addAll(updatedData)
//        cartProductList.addAll(FirebaseDatabaseManager.productData)
//
//        cartProductList.retainAll { product ->
//            cartItemList.any { item -> item.itemID == product.productID }
//        }
//
//        cartAdapter.resetItemGrandTotal()
//        cartAdapter.notifyDataSetChanged()
//    }

}