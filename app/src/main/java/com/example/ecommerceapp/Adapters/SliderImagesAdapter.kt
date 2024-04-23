package com.example.ecommerceapp.Adapters

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.denzcoskun.imageslider.models.SlideModel
import com.example.ecommerceapp.Helpers.ItemMoveListener
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.Utils.Globals.getFileFromUri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.Collections

class SliderImagesAdapter(private var context : Context, private val currentProduct : String?, private var sliderImgList: MutableList<String>, private var imageList: MutableList<SlideModel>, private val itemMoveListener: ItemMoveListener) : RecyclerView.Adapter<SliderImagesAdapter.RCVholder>(),
    ItemMoveListener {

    class RCVholder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val image =  itemView.findViewById<ImageView>(R.id.sliderImg_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RCVholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.slider_item_view,parent,false)
        return RCVholder(itemView)
    }

    override fun onBindViewHolder(holder: RCVholder, position: Int) {
        val currentItem = sliderImgList[position]

        Glide.with(context)
            .load(currentItem)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
            .into(holder.image)

        holder.itemView.setOnLongClickListener {
            showDeleteDialog(position)
            true
        }

    }

    override fun getItemCount(): Int {
        return sliderImgList.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(sliderImgList, fromPosition, toPosition)
        Collections.swap(imageList, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        itemMoveListener.onItemMove(fromPosition, toPosition)
    }

    private fun showDeleteDialog(position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Image")
            .setMessage("Are you sure you want to delete this image?")
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
        val sliderImagePath = "images/slider/${Globals.calculateHash(getFileFromUri(context, Uri.parse(sliderImgList[position]))!!)}.jpg"

        Constants.CLOUD_STORAGE_REFERENCE.child(sliderImagePath).delete()

        if (!currentProduct.isNullOrEmpty()){
            val productImagePath = "images/products/${currentProduct}/${Globals.calculateHash(getFileFromUri(context, Uri.parse(sliderImgList[position]))!!)}.jpg"
            Constants.CLOUD_STORAGE_REFERENCE.child(productImagePath).delete()

        }

        sliderImgList.removeAt(position)
        imageList.removeAt(position)
        notifyItemRemoved(position)
        itemMoveListener.onItemMove(position, -1) // Inform listener about the delete action
    }


}