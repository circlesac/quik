package dev.octoshrimpy.quik.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import dev.octoshrimpy.quik.data.db.entity.EmojiSyncNeededEntity
import dev.octoshrimpy.quik.data.db.entity.SyncLogEntity

@Dao
interface SyncDao {

    @Insert
    fun insertSyncLog(log: SyncLogEntity)

    @Query("SELECT MAX(date) FROM sync_log")
    fun getLastSyncDate(): Long?

    @Query("SELECT * FROM emoji_sync_needed LIMIT 1")
    fun getEmojiSyncNeeded(): EmojiSyncNeededEntity?

    @Insert
    fun insertEmojiSyncNeeded(entity: EmojiSyncNeededEntity)

    @Query("DELETE FROM emoji_sync_needed")
    fun deleteAllEmojiSyncNeeded()
}
