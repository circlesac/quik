package dev.octoshrimpy.quik.data.db.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import dev.octoshrimpy.quik.data.db.entity.ConversationEntity
import dev.octoshrimpy.quik.data.db.entity.ConversationRecipientEntity
import dev.octoshrimpy.quik.data.db.entity.MessageEntity
import dev.octoshrimpy.quik.data.db.entity.RecipientEntity

data class ConversationWithRelations(
    @Embedded val conversation: ConversationEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ConversationRecipientEntity::class,
            parentColumn = "conversationId",
            entityColumn = "recipientId"
        ),
        entity = RecipientEntity::class
    )
    val recipients: List<RecipientWithContact> = emptyList(),

    @Relation(
        parentColumn = "lastMessageId",
        entityColumn = "id",
        entity = MessageEntity::class
    )
    val lastMessage: MessageWithRelations? = null
)
