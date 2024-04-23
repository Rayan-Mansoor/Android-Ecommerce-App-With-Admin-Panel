package com.example.ecommerceapp.Utils

import android.util.Log
import com.example.ecommerceapp.Models.Items
import com.example.ecommerceapp.Models.Order
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.Models.PromoCode
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.Models.Review
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

object FirebaseDatabaseManager {
    private val reviewListeners = mutableListOf<ReviewDataChangeListener>()
    private val productListeners = mutableListOf<ProductDataChangeListener>()
    private val registeredUsersListeners = mutableListOf<RegisteredUsersDataChangeListener>()
    private val wishlistListeners = mutableListOf<WishlistDataChangeListener>()
//    private val wishlistListeners = mutableMapOf<String, MutableList<WishlistDataChangeListener>>()
    private val orderListeners = mutableListOf<OrderDataChangeListener>()
    private val cartListeners = mutableListOf<CartDataChangeListener>()
    private val promoCodeListeners = mutableListOf<PromoCodeDataChangeListener>()

    val reviewData = mutableListOf<Review>()
    val productData = mutableListOf<Product>()
    val registeredUsersData = mutableListOf<RegisteredUser>()
    val wishlistData = mutableListOf<String>()
    val orderData = mutableMapOf<String, List<Order>>()
    val cartData = mutableListOf<Items>()
    val promoCodeData = mutableListOf<PromoCode>()

    private var reviewDatabaseListener: ValueEventListener? = null
    private var productsDatabaseListener: ValueEventListener? = null
    private var registeredUsersDatabaseListener: ValueEventListener? = null
    private var wishlistDatabaseListener: ValueEventListener? = null
//    private val wishlistDatabaseListener: MutableMap<String, ValueEventListener> = mutableMapOf()

    private var orderDatabaseListener: ValueEventListener? = null
    private var cartDatabaseListener: ValueEventListener? = null
    private var promoCodeDatabaseListener: ValueEventListener? = null

    private var lastCartListenerUser = ""
    private var lastWishlistListenerUser = ""


    interface ReviewDataChangeListener {
        fun onReviewDataChanged(updatedData: List<Review>)
    }

    interface ProductDataChangeListener {
        fun onProductDataChanged(updatedData: List<Product>)
    }

    interface RegisteredUsersDataChangeListener {
        fun onRegisteredUsersDataChanged(updatedData: List<RegisteredUser>)
    }

    interface WishlistDataChangeListener {
        fun onWishlistDataChanged(updatedData: List<String>)
    }

    interface OrderDataChangeListener {
        fun onOrderDataChanged(updatedData: Map<String, List<Order>>)
    }

    interface CartDataChangeListener {
        fun onCartDataChanged(updatedData: List<Items>)
    }

    interface PromoCodeDataChangeListener {
        fun onPromoCodeDataChanged(updatedData: List<PromoCode>)
    }

    fun addReviewDataChangeListener(listener: ReviewDataChangeListener, selectedProductID : String) {
        reviewListeners.add(listener)

        if (reviewListeners.size == 1) {
            startListeningForReviews(selectedProductID)
        }

        if (reviewData.isNotEmpty()) {
            listener.onReviewDataChanged(reviewData)
        }
    }

    fun addProductDataChangeListener(listener: ProductDataChangeListener) {
        productListeners.add(listener)

        if (productListeners.size == 1) {
            startListeningForProducts()
        }

        if (productData.isNotEmpty()){
            listener.onProductDataChanged(productData)
        }
    }

    fun addRegisteredUsersDataChangeListener(listener: RegisteredUsersDataChangeListener) {
        registeredUsersListeners.add(listener)

        if (registeredUsersListeners.size == 1) {
            startListeningForRegisteredUsers()
        }

        if (registeredUsersData.isNotEmpty()){
            listener.onRegisteredUsersDataChanged(registeredUsersData)
        }
    }

    fun addWishlistDataChangeListener(listener: WishlistDataChangeListener, userID : String) {
//        val listeners = wishlistListeners.getOrPut(userID) { mutableListOf() }
//        listeners.add(listener)
//
//        if (listeners.size == 1) {
//            startListeningForWishList(userID)
//        }
//
//        if (wishlistData.isNotEmpty()){
//            listener.onWishlistDataChanged(wishlistData)
//        }


        wishlistListeners.add(listener)

        if (lastWishlistListenerUser != userID) {
            startListeningForWishList(userID)
            lastWishlistListenerUser = userID
        }

        if (wishlistData.isNotEmpty()){
            listener.onWishlistDataChanged(wishlistData)
        }
    }

    fun addOrderDataChangeListener(listener: OrderDataChangeListener) {
        orderListeners.add(listener)

        if (orderListeners.size == 1) {
            startListeningForOrders()
        }

        if (orderData.isNotEmpty()){
            listener.onOrderDataChanged(orderData)
        }
    }

    fun addCartDataChangeListener(listener: CartDataChangeListener, userID : String) {
        cartListeners.add(listener)

        if (lastCartListenerUser != userID){
            Log.d("FDM", lastCartListenerUser)
            Log.d("FDM", userID)
            startListeningForCart(userID)
            lastCartListenerUser = userID
        }

        if (cartData.isNotEmpty()){
            listener.onCartDataChanged(cartData)
        }
    }

    fun addPromoCodeDataChangeListener(listener: PromoCodeDataChangeListener) {
        promoCodeListeners.add(listener)

        if (promoCodeListeners.size == 1) {
            startListeningForPromoCode()
        }

        if (promoCodeData.isNotEmpty()){
            listener.onPromoCodeDataChanged(promoCodeData)
        }
    }

    fun removeReviewDataChangeListener(listener: ReviewDataChangeListener, selectedProductID : String) {
        reviewListeners.remove(listener)

        if (reviewListeners.size > 1) {
            stopListeningForReviews(selectedProductID)
        }
    }

    fun removeProductDataChangeListener(listener: ProductDataChangeListener) {
        productListeners.remove(listener)

        if (productListeners.size > 1) {
            stopListeningForProducts()
        }
    }

    fun removeRegisteredUsersDataChangeListener(listener: RegisteredUsersDataChangeListener) {
        registeredUsersListeners.remove(listener)

        if (registeredUsersListeners.size > 1) {
            stopListeningForRegisteredUsers()
        }
    }

    fun removeWishlistDataChangeListener(listener: WishlistDataChangeListener, userID: String) {
//        if (wishlistListeners[userID]?.size ?: 0 > 1) {
//            wishlistListeners[userID]?.remove(listener)
//        }

        wishlistListeners.remove(listener)

        if (wishlistListeners.size > 1) {
            stopListeningForWishList()
        }
    }

    fun removeOrderDataChangeListener(listener: OrderDataChangeListener) {
        orderListeners.remove(listener)

        if (orderListeners.size > 1) {
            stopListeningForOrders()
        }
    }

    fun removePromoCodeDataChangeListener(listener: PromoCodeDataChangeListener) {
        promoCodeListeners.remove(listener)

        if (promoCodeListeners.size > 1) {
            stopListeningForPromoCode()
        }
    }

    private fun notifyReviewDataChanged() {
        reviewListeners.forEach { it.onReviewDataChanged(reviewData) }
    }

    private fun notifyProductDataChanged() {
        productListeners.forEach { it.onProductDataChanged(productData) }
    }

    private fun notifyRegisteredUsersDataChanged() {
        registeredUsersListeners.forEach { it.onRegisteredUsersDataChanged(registeredUsersData) }
    }

    private fun notifyWishlistDataChanged() {
        wishlistListeners.forEach { it.onWishlistDataChanged(wishlistData) }
    }

    private fun notifyOrderDataChanged() {
        orderListeners.forEach { it.onOrderDataChanged(orderData) }
    }

    private fun notifyCartDataChanged() {
        cartListeners.forEach { it.onCartDataChanged(cartData) }
    }

    private fun notifyPromoCodeDataChanged() {
        promoCodeListeners.forEach { it.onPromoCodeDataChanged(promoCodeData) }
    }

    private fun startListeningForReviews(prodID : String) {
        if (reviewDatabaseListener == null) {
            reviewDatabaseListener = Constants.DATABASE_REFERENCE_Reviews.child(prodID).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reviewData.clear()
                    for (snap in snapshot.children) {
                        val dataRetrieved = snap.getValue(Review::class.java)
                        Log.d("FirebaseDatabaseManagerLog", "New review data retrieved from firebase")
                        if (dataRetrieved != null) {
                            reviewData.add(dataRetrieved)
                        }
                    }

                    notifyReviewDataChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
        }
    }

    private fun stopListeningForReviews(prodID : String) {
        reviewDatabaseListener?.let {
            Constants.DATABASE_REFERENCE_Reviews.child(prodID).removeEventListener(it)
            reviewDatabaseListener = null
        }
    }

    private fun startListeningForProducts() {
        if (productsDatabaseListener == null) {
            productsDatabaseListener = Constants.DATABASE_REFERENCE_PRODUCTS.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    productData.clear()
                    for (snap in snapshot.children) {
                        val dataRetrieved = snap.getValue(Product::class.java)
                        Log.d("FirebaseDatabaseManagerLog", "New product data retrieved from firebase")
                        if (dataRetrieved != null) {
                            productData.add(dataRetrieved)
                        }
                    }

                    notifyProductDataChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
        }
    }

    private fun stopListeningForProducts() {
        productsDatabaseListener?.let {
            Constants.DATABASE_REFERENCE_PRODUCTS.removeEventListener(it)
            productsDatabaseListener = null
        }
    }

    private fun startListeningForRegisteredUsers() {
        if (registeredUsersDatabaseListener == null) {
            registeredUsersDatabaseListener = Constants.DATABASE_REFERENCE_USERS.child("Registered").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    registeredUsersData.clear()
                    for (snap in snapshot.children){
                        val dataRetrieved = snap.getValue(RegisteredUser::class.java)
                        Log.d("FirebaseDatabaseManagerLog", "New registered user data retrieved from firebase")
                        if (dataRetrieved != null) {
                            registeredUsersData.add(dataRetrieved)
                        }
                    }
                    notifyRegisteredUsersDataChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }


    private fun stopListeningForRegisteredUsers() {
        registeredUsersDatabaseListener?.let {
            Constants.DATABASE_REFERENCE_USERS.child("Registered").removeEventListener(it)
            registeredUsersDatabaseListener = null
        }
    }

    private fun startListeningForWishList(userID: String) {
        wishlistDatabaseListener  = Constants.DATABASE_REFERENCE_WISHLIST.child(userID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(wishlistSnapshot: DataSnapshot) {
                wishlistData.clear()
                for (itemSnapshot in wishlistSnapshot.children) {
                    val productID = itemSnapshot.value as? String
                    productID?.let {
                        wishlistData.add(it)
                    }
                }

                notifyWishlistDataChanged()

            }

            override fun onCancelled(error: DatabaseError) {}
        })

//        wishlistDatabaseListener[userID] = wishlistListener
    }


    private fun stopListeningForWishList() {
//        wishlistDatabaseListener[userID]?.let {
//            Constants.DATABASE_REFERENCE_WISHLIST.child(userID).removeEventListener(it)
//            wishlistListeners.remove(userID)
//        }

        wishlistDatabaseListener?.let {
            Constants.DATABASE_REFERENCE_WISHLIST.child(Globals.CURRENT_USER_ID!!).removeEventListener(it)
            wishlistDatabaseListener = null
        }
    }

    private fun startListeningForOrders() {
        if (orderDatabaseListener == null) {
            orderDatabaseListener = Constants.DATABASE_REFERENCE_Orders.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(orderSnapshot: DataSnapshot) {
                    if (orderSnapshot.exists()) {
                        orderData.clear()
                        for (userKeySnapshot in orderSnapshot.children) {
                            val userOrders = mutableListOf<Order>()

                            for (orderChildSnapshot in userKeySnapshot.children) {
                                val order = orderChildSnapshot.getValue(Order::class.java)
                                if (order != null) {
                                    userOrders.add(order)
                                }
                            }

                            orderData[userKeySnapshot.key!!] = userOrders
                        }

                        Log.d("OrderSpecificLog", orderData.toString())

                        notifyOrderDataChanged()

                    } else {
                        Log.d("OrderSpecificLog", orderData.toString())
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("AdminHomeFragmentLog", "Error fetching Orders: ${error.message}")
                }
            })
        }
    }


    private fun stopListeningForOrders() {
        orderDatabaseListener?.let {
            Constants.DATABASE_REFERENCE_Orders.removeEventListener(it)
            orderDatabaseListener = null
        }
    }


    private fun startListeningForCart(userID : String) {
        Log.d("FDM", "startListeningForCart called in FDM")

        cartDatabaseListener = Constants.DATABASE_REFERENCE_CARTS.child(userID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(cartSnapshot: DataSnapshot) {
                cartData.clear()
                for (cartSnap in cartSnapshot.children) {
                    val cart = cartSnap.getValue(Items::class.java)
                    if (cart != null) {
                        cartData.add(cart)
                    }
                }

                Log.d("FDM", "startListeningForCart data retrieved: ${cartData}")

                notifyCartDataChanged()

            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun startListeningForPromoCode() {
        if (promoCodeDatabaseListener == null) {
            promoCodeDatabaseListener = Constants.DATABASE_REFERENCE_PROMO_CODE.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(promoCodeSnapshot: DataSnapshot) {
                    promoCodeData.clear()
                    for (promoSnap in promoCodeSnapshot.children) {
                        val code = promoSnap.getValue(PromoCode::class.java)
                        if (code != null) {
                            promoCodeData.add(code)
                        }
                    }

                    notifyPromoCodeDataChanged()

                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }


    private fun stopListeningForPromoCode() {
        promoCodeDatabaseListener?.let {
            Constants.DATABASE_REFERENCE_PROMO_CODE.removeEventListener(it)
            promoCodeDatabaseListener = null
        }
    }
}