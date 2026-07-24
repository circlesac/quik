package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emoji_reaction",
    indices = [
        Index("reactionMessageId"),
        Index("threadId"),
        Index("targetMessageId")
    ]
)
data class EmojiReactionEntity(
    @PrimaryKey val id: Long = 0,
    val reactionMessageId: Long = 0,
    val senderAddress: String = "",
    val emoji: String = "",
    val originalMessageText: String = "",
    val threadId: Long = 0,
    val targetMessageId: Long = 0
)
