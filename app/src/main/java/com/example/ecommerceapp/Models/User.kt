package com.example.ecommerceapp.Models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

interface User : Serializable

@Entity
data class RegisteredUser(
    @PrimaryKey val userID: String,
    val userName: String,
    val userEmail: String,
    val userPassword : String,
    val userPhoneNo : String?,
    val userAge : Int?,
    val userGender: Gender?,
) : User {
    @Ignore
    constructor() : this("","","","",null, null, null)
}

@Entity
data class AdminUser(
    @PrimaryKey val adminID : String,
    val adminEmail : String,
    val adminPassword : String
) : User {
    @Ignore
    constructor() : this("", "", "")
}

@Entity
data class GuestUser(
    @PrimaryKey val guestID : String
) : User {
    @Ignore
    constructor() : this("")
}

enum class Gender {
    MALE,
    FEMALE
}
