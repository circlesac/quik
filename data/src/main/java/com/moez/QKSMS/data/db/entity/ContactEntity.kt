package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "contact"
)
data class ContactEntity(
    @PrimaryKey val lookupKey: String = "",
    val name: String = "",
    val photoUri: String? = null,
    val starred: Boolean = false,
    val lastUpdate: Long = 0
)
