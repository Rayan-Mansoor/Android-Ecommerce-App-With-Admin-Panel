package com.example.ecommerceapp.Activities

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.ecommerceapp.Models.PromoCode
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.databinding.ActivityGeneratePromoCodeBinding

class GeneratePromoCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGeneratePromoCodeBinding
    private lateinit var selectedCategories : List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGeneratePromoCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.selectCategoryDropdown.setOnClickListener {
            showCategoryDialog { selectedCat ->
                selectedCategories = selectedCat
            }

        }

        binding.generatePCBtn.setOnClickListener {
            val discount = binding.discountET.text.toString().toInt()

            val year = binding.expiryDatePicker.year
            val month = binding.expiryDatePicker.month + 1
            val day = binding.expiryDatePicker.dayOfMonth

            // Create a string representation in ISO 8601 format (YYYY-MM-DD)
            val expiryDateString = String.format("%04d-%02d-%02d", year, month, day)

            val generatedCode = generatePromoCode()

            val promocode = PromoCode(generatedCode, discount, expiryDateString, selectedCategories)
            Constants.DATABASE_REFERENCE_PROMO_CODE.push().setValue(promocode)

            Toast.makeText(this,"Promo Code Generated Successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showCategoryDialog(onCategorySelected: (List<String>) -> Unit) {
        Log.d("ManagePromoCodesActivityLog", "showCategoryDialog called")
//        val categoryList = arrayOf("Cars", "Furniture", "Smartphones")
        val checkedItems = BooleanArray(Globals.CategoryList.size) { false }
        val selectedCategories = mutableListOf<String>()

        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle("Select Categories")
        alertDialog.setCancelable(false)
        alertDialog.setMultiChoiceItems(Globals.CategoryList, checkedItems) { _: DialogInterface, which: Int, isChecked: Boolean ->
            checkedItems[which] = isChecked
        }
            .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                for (i in Globals.CategoryList.indices) {
                    if (checkedItems[i]) {
                        selectedCategories.add(Globals.CategoryList[i])
                    }
                }
                onCategorySelected.invoke(selectedCategories)
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }
            .setNeutralButton("Clear All") { _: DialogInterface, _: Int ->
                for (i in checkedItems.indices) {
                    checkedItems[i] = false
                }

            }.show()
    }


    fun generatePromoCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..5)
            .map { chars.random() }
            .joinToString("")
    }
}