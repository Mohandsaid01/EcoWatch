package com.example.ecowatch.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ecowatch.data.model.SpeciesEntity

@Database(
    entities = [SpeciesEntity::class],
    version = 2,              //
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun speciesDao(): SpeciesDao
}
