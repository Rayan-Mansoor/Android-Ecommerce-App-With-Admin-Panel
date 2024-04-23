package com.example.ecommerceapp.Activities

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.denzcoskun.imageslider.models.SlideModel
import com.example.ecommerceapp.Adapters.ReviewsAdapter
import com.example.ecommerceapp.Adapters.SliderImagesAdapter
import com.example.ecommerceapp.Helpers.ImageStorageManager
import com.example.ecommerceapp.Models.GuestUser
import com.example.ecommerceapp.Models.Items
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.Models.Review
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.Utils.Globals.downloadImageAndGetUri
import com.example.ecommerceapp.Utils.Globals.dpToPx
import com.example.ecommerceapp.Utils.Globals.fillSliderArrayListWithDummyData
import com.example.ecommerceapp.Utils.Globals.fillStringArrayListWithDummyData
import com.example.ecommerceapp.databinding.ActivityProductDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityProductDetailBinding
    private lateinit var reviewsAdapter: ReviewsAdapter
    private var productSliderImageList : ArrayList<SlideModel> = ArrayList()
    private lateinit var reviewList : ArrayList<Review>
    private lateinit var imageStorageManager : ImageStorageManager
    private lateinit var receivedProduct : Product
    private var productInCart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("ProductDetailActivityLog", "onCreate")


        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receivedProduct = intent.getSerializableExtra("Current_Product") as Product
        productInCart = intent.getBooleanExtra("Is_In_Cart", false)

        imageStorageManager = ImageStorageManager(applicationContext)

        initializeSlider()
        reviewList = ArrayList()
        reviewsAdapter = ReviewsAdapter(reviewList)


        binding.productName.text = receivedProduct.productName
        binding.productDescription.text = receivedProduct.productDescription
        binding.productPrice.text = "${receivedProduct.productPrice}$"

        if (Globals.CURRENT_USER is GuestUser){
            binding.addReviewBtn.isEnabled = false
            binding.addReviewBtn.alpha = 0.5f
        }

        binding.addReviewBtn.setOnClickListener {
            newReviewPopup(receivedProduct.productID)
        }

        if (productInCart){
            binding.addToCartBtn.isEnabled = false
            binding.addToCartBtn.alpha = 0.5f
        }

        binding.addToCartBtn.setOnClickListener {
            val newItem = Items(receivedProduct.productID, receivedProduct.productCategory, 1)
            Constants.DATABASE_REFERENCE_CARTS.child(Globals.CURRENT_USER_ID!!).push().setValue(newItem)
            Toast.makeText(this,"Product Added To Cart", Toast.LENGTH_SHORT).show()
            binding.addToCartBtn.isEnabled = false
            binding.addToCartBtn.alpha = 0.5f
        }

        if (receivedProduct.productHasReviews) {

            Constants.DATABASE_REFERENCE_Reviews.child(receivedProduct.productID).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    reviewList.clear()
                    for (childSnapshot in snapshot.children) {
                        val dataRetrieved = childSnapshot.getValue(Review::class.java)
                        if (dataRetrieved != null) {

                            Log.d("ProductDetailActivity", dataRetrieved.toString())
                            reviewList.add(dataRetrieved)
                        }
                    }
                    reviewsAdapter.notifyDataSetChanged()

                    val avgRating = calculateAverageRating(reviewList)
                    binding.ratingBar.rating = avgRating

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO()
                }
            })
        }

        if (receivedProduct.productContainsImages){

            Constants.DATABASE_REFERENCE_IMAGES.child("Products").child(receivedProduct.productID).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    fillSliderArrayListWithDummyData(productSliderImageList, snapshot.children.count())

                    for (childSnapshot in snapshot.children) {
                        val imagePath = childSnapshot.value.toString()
                        val imageIndex = childSnapshot.key.toString()


                        downloadImageAndGetUri(imageIndex, imagePath, imageStorageManager,
                            onSuccess = { index, uri ->

                                productSliderImageList.set(index.toInt(), SlideModel(uri.toString()))
                                initializeSlider()

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

//        val colorList = listOf("RED", "BLUE", "GREEN", "BLACK")
        val colorList = listOf("#FF7675", "#74B9FF", "#55efc4", "#2d3436")
        for (colorName in colorList) {
            Log.d("MainActivityCategory", colorName)

            val colorImageButton = ImageButton(this)
            colorImageButton.layoutParams = LinearLayout.LayoutParams(
                resources.dpToPx(40), // Adjust the size as needed
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            colorImageButton.setPadding(resources.dpToPx(10), 0, resources.dpToPx(10), 0)
            colorImageButton.setBackgroundResource(android.R.color.transparent)
            colorImageButton.scaleType = ImageView.ScaleType.FIT_CENTER

            // Set color based on the color name
//            val color = getColorByName(colorName)
//            val coloredCircleDrawable = createColoredCircleDrawable(colorName)
            val color = Color.parseColor(colorName)
            val coloredCircleDrawable = createColoredCircleDrawable(color)
            colorImageButton.setImageDrawable(coloredCircleDrawable)

            colorImageButton.setOnClickListener {
                // Handle the click event
                Log.d("MainActivityCategory", "Clicked on $colorName")
            }

            binding.colorsLinearLayout.addView(colorImageButton)
        }


        binding.rcvReviewsList.adapter = reviewsAdapter
        binding.rcvReviewsList.layoutManager = LinearLayoutManager(this)

    }

    private fun initializeSlider(){
        binding.productImageSlider.setImageList(productSliderImageList)

    }

    private fun newReviewPopup(productID :String){
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.review_dialog, null)
        dialogBuilder.setView(dialogView)

        val newComment = dialogView.findViewById<EditText>(R.id.comment_dialogBox)
        val newrating = dialogView.findViewById<RatingBar>(R.id.rating_dialogBox)

        val currentUser = Globals.CURRENT_USER
        var username = ""
        if (currentUser is RegisteredUser){
            username = currentUser.userName
        }

        dialogBuilder.setPositiveButton("ok"){ _,_ ->
            val review = Review(productID, Globals.CURRENT_USER_ID!!, username, newComment.text.toString(), newrating.rating)

            reviewList.add(review)
            reviewsAdapter.notifyDataSetChanged()

            Constants.DATABASE_REFERENCE_Reviews.child(receivedProduct.productID).child(Globals.CURRENT_USER_ID!!).setValue(review)
            Constants.DATABASE_REFERENCE_PRODUCTS.child(receivedProduct.productID).child("productHasReviews").setValue(true)

        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()

    }

    private fun calculateAverageRating(reviews: ArrayList<Review>): Float {
        if (reviews.isEmpty()) {
            return 0f // Return 0 if the list is empty to avoid division by zero
        }

        var totalRating = 0f

        for (review in reviews) {
            totalRating += review.rating
        }

        return totalRating / reviews.size
    }

    fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    // Function to get color based on color name
//    fun getColorByName(colorName: String): Int {
//        return when (colorName.toUpperCase()) {
//            "RED" -> Color.RED
//            "BLUE" -> Color.BLUE
//            "GREEN" -> Color.GREEN
//            "BLACK" -> Color.BLACK
//            else -> Color.BLACK // Default color or handle additional colors
//        }
//    }
//    fun getColorByName(colorName: String): Int {
//        val sanitizedColorName = colorName.toUpperCase().trim()
//
//        val colorIndex = when (sanitizedColorName) {
//            "RED" -> 0
//            "BLUE" -> 1
//            "GREEN" -> 2
//            "BLACK" -> 3
//            else -> -1
//        }
//
//        return if (colorIndex != -1) {
//            Color.parseColor(colorList[colorIndex])
//        } else {
//            Color.parseColor("#2d3436") // Default color or handle additional colors
//        }
//    }


    // Function to create colored circle drawable
    fun createColoredCircleDrawable(color: Int): Drawable {
        val shape = GradientDrawable()
        shape.shape = GradientDrawable.OVAL
        shape.setSize(50.dpToPx(), 50.dpToPx()) // Convert dp to pixels
        shape.setColor(color)
        shape.setStroke(4.dpToPx(), Color.BLACK)
        return shape
    }

}