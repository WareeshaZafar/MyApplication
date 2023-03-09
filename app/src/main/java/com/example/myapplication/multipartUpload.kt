package com.t2r2.volleyexample

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import java.io.*
import kotlin.math.min

import android.util.Base64
import java.util.*

open class VolleyFileUploadRequest(
    method: Int,
    url: String,
    listener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener,
    private val dataParts: List<DataPart>
) : Request<NetworkResponse>(method, url, errorListener) {
    private var responseListener: Response.Listener<NetworkResponse>? = null

    init {
        this.responseListener = listener
    }

    private var headers: Map<String, String>? = null
    private val divider: String = "--"
    private val ending = "\r\n"
    private val boundary = "imageRequest${System.currentTimeMillis()}"

    data class DataPart(
        val fileName: String,
        val data: ByteArray?,
        val mimeType: String = "image/png"
    )

    override fun getHeaders(): MutableMap<String, String> =
        when (headers) {
            null -> super.getHeaders()
            else -> headers!!.toMutableMap()
        }

    override fun getBodyContentType() = "multipart/form-data;boundary=$boundary"

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(byteArrayOutputStream)
        try {
            if (params != null && params!!.isNotEmpty()) {
                processParams(dataOutputStream, params!!, paramsEncoding)
            }
            val data = getByteData() as? Map<String, FileDataPart>?
            if (data != null && data.isNotEmpty()) {
                processData(dataOutputStream, data)
            }
            dataParts.forEach { dataPart ->
                dataOutputStream.writeBytes(divider + boundary + ending)
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"${UUID.randomUUID()}\"; filename=\"${dataPart.fileName}\"$ending")
                dataOutputStream.writeBytes("Content-Type: ${dataPart.mimeType}$ending")
                dataOutputStream.writeBytes(ending)

                // convert image data to base64
                val base64Data = Base64.encodeToString(dataPart.data, Base64.DEFAULT)

                // write base64 data to the output stream
                dataOutputStream.writeBytes(base64Data)
                dataOutputStream.writeBytes(ending)
            }
            dataOutputStream.writeBytes(divider + boundary + divider + ending)
            return byteArrayOutputStream.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return super.getBody()
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return try {
            Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: Exception) {
            Response.error(ParseError(e))
        }
    }

    override fun deliverResponse(response: NetworkResponse) {
        responseListener?.onResponse(response)
    }

    override fun deliverError(error: VolleyError) {
        errorListener?.onErrorResponse(error)
    }

    @Throws(IOException::class)
    private fun processParams(dataOutputStream: DataOutputStream, params: Map<String, String>, encoding: String) {
        try {
            params.forEach {
                dataOutputStream.writeBytes(divider + boundary + ending)
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"${it.key}\"$ending")
                dataOutputStream.writeBytes(ending)
                dataOutputStream.writeBytes(it.value + ending)
            }
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("Unsupported encoding not supported: $encoding with error: ${e.message}")
        }
    }

    @Throws(IOException::class)
    private fun processData(dataOutputStream: DataOutputStream, data: Map<String, FileDataPart>) {
        data.forEach {
            val fileName = it.key
            val fileDataPart = it.value
            dataOutputStream.writeBytes(divider + boundary + ending)
            dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"$fileName\"; filename=\"${fileDataPart.fileName}\"$ending")
            if (fileDataPart.type != null && !fileDataPart.type!!.trim().isEmpty()) {
                dataOutputStream.writeBytes("Content-Type: ${fileDataPart.type}$ending")
            }
            dataOutputStream.writeBytes(ending)
            dataOutputStream.write(fileDataPart.data, 0, fileDataPart.data.size)
            dataOutputStream.writeBytes(ending)
        }
    }

     open fun getByteData(): Map<String, FileDataPart>? {
        return null
    }

    data class FileDataPart(
        val fileName: String,
        val data: ByteArray,
        val type: String? = null
    )
}

