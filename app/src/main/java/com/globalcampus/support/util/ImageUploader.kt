package com.globalcampus.support.util

import android.content.Context
import android.net.Uri
import com.globalcampus.support.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object ImageUploader {
    suspend fun uploadImage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bytes = inputStream.readBytes()
            inputStream.close()

            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart("file", "image.jpg", bytes.toRequestBody("image/jpeg".toMediaTypeOrNull()))
                .build()

            val request = Request.Builder()
                .url("https://api.cloudinary.com/v1_1/${BuildConfig.CLOUDINARY_CLOUD_NAME}/image/upload")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            val json = org.json.JSONObject(responseBody ?: "")
            return@withContext json.optString("secure_url")
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext null
        }
    }
}