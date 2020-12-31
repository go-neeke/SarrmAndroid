package com.sarrm.android.data.realm

import androidx.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmList

class RealmListLiveData<T : ReplyResult>(private val results: RealmList<T>) :
    LiveData<RealmList<T>>() {
    private val listener: RealmChangeListener<RealmList<T>> =
        RealmChangeListener { results -> value = results }

    override fun onActive() {
        super.onActive()

        if (results.isValid) { // invalidated results can no longer be observed.
            results.addChangeListener(listener)

            if (results.isLoaded) {
                value = results
            }
        }
    }

    override fun onInactive() {
        results.removeChangeListener(listener)
    }
}