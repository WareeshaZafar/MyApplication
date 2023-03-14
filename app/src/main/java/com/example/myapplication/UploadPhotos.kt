package com.t2r2.volleyexample

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

open class UploadPhotos : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var imageButton: Button
    private lateinit var sendButton: Button

    private lateinit var imageSwitcher: ImageSwitcher
    private lateinit var nextBtn: Button
    private lateinit var prevBtn: Button

    //store uris of picked images
    private var images: ArrayList<String> = ArrayList()

    //current position of picked images
    private var position = 0

    //request code to pick images
    private val PICK_IMAGES_CODE = 999

//    private lateinit var storage: Storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_photos)

        imageSwitcher = findViewById(R.id.imageSwitcher)
        imageSwitcher.setFactory { ImageView(applicationContext) }

        nextBtn = findViewById(R.id.nextBtn)
        nextBtn.setOnClickListener {
            if (position < images.size - 1) {
                position++
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(images[position]))
                imageView.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "no more images", Toast.LENGTH_SHORT).show()
            }
        }

        prevBtn = findViewById(R.id.prevBtn)
        prevBtn.setOnClickListener {
            if (position > 0) {
                position--
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(images[position]))
                imageView.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, "no more images", Toast.LENGTH_SHORT).show()
            }
        }

        imageView = findViewById(R.id.imageView)

        imageButton = findViewById(R.id.imageButton) //choose image wala button
        imageButton.setOnClickListener {
            launchGallery()
        }

        sendButton = findViewById(R.id.sendButton) //upload image wala button
        sendButton.setOnClickListener {
            uploadImages()
        }
////
//          val context = this
//        val inputStream: InputStream = context.assets.open("total-dreamer-380120-1621106aee60.json")
//        val credentials = GoogleCredentials.fromStream(inputStream)
//         storage = StorageOptions.newBuilder()
//            .setProjectId("total-dreamer-380120")
//            .setCredentials(credentials)
//            .build()
//            .service
//        println("Creds valid hain bro!")



    }

    public fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK) //Creates an Intent with the ACTION_PICK action, which is used to select an item from a list
        intent.type = "image/*" //Sets the type of data to select to = image files
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) //Enables the user to select multiple images
        intent.action = Intent.ACTION_GET_CONTENT //Sets the action to "get content", which is used to retrieve data from a content provider
        startActivityForResult(Intent.createChooser(intent, "selected images"), PICK_IMAGES_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGES_CODE && resultCode == RESULT_OK) {
            if (data?.clipData != null) { //if multiple images are selected
                val count = data.clipData!!.itemCount //get the count of selected images
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri //get the uri of each selected image
                    images.add(imageUri.toString()) //add the uri to the list of selected images
                }
                position = 0 //set the current position to the first image
                showImage(images[position]) //display the first image
            } else if (data?.data != null) { //if only one image is selected
                val imageUri = data.data //get the uri of the selected image
                images.add(imageUri.toString()) //add the uri to the list of selected images
                position = 0 //set the current position to the first image
                showImage(images[position]) //display the selected image
            }
        }
    }

    private fun showImage(imageUri: String) {
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(imageUri))
        imageView.setImageBitmap(bitmap)
    }
    private fun uploadImages() = lifecycleScope.launch {
        if (images.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                val credentials = GoogleCredentials.fromStream(assets.open("total-dreamer-380120-1621106aee60.json"))
                val storage = StorageOptions.newBuilder().setCredentials(credentials).build().service

                for (i in 0 until images.size) {
                    val imageUri = images[i]
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(imageUri))
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)

                    val imageName = "image_$i.png"
                    val blobId = BlobId.of("fypphotoproject", imageName)
                    val blobInfo = BlobInfo.newBuilder(blobId).build()
                    val imageByteArray = byteArrayOutputStream.toByteArray()
                    val imageInputStream: InputStream = ByteArrayInputStream(imageByteArray)
                    storage.create(blobInfo, imageInputStream)
                }
            }

            println("Success")
        } else {
            println("Failed")
        }

    }


}
