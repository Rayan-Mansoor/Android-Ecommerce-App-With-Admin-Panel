package com.example.ecommerceapp.Activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.Adapters.CheckoutAdapter
import com.example.ecommerceapp.Models.Address
import com.example.ecommerceapp.Models.Amount
import com.example.ecommerceapp.Models.DeliveryStatus
import com.example.ecommerceapp.Models.GuestUser
import com.example.ecommerceapp.Models.Items
import com.example.ecommerceapp.Models.Location
import com.example.ecommerceapp.Models.Order
import com.example.ecommerceapp.Models.PaymentMethod
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.Models.PromoCode
import com.example.ecommerceapp.Models.Receiver
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.FirebaseDatabaseManager
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.ActivityCheckoutBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.properties.Delegates

private const val MAPS_PERMISSION_REQUEST = 100

class CheckoutActivity : AppCompatActivity(){
    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var checkoutCartAdapter: CheckoutAdapter
    private  var checkoutCartList : ArrayList<Product> = ArrayList()
    private  var checkoutItemList : ArrayList<Items> = ArrayList()
    private lateinit var receiver : Receiver
    private lateinit var location: Location
    private lateinit var cart : ArrayList<Items>
    private lateinit var amount: Amount
    private lateinit var deliveryType : PaymentMethod
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude : Double = 0.0
    private var longitude : Double = 0.0

    private var counter: Int by Delegates.observable(0) { _, _, newValue ->
        if (newValue == 2) {
            confirmTheOrder()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemsGrandTotal = intent.getIntExtra("ItemsTotal", 0)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.itemsAmount.text = "${itemsGrandTotal.toString()}$"
        binding.totalAmount.text = "${(itemsGrandTotal + 200).toString()}$"

        binding.deliveryAmount.text = "200$"

        cart = ArrayList()

        amount = Amount(itemsGrandTotal.toDouble(), 0.0,200.0, itemsGrandTotal + 200.0)

        Log.d("CheckoutActivityLog", Globals.CURRENT_USER_ID!!)

        Constants.DATABASE_REFERENCE_CARTS.child(Globals.CURRENT_USER_ID!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val itemIDs = mutableListOf<String>()

                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(Items::class.java)
                        if (item != null) {
                            checkoutItemList.add(item)
                            itemIDs.add(item.itemID)
                        }
                    }

                    if (itemIDs.isNotEmpty()) {
                        Constants.DATABASE_REFERENCE_PRODUCTS.orderByChild("productID").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(productSnapshot: DataSnapshot) {
                                if (productSnapshot.exists()) {
                                    for (productChildSnapshot in productSnapshot.children) {
                                        val product = productChildSnapshot.getValue(Product::class.java)
                                        if (product != null && itemIDs.contains(product.productID)) {
                                            Log.d("CartActivityLog", product.toString())
                                            checkoutCartList.add(product)
                                        }
                                    }
                                    checkoutCartAdapter.notifyDataSetChanged()
                                } else {
                                    Log.d("CartActivityLog", "Products not found")
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                                Log.e("CartActivityLog", "Error fetching products: ${error.message}")
                            }
                        })
                    } else {
                        Log.d("CartActivityLog", "No itemIDs found")
                    }
                } else {
                    println("No data found at the specified location")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if any
                println("Error retrieving data: ${error.message}")
            }
        })

        val currentUser = Globals.CURRENT_USER
        if (currentUser is GuestUser){
            Log.d("CheckoutActivityLog", currentUser.toString())

            binding.receiverNameET.setText(Globals.CURRENT_USER_ID)
            counter++

        }
        else {

            val currentUser = Globals.CURRENT_USER
            if (currentUser is RegisteredUser){
                Log.d("CheckoutActivityLog", currentUser.toString())

                binding.receiverNameET.setText(currentUser.userName)
                binding.phoneET.setText(currentUser.userPhoneNo)

                counter++
            }
        }

        binding.promoCodeET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                if (p0?.length == 5){
                    Constants.DATABASE_REFERENCE_PROMO_CODE.orderByChild("code").equalTo(p0.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (childSnapshot in snapshot.children){
                                    val promoCode = childSnapshot.getValue(PromoCode::class.java)

                                    Log.d("CheckoutActivityLog", promoCode.toString())

                                    if (promoCode != null) {
                                        val formattedExpiryDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        val expiryDate = formattedExpiryDate.parse(promoCode.expiryDate)

                                        Log.d("CheckoutActivityLog", expiryDate.toString())
                                        Log.d("CheckoutActivityLog", Date().toString())



                                        if (expiryDate.after(Date())) {
                                            Log.d("CheckoutActivityLog", "Promo Code is valid")

                                            var totalDiscount = 0.0

                                            // Iterate through checkout items
                                            for (checkoutItem in checkoutItemList) {
                                                val product = checkoutCartList.find { it.productID == checkoutItem.itemID }

                                                // Check if product exists and its category is applicable
                                                if (product != null && promoCode.applicableProducts.contains(product.productCategory.name)) {
                                                    Log.d("CheckoutActivityLog", "product category is valid")

                                                    binding.PromoIsValid.setImageResource(R.drawable.baseline_check_circle_24)
                                                    binding.PromoIsValid.visibility = View.VISIBLE

                                                    val itemTotal = product.productPrice * checkoutItem.quantity
                                                    val itemDiscount = (promoCode.discountPercent / 100.0) * itemTotal

                                                    totalDiscount += itemDiscount
                                                }
                                                else {
                                                    binding.PromoIsValid.setImageResource(R.drawable.baseline_error_24)
                                                    binding.PromoIsValid.visibility = View.VISIBLE
                                                }
                                            }

                                            binding.couponAmount.text = "${totalDiscount.toString()}$"

                                            val grandTotal = itemsGrandTotal - totalDiscount + 200.0
                                            binding.totalAmount.text = "${grandTotal.toString()}$"

                                            amount = Amount(itemsGrandTotal.toDouble(), totalDiscount, 200.0, grandTotal)
                                        }

                                        else{
                                            Log.d("CheckoutActivityLog", "Promo Code is not valid")

                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
                else {
                    if (p0?.length == 0) {
                        binding.PromoIsValid.visibility = View.GONE
                    }
                    Log.d("CheckoutActivityLogg", "length not 5")
                    if (binding.couponAmount.text != "0$"){
                        Log.d("CheckoutActivityLogg", "already coupon applied")
                        val noPromoCodeAmount = addNumericValues(binding.couponAmount.text.toString(), binding.totalAmount.text.toString())
                        Log.d("CheckoutActivityLogg", noPromoCodeAmount.toString())
                        binding.couponAmount.setText("0$")
                        binding.totalAmount.setText("${noPromoCodeAmount}$")
                        binding.PromoIsValid.setImageResource(R.drawable.baseline_error_24)

                    }
                }
            }

        })

        binding.deliveryRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.COD_radioBtn -> {
                    deliveryType = PaymentMethod.CASH_ON_DELIVERY
                }
                R.id.Card_radioBtn -> {
                    deliveryType = PaymentMethod.CREDIT_DEBIT_CARD
                    val intent = Intent(this, PaymentActivity::class.java)
                    intent.putExtra("user_name", binding.receiverNameET.text.toString())
                    intent.putExtra("payment_amount", binding.totalAmount.text.toString())
                    startActivity(intent)
                }
            }
        }

        binding.openMapsIntentBtn.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), MAPS_PERMISSION_REQUEST)
            } else {
                getLastKnownLocation()
            }
        }

        binding.placeOrderBtn.setOnClickListener {
            receiver = Receiver(binding.receiverNameET.text.toString(),binding.phoneET.text.toString())
            location = Location(null, Address(binding.addressET.text.toString(),"Rawalpindi"))

            Constants.DATABASE_REFERENCE_CARTS.child(Globals.CURRENT_USER_ID!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childSnapshot in dataSnapshot.children) {
                        val item = childSnapshot.getValue(Items::class.java)
                        cart.add(item!!)
                        Log.d("CartOperation", item.toString())

                    }
                    Constants.DATABASE_REFERENCE_CARTS.child(Globals.CURRENT_USER_ID!!).removeValue()

                    counter++

                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }


        checkoutCartAdapter = CheckoutAdapter(this, checkoutCartList, checkoutItemList)

        binding.checkoutCartRCV.adapter = checkoutCartAdapter
        binding.checkoutCartRCV.layoutManager = LinearLayoutManager(this)
    }


    private fun confirmTheOrder() {
        if (!::deliveryType.isInitialized){
            Toast.makeText(this@CheckoutActivity, "Please Select Payment Method", Toast.LENGTH_SHORT).show()
            return
        }

        val orderID = UUID.randomUUID().toString()

        val order = Order(orderID, receiver, cart, location, deliveryType, amount, System.currentTimeMillis(), DeliveryStatus.PENDING)

        Constants.DATABASE_REFERENCE_Orders.child(Globals.CURRENT_USER_ID!!).child(orderID).setValue(order)

        Constants.DATABASE_REFERENCE_PRODUCTS.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(productSnapshot: DataSnapshot) {
                if (productSnapshot.exists()) {
                    for (productChildSnapshot in productSnapshot.children) {
                        var product = productChildSnapshot.getValue(Product::class.java)
                        val item = cart.find { it.itemID == product!!.productID }

                        if (product != null && item != null){

                            val newStock = product.productStock - item.quantity

                            Constants.DATABASE_REFERENCE_PRODUCTS.child(productChildSnapshot.key!!).child("productStock").setValue(newStock)

                            Constants.DATABASE_REFERENCE_CARTS.child(Globals.CURRENT_USER_ID!!).ref.removeValue()
                        }
                    }
                } else {
                    Log.d("CartActivityLog", "Products not found")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Log.e("CartActivityLog", "Error fetching products: ${error.message}")
            }
        })
        Toast.makeText(applicationContext, "Order Placed Successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, UserActivity::class.java))
        finish()
    }


    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                latitude = location.latitude
                longitude = location.longitude
                openMapIntent(latitude, longitude)
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openMapIntent(latitude: Double, longitude: Double) {
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun addNumericValues(s1: String, s2: String): Int {
        val numericValueS1 = s1.filter { it.isDigit() }.toIntOrNull() ?: 0
        val numericValueS2 = s2.filter { it.isDigit() }.toIntOrNull() ?: 0

        return numericValueS2 + numericValueS1
    }

    override fun onResume() {
        super.onResume()
        if (latitude != 0.0 && longitude != 0.0){
            binding.addressET.setText("${latitude}, ${longitude}")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MAPS_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}