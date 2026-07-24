package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipient",
    indices = [
        Index("contactLookupKey")
    ]
)
data class RecipientEntity(
    @PrimaryKey val id: Long = 0,
    val address: String = "",
    val contactLookupKey: String? = null,
    val lastUpdate: Long = 0
)
