package com.example.alertatemprana.network

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        PersistanceManager.init(this)
    }
}