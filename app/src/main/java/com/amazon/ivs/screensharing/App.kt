package com.amazon.ivs.screensharing

import android.app.Application
import android.content.Context
import com.amazon.ivs.screensharing.core.common.LineNumberDebugTree
import timber.log.Timber

lateinit var appContext: Context

class App : Application() {
    override fun onCreate() {
        appContext = this
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(LineNumberDebugTree())
        }
    }
}
