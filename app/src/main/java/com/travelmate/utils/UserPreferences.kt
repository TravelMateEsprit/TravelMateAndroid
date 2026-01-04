package com.travelmate.utils

import android.content.Context
import android.content.SharedPreferences
import com.travelmate.data.models.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("travelmate_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_STATUS = "user_status"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_RECOMMENDED_INSURANCES = "recommended_insurance_names"
    }
    
    fun saveAuthResponse(accessToken: String, refreshToken: String, user: User?) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            user?.let {
                putString(KEY_USER_ID, it._id)
                putString(KEY_USER_TYPE, it.userType)
                putString(KEY_USER_EMAIL, it.email)
                putString(KEY_USER_NAME, it.name ?: "")
                putString(KEY_USER_STATUS, it.status)
                putString(KEY_USER_PHONE, it.phone)
                // Save full user object as JSON
                try {
                    val userJson = kotlinx.serialization.json.Json.encodeToString(User.serializer(), it)
                    putString(KEY_USER_DATA, userJson)
                } catch (e: Exception) {
                    android.util.Log.e("UserPreferences", "Error saving user data: ${e.message}")
                }
            }
            apply()
        }
    }
    
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    
    fun getUserType(): String? = prefs.getString(KEY_USER_TYPE, null)
    
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)
    
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    
    fun getUserPhone(): String? = prefs.getString(KEY_USER_PHONE, null)
    
    fun getUserData(): String? = prefs.getString(KEY_USER_DATA, null)
    
    fun isLoggedIn(): Boolean = getAccessToken() != null
    
    fun isUser(): Boolean = getUserType() == "user"
    
    fun isAgency(): Boolean = getUserType() == "agence"
    
    fun saveRecommendedInsuranceNames(names: Set<String>) {
        prefs.edit().putStringSet(KEY_RECOMMENDED_INSURANCES, names).apply()
    }
    
    fun getRecommendedInsuranceNames(): Set<String> {
        return prefs.getStringSet(KEY_RECOMMENDED_INSURANCES, emptySet()) ?: emptySet()
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
