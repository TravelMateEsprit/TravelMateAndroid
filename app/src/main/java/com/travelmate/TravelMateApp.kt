package com.travelmate

import android.app.Application
import com.travelmate.utils.ImageUploadHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TravelMateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Cloudinary for image uploads
        ImageUploadHelper.initialize(
            context = this,
            cloudName = "TravelMate",
            apiKey = "952399415715477",
            apiSecret = "inaZGDNRUKV17TPntjJmaHCvUU4"
        )
    }
}
