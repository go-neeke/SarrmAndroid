package com.android.sarrm.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android.sarrm.data.models.ReplySetting
import com.android.sarrm.utils.AppConstants.Companion.REPLY_SETTING_DATABASE

@Database(entities = [ReplySetting::class], version = 1)
@TypeConverters(ReplySetting::class)
abstract class ReplySettingDatabase : RoomDatabase() {

    abstract val dao: ReplySettingDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: ReplySettingDatabase? = null

        fun getInstance(context: Context): ReplySettingDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ReplySettingDatabase::class.java,
                        REPLY_SETTING_DATABASE
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}