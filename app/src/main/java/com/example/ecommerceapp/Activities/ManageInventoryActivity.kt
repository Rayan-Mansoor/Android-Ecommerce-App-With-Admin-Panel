package com.example.ecommerceapp.Activities

import android.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.Adapters.InventoryListAdapter
import com.example.ecommerceapp.Models.Categories
import com.example.ecommerceapp.Models.DeliveryStatus
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.Utils.FirebaseDatabaseManager
import com.example.ecommerceapp.databinding.ActivityManageInventoryBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ManageInventoryActivity : AppCompatActivity(), FirebaseDatabaseManager.ProductDataChangeListener {
    private lateinit var binding: ActivityManageInventoryBinding
    lateinit var inventoryListAdapter: InventoryListAdapter
    private var inventoryList : ArrayList<Product> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManageInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val allOptions = arrayOf("All") + Globals.CategoryList

        Log.d("ManageInventoryActivityLog", allOptions.joinToString(", "))


        val categoryAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, allOptions)
        categoryAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        val defaultSelection = "All"
        val defaultSelectionIndex = allOptions.indexOf(defaultSelection)

        Log.d("AdminHomeFragmentLog", defaultSelectionIndex.toString())

        binding.categoryFilter.setSelection(defaultSelectionIndex)

        binding.categoryFilter.adapter = categoryAdapter

        binding.categoryFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = if (position == 0) {
                    "All"
                } else {
                    Globals.CategoryList[position - 1]
                }
                Log.d("ManageInventoryActivityLog", selectedCategory)

                inventoryListAdapter.filterByCategory(selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where nothing is selected (if needed)
            }
        }

        binding.addProdBtn.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }


        inventoryListAdapter = InventoryListAdapter(this, "ManageInventoryActivity",inventoryList, object : InventoryListAdapter.ManageMyProducts {
            override fun addToCart(productID: String, productCategory : Categories) {}

            override fun addToWishList(productID: String) {}

            override fun deleteFromWishList(productID: String) {}
        })

        FirebaseDatabaseManager.addProductDataChangeListener(this)

        binding.rcvProductList.adapter = inventoryListAdapter
        binding.rcvProductList.layoutManager = LinearLayoutManager(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        FirebaseDatabaseManager.removeProductDataChangeListener(this)

    }

    override fun onProductDataChanged(updatedData: List<Product>) {
        inventoryList.clear()
        inventoryList.addAll(updatedData)
        inventoryListAdapter.notifyDataSetChanged()

    }
}