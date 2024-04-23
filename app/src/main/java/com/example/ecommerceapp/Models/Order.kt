package com.example.ecommerceapp.Models

import androidx.room.PrimaryKey

data class Order(
    val orderID: String,
    val receiver: Receiver,
    val cart: ArrayList<Items>,
    val deliveryLocation : Location,
    val paymentMethod: PaymentMethod,
    val amount: Amount,
    val timeStamp : Long,
    val status : DeliveryStatus
) {
    constructor() : this(
        "",
        Receiver("", ""),
        ArrayList(),
        Location(null, Address("", "")),
        PaymentMethod.CASH_ON_DELIVERY,
        Amount(0.0, 0.0, 0.0, 0.0),
        0L,
        DeliveryStatus.PENDING
    )
}

data class Receiver (
    val receiverName : String,
    val receiverPhoneNo: String
) {
    constructor() : this("", "")
}


data class Location (
    val coordinates : Coordinates?,
    val address: Address
) {
    constructor() : this(null, Address("", ""))
}


data class Items (
    val itemID : String,
    val itemCategory : Categories,
    val quantity : Int
) {
    constructor() : this("", Categories.APPAREL,1)
}

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)  {
    constructor() : this(0.0, 0.0)
}


data class Address(
    val general: String,
    val city: String
) {
    constructor() : this("", "")
}

data class Amount(
    val itemTotal : Double,
    val discount : Double,
    val deliverFee : Double,
    val grandTotal : Double
)  {
    constructor() : this(0.0, 0.0, 0.0, 0.0)
}

enum class PaymentMethod {
    CASH_ON_DELIVERY,
    CREDIT_DEBIT_CARD
}

enum class DeliveryStatus {
    PENDING,
    DELIVERED,
    CANCELLED
}
