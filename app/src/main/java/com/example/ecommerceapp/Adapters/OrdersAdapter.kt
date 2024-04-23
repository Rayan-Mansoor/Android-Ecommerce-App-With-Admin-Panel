package com.example.ecommerceapp.Adapters

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ecommerceapp.Models.DeliveryStatus
import com.example.ecommerceapp.Models.Order
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class OrdersAdapter(private val context: Context, private val activityIdentifier : String, private var orderList: List<Order>) : RecyclerView.Adapter<OrdersAdapter.RCVholder>() {
    private var filteredList: List<Order> = orderList

    class RCVholder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val order_id = itemView.findViewById<TextView>(R.id.orderID_iv)
        val receiver_name = itemView.findViewById<TextView>(R.id.orderReceiverName_iv)
        val amount = itemView.findViewById<TextView>(R.id.orderAmount_iv)
        val status = itemView.findViewById<TextView>(R.id.orderStatus_iv)
        val changeStatus = itemView.findViewById<CheckBox>(R.id.changeOrderStatus)
        val changeMyStatus = itemView.findViewById<CheckBox>(R.id.changeMyOrderStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RCVholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.orders_item_view,parent,false)
        return RCVholder(itemView)
    }

    override fun onBindViewHolder(holder: RCVholder, position: Int) {
        val currentItem = filteredList[position]
        holder.order_id.text = currentItem.orderID
        holder.receiver_name.text = currentItem.receiver.receiverName
        holder.amount.text = "${currentItem.amount.grandTotal.toString()}$"
        holder.status.text = currentItem.status.name
        holder.changeStatus.visibility = View.GONE

        holder.itemView.setOnClickListener {
            detailsPopup(currentItem)
        }

        if (activityIdentifier == "MyOrdersActivity" && holder.status.text == DeliveryStatus.PENDING.name){
            holder.changeMyStatus.visibility = View.VISIBLE
        }

        holder.changeMyStatus.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Constants.DATABASE_REFERENCE_Orders.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(orderSnapshot: DataSnapshot) {
                        if (orderSnapshot.exists()) {
                            for (userKeySnapshot in orderSnapshot.children) {
                                for (orderChildSnapshot in userKeySnapshot.children) {
                                    val order = orderChildSnapshot.getValue(Order::class.java)
                                    if (order != null && order.orderID == currentItem.orderID) {
                                        // update its status to Delivered
                                        orderChildSnapshot.child("status").ref.setValue(DeliveryStatus.CANCELLED.name)
                                    }
                                }
                            }
                        } else {
                            Log.d("AdminHomeFragmentLog", "Orders not found")
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("AdminHomeFragmentLog", "Error fetching Orders: ${error.message}")
                    }
                })

                holder.status.text = DeliveryStatus.CANCELLED.name
                holder.changeMyStatus.visibility = View.GONE
            } else {
                // Checkbox is unchecked
            }
        }

        if (activityIdentifier == "AdminHomeFragment" && currentItem.status.name == DeliveryStatus.PENDING.name){
            holder.changeStatus.visibility = View.VISIBLE
        }

        holder.changeStatus.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Constants.DATABASE_REFERENCE_Orders.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(orderSnapshot: DataSnapshot) {
                        if (orderSnapshot.exists()) {
                            for (userKeySnapshot in orderSnapshot.children) {
                                for (orderChildSnapshot in userKeySnapshot.children) {
                                    val order = orderChildSnapshot.getValue(Order::class.java)
                                    if (order != null && order.orderID == currentItem.orderID) {
                                        // update its status to Delivered
                                        orderChildSnapshot.child("status").ref.setValue(DeliveryStatus.DELIVERED.name)
                                    }
                                }
                            }
                        } else {
                            Log.d("AdminHomeFragmentLog", "Orders not found")
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("AdminHomeFragmentLog", "Error fetching Orders: ${error.message}")
                    }
                })

                holder.status.text = DeliveryStatus.DELIVERED.name
                holder.changeStatus.visibility = View.GONE
            } else {
                // Checkbox is unchecked
            }
        }

    }

    override fun getItemCount(): Int {
        return filteredList.size
    }


    fun filterByStatus(status: String) {
        if (status == "All"){
            filteredList = orderList
        } else{
            filteredList = orderList.filter { it.status.name == status }
        }

        notifyDataSetChanged()
    }

    private fun detailsPopup(order: Order){
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val customerView = inflater.inflate(R.layout.order_detail_item_view, null)
        dialogBuilder.setView(customerView)

        val id = customerView.findViewById<TextView>(R.id.orderIdTV)
        val name = customerView.findViewById<TextView>(R.id.orderReceiverNameTV)
        val phone = customerView.findViewById<TextView>(R.id.orderReceiverPhoneTV)
        val completeAddress = customerView.findViewById<TextView>(R.id.receiverCompleteAddressTV)
        val cityAddress = customerView.findViewById<TextView>(R.id.receiverCityTV)
        val itemsTotal = customerView.findViewById<TextView>(R.id.orderItemsTotalTV)
        val discountTotal = customerView.findViewById<TextView>(R.id.orderDiscountTV)
        val deliveryFeeTotal = customerView.findViewById<TextView>(R.id.orderDeliveryFeeTV)
        val grandTotal = customerView.findViewById<TextView>(R.id.orderGrandTotalTV)
        val date = customerView.findViewById<TextView>(R.id.orderDateTV)
        val status = customerView.findViewById<TextView>(R.id.orderDeliveryStatusTV)
        val cartLinearLayout = customerView.findViewById<LinearLayout>(R.id.cartLinearLayout)

        id.text = "Order ID: ${order.orderID}"
        name.text = "Receiver Name: ${order.receiver.receiverName}"
        phone.text = "Receiver Phone No: ${order.receiver.receiverPhoneNo}"
        completeAddress.text = "Complete Address: ${order.deliveryLocation.address.general}"
        cityAddress.text = "City: ${order.deliveryLocation.address.city}"
        itemsTotal.text = "Items Total: ${order.amount.itemTotal}$"
        discountTotal.text = "Discount Total: ${order.amount.discount}$"
        deliveryFeeTotal.text = "Delivery Fee: ${order.amount.deliverFee}$"
        grandTotal.text = "Grand Total: ${order.amount.grandTotal}$"
        date.text = "Order Placed On: ${Globals.convertMillisToDateTime(order.timeStamp, "HH:mm dd/MM/yyyy")}"
        status.text = "Order Status: ${order.status}"

        for (item in order.cart) {
            val textView = TextView(context)
            textView.text = "Item ID: ${item.itemID}\nQuantity: ${item.quantity}\n\n"
            cartLinearLayout.addView(textView)
        }

        dialogBuilder.setTitle("Order Details")
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
}