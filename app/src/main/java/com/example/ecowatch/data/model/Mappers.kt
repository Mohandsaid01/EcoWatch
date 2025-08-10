package com.example.ecowatch.data.model

fun SpeciesEntity.toDomain() = Species(
    id = id,
    name = name,
    habitat = habitat,
    status = status,
    population = population,
    minTemp = minTemp,
    maxTemp = maxTemp,
    minHumidity = minHumidity,
    maxHumidity = maxHumidity,
    lat = lat,
    lng = lng,
    address = address,
    createdAt = createdAt
)

fun Species.toEntity() = SpeciesEntity(
    id = id,
    name = name,
    habitat = habitat,
    status = status,
    population = population,
    minTemp = minTemp,
    maxTemp = maxTemp,
    minHumidity = minHumidity,
    maxHumidity = maxHumidity,
    lat = lat,
    lng = lng,
    address = address,
    createdAt = createdAt
)
