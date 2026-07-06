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

object FirestoreSyncHelper {
    private const val TAG = "FirestoreSyncHelper"
    
    // Default project ID for Gupta Lakhani & Associates enterprise backup
    var activeProjectId = "gla-onsite-sync-e1d9fa9c"
    
    private val client = OkHttpClient.Builder().build()
    
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // Firestore REST structures for Moshi
    class FirestoreDocument(val fields: FirestoreFields? = null)
    class FirestoreFields(val data: FirestoreValue? = null)
    class FirestoreValue(val stringValue: String? = null)

    private fun buildUrl(key: String): String {
        return "https://firestore.googleapis.com/v1/projects/$activeProjectId/databases/(default)/documents/enterprise_backups/$key"
    }

    suspend fun <T> fetchList(key: String, elementClass: Class<T>): List<T>? = withContext(Dispatchers.IO) {
        val url = buildUrl(key)
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: return@use null
                    val docAdapter = moshi.adapter(FirestoreDocument::class.java)
                    val doc = docAdapter.fromJson(bodyString)
                    val jsonListString = doc?.fields?.data?.stringValue ?: return@use emptyList<T>()
                    
                    val listType = Types.newParameterizedType(List::class.java, elementClass)
                    val listAdapter = moshi.adapter<List<T>>(listType)
                    return@withContext listAdapter.fromJson(jsonListString)
                } else if (response.code == 404) {
                    Log.d(TAG, "Backup for $key not found in Firestore (404). Treating as empty.")
                    return@withContext emptyList<T>()
                } else {
                    Log.w(TAG, "Failed to fetch $key from Firestore: ${response.code} ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching list for $key from Firestore", e)
        }
        return@withContext null
    }

    suspend fun <T> saveList(key: String, list: List<T>, elementClass: Class<T>): Boolean = withContext(Dispatchers.IO) {
        val url = buildUrl(key) + "?updateMask.fieldPaths=data"
        
        try {
            val listType = Types.newParameterizedType(List::class.java, elementClass)
            val listAdapter = moshi.adapter<List<T>>(listType)
            val jsonListString = listAdapter.toJson(list)
            
            val doc = FirestoreDocument(FirestoreFields(FirestoreValue(jsonListString)))
            val docAdapter = moshi.adapter(FirestoreDocument::class.java)
            val bodyJson = docAdapter.toJson(doc)

            val requestBody = bodyJson.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .patch(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d(TAG, "Successfully backed up $key to Firestore.")
                    return@withContext true
                } else {
                    Log.w(TAG, "Failed to back up $key to Firestore: ${response.code} ${response.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error backing up list for $key to Firestore", e)
        }
        return@withContext false
    }
}
