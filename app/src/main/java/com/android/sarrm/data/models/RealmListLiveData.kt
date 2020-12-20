package com.android.sarrm.data.models

import androidx.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmList

class RealmListLiveData<T : String>(private val results: RealmList<T>) :
    LiveData<RealmList<T>>() {
    private val listener: RealmChangeListener<RealmList<T>> =
        RealmChangeListener { results -> value = results }

    override fun onActive() {
        results.addChangeListener(listener)
    }

    override fun onInactive() {
        results.removeChangeListener(listener)
    }
}