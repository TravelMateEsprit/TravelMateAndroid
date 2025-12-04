package com.travelmate.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.travelmate.data.api.AuthApi
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val authApi: AuthApi,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("travelmate_prefs", Context.MODE_PRIVATE)
    
    private fun getAuthToken(): String {
        val token = prefs.getString("access_token", "") ?: ""
        return "Bearer $token"
    }
    
    suspend fun uploadSignature(signature: MultipartBody.Part): Result<Unit> {
        return try {
            Log.d("AuthService", "=== UPLOAD SIGNATURE ===")
            
            val response = authApi.uploadSignature(getAuthToken(), signature)
            Log.d("AuthService", "Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                Log.d("AuthService", "Signature uploaded successfully")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de l'upload de la signature - HTTP ${response.code()}"
                Log.e("AuthService", errorMsg)
                Log.e("AuthService", "Error body: $errorBody")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("AuthService", errorMsg, e)
            Result.failure(e)
        }
    }
    
    suspend fun updateSignatureName(body: Map<String, String>): Result<Unit> {
        return try {
            Log.d("AuthService", "=== UPDATE SIGNATURE NAME ===")
            Log.d("AuthService", "Signature name: ${body["signatureName"]}")
            
            val response = authApi.updateSignatureName(getAuthToken(), body)
            Log.d("AuthService", "Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                Log.d("AuthService", "Signature name updated successfully")
                Result.success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la mise à jour du nom - HTTP ${response.code()}"
                Log.e("AuthService", errorMsg)
                Log.e("AuthService", "Error body: $errorBody")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("AuthService", errorMsg, e)
            Result.failure(e)
        }
    }
}
