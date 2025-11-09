package com.travelmate.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Helper class for uploading images to Cloudinary
 * 
 * To use this helper, initialize Cloudinary in your Application class:
 * 
 * In TravelMateApp.onCreate():
 * ImageUploadHelper.initialize(
 *     context = this,
 *     cloudName = "TravelMate",
 *     apiKey = "952399415715477",
 *     apiSecret = "inaZGDNRUKV17TPntjJmaHCvUU4"
 * )
 */
object ImageUploadHelper {
    
    private const val TAG = "ImageUploadHelper"
    private var isInitialized = false
    
    /**
     * Upload an image from URI to Cloudinary
     * @param context Application context
     * @param imageUri URI of the image to upload
     * @param folder Optional folder path in Cloudinary (e.g., "travelmate/voyages")
     * @return Public URL of the uploaded image
     */
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        folder: String? = "travelmate/voyages"
    ): Result<String> = suspendCancellableCoroutine { continuation ->
        
        if (!isInitialized) {
            continuation.resumeWithException(
                IllegalStateException("Cloudinary MediaManager not initialized. Please call ImageUploadHelper.initialize() in Application.onCreate()")
            )
            return@suspendCancellableCoroutine
        }
        
        try {
            val uploadRequest = MediaManager.get().upload(imageUri)
                .option("resource_type", "image")
                .option("transformation", "f_auto,q_auto")
            
            if (folder != null) {
                uploadRequest.option("folder", folder)
            }
            
            val requestId = uploadRequest
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(TAG, "Upload started: $requestId")
                    }
                    
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = if (totalBytes > 0) (bytes * 100 / totalBytes).toInt() else 0
                        Log.d(TAG, "Upload progress: $progress%")
                    }
                    
                    override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                        val url = resultData["url"] as? String
                        if (url != null) {
                            Log.d(TAG, "Upload successful: $url")
                            continuation.resume(Result.success(url))
                        } else {
                            Log.e(TAG, "Upload succeeded but no URL in response")
                            continuation.resumeWithException(
                                Exception("Upload succeeded but no URL returned")
                            )
                        }
                    }
                    
                    override fun onError(requestId: String, error: ErrorInfo) {
                        val errorMsg = "Upload failed: ${error.description}"
                        Log.e(TAG, errorMsg)
                        continuation.resumeWithException(
                            Exception(errorMsg)
                        )
                    }
                    
                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w(TAG, "Upload rescheduled: ${error.description}")
                    }
                })
                .dispatch()
            
            continuation.invokeOnCancellation {
                try {
                    MediaManager.get().cancelRequest(requestId)
                } catch (e: Exception) {
                    Log.w(TAG, "Error canceling upload: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting upload: ${e.message}", e)
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * Upload a Bitmap to Cloudinary
     * Converts Bitmap to temporary file first, then uploads
     * @param context Application context
     * @param bitmap Bitmap to upload
     * @param folder Optional folder path in Cloudinary
     * @return Public URL of the uploaded image
     */
    suspend fun uploadBitmap(
        context: Context,
        bitmap: Bitmap,
        folder: String? = "travelmate/voyages"
    ): Result<String> = suspendCancellableCoroutine { continuation ->
        
        if (!isInitialized) {
            continuation.resumeWithException(
                IllegalStateException("Cloudinary MediaManager not initialized. Please call ImageUploadHelper.initialize() in Application.onCreate()")
            )
            return@suspendCancellableCoroutine
        }
        
        try {
            // Convert Bitmap to temporary file
            val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            
            val uploadRequest = MediaManager.get().upload(tempFile.absolutePath)
                .option("resource_type", "image")
                .option("transformation", "f_auto,q_auto")
            
            if (folder != null) {
                uploadRequest.option("folder", folder)
            }
            
            val requestId = uploadRequest
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {
                        Log.d(TAG, "Upload started: $requestId")
                    }
                    
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        val progress = if (totalBytes > 0) (bytes * 100 / totalBytes).toInt() else 0
                        Log.d(TAG, "Upload progress: $progress%")
                    }
                    
                    override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                        // Clean up temp file
                        try {
                            tempFile.delete()
                        } catch (e: Exception) {
                            Log.w(TAG, "Error deleting temp file: ${e.message}")
                        }
                        
                        val url = resultData["url"] as? String
                        if (url != null) {
                            Log.d(TAG, "Upload successful: $url")
                            continuation.resume(Result.success(url))
                        } else {
                            Log.e(TAG, "Upload succeeded but no URL in response")
                            continuation.resumeWithException(
                                Exception("Upload succeeded but no URL returned")
                            )
                        }
                    }
                    
                    override fun onError(requestId: String, error: ErrorInfo) {
                        // Clean up temp file
                        try {
                            tempFile.delete()
                        } catch (e: Exception) {
                            Log.w(TAG, "Error deleting temp file: ${e.message}")
                        }
                        
                        val errorMsg = "Upload failed: ${error.description}"
                        Log.e(TAG, errorMsg)
                        continuation.resumeWithException(
                            Exception(errorMsg)
                        )
                    }
                    
                    override fun onReschedule(requestId: String, error: ErrorInfo) {
                        Log.w(TAG, "Upload rescheduled: ${error.description}")
                    }
                })
                .dispatch()
            
            continuation.invokeOnCancellation {
                try {
                    MediaManager.get().cancelRequest(requestId)
                    tempFile.delete()
                } catch (e: Exception) {
                    Log.w(TAG, "Error canceling upload: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting upload: ${e.message}", e)
            continuation.resumeWithException(e)
        }
    }
    
    /**
     * Initialize Cloudinary MediaManager
     * Should be called in Application.onCreate()
     * 
     * @param context Application context
     * @param cloudName Cloudinary cloud name
     * @param apiKey Cloudinary API key
     * @param apiSecret Cloudinary API secret
     */
    fun initialize(
        context: Context,
        cloudName: String,
        apiKey: String,
        apiSecret: String
    ) {
        if (!isInitialized) {
            try {
                val config = HashMap<String, Any>()
                config["cloud_name"] = cloudName
                config["api_key"] = apiKey
                config["api_secret"] = apiSecret
                MediaManager.init(context, config)
                isInitialized = true
                Log.d(TAG, "Cloudinary initialized with cloud name: $cloudName")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Cloudinary: ${e.message}", e)
                throw e
            }
        } else {
            Log.d(TAG, "Cloudinary already initialized")
        }
    }
}
