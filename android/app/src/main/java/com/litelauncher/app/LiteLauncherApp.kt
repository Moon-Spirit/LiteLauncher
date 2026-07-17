package com.litelauncher.app

import android.app.Application

class LiteLauncherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: LiteLauncherApp
            private set
    }
}
