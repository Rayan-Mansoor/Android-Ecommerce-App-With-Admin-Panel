package com.example.ecommerceapp.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.ecommerceapp.Helpers.ImageStorageManager
import com.example.ecommerceapp.Models.Items
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class CheckoutAdapter(private var context : Context, private var checkoutProductList: List<Product>, private var checkoutItemList: List<Items>) : RecyclerView.Adapter<CheckoutAdapter.RCVholder>() {
    private var quantity = 1
    private var imageStorageManager = ImageStorageManager(context.applicationContext)

    class RCVholder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val image =  itemView.findViewById<ImageView>(R.id.checkoutProdImg_iv)
        val name = itemView.findViewById<TextView>(R.id.checkoutProdName_iv)
        val totalPrice = itemView.findViewById<TextView>(R.id.checkoutTotal_iv)
        val quantity = itemView.findViewById<TextView>(R.id.checkoutQuantity_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RCVholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.checkout_item_view,parent,false)
        return RCVholder(itemView)
    }

    override fun onBindViewHolder(holder: RCVholder, position: Int) {
        val currentItem = checkoutProductList[position]
        holder.name.text = currentItem.productName


        for (item in checkoutItemList){
            if (item.itemID == currentItem.productID){
                quantity = item.quantity
                holder.quantity.text = item.quantity.toString()
                holder.totalPrice.text = "${(currentItem.productPrice * quantity).toString()}$"
            }
        }


        if (currentItem.productContainsImages){

            Constants.DATABASE_REFERENCE_IMAGES.child("Products").child(currentItem.productID).child("0").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productMainImgPath = snapshot.getValue(String::class.java)
                    if (productMainImgPath != null) {
                        Globals.downloadImageAndGetUri("", productMainImgPath, imageStorageManager,
                            onSuccess = { _, uri ->
                                Glide.with(context)
                                    .load(uri)
                                    .apply(
                                        RequestOptions()
                                            .placeholder(R.drawable.baseline_person_24)  // Optional placeholder image
                                            .error(R.drawable.baseline_error_24)
                                    )  // Optional error image
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)  // Caching strategy
                                    .into(holder.image)
                                println("Image Uri: $uri")
                            },
                            onFailure = { exception ->
                                // Handle errors
                                println("Error downloading image: $exception")
                            })
                        Log.d("DatabaseLog", "Value from database: $productMainImgPath")
                    } else {
                        // Value is null, handle accordingly
                        Log.d("DatabaseLog", "Value is null")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                    Log.e("DatabaseLog", "Error fetching value from database: $error")
                }
            })
        }
    }

    override fun getItemCount(): Int {
        return checkoutProductList.size
    }

}