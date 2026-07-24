package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "blocked_number"
)
data class BlockedNumberEntity(
    @PrimaryKey val id: Long = 0,
    val address: String = ""
)
