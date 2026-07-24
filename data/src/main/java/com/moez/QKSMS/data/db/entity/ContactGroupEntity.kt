package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "contact_group"
)
data class ContactGroupEntity(
    @PrimaryKey val id: Long = 0,
    val title: String = ""
)
