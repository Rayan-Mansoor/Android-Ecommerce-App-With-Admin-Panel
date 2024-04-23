package com.example.ecommerceapp.Models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Product(
    @PrimaryKey val productID: String,
    val productName: String,
    val productDescription: String,
    val productCategory: Categories,
    val productPrice: Double,
    var productStock : Int,
    val productRating : Float,
    val productContainsImages : Boolean,
    val productHasReviews : Boolean,
) : Serializable {
    @Ignore
    constructor() : this("", "","",Categories.APPAREL,0.0, 0, 0F, false, false)
}


enum class Categories{
    APPAREL,
    FOOTWEAR,
    EYEWEAR,
    WATCHES,
    JEWELLERY,
    BAGS
}