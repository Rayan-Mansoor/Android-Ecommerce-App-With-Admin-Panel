package com.example.ecommerceapp.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ecommerceapp.Adapters.PromoCodeAdapter
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Models.PromoCode
import com.example.ecommerceapp.Utils.FirebaseDatabaseManager
import com.example.ecommerceapp.databinding.ActivityManagePromoCodesBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ManagePromoCodesActivity : AppCompatActivity(), FirebaseDatabaseManager.PromoCodeDataChangeListener {
    private lateinit var binding: ActivityManagePromoCodesBinding
    private lateinit var promoCodeAdapter: PromoCodeAdapter
    private  var promoCodeList : ArrayList<PromoCode> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManagePromoCodesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createPCBtn.setOnClickListener {
            startActivity(Intent(this, GeneratePromoCodeActivity::class.java))
        }

        promoCodeAdapter = PromoCodeAdapter(this, promoCodeList)

        FirebaseDatabaseManager.addPromoCodeDataChangeListener(this)

        binding.promoCodesRCV.adapter = promoCodeAdapter
        binding.promoCodesRCV.layoutManager = LinearLayoutManager(this)
    }

    override fun onPromoCodeDataChanged(updatedData: List<PromoCode>) {
        promoCodeList.clear()
        promoCodeList.addAll(updatedData)
        promoCodeAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        FirebaseDatabaseManager.removePromoCodeDataChangeListener(this)
    }
}