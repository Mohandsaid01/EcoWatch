package com.example.ecowatch.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "species")
data class SpeciesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    // Base
    val name: String,                    // Nom de l’espèce (requis)
    val habitat: String? = null,         // Description courte ou type d’habitat

    // Statut & population (énoncé)
    val status: String? = null,          // Statut de conservation (ex: "vulnérable")
    val population: Int? = null,         // Population estimée

    // Seuils écologiques (énoncé)
    val minTemp: Double? = null,         // °C
    val maxTemp: Double? = null,         // °C
    val minHumidity: Double? = null,     // %
    val maxHumidity: Double? = null,     // %

    // Localisation & géocodage (énoncé)
    val lat: Double? = null,
    val lng: Double? = null,
    val address: String? = null,         // Adresse lisible (reverse geocoding)

    // Métadonnées
    val createdAt: Long = System.currentTimeMillis()
)
