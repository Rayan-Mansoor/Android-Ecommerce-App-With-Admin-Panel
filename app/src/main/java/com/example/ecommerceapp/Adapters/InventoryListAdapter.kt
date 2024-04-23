package com.example.ecommerceapp.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.denzcoskun.imageslider.models.SlideModel
import com.example.ecommerceapp.Activities.AddProductActivity
import com.example.ecommerceapp.Activities.ProductDetailActivity
import com.example.ecommerceapp.Helpers.ImageStorageManager
import com.example.ecommerceapp.Models.Categories
import com.example.ecommerceapp.Models.Notification
import com.example.ecommerceapp.Models.NotificationType
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class InventoryListAdapter(private var context : Context, private val activityIdentifier : String, private var inventoryList: ArrayList<Product>, private val myProductClick: ManageMyProducts) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var filteredList: List<Product> = inventoryList
    private var imageStorageManager = ImageStorageManager(context.applicationContext)

    class RCVholderUser(itemView : View) : RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.prodName_iv)
        val desc = itemView.findViewById<TextView>(R.id.prodDesc_iv)
        val price = itemView.findViewById<TextView>(R.id.prodPrice_iv)
        val addFrvt = itemView.findViewById<ImageButton>(R.id.fvrtBtn_iv)
        val addCart = itemView.findViewById<ImageButton>(R.id.addToCartBtn_iv)
        val mainImage = itemView.findViewById<ImageView>(R.id.prodImg_iv)
        var inCart = false
        var inFrvt = false

    }

    class RCVholderAdmin(itemView : View) : RecyclerView.ViewHolder(itemView){
        val admName = itemView.findViewById<TextView>(R.id.pname_iv)
        val admPrice = itemView.findViewById<TextView>(R.id.pprice_iv)
        val admImg = itemView.findViewById<ImageView>(R.id.pImg_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.product_item_view, parent, false)
                RCVholderUser(itemView)
            }
            1 -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.inventory_item_view, parent, false)
                RCVholderAdmin(itemView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is RCVholderUser) {
            val currentItem = filteredList[position]

            holder.name.text = currentItem.productName
            holder.price.text = "${currentItem.productPrice.toInt()}$"
            holder.desc.text = currentItem.productDescription

            if (currentItem.productContainsImages) {
                retrieveMainImage(currentItem, holder)
            }

            checkCart(currentItem, holder)
            checkWishlist(currentItem, holder)

            holder.addCart.setOnClickListener {
                myProductClick.addToCart(currentItem.productID, currentItem.productCategory)
                holder.addCart.visibility = View.INVISIBLE
            }

            holder.addFrvt.setOnClickListener {
                if (!holder.inFrvt){
                    Log.d("WishlistCheck", "not in list")

                    myProductClick.addToWishList(currentItem.productID)
                    holder.addFrvt.setImageResource(R.drawable.baseline_favorite_24)
                    holder.inFrvt = true

                }
                else {
                    Log.d("WishlistCheck", "in list")

                    myProductClick.deleteFromWishList(currentItem.productID)
                    holder.addFrvt.setImageResource(R.drawable.baseline_favorite_border_24)
                    holder.inFrvt = false
                }

            }

            holder.itemView.setOnClickListener {
                val intent = Intent(context, ProductDetailActivity::class.java)
                intent.putExtra("Current_Product", currentItem)
                Log.d("InventoryListAdapterLog",holder.inCart.toString())
                intent.putExtra("Is_In_Cart", holder.inCart)
                context.startActivity(intent)
            }
        }

        if (holder is RCVholderAdmin){
            val currentItem = filteredList[position]
            holder.admName.text = currentItem.productName
            holder.admPrice.text = "${currentItem.productPrice.toString()}$"
            if (currentItem.productContainsImages) {
                retrieveMainImage(currentItem, holder)
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(context, AddProductActivity::class.java)
                intent.putExtra("Update_Product", currentItem)
                context.startActivity(intent)
            }

            holder.itemView.setOnLongClickListener {
                showDeleteDialog(position)
                true
            }
        }

    }

    private fun checkCart(currentItem : Product, holder: RCVholderUser) {
        Constants.DATABASE_REFERENCE_CARTS.child(Globals.CURRENT_USER_ID!!).orderByChild("itemID").equalTo(currentItem.productID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    holder.addCart.visibility = View.INVISIBLE
                    holder.inCart = true
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun checkWishlist(currentItem : Product, holder: RCVholderUser) {
            Constants.DATABASE_REFERENCE_WISHLIST.child(Globals.CURRENT_USER_ID!!).orderByValue().equalTo(currentItem.productID).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        holder.addFrvt.setImageResource(R.drawable.baseline_favorite_24)
                        holder.inFrvt = true
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun retrieveMainImage(currentItem : Product, holder: RecyclerView.ViewHolder) {
        val context = holder.itemView.context

        // Check if the context is still valid (activity is not destroyed)
        if (context is Activity && !context.isDestroyed) {
            Constants.DATABASE_REFERENCE_IMAGES.child("Products").child(currentItem.productID).child("0").get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result

                    if (snapshot != null && snapshot.exists()) {
                        val imagePath = snapshot.getValue(String::class.java)

                        if (imagePath != null) {
                            Globals.downloadImageAndGetUri("", imagePath, imageStorageManager,
                                onSuccess = { _, uri ->


                                    if (holder is RCVholderUser){
                                        Glide.with(context)
                                            .load(uri)
                                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                                            .into(holder.mainImage)
                                    }
                                    else if (holder is RCVholderAdmin) {
                                        Glide.with(context)
                                            .load(uri)
                                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                                            .into(holder.admImg)
                                    }

                                },
                                onFailure = { exception ->
                                    // Handle errors
                                    println("Error downloading image: $exception")
                                })
                        } else {
                            println("Value is null")
                        }
                    } else {
                        println("Snapshot does not exist at the specified location")
                    }
                } else {
                    // Handle the error if any
                    val exception = task.exception
                    println("Error retrieving data: ${exception?.message}")
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (activityIdentifier == "UserHomeFragment") {
            0
        } else if (activityIdentifier == "ManageInventoryActivity") {
            1
        } else {
            -1
        }
    }

    fun filterByCategory(category: String) {
        if (category == "All"){
            filteredList = inventoryList
        } else{
            filteredList = inventoryList.filter { it.productCategory.name == category }
        }

        notifyDataSetChanged()
    }

    fun filterByName(name: String) {
        if (name == ""){
            filteredList = inventoryList
        } else{
            filteredList = inventoryList.filter { it.productName.lowercase().contains(name.lowercase()) }
        }

        notifyDataSetChanged()
    }

    fun filterByProductIDs(ids: List<String>) {
        if (ids.isEmpty()) {
            filteredList = emptyList()
        } else {
            filteredList = inventoryList.filter { it.productID in ids }
        }

        notifyDataSetChanged()
    }


    private fun showDeleteDialog(position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Image")
            .setMessage("Are you sure you want to delete this Product?")
            .setPositiveButton("Delete") { _, _ ->
                // Handle delete action
                deleteItem(position)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteItem(position: Int) {

        val imagePath = "images/products/${inventoryList[position].productID}"

        Constants.CLOUD_STORAGE_REFERENCE.child(imagePath).delete()

        Constants.DATABASE_REFERENCE_IMAGES.child("Products").child(inventoryList[position].productID).removeValue()
        Constants.DATABASE_REFERENCE_PRODUCTS.child(inventoryList[position].productID).removeValue()

        inventoryList.removeAt(position)
        notifyItemRemoved(position)
    }

    interface ManageMyProducts{
        fun addToCart(productID: String, productCategory: Categories)
        fun addToWishList(productID: String)
        fun deleteFromWishList(productID: String)
    }

}

