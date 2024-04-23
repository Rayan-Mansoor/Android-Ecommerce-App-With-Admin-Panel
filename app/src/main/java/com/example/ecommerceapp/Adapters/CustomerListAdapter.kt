package com.example.ecommerceapp.Adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerceapp.Models.Notification
import com.example.ecommerceapp.Models.NotificationType
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.Models.PromoCode
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.Models.Review
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

class CustomerListAdapter(private val context : Context, private val activityIdentifier : String, private var customerList: List<RegisteredUser>, private val receiverCode : PromoCode?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class RCVholderMCA(itemView : View) : RecyclerView.ViewHolder(itemView){
        val image =  itemView.findViewById<ImageView>(R.id.uImg_iv)
        val name = itemView.findViewById<TextView>(R.id.uname_iv)
        val phone = itemView.findViewById<TextView>(R.id.unumber_iv)
    }

    class RCVholderSPA(itemView : View) : RecyclerView.ViewHolder(itemView){
        val regUserName =  itemView.findViewById<TextView>(R.id.SPC_UserName_iv)
        val regNoOrders = itemView.findViewById<TextView>(R.id.SPC_no_of_orders)
        val sendCode = itemView.findViewById<ImageButton>(R.id.SPC_send_code)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.customer_item_view, parent, false)
                RCVholderMCA(itemView)
            }
            1 -> {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.customer_promo_code_item_view, parent, false)
                RCVholderSPA(itemView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RCVholderMCA){
            val currentItem = customerList[position]
            holder.name.text = currentItem.userName
            holder.phone.text = currentItem.userPhoneNo.toString()

            holder.itemView.setOnClickListener {
                detailsPopup(currentItem)
            }
        }
        if (holder is RCVholderSPA){
            val currentItem = customerList[position]
            holder.regUserName.text = currentItem.userName

            Constants.DATABASE_REFERENCE_Orders.child(currentItem.userID).get().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    holder.regNoOrders.text = "No. of orders: ${task.result.childrenCount}"
                }
            }
            holder.sendCode.setOnClickListener {
                val notification = Notification(NotificationType.PROMO_CODE, "You have been awarded a discount coupon : ${receiverCode!!.code}", System.currentTimeMillis())
                Constants.DATABASE_REFERENCE_NOTIFICATIONS.child(currentItem.userID).push().setValue(notification)

                sendEmail("Ecommerce Store - Promo Code", "Hey! Its your lucky day.\nYou have been awarded a discount promo code\n\nCode: ${receiverCode.code}\n\nApplicable Categories: ${receiverCode.applicableProducts.joinToString(", ")}\n\nExpiry Date: ${receiverCode.expiryDate}", currentItem.userEmail)
//                Toast.makeText(context, "Email Sent Successfully", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun getItemCount(): Int {
        return customerList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (activityIdentifier == "ManageCustomersActivity") {
            0
        } else if (activityIdentifier == "SendPromoCodeActivity") {
            1
        } else {
            -1
        }
    }

    private fun detailsPopup(user : RegisteredUser){
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val customerView = inflater.inflate(R.layout.customer_detail_item_view, null)
        dialogBuilder.setView(customerView)

        val name = customerView.findViewById<TextView>(R.id.nameDetTV)
        val email = customerView.findViewById<TextView>(R.id.emailDetTV)
        val phone = customerView.findViewById<TextView>(R.id.phoneDetTV)
        val age = customerView.findViewById<TextView>(R.id.ageDetTV)
        val gender = customerView.findViewById<TextView>(R.id.genderDetTV)

        name.text = "Customer Name: ${user.userName}"
        email.text = "Customer Email: ${user.userEmail}"
        phone.text = "Customer Phone No.: ${user.userPhoneNo}"
        age.text = "Customer Age: ${user.userAge}"
        gender.text = "Customer Gender: ${user.userGender?.name}"

        dialogBuilder.setTitle("Customer Details")
            .setView(customerView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

    }

    private fun sendEmail(subject: String, body: String, recipient: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            data = Uri.parse("mailto:")
            type ="text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Send Email"))
        }
        catch (e : Exception){
            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()

        }
    }

}