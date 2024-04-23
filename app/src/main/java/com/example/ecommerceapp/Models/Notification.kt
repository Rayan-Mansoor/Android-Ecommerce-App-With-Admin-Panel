package com.example.ecommerceapp.Models

data class Notification(
    val title: NotificationType,
    val content : String,
    val timeStamp : Long
) {
    constructor() : this(NotificationType.PROMO_CODE, "", 0)
}

enum class NotificationType {
    PROMO_CODE,
    WARNING
}