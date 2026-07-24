package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversation",
    indices = [
        Index("archived"),
        Index("blocked"),
        Index("pinned"),
        Index("lastMessageId")
    ]
)
data class ConversationEntity(
    @PrimaryKey val id: Long = 0,
    val archived: Boolean = false,
    val blocked: Boolean = false,
    val pinned: Boolean = false,
    val draft: String = "",
    val draftDate: Long = 0,
    val blockingClient: Int? = null,
    val blockReason: String? = null,
    val name: String = "",
    val sendAsGroup: Boolean = true,
    val lastMessageId: Long? = null
)
