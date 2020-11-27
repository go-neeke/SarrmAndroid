package com.android.sarrm.application

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication

class SarrmApplication : MultiDexApplication() {

    companion object {
        private lateinit var instance: SarrmApplication

        fun getInstance() : SarrmApplication {
            return instance
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}