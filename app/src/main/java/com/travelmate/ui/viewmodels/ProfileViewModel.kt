package com.travelmate.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelmate.utils.UserPreferences
import com.travelmate.data.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val userJson = userPreferences.getUserData()
                android.util.Log.d("ProfileViewModel", "Loading user data: $userJson")
                
                if (userJson != null) {
                    // Parse user from JSON
                    val user = kotlinx.serialization.json.Json { 
                        ignoreUnknownKeys = true 
                    }.decodeFromString<User>(userJson)
                    
                    android.util.Log.d("ProfileViewModel", "User loaded: ${user.email}, type: ${user.userType}")
                    _currentUser.value = user
                } else {
                    // Fallback: create user from individual preferences
                    val email = userPreferences.getUserEmail()
                    val name = userPreferences.getUserName()
                    val userId = userPreferences.getUserId()
                    val userType = userPreferences.getUserType()
                    val phone = userPreferences.getUserPhone()
                    
                    android.util.Log.d("ProfileViewModel", "No JSON, using individual fields: email=$email, name=$name, type=$userType")
                    
                    if (email != null && userId != null && userType != null) {
                        _currentUser.value = User(
                            _id = userId,
                            email = email,
                            name = name,
                            userType = userType,
                            phone = phone
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ProfileViewModel", "Error loading user: ${e.message}", e)
                
                // Fallback to individual fields
                val email = userPreferences.getUserEmail()
                val name = userPreferences.getUserName()
                val userId = userPreferences.getUserId()
                val userType = userPreferences.getUserType()
                val phone = userPreferences.getUserPhone()
                
                if (email != null && userId != null && userType != null) {
                    _currentUser.value = User(
                        _id = userId,
                        email = email,
                        name = name,
                        userType = userType,
                        phone = phone
                    )
                }
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            userPreferences.clearAll()
            _currentUser.value = null
        }
    }
}
