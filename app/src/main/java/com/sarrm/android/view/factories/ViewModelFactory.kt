package com.sarrm.android.view.factories

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.sarrm.android.view.models.ReplyResultViewModel
import com.sarrm.android.view.models.ReplySettingListViewModel
import com.sarrm.android.view.models.ReplySettingViewModel

class ViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val activity: Activity,
    defaultArgs: Bundle? = null
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ) = with(modelClass) {
        when {
            isAssignableFrom(ReplySettingViewModel::class.java) ->
                ReplySettingViewModel(activity)
            isAssignableFrom(ReplySettingListViewModel::class.java) ->
                ReplySettingListViewModel(activity)
            isAssignableFrom(ReplyResultViewModel::class.java) ->
                ReplyResultViewModel(activity)

            else ->
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    } as T
}