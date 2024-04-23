package com.example.ecommerceapp.Utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.documentfile.provider.DocumentFile
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.models.SlideModel
import com.example.ecommerceapp.Database.AppDAO
import com.example.ecommerceapp.Helpers.ImageStorageManager
import com.example.ecommerceapp.Models.AdminUser
import com.example.ecommerceapp.Models.GuestUser
import com.example.ecommerceapp.Models.Notification
import com.example.ecommerceapp.Models.RegisteredUser
import com.example.ecommerceapp.Models.User
import com.example.ecommerceapp.Models.UserType
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import org.mindrot.jbcrypt.BCrypt
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Globals {
    var CURRENT_USER_ID: String? = null
    var CURRENT_USER: User? = null
    var DEVICE_ANDROID_ID: String? = null
    var CategoryList: Array<String> = emptyArray()
    var APP_DAO: AppDAO? = null
    var IS_ONLINE: Boolean = false

    fun selectImageFromGallery(
        pickMedia: ActivityResultLauncher<Intent>
    ) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickMedia.launch(intent)
    }

    fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_android_id"
    }

    fun calculateHash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        val inputStream = file.inputStream()

        while (true) {
            val bytesRead = inputStream.read(buffer)
            if (bytesRead == -1) break
            digest.update(buffer, 0, bytesRead)
        }

        val hashBytes = digest.digest()
        val hexString = StringBuilder()

        for (byte in hashBytes) {
            hexString.append(String.format("%02X", byte))
        }

        return hexString.toString()
    }

    fun getFileFromUri(context: Context, uri: Uri): File? {
        return if ("file" == uri.scheme) {
            // Direct file URI
            File(uri.path.orEmpty())
        } else {
            // Content URI, use DocumentFile API
            val documentFile = DocumentFile.fromSingleUri(context, uri)
            documentFile?.let {
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputFile = File(context.cacheDir, "temp_file")
                inputStream?.use { input ->
                    outputFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                outputFile
            }
        }
    }

    fun downloadImageAndGetUri(index: String, path: String, imageStorageManager: ImageStorageManager, onSuccess: (String, Uri) -> Unit, onFailure: (Exception) -> Unit) {
        val storedUri = imageStorageManager.getStoredImageUri(path)
        if (storedUri != null) {
            onSuccess(index, storedUri)
            return
        }

        val imageRef: StorageReference = Constants.CLOUD_STORAGE_REFERENCE.child(path)

        val localFile = File.createTempFile("image", "jpg")

        val downloadTask: StorageTask<FileDownloadTask.TaskSnapshot> = imageRef.getFile(localFile)

        downloadTask.addOnSuccessListener {
            Log.d("Check URI", "Download : ${calculateHash(localFile)}")

            // Store the downloaded image URI
            imageStorageManager.storeImageUri(path, Uri.fromFile(localFile))

            onSuccess(index, Uri.fromFile(localFile))
        }.addOnFailureListener {
            onFailure(it)
        }
    }

    fun retrieveCategories(callback: (Array<String>) -> Unit) {
        val categoriesList = mutableListOf<String>()

        Constants.DATABASE_REFERENCE_CATEGORIES.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (categorySnapshot in dataSnapshot.children) {

                        val categoryValue = categorySnapshot.value.toString()
                        categoriesList.add(categoryValue)
                    }

                    CategoryList = categoriesList.toTypedArray()
                    callback(CategoryList)

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun retrieveNotifications(callback: (Array<Notification>?) -> Unit){
        val notificationList = mutableListOf<Notification>()

        Constants.DATABASE_REFERENCE_NOTIFICATIONS.child(CURRENT_USER_ID!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (notificationSnapshot in dataSnapshot.children) {
                        val notification = notificationSnapshot.getValue(Notification::class.java)
                        Log.d("Notifications", notification.toString())

                        notification?.let {
                            notificationList.add(it)
                        }
                    }
                    callback(notificationList.toTypedArray())
                }
                else {
                    callback(null)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(null)
            }
        })

    }

    fun fillStringArrayListWithDummyData(arrayList: ArrayList<String>, size: Int) {
        for (i in 0 until size) {
            // Add dummy data to the ArrayList
            arrayList.add("DummyData$i")
        }
    }

    fun fillSliderArrayListWithDummyData(arrayList: ArrayList<SlideModel>, size: Int) {
        for (i in 0 until size) {
            // Add dummy data to the ArrayList
            arrayList.add(SlideModel("DummyData$i"))
        }
    }

    fun convertMillisToDateTime(millis: Long, format : String): String {
        val date = Date(millis)
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(date)
    }

    fun initializeSlider(slider : ImageSlider, sliderImageList: List<SlideModel>){
        slider.setImageList(sliderImageList)
    }

    fun Resources.dpToPx(dp: Int): Int {
        val scale = displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun sha256PasswordEncryption(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(bytes)
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }

    fun bcryptHashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun bcryptverifyPassword(inputPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(inputPassword, hashedPassword)
    }

    fun retrieveCurrentUser(sharedPreferences : SharedPreferences, firebaseUser : FirebaseUser?) {

        if (sharedPreferences.getString("current_user", UserType.GUEST.name) == UserType.GUEST.name){
            Log.d("AppLog", UserType.GUEST.name)


            CURRENT_USER = GuestUser(DEVICE_ANDROID_ID!!)
        }
        else if (sharedPreferences.getString("current_user", UserType.GUEST.name) == UserType.REGISTERED.name){
            Log.d("AppLog", UserType.REGISTERED.name)

            Constants.DATABASE_REFERENCE_USERS.child("Registered").orderByChild("userID").equalTo(firebaseUser!!.uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (childSnap in dataSnapshot.children){
                            val registeredUser = childSnap.getValue(RegisteredUser::class.java)!!

                            Log.d("AppLog", registeredUser.toString())

                            CURRENT_USER = registeredUser
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }
        else if (sharedPreferences.getString("current_user", UserType.GUEST.name) == UserType.ADMIN.name){
            Log.d("AppLog", UserType.ADMIN.name)

            Constants.DATABASE_REFERENCE_USERS.child("Admins").orderByChild("adminID").equalTo(firebaseUser!!.uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (childSnap in dataSnapshot.children){
                            val adminUser = childSnap.getValue(AdminUser::class.java)!!

                            CURRENT_USER = adminUser

                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }
    }
}

