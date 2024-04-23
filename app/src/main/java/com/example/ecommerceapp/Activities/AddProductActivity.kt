package com.example.ecommerceapp.Activities

import android.R
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.denzcoskun.imageslider.models.SlideModel
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.Helpers.ItemMoveListener
import com.example.ecommerceapp.Helpers.ItemTouchHelperCallback
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.Adapters.SliderImagesAdapter
import com.example.ecommerceapp.Helpers.ImageStorageManager
import com.example.ecommerceapp.Models.Categories
import com.example.ecommerceapp.databinding.ActivityAddProductBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.UUID

class AddProductActivity : AppCompatActivity() , ItemMoveListener {
    private lateinit var binding: ActivityAddProductBinding
    private var sliderProductImageList : ArrayList<SlideModel> = ArrayList()
    private var productImageList : ArrayList<String> = ArrayList()
    private lateinit var sliderImageListAdapter : SliderImagesAdapter
    private lateinit var pickMedia: ActivityResultLauncher<Intent>
    private lateinit var imageStorageManager : ImageStorageManager

    private var productToUpdate : Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageStorageManager = ImageStorageManager(applicationContext)

        productToUpdate = intent.getSerializableExtra("Update_Product") as Product?

        if (productToUpdate != null){
            binding.saveProdBtn.text = "Update Product"
            binding.itmNameEt.setText(productToUpdate!!.productName)
            binding.itmDescEt.setText(productToUpdate!!.productDescription)

            val categoryAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, Globals.CategoryList)
            categoryAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

            binding.itmCategorySpinner.adapter = categoryAdapter

            categoryAdapter.notifyDataSetChanged()

            binding.itmCategorySpinner.post {
                // Use post to ensure that the code is executed after the view is created
                binding.itmCategorySpinner.setSelection(Globals.CategoryList.indexOf(productToUpdate!!.productCategory.name))
            }

            binding.itmCategorySpinner.isEnabled = false


            binding.itmPriceEt.setText(productToUpdate!!.productPrice.toString())
            binding.itmStockEt.setText(productToUpdate!!.productStock.toString())

            Constants.DATABASE_REFERENCE_IMAGES.child("Products").child(productToUpdate!!.productID).addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    Globals.fillSliderArrayListWithDummyData(sliderProductImageList, snapshot.children.count())
                    Globals.fillStringArrayListWithDummyData(productImageList, snapshot.children.count())

                    for (childSnapshot in snapshot.children) {
                        val imagePath = childSnapshot.value.toString()
                        val imageIndex = childSnapshot.key.toString()

                        Globals.downloadImageAndGetUri(imageIndex, imagePath, imageStorageManager,
                            onSuccess = { index, uri ->

                                productImageList.set(index.toInt(), uri.toString())
                                sliderProductImageList.set(index.toInt(), SlideModel(uri.toString()))
                                Globals.initializeSlider(binding.addProdImgSlider, sliderProductImageList)
                                sliderImageListAdapter.notifyDataSetChanged()

                            },
                            onFailure = { exception ->
                                // Handle errors
                                println("Error downloading image: $exception")
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error: $error")
                }
            })
        }

        val categoryAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, Globals.CategoryList)
        categoryAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        binding.itmCategorySpinner.adapter = categoryAdapter

        sliderImageListAdapter = SliderImagesAdapter(this, productToUpdate?.productID, productImageList, sliderProductImageList, this)

        binding.prodImgRCV.adapter = sliderImageListAdapter
        binding.prodImgRCV.layoutManager = GridLayoutManager(this, 3)

        val callback = ItemTouchHelperCallback(sliderImageListAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.prodImgRCV)

        pickMedia = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data!!.data

                sliderProductImageList.add(SlideModel(selectedImageUri.toString()))
                productImageList.add(selectedImageUri.toString())
                sliderImageListAdapter.notifyDataSetChanged()
                Globals.initializeSlider(binding.addProdImgSlider, sliderProductImageList)
            }
        }

        binding.selectItemBtn.setOnClickListener {
            Globals.selectImageFromGallery(pickMedia)
        }

        binding.saveProdBtn.setOnClickListener {

            val generatedProductID = UUID.randomUUID().toString()

            val itmName = binding.itmNameEt.text.toString()
            val itmDesc = binding.itmDescEt.text.toString()
            val itmCat = binding.itmCategorySpinner.selectedItem.toString()
            val itmPrice = binding.itmPriceEt.text.toString().toDouble()
            val itmStock = binding.itmStockEt.text.toString().toInt()

            var hasImages = false
            val hasReviews = false

            var pID : String

            if (productToUpdate != null) {
                pID = productToUpdate!!.productID
            }
            else {
                pID = generatedProductID
            }

            Constants.DATABASE_REFERENCE_IMAGES.child("Products").child(pID).removeValue()

            for ((index,image) in productImageList.withIndex()) {

                val imagePath = "images/products/${pID}/${
                    Globals.calculateHash(
                        Globals.getFileFromUri(
                            this,
                            Uri.parse(image)
                        )!!
                    )
                }.jpg"

                Constants.CLOUD_STORAGE_REFERENCE.child(imagePath).putFile(Uri.parse(image))

                Constants.DATABASE_REFERENCE_IMAGES.child("Products").child(pID).child(index.toString()).setValue(imagePath)

                hasImages = true
            }

            if (productToUpdate != null){
                Constants.DATABASE_REFERENCE_PRODUCTS.child(productToUpdate!!.productID).child("productName").setValue(itmName)
                Constants.DATABASE_REFERENCE_PRODUCTS.child(productToUpdate!!.productID).child("productDescription").setValue(itmDesc)
                Constants.DATABASE_REFERENCE_PRODUCTS.child(productToUpdate!!.productID).child("productPrice").setValue(itmPrice)
                Constants.DATABASE_REFERENCE_PRODUCTS.child(productToUpdate!!.productID).child("productStock").setValue(itmStock)

                finish()

                return@setOnClickListener
            }

            val createdProduct = Product(generatedProductID, itmName, itmDesc, Categories.valueOf(itmCat), itmPrice, itmStock, 0F, hasImages, hasReviews)
            Constants.DATABASE_REFERENCE_PRODUCTS.child(generatedProductID).setValue(createdProduct)

            finish()
        }
    }


    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Globals.initializeSlider(binding.addProdImgSlider, sliderProductImageList)
    }
}