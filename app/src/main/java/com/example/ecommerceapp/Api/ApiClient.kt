package com.example.ecommerceapp.Api

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/create-payment-intent")
    fun createPaymentIntent(@Query("amount") amount: Long): Call<PaymentIntentResponse>
}

data class PaymentIntentResponse(val clientSecret: String)

object ApiClient {
    private const val BASE_URL = "https://brainy-dull-scowl.glitch.me/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}