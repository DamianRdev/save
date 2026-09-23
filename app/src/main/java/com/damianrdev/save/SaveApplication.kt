package com.damianrdev.save

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SaveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
