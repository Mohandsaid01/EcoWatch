package com.example.ecowatch.data.remote

import com.example.ecowatch.data.model.Species
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("species")

    // 🔹 Sauvegarde toutes les espèces dans Firestore
    suspend fun backupSpecies(list: List<Species>) {
        list.forEach { species ->
            collection.document(species.id.toString())
                .set(species)
                .await()
        }
    }

    // 🔹 Restaure toutes les espèces depuis Firestore
    suspend fun restoreSpecies(): List<Species> {
        val snapshot = collection.get().await()
        return snapshot.toObjects(Species::class.java)
    }
}
