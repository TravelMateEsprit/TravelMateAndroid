package com.travelmate

import android.app.Application
import android.preference.PreferenceManager
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class TravelMateApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize OSMDroid configuration
        Configuration.getInstance()
                .load(
                        applicationContext,
                        PreferenceManager.getDefaultSharedPreferences(applicationContext)
                )
        // Set a proper user agent
        Configuration.getInstance().userAgentValue = packageName
    }
}
