package com.travelmate.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.travelmate.data.api.UserApi
import com.travelmate.data.models.User
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserService @Inject constructor(
    private val userApi: UserApi,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("travelmate_prefs", Context.MODE_PRIVATE)
    
    private fun getAuthToken(): String {
        val token = prefs.getString("access_token", "") ?: ""
        return "Bearer $token"
    }
    
    suspend fun getUserById(userId: String): Result<User> {
        return try {
            Log.d("UserService", "=== GET USER BY ID ===")
            Log.d("UserService", "User ID: $userId")
            
            val response = userApi.getUserById(userId, getAuthToken())
            Log.d("UserService", "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                Log.d("UserService", "Successfully retrieved user: ${user.email}")
                Result.success(user)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération de l'utilisateur - HTTP ${response.code()}"
                Log.e("UserService", errorMsg)
                Log.e("UserService", "Error body: $errorBody")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("UserService", errorMsg, e)
            Result.failure(e)
        }
    }
    
    suspend fun getUsersByIds(userIds: List<String>): Result<List<User>> {
        return try {
            Log.d("UserService", "=== GET USERS BY IDS ===")
            Log.d("UserService", "Fetching ${userIds.size} users")
            
            val response = userApi.getUsersByIds(getAuthToken(), userIds)
            Log.d("UserService", "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val users = response.body()!!
                Log.d("UserService", "Successfully retrieved ${users.size} users")
                Result.success(users)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération des utilisateurs - HTTP ${response.code()}"
                Log.e("UserService", errorMsg)
                Log.e("UserService", "Error body: $errorBody")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("UserService", errorMsg, e)
            Result.failure(e)
        }
    }

    suspend fun getUserProfileById(userId: String): Result<User> {
        return try {
            Log.d("UserService", "=== GET USER PROFILE BY ID (api/users/:id) ===")
            Log.d("UserService", "User ID: $userId")
            
            val response = userApi.getUserById(userId, getAuthToken())
            Log.d("UserService", "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                Log.d("UserService", "Successfully retrieved user profile: ${user.email}")
                Result.success(user)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = "Erreur lors de la récupération du profil utilisateur - HTTP ${response.code()}"
                Log.e("UserService", errorMsg)
                Log.e("UserService", "Error body: $errorBody")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val errorMsg = "Exception: ${e.javaClass.simpleName} - ${e.message}"
            Log.e("UserService", errorMsg, e)
            Result.failure(e)
        }
    }
}
