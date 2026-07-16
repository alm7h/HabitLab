package com.hudnah.habitlab

import android.app.Application
import com.hudnah.habitlab.data.local.AppDatabase
import com.hudnah.habitlab.di.AppContainer

class HabitLabApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(AppDatabase.getInstance(this))
    }
}
