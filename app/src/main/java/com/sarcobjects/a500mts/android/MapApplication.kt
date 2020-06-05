package com.sarcobjects.a500mts.android

import android.app.Application

class MapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        applicationComponent = DaggerApplicationComponent.builder()
                .contextModule(ContextModule(applicationContext))
                .build()
    }

    companion object {
        // Reference to the application graph that is used across the whole app
        var applicationComponent: ApplicationComponent? = null
    }
}