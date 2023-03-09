package com.t2r2.volleyexample

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageSwitcher
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.example.myapplication.R
import java.io.IOException
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap



class UploadPhotos : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageButton: Button
    private lateinit var sendButton: Button

    private lateinit var imageSwitcher: ImageSwitcher
    private lateinit var nextBtn: Button
    private lateinit var prevBtn: Button

    private var imageData: ByteArray? = null
    private val postURL: String = "https://ptsv2.com/" // remember to use your own api

    //store uris of picked images
    private var images : ArrayList<Uri?>? = null

    //current position of picked images
    private var position = 0

    //request code to pick images
    private val PICK_IMAGES_CODE = 999

//    companion object {
//        private const val IMAGE_PICK_CODE = 999
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_photos)

        images = ArrayList()

        imageSwitcher = findViewById(R.id.imageSwitcher)
        imageSwitcher.setFactory{ImageView(applicationContext)}

        nextBtn = findViewById(R.id.nextBtn)
        nextBtn.setOnClickListener{
            if(position<images!!.size-1){
                position++
                imageSwitcher.setImageURI(images!![position])
            }
            else{
                Toast.makeText(this,"no more images", Toast.LENGTH_SHORT).show()
            }
        }

        prevBtn = findViewById(R.id.prevBtn)
        prevBtn.setOnClickListener {
            if (position>0){
                position--
                imageSwitcher.setImageURI(images!![position])
            }
            else{
                Toast.makeText(this,"no more images", Toast.LENGTH_SHORT).show()
            }
        }

        imageView = findViewById(R.id.imageView)

        imageButton = findViewById(R.id.imageButton)    //choose image wala button
        imageButton.setOnClickListener {
            launchGallery()
        }
        sendButton = findViewById(R.id.sendButton)
        sendButton.setOnClickListener {
            uploadImage()
        }
    }

    public fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        intent.action=Intent.ACTION_GET_CONTENT
        //startActivityForResult(intent, IMAGE_PICK_CODE)
        startActivityForResult(Intent.createChooser(intent,"selected images"), PICK_IMAGES_CODE)

    }

    private fun uploadImage() {
        if (images == null || images!!.isEmpty()) {
            Toast.makeText(this, "No images to upload", Toast.LENGTH_SHORT).show()
            return
        }

        val requestQueue = Volley.newRequestQueue(this)

        val volleyMultipartRequest = object : VolleyFileUploadRequest(Method.POST, postURL,
            Response.Listener { response ->
                val responseString = String(response.data)
                Toast.makeText(this, "Upload success: $responseString", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error: VolleyError? ->
                Toast.makeText(this, "Upload failed: ${error?.message}", Toast.LENGTH_SHORT).show()
            },
            dataParts = listOf(
                DataPart(
                    fileName = "my_image.png",
                    data = imageData,   ///////////////////////////////////////////////////////////////////////////////////
                    mimeType = "image/png"
                )
            )
        )
        {
            override fun getByteData(): Map<String, FileDataPart>? {
                val params = HashMap<String, FileDataPart>()
                images!!.forEachIndexed { index, uri ->
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                        params["image${index + 1}"] = FileDataPart("image${index + 1}.png", byteArrayOutputStream.toByteArray())
                    }
                    return params
                }
            }

        requestQueue.add(volleyMultipartRequest)
    }



    @Throws(IOException::class)
    private fun createImageData(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.buffered()?.use {
            imageData = it.readBytes()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGES_CODE) {
            val uri = data?.data
            if (uri != null) {
                imageView.setImageURI(uri)
                createImageData(uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
        //////////////
        if (requestCode==PICK_IMAGES_CODE){
            if (resultCode==Activity.RESULT_OK){
                if (data!!.clipData != null ){
                    val count = data.clipData!!.itemCount
                    for (i in 0 until count){
                        val imageUri = data.clipData!!.getItemAt(i).uri
                        images!!.add(imageUri)
                    }
                    imageSwitcher.setImageURI(images!![0])
                }
                else{
                    val imageUri = data.data
                    imageSwitcher.setImageURI(imageUri)
                }
            }
        }
    }
}