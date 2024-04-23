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
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CartAdapter(private var context : Context, private var cartProductList: List<Product>, private var cartItemList: List<Items>, private val updateItemTotal: TotalAmountListener) : RecyclerView.Adapter<CartAdapter.RCVholder>() {
    private var itemGrandTotal = 0
    private var imageStorageManager = ImageStorageManager(context.applicationContext)


    class RCVholder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val image =  itemView.findViewById<ImageView>(R.id.cartProdImg_iv)
        val name = itemView.findViewById<TextView>(R.id.cartProdName_iv)
        val totalPrice = itemView.findViewById<TextView>(R.id.cartTotal_iv)
        val quantity = itemView.findViewById<TextView>(R.id.cartQuantity_iv)
        val increaseQuantity = itemView.findViewById<ImageView>(R.id.cartAdd_iv)
        val decreaseQuantity = itemView.findViewById<ImageView>(R.id.cartSub_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RCVholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cart_item_view,parent,false)

        return RCVholder(itemView)
    }

    override fun onBindViewHolder(holder: RCVholder, position: Int) {
        val currentItem = cartProductList[position]
        var quantity = 1
        
        holder.name.text = currentItem.productName


        for (item in cartItemList){
            if (item.itemID == currentItem.productID){
                quantity = item.quantity
                holder.quantity.text = item.quantity.toString()
                holder.totalPrice.text = "${(currentItem.productPrice * quantity).toInt().toString()}$"
                itemGrandTotal += (currentItem.productPrice * quantity).toInt()

                updateItemTotal.onTotalAmountChanged(itemGrandTotal)
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
                        Log.d("DatabaseLog", "Value is null")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DatabaseLog", "Error fetching value from database: $error")
                }
            })
        }


        holder.increaseQuantity.setOnClickListener {
            quantity++
            holder.quantity.text = quantity.toString()
            holder.totalPrice.text = "${currentItem.productPrice.toInt() * quantity}$"

            itemGrandTotal += currentItem.productPrice.toInt()

            updateItemTotal.onTotalAmountChanged(itemGrandTotal)

            Constants.DATABASE_REFERENCE_CARTS.child(Globals.CURRENT_USER_ID!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {

                        val key = childSnapshot.key
                        val item = childSnapshot.getValue(Items::class.java)
                        if (item!!.itemID == currentItem.productID){
                            Constants.DATABASE_REFERENCE_CARTS.child(Globals.CURRENT_USER_ID!!).child(key!!).child("quantity").setValue(quantity)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })

        }

        holder.decreaseQuantity.setOnClickListener {
            if (quantity == 0){
                return@setOnClickListener
            }
            quantity--
            holder.quantity.text = quantity.toString()
            holder.totalPrice.text = "${currentItem.productPrice.toInt() * quantity}$"

            itemGrandTotal -= currentItem.productPrice.toInt()

            updateItemTotal.onTotalAmountChanged(itemGrandTotal)

            Constants.DATABASE_REFERENCE_CARTS.child(Globals.CURRENT_USER_ID!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {

                        val key = childSnapshot.key
                        val item = childSnapshot.getValue(Items::class.java)
                        if (item!!.itemID == currentItem.productID){
                            Constants.DATABASE_REFERENCE_CARTS.child(Globals.CURRENT_USER_ID!!).child(key!!).child("quantity").setValue(quantity)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    }

    fun resetItemGrandTotal() {
        itemGrandTotal = 0
    }

    override fun getItemCount(): Int {
        return cartProductList.size
    }

    interface TotalAmountListener {
        fun onTotalAmountChanged(value: Int)
    }

}