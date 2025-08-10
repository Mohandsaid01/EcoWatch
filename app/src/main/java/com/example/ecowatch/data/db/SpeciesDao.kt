package com.example.ecowatch.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.ecowatch.data.model.SpeciesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeciesDao {

    @Query("SELECT * FROM species ORDER BY id DESC")
    fun getAll(): Flow<List<SpeciesEntity>>

    @Query("SELECT * FROM species WHERE id = :id")
    fun getById(id: Long): Flow<SpeciesEntity?>

    @Query("SELECT * FROM species WHERE name LIKE '%' || :q || '%' ORDER BY name ASC")
    fun searchByName(q: String): Flow<List<SpeciesEntity>>

    @Upsert
    suspend fun upsert(entity: SpeciesEntity)

    @Query("DELETE FROM species WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM species")
    suspend fun deleteAll()
}
