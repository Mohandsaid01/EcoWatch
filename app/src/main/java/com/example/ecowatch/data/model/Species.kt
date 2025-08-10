package com.example.ecowatch.data.model

data class Species(
    val id: Long = 0L,
    val name: String,
    val habitat: String? = null,
    val status: String? = null,
    val population: Int? = null,
    val minTemp: Double? = null,
    val maxTemp: Double? = null,
    val minHumidity: Double? = null,
    val maxHumidity: Double? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val address: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
