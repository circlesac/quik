package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "conversation_recipient",
    primaryKeys = ["conversationId", "recipientId"],
    indices = [
        Index("conversationId"),
        Index("recipientId")
    ]
)
data class ConversationRecipientEntity(
    val conversationId: Long = 0,
    val recipientId: Long = 0
)
