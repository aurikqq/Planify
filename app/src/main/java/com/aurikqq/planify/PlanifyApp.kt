package com.aurikqq.planify

import android.app.Application
import android.content.Context

class PlanifyApp : Application() {
    val repository: Repository by lazy {
        Repository(
            getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE),
            applicationContext
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: PlanifyApp
            private set
    }
}
