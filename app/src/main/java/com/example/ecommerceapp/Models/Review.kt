package com.example.ecommerceapp.Models

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(foreignKeys = [ForeignKey(entity = Product::class, parentColumns = ["productID"], childColumns = ["pid"]),
    ForeignKey(entity = RegisteredUser::class, parentColumns = ["userID"], childColumns = ["uid"])],
    primaryKeys = ["pid","uid"])
data class Review(
    val pid: String,
    val uid: String,
    val uname: String,
    val comment: String,
    val rating: Float,
) {
    constructor() : this("", "","","", 0F)
}
