package com.example.ecommerceapp.Activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.denzcoskun.imageslider.models.SlideModel
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.Utils.Globals.getFileFromUri
import com.example.ecommerceapp.Helpers.ItemMoveListener
import com.example.ecommerceapp.Helpers.ItemTouchHelperCallback
import com.example.ecommerceapp.Adapters.SliderImagesAdapter
import com.example.ecommerceapp.Helpers.ImageStorageManager
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals.downloadImageAndGetUri
import com.example.ecommerceapp.Utils.Globals.fillSliderArrayListWithDummyData
import com.example.ecommerceapp.Utils.Globals.fillStringArrayListWithDummyData
import com.example.ecommerceapp.databinding.ActivityEditSliderBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class EditSliderActivity : AppCompatActivity() , ItemMoveListener {
    private lateinit var binding : ActivityEditSliderBinding
    private lateinit var sliderImageListAdapter: SliderImagesAdapter
    private var imageList : ArrayList<SlideModel> = ArrayList()
    private var sliderImageList : ArrayList<String> = ArrayList()
    private lateinit var pickMedia: ActivityResultLauncher<Intent>
    private lateinit var imageStorageManager : ImageStorageManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditSliderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageStorageManager = ImageStorageManager(applicationContext)

        sliderImageListAdapter = SliderImagesAdapter(this, null, sliderImageList, imageList, this)

        binding.rcvSliderImgs.adapter = sliderImageListAdapter
        binding.rcvSliderImgs.layoutManager = GridLayoutManager(this, 3)

        val callback = ItemTouchHelperCallback(sliderImageListAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.rcvSliderImgs)

        Globals.initializeSlider(binding.imageSlider, imageList)

        pickMedia = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedImageUri = result.data!!.data

                sliderImageList.add(selectedImageUri.toString())
                imageList.add(SlideModel(selectedImageUri.toString()))
                sliderImageListAdapter.notifyDataSetChanged()
                Globals.initializeSlider(binding.imageSlider, imageList)
            }
        }

        binding.addSliderImageBtn.setOnClickListener {
            Globals.selectImageFromGallery(pickMedia)
        }

        Constants.DATABASE_REFERENCE_IMAGES.child("Slider").addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                fillSliderArrayListWithDummyData(imageList, snapshot.children.count())
                fillStringArrayListWithDummyData(sliderImageList, snapshot.children.count())

                for (childSnapshot in snapshot.children) {
                    val imagePath = childSnapshot.value.toString()
                    val imageIndex = childSnapshot.key.toString()

                    downloadImageAndGetUri(imageIndex, imagePath, imageStorageManager,
                        onSuccess = { index, uri->

                            sliderImageList.set(index.toInt(), uri.toString())
                            imageList.set(index.toInt(), SlideModel(uri.toString()))
                            Globals.initializeSlider(binding.imageSlider, imageList)
                            sliderImageListAdapter.notifyDataSetChanged()

                        },
                        onFailure = { exception ->
                            // Handle errors
                            println("Error downloading image: $exception")
                        })
                }


            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                println("Error: $error")
            }
        })

        binding.UploadSlider.setOnClickListener {
            Constants.DATABASE_REFERENCE_IMAGES.child("Slider").removeValue()
            for ((index,imageName) in sliderImageList.withIndex()) {
                Log.d("EditSliderActivityLog", imageName)
                Log.d("EditSliderActivityLog", Uri.parse(imageName).lastPathSegment!!)

                val imagePath = "images/slider/${
                    Globals.calculateHash(
                        getFileFromUri(
                            this,
                            Uri.parse(imageName)
                        )!!
                    )
                }.jpg" // Replace with your actual path

                // Get a reference to the image
                val imageRef = Constants.CLOUD_STORAGE_REFERENCE.child(imagePath)

                imageRef.putFile(Uri.parse(imageName))

                // Store the imagePath in the database
                Constants.DATABASE_REFERENCE_IMAGES.child("Slider").child(index.toString()).setValue(imagePath)
            }
            Toast.makeText(this,"Slider Updated Successfully", Toast.LENGTH_SHORT).show()
            finish()

        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        binding.imageSlider.setImageList(imageList)
    }

}