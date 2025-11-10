package com.travelmate

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TravelMateApp : Application() {
    override fun onCreate() {
        super.onCreate()

    }
}
