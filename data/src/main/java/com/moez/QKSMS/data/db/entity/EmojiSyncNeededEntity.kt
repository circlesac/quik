package dev.octoshrimpy.quik.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "emoji_sync_needed"
)
data class EmojiSyncNeededEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
