package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "message_content_filter"
)
data class MessageContentFilterEntity(
    @PrimaryKey val id: Long = 0,
    val value: String = "",
    val caseSensitive: Boolean = false,
    val isRegex: Boolean = false,
    val includeContacts: Boolean = false
)
