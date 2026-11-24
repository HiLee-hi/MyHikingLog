package com.myhiking.log

import android.app.Application
import com.myhiking.log.util.AppLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyHikingLogApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLogger.i("APP_INIT", "Application started | version=${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE}) | debug=${BuildConfig.DEBUG}")
    }
}
