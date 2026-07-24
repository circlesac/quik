package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scheduled_message",
    indices = [
        Index("date")
    ]
)
data class ScheduledMessageEntity(
    @PrimaryKey val id: Long = 0,
    val date: Long = 0,
    val subId: Int = -1,
    val recipients: List<String> = emptyList(),
    val sendAsGroup: Boolean = true,
    val body: String = "",
    val attachments: List<String> = emptyList(),
    val conversationId: Long = 0
)
