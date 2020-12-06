package com.android.sarrm.application

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import io.realm.Realm
import io.realm.RealmConfiguration


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

        Realm.init(this)
        Realm.setDefaultConfiguration(getDefaultConfiguration())
        Logger.addLogAdapter(AndroidLogAdapter())
    }

    private fun getDefaultConfiguration() :RealmConfiguration {
        return RealmConfiguration.Builder().schemaVersion(0).build()
    }
}