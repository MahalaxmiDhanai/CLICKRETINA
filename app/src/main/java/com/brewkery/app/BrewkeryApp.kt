package com.brewkery.app

import android.app.Application

class BrewkeryApp : Application() {
    /** Single instance of the dependency container for the app's lifetime. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
