package com.example.data

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object CloudSyncHelper {
    private const val TAG = "CloudSyncHelper"
    private const val BASE_URL = "https://kvdb.io/GLAOnSiteSync_e1d9fa9c_v2/"

    private val client = OkHttpClient.Builder().build()
    
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun <T> fetchList(key: String, elementClass: Class<T>): List<T>? = withContext(Dispatchers.IO) {
        val url = "$BASE_URL$key"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: return@use null
                    val listType = Types.newParameterizedType(List::class.java, elementClass)
                    val adapter = moshi.adapter<List<T>>(listType)
                    return@withContext adapter.fromJson(bodyString)
                } else if (response.code == 404) {
                    // Key doesn't exist yet, which is normal for first startup
                    Log.d(TAG, "Key $key not found on remote (404).")
                    return@withContext emptyList<T>()
                } else {
                    Log.w(TAG, "Failed to fetch $key: ${response.code} ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching list for $key", e)
        }
        return@withContext null
    }

    suspend fun <T> saveList(key: String, list: List<T>, elementClass: Class<T>): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL$key"
        val listType = Types.newParameterizedType(List::class.java, elementClass)
        val adapter = moshi.adapter<List<T>>(listType)
        val jsonString = adapter.toJson(list)

        val requestBody = jsonString.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully saved $key to cloud sync.")
                    return@withContext true
                } else {
                    Log.w(TAG, "Failed to save $key: ${response.code} ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving list for $key", e)
        }
        return@withContext false
    }

    suspend fun <T> fetchSingle(key: String, elementClass: Class<T>): T? = withContext(Dispatchers.IO) {
        val url = "$BASE_URL$key"
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: return@use null
                    val adapter = moshi.adapter(elementClass)
                    return@withContext adapter.fromJson(bodyString)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching single for $key", e)
        }
        return@withContext null
    }

    suspend fun <T> saveSingle(key: String, data: T, elementClass: Class<T>): Boolean = withContext(Dispatchers.IO) {
        val url = "$BASE_URL$key"
        val adapter = moshi.adapter(elementClass)
        val jsonString = adapter.toJson(data)

        val requestBody = jsonString.toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                return@withContext response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving single for $key", e)
        }
        return@withContext false
    }
}
