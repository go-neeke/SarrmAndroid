package com.android.sarrm.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.android.sarrm.data.models.ReplySetting


@Dao
interface ReplySettingDatabaseDao {
    @Query("SELECT * FROM reply_setting_table")
    fun getAll(): List<ReplySetting>?

    @Insert
    fun insert(number: ReplySetting)

    @Delete
    fun delete(number: ReplySetting)
}