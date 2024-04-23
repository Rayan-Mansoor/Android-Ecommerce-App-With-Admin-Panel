package com.example.ecommerceapp.Activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ecommerceapp.Adapters.InventoryListAdapter
import com.example.ecommerceapp.Models.Categories
import com.example.ecommerceapp.Models.Items
import com.example.ecommerceapp.Models.Order
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.FirebaseDatabaseManager
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.ActivityWishlistBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class WishlistActivity : AppCompatActivity(), FirebaseDatabaseManager.WishlistDataChangeListener {
    private lateinit var binding : ActivityWishlistBinding
    private lateinit var inventoryListAdapter: InventoryListAdapter
    private  var inventoryList : ArrayList<Product> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inventoryListAdapter = InventoryListAdapter(this, "UserHomeFragment", inventoryList, object : InventoryListAdapter.ManageMyProducts {
            override fun addToCart(productID: String, productCat : Categories) {
                val cartReference = Globals.CURRENT_USER_ID?.let {
                    Constants.DATABASE_REFERENCE_CARTS.child(
                        it
                    )
                }

                cartReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var productAlreadyInCart = false

                        for (childSnapshot in snapshot.children) {
                            val value = childSnapshot.value as? String

                            if (value == productID) {
                                Toast.makeText(this@WishlistActivity,"Product Already In Cart", Toast.LENGTH_SHORT).show()
                                productAlreadyInCart = true
                                break
                            }
                        }

                        if (!productAlreadyInCart) {
                            val newCartItemReference = cartReference.push()

                            val newItem = Items(productID, productCat, 1)
                            newCartItemReference.setValue(newItem)

                            Toast.makeText(this@WishlistActivity,"Product Added To Cart", Toast.LENGTH_SHORT).show()

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }

            override fun addToWishList(productID: String) {
                Constants.DATABASE_REFERENCE_WISHLIST.child(Globals.CURRENT_USER_ID!!).push().setValue(productID)
            }

            override fun deleteFromWishList(productID: String) {
                Constants.DATABASE_REFERENCE_WISHLIST.child(Globals.CURRENT_USER_ID!!).orderByValue().equalTo(productID).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (childSnapshot in dataSnapshot.children) {
                                childSnapshot.ref.removeValue()
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }
        })

        FirebaseDatabaseManager.addWishlistDataChangeListener(this, Globals.CURRENT_USER_ID!!)

        binding.myWishlistRCV.adapter = inventoryListAdapter
        binding.myWishlistRCV.layoutManager = GridLayoutManager(this, 3)

    }

    override fun onWishlistDataChanged(updatedData: List<String>) {
        inventoryList.clear()
        inventoryList.addAll(FirebaseDatabaseManager.productData)
        inventoryListAdapter.filterByProductIDs(updatedData)
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseDatabaseManager.removeWishlistDataChangeListener(this, Globals.CURRENT_USER_ID!!)
    }
}