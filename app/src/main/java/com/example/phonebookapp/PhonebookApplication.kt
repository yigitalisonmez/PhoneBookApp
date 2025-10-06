package com.example.phonebookapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PhonebookApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Uygulama ilk başlatıldığında burası çalışır (sadece 1 kez)
        // Global ayarlar burada yapılabilir

        android.util.Log.d("PhonebookApp", "🚀 Uygulama başlatıldı!")
    }

    override fun onTerminate() {
        super.onTerminate()
        android.util.Log.d("PhonebookApp", "❌ Uygulama sonlandırılıyor")
    }
}