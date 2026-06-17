package com.heathen.ialemus

import android.app.Application
import android.os.StrictMode
import com.heathen.ialemus.BuildConfig

class IalemusApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build(),
            )
        }
        container = AppContainer(this)
    }
}
