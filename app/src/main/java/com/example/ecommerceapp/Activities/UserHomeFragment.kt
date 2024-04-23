package com.example.ecommerceapp.Activities

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.denzcoskun.imageslider.models.SlideModel
import com.example.ecommerceapp.Adapters.InventoryListAdapter
import com.example.ecommerceapp.Helpers.ImageStorageManager
import com.example.ecommerceapp.Models.Categories
import com.example.ecommerceapp.Models.Items
import com.example.ecommerceapp.Models.Notification
import com.example.ecommerceapp.Models.NotificationType
import com.example.ecommerceapp.Models.Product
import com.example.ecommerceapp.R
import com.example.ecommerceapp.Utils.Constants
import com.example.ecommerceapp.Utils.FirebaseDatabaseManager
import com.example.ecommerceapp.Utils.Globals
import com.example.ecommerceapp.Utils.Globals.dpToPx
import com.example.ecommerceapp.databinding.FragmentUserHomeBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val VOICE_SEARCH_REQUEST = 100
private const val VOICE_PERMISSION_REQUEST = 200

class UserHomeFragment : Fragment(), FirebaseDatabaseManager.ProductDataChangeListener {
    private lateinit var binding: FragmentUserHomeBinding
    private lateinit var inventoryListAdapter: InventoryListAdapter
    private lateinit var imageStorageManager : ImageStorageManager
    private lateinit var progressDialog : ProgressDialog
    private  var inventoryList : ArrayList<Product> = ArrayList()
    private var sliderImageList : ArrayList<SlideModel> = ArrayList()
    private var notificationList : ArrayList<Notification> = ArrayList()
    private var selectedCategory: String? = null
    private var selectedCategoryButton: ImageButton? = null
    private var asyncTasksCounter = 0
    private var initialLaunch = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageStorageManager = ImageStorageManager(requireContext())

        Log.d("UserActivityLog", "onCreate UserHomeFragment")

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Retrieving Data From Firebase")


        Globals.retrieveNotifications { notifications ->
            notifications?.let {
                notificationList.addAll(it)

            }
            asyncTasksCounter++
            if (isAdded) {
                checkAndDismissProgressDialog()
            }
        }


        inventoryListAdapter = InventoryListAdapter(requireContext(), "UserHomeFragment", inventoryList, object : InventoryListAdapter.ManageMyProducts {
            override fun addToCart(productID: String, productCat : Categories) {
                val cartReference = Globals.CURRENT_USER_ID?.let {
                    Constants.DATABASE_REFERENCE_CARTS.child(
                        it
                    )
                }

                cartReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var productAlreadyInCart = false

                        for (childSnapshot in snapshot.children) {
                            val value = childSnapshot.value as? String

                            if (value == productID) {
                                Toast.makeText(requireContext(),"Product Already In Cart", Toast.LENGTH_SHORT).show()
                                productAlreadyInCart = true
                                break
                            }
                        }

                        if (!productAlreadyInCart) {
                            val newCartItemReference = cartReference.push()

                            val newItem = Items(productID, productCat, 1)
                            newCartItemReference.setValue(newItem)

                            Toast.makeText(requireContext(),"Product Added To Cart", Toast.LENGTH_SHORT).show()

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }

            override fun addToWishList(productID: String) {
                Constants.DATABASE_REFERENCE_WISHLIST.child(Globals.CURRENT_USER_ID!!).push().setValue(productID)
            }

            override fun deleteFromWishList(productID: String) {
                Constants.DATABASE_REFERENCE_WISHLIST.child(Globals.CURRENT_USER_ID!!).orderByValue().equalTo(productID).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (childSnapshot in dataSnapshot.children) {
                                childSnapshot.ref.removeValue()
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
            }
        })

        FirebaseDatabaseManager.addProductDataChangeListener(this)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (initialLaunch){
            progressDialog.show()
        }

        Globals.retrieveCategories { categoriesList ->
            if (isAdded) {
                for (category in categoriesList) {
                    Log.d("MainActivityCategory", category)

                    val categoryImageButton = ImageButton(requireContext())
                    categoryImageButton.layoutParams = LinearLayout.LayoutParams(
                        resources.dpToPx(120),
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    categoryImageButton.setPadding(resources.dpToPx(10), 0, resources.dpToPx(10), 0)
                    categoryImageButton.setBackgroundResource(android.R.color.transparent)
                    categoryImageButton.scaleType = ImageView.ScaleType.FIT_CENTER

                    Constants.DATABASE_REFERENCE_IMAGES.child("Categories").child(category).get().addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val imagePath = snapshot.value.toString()

                            Globals.downloadImageAndGetUri("", imagePath, imageStorageManager,
                                onSuccess = { _, uri ->
                                    Glide.with(requireContext())
                                        .load(uri)
                                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                                        .into(categoryImageButton)

                                },
                                onFailure = { exception ->
                                    Glide.with(requireContext())
                                        .load(R.drawable.baseline_error_24)
                                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                                        .into(categoryImageButton)
                                })
                        }
                    }

                    categoryImageButton.setOnClickListener {
                        toggleCategoryFilter(category, categoryImageButton)
                    }


                    binding.categoriesLinearLayout.addView(categoryImageButton)
                }
                asyncTasksCounter++
                if (isAdded) {
                    checkAndDismissProgressDialog()
                }
            }
        }

        Constants.DATABASE_REFERENCE_IMAGES.child("Slider").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                sliderImageList.clear()

                Globals.fillSliderArrayListWithDummyData(sliderImageList, snapshot.children.count())

                for (childSnapshot in snapshot.children) {
                    val imagePath = childSnapshot.value.toString()
                    val imageIndex = childSnapshot.key.toString()


                    Globals.downloadImageAndGetUri(imageIndex, imagePath, imageStorageManager,
                        onSuccess = { index, uri ->

                            sliderImageList.set(index.toInt(), SlideModel(uri.toString()))
                            Globals.initializeSlider(binding.mainSlider, sliderImageList)

                        },
                        onFailure = { exception ->
                            // Handle errors
                            println("Error downloading image: $exception")
                        })
                }
                asyncTasksCounter++
                if (isAdded) {
                    checkAndDismissProgressDialog()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                asyncTasksCounter++
                if (isAdded) {
                    checkAndDismissProgressDialog()
                }
            }
        })

        binding.inbox.setOnClickListener {
            showNotificationPopup(it)
        }

        binding.searchQuery.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                inventoryListAdapter.filterByName(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {}

        })


        binding.searchQuery.setOnSearchActionListener(object : MaterialSearchBar.OnSearchActionListener {
            override fun onSearchStateChanged(enabled: Boolean) {
                if (enabled) {
                    binding.mainSlider.visibility = View.GONE
                    binding.textView2.visibility = View.GONE
                    binding.categoriesHorizontalSV.visibility = View.GONE
                    binding.textView3.visibility = View.GONE
                    binding.searchIcon.visibility = View.GONE

                } else {
                    binding.mainSlider.visibility = View.VISIBLE
                    binding.textView2.visibility = View.VISIBLE
                    binding.categoriesHorizontalSV.visibility = View.VISIBLE
                    binding.textView3.visibility = View.VISIBLE
                    binding.searchIcon.visibility = View.VISIBLE
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {}

            override fun onButtonClicked(buttonCode: Int) {
                if (buttonCode == MaterialSearchBar.BUTTON_SPEECH) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.RECORD_AUDIO), VOICE_PERMISSION_REQUEST)
                    } else {
                        openVoiceSearchDialog()
                    }
                }
                else if ( buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    selectedCategoryButton?.let {
                        it.scaleX = 1f
                        it.scaleY = 1f
                    }
                    inventoryListAdapter.filterByCategory("All")
                }
            }
        })


        binding.mainSlider.setImageList(sliderImageList)

        binding.allProdRCV.adapter = inventoryListAdapter
        binding.allProdRCV.layoutManager = GridLayoutManager(requireContext(), 3)


    }

    private fun showNotificationPopup(view: View) {
        Log.d("UserHomeFragmentLog", "Inbox clicked")

        val popup = PopupMenu(requireContext(), view)

        if (notificationList.isNullOrEmpty()){
            popup.menu.add(Menu.NONE, Menu.NONE, 0, "Inbox Empty")
        }

        for (notification in notificationList) {
            Log.d("UserHomeFragmentLog", notification.toString())

            val menuItem = when (notification.title) {
                NotificationType.PROMO_CODE -> "${Globals.convertMillisToDateTime(notification.timeStamp, "HH:mm dd/MM/yyyy")}\nPromo Code: ${notification.content}"
                NotificationType.WARNING -> "Warning: ${notification.content}"
            }

            popup.menu.add(Menu.NONE, Menu.NONE, 0, menuItem)
        }

        popup.show()
    }

    private fun toggleCategoryFilter(category: String, categoryButton: ImageButton) {
        // Check if a category is already selected
        if (selectedCategory == category) {
            // Deactivate the filter
            binding.mainSlider.visibility = View.VISIBLE
            inventoryListAdapter.filterByCategory("All")
            selectedCategory = null
            categoryButton.scaleX = 1f
            categoryButton.scaleY = 1f
        } else {
            // Activate the filter with the clicked category
            binding.mainSlider.visibility = View.GONE
            inventoryListAdapter.filterByCategory(category)

            // Reset the scale of the previously selected category's ImageButton
            selectedCategoryButton?.let {
                it.scaleX = 1f
                it.scaleY = 1f
            }

            // Set the scale of the clicked category's ImageButton
            selectedCategoryButton = categoryButton
            selectedCategoryButton?.let {
                it.scaleX = 0.6f
                it.scaleY = 0.6f
            }

            selectedCategory = category
        }
    }

    private fun checkAndDismissProgressDialog() {

        if (asyncTasksCounter == 4) {
            initialLaunch = false

            Handler().postDelayed({
                progressDialog.dismiss()
            }, 2000)
        }
    }

    private fun openVoiceSearchDialog() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )
        startActivityForResult(intent, VOICE_SEARCH_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == VOICE_SEARCH_REQUEST && resultCode == AppCompatActivity.RESULT_OK){
            val StringList = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (StringList != null) {
                binding.searchQuery.openSearch()
                binding.searchQuery.text = StringList[0]
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == VOICE_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openVoiceSearchDialog()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog.dismiss()
        FirebaseDatabaseManager.removeProductDataChangeListener(this)
    }


    override fun onProductDataChanged(updatedData: List<Product>) {
        inventoryList.clear()
        inventoryList.addAll(updatedData)
        inventoryListAdapter.notifyDataSetChanged()
        asyncTasksCounter++
        if (isAdded) {
            checkAndDismissProgressDialog()
        }
    }

}