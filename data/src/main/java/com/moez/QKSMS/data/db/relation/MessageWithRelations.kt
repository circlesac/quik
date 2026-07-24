package dev.octoshrimpy.quik.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import dev.octoshrimpy.quik.data.db.entity.EmojiReactionEntity
import dev.octoshrimpy.quik.data.db.entity.MessageEntity
import dev.octoshrimpy.quik.data.db.entity.MmsPartEntity

data class MessageWithRelations(
    @Embedded val message: MessageEntity,

    @Relation(parentColumn = "contentId", entityColumn = "messageId")
    val parts: List<MmsPartEntity> = emptyList(),

    @Relation(parentColumn = "id", entityColumn = "targetMessageId")
    val emojiReactions: List<EmojiReactionEntity> = emptyList()
)
