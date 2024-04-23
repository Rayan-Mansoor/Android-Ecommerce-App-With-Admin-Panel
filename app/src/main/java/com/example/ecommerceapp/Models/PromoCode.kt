package com.example.ecommerceapp.Models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class PromoCode(
    @PrimaryKey val code: String,
    val discountPercent: Int,
    val expiryDate: String,
    val applicableProducts : List<String>
) : Serializable {
    @Ignore
    constructor() : this("", 0,"", emptyList() )
}
