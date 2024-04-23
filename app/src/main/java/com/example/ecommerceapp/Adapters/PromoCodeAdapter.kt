package com.example.ecommerceapp.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerceapp.Activities.SendPromoCodeActivity
import com.example.ecommerceapp.Models.PromoCode
import com.example.ecommerceapp.R

class PromoCodeAdapter(private val context: Context, private var promoCodeList: List<PromoCode>) : RecyclerView.Adapter<PromoCodeAdapter.RCVholder>() {

    class RCVholder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val code_id = itemView.findViewById<TextView>(R.id.promoCodeID_iv)
        val applicable_categories = itemView.findViewById<TextView>(R.id.promoCodeApplicableCategories_iv)
        val discount_percent = itemView.findViewById<TextView>(R.id.promoCodeDiscountPercent_iv)
        val expiry_date = itemView.findViewById<TextView>(R.id.promoCodeExpiryDate_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RCVholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.promo_code_item_view,parent,false)
        return RCVholder(itemView)
    }

    override fun onBindViewHolder(holder: RCVholder, position: Int) {
        val currentItem = promoCodeList[position]
        holder.code_id.text = currentItem.code
        holder.applicable_categories.text = currentItem.applicableProducts.joinToString(", ")
        holder.discount_percent.text = "${currentItem.discountPercent}%"
        holder.expiry_date.text = currentItem.expiryDate

        holder.itemView.setOnClickListener {
            val codeIntent = Intent(context, SendPromoCodeActivity::class.java)
            codeIntent.putExtra("Current_Promo_Code", currentItem)
            context.startActivity(codeIntent)
        }
    }

    override fun getItemCount(): Int {
        return promoCodeList.size
    }

}