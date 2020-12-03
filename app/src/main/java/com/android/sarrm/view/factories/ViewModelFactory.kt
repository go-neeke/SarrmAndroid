package com.android.sarrm.view.factories

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.android.sarrm.data.db.ReplySettingDatabaseDao
import com.android.sarrm.view.models.ReplySettingViewModel

class ViewModelFactory(
    private val datasource: ReplySettingDatabaseDao,
    owner: SavedStateRegistryOwner,
    private val activity: Activity,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ) = with(modelClass) {
        when {
            isAssignableFrom(ReplySettingViewModel::class.java) ->
                ReplySettingViewModel(activity, datasource)
            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}