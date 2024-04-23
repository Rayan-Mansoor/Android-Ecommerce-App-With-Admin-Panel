package com.example.ecommerceapp.Activities

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.ecommerceapp.Api.ApiClient
import com.example.ecommerceapp.Api.PaymentIntentResponse
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.ActivityPaymentBinding
import com.google.android.material.button.MaterialButton
import com.stripe.android.PaymentConfiguration
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.payments.paymentlauncher.PaymentLauncher
import com.stripe.android.payments.paymentlauncher.PaymentResult
import com.stripe.android.view.CardInputWidget
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding : ActivityPaymentBinding
    private var paymentIntentClientSecret: String? = null
    private lateinit var paymentLauncher: PaymentLauncher
    private lateinit var buyerName : String
    private lateinit var buyerAmount : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buyerName = intent.getStringExtra("user_name")!!
        buyerAmount = intent.getStringExtra("payment_amount")!!

        val paymentConfiguration = PaymentConfiguration.getInstance(applicationContext)
        paymentLauncher = PaymentLauncher.Companion.create(
            this,
            paymentConfiguration.publishableKey,
            paymentConfiguration.stripeAccountId,
            ::onPaymentResult
        )
        startCheckout()

    }

    private fun startCheckout() {
        ApiClient.apiService.createPaymentIntent(extractNumericValue(buyerAmount)!!*100).enqueue(object :
            Callback<PaymentIntentResponse> {
            override fun onResponse(call: Call<PaymentIntentResponse>, response: Response<PaymentIntentResponse>) {
                if (response.isSuccessful) {
                    val clientSecret = response.body()?.clientSecret
                    clientSecret?.let {
                        paymentIntentClientSecret = it
                        enablePaymentButton()
                    }
                } else {
                    Log.e("MainActivityLog", "Failed to create Payment Intent: ${response.code()}")
                    // Handle unsuccessful response, e.g., show an error message
                }
            }

            override fun onFailure(call: Call<PaymentIntentResponse>, t: Throwable) {
                Log.e("MainActivityLog", "Failed to create Payment Intent: ${t.message}")
                Toast.makeText(this@PaymentActivity, "Couldn't Connect to Server", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun enablePaymentButton() {

        binding.payBtn.setOnClickListener {
            Log.e("MainActivityLog", "pay button clicked")
            binding.cardDetailsET.paymentMethodCreateParams?.let { params ->
                paymentIntentClientSecret?.let {
                    val confirmParams = ConfirmPaymentIntentParams
                        .createWithPaymentMethodCreateParams(params, it)

                    Log.e("MainActivityLog", "${confirmParams.toString()}")

                    lifecycleScope.launch {
                        paymentLauncher.confirm(confirmParams)
                    }
                }
            }
        }
    }

    private fun onPaymentResult(paymentResult: PaymentResult) {
        val message = when (paymentResult) {
            is PaymentResult.Completed -> "Completed!"
            is PaymentResult.Canceled -> "Canceled!"
            is PaymentResult.Failed -> "Failed: ${paymentResult.throwable.message}"
        }

        Log.d("MainActivityLog", "Payment Result: $message")

        runOnUiThread {

            Toast.makeText(this, "Payment Completed Successfully", Toast.LENGTH_SHORT).show()
            paymentDetailsPopup()
        }
    }

    private fun paymentDetailsPopup(){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val paymentView = inflater.inflate(R.layout.payment_receipt_dialog, null)
        dialogBuilder.setView(paymentView)

        val by = paymentView.findViewById<TextView>(R.id.paymentNameTV)
        val amount = paymentView.findViewById<TextView>(R.id.paymentAmountTV)
        val date = paymentView.findViewById<TextView>(R.id.paymentDateTV)


        by.text = "By: ${buyerName}"
        amount.text = "Amount:  $buyerAmount"
        date.text = "Date: ${Globals.convertMillisToDateTime(System.currentTimeMillis(), "HH:mm dd/MM/yyyy")}"


        dialogBuilder
            .setView(paymentView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

    }

    fun extractNumericValue(input: String): Long? {
        val regex = """([0-9]+(\.[0-9]+)?)""".toRegex()
        val matchResult = regex.find(input)

        return matchResult?.value?.toLongOrNull()
    }
}