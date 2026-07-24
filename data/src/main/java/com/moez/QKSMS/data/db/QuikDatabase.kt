package dev.octoshrimpy.quik.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.octoshrimpy.quik.data.db.dao.BlockedNumberDao
import dev.octoshrimpy.quik.data.db.dao.ContactDao
import dev.octoshrimpy.quik.data.db.dao.ConversationDao
import dev.octoshrimpy.quik.data.db.dao.EmojiReactionDao
import dev.octoshrimpy.quik.data.db.dao.MessageContentFilterDao
import dev.octoshrimpy.quik.data.db.dao.MessageDao
import dev.octoshrimpy.quik.data.db.dao.ScheduledMessageDao
import dev.octoshrimpy.quik.data.db.dao.SyncDao
import dev.octoshrimpy.quik.data.db.entity.BlockedNumberEntity
import dev.octoshrimpy.quik.data.db.entity.ContactEntity
import dev.octoshrimpy.quik.data.db.entity.ContactGroupContactEntity
import dev.octoshrimpy.quik.data.db.entity.ContactGroupEntity
import dev.octoshrimpy.quik.data.db.entity.ConversationEntity
import dev.octoshrimpy.quik.data.db.entity.ConversationRecipientEntity
import dev.octoshrimpy.quik.data.db.entity.EmojiReactionEntity
import dev.octoshrimpy.quik.data.db.entity.EmojiSyncNeededEntity
import dev.octoshrimpy.quik.data.db.entity.MessageContentFilterEntity
import dev.octoshrimpy.quik.data.db.entity.MessageEntity
import dev.octoshrimpy.quik.data.db.entity.MmsPartEntity
import dev.octoshrimpy.quik.data.db.entity.PhoneNumberEntity
import dev.octoshrimpy.quik.data.db.entity.RecipientEntity
import dev.octoshrimpy.quik.data.db.entity.ScheduledMessageEntity
import dev.octoshrimpy.quik.data.db.entity.SyncLogEntity

@Database(
    entities = [
        MessageEntity::class,
        MmsPartEntity::class,
        EmojiReactionEntity::class,
        ConversationEntity::class,
        RecipientEntity::class,
        ContactEntity::class,
        PhoneNumberEntity::class,
        ContactGroupEntity::class,
        ScheduledMessageEntity::class,
        BlockedNumberEntity::class,
        MessageContentFilterEntity::class,
        EmojiSyncNeededEntity::class,
        SyncLogEntity::class,
        ConversationRecipientEntity::class,
        ContactGroupContactEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class QuikDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun contactDao(): ContactDao
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun messageContentFilterDao(): MessageContentFilterDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun emojiReactionDao(): EmojiReactionDao
    abstract fun syncDao(): SyncDao

    companion object {
        const val NAME = "quik.db"
    }
}
