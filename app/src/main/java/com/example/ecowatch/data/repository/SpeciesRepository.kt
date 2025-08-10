package com.example.ecowatch.data.repository

import android.content.Context
import com.example.ecowatch.data.db.DatabaseProvider
import com.example.ecowatch.data.db.SpeciesDao
import com.example.ecowatch.data.model.Species
import com.example.ecowatch.data.model.SpeciesEntity
import com.example.ecowatch.data.model.toDomain
import com.example.ecowatch.data.model.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class SpeciesRepository private constructor(
    private val dao: SpeciesDao
) {
    val allSpecies: Flow<List<Species>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    fun searchByName(q: String): Flow<List<Species>> =
        dao.searchByName(q).map { list -> list.map { it.toDomain() } }

    fun getById(id: Long): Flow<Species?> =
        dao.getById(id).map { it?.toDomain() }

    suspend fun save(species: Species) = dao.upsert(species.toEntity())

    suspend fun insertEntity(entity: SpeciesEntity) = dao.upsert(entity)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun deleteAll() = dao.deleteAll()

    fun checkThresholds(species: Species, currentTemp: Double?, currentHum: Double?): List<String> {
        val alerts = mutableListOf<String>()
        species.minTemp?.let { minT -> currentTemp?.let { t -> if (t < minT) alerts += "Température actuelle ${t}°C < min ${minT}°C" } }
        species.maxTemp?.let { maxT -> currentTemp?.let { t -> if (t > maxT) alerts += "Température actuelle ${t}°C > max ${maxT}°C" } }
        species.minHumidity?.let { minH -> currentHum?.let { h -> if (h < minH) alerts += "Humidité actuelle ${h}% < min ${minH}%" } }
        species.maxHumidity?.let { maxH -> currentHum?.let { h -> if (h > maxH) alerts += "Humidité actuelle ${h}% > max ${maxH}%" } }
        return alerts
    }

    // ---------- Firestore backup / restore ----------
    private val fs by lazy { FirebaseFirestore.getInstance() }
    private val collectionName = "species"

    suspend fun syncUp(species: Species) {
        val docId = if ((species.id ?: 0L) != 0L) species.id.toString() else null
        val col = fs.collection(collectionName)
        val ref = if (docId != null) col.document(docId) else col.document()
        val data = mapOf(
            "id" to species.id,
            "name" to species.name,
            "habitat" to species.habitat,
            "status" to species.status,
            "population" to species.population,
            "minTemp" to species.minTemp,
            "maxTemp" to species.maxTemp,
            "minHumidity" to species.minHumidity,
            "maxHumidity" to species.maxHumidity,
            "lat" to species.lat,
            "lng" to species.lng,
            "address" to species.address,
            "createdAt" to (species.createdAt ?: System.currentTimeMillis())
        )
        ref.set(data, SetOptions.merge()).await()
    }

    suspend fun syncDownReplace() {
        val snap = fs.collection(collectionName).get().await()
        val list = snap.documents.mapNotNull { doc ->
            val d = doc.data ?: return@mapNotNull null
            Species(
                id = (d["id"] as? Number)?.toLong() ?: 0L,
                name = d["name"] as? String ?: return@mapNotNull null,
                habitat = d["habitat"] as? String,
                status = d["status"] as? String,
                population = (d["population"] as? Number)?.toInt(),
                minTemp = (d["minTemp"] as? Number)?.toDouble(),
                maxTemp = (d["maxTemp"] as? Number)?.toDouble(),
                minHumidity = (d["minHumidity"] as? Number)?.toDouble(),
                maxHumidity = (d["maxHumidity"] as? Number)?.toDouble(),
                lat = (d["lat"] as? Number)?.toDouble(),
                lng = (d["lng"] as? Number)?.toDouble(),
                address = d["address"] as? String,
                createdAt = (d["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
        dao.deleteAll()
        list.forEach { dao.upsert(it.toEntity()) }
    }
    // -----------------------------------------------

    companion object {
        @Volatile private var INSTANCE: SpeciesRepository? = null
        fun get(context: Context): SpeciesRepository =
            INSTANCE ?: synchronized(this) {
                val db = DatabaseProvider.get(context)
                SpeciesRepository(db.speciesDao()).also { INSTANCE = it }
            }
    }
}
