package com.example.ecowatch.data.db

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase =
        INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ecowatch.db"
            ).fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
        }
}
