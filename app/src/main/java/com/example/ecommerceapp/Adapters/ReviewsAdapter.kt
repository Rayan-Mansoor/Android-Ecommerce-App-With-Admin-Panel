package com.example.ecommerceapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerceapp.Models.Review
import com.example.ecommerceapp.R

class ReviewsAdapter(private var reviewsList: List<Review>) : RecyclerView.Adapter<ReviewsAdapter.RCVholder>() {

    class RCVholder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val user_name = itemView.findViewById<TextView>(R.id.rName_iv)
        val user_comment = itemView.findViewById<TextView>(R.id.uComments_iv)
        val user_rating = itemView.findViewById<RatingBar>(R.id.uRating_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RCVholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.review_item_view,parent,false)
        return RCVholder(itemView)
    }

    override fun onBindViewHolder(holder: RCVholder, position: Int) {
        val currentItem = reviewsList[position]
        holder.user_name.text = currentItem.uname
        holder.user_comment.text = currentItem.comment
        holder.user_rating.rating = currentItem.rating
    }

    override fun getItemCount(): Int {
        return reviewsList.size
    }

}